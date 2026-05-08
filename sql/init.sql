-- ============ 系统管理 ============

CREATE TABLE t_sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username    VARCHAR(64)   NOT NULL COMMENT '用户名',
    password    VARCHAR(256)  NOT NULL COMMENT '密码',
    nickname    VARCHAR(64)   COMMENT '用户昵称',
    email       VARCHAR(128)  COMMENT '邮箱',
    phone       VARCHAR(20)   COMMENT '手机号码',
    avatar      VARCHAR(512)  COMMENT '头像URL',
    status      TINYINT       DEFAULT 1 COMMENT '0-禁用 1-正常',
    create_by   VARCHAR(64)   COMMENT '创建人',
    create_time DATETIME      DEFAULT NOW() COMMENT '创建时间',
    update_by   VARCHAR(64)   COMMENT '更新人',
    update_time DATETIME      DEFAULT NOW() COMMENT '更新时间',
    del_flag    TINYINT       DEFAULT 0 COMMENT '0-未删 1-已删'
) COMMENT '系统用户表';

CREATE TABLE t_sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name   VARCHAR(64)   NOT NULL COMMENT '角色名称',
    role_key    VARCHAR(64)   NOT NULL COMMENT '角色权限标识',
    data_scope  TINYINT       DEFAULT 1 COMMENT '1-全部 2-本部门',
    status      TINYINT       DEFAULT 1 COMMENT '0-禁用 1-正常',
    create_by   VARCHAR(64)   COMMENT '创建人',
    create_time DATETIME      DEFAULT NOW() COMMENT '创建时间',
    update_by   VARCHAR(64)   COMMENT '更新人',
    update_time DATETIME      DEFAULT NOW() COMMENT '更新时间',
    del_flag    TINYINT       DEFAULT 0 COMMENT '0-未删 1-已删'
) COMMENT '系统角色表';

CREATE TABLE t_sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) COMMENT '用户角色关联表';

CREATE TABLE t_sys_menu (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    menu_name   VARCHAR(64)   NOT NULL COMMENT '菜单名称',
    permission  VARCHAR(128)  COMMENT '权限标识',
    path        VARCHAR(256)  COMMENT '路由地址',
    component   VARCHAR(256)  COMMENT '组件路径',
    icon        VARCHAR(64)   COMMENT '图标',
    parent_id   BIGINT        DEFAULT 0 COMMENT '父菜单ID',
    sort        INT           DEFAULT 0 COMMENT '排序号',
    menu_type   CHAR(1)       COMMENT 'M-目录 C-菜单 F-按钮',
    status      TINYINT       DEFAULT 1 COMMENT '0-隐藏 1-显示',
    create_by   VARCHAR(64)   COMMENT '创建人',
    create_time DATETIME      DEFAULT NOW() COMMENT '创建时间',
    update_by   VARCHAR(64)   COMMENT '更新人',
    update_time DATETIME      DEFAULT NOW() COMMENT '更新时间',
    del_flag    TINYINT       DEFAULT 0 COMMENT '0-未删 1-已删'
) COMMENT '系统菜单表';

CREATE TABLE t_sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) COMMENT '角色菜单关联表';

-- ============ 字典 ============

CREATE TABLE t_sys_dict_type (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典类型ID',
    dict_name   VARCHAR(64)   NOT NULL COMMENT '字典名称',
    dict_type   VARCHAR(64)   NOT NULL UNIQUE COMMENT '字典类型标识',
    status      TINYINT       DEFAULT 1 COMMENT '0-禁用 1-正常',
    create_by   VARCHAR(64)   COMMENT '创建人',
    create_time DATETIME      DEFAULT NOW() COMMENT '创建时间',
    update_by   VARCHAR(64)   COMMENT '更新人',
    update_time DATETIME      DEFAULT NOW() COMMENT '更新时间',
    del_flag    TINYINT       DEFAULT 0 COMMENT '0-未删 1-已删'
) COMMENT '字典类型表';

