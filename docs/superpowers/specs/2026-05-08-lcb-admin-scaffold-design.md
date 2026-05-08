# LCB 管理系统脚手架 — 设计文档

> 日期: 2026-05-08
> 状态: 草稿

## 1. 概述

LCB 是一个可复用的后台管理系统脚手架，提供开箱即用的权限管理、代码生成等核心功能。采用前后端分离架构，单仓库管理。

## 2. 技术栈

| 层 | 技术 | 版本 |
|------|------|---------|
| 前端框架 | React | 18 |
| UI 组件库 | Ant Design | 5 |
| 前端构建 | Vite | 最新 |
| 路由 | React Router | 6 |
| HTTP 请求 | Axios | 最新 |
| 后端框架 | Spring Boot | 3.x |
| JDK | JDK | 21 |
| 项目管理 | Maven | 最新 |
| ORM | MyBatis-Plus | 最新 |
| 认证鉴权 | Sa-Token | 最新 |
| 缓存 | Redis | 可选 |
| API 文档 | SpringDoc OpenAPI (Swagger) | 2 |
| 代码简化 | Lombok | 最新 |
| 数据库 | MySQL | 8.x |

## 3. 项目目录结构

```
lcb/
├── frontend/                       # Vite + React 18 + Ant Design 5
│   ├── src/
│   │   ├── api/                    # API 请求层 (axios)
│   │   ├── components/             # 通用组件
│   │   │   └── Auth.tsx            # 按钮级权限组件
│   │   ├── layouts/
│   │   │   └── MainLayout.tsx      # 侧边栏 + 顶栏 + 内容区
│   │   ├── pages/
│   │   │   ├── login/index.tsx     # 登录页
│   │   │   ├── dashboard/index.tsx # 首页
│   │   │   ├── system/
│   │   │   │   ├── user/index.tsx  # 用户管理
│   │   │   │   ├── role/index.tsx  # 角色管理
│   │   │   │   └── menu/index.tsx  # 菜单管理
│   │   │   ├── dict/index.tsx      # 字典管理
│   │   │   ├── file/index.tsx      # 文件管理
│   │   │   ├── monitor/
│   │   │   │   └── audit-log.tsx   # 审计日志
│   │   │   └── generator/
│   │   │       └── index.tsx       # 代码生成器
│   │   ├── router/index.tsx        # 路由配置
│   │   ├── store/                  # 全局状态
│   │   └── utils/                  # 工具函数
│   ├── vite.config.ts
│   └── package.json
│
├── backend/
│   ├── lcb-common/                 # 公共模块
│   │   ├── src/main/java/.../common/
│   │   │   ├── core/               # Result、PageQuery 等基础类
│   │   │   ├── domain/             # BaseEntity
│   │   │   ├── exception/          # 全局异常处理
│   │   │   └── utils/              # 工具类
│   │   └── pom.xml
│   │
│   ├── lcb-framework/              # 框架核心
│   │   ├── config/
│   │   │   ├── SaTokenConfig.java
│   │   │   └── SwaggerConfig.java
│   │   ├── security/               # Sa-Token 鉴权逻辑
│   │   └── aspect/                 # 日志切面、接口限流
│   │
│   ├── lcb-system/                 # 系统管理模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   └── domain/
│   │
│   ├── lcb-generator/              # 代码生成器
│   │   ├── controller/
│   │   ├── service/
│   │   └── templates/              # Velocity 模板
│   │
│   ├── lcb-file/                   # 文件管理模块
│   │   ├── controller/
│   │   └── service/
│   │
│   └── lcb-admin/                  # 启动入口
│       ├── src/main/java/.../LcbApplication.java
│       └── src/main/resources/
│           ├── application.yml
│           └── application-dev.yml
│
├── sql/                            # 初始化脚本
│   ├── init.sql
│   └── data.sql
│
└── pom.xml                         # 父 POM
```

## 4. 数据库设计

### 4.1 设计原则

- 不使用物理外键，关联关系由代码维护
- 表名前缀 `t_`
- 实体类名自动去除前缀（如 `t_sys_user` → `SysUser`）
- 每字段必须有中文注释
- 所有表包含公共字段

### 4.2 公共字段（BaseEntity）

所有业务表继承以下字段：

```sql
create_by    VARCHAR(64)  COMMENT '创建人'
create_time  DATETIME     DEFAULT NOW()  COMMENT '创建时间'
update_by    VARCHAR(64)  COMMENT '更新人'
update_time  DATETIME     DEFAULT NOW()  COMMENT '更新时间'
del_flag     TINYINT      DEFAULT 0      COMMENT '逻辑删除：0-未删 1-已删'
```

MyBatis-Plus 自动填充 `create_time`/`update_time`，逻辑删除通过 `@TableLogic` 处理。

### 4.3 表结构

