# 文档评审系统 - 测试计划

**项目**: 文档评审系统  
**测试负责人**: qa-automation  
**创建日期**: 2026-03-06  
**版本**: v1.0

---

## 1. 测试范围

### 1.1 功能模块覆盖

| 模块 | API数量 | 测试用例数 | 优先级 |
|------|---------|------------|--------|
| 认证管理 | 4 | 12 | P0 |
| 用户管理 | 9 | 18 | P0 |
| 角色管理 | 12 | 20 | P0 |
| 菜单管理 | 6 | 12 | P1 |
| 文档管理 | 10 | 25 | P0 |
| 评审管理 | 7 | 18 | P0 |
| 评论管理 | 6 | 15 | P1 |
| 归档管理 | 5 | 12 | P1 |
| 系统配置 | 6 | 10 | P2 |

**总计**: 65个API，142个测试用例

### 1.2 测试类型

- **功能测试**: 验证所有API功能正确性
- **权限测试**: 验证RBAC权限控制
- **边界测试**: 验证参数边界值处理
- **异常测试**: 验证错误场景处理
- **安全测试**: 验证认证授权机制

---

## 2. 测试环境

### 2.1 技术栈

| 组件 | 版本 |
|------|------|
| 后端框架 | Spring Boot 3.x |
| 前端框架 | Vue 3 + TypeScript |
| 数据库 | PostgreSQL 15.x |
| 缓存 | Redis 7.x |
| 对象存储 | MinIO |
| 认证框架 | Sa-Token |

### 2.2 测试数据

```sql
-- 初始管理员账号
INSERT INTO sys_user (username, password, real_name, status) 
VALUES ('admin', '$2a$10$...', '系统管理员', 1);

-- 初始角色
INSERT INTO sys_role (role_name, role_code, status) VALUES 
('管理员', 'admin', 1),
('评审员', 'reviewer', 1),
('提交者', 'submitter', 1);

-- 初始菜单
INSERT INTO sys_menu (menu_name, menu_type, path, perms, status) VALUES 
('文档管理', 1, '/document', 'doc:list', 1),
('评审管理', 1, '/review', 'review:list', 1),
('系统管理', 1, '/system', 'sys:list', 1);
```

---

## 3. 测试用例设计

### 3.1 认证管理 (P0)

#### TC-AUTH-001: 用户登录 - 正常流程
- **前置条件**: 用户已注册且状态为启用
- **测试步骤**: 
  1. 发送POST请求到 `/api/v1/auth/login`
  2. 请求体包含正确的username和password
- **预期结果**: 
  - HTTP状态码200
  - 返回token和用户信息
  - 用户信息包含角色和权限列表

#### TC-AUTH-002: 用户登录 - 错误密码
- **测试步骤**: 发送错误密码
- **预期结果**: HTTP 200, code=40001, "用户名或密码错误"

#### TC-AUTH-003: 用户登录 - 账号禁用
- **前置条件**: 用户状态为禁用
- **预期结果**: HTTP 200, code=40002, "账号已被禁用"

#### TC-AUTH-004: 用户登出
- **前置条件**: 用户已登录
- **测试步骤**: POST `/api/v1/auth/logout`
- **预期结果**: HTTP 200, 登出成功

#### TC-AUTH-005: 获取当前用户信息
- **前置条件**: 用户已登录
- **测试步骤**: GET `/api/v1/auth/me`
- **预期结果**: 返回用户完整信息

#### TC-AUTH-006: Token过期处理
- **前置条件**: Token已过期
- **预期结果**: HTTP 401, 提示重新登录

### 3.2 用户管理 (P0)

#### TC-USER-001: 获取用户列表 - 分页
- **权限要求**: `sys:user:list`
- **测试步骤**: GET `/api/v1/users?current=1&size=10`
- **预期结果**: 返回分页用户列表

#### TC-USER-002: 创建用户 - 正常流程
- **权限要求**: `sys:user:create`
- **测试数据**:
  ```json
  {
    "username": "testuser",
    "password": "Test@123",
    "realName": "测试用户",
    "email": "test@example.com",
    "phone": "13800138000"
  }
  ```
- **预期结果**: 创建成功，返回用户ID

