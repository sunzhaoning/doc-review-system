-- 文档评审系统数据库初始化脚本
-- 创建时间：2026-03-05
-- 数据库：PostgreSQL 15.x

-- ========================================
-- 1. RBAC 权限管理表
-- ========================================

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    dept_name VARCHAR(50) NOT NULL,
    dept_code VARCHAR(50),
    leader VARCHAR(50),
    phone VARCHAR(20),
    sort INT DEFAULT 0,
    status INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

COMMENT ON TABLE sys_dept IS '部门表';
COMMENT ON COLUMN sys_dept.id IS '部门ID';
COMMENT ON COLUMN sys_dept.parent_id IS '父部门ID';
COMMENT ON COLUMN sys_dept.dept_name IS '部门名称';
COMMENT ON COLUMN sys_dept.dept_code IS '部门编码';
COMMENT ON COLUMN sys_dept.leader IS '部门负责人';
COMMENT ON COLUMN sys_dept.phone IS '联系电话';
COMMENT ON COLUMN sys_dept.sort IS '排序号';
COMMENT ON COLUMN sys_dept.status IS '状态：0禁用 1启用';
COMMENT ON COLUMN sys_dept.deleted IS '逻辑删除：0未删除 1已删除';

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255),
    email VARCHAR(100),
    phone VARCHAR(20),
    real_name VARCHAR(50),
    avatar VARCHAR(255),
    dept_id BIGINT,
    status INT DEFAULT 1,
    ldap_dn VARCHAR(255),
    ldap_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.id IS '用户ID';
COMMENT ON COLUMN sys_user.username IS '用户名';
COMMENT ON COLUMN sys_user.password IS '密码';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.phone IS '手机号';
COMMENT ON COLUMN sys_user.real_name IS '真实姓名';
COMMENT ON COLUMN sys_user.avatar IS '头像URL';
COMMENT ON COLUMN sys_user.dept_id IS '部门ID';
COMMENT ON COLUMN sys_user.status IS '状态：0禁用 1启用';
COMMENT ON COLUMN sys_user.ldap_dn IS 'LDAP Distinguished Name';
COMMENT ON COLUMN sys_user.ldap_enabled IS '是否LDAP用户';
COMMENT ON COLUMN sys_user.deleted IS '逻辑删除：0未删除 1已删除';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    data_scope INT DEFAULT 1,
    status INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.id IS '角色ID';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_code IS '角色编码';
COMMENT ON COLUMN sys_role.description IS '角色描述';
COMMENT ON COLUMN sys_role.data_scope IS '数据权限范围：1全部 2自定义 3本部门及下级 4本部门 5仅本人';
COMMENT ON COLUMN sys_role.status IS '状态：0禁用 1启用';
COMMENT ON COLUMN sys_role.deleted IS '逻辑删除：0未删除 1已删除';

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    menu_name VARCHAR(50) NOT NULL,
    menu_code VARCHAR(50),
    menu_type INT NOT NULL,
    path VARCHAR(255),
    component VARCHAR(255),
    perms VARCHAR(100),
    icon VARCHAR(50),
    sort INT DEFAULT 0,
    visible INT DEFAULT 1,
    status INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

COMMENT ON TABLE sys_menu IS '菜单表';
COMMENT ON COLUMN sys_menu.id IS '菜单ID';
COMMENT ON COLUMN sys_menu.parent_id IS '父菜单ID';
COMMENT ON COLUMN sys_menu.menu_name IS '菜单名称';
COMMENT ON COLUMN sys_menu.menu_code IS '菜单编码';
COMMENT ON COLUMN sys_menu.menu_type IS '菜单类型：1目录 2菜单 3按钮';
COMMENT ON COLUMN sys_menu.path IS '路由地址';
COMMENT ON COLUMN sys_menu.component IS '组件路径';
COMMENT ON COLUMN sys_menu.perms IS '权限标识';
COMMENT ON COLUMN sys_menu.icon IS '菜单图标';
COMMENT ON COLUMN sys_menu.sort IS '排序号';
COMMENT ON COLUMN sys_menu.visible IS '是否可见：0隐藏 1显示';
COMMENT ON COLUMN sys_menu.status IS '状态：0禁用 1启用';
COMMENT ON COLUMN sys_menu.deleted IS '逻辑删除：0未删除 1已删除';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGSERIAL PRIMARY KEY,
    perm_name VARCHAR(50) NOT NULL,
    perm_code VARCHAR(100) NOT NULL UNIQUE,
    resource_type VARCHAR(20),
    resource_url VARCHAR(255),
    method VARCHAR(10),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

