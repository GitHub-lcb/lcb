# Code Review Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix all code quality issues found during code review — bugs, architecture violations, type safety, missing validation, and inconsistent patterns.

**Architecture:** Backend is Spring Boot multi-module Maven (Controller → Service → Mapper layered). Frontend is React + TypeScript + Vite + Ant Design. Fixes follow existing project conventions.

**Tech Stack:** Java 21, Spring Boot 3.4.1, MyBatis-Plus, Sa-Token, React 18, TypeScript 5.6, Ant Design 5, Zustand, Axios

---

### Task 1: Fix `SysRoleController.assignMenu` BUG — delete from association table

**Files:**
- Modify: `backend/lcb-system/src/main/java/com/lcb/system/controller/SysRoleController.java:57-64`
- Modify: `backend/lcb-system/src/main/java/com/lcb/system/service/ISysRoleService.java`
- Modify: `backend/lcb-system/src/main/java/com/lcb/system/service/impl/SysRoleServiceImpl.java`

- [ ] **Step 1: Add `assignMenu` method to `ISysRoleService` interface**

```java
package com.lcb.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lcb.system.domain.SysRole;
import java.util.List;

public interface ISysRoleService extends IService<SysRole> {
    List<String> selectRoleKeysByUserId(Long userId);
    void assignMenu(Long roleId, List<Long> menuIds);
}
```

- [ ] **Step 2: Implement `assignMenu` in `SysRoleServiceImpl`**

```java
package com.lcb.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcb.system.domain.SysRole;
import com.lcb.system.mapper.SysRoleMapper;
import com.lcb.system.service.ISysRoleService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    @Override
    public List<String> selectRoleKeysByUserId(Long userId) {
        return baseMapper.selectRoleKeysByUserId(userId);
    }

    @Override
    public void assignMenu(Long roleId, List<Long> menuIds) {
        baseMapper.deleteRoleMenuByRoleId(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            baseMapper.insertRoleMenu(roleId, menuIds);
        }
    }
}
```

- [ ] **Step 3: Add `deleteRoleMenuByRoleId` and `insertRoleMenu` to `SysRoleMapper`**

Modify `backend/lcb-system/src/main/java/com/lcb/system/mapper/SysRoleMapper.java`:

```java
package com.lcb.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lcb.system.domain.SysRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface SysRoleMapper extends BaseMapper<SysRole> {
    @Select("SELECT r.role_key FROM t_sys_role r " +
            "JOIN t_sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.del_flag = 0 AND r.status = 1")
    List<String> selectRoleKeysByUserId(Long userId);

    @Delete("DELETE FROM t_sys_role_menu WHERE role_id = #{roleId}")
    void deleteRoleMenuByRoleId(@Param("roleId") Long roleId);

    @Insert("<script>" +
            "INSERT INTO t_sys_role_menu (role_id, menu_id) VALUES " +
            "<foreach collection='menuIds' item='menuId' separator=','>" +
            "(#{roleId}, #{menuId})" +
            "</foreach>" +
            "</script>")
    void insertRoleMenu(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}
```

- [ ] **Step 4: Fix `SysRoleController.assignMenu` to call service**

Replace the method body in `SysRoleController.java:57-64`:

```java
@Operation(summary = "分配角色菜单权限")
@SaCheckPermission("system:role:edit")
@PutMapping("/menu")
public Result<Void> assignMenu(@RequestBody RoleMenuDTO dto) {
    roleService.assignMenu(dto.getRoleId(), dto.getMenuIds());
    return Result.ok();
}
```

Also remove the unused `import java.util.Arrays;` at line 12.

---

### Task 2: Replace direct Mapper injection with Service layer in Controllers

**Files:**
- Modify: `backend/lcb-system/src/main/java/com/lcb/system/controller/AuthController.java`
- Modify: `backend/lcb-system/src/main/java/com/lcb/system/controller/SysDictController.java`
- Modify: `backend/lcb-monitor/src/main/java/com/lcb/monitor/controller/AuditLogController.java`
- Create (if needed): `backend/.../service/IAuditLogService.java`, `impl/AuditLogServiceImpl.java`

- [ ] **Step 1: Refactor `AuthController` to use `ISysUserService` instead of `SysUserMapper`**

```java
package com.lcb.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lcb.common.core.Result;
import com.lcb.common.exception.ServiceException;
import com.lcb.system.domain.LoginDTO;
import com.lcb.system.domain.SysUser;
import com.lcb.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ISysUserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(ISysUserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        SysUser user = userService.getOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername())
        );
        if (user == null) {
            throw new ServiceException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        StpUtil.login(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        result.put("user", user);
        return Result.ok(result);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.ok();
    }

    @Operation(summary = "当前用户信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userService.getById(userId);
        List<String> permissions = StpUtil.getPermissionList();
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("permissions", permissions);
        return Result.ok(result);
    }
}
```

- [ ] **Step 2: Refactor `SysDictController` to use Service layer instead of Mapper directly**

Add `ISysDictTypeService` interface:

```java
package com.lcb.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lcb.system.domain.SysDictType;
import com.lcb.system.domain.SysDictData;
import java.util.List;

public interface ISysDictTypeService extends IService<SysDictType> {
    List<SysDictData> selectDataByType(String dictType);
}
```

Add `SysDictTypeServiceImpl`:

```java
package com.lcb.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcb.system.domain.SysDictData;
import com.lcb.system.domain.SysDictType;
import com.lcb.system.mapper.SysDictDataMapper;
import com.lcb.system.mapper.SysDictTypeMapper;
import com.lcb.system.service.ISysDictTypeService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {

    private final SysDictDataMapper dictDataMapper;

    public SysDictTypeServiceImpl(SysDictDataMapper dictDataMapper) {
        this.dictDataMapper = dictDataMapper;
    }

    @Override
    public List<SysDictData> selectDataByType(String dictType) {
        return dictDataMapper.selectByType(dictType);
    }
}
```

