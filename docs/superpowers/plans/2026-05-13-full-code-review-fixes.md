# Full Code Review Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use subagent-driven-development (recommended) or executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix all 21 issues from the full codebase code review across backend, frontend, and database layers.

**Architecture:** Multi-module Maven backend (Spring Boot 3.4.1, Java 21) + Vite/React/TypeScript frontend (React 18, Ant Design 5). Fixes span security (hardcoded passwords, Swagger auth), critical bugs (audit log not recording failures, test assertion wrong), code quality (password validation dedup, @Transactional scope, dead code), DB indexing, frontend missing features, and config gaps.

**Tech Stack:** Java 21, Spring Boot 3.4.1, MyBatis-Plus 3.5.7, Sa-Token 1.39.0, React 18, Ant Design 5, TypeScript 5.6

---

### Task 1: Fix audit log uses numeric userId instead of username

**Files:**
- Modify: `backend/lcb-monitor/src/main/java/com/lcb/monitor/aspect/AuditLogAspect.java`
- Modify: `backend/lcb-monitor/src/main/java/com/lcb/monitor/service/IAuditLogService.java`
- Modify: `backend/lcb-monitor/src/main/java/com/lcb/monitor/service/impl/AuditLogServiceImpl.java`

**Problem:** `StpUtil.getLoginIdAsString()` returns the numeric user ID, not the username. The `SysAuditLog.username` column stores meaningless numbers.

- [ ] **Step 1: Add username resolution to AuditLogServiceImpl**

Edit `AuditLogServiceImpl.java`:

```java
package com.lcb.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcb.monitor.domain.SysAuditLog;
import com.lcb.monitor.mapper.SysAuditLogMapper;
import com.lcb.monitor.service.IAuditLogService;
import com.lcb.system.domain.SysUser;
import com.lcb.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl extends ServiceImpl<SysAuditLogMapper, SysAuditLog> implements IAuditLogService {

    private final SysAuditLogMapper auditLogMapper;
    private final SysUserMapper sysUserMapper;

    public AuditLogServiceImpl(SysAuditLogMapper auditLogMapper, SysUserMapper sysUserMapper) {
        super(auditLogMapper);
        this.auditLogMapper = auditLogMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public String resolveUsername(Long userId) {
        if (userId == null) return "anonymous";
        SysUser user = sysUserMapper.selectById(userId);
        return user != null ? user.getUsername() : String.valueOf(userId);
    }
}
```

- [ ] **Step 2: Add resolveUsername to IAuditLogService**

Edit `IAuditLogService.java`:

```java
package com.lcb.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lcb.monitor.domain.SysAuditLog;

public interface IAuditLogService extends IService<SysAuditLog> {
    String resolveUsername(Long userId);
}
```

- [ ] **Step 3: Fix AuditLogAspect to use service + username resolution**

Edit `AuditLogAspect.java` to replace direct mapper injection with service, and resolve username:

```java
package com.lcb.monitor.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.lcb.monitor.domain.SysAuditLog;
import com.lcb.monitor.service.IAuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {

    private final IAuditLogService auditLogService;

    public AuditLogAspect(IAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("execution(* com.lcb.*.controller.*.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) pjp.getSignature();

        SysAuditLog log = new SysAuditLog();
        try {
            long userId = StpUtil.getLoginIdAsLong();
            log.setUsername(auditLogService.resolveUsername(userId));
        } catch (Exception e) {
            log.setUsername("anonymous");
        }
        log.setOperation(signature.getMethod().getName());
        log.setMethod(signature.getDeclaringTypeName() + "." + signature.getMethod().getName());

        try {
            Object result = pjp.proceed();
            log.setDuration(System.currentTimeMillis() - start);
            log.setStatus(1);
            return result;
        } catch (Throwable e) {
            log.setDuration(System.currentTimeMillis() - start);
            log.setStatus(0);
            throw e;
        } finally {
            auditLogService.save(log);
        }
    }
}
```

- [ ] **Step 4: Verify compilation**

```bash
cd backend
mvn compile -q
```

---

### Task 2: Fix login permissions not fetched after login

**Files:**
- Modify: `frontend/src/pages/login/index.tsx`
- Modify: `frontend/src/store/index.ts`
- Modify: `frontend/src/pages/dashboard/index.tsx` (add initial permission fetch)

**Problem:** After login, only the token is stored. The `/api/auth/info` endpoint is never called, so `permissions` in Zustand stays empty and `<Auth>` component always hides children.

- [ ] **Step 1: Add userInfo fetch to store**

Edit `frontend/src/store/index.ts`:

```typescript
import { create } from 'zustand'
import { authApi } from '@/api/auth'

interface AppState {
  permissions: string[]
  setPermissions: (perms: string[]) => void
  fetchUserInfo: () => Promise<void>
  logout: () => void
}

export const useStore = create<AppState>((set) => ({
  permissions: [],
  setPermissions: (permissions) => set({ permissions }),
  fetchUserInfo: async () => {
    try {
      const res = await authApi.getInfo()
      set({ permissions: res.permissions || [] })
    } catch {
      set({ permissions: [] })
    }
  },
  logout: () => {
    localStorage.removeItem('token')
    set({ permissions: [] })
  },
}))
```

- [ ] **Step 2: Update login page to fetch permissions**

Edit `frontend/src/pages/login/index.tsx`:

```typescript
import { useStore } from '@/store'

export default function Login() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const fetchUserInfo = useStore((s) => s.fetchUserInfo)

  const onFinish = async (values: LoginParams) => {
    setLoading(true)
    try {
      const res = await authApi.login(values)
      localStorage.setItem('token', res.token)
      await fetchUserInfo()
      message.success('登录成功')
      navigate('/')
    } catch {
      // error handled by interceptor
    } finally {
      setLoading(false)
    }
  }

  // rest unchanged
}
```

- [ ] **Step 3: Fetch permissions on Dashboard mount (if already logged in)**

Edit `frontend/src/pages/dashboard/index.tsx` to add permission init on mount. Actually, better approach: add a root-level init in `MainLayout.tsx` since it wraps all authenticated routes.

Edit `frontend/src/layouts/MainLayout.tsx`:

```typescript
import { useEffect } from 'react'
import { useStore } from '@/store'

export default function MainLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const [collapsed, setCollapsed] = useState(false)
  const fetchUserInfo = useStore((s) => s.fetchUserInfo)
  const permissions = useStore((s) => s.permissions)
  const logout = useStore((s) => s.logout)

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) {
      navigate('/login')
      return
    }
    if (permissions.length === 0) {
      fetchUserInfo().catch(() => {
        logout()
        navigate('/login')
      })
    }
  }, [])

  const handleLogout = () => {
    authApi.logout().then(() => {
      logout()
      navigate('/login')
    }).catch(() => {
      logout()
      navigate('/login')
    })
  }

  // rest unchanged
}
```

- [ ] **Step 4: Verify frontend builds**

```bash
cd frontend
npx tsc --noEmit
```

---

### Task 3: Fix GlobalExceptionHandler and add validation for empty string password in edit

**Files:**
- Modify: `backend/lcb-system/src/main/java/com/lcb/system/controller/SysUserController.java`
- Modify: `backend/lcb-system/src/main/java/com/lcb/system/service/impl/SysUserServiceImpl.java`

