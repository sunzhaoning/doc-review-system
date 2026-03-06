# 文档评审系统 - 测试用例清单

**项目**: 文档评审系统  
**版本**: v1.0  
**更新日期**: 2026-03-06

---

## 1. 认证管理模块

### 1.1 登录功能

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-AUTH-001 | 正常登录 | P0 | 用户已注册且启用 | POST /api/v1/auth/login {"username":"admin","password":"123456"} | HTTP 200, 返回token和用户信息 |
| TC-AUTH-002 | 错误密码登录 | P0 | 用户已注册 | POST /api/v1/auth/login {"username":"admin","password":"wrong"} | HTTP 200, code=40001, "用户名或密码错误" |
| TC-AUTH-003 | 不存在的用户登录 | P0 | 无 | POST /api/v1/auth/login {"username":"notexist","password":"123456"} | HTTP 200, code=40001, "用户名或密码错误" |
| TC-AUTH-004 | 禁用账号登录 | P0 | 用户状态为0 | POST /api/v1/auth/login | HTTP 200, code=40002, "账号已被禁用" |
| TC-AUTH-005 | 空用户名登录 | P1 | 无 | POST /api/v1/auth/login {"password":"123456"} | HTTP 400, 参数校验失败 |
| TC-AUTH-006 | 空密码登录 | P1 | 无 | POST /api/v1/auth/login {"username":"admin"} | HTTP 400, 参数校验失败 |
| TC-AUTH-007 | Token获取验证 | P0 | 登录成功 | 检查返回的token格式 | token为非空字符串 |

### 1.2 登出功能

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-AUTH-010 | 正常登出 | P0 | 用户已登录 | POST /api/v1/auth/logout (带Token) | HTTP 200, "登出成功" |
| TC-AUTH-011 | 未登录登出 | P1 | 无 | POST /api/v1/auth/logout (不带Token) | HTTP 401 |

### 1.3 用户信息

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-AUTH-020 | 获取当前用户信息 | P0 | 用户已登录 | GET /api/v1/auth/me | HTTP 200, 返回用户信息含角色权限 |
| TC-AUTH-021 | 未登录获取用户信息 | P0 | 无 | GET /api/v1/auth/me (无Token) | HTTP 401 |
| TC-AUTH-022 | 获取当前用户菜单 | P0 | 用户已登录 | GET /api/v1/auth/menus | HTTP 200, 返回树形菜单 |

---

## 2. 用户管理模块

### 2.1 用户列表

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-USER-001 | 获取用户列表-默认分页 | P0 | 有sys:user:list权限 | GET /api/v1/users | HTTP 200, 返回第一页10条 |
| TC-USER-002 | 获取用户列表-指定分页 | P0 | 有权限 | GET /api/v1/users?current=2&size=20 | HTTP 200, 返回第二页20条 |
| TC-USER-003 | 获取用户列表-按用户名筛选 | P1 | 有权限 | GET /api/v1/users?username=admin | HTTP 200, 返回匹配用户 |
| TC-USER-004 | 获取用户列表-按状态筛选 | P1 | 有权限 | GET /api/v1/users?status=1 | HTTP 200, 返回启用用户 |
| TC-USER-005 | 获取用户列表-无权限 | P0 | 无sys:user:list权限 | GET /api/v1/users | HTTP 403 |

### 2.2 用户详情

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-USER-010 | 获取用户详情 | P0 | 有sys:user:query权限 | GET /api/v1/users/1 | HTTP 200, 返回用户详情含角色ID |
| TC-USER-011 | 获取不存在用户 | P1 | 有权限 | GET /api/v1/users/99999 | HTTP 200, code=40001, "用户不存在" |

### 2.3 用户创建

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-USER-020 | 创建用户-正常 | P0 | 有sys:user:create权限 | POST /api/v1/users 完整用户信息 | HTTP 200, 返回用户ID |
| TC-USER-021 | 创建用户-用户名重复 | P0 | 有权限,用户名已存在 | POST /api/v1/users | HTTP 200, 错误提示 |
| TC-USER-022 | 创建用户-必填项为空 | P1 | 有权限 | POST /api/v1/users 空用户名 | HTTP 400, 参数校验失败 |
| TC-USER-023 | 创建用户-无权限 | P0 | 无sys:user:create权限 | POST /api/v1/users | HTTP 403 |