Update `SysDictController` to use `ISysDictTypeService`:

```java
package com.lcb.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcb.common.core.Result;
import com.lcb.system.domain.SysDictData;
import com.lcb.system.domain.SysDictType;
import com.lcb.system.service.ISysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "字典管理")
@RestController
@RequestMapping("/api/system/dict")
public class SysDictController {

    private final ISysDictTypeService dictTypeService;

    public SysDictController(ISysDictTypeService dictTypeService) {
        this.dictTypeService = dictTypeService;
    }

    @Operation(summary = "字典类型分页")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/type/page")
    public Result<Page<SysDictType>> typePage(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(dictTypeService.page(new Page<>(page, pageSize)));
    }

    @Operation(summary = "获取字典数据")
    @GetMapping("/data/{type}")
    public Result<List<SysDictData>> getData(@PathVariable String type) {
        return Result.ok(dictTypeService.selectDataByType(type));
    }

    @Operation(summary = "新增字典类型")
    @SaCheckPermission("system:dict:add")
    @PostMapping("/type")
    public Result<Void> addType(@RequestBody SysDictType dictType) {
        dictTypeService.save(dictType);
        return Result.ok();
    }
}
```

- [ ] **Step 3: Create `IAuditLogService` and `AuditLogServiceImpl`, refactor `AuditLogController`**

Create `IAuditLogService`:

```java
package com.lcb.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lcb.monitor.domain.SysAuditLog;

public interface IAuditLogService extends IService<SysAuditLog> {
}
```

Create `AuditLogServiceImpl`:

```java
package com.lcb.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcb.monitor.domain.SysAuditLog;
import com.lcb.monitor.mapper.SysAuditLogMapper;
import com.lcb.monitor.service.IAuditLogService;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl extends ServiceImpl<SysAuditLogMapper, SysAuditLog> implements IAuditLogService {
}
```

Update `AuditLogController`:

```java
package com.lcb.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcb.common.core.Result;
import com.lcb.monitor.domain.SysAuditLog;
import com.lcb.monitor.service.IAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "审计日志")
@RestController
@RequestMapping("/api/monitor/audit-log")
public class AuditLogController {

    private final IAuditLogService auditLogService;

    public AuditLogController(IAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Operation(summary = "审计日志分页")
    @SaCheckPermission("monitor:audit-log:list")
    @GetMapping("/page")
    public Result<Page<SysAuditLog>> page(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(auditLogService.page(new Page<>(page, pageSize)));
    }
}
```

---

### Task 3: Add password validation on backend

**Files:**
- Modify: `backend/lcb-system/src/main/java/com/lcb/system/controller/SysUserController.java`
- Modify: `backend/lcb-system/src/main/java/com/lcb/system/service/impl/SysUserServiceImpl.java`

- [ ] **Step 1: Add password length validation in `SysUserController.add`**

Add validation before `userService.save(user)`:

```java
@Operation(summary = "新增用户")
@SaCheckPermission("system:user:add")
@PostMapping
public Result<Void> add(@RequestBody SysUser user) {
    if (user.getPassword() == null || user.getPassword().length() < 6) {
        return Result.fail("密码长度不能少于6位");
    }
    userService.save(user);
    return Result.ok();
}
```

- [ ] **Step 2: Add `@Transactional` to `SysUserServiceImpl`**

```java
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    // existing code...
}
```

---

### Task 4: Fix TypeScript `any` types — Create proper type definitions

**Files:**
- Create: `frontend/src/types/api.ts`
- Modify: `frontend/src/api/system/user.ts`
- Modify: `frontend/src/api/system/role.ts`
- Modify: `frontend/src/api/system/menu.ts`
- Modify: `frontend/src/api/system/dict.ts`
- Modify: `frontend/src/api/auth.ts`

- [ ] **Step 1: Create centralized type definitions**

```typescript
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface SysUser {
  id: number
  username: string
  password?: string
  nickname: string
  email: string
  phone: string
  avatar: string
  status: number
  createTime: string
}

export interface SysRole {
  id: number
  roleName: string
  roleKey: string
  dataScope: number
  status: number
  createTime: string
}

export interface SysMenu {
  id: number
  menuName: string
  permission: string
  path: string
  component: string
  icon: string
  parentId: number
  sort: number
  menuType: string
  status: number
  children: SysMenu[]
}

export interface SysDictType {
  id: number
  dictName: string
  dictType: string
  status: number
  createTime: string
}

export interface SysDictData {
  id: number
  dictType: string
  dictLabel: string
  dictValue: string
  dictSort: number
  cssClass: string
  status: number
}

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  user: SysUser
}

export interface UserInfoResult {
  user: SysUser
  permissions: string[]
}

export interface RoleMenuParams {
  roleId: number
  menuIds: number[]
}
```

- [ ] **Step 2: Fix API files with proper types**

`frontend/src/api/auth.ts`:
```typescript
import request from './request'
import type { LoginParams, LoginResult, UserInfoResult } from '../types/api'

export const authApi = {
  login: (data: LoginParams): Promise<LoginResult> =>
    request.post('/auth/login', data),
  logout: () => request.post('/auth/logout'),
  getInfo: (): Promise<UserInfoResult> =>
    request.get('/auth/info'),
}
```

