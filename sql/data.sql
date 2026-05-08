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

-- 分配管理员全部权限
INSERT INTO t_sys_role_menu (role_id, menu_id)
SELECT 1, id FROM t_sys_menu;