#### TC-USER-003: 创建用户 - 用户名重复
- **前置条件**: 用户名已存在
- **预期结果**: 创建失败，提示用户名已存在

#### TC-USER-004: 更新用户信息
- **权限要求**: `sys:user:edit`
- **预期结果**: 更新成功

#### TC-USER-005: 删除用户
- **权限要求**: `sys:user:delete`
- **预期结果**: 删除成功（逻辑删除）

#### TC-USER-006: 分配用户角色
- **权限要求**: `sys:user:edit`
- **测试步骤**: PUT `/api/v1/users/{id}/roles` with `{"roleIds": [1,2]}`
- **预期结果**: 分配成功

#### TC-USER-007: 重置密码
- **权限要求**: `sys:user:resetPwd`
- **预期结果**: 重置成功

#### TC-USER-008: 无权限访问
- **前置条件**: 用户无`sys:user:list`权限
- **预期结果**: HTTP 403, 权限不足

### 3.3 角色管理 (P0)

#### TC-ROLE-001: 获取角色列表
- **权限要求**: `sys:role:list`
- **预期结果**: 返回角色分页列表

#### TC-ROLE-002: 创建角色
- **权限要求**: `sys:role:create`
- **测试数据**:
  ```json
  {
    "roleName": "测试角色",
    "roleCode": "test_role",
    "description": "测试用角色"
  }
  ```
- **预期结果**: 创建成功

#### TC-ROLE-003: 分配角色权限
- **权限要求**: `sys:role:edit`
- **测试步骤**: PUT `/api/v1/roles/{id}/permissions`
- **预期结果**: 分配成功

#### TC-ROLE-004: 分配角色菜单
- **权限要求**: `sys:role:edit`
- **测试步骤**: PUT `/api/v1/roles/{id}/menus`
- **预期结果**: 分配成功

### 3.4 文档管理 (P0)

#### TC-DOC-001: 上传文档 - 正常流程
- **权限要求**: `doc:create`
- **测试步骤**: POST `/api/v1/documents` with MultipartFile
- **测试数据**: PDF文件，大小 < 50MB
- **预期结果**: 上传成功，返回文档ID

#### TC-DOC-002: 上传文档 - 文件类型限制
- **测试数据**: .exe文件
- **预期结果**: 上传失败，提示不支持的文件类型

#### TC-DOC-003: 上传文档 - 文件大小限制
- **测试数据**: 文件大小 > 50MB
- **预期结果**: 上传失败，提示文件过大

#### TC-DOC-004: 获取文档列表
- **权限要求**: `doc:list`
- **测试步骤**: GET `/api/v1/documents?current=1&size=10`
- **预期结果**: 返回分页文档列表

#### TC-DOC-005: 获取文档详情
- **权限要求**: `doc:query`
- **测试步骤**: GET `/api/v1/documents/{id}`
- **预期结果**: 返回文档详细信息

#### TC-DOC-006: 提交评审
- **权限要求**: `doc:submit`
- **前置条件**: 文档状态为DRAFT
- **测试步骤**: POST `/api/v1/documents/{id}/submit` with `{"reviewerIds": [2,3]}`
- **预期结果**: 提交成功，文档状态变为PENDING

#### TC-DOC-007: 撤回评审
- **权限要求**: `doc:withdraw`
- **前置条件**: 文档状态为PENDING
- **预期结果**: 撤回成功，文档状态恢复为DRAFT

#### TC-DOC-008: 下载文档
- **权限要求**: `doc:download`
- **预期结果**: 返回文件流

### 3.5 评审管理 (P0)

#### TC-REV-001: 获取待评审列表
- **权限要求**: `review:list`
- **测试步骤**: GET `/api/v1/reviews/pending`
- **预期结果**: 返回当前用户的待评审文档列表

#### TC-REV-002: 开始评审
- **权限要求**: `review:query`
- **前置条件**: 文档已分配给当前评审者
- **测试步骤**: POST `/api/v1/reviews/start/{documentId}`
- **预期结果**: 开始成功，状态变为IN_PROGRESS