#### 系统用户表 `t_sys_user`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 用户ID |
| username | VARCHAR(64) | 用户名 |
| password | VARCHAR(256) | 密码（加密后） |
| nickname | VARCHAR(64) | 用户昵称 |
| email | VARCHAR(128) | 邮箱地址 |
| phone | VARCHAR(20) | 手机号码 |
| avatar | VARCHAR(512) | 头像URL |
| status | TINYINT | 状态：0-禁用 1-正常 |
| + 公共字段 | | |

#### 系统角色表 `t_sys_role`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 角色ID |
| role_name | VARCHAR(64) | 角色名称 |
| role_key | VARCHAR(64) | 角色权限标识 |
| data_scope | TINYINT | 数据范围：1-全部 2-本部门 |
| status | TINYINT | 状态：0-禁用 1-正常 |
| + 公共字段 | | |

#### 用户角色关联表 `t_sys_user_role`

| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | BIGINT | 用户ID |
| role_id | BIGINT | 角色ID |
| 联合主键 | (user_id, role_id) | |

#### 系统菜单/权限表 `t_sys_menu`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 菜单ID |
| menu_name | VARCHAR(64) | 菜单名称 |
| permission | VARCHAR(128) | 权限标识（如 system:user:list） |
| path | VARCHAR(256) | 路由地址 |
| component | VARCHAR(256) | 组件路径 |
| icon | VARCHAR(64) | 菜单图标 |
| parent_id | BIGINT | 父菜单ID |
| sort | INT | 排序号 |
| menu_type | CHAR(1) | 类型：M-目录 C-菜单 F-按钮 |
| status | TINYINT | 状态：0-隐藏 1-显示 |
| + 公共字段 | | |

#### 角色菜单关联表 `t_sys_role_menu`

| 字段 | 类型 | 说明 |
|------|------|------|
| role_id | BIGINT | 角色ID |
| menu_id | BIGINT | 菜单ID |
| 联合主键 | (role_id, menu_id) | |

#### 字典类型表 `t_sys_dict_type`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 字典类型ID |
| dict_name | VARCHAR(64) | 字典名称 |
| dict_type | VARCHAR(64) | 字典类型标识（唯一） |
| status | TINYINT | 状态：0-禁用 1-正常 |
| + 公共字段 | | |

#### 字典数据表 `t_sys_dict_data`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 字典数据ID |
| dict_type | VARCHAR(64) | 所属字典类型标识 |
| dict_label | VARCHAR(64) | 字典标签 |
| dict_value | VARCHAR(128) | 字典值 |
| dict_sort | INT | 排序号 |
| css_class | VARCHAR(64) | 样式class |
| status | TINYINT | 状态：0-停用 1-正常 |
| + 公共字段 | | |

#### 审计日志表 `t_sys_audit_log`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 日志ID |
| username | VARCHAR(64) | 操作用户名 |
| operation | VARCHAR(128) | 操作描述 |
| method | VARCHAR(256) | 请求方法 |
| params | TEXT | 请求参数 |
| ip | VARCHAR(64) | 操作IP |
| duration | BIGINT | 执行耗时(ms) |
| status | TINYINT | 状态：0-失败 1-成功 |
| create_time | DATETIME | 操作时间 |

#### 文件记录表 `t_sys_file`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 文件ID |
| file_name | VARCHAR(256) | 存储文件名 |
| original_name | VARCHAR(256) | 原始文件名 |
| file_size | BIGINT | 文件大小(字节) |
| file_type | VARCHAR(32) | 文件类型(MIME) |
| url | VARCHAR(512) | 文件访问URL |
| storage_type | VARCHAR(16) | 存储方式：local-本地 oss-对象存储 |
| + 公共字段 | | |

#### 代码生成-表配置 `t_gen_table`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 生成配置ID |
| table_name | VARCHAR(128) | 数据库表名 |
| table_comment | VARCHAR(256) | 表备注 |
| class_name | VARCHAR(128) | 类名(Java驼峰，无前缀) |
| module_name | VARCHAR(64) | 所属模块名 |
| package_name | VARCHAR(128) | 包名 |
| tpl_category | VARCHAR(32) | 模板类型：crud-单表 tree-树表 |
| + 公共字段 | | |

#### 代码生成-列配置 `t_gen_table_column`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 列配置ID |
| table_id | BIGINT | 所属表配置ID |
| column_name | VARCHAR(128) | 数据库列名 |
| column_comment | VARCHAR(256) | 列备注 |
| java_type | VARCHAR(64) | Java类型 |
| java_field | VARCHAR(128) | Java字段名 |
| is_insert | TINYINT | 是否插入字段：0-否 1-是 |
| is_edit | TINYINT | 是否编辑字段 |
| is_list | TINYINT | 是否列表字段 |
| query_type | VARCHAR(32) | 查询方式：EQ/LIKE/BETWEEN |
| + 公共字段 | | |

## 5. API 接口设计

### 5.1 统一规范