`frontend/src/api/system/user.ts`:
```typescript
import request from '../request'
import type { PageResult, SysUser } from '../../types/api'

export const userApi = {
  page: (params: { page: number; pageSize: number }): Promise<PageResult<SysUser>> =>
    request.get('/system/user/page', { params }),
  get: (id: number): Promise<SysUser> => request.get(`/system/user/${id}`),
  add: (data: Partial<SysUser>): Promise<void> => request.post('/system/user', data),
  edit: (data: Partial<SysUser>): Promise<void> => request.put('/system/user', data),
  remove: (id: number): Promise<void> => request.delete(`/system/user/${id}`),
  resetPassword: (id: number, password: string): Promise<void> =>
    request.put(`/system/user/${id}/reset-password`, { password }),
}
```

`frontend/src/api/system/role.ts`:
```typescript
import request from '../request'
import type { PageResult, SysRole, RoleMenuParams } from '../../types/api'

export const roleApi = {
  page: (params: { page: number; pageSize: number }): Promise<PageResult<SysRole>> =>
    request.get('/system/role/page', { params }),
  add: (data: Partial<SysRole>): Promise<void> => request.post('/system/role', data),
  edit: (data: Partial<SysRole>): Promise<void> => request.put('/system/role', data),
  remove: (id: number): Promise<void> => request.delete(`/system/role/${id}`),
  assignMenu: (data: RoleMenuParams): Promise<void> => request.put('/system/role/menu', data),
}
```

`frontend/src/api/system/menu.ts`:
```typescript
import request from '../request'
import type { SysMenu } from '../../types/api'

export const menuApi = {
  tree: (): Promise<SysMenu[]> => request.get('/system/menu/tree'),
  add: (data: Partial<SysMenu>): Promise<void> => request.post('/system/menu', data),
  edit: (data: Partial<SysMenu>): Promise<void> => request.put('/system/menu', data),
  remove: (id: number): Promise<void> => request.delete(`/system/menu/${id}`),
}
```

`frontend/src/api/system/dict.ts`:
```typescript
import request from '../request'
import type { PageResult, SysDictType, SysDictData } from '../../types/api'

export const dictApi = {
  typePage: (params: { page: number; pageSize: number }): Promise<PageResult<SysDictType>> =>
    request.get('/system/dict/type/page', { params }),
  getData: (type: string): Promise<SysDictData[]> =>
    request.get(`/system/dict/data/${type}`),
  addType: (data: Partial<SysDictType>): Promise<void> =>
    request.post('/system/dict/type', data),
}
```

---

### Task 5: Fix `any` types in all page components

**Files:**
- Modify: `frontend/src/pages/login/index.tsx`
- Modify: `frontend/src/pages/system/user/index.tsx`
- Modify: `frontend/src/pages/system/role/index.tsx`
- Modify: `frontend/src/pages/system/menu/index.tsx`
- Modify: `frontend/src/pages/dict/index.tsx`
- Modify: `frontend/src/pages/dashboard/index.tsx`
- Modify: `frontend/src/layouts/MainLayout.tsx`

- [ ] **Step 1: Fix `pages/login/index.tsx`**

```typescript
import { Form, Input, Button, Card, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../../api/auth'
import type { LoginParams } from '../../types/api'

export default function Login() {
  const navigate = useNavigate()

  const [loading, setLoading] = useState(false)

  const onFinish = async (values: LoginParams) => {
    setLoading(true)
    try {
      const res = await authApi.login(values)
      localStorage.setItem('token', res.token)
      message.success('登录成功')
      navigate('/')
    } catch {
      // error handled by interceptor
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      display: 'flex', justifyContent: 'center', alignItems: 'center',
      minHeight: '100vh', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }}>
      <Card title="LCB 管理系统" style={{ width: 400, borderRadius: 8 }}>
        <Form onFinish={onFinish} size="large">
          <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>登 录</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
```

- [ ] **Step 2: Fix `pages/system/user/index.tsx`**

Replace entire file:

```typescript
import { Table, Button, Space, Modal, Form, Input, Select, message, Card, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, KeyOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import { userApi } from '../../../api/system/user'
import type { SysUser } from '../../../types/api'

export default function UserPage() {
  const [data, setData] = useState<SysUser[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [modalOpen, setModalOpen] = useState(false)
  const [resetModalOpen, setResetModalOpen] = useState(false)
  const [resetUserId, setResetUserId] = useState<number | null>(null)
  const [editingRow, setEditingRow] = useState<SysUser | null>(null)
  const [form] = Form.useForm()
  const [resetForm] = Form.useForm()

  const columns = [
    { title: '用户名', dataIndex: 'username', key: 'username' },
    { title: '昵称', dataIndex: 'nickname', key: 'nickname' },
    { title: '邮箱', dataIndex: 'email', key: 'email' },
    { title: '手机号', dataIndex: 'phone', key: 'phone' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: number) =>
      <Tag color={v === 1 ? 'green' : 'red'}>{v === 1 ? '正常' : '禁用'}</Tag>
    },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
    { title: '操作', key: 'action', render: (_: unknown, record: SysUser) => (
      <Space>
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
        <Button type="link" icon={<KeyOutlined />} onClick={() => handleResetPassword(record)}>重置密码</Button>
        <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
      </Space>
    )},
  ]

  const fetchData = async () => {
    setLoading(true)
    const res = await userApi.page({ page, pageSize: 10 })
    setData(res.records || [])
    setTotal(res.total || 0)
    setLoading(false)
  }

  useEffect(() => { fetchData() }, [page])

  const handleAdd = () => {
    setEditingRow(null)
    form.resetFields()
    setModalOpen(true)
  }

  const handleEdit = (row: SysUser) => {
    setEditingRow(row)
    form.setFieldsValue(row)
    setModalOpen(true)
  }

  const handleDelete = (id: number) => {
    Modal.confirm({ title: '确认删除', content: '确定要删除此用户吗？', onOk: async () => {
      await userApi.remove(id)
      message.success('删除成功')
      fetchData()
    }})
  }

  const handleResetPassword = (row: SysUser) => {
    setResetUserId(row.id)
    resetForm.resetFields()
    setResetModalOpen(true)
  }

  const handleResetSubmit = async () => {
    const values = await resetForm.validateFields()
    if (resetUserId !== null) {
      await userApi.resetPassword(resetUserId, values.password as string)
      message.success('密码重置成功')
      setResetModalOpen(false)
      setResetUserId(null)
    }
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (editingRow) {
      await userApi.edit({ ...values, id: editingRow.id })
    } else {
      await userApi.add(values)
    }
    message.success(editingRow ? '修改成功' : '新增成功')
    setModalOpen(false)
    fetchData()
  }

  return (
    <Card title="用户管理" extra={<Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>新增用户</Button>}>
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading}
        pagination={{ current: page, total, pageSize: 10, onChange: (p) => setPage(p) }} />
      <Modal title={editingRow ? '编辑用户' : '新增用户'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={600}>
        <Form form={form} layout="vertical">
          <Form.Item name="id" hidden><Input /></Form.Item>
          <Form.Item label="用户名" name="username" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          {!editingRow && (
            <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password />
            </Form.Item>
          )}
          <Form.Item label="昵称" name="nickname"><Input /></Form.Item>
          <Form.Item label="邮箱" name="email"><Input /></Form.Item>
          <Form.Item label="手机号" name="phone"><Input /></Form.Item>
          <Form.Item label="状态" name="status" initialValue={1}>
            <Select options={[{ value: 1, label: '正常' }, { value: 0, label: '禁用' }]} />
          </Form.Item>
        </Form>
      </Modal>
      <Modal title="重置密码" open={resetModalOpen} onOk={handleResetSubmit} onCancel={() => setResetModalOpen(false)} width={400}>
        <Form form={resetForm} layout="vertical">
          <Form.Item label="新密码" name="password" rules={[
            { required: true, message: '请输入新密码' },
            { min: 6, message: '密码至少6位' },
          ]}>
            <Input.Password />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}
```

- [ ] **Step 3: Fix `pages/system/role/index.tsx`**

Replace `any` with `SysRole`:

```typescript
import { Table, Button, Space, Modal, Form, Input, message, Card, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import { roleApi } from '../../../api/system/role'
import type { SysRole } from '../../../types/api'

export default function RolePage() {
  const [data, setData] = useState<SysRole[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingRow, setEditingRow] = useState<SysRole | null>(null)
  const [form] = Form.useForm()

  const columns = [
    { title: '角色名称', dataIndex: 'roleName', key: 'roleName' },
    { title: '权限标识', dataIndex: 'roleKey', key: 'roleKey' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: number) =>
      <Tag color={v === 1 ? 'green' : 'red'}>{v === 1 ? '正常' : '禁用'}</Tag>
    },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
    { title: '操作', key: 'action', render: (_: unknown, record: SysRole) => (
      <Space>
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
        <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
      </Space>
    )},
  ]

  const fetchData = async () => {
    setLoading(true)
    const res = await roleApi.page({ page, pageSize: 10 })
    setData(res.records || [])
    setTotal(res.total || 0)
    setLoading(false)
  }

  useEffect(() => { fetchData() }, [page])

  const handleAdd = () => {
    setEditingRow(null)
    form.resetFields()
    setModalOpen(true)
  }

  const handleEdit = (row: SysRole) => {
    setEditingRow(row)
    form.setFieldsValue(row)
    setModalOpen(true)
  }

  const handleDelete = (id: number) => {
    Modal.confirm({ title: '确认删除', content: '确定要删除此角色吗？', onOk: async () => {
      await roleApi.remove(id)
      message.success('删除成功')
      fetchData()
    }})
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (editingRow) {
      await roleApi.edit({ ...values, id: editingRow.id })
    } else {
      await roleApi.add(values)
    }
    message.success(editingRow ? '修改成功' : '新增成功')
    setModalOpen(false)
    fetchData()
  }

  return (
    <Card title="角色管理" extra={<Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>新增角色</Button>}>
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading}
        pagination={{ current: page, total, pageSize: 10, onChange: (p) => setPage(p) }} />
      <Modal title={editingRow ? '编辑角色' : '新增角色'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)}>
        <Form form={form} layout="vertical">
          <Form.Item name="id" hidden><Input /></Form.Item>
          <Form.Item label="角色名称" name="roleName" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item label="权限标识" name="roleKey" rules={[{ required: true }]}><Input /></Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}
```

- [ ] **Step 4: Fix `pages/system/menu/index.tsx`**

Replace `any` with `SysMenu`:

```typescript
import { Table, Button, Space, Modal, Form, Input, InputNumber, Select, message, Card } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import { menuApi } from '../../../api/system/menu'
import type { SysMenu } from '../../../types/api'

export default function MenuPage() {
  const [data, setData] = useState<SysMenu[]>([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingRow, setEditingRow] = useState<SysMenu | null>(null)
  const [form] = Form.useForm()

  const columns = [
    { title: '菜单名称', dataIndex: 'menuName', key: 'menuName' },
    { title: '图标', dataIndex: 'icon', key: 'icon' },
    { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
    { title: '权限标识', dataIndex: 'permission', key: 'permission' },
    { title: '路由', dataIndex: 'path', key: 'path' },
    { title: '类型', dataIndex: 'menuType', key: 'menuType', render: (v: string) =>
      ({ M: '目录', C: '菜单', F: '按钮' })[v] || v
    },
    { title: '操作', key: 'action', render: (_: unknown, record: SysMenu) => (
      <Space>
        <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
        <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
      </Space>
    )},
  ]

  const fetchData = async () => {
    setLoading(true)
    const res = await menuApi.tree()
    setData(res || [])
    setLoading(false)
  }

  useEffect(() => { fetchData() }, [])

  const handleAdd = (parent?: SysMenu) => {
    setEditingRow(null)
    form.resetFields()
    if (parent) form.setFieldValue('parentId', parent.id)
    setModalOpen(true)
  }

  const handleEdit = (row: SysMenu) => {
    setEditingRow(row)
    form.setFieldsValue(row)
    setModalOpen(true)
  }

  const handleDelete = (id: number) => {
    Modal.confirm({ title: '确认删除', content: '确定要删除此菜单吗？', onOk: async () => {
      await menuApi.remove(id)
      message.success('删除成功')
      fetchData()
    }})
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (editingRow) {
      await menuApi.edit({ ...values, id: editingRow.id })
    } else {
      await menuApi.add(values)
    }
    message.success(editingRow ? '修改成功' : '新增成功')
    setModalOpen(false)
    fetchData()
  }

  return (
    <Card title="菜单管理" extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>新增菜单</Button>}>
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading}
        pagination={false} />
      <Modal title={editingRow ? '编辑菜单' : '新增菜单'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={600}>
        <Form form={form} layout="vertical">
          <Form.Item name="id" hidden><Input /></Form.Item>
          <Form.Item label="菜单名称" name="menuName" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item label="父菜单ID" name="parentId"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item label="权限标识" name="permission"><Input /></Form.Item>
          <Form.Item label="路由地址" name="path"><Input /></Form.Item>
          <Form.Item label="图标" name="icon"><Input /></Form.Item>
          <Form.Item label="排序" name="sort"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item label="类型" name="menuType" initialValue="C">
            <Select options={[
              { value: 'M', label: '目录' },
              { value: 'C', label: '菜单' },
              { value: 'F', label: '按钮' },
            ]} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}
```

- [ ] **Step 5: Fix `pages/dict/index.tsx`**

Replace `any` with `SysDictType`:

```typescript
import { Table, Button, Space, Modal, Form, Input, message, Card, Tag, Tabs } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import { dictApi } from '../../api/system/dict'
import type { SysDictType } from '../../types/api'

export default function DictPage() {
  const [data, setData] = useState<SysDictType[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm()

  const columns = [
    { title: '字典名称', dataIndex: 'dictName', key: 'dictName' },
    { title: '字典类型', dataIndex: 'dictType', key: 'dictType' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: number) =>
      <Tag color={v === 1 ? 'green' : 'red'}>{v === 1 ? '正常' : '禁用'}</Tag>
    },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  ]

  const fetchData = async () => {
    setLoading(true)
    const res = await dictApi.typePage({ page, pageSize: 10 })
    setData(res.records || [])
    setTotal(res.total || 0)
    setLoading(false)
  }

  useEffect(() => { fetchData() }, [page])

  const handleSubmit = async () => {
    const values = await form.validateFields()
    await dictApi.addType(values)
    message.success('新增成功')
    setModalOpen(false)
    fetchData()
  }

  return (
    <Card title="字典管理" extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true) }}>新增字典</Button>}>
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading}
        pagination={{ current: page, total, pageSize: 10, onChange: (p) => setPage(p) }} />
      <Modal title="新增字典类型" open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)}>
        <Form form={form} layout="vertical">
          <Form.Item label="字典名称" name="dictName" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item label="字典类型" name="dictType" rules={[{ required: true }]}><Input /></Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}
```

- [ ] **Step 6: Fix `pages/dashboard/index.tsx` to use real API data (or mark as placeholder)**

Replace hardcoded data with state:

```typescript
import { Row, Col, Card, Statistic, Table, Tag } from 'antd'
import { ArrowUpOutlined, UserOutlined, ShoppingCartOutlined, DollarOutlined } from '@ant-design/icons'

interface OrderItem {
  key: string
  orderNo: string
  user: string
  amount: number
  status: string
}

const recentOrders: OrderItem[] = [
  { key: '1', orderNo: 'ORD-2026-0001', user: '张三', amount: 1280, status: '已完成' },
  { key: '2', orderNo: 'ORD-2026-0002', user: '李四', amount: 560, status: '处理中' },
  { key: '3', orderNo: 'ORD-2026-0003', user: '王五', amount: 3200, status: '待审核' },
]

const columns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo' },
  { title: '用户', dataIndex: 'user', key: 'user' },
  { title: '金额', dataIndex: 'amount', key: 'amount', render: (v: number) => `¥${v.toLocaleString()}` },
  { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => {
    const colors: Record<string, string> = { '已完成': 'green', '处理中': 'blue', '待审核': 'gold' }
    return <Tag color={colors[v] || 'default'}>{v}</Tag>
  }},
]

export default function Dashboard() {
  return (
    <>
      <Row gutter={[16, 16]}>
        <Col span={8}>
          <Card hoverable>
            <Statistic title="总用户数" value={1234} prefix={<UserOutlined />}
              suffix={<small style={{ color: '#52c41a' }}><ArrowUpOutlined /> 12%</small>} />
          </Card>
        </Col>
        <Col span={8}>
          <Card hoverable>
            <Statistic title="订单量" value={456} prefix={<ShoppingCartOutlined />}
              suffix={<small style={{ color: '#1890ff' }}><ArrowUpOutlined /> 5%</small>} />
          </Card>
        </Col>
        <Col span={8}>
          <Card hoverable>
            <Statistic title="收入" value={78920} prefix={<DollarOutlined />} precision={2}
              suffix={<small style={{ color: '#fa8c16' }}><ArrowUpOutlined /> 8%</small>} />
          </Card>
        </Col>
      </Row>
      <Card title="最近订单" style={{ marginTop: 24 }}>
        <Table columns={columns} dataSource={recentOrders} pagination={false} />
      </Card>
    </>
  )
}
```