#### TC-REV-003: 提交评审意见
- **权限要求**: `review:submit`
- **测试数据**:
  ```json
  {
    "decision": "APPROVED",
    "overallComment": "文档内容完整，符合规范",
    "pros": "结构清晰，逻辑严密",
    "cons": "",
    "suggestions": "建议增加更多示例"
  }
  ```
- **预期结果**: 提交成功

#### TC-REV-004: 获取评审进度
- **权限要求**: `review:query`
- **预期结果**: 返回评审进度信息

### 3.6 评论管理 (P1)

#### TC-CMT-001: 创建评论
- **权限要求**: `comment:create`
- **测试数据**:
  ```json
  {
    "content": "第3章节需要补充",
    "type": "ISSUE",
    "priority": "HIGH",
    "position": "page:3,line:15"
  }
  ```
- **预期结果**: 创建成功

#### TC-CMT-002: 回复评论
- **权限要求**: `comment:create`
- **预期结果**: 回复成功，关联父评论

#### TC-CMT-003: 删除评论
- **权限要求**: `comment:delete`
- **前置条件**: 只能删除自己的评论
- **预期结果**: 删除成功

### 3.7 归档管理 (P1)

#### TC-ARC-001: 搜索归档文档
- **权限要求**: `archive:list`
- **测试步骤**: GET `/api/v1/archives?keyword=test`
- **预期结果**: 返回匹配的归档文档列表

#### TC-ARC-002: 归档文档
- **权限要求**: `archive:create`
- **前置条件**: 文档状态为APPROVED
- **预期结果**: 归档成功

#### TC-ARC-003: 批量归档
- **权限要求**: `archive:create`
- **测试步骤**: POST `/api/v1/archives/batch` with `{"ids": [1,2,3]}`
- **预期结果**: 批量归档成功

#### TC-ARC-004: 导出评审报告
- **权限要求**: `archive:export`
- **预期结果**: 返回Markdown格式的评审报告

### 3.8 系统配置 (P2)

#### TC-SYS-001: 获取LDAP配置
- **权限要求**: `sys:config:query`
- **预期结果**: 返回LDAP配置信息

#### TC-SYS-002: 更新LDAP配置
- **权限要求**: `sys:config:edit`
- **测试数据**:
  ```json
  {
    "enabled": true,
    "serverUrl": "ldap://localhost:389",
    "baseDn": "dc=example,dc=com",
    "bindDn": "cn=admin,dc=example,dc=com",
    "bindPassword": "password"
  }
  ```
- **预期结果**: 更新成功

#### TC-SYS-003: 测试LDAP连接
- **权限要求**: `sys:config:edit`
- **预期结果**: 返回连接测试结果

---

## 4. 测试执行

### 4.1 执行顺序

1. **环境准备**: 启动数据库、Redis、MinIO
2. **数据初始化**: 执行init.sql
3. **后端启动**: 启动Spring Boot应用
4. **前端启动**: 启动Vue开发服务器
5. **功能测试**: 按P0->P1->P2顺序执行
6. **回归测试**: 修复Bug后重新测试

### 4.2 缺陷管理

| 级别 | 描述 | 响应时间 |
|------|------|----------|
| P0-Critical | 系统崩溃、数据丢失、安全漏洞 | 立即修复 |
| P1-Major | 主要功能无法使用 | 24小时内 |
| P2-Minor | 次要功能问题 | 48小时内 |
| P3-Trivial | UI问题、建议 | 下版本 |

---

## 5. 退出标准

- [ ] P0测试用例100%通过
- [ ] P1测试用例≥95%通过
- [ ] P2测试用例≥90%通过
- [ ] 无P0、P1级别Bug未修复
- [ ] P2级别Bug≤3个

---

## 6. 测试工具

- **API测试**: Postman / curl
- **自动化测试**: JUnit 5 + MockMvc
- **前端测试**: Vitest + Vue Test Utils
- **E2E测试**: Playwright / Cypress

---

## 7. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 测试环境不稳定 | 测试进度延迟 | 使用Docker容器化环境 |
| 测试数据不足 | 测试覆盖不完整 | 编写数据初始化脚本 |
| 接口变更 | 测试用例失效 | 及时同步接口文档 |

---

**审批签字**: ________________  
**审批日期**: ________________