- 基础路径：`/api/`
- 返回格式：`Result<T>` 包含 `code`、`msg`、`data`
- 分页参数：`page`、`pageSize`
- 分页返回：`{ total: long, records: list }`
- 认证方式：Sa-Token，请求头 `Authorization: Bearer {token}`
- 接口文档：Swagger UI (`/swagger-ui.html`)

### 5.2 接口清单

#### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 登录 |
| POST | /api/auth/logout | 退出 |
| GET | /api/auth/info | 当前用户信息+权限 |

#### 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/system/user/page | 用户分页 |
| GET | /api/system/user/{id} | 用户详情 |
| POST | /api/system/user | 新增 |
| PUT | /api/system/user | 修改 |
| DELETE | /api/system/user/{id} | 删除 |
| PUT | /api/system/user/resetPwd | 重置密码 |

#### 角色管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/system/role/page | 角色分页 |
| POST | /api/system/role | 新增 |
| PUT | /api/system/role | 修改 |
| DELETE | /api/system/role/{id} | 删除 |
| PUT | /api/system/role/menu | 分配菜单权限 |

#### 菜单管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/system/menu/tree | 菜单树 |
| POST | /api/system/menu | 新增 |
| PUT | /api/system/menu | 修改 |
| DELETE | /api/system/menu/{id} | 删除 |

#### 字典管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/system/dict/type/page | 字典类型分页 |
| GET | /api/system/dict/data/{type} | 获取字典数据 |
| POST | /api/system/dict/type | 新增字典类型 |

#### 文件管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/file/upload | 上传文件 |
| GET | /api/file/page | 文件列 |
| DELETE | /api/file/{id} | 删除文件 |

#### 审计日志

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/monitor/audit-log/page | 审计日志分页 |

#### 代码生成

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/generator/table/page | 数据库表列表 |
| GET | /api/generator/table/{tableId}/columns | 表列信息 |
| POST | /api/generator/code/{tableId} | 生成代码到对应目录 |

## 6. 前端设计

### 6.1 路由

| 路径 | 页面 | 权限标识 |
|------|------|----------|
| /login | 登录页 | 公开 |
| / | Dashboard | 需登录 |
| /system/user | 用户管理 | system:user:list |
| /system/role | 角色管理 | system:role:list |
| /system/menu | 菜单管理 | system:menu:list |
| /dict | 字典管理 | system:dict:list |
| /file | 文件管理 | system:file:list |
| /monitor/audit-log | 审计日志 | monitor:audit-log:list |
| /generator | 代码生成 | generator:code |

### 6.2 核心约定

- 所有表格页使用统一封装（分页、搜索、批量删除）
- 按钮级权限通过 `<Auth permission="xxx">` 组件控制显隐
- Axios 拦截器自动处理 Token 注入、401 跳转登录

## 7. 代码生成器设计

### 7.1 流程

```
导入数据库表 → 配置生成选项 → 执行生成 → 代码写入对应目录
```

### 7.2 生成的代码

生成器自动将代码写入项目对应目录，覆盖以下各层：

| 层 | 文件 | 目标路径 |
|----|------|----------|
| Controller | XxxController.java | backend/lcb-xxx/.../controller/ |
| Service | IXxxService.java | backend/lcb-xxx/.../service/ |
| ServiceImpl | XxxServiceImpl.java | backend/lcb-xxx/.../service/impl/ |
| Mapper | XxxMapper.java | backend/lcb-xxx/.../mapper/ |
| XML | XxxMapper.xml | backend/lcb-xxx/.../mapper/ |
| Entity | Xxx.java | backend/lcb-xxx/.../domain/ |
| Page | index.vue | frontend/src/pages/xxx/ |
| API | index.ts | frontend/src/api/xxx/ |
| SQL | menu.sql | sql/generator/ |

### 7.3 模板引擎

- 使用 Velocity 模板
- 模板位置：`lcb-generator/src/main/resources/templates/`
- 支持自定义模板覆盖

## 8. 数据流架构

```
Browser → Vite Dev Server → Axios HTTP → Spring Boot Controller
                                              ↓
                                         Service
                                              ↓
                                   Sa-Token (Auth/Redis)
                                              ↓
                                    MyBatis-Plus Mapper
                                              ↓
                                          MySQL
```

- 前端请求时，Axios 拦截器自动注入 Token
- 后端 Sa-Token 在 Controller 层通过拦截器校验登录和权限
- 字典/权限缓存通过 Redis 加速
- 审计日志通过 AOP 切面自动记录

## 9. 部署与测试

### 9.1 Docker 部署

项目根目录提供 `docker-compose.yml`，一键启动完整环境：

```yaml
services:
  mysql:     # MySQL 8.x
  redis:     # Redis 7.x
  app:       # Spring Boot 应用 (构建 jar 后)
```

### 9.2 集成测试

- `lcb-admin/src/test/` 下提供 Spring Boot 集成测试基础配置
- 使用 H2 内存数据库或 Testcontainers for MySQL
- 核心接口覆盖率不低于 80%