### 2.4 用户更新

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-USER-030 | 更新用户信息 | P0 | 有sys:user:edit权限 | PUT /api/v1/users/1 | HTTP 200, "更新成功" |
| TC-USER-031 | 更新不存在用户 | P1 | 有权限 | PUT /api/v1/users/99999 | HTTP 200, 错误提示 |

### 2.5 用户删除

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-USER-040 | 删除用户 | P0 | 有sys:user:delete权限 | DELETE /api/v1/users/1 | HTTP 200, "删除成功" |
| TC-USER-041 | 删除不存在用户 | P1 | 有权限 | DELETE /api/v1/users/99999 | HTTP 200, 错误提示 |
| TC-USER-042 | 删除用户-无权限 | P0 | 无权限 | DELETE /api/v1/users/1 | HTTP 403 |

### 2.6 用户状态

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-USER-050 | 禁用用户 | P0 | 有sys:user:edit权限 | PUT /api/v1/users/1/status {"status":0} | HTTP 200, "更新成功" |
| TC-USER-051 | 启用用户 | P0 | 有权限 | PUT /api/v1/users/1/status {"status":1} | HTTP 200, "更新成功" |

### 2.7 用户角色

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-USER-060 | 获取用户角色 | P0 | 有sys:user:query权限 | GET /api/v1/users/1/roles | HTTP 200, 返回角色列表 |
| TC-USER-061 | 分配用户角色 | P0 | 有sys:user:edit权限 | PUT /api/v1/users/1/roles {"roleIds":[1,2]} | HTTP 200, "分配成功" |
| TC-USER-062 | 清空用户角色 | P1 | 有权限 | PUT /api/v1/users/1/roles {"roleIds":[]} | HTTP 200, "分配成功" |

### 2.8 密码管理

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-USER-070 | 重置密码 | P0 | 有sys:user:resetPwd权限 | PUT /api/v1/users/1/reset-password {"password":"newpass"} | HTTP 200, "重置成功" |

---

## 3. 角色管理模块

### 3.1 角色列表

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-ROLE-001 | 获取角色列表-分页 | P0 | 有sys:role:list权限 | GET /api/v1/roles | HTTP 200, 返回分页数据 |
| TC-ROLE-002 | 获取所有角色 | P0 | 已登录 | GET /api/v1/roles/all | HTTP 200, 返回所有角色 |
| TC-ROLE-003 | 按名称筛选角色 | P1 | 有权限 | GET /api/v1/roles?roleName=管理 | HTTP 200, 返回匹配角色 |

### 3.2 角色CRUD

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-ROLE-010 | 创建角色 | P0 | 有sys:role:create权限 | POST /api/v1/roles | HTTP 200, 返回角色ID |
| TC-ROLE-011 | 创建角色-编码重复 | P0 | 有权限,编码已存在 | POST /api/v1/roles | HTTP 200, 错误提示 |
| TC-ROLE-012 | 更新角色 | P0 | 有sys:role:edit权限 | PUT /api/v1/roles/1 | HTTP 200, "更新成功" |
| TC-ROLE-013 | 删除角色 | P0 | 有sys:role:delete权限 | DELETE /api/v1/roles/1 | HTTP 200, "删除成功" |

### 3.3 角色权限分配

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-ROLE-020 | 获取角色权限 | P0 | 有sys:role:query权限 | GET /api/v1/roles/1/permissions | HTTP 200, 返回权限ID列表 |
| TC-ROLE-021 | 分配角色权限 | P0 | 有sys:role:edit权限 | PUT /api/v1/roles/1/permissions {"permissionIds":[1,2,3]} | HTTP 200, "分配成功" |

