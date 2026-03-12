-- 迁移脚本 v1.1
-- 修复权限管理功能：添加缺失的 sys:perm:* 权限码，修复菜单权限标识
-- 执行时间：适用于已初始化数据库的实例

-- 1. 添加权限管理相关权限条目
INSERT INTO sys_permission (id, perm_name, perm_code, resource_type, resource_url, method, description) VALUES
(70, '权限列表', 'sys:perm:list', 'menu', '/api/v1/permissions', 'GET', '查看权限列表'),
(71, '权限新增', 'sys:perm:add', 'button', '/api/v1/permissions', 'POST', '新增权限'),
(72, '权限编辑', 'sys:perm:edit', 'button', '/api/v1/permissions/*', 'PUT', '编辑权限'),
(73, '权限删除', 'sys:perm:delete', 'button', '/api/v1/permissions/*', 'DELETE', '删除权限')
ON CONFLICT (id) DO NOTHING;

-- 更新序列
SELECT setval('sys_permission_id_seq', GREATEST((SELECT MAX(id) FROM sys_permission), 73));

-- 2. 给超级管理员角色分配新权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 70), (1, 71), (1, 72), (1, 73)
ON CONFLICT DO NOTHING;

-- 3. 修复权限管理菜单的 perms 字段（从 sys:permission:list 改为 sys:perm:list）
UPDATE sys_menu SET perms = 'sys:perm:list' WHERE menu_code = 'system-permission' AND perms = 'sys:permission:list';