COMMENT ON TABLE sys_permission IS '权限表';
COMMENT ON COLUMN sys_permission.id IS '权限ID';
COMMENT ON COLUMN sys_permission.perm_name IS '权限名称';
COMMENT ON COLUMN sys_permission.perm_code IS '权限编码';
COMMENT ON COLUMN sys_permission.resource_type IS '资源类型：menu/button/api';
COMMENT ON COLUMN sys_permission.resource_url IS '资源URL';
COMMENT ON COLUMN sys_permission.method IS 'HTTP方法';
COMMENT ON COLUMN sys_permission.description IS '权限描述';
COMMENT ON COLUMN sys_permission.deleted IS '逻辑删除：0未删除 1已删除';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_user_role IS '用户角色关联表';
CREATE UNIQUE INDEX uk_user_role ON sys_user_role(user_id, role_id);

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_role_menu IS '角色菜单关联表';
CREATE UNIQUE INDEX uk_role_menu ON sys_role_menu(role_id, menu_id);

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_role_permission IS '角色权限关联表';
CREATE UNIQUE INDEX uk_role_permission ON sys_role_permission(role_id, permission_id);

-- 角色部门关联表（数据权限）
CREATE TABLE IF NOT EXISTS sys_role_dept (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_role_dept IS '角色部门关联表';
CREATE UNIQUE INDEX uk_role_dept ON sys_role_dept(role_id, dept_id);

-- ========================================
-- 2. 文档评审业务表
-- ========================================

-- 文档表
CREATE TABLE IF NOT EXISTS doc_document (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_path VARCHAR(500),
    file_name VARCHAR(255),
    file_size BIGINT,
    file_type VARCHAR(50),
    review_type VARCHAR(20),
    status VARCHAR(20) DEFAULT 'DRAFT',
    submitter_id BIGINT,
    dept_id BIGINT,
    deadline TIMESTAMP,
    version VARCHAR(20) DEFAULT 'v1.0',
    archived BOOLEAN DEFAULT FALSE,
    archived_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

COMMENT ON TABLE doc_document IS '文档表';
COMMENT ON COLUMN doc_document.id IS '文档ID';
COMMENT ON COLUMN doc_document.title IS '文档标题';
COMMENT ON COLUMN doc_document.description IS '文档描述';
COMMENT ON COLUMN doc_document.file_path IS 'MinIO文件路径';
COMMENT ON COLUMN doc_document.file_name IS '原始文件名';
COMMENT ON COLUMN doc_document.file_size IS '文件大小（字节）';
COMMENT ON COLUMN doc_document.file_type IS '文件类型';
COMMENT ON COLUMN doc_document.review_type IS '评审类型：TECHNICAL/DESIGN/CODE';
COMMENT ON COLUMN doc_document.status IS '状态：DRAFT/PENDING/REVIEWING/REVISION/APPROVED/REJECTED/ARCHIVED';
COMMENT ON COLUMN doc_document.submitter_id IS '提交者ID';
COMMENT ON COLUMN doc_document.dept_id IS '部门ID';
COMMENT ON COLUMN doc_document.deadline IS '评审截止时间';
COMMENT ON COLUMN doc_document.version IS '版本号';
COMMENT ON COLUMN doc_document.archived IS '是否已归档';
COMMENT ON COLUMN doc_document.archived_at IS '归档时间';
COMMENT ON COLUMN doc_document.deleted IS '逻辑删除：0未删除 1已删除';

-- 评审者分配表
CREATE TABLE IF NOT EXISTS doc_reviewer_assignment (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE doc_reviewer_assignment IS '评审者分配表';
COMMENT ON COLUMN doc_reviewer_assignment.id IS 'ID';
COMMENT ON COLUMN doc_reviewer_assignment.document_id IS '文档ID';
COMMENT ON COLUMN doc_reviewer_assignment.reviewer_id IS '评审者ID';
COMMENT ON COLUMN doc_reviewer_assignment.status IS '状态：PENDING/REVIEWING/COMPLETED';
CREATE UNIQUE INDEX uk_doc_reviewer ON doc_reviewer_assignment(document_id, reviewer_id);

-- 评审表
CREATE TABLE IF NOT EXISTS doc_review (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    decision VARCHAR(20),
    overall_comment TEXT,
    pros TEXT,
    cons TEXT,
    suggestions TEXT,
    submitted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

COMMENT ON TABLE doc_review IS '评审表';
COMMENT ON COLUMN doc_review.id IS '评审ID';
COMMENT ON COLUMN doc_review.document_id IS '文档ID';
COMMENT ON COLUMN doc_review.reviewer_id IS '评审者ID';
COMMENT ON COLUMN doc_review.status IS '状态：PENDING/IN_PROGRESS/SUBMITTED';
COMMENT ON COLUMN doc_review.decision IS '决定：APPROVED/REJECTED/REVISION_REQUIRED';
COMMENT ON COLUMN doc_review.overall_comment IS '总体评价';
COMMENT ON COLUMN doc_review.pros IS '优点';
COMMENT ON COLUMN doc_review.cons IS '问题';
COMMENT ON COLUMN doc_review.suggestions IS '建议';
COMMENT ON COLUMN doc_review.submitted_at IS '提交时间';
COMMENT ON COLUMN doc_review.deleted IS '逻辑删除：0未删除 1已删除';

-- 评论表
CREATE TABLE IF NOT EXISTS doc_comment (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    review_id BIGINT,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) DEFAULT 'ISSUE',
    priority VARCHAR(10) DEFAULT 'MEDIUM',
    position VARCHAR(100),
    parent_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

COMMENT ON TABLE doc_comment IS '评论表';
COMMENT ON COLUMN doc_comment.id IS '评论ID';
COMMENT ON COLUMN doc_comment.document_id IS '文档ID';
COMMENT ON COLUMN doc_comment.review_id IS '评审ID';
COMMENT ON COLUMN doc_comment.author_id IS '作者ID';
COMMENT ON COLUMN doc_comment.content IS '评论内容';
COMMENT ON COLUMN doc_comment.type IS '类型：ISSUE/SUGGESTION/QUESTION';
COMMENT ON COLUMN doc_comment.priority IS '优先级：HIGH/MEDIUM/LOW';
COMMENT ON COLUMN doc_comment.position IS '行内批注位置';
COMMENT ON COLUMN doc_comment.parent_id IS '父评论ID';
COMMENT ON COLUMN doc_comment.deleted IS '逻辑删除：0未删除 1已删除';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_config IS '系统配置表';
COMMENT ON COLUMN sys_config.id IS '配置ID';
COMMENT ON COLUMN sys_config.config_key IS '配置键';
COMMENT ON COLUMN sys_config.config_value IS '配置值';
COMMENT ON COLUMN sys_config.description IS '配置描述';

-- 审计日志表
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(100),
    method VARCHAR(200),
    params TEXT,
    ip VARCHAR(50),
    duration BIGINT,
    success BOOLEAN DEFAULT TRUE,
    error_msg TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_audit_log IS '审计日志表';
COMMENT ON COLUMN sys_audit_log.id IS '日志ID';
COMMENT ON COLUMN sys_audit_log.user_id IS '用户ID';
COMMENT ON COLUMN sys_audit_log.username IS '用户名';
COMMENT ON COLUMN sys_audit_log.operation IS '操作名称';
COMMENT ON COLUMN sys_audit_log.method IS '请求方法';
COMMENT ON COLUMN sys_audit_log.params IS '请求参数';
COMMENT ON COLUMN sys_audit_log.ip IS 'IP地址';
COMMENT ON COLUMN sys_audit_log.duration IS '执行时长(ms)';
COMMENT ON COLUMN sys_audit_log.success IS '是否成功';
COMMENT ON COLUMN sys_audit_log.error_msg IS '错误信息';
