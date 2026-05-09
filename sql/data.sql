-- 默认管理员 (BCrypt 加密)
INSERT INTO t_sys_user (id, username, password, nickname, status)
VALUES (1, 'admin', '$2b$10$r4szzJP0/NAGjfVGynZ9Xe6xPClsLQmePXN/goNfuXR9Ip57PoUJy', '系统管理员', 1);

-- 默认角色
INSERT INTO t_sys_role (id, role_name, role_key, status)
VALUES (1, '超级管理员', 'admin', 1);

-- 分配角色
INSERT INTO t_sys_user_role (user_id, role_id) VALUES (1, 1);

-- 根菜单
INSERT INTO t_sys_menu (id, menu_name, parent_id, sort, menu_type, icon)
VALUES (1, '系统管理', 0, 1, 'M', 'SettingOutlined');
INSERT INTO t_sys_menu (id, menu_name, parent_id, sort, menu_type, icon)
VALUES (2, '系统监控', 0, 2, 'M', 'SafetyOutlined');
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type, icon, component, path)
VALUES (7, '字典管理', 'system:dict:list', 0, 3, 'C', 'BookOutlined', '/dict', '/dict');
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type, icon, component, path)
VALUES (8, '文件管理', 'system:file:list', 0, 4, 'C', 'FileOutlined', '/file', '/file');
INSERT INTO t_sys_menu (id, menu_name, parent_id, sort, menu_type, icon, component, path)
VALUES (9, '代码生成', 0, 5, 'C', 'CodeOutlined', '/generator', '/generator');

-- 系统管理子菜单
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type, component, path)
VALUES (3, '用户管理', 'system:user:list', 1, 1, 'C', '/system/user', '/system/user');
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type, component, path)
VALUES (4, '角色管理', 'system:role:list', 1, 2, 'C', '/system/role', '/system/role');
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type, component, path)
VALUES (5, '菜单管理', 'system:menu:list', 1, 3, 'C', '/system/menu', '/system/menu');

-- 监控子菜单
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type, component, path)
VALUES (6, '审计日志', 'monitor:audit-log:list', 2, 1, 'C', '/monitor/audit-log', '/monitor/audit-log');

-- ============ 按钮权限 ============

-- 用户管理按钮
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type)
VALUES (20, '用户新增', 'system:user:add', 3, 1, 'F');
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type)
VALUES (21, '用户修改', 'system:user:edit', 3, 2, 'F');
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type)
VALUES (22, '用户删除', 'system:user:remove', 3, 3, 'F');

-- 角色管理按钮
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type)
VALUES (23, '角色新增', 'system:role:add', 4, 1, 'F');
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type)
VALUES (24, '角色修改', 'system:role:edit', 4, 2, 'F');
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type)
VALUES (25, '角色删除', 'system:role:remove', 4, 3, 'F');

-- 菜单管理按钮
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type)
VALUES (26, '菜单新增', 'system:menu:add', 5, 1, 'F');
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type)
VALUES (27, '菜单修改', 'system:menu:edit', 5, 2, 'F');
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type)
VALUES (28, '菜单删除', 'system:menu:remove', 5, 3, 'F');

-- 字典管理按钮
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type)
VALUES (29, '字典新增', 'system:dict:add', 7, 1, 'F');

-- 文件管理按钮
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type)
VALUES (30, '文件上传', 'system:file:add', 8, 1, 'F');
INSERT INTO t_sys_menu (id, menu_name, permission, parent_id, sort, menu_type)
VALUES (31, '文件删除', 'system:file:remove', 8, 2, 'F');

-- 分配管理员全部权限
INSERT INTO t_sys_role_menu (role_id, menu_id)
SELECT 1, id FROM t_sys_menu;