### 3.4 角色菜单分配

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-ROLE-030 | 获取角色菜单 | P0 | 有sys:role:query权限 | GET /api/v1/roles/1/menus | HTTP 200, 返回菜单ID列表 |
| TC-ROLE-031 | 分配角色菜单 | P0 | 有sys:role:edit权限 | PUT /api/v1/roles/1/menus {"menuIds":[1,2,3]} | HTTP 200, "分配成功" |

---

## 4. 菜单管理模块

### 4.1 菜单树

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-MENU-001 | 获取菜单树 | P0 | 有sys:menu:list权限 | GET /api/v1/menus/tree | HTTP 200, 返回树形菜单 |
| TC-MENU-002 | 获取当前用户菜单 | P0 | 已登录 | GET /api/v1/menus/user | HTTP 200, 返回用户菜单树 |

### 4.2 菜单CRUD

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-MENU-010 | 创建目录 | P0 | 有sys:menu:create权限 | POST /api/v1/menus menuType=1 | HTTP 200, 返回菜单ID |
| TC-MENU-011 | 创建菜单 | P0 | 有权限 | POST /api/v1/menus menuType=2 | HTTP 200, 返回菜单ID |
| TC-MENU-012 | 创建按钮 | P0 | 有权限 | POST /api/v1/menus menuType=3 | HTTP 200, 返回菜单ID |
| TC-MENU-013 | 更新菜单 | P0 | 有sys:menu:edit权限 | PUT /api/v1/menus/1 | HTTP 200, "更新成功" |
| TC-MENU-014 | 删除菜单-无子节点 | P0 | 有sys:menu:delete权限 | DELETE /api/v1/menus/10 | HTTP 200, "删除成功" |
| TC-MENU-015 | 删除菜单-有子节点 | P0 | 有权限,菜单有子节点 | DELETE /api/v1/menus/1 | HTTP 200, 错误提示 |

---

## 5. 文档管理模块

### 5.1 文档上传

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-DOC-001 | 上传PDF文档 | P0 | 有doc:create权限 | POST /api/v1/users 上传test.pdf | HTTP 200, 返回文档ID |
| TC-DOC-002 | 上传Word文档 | P0 | 有权限 | POST /api/v1/documents 上传test.docx | HTTP 200, 返回文档ID |
| TC-DOC-003 | 上传Excel文档 | P0 | 有权限 | POST /api/v1/documents 上传test.xlsx | HTTP 200, 返回文档ID |
| TC-DOC-004 | 上传图片 | P1 | 有权限 | POST /api/v1/documents 上传test.png | HTTP 200, 返回文档ID |
| TC-DOC-005 | 上传不支持的类型 | P0 | 有权限 | POST /api/v1/documents 上传test.exe | HTTP 200, 错误提示 |
| TC-DOC-006 | 上传超大文件 | P0 | 有权限,文件>50MB | POST /api/v1/documents | HTTP 200, "文件过大" |

### 5.2 文档列表

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-DOC-010 | 获取文档列表 | P0 | 有doc:list权限 | GET /api/v1/documents | HTTP 200, 返回分页列表 |
| TC-DOC-011 | 按标题筛选 | P1 | 有权限 | GET /api/v1/documents?title=test | HTTP 200, 返回匹配文档 |
| TC-DOC-012 | 按状态筛选 | P0 | 有权限 | GET /api/v1/documents?status=PENDING | HTTP 200, 返回待评审文档 |
| TC-DOC-013 | 获取我的文档 | P0 | 已登录 | GET /api/v1/documents/my | HTTP 200, 返回当前用户提交的文档 |

### 5.3 文档详情

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-DOC-020 | 获取文档详情 | P0 | 有doc:query权限 | GET /api/v1/documents/1 | HTTP 200, 返回完整信息 |
| TC-DOC-021 | 获取不存在文档 | P1 | 有权限 | GET /api/v1/documents/99999 | HTTP 200, 错误提示 |

