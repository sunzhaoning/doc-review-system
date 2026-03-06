-- 初始化数据脚本
-- 创建时间：2026-03-05

-- ========================================
-- 1. 初始化部门数据
-- ========================================

INSERT INTO sys_dept (id, parent_id, dept_name, dept_code, leader, phone, sort, status) VALUES
(1, 0, '总公司', 'HQ', '管理员', '10000', 0, 1),
(2, 1, '研发部', 'RD', '张三', '10001', 1, 1),
(3, 1, '产品部', 'PM', '李四', '10002', 2, 1),
(4, 1, '测试部', 'QA', '王五', '10003', 3, 1);

SELECT setval('sys_dept_id_seq', (SELECT MAX(id) FROM sys_dept));

-- ========================================
-- 2. 初始化菜单数据
-- ========================================

-- 一级菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, perms, icon, sort, visible, status) VALUES
(1, 0, '首页', 'dashboard', 2, '/dashboard', 'views/Dashboard.vue', '', 'HomeFilled', 0, 1, 1),
(2, 0, '文档管理', 'document', 1, '/document', '', '', 'Document', 1, 1, 1),
(3, 0, '评审管理', 'review', 1, '/review', '', '', 'Edit', 2, 1, 1),
(4, 0, '归档检索', 'archive', 2, '/archive', 'views/archive/Archive.vue', 'archive:list', 'FolderOpened', 3, 1, 1),
(5, 0, '系统管理', 'system', 1, '/system', '', '', 'Setting', 4, 1, 1);

-- 二级菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, perms, icon, sort, visible, status) VALUES
(6, 2, '我的文档', 'document-list', 2, '/document/list', 'views/document/DocumentList.vue', 'doc:list', 'Files', 0, 1, 1),
(7, 2, '上传文档', 'document-upload', 2, '/document/upload', 'views/document/DocumentUpload.vue', 'doc:create', 'Upload', 1, 1, 1),
(8, 3, '待评审', 'review-pending', 2, '/review/pending', 'views/review/ReviewPending.vue', 'review:list', 'Clock', 0, 1, 1),
(9, 3, '评审历史', 'review-history', 2, '/review/history', 'views/review/ReviewHistory.vue', 'review:history', 'Finished', 1, 1, 1),
(10, 5, '用户管理', 'system-user', 2, '/system/user', 'views/system/UserManage.vue', 'sys:user:list', 'User', 0, 1, 1),
(11, 5, '角色管理', 'system-role', 2, '/system/role', 'views/system/RoleManage.vue', 'sys:role:list', 'UserFilled', 1, 1, 1),
(12, 5, '菜单管理', 'system-menu', 2, '/system/menu', 'views/system/MenuManage.vue', 'sys:menu:list', 'Menu', 2, 1, 1),
(13, 5, '权限管理', 'system-permission', 2, '/system/permission', 'views/system/PermissionManage.vue', 'sys:permission:list', 'Lock', 3, 1, 1);

-- 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, perms, icon, sort, visible, status) VALUES
-- 文档管理按钮
(100, 6, '查看文档', 'doc-query', 3, '', '', 'doc:query', '', 0, 1, 1),
(101, 6, '新增文档', 'doc-create', 3, '', '', 'doc:create', '', 1, 1, 1),
(102, 6, '编辑文档', 'doc-edit', 3, '', '', 'doc:edit', '', 2, 1, 1),
(103, 6, '删除文档', 'doc-delete', 3, '', '', 'doc:delete', '', 3, 1, 1),
(104, 6, '提交评审', 'doc-submit', 3, '', '', 'doc:submit', '', 4, 1, 1),
-- 评审管理按钮
(110, 8, '评审查看', 'review-query', 3, '', '', 'review:query', '', 0, 1, 1),
(111, 8, '评审提交', 'review-submit', 3, '', '', 'review:submit', '', 1, 1, 1),
-- 用户管理按钮
(120, 10, '用户查询', 'user-query', 3, '', '', 'sys:user:query', '', 0, 1, 1),
(121, 10, '用户新增', 'user-create', 3, '', '', 'sys:user:create', '', 1, 1, 1),
(122, 10, '用户编辑', 'user-edit', 3, '', '', 'sys:user:edit', '', 2, 1, 1),
(123, 10, '用户删除', 'user-delete', 3, '', '', 'sys:user:delete', '', 3, 1, 1);

SELECT setval('sys_menu_id_seq', (SELECT MAX(id) FROM sys_menu));

-- ========================================
-- 3. 初始化角色数据
-- ========================================

INSERT INTO sys_role (id, role_name, role_code, description, data_scope, status) VALUES
(1, '超级管理员', 'admin', '系统超级管理员，拥有所有权限', 1, 1),
(2, '管理员', 'manager', '系统管理员，拥有部分管理权限', 2, 1),
(3, '提交者', 'submitter', '文档提交者，可以上传和管理自己的文档', 5, 1),
(4, '评审者', 'reviewer', '文档评审者，可以评审文档', 5, 1),
(5, '观察者', 'observer', '观察者，只读权限', 5, 1);

SELECT setval('sys_role_id_seq', (SELECT MAX(id) FROM sys_role));

-- ========================================
-- 4. 初始化权限数据
-- ========================================