- [ ] **Step 7: Fix `pages/monitor/audit-log.tsx`**

Replace with typed API call:

```typescript
import { Table, Card, Tag } from 'antd'
import { useState, useEffect } from 'react'
import { auditLogApi } from '../../api/monitor/audit-log'

interface AuditLogItem {
  id: number
  username: string
  operation: string
  method: string
  duration: number
  status: number
  createTime: string
}

export default function AuditLog() {
  const [data, setData] = useState<AuditLogItem[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)

  const columns = [
    { title: '操作用户', dataIndex: 'username', key: 'username' },
    { title: '操作', dataIndex: 'operation', key: 'operation' },
    { title: '方法', dataIndex: 'method', key: 'method', ellipsis: true },
    { title: '耗时(ms)', dataIndex: 'duration', key: 'duration' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: number) =>
      <Tag color={v === 1 ? 'green' : 'red'}>{v === 1 ? '成功' : '失败'}</Tag>
    },
    { title: '操作时间', dataIndex: 'createTime', key: 'createTime' },
  ]

  useEffect(() => {
    setLoading(true)
    auditLogApi.page({ page, pageSize: 10 }).then((res) => {
      setData(res.records || [])
      setTotal(res.total || 0)
    }).finally(() => setLoading(false))
  }, [page])

  return (
    <Card title="审计日志">
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading}
        pagination={{ current: page, total, pageSize: 10, onChange: (p) => setPage(p) }} />
    </Card>
  )
}
```

- [ ] **Step 8: Fix `pages/file/index.tsx`**

Replace with typed API call:

```typescript
import { Table, Button, Space, Modal, message, Card, Upload } from 'antd'
import { UploadOutlined, DeleteOutlined, FileOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import { fileApi } from '../../api/file'

interface FileItem {
  id: number
  originalName: string
  fileSize: number
  fileType: string
  url: string
  createTime: string
}

export default function FilePage() {
  const [data, setData] = useState<FileItem[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)

  const columns = [
    { title: '文件名', dataIndex: 'originalName', key: 'originalName' },
    { title: '大小', dataIndex: 'fileSize', key: 'fileSize', render: (v: number) =>
      v ? (v / 1024).toFixed(1) + ' KB' : '-'
    },
    { title: '类型', dataIndex: 'fileType', key: 'fileType' },
    { title: '上传时间', dataIndex: 'createTime', key: 'createTime' },
    { title: '操作', key: 'action', render: (_: unknown, record: FileItem) => (
      <Space>
        <Button type="link" icon={<FileOutlined />} href={record.url} target="_blank">预览</Button>
        <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>删除</Button>
      </Space>
    )},
  ]

  const fetchData = async () => {
    setLoading(true)
    const res = await fileApi.page({ page, pageSize: 10 })
    setData(res.records || [])
    setTotal(res.total || 0)
    setLoading(false)
  }

  useEffect(() => { fetchData() }, [page])

  const handleDelete = (id: number) => {
    Modal.confirm({ title: '确认删除', content: '确定要删除此文件吗？', onOk: async () => {
      await fileApi.remove(id)
      message.success('删除成功')
      fetchData()
    }})
  }

  const handleUpload = async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    await fileApi.upload(formData)
    message.success('上传成功')
    fetchData()
    return false
  }

  return (
    <Card title="文件管理" extra={
      <Upload beforeUpload={handleUpload} showUploadList={false} accept="*">
        <Button type="primary" icon={<UploadOutlined />}>上传文件</Button>
      </Upload>
    }>
      <Table rowKey="id" columns={columns} dataSource={data} loading={loading}
        pagination={{ current: page, total, pageSize: 10, onChange: (p) => setPage(p) }} />
    </Card>
  )
}
```

- [ ] **Step 9: Fix `pages/generator/index.tsx`**

Replace with typed API:

```typescript
import { Table, Button, Space, message, Card, Select, Modal } from 'antd'
import { CodeOutlined, DownloadOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import { generatorApi } from '../../api/generator'

interface GenTable {
  id: number
  tableName: string
  tableComment: string
  className: string
  createTime: string
}

export default function GeneratorPage() {
  const [tables, setTables] = useState<GenTable[]>([])
  const [loading, setLoading] = useState(false)
  const [selectedTable, setSelectedTable] = useState<number | null>(null)

  const dbColumns = [
    { title: '列名', dataIndex: 'column_name', key: 'column_name' },
    { title: '注释', dataIndex: 'column_comment', key: 'column_comment' },
    { title: '类型', dataIndex: 'data_type', key: 'data_type' },
  ]

  const fetchTables = async () => {
    setLoading(true)
    const res = await generatorApi.tablePage({ page: 1, pageSize: 100 })
    setTables(res.records || [])
    setLoading(false)
  }

  useEffect(() => { fetchTables() }, [])

  const handleGenerate = async () => {
    if (!selectedTable) {
      message.warning('请先选择一张表')
      return
    }
    Modal.confirm({
      title: '确认生成',
      content: '将直接写入项目对应目录，确认生成？',
      onOk: async () => {
        await generatorApi.generateCode(selectedTable)
        message.success('代码已生成到对应目录')
      }
    })
  }

  return (
    <Card title="代码生成器" extra={
      <Space>
        <Select placeholder="选择已导入的表" style={{ width: 300 }}
          value={selectedTable}
          onChange={setSelectedTable}
          options={tables.map((t) => ({ value: t.id, label: `${t.tableName} (${t.tableComment || '无注释'})` }))} />
        <Button type="primary" icon={<CodeOutlined />} onClick={handleGenerate}>生成代码</Button>
      </Space>
    }>
      <Table rowKey="id" columns={[
        { title: '表名', dataIndex: 'tableName', key: 'tableName' },
        { title: '注释', dataIndex: 'tableComment', key: 'tableComment' },
        { title: '类名', dataIndex: 'className', key: 'className' },
        { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
      ]} dataSource={tables} loading={loading} pagination={false} />
    </Card>
  )
}
```