### 5.4 文档更新删除

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-DOC-030 | 更新文档信息 | P0 | 有doc:edit权限 | PUT /api/v1/documents/1 | HTTP 200, "更新成功" |
| TC-DOC-031 | 删除草稿文档 | P0 | 有doc:delete权限,状态DRAFT | DELETE /api/v1/documents/1 | HTTP 200, "删除成功" |
| TC-DOC-032 | 删除评审中文档 | P0 | 有权限,状态PENDING | DELETE /api/v1/documents/1 | HTTP 200, 错误提示 |

### 5.5 文档评审操作

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-DOC-040 | 提交评审 | P0 | 有doc:submit权限,状态DRAFT | POST /api/v1/documents/1/submit {"reviewerIds":[2,3]} | HTTP 200, "提交成功",状态PENDING |
| TC-DOC-041 | 提交评审-无评审者 | P1 | 有权限 | POST /api/v1/documents/1/submit {"reviewerIds":[]} | HTTP 400, 参数校验失败 |
| TC-DOC-042 | 撤回评审 | P0 | 有doc:withdraw权限,状态PENDING | POST /api/v1/documents/1/withdraw | HTTP 200, "撤回成功",状态DRAFT |

### 5.6 文档预览下载

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-DOC-050 | 获取预览URL | P0 | 有doc:query权限 | GET /api/v1/documents/1/preview | HTTP 200, 返回MinIO签名URL |
| TC-DOC-051 | 下载文档 | P0 | 有doc:download权限 | GET /api/v1/documents/1/download | HTTP 200, 返回文件流 |

---

## 6. 评审管理模块

### 6.1 评审列表

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-REV-001 | 获取待评审列表 | P0 | 有review:list权限 | GET /api/v1/reviews/pending | HTTP 200, 返回待评审文档 |
| TC-REV-002 | 获取评审历史 | P0 | 已登录 | GET /api/v1/reviews/history | HTTP 200, 返回已提交评审 |
| TC-REV-003 | 获取文档评审记录 | P0 | 有review:query权限 | GET /api/v1/reviews/document/1 | HTTP 200, 返回所有评审者记录 |

### 6.2 评审操作

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-REV-010 | 开始评审 | P0 | 有review:query权限,已分配 | POST /api/v1/reviews/start/1 | HTTP 200, "开始评审" |
| TC-REV-011 | 提交评审-通过 | P0 | 有review:submit权限 | POST /api/v1/reviews/submit/1 {"decision":"APPROVED",...} | HTTP 200, "提交成功" |
| TC-REV-012 | 提交评审-拒绝 | P0 | 有权限 | POST /api/v1/reviews/submit/1 {"decision":"REJECTED",...} | HTTP 200, "提交成功" |
| TC-REV-013 | 提交评审-需修改 | P0 | 有权限 | POST /api/v1/reviews/submit/1 {"decision":"REVISION_REQUIRED",...} | HTTP 200, "提交成功" |

### 6.3 评审进度

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-REV-020 | 获取评审进度 | P0 | 有review:query权限 | GET /api/v1/reviews/progress/1 | HTTP 200, 返回进度信息 |

---

## 7. 评论管理模块

### 7.1 评论列表

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-CMT-001 | 获取文档评论 | P0 | 有comment:list权限 | GET /api/v1/documents/1/comments | HTTP 200, 返回评论列表 |
| TC-CMT-002 | 获取评论回复 | P1 | 已登录 | GET /api/v1/comments/1/replies | HTTP 200, 返回回复列表 |

### 7.2 评论CRUD

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-CMT-010 | 创建评论-问题 | P0 | 有comment:create权限 | POST /api/v1/documents/1/comments {"type":"ISSUE",...} | HTTP 200, 返回评论ID |
| TC-CMT-011 | 创建评论-建议 | P0 | 有权限 | POST /api/v1/documents/1/comments {"type":"SUGGESTION",...} | HTTP 200, 返回评论ID |
| TC-CMT-012 | 创建评论-疑问 | P1 | 有权限 | POST /api/v1/documents/1/comments {"type":"QUESTION",...} | HTTP 200, 返回评论ID |
| TC-CMT-013 | 更新评论 | P0 | 有comment:edit权限,自己的评论 | PUT /api/v1/comments/1 | HTTP 200, "更新成功" |
| TC-CMT-014 | 删除评论 | P0 | 有comment:delete权限,自己的评论 | DELETE /api/v1/comments/1 | HTTP 200, "删除成功" |
| TC-CMT-015 | 回复评论 | P0 | 有comment:create权限 | POST /api/v1/comments/1/reply | HTTP 200, 返回评论ID |