INSERT INTO sys_permission (id, perm_name, perm_code, resource_type, resource_url, method, description) VALUES
-- 文档权限
(1, '文档列表', 'doc:list', 'menu', '/api/v1/documents', 'GET', '查看文档列表'),
(2, '文档详情', 'doc:query', 'button', '/api/v1/documents/*', 'GET', '查看文档详情'),
(3, '新增文档', 'doc:create', 'button', '/api/v1/documents', 'POST', '新增文档'),
(4, '编辑文档', 'doc:edit', 'button', '/api/v1/documents/*', 'PUT', '编辑文档'),
(5, '删除文档', 'doc:delete', 'button', '/api/v1/documents/*', 'DELETE', '删除文档'),
(6, '提交评审', 'doc:submit', 'button', '/api/v1/documents/*/submit', 'POST', '提交评审'),
-- 评审权限
(10, '评审列表', 'review:list', 'menu', '/api/v1/reviews/pending', 'GET', '查看评审列表'),
(11, '评审详情', 'review:query', 'button', '/api/v1/reviews/*', 'GET', '查看评审详情'),
(12, '提交评审', 'review:submit', 'button', '/api/v1/reviews/*/submit', 'POST', '提交评审意见'),
-- 归档权限
(20, '归档列表', 'archive:list', 'menu', '/api/v1/archives', 'GET', '查看归档列表'),
-- 用户管理权限
(30, '用户列表', 'sys:user:list', 'menu', '/api/v1/users', 'GET', '查看用户列表'),
(31, '用户查询', 'sys:user:query', 'button', '/api/v1/users/*', 'GET', '查看用户详情'),
(32, '用户新增', 'sys:user:create', 'button', '/api/v1/users', 'POST', '新增用户'),
(33, '用户编辑', 'sys:user:edit', 'button', '/api/v1/users/*', 'PUT', '编辑用户'),
(34, '用户删除', 'sys:user:delete', 'button', '/api/v1/users/*', 'DELETE', '删除用户'),
-- 角色管理权限
(40, '角色列表', 'sys:role:list', 'menu', '/api/v1/roles', 'GET', '查看角色列表'),
(41, '角色查询', 'sys:role:query', 'button', '/api/v1/roles/*', 'GET', '查看角色详情'),
(42, '角色新增', 'sys:role:create', 'button', '/api/v1/roles', 'POST', '新增角色'),
(43, '角色编辑', 'sys:role:edit', 'button', '/api/v1/roles/*', 'PUT', '编辑角色'),
(44, '角色删除', 'sys:role:delete', 'button', '/api/v1/roles/*', 'DELETE', '删除角色'),
-- 菜单管理权限
(50, '菜单列表', 'sys:menu:list', 'menu', '/api/v1/menus', 'GET', '查看菜单列表'),
-- 系统配置权限
(60, '系统配置', 'sys:config:edit', 'button', '/api/v1/system/config', 'PUT', '编辑系统配置');

SELECT setval('sys_permission_id_seq', (SELECT MAX(id) FROM sys_permission));

-- ========================================
-- 5. 初始化管理员账号
-- ========================================

-- 密码: admin123 (BCrypt加密，cost=10)
-- 使用 bcryptjs 生成，将 $2b$ 改为 $2a$ 以兼容 jBCrypt/Hutool
INSERT INTO sys_user (id, username, password, email, phone, real_name, dept_id, status) VALUES
(1, 'admin', '$2a$10$e073rpXYclRBCL0A6rQB5.4rWSThfyBUm0g9Ngo1OTn.rBjjKOsLS', 'admin@example.com', '13800000000', '超级管理员', 1, 1);

SELECT setval('sys_user_id_seq', (SELECT MAX(id) FROM sys_user));

-- ========================================
-- 6. 分配角色
-- ========================================

-- 给管理员分配超级管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1);

-- ========================================
-- 7. 分配角色菜单
-- ========================================

-- 给超级管理员分配所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE deleted = 0;

-- 给提交者分配文档管理菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1), (3, 2), (3, 6), (3, 7), (3, 100), (3, 101), (3, 102), (3, 103), (3, 104);

-- 给评审者分配评审管理菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1), (4, 3), (4, 8), (4, 9), (4, 110), (4, 111);

-- ========================================
-- 8. 分配角色权限
-- ========================================

-- 给超级管理员分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE deleted = 0;

-- 给提交者分配文档相关权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 20);

-- 给评审者分配评审相关权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(4, 10), (4, 11), (4, 12), (4, 20);

-- ========================================
-- 9. 初始化系统配置
-- ========================================

INSERT INTO sys_config (config_key, config_value, description) VALUES
('ldap.enabled', 'false', 'LDAP认证开关'),
('ldap.url', 'ldap://localhost:389', 'LDAP服务器地址'),
('ldap.base-dn', 'dc=company,dc=com', 'LDAP Base DN'),
('ldap.bind-dn', 'cn=admin,dc=company,dc=com', 'LDAP绑定账号'),
('ldap.bind-password', '', 'LDAP绑定密码'),
('ldap.user-attribute', 'sAMAccountName', 'LDAP用户名属性'),
('review.pass-condition', 'ALL', '评审通过条件：ALL全部通过/MAJORITY多数通过/THREE_QUARTERS四分之三通过'),
('file.max-size', '52428800', '文件最大大小（字节）50MB'),
('file.allowed-types', 'pdf,docx,doc,xlsx,xls,pptx,ppt,txt,md', '允许的文件类型');