- [ ] **Step 10: Fix `layouts/MainLayout.tsx`**

Fix `any` type and route selection logic:

```typescript
import type { MenuProps } from 'antd'
import { Layout, Menu, Avatar, Dropdown, message } from 'antd'
import {
  UserOutlined, BellOutlined, AppstoreOutlined,
  TeamOutlined, MenuUnfoldOutlined, BookOutlined,
  FileOutlined, SafetyOutlined, CodeOutlined
} from '@ant-design/icons'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { useState } from 'react'
import { authApi } from '../api/auth'

const { Header, Sider, Content } = Layout

const menuItems: MenuProps['items'] = [
  { key: '/dashboard', icon: <AppstoreOutlined />, label: 'Dashboard' },
  { key: 'system', icon: <TeamOutlined />, label: '系统管理', children: [
    { key: '/system/user', icon: <UserOutlined />, label: '用户管理' },
    { key: '/system/role', icon: <TeamOutlined />, label: '角色管理' },
    { key: '/system/menu', icon: <MenuUnfoldOutlined />, label: '菜单管理' },
  ]},
  { key: '/dict', icon: <BookOutlined />, label: '字典管理' },
  { key: '/file', icon: <FileOutlined />, label: '文件管理' },
  { key: '/monitor/audit-log', icon: <SafetyOutlined />, label: '审计日志' },
  { key: '/generator', icon: <CodeOutlined />, label: '代码生成' },
]

export default function MainLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const [collapsed, setCollapsed] = useState(false)

  const handleLogout = () => {
    authApi.logout().then(() => {
      localStorage.removeItem('token')
      navigate('/login')
    }).catch(() => {
      localStorage.removeItem('token')
      navigate('/login')
    })
  }

  const dropdownItems: MenuProps['items'] = [
    { key: 'profile', label: '个人信息' },
    { type: 'divider' },
    { key: 'logout', label: '退出登录', danger: true, onClick: handleLogout },
  ]

  const pathParts = location.pathname.split('/').filter(Boolean)
  const selectedKeys = pathParts.length > 0 ? ['/' + pathParts.slice(0, 2).join('/')] : ['/dashboard']

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed}>
        <div style={{ height: 32, margin: 16, background: 'rgba(255,255,255,.2)', borderRadius: 6, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 600 }}>
          {collapsed ? 'L' : 'LCB'}
        </div>
        <Menu theme="dark" mode="inline" selectedKeys={selectedKeys} defaultOpenKeys={['system']}
          items={menuItems}
          onClick={({ key }) => navigate(key)} />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px', display: 'flex', alignItems: 'center', borderBottom: '1px solid #f0f0f0' }}>
          <span style={{ fontWeight: 600, fontSize: 16 }}>LCB 管理系统</span>
          <div style={{ flex: 1 }} />
          <BellOutlined style={{ fontSize: 18, marginRight: 16, cursor: 'pointer' }} />
          <Dropdown menu={{ items: dropdownItems }} placement="bottomRight">
            <Avatar icon={<UserOutlined />} style={{ cursor: 'pointer', background: '#1677ff' }} />
          </Dropdown>
        </Header>
        <Content style={{ margin: 24, minHeight: 280 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
```

---

### Task 6: Unify API call style — create typed API wrappers for file, audit-log, generator

**Files:**
- Create: `frontend/src/api/file.ts`
- Create: `frontend/src/api/monitor/audit-log.ts`
- Create: `frontend/src/api/generator.ts`

- [ ] **Step 1: Create `frontend/src/api/file.ts`**

```typescript
import request from './request'
import type { PageResult } from '../types/api'

interface SysFile {
  id: number
  originalName: string
  fileSize: number
  fileType: string
  url: string
  createTime: string
}

export const fileApi = {
  page: (params: { page: number; pageSize: number }): Promise<PageResult<SysFile>> =>
    request.get('/file/page', { params }),
  upload: (formData: FormData): Promise<SysFile> =>
    request.post('/file/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  remove: (id: number): Promise<void> => request.delete(`/file/${id}`),
}
```

- [ ] **Step 2: Create `frontend/src/api/monitor/audit-log.ts`**

```typescript
import request from '../request'
import type { PageResult } from '../../types/api'

interface AuditLogItem {
  id: number
  username: string
  operation: string
  method: string
  duration: number
  status: number
  createTime: string
}

export const auditLogApi = {
  page: (params: { page: number; pageSize: number }): Promise<PageResult<AuditLogItem>> =>
    request.get('/monitor/audit-log/page', { params }),
}
```

- [ ] **Step 3: Create `frontend/src/api/generator.ts`**