CREATE TABLE t_sys_dict_data (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典数据ID',
    dict_type   VARCHAR(64)   NOT NULL COMMENT '所属字典类型',
    dict_label  VARCHAR(64)   NOT NULL COMMENT '字典标签',
    dict_value  VARCHAR(128)  NOT NULL COMMENT '字典值',
    dict_sort   INT           DEFAULT 0 COMMENT '排序号',
    css_class   VARCHAR(64)   COMMENT '样式class',
    status      TINYINT       DEFAULT 1 COMMENT '0-停用 1-正常',
    create_by   VARCHAR(64)   COMMENT '创建人',
    create_time DATETIME      DEFAULT NOW() COMMENT '创建时间',
    update_by   VARCHAR(64)   COMMENT '更新人',
    update_time DATETIME      DEFAULT NOW() COMMENT '更新时间',
    del_flag    TINYINT       DEFAULT 0 COMMENT '0-未删 1-已删'
) COMMENT '字典数据表';

-- ============ 审计日志 ============

CREATE TABLE t_sys_audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    username    VARCHAR(64)   COMMENT '操作用户名',
    operation   VARCHAR(128)  COMMENT '操作描述',
    method      VARCHAR(256)  COMMENT '请求方法',
    params      TEXT          COMMENT '请求参数',
    ip          VARCHAR(64)   COMMENT '操作IP',
    duration    BIGINT        COMMENT '耗时(ms)',
    status      TINYINT       DEFAULT 1 COMMENT '0-失败 1-成功',
    create_time DATETIME      DEFAULT NOW() COMMENT '操作时间'
) COMMENT '审计日志表';

-- ============ 文件 ============

CREATE TABLE t_sys_file (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文件ID',
    file_name     VARCHAR(256)  NOT NULL COMMENT '存储文件名',
    original_name VARCHAR(256)  NOT NULL COMMENT '原始文件名',
    file_size     BIGINT        COMMENT '文件大小(字节)',
    file_type     VARCHAR(32)   COMMENT '文件类型',
    url           VARCHAR(512)  COMMENT '访问URL',
    storage_type  VARCHAR(16)   COMMENT 'local-本地 oss-对象存储',
    create_by     VARCHAR(64)   COMMENT '创建人',
    create_time   DATETIME      DEFAULT NOW() COMMENT '创建时间',
    update_by     VARCHAR(64)   COMMENT '更新人',
    update_time   DATETIME      DEFAULT NOW() COMMENT '更新时间',
    del_flag      TINYINT       DEFAULT 0 COMMENT '0-未删 1-已删'
) COMMENT '文件记录表';

-- ============ 代码生成 ============

CREATE TABLE t_gen_table (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '生成配置ID',
    table_name    VARCHAR(128)  NOT NULL COMMENT '数据库表名',
    table_comment VARCHAR(256)  COMMENT '表备注',
    class_name    VARCHAR(128)  COMMENT '类名',
    module_name   VARCHAR(64)   COMMENT '所属模块名',
    package_name  VARCHAR(128)  COMMENT '包名',
    tpl_category  VARCHAR(32)   DEFAULT 'crud' COMMENT 'crud-单表 tree-树表',
    create_by     VARCHAR(64)   COMMENT '创建人',
    create_time   DATETIME      DEFAULT NOW() COMMENT '创建时间',
    update_by     VARCHAR(64)   COMMENT '更新人',
    update_time   DATETIME      DEFAULT NOW() COMMENT '更新时间',
    del_flag      TINYINT       DEFAULT 0 COMMENT '0-未删 1-已删'
) COMMENT '代码生成-表配置';

CREATE TABLE t_gen_table_column (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '列配置ID',
    table_id      BIGINT        NOT NULL COMMENT '所属表配置ID',
    column_name   VARCHAR(128)  NOT NULL COMMENT '数据库列名',
    column_comment VARCHAR(256) COMMENT '列备注',
    java_type     VARCHAR(64)   COMMENT 'Java类型',
    java_field    VARCHAR(128)  COMMENT 'Java字段名',
    is_insert     TINYINT       DEFAULT 1 COMMENT '0-否 1-是',
    is_edit       TINYINT       DEFAULT 1 COMMENT '0-否 1-是',
    is_list       TINYINT       DEFAULT 1 COMMENT '0-否 1-是',
    query_type    VARCHAR(32)   DEFAULT 'EQ' COMMENT 'EQ/LIKE/BETWEEN',
    create_by     VARCHAR(64)   COMMENT '创建人',
    create_time   DATETIME      DEFAULT NOW() COMMENT '创建时间',
    update_by     VARCHAR(64)   COMMENT '更新人',
    update_time   DATETIME      DEFAULT NOW() COMMENT '更新时间',
    del_flag      TINYINT       DEFAULT 0 COMMENT '0-未删 1-已删'
) COMMENT '代码生成-列配置';