**Problem:** Password validation is duplicated in 3 controller methods. Also, if someone sends password as empty string in edit, MyBatis-Plus's NOT_NULL strategy won't skip it (empty string != null) and an empty string gets saved (which gets BCrypt-encoded to a valid hash, but then the user can't login with their original password).

- [ ] **Step 1: Move password validation to SysUserServiceImpl**

Edit `SysUserServiceImpl.java`:

```java
@Override
public boolean save(SysUser entity) {
    if (entity.getPassword() == null || entity.getPassword().length() < 6) {
        throw new IllegalArgumentException("密码长度不能少于6位");
    }
    entity.setPassword(passwordEncoder.encode(entity.getPassword()));
    return super.save(entity);
}

@Override
public boolean updateById(SysUser entity) {
    // Only update password if explicitly provided and valid
    if (entity.getPassword() != null) {
        if (entity.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
    } else {
        // MyBatis-Plus default UpdateStrategy.NOT_NULL skips null fields
        // but also un-set the password field in the entity to be explicit
    }
    return super.updateById(entity);
}
```

- [ ] **Step 2: Remove password validation from SysUserController**

Remove all `dto.getPassword() == null || dto.getPassword().length() < 6` checks from `SysUserController.java`:

```java
@PostMapping
public Result<Void> add(@RequestBody SysUserDTO dto) {
    SysUser user = new SysUser();
    user.setUsername(dto.getUsername());
    user.setPassword(dto.getPassword());
    // ... rest
    userService.save(user);
    return Result.ok();
}

@PutMapping
public Result<Void> edit(@RequestBody SysUserDTO dto) {
    SysUser user = new SysUser();
    user.setId(dto.getId());
    user.setUsername(dto.getUsername());
    user.setPassword(dto.getPassword());
    // ... rest
    userService.updateById(user);
    return Result.ok();
}
```

- [ ] **Step 3: Add IllegalArgumentException handler to GlobalExceptionHandler**

Edit `GlobalExceptionHandler.java`:

```java
@ExceptionHandler(IllegalArgumentException.class)
public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
    log.warn("参数校验失败: {}", e.getMessage());
    return Result.fail(e.getMessage());
}
```

- [ ] **Step 4: Verify compilation**

```bash
cd backend
mvn compile -q
```

---

### Task 4: Reduce @Transactional scope to write operations only

**Files:**
- Modify: `backend/lcb-system/src/main/java/com/lcb/system/service/impl/SysUserServiceImpl.java`

**Problem:** `@Transactional` on the class wraps everything including reads. Should use `readOnly = true` at class level.

- [ ] **Step 1: Change class-level @Transactional to readOnly**

```java
@Service
@Transactional(readOnly = true)
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Override
    @Transactional
    public boolean save(SysUser entity) { ... }

    @Override
    @Transactional
    public boolean updateById(SysUser entity) { ... }
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd backend
mvn compile -q
```

---

### Task 5: Extract pagination VO conversion utility

**Files:**
- Add: `backend/lcb-common/src/main/java/com/lcb/common/core/PageUtils.java`
- Modify: `backend/lcb-system/src/main/java/com/lcb/system/controller/SysUserController.java`
- Modify: `backend/lcb-system/src/main/java/com/lcb/system/controller/SysRoleController.java`
- Modify: `backend/lcb-monitor/src/main/java/com/lcb/monitor/controller/AuditLogController.java`
- Modify: `backend/lcb-file/src/main/java/com/lcb/file/controller/FileController.java`

**Problem:** The same 8-line Entity-to-VO page conversion is duplicated in 5 controllers.

- [ ] **Step 1: Create PageUtils utility class**

Create `backend/lcb-common/src/main/java/com/lcb/common/core/PageUtils.java`:

```java
package com.lcb.common.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageUtils {

    public static <E, V> Page<V> convert(IPage<E> entityPage, Function<E, V> converter) {
        Page<V> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
            .map(converter)
            .collect(Collectors.toList()));
        return voPage;
    }
}
```

- [ ] **Step 2: Replace all page conversions with PageUtils.convert**

Each controller changes from:
```java
Page<SysUser> entityPage = userService.page(new Page<>(page, pageSize));
Page<SysUserVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
voPage.setRecords(entityPage.getRecords().stream()
    .map(SysUserVO::fromEntity)
    .collect(Collectors.toList()));
return Result.ok(voPage);
```

To:
```java
Page<SysUser> entityPage = userService.page(new Page<>(page, pageSize));
return Result.ok(PageUtils.convert(entityPage, SysUserVO::fromEntity));
```

Affected files:
- `SysUserController.java:35-40`
- `SysRoleController.java:32-37`
- `SysDictController.java:33-38`
- `AuditLogController.java:30-35`
- `FileController.java:38-43`
- `GeneratorController.java:35-40`

- [ ] **Step 3: Verify compilation**

```bash
cd backend
mvn compile -q
```

---

### Task 6: Fix GeneratorService direct Mapper injection in Controller

**Files:**
- Modify: `backend/lcb-generator/src/main/java/com/lcb/generator/controller/GeneratorController.java`
- Modify: `backend/lcb-generator/src/main/java/com/lcb/generator/service/GeneratorService.java`

**Problem:** `GenTableMapper` is injected directly into `GeneratorController` (architecture violation — bypasses service layer).

- [ ] **Step 1: Move selectPage logic to GeneratorService**

Add to `GeneratorService.java`:

```java
public Page<GenTable> selectTablePage(Page<GenTable> page) {
    return genTableMapper.selectPage(page, null);
}

public GenTable getTableById(Long tableId) {
    return genTableMapper.selectById(tableId);
}
```

- [ ] **Step 2: Remove GenTableMapper from GeneratorController**

Edit `GeneratorController.java`: remove `GenTableMapper genTableMapper` field and constructor param. Replace calls with `generatorService` calls:

```java
@GetMapping("/table/page")
public Result<Page<GenTableVO>> page(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int pageSize) {
    Page<GenTable> entityPage = generatorService.selectTablePage(new Page<>(page, pageSize));
    return Result.ok(PageUtils.convert(entityPage, GenTableVO::fromEntity));
}

@GetMapping("/table/{tableId}/columns")
public Result<List<Map<String, Object>>> columns(@PathVariable Long tableId) {
    GenTable table = generatorService.getTableById(tableId);
    return Result.ok(generatorService.getDbColumns(table.getTableName()));
}
```

- [ ] **Step 3: Verify compilation**

```bash
cd backend
mvn compile -q
```

---

### Task 7: Make VelocityEngine a singleton bean in GeneratorService

**Files:**
- Modify: `backend/lcb-generator/src/main/java/com/lcb/generator/service/GeneratorService.java`

**Problem:** `VelocityEngine` is created on every `generateCode()` call — expensive (loads templates, initializes runtime).

- [ ] **Step 1: Make VelocityEngine a field initialized once**

Add to `GeneratorService.java`:

```java
import jakarta.annotation.PostConstruct;

// Add field
private VelocityEngine velocityEngine;

@PostConstruct
public void init() {
    velocityEngine = new VelocityEngine();
    velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
    velocityEngine.setProperty("resource.loader.classpath.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    velocityEngine.init();
}
```

Remove the VelocityEngine creation lines from `generateCode()` and use the field:
```java
// Remove these from generateCode():
// VelocityEngine ve = new VelocityEngine();
// ve.setProperty(...)
// ve.init();

// Change to:
org.apache.velocity.Template tpl = velocityEngine.getTemplate("templates/" + entry.getKey(), "UTF-8");
```

- [ ] **Step 2: Verify compilation**

```bash
cd backend
mvn compile -q
```

---

### Task 8: Make upload directory configurable via @Value

**Files:**
- Modify: `backend/lcb-file/src/main/java/com/lcb/file/service/impl/FileServiceImpl.java`

**Problem:** Upload directory `"upload"` is hardcoded.

- [ ] **Step 1: Add @Value for uploadDir**

```java
@Value("${file.upload-dir:upload}")
private String uploadDir;
```

Remove the field initializer `private final String uploadDir = "upload";`.

- [ ] **Step 2: Verify compilation**

```bash
cd backend
mvn compile -q
```

---

### Task 9: Add Swagger security scheme for Sa-Token

**Files:**
- Modify: `backend/lcb-framework/src/main/java/com/lcb/framework/config/SwaggerConfig.java`

**Problem:** Swagger UI has no way to pass Sa-Token auth header, so authenticated endpoints can't be tested from Swagger.

- [ ] **Step 1: Add security scheme to OpenAPI config**

```java
package com.lcb.framework.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Sa-Token";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("LCB 管理系统 API")
                .version("1.0.0")
                .description("LCB 管理系统脚手架接口文档")
                .license(new License().name("Apache 2.0")))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .description("Sa-Token 认证，输入 token 值即可（不需要 Bearer 前缀）")))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd backend
mvn compile -q
```

---

### Task 10: Add database indexes to init.sql

**Files:**
- Modify: `sql/init.sql`

**Problem:** `t_sys_audit_log` has no indexes. `t_sys_user_role` and `t_sys_role_menu` have only composite PK indexes but are also queried by single columns.

- [ ] **Step 1: Add indexes**

Add after table creation statements:

```sql
-- Audit log indexes
CREATE INDEX idx_audit_log_create_time ON t_sys_audit_log (create_time);
CREATE INDEX idx_audit_log_username ON t_sys_audit_log (username);

-- User-Role association indexes (for single-column lookups)
CREATE INDEX idx_user_role_role_id ON t_sys_user_role (role_id);

-- Role-Menu association indexes (for single-column lookups)
CREATE INDEX idx_role_menu_menu_id ON t_sys_role_menu (menu_id);

-- Dict data lookup
CREATE INDEX idx_dict_data_type ON t_sys_dict_data (dict_type);

-- File indexes
CREATE INDEX idx_sys_file_create_time ON t_sys_file (create_time);

-- Generator table indexes
CREATE INDEX idx_gen_table_column_table_id ON t_gen_table_column (table_id);
```

- [ ] **Step 2: Verify SQL syntax**

```bash
# Read the file to verify SQL looks correct
```

---

### Task 11: Fix test assertion (unauthenticated should fail, not return 200)

**Files:**
- Modify: `backend/lcb-admin/src/test/java/com/lcb/admin/controller/UserControllerTest.java`
- Modify: `backend/lcb-admin/src/test/java/com/lcb/admin/LcbApplicationTests.java`

**Problem:** The only test asserts that an unauthenticated request to a protected endpoint returns 200 OK, which is wrong — it should return 401.

- [ ] **Step 1: Fix test to expect 401**

```java
package com.lcb.admin.controller;

import com.lcb.admin.LcbApplicationTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
class UserControllerTest extends LcbApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void testUserPageUnauthenticated() throws Exception {
        mvc.perform(get("/api/system/user/page"))
            .andExpect(status().isUnauthorized())  // 401, not 200
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.msg").value("未登录，请先登录"));
    }
}
```

- [ ] **Step 2: Add test for login success**

```java
@Test
void testLoginSuccess() throws Exception {
    String body = """
        {"username": "admin", "password": "admin123"}
        """;
    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/login")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
}
```

- [ ] **Step 3: Run test and verify**

```bash
cd backend
mvn test -pl lcb-admin -Dtest=UserControllerTest -Dspring.profiles.active=dev
```

---

### Task 12: Add CORS configuration for Spring Boot

**Files:**
- Add: `backend/lcb-framework/src/main/java/com/lcb/framework/config/CorsConfig.java`

**Problem:** No CORS config exists. Frontend served from a different origin will fail cross-origin requests.

- [ ] **Step 1: Create CorsConfig**

```java
package com.lcb.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd backend
mvn compile -q
```

---

### Task 13: Fix frontend Auth component React import

**Files:**
- Modify: `frontend/src/components/Auth.tsx`

**Problem:** `React.ReactNode` is used but `React` is not imported.

- [ ] **Step 1: Fix import**

```typescript
import type { ReactNode } from 'react'
import { useStore } from '@/store'

interface AuthProps {
  permission: string
  children: ReactNode
}

export default function Auth({ permission, children }: AuthProps) {
  const permissions = useStore((s) => s.permissions)
  if (!permissions.includes(permission)) return null
  return <>{children}</>
}
```

- [ ] **Step 2: Verify TypeScript**

```bash
cd frontend
npx tsc --noEmit
```

---

### Task 14: Clean up dead PageQuery class

**Files:**
- Modify: `backend/lcb-common/src/main/java/com/lcb/common/core/PageQuery.java`

**Problem:** `PageQuery` is defined but never used anywhere.

- [ ] **Step 1: Verify it's truly unused then remove**

First grep to confirm:
```bash
rg "PageQuery" --type java
```

If no references besides the definition, delete the file or add a @Deprecated comment.

- [ ] **Step 2 (conditional): If unused, delete the file**

```bash
rm backend/lcb-common/src/main/java/com/lcb/common/core/PageQuery.java
```

Recompile:
```bash
cd backend
mvn compile -q
```

---

### Task 15: Restrict AuditLogAspect to write operations only

**Files:**
- Modify: `backend/lcb-monitor/src/main/java/com/lcb/monitor/aspect/AuditLogAspect.java`

**Problem:** All controller methods including GET/query are audited, flooding the log table with noise.

- [ ] **Step 1: Change pointcut to only intercept methods with @PostMapping, @PutMapping, @DeleteMapping**

```java
@Around("execution(* com.lcb.*.controller.*.*(..)) && " +
        "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
        " @annotation(org.springframework.web.bind.annotation.PutMapping) || " +
        " @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
public Object around(ProceedingJoinPoint pjp) throws Throwable {
    // ... same implementation as Task 1+2 combined
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd backend
mvn compile -q
```

---

### Task 16: Fix hardcoded dev database password

**Files:**
- Modify: `backend/lcb-admin/src/main/resources/application-dev.yml`

**Problem:** Plaintext `password: 123456` in dev profile.

- [ ] **Step 1: Replace with environment variable reference**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lcb?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD:123456}
  data:
    redis:
      host: localhost
      port: 6379

logging:
  level:
    com.lcb: debug
```

---

## Summary of All Changes

| Task | Issue | Severity | Files Changed |
|------|-------|----------|---------------|
| 1 | Audit log uses userId | P1 | 3 |
| 2 | Login doesn't fetch permissions | P2 | 4 |
| 3 | Password validation fragile | P2 | 2 |
| 4 | @Transactional scope too broad | P2 | 1 |
| 5 | Duplicated page conversion code | P3 | 6 |
| 6 | Direct Mapper in Controller | P2 | 2 |
| 7 | VelocityEngine created per request | P3 | 1 |
| 8 | Upload directory hardcoded | P3 | 1 |
| 9 | Swagger no auth scheme | P2 | 1 |
| 10 | Missing DB indexes | P2 | 1 |
| 11 | Test assertion wrong | P1 | 2 |
| 12 | Missing CORS config | P3 | 1 |
| 13 | React missing import | P2 | 1 |
| 14 | Dead code PageQuery | P3 | 1 |
| 15 | Audit logs too noisy | P2 | 1 |
| 16 | Hardcoded dev password | P2 | 1 |