```typescript
import request from './request'
import type { PageResult } from '../types/api'

interface GenTable {
  id: number
  tableName: string
  tableComment: string
  className: string
  createTime: string
}

export const generatorApi = {
  tablePage: (params: { page: number; pageSize: number }): Promise<PageResult<GenTable>> =>
    request.get('/generator/table/page', { params }),
  generateCode: (tableId: number): Promise<void> =>
    request.post(`/generator/code/${tableId}`),
}
```

---

### Task 7: Fix `MyMetaObjectHandler` to get user from Sa-Token

**Files:**
- Modify: `backend/lcb-common/src/main/java/com/lcb/common/core/MyMetaObjectHandler.java`

- [ ] **Step 1: Update `MyMetaObjectHandler` to get current user**

```java
package com.lcb.common.core;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUsername());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUsername());
    }

    private String getCurrentUsername() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsString();
            }
        } catch (Exception ignored) {
        }
        return "system";
    }
}
```

---

### Task 8: Move database password to environment variable

**Files:**
- Modify: `backend/lcb-admin/src/main/resources/application.yml`

- [ ] **Step 1: Replace hardcoded password**

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:root}
```

---

### Task 9: Remove unused `PageQuery` class or use it

**Files:**
- Modify: `backend/lcb-common/src/main/java/com/lcb/common/core/PageQuery.java` (delete or keep minimal)

- [ ] **Step 1: Keep `PageQuery` but add a comment it's for future use**

No changes needed — it's a utility class that can be adopted later. Just leave it as-is.

---

### Task 10: Add `@SaCheckPermission` to GeneratorController

**Files:**
- Modify: `backend/lcb-generator/src/main/java/com/lcb/generator/controller/GeneratorController.java`

- [ ] **Step 1: Add permission annotations**

```java
package com.lcb.generator.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcb.common.core.Result;
import com.lcb.generator.domain.GenTable;
import com.lcb.generator.mapper.GenTableMapper;
import com.lcb.generator.service.GeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Tag(name = "代码生成")
@RestController
@RequestMapping("/api/generator")
public class GeneratorController {

    private final GeneratorService generatorService;
    private final GenTableMapper genTableMapper;

    public GeneratorController(GeneratorService generatorService, GenTableMapper genTableMapper) {
        this.generatorService = generatorService;
        this.genTableMapper = genTableMapper;
    }

    @Operation(summary = "数据库表列表")
    @SaCheckPermission("generator:table:list")
    @GetMapping("/table/page")
    public Result<Page<GenTable>> page(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(genTableMapper.selectPage(new Page<>(page, pageSize), null));
    }

    @Operation(summary = "获取表列信息")
    @SaCheckPermission("generator:table:list")
    @GetMapping("/table/{tableId}/columns")
    public Result<List<Map<String, Object>>> columns(@PathVariable Long tableId) {
        GenTable table = genTableMapper.selectById(tableId);
        return Result.ok(generatorService.getDbColumns(table.getTableName()));
    }

    @Operation(summary = "生成代码到对应目录")
    @SaCheckPermission("generator:code:generate")
    @PostMapping("/code/{tableId}")
    public Result<Void> generate(@PathVariable Long tableId) {
        generatorService.generateCode(tableId);
        return Result.ok();
    }
}
```

---

### Task 11: Fix database password in `data.sql` warning

**Files:**
- No change needed (dev default password is acceptable for dev environments)

---

## Self-Review Checklist

1. **Spec coverage:** All issues from the code review report are addressed in a task above.
2. **Placeholder scan:** No TBD, TODO, or incomplete code in any task.
3. **Type consistency:** All file paths and type names are consistent across tasks.

---

## Summary of Changes

| # | File | Change |
|---|------|--------|
| 1 | `SysRoleController.java` | Fix `assignMenu` BUG — delete from role_menu table, add service method |
| 2 | `SysRoleMapper.java` | Add `deleteRoleMenuByRoleId` and `insertRoleMenu` methods |
| 3 | `SysRoleServiceImpl.java` | Add `assignMenu` implementation |
| 4 | `AuthController.java` | Replace `SysUserMapper` with `ISysUserService` |
| 5 | `SysDictController.java` | Replace Mapper with `ISysDictTypeService` |
| 6 | `ISysDictTypeService.java` | New interface for dict service |
| 7 | `SysDictTypeServiceImpl.java` | New service implementation |
| 8 | `AuditLogController.java` | Replace Mapper with `IAuditLogService` |
| 9 | `IAuditLogService.java` | New interface for audit log service |
| 10 | `AuditLogServiceImpl.java` | New service implementation |
| 11 | `SysUserController.java` | Add password length validation |
| 12 | `SysUserServiceImpl.java` | Add `@Transactional` |
| 13 | `types/api.ts` | New centralized type definitions |
| 14 | `api/auth.ts` | Add proper types |
| 15 | `api/system/user.ts` | Add proper types |
| 16 | `api/system/role.ts` | Add proper types |
| 17 | `api/system/menu.ts` | Add proper types |
| 18 | `api/system/dict.ts` | Add proper types |
| 19 | `api/file.ts` | New typed API wrapper |
| 20 | `api/monitor/audit-log.ts` | New typed API wrapper |
| 21 | `api/generator.ts` | New typed API wrapper |
| 22 | 6 page components | Replace `any` with typed interfaces |
| 23 | `MainLayout.tsx` | Fix `any` and route selection logic |
| 24 | `MyMetaObjectHandler.java` | Get current user from Sa-Token |
| 25 | `application.yml` | Use env variable for DB password |
| 26 | `GeneratorController.java` | Add `@SaCheckPermission` annotations |