---

## 8. 归档管理模块

### 8.1 归档搜索

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-ARC-001 | 搜索归档文档-关键字 | P0 | 有archive:list权限 | GET /api/v1/archives?keyword=test | HTTP 200, 返回匹配文档 |
| TC-ARC-002 | 搜索归档文档-日期范围 | P1 | 有权限 | GET /api/v1/archives?startDate=2026-01-01&endDate=2026-12-31 | HTTP 200, 返回范围内文档 |
| TC-ARC-003 | 搜索归档文档-类型 | P1 | 有权限 | GET /api/v1/archives?reviewType=TECHNICAL | HTTP 200, 返回技术评审归档 |

### 8.2 归档操作

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-ARC-010 | 归档文档 | P0 | 有archive:create权限,状态APPROVED | POST /api/v1/archives/documents/1 | HTTP 200, "归档成功" |
| TC-ARC-011 | 取消归档 | P1 | 有archive:delete权限 | DELETE /api/v1/archives/1 | HTTP 200, "取消归档成功" |
| TC-ARC-012 | 批量归档 | P0 | 有archive:create权限 | POST /api/v1/archives/batch {"ids":[1,2,3]} | HTTP 200, "批量归档成功" |
| TC-ARC-013 | 导出评审报告 | P1 | 有archive:export权限 | GET /api/v1/archives/1/export | HTTP 200, 返回Markdown文件 |

---

## 9. 系统配置模块

### 9.1 配置管理

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-SYS-001 | 获取所有配置 | P1 | 有sys:config:query权限 | GET /api/v1/system/config | HTTP 200, 返回配置Map |
| TC-SYS-002 | 获取指定配置 | P1 | 有权限 | GET /api/v1/system/config/ldap.enabled | HTTP 200, 返回配置值 |
| TC-SYS-003 | 设置配置 | P1 | 有sys:config:edit权限 | PUT /api/v1/system/config/test {"value":"test"} | HTTP 200, "设置成功" |

### 9.2 LDAP配置

| 用例ID | 用例名称 | 优先级 | 前置条件 | 测试步骤 | 预期结果 |
|--------|----------|--------|----------|----------|----------|
| TC-SYS-010 | 获取LDAP配置 | P0 | 有sys:config:query权限 | GET /api/v1/system/ldap-config | HTTP 200, 返回LDAP配置 |
| TC-SYS-011 | 更新LDAP配置 | P0 | 有sys:config:edit权限 | PUT /api/v1/system/ldap-config | HTTP 200, "更新成功" |
| TC-SYS-012 | 测试LDAP连接-成功 | P2 | 有权限,配置正确 | POST /api/v1/system/ldap-test | HTTP 200, 返回true |
| TC-SYS-013 | 测试LDAP连接-失败 | P2 | 有权限,配置错误 | POST /api/v1/system/ldap-test | HTTP 500, "连接失败" |

---

## 测试统计

| 模块 | 用例数 | P0 | P1 | P2 |
|------|--------|----|----|-----|
| 认证管理 | 14 | 10 | 4 | 0 |
| 用户管理 | 24 | 14 | 8 | 2 |
| 角色管理 | 14 | 10 | 4 | 0 |
| 菜单管理 | 8 | 6 | 2 | 0 |
| 文档管理 | 22 | 16 | 6 | 0 |
| 评审管理 | 10 | 8 | 2 | 0 |
| 评论管理 | 8 | 6 | 2 | 0 |
| 归档管理 | 7 | 4 | 3 | 0 |
| 系统配置 | 7 | 2 | 3 | 2 |
| **总计** | **114** | **76** | **34** | **4** |
