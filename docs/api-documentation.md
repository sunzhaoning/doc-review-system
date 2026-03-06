# API 文档

## 概述

文档评审系统 API 基于 RESTful 架构设计，提供完整的文档管理、评审流程、权限控制等功能接口。

### 基础信息

| 项目 | 说明 |
|------|------|
| 基础路径 | `/api/v1` |
| 协议 | HTTP/HTTPS |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |
| API 文档 | http://localhost:8080/doc.html (Knife4j) |

### 认证方式

系统使用 Sa-Token 进行认证授权。登录成功后获取 Token，后续请求在 Header 中携带：

```
Authorization: <token>
```

### 响应格式

所有接口统一响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码：200成功，400xx客户端错误，500xx服务端错误 |
| message | String | 响应消息 |
| data | Object | 响应数据 |

### 分页格式

列表接口统一返回分页格式：

```json
{
  "records": [...],
  "total": 100,
  "current": 1,
  "size": 10,
  "pages": 10
}
```

---

## 认证管理 API

### 用户登录

**POST** `/api/v1/auth/login`

登录系统，获取 Token 和用户信息。

**请求体**

```json
{
  "username": "admin",
  "password": "admin123"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**响应示例**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "avatar": null,
      "roles": ["admin"],
      "permissions": ["*"]
    }
  }
}
```

---

### 用户登出

**POST** `/api/v1/auth/logout`

退出登录，清除 Token。

**请求头**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Authorization | String | 是 | 登录获取的 Token |

**响应示例**

```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

---

### 获取当前用户信息

**GET** `/api/v1/auth/me`

获取当前登录用户的详细信息。

**请求头**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Authorization | String | 是 | 登录获取的 Token |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "avatar": null,
    "roles": ["admin"],
    "permissions": ["*"]
  }
}
```

---

### 获取当前用户菜单

**GET** `/api/v1/auth/menus`

获取当前用户有权访问的菜单列表。

**请求头**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Authorization | String | 是 | 登录获取的 Token |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "parentId": 0,
      "menuName": "工作台",
      "menuCode": "dashboard",
      "menuType": 2,
      "path": "/dashboard",
      "component": "views/Dashboard",
      "icon": "dashboard",
      "sort": 1,
      "visible": 1,
      "children": []
    }
  ]
}
```

---

## 用户管理 API

### 获取用户列表

**GET** `/api/v1/users`

分页获取用户列表。

**权限**: `sys:user:list`

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码，默认 1 |
| size | Integer | 否 | 每页数量，默认 10 |
| username | String | 否 | 用户名（模糊查询） |
| realName | String | 否 | 真实姓名（模糊查询） |
| status | Integer | 否 | 状态：0禁用 1启用 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "email": "admin@example.com",
        "phone": "13800000000",
        "deptName": "技术部",
        "status": 1,
        "createdAt": "2026-03-01T00:00:00",
        "roles": [
          {"id": 1, "roleName": "超级管理员", "roleCode": "admin"}
        ]
      }
    ],
    "total": 10,
    "current": 1,
    "size": 10,
    "pages": 1
  }
}
```

---

### 获取用户详情

**GET** `/api/v1/users/{id}`

获取指定用户的详细信息。

**权限**: `sys:user:query`

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "email": "admin@example.com",
    "phone": "13800000000",
    "deptId": 1,
    "deptName": "技术部",
    "avatar": null,
    "status": 1,
    "roleIds": [1, 2],
    "createdAt": "2026-03-01T00:00:00"
  }
}
```

---

### 创建用户

**POST** `/api/v1/users`

创建新用户。

**权限**: `sys:user:create`

**请求体**

```json
{
  "username": "zhangsan",
  "password": "password123",
  "realName": "张三",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "deptId": 1,
  "roleIds": [2, 3]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（唯一） |
| password | String | 是 | 密码 |
| realName | String | 是 | 真实姓名 |
| email | String | 否 | 邮箱 |
| phone | String | 否 | 手机号 |
| deptId | Long | 否 | 部门ID |
| roleIds | Array | 否 | 角色ID列表 |

**响应示例**

```json
{
  "code": 200,
  "message": "创建成功",
  "data": 2
}
```

---

### 更新用户

**PUT** `/api/v1/users/{id}`

更新用户信息。

**权限**: `sys:user:edit`

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |

**请求体**

```json
{
  "realName": "张三",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "deptId": 1,
  "roleIds": [2, 3]
}
```

---

### 删除用户

**DELETE** `/api/v1/users/{id}`

删除指定用户。

**权限**: `sys:user:delete`

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |

---

### 更新用户状态

**PUT** `/api/v1/users/{id}/status`

启用/禁用用户。

**权限**: `sys:user:edit`

**请求体**

```json
{
  "status": 0
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 是 | 状态：0禁用 1启用 |

---

### 重置密码

**PUT** `/api/v1/users/{id}/reset-password`

重置用户密码。

**权限**: `sys:user:resetPwd`

**请求体**

```json
{
  "password": "newPassword123"
}
```

---

### 分配用户角色

**PUT** `/api/v1/users/{id}/roles`

为用户分配角色。

**权限**: `sys:user:edit`

**请求体**

```json
{
  "roleIds": [1, 2, 3]
}
```

---

## 文档管理 API

### 上传文档

**POST** `/api/v1/documents`

上传文档文件。

**权限**: `doc:create`

**请求类型**: `multipart/form-data`

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 文档文件（最大50MB） |
| title | String | 是 | 文档标题 |
| description | String | 否 | 文档描述 |
| reviewType | String | 是 | 评审类型：TECHNICAL/DESIGN/CODE |
| deadline | String | 否 | 评审截止时间（ISO格式） |

**响应示例**

```json
{
  "code": 200,
  "message": "上传成功",
  "data": 1
}
```

---

### 获取文档列表

**GET** `/api/v1/documents`

分页获取文档列表。

**权限**: `doc:list`

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码，默认 1 |
| size | Integer | 否 | 每页数量，默认 10 |
| title | String | 否 | 标题（模糊查询） |
| status | String | 否 | 状态：DRAFT/PENDING/REVIEWING/REVISION/APPROVED/REJECTED/ARCHIVED |
| reviewType | String | 否 | 评审类型 |
| submitterId | Long | 否 | 提交者ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "系统架构设计文档",
        "description": "系统整体架构设计说明",
        "fileName": "architecture.pdf",
        "fileSize": 1024000,
        "fileType": "pdf",
        "reviewType": "DESIGN",
        "status": "PENDING",
        "submitterName": "张三",
        "deptName": "技术部",
        "deadline": "2026-03-10T00:00:00",
        "version": "v1.0",
        "createdAt": "2026-03-01T00:00:00"
      }
    ],
    "total": 10,
    "current": 1,
    "size": 10
  }
}
```

---

### 获取我的文档

**GET** `/api/v1/documents/my`

获取当前用户提交的文档列表。

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页数量 |
| status | String | 否 | 状态过滤 |

---

### 获取文档详情

**GET** `/api/v1/documents/{id}`

获取文档详细信息。

**权限**: `doc:query`

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "title": "系统架构设计文档",
    "description": "系统整体架构设计说明",
    "filePath": "documents/2026/03/xxx.pdf",
    "fileName": "architecture.pdf",
    "fileSize": 1024000,
    "fileType": "pdf",
    "reviewType": "DESIGN",
    "status": "REVIEWING",
    "submitterId": 1,
    "submitterName": "张三",
    "deptId": 1,
    "deptName": "技术部",
    "deadline": "2026-03-10T00:00:00",
    "version": "v1.0",
    "archived": false,
    "createdAt": "2026-03-01T00:00:00",
    "reviewers": [
      {"id": 2, "realName": "李四", "status": "COMPLETED"},
      {"id": 3, "realName": "王五", "status": "PENDING"}
    ]
  }
}
```

---

### 更新文档

**PUT** `/api/v1/documents/{id}`

更新文档信息（仅草稿状态可更新）。

**权限**: `doc:edit`

**请求体**

```json
{
  "title": "更新后的标题",
  "description": "更新后的描述",
  "reviewType": "TECHNICAL",
  "deadline": "2026-03-15T00:00:00"
}
```

---

### 删除文档

**DELETE** `/api/v1/documents/{id}`

删除文档（仅草稿状态可删除）。

**权限**: `doc:delete`

---

### 提交评审

**POST** `/api/v1/documents/{id}/submit`

提交文档进行评审。

**权限**: `doc:submit`

**请求体**

```json
{
  "reviewerIds": [2, 3, 4]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reviewerIds | Array | 是 | 评审者用户ID列表 |

---

### 撤回评审

**POST** `/api/v1/documents/{id}/withdraw`

撤回已提交的评审（仅待评审状态可撤回）。

**权限**: `doc:withdraw`

---

### 获取预览URL

**GET** `/api/v1/documents/{id}/preview`

获取文档预览地址。

**权限**: `doc:query`

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "http://minio:9000/doc-review/documents/xxx.pdf?X-Amz-..."
}
```

---

### 下载文档

**GET** `/api/v1/documents/{id}/download`

下载文档文件。

**权限**: `doc:download`

**响应**: 文件流（Content-Disposition: attachment）

---

## 评审管理 API

### 获取待评审列表

**GET** `/api/v1/reviews/pending`

获取分配给当前用户的待评审文档列表。

**权限**: `review:list`

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页数量 |
| status | String | 否 | 状态：PENDING/IN_PROGRESS |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "documentId": 1,
        "documentTitle": "系统架构设计文档",
        "reviewType": "DESIGN",
        "status": "PENDING",
        "deadline": "2026-03-10T00:00:00",
        "submitterName": "张三"
      }
    ],
    "total": 5,
    "current": 1,
    "size": 10
  }
}
```

---

### 获取我的评审历史

**GET** `/api/v1/reviews/history`

获取当前用户的评审历史记录。

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页数量 |
| decision | String | 否 | 决定过滤：APPROVED/REJECTED/REVISION_REQUIRED |

---

### 获取评审详情

**GET** `/api/v1/reviews/{id}`

获取评审详细信息。

**权限**: `review:query`

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "documentId": 1,
    "documentTitle": "系统架构设计文档",
    "reviewerId": 2,
    "reviewerName": "李四",
    "status": "SUBMITTED",
    "decision": "APPROVED",
    "overallComment": "整体设计合理，架构清晰",
    "pros": "模块划分清晰，接口设计规范",
    "cons": "部分细节需要补充",
    "suggestions": "建议增加性能优化章节",
    "submittedAt": "2026-03-05T00:00:00"
  }
}
```

---

### 获取文档评审记录

**GET** `/api/v1/reviews/document/{documentId}`

获取指定文档的所有评审记录。

**权限**: `review:query`

---

### 获取评审进度

**GET** `/api/v1/reviews/progress/{documentId}`

获取文档的评审进度汇总。

**权限**: `review:query`

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "documentId": 1,
    "totalReviewers": 3,
    "completedCount": 2,
    "pendingCount": 1,
    "progress": 66.67,
    "reviews": [
      {
        "reviewerName": "李四",
        "status": "COMPLETED",
        "decision": "APPROVED"
      },
      {
        "reviewerName": "王五",
        "status": "COMPLETED",
        "decision": "REVISION_REQUIRED"
      },
      {
        "reviewerName": "赵六",
        "status": "PENDING"
      }
    ]
  }
}
```

---

### 开始评审

**POST** `/api/v1/reviews/start/{documentId}`

开始评审文档，将状态变更为进行中。

**权限**: `review:query`

---

### 提交评审意见

**POST** `/api/v1/reviews/submit/{documentId}`

提交评审结果。

**权限**: `review:submit`

**请求体**

```json
{
  "decision": "APPROVED",
  "overallComment": "整体设计合理，架构清晰",
  "pros": "模块划分清晰，接口设计规范",
  "cons": "部分细节需要补充",
  "suggestions": "建议增加性能优化章节"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| decision | String | 是 | 决定：APPROVED/REJECTED/REVISION_REQUIRED |
| overallComment | String | 是 | 总体评价 |
| pros | String | 否 | 优点 |
| cons | String | 否 | 问题 |
| suggestions | String | 否 | 建议 |

---

## 评论管理 API

### 获取文档评论

**GET** `/api/v1/documents/{documentId}/comments`

获取指定文档的所有评论。

**权限**: `comment:list`

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "documentId": 1,
      "authorId": 2,
      "authorName": "李四",
      "content": "第三章架构图需要更新",
      "type": "ISSUE",
      "priority": "HIGH",
      "position": "page:3,line:15",
      "parentId": null,
      "createdAt": "2026-03-05T00:00:00",
      "replies": [
        {
          "id": 2,
          "authorName": "张三",
          "content": "已更新",
          "createdAt": "2026-03-05T01:00:00"
        }
      ]
    }
  ]
}
```

---

### 创建评论

**POST** `/api/v1/documents/{documentId}/comments`

创建新评论。

**权限**: `comment:create`

**请求体**

```json
{
  "content": "这里需要补充说明",
  "type": "SUGGESTION",
  "priority": "MEDIUM",
  "position": "page:5,line:20"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | String | 是 | 评论内容 |
| type | String | 否 | 类型：ISSUE/SUGGESTION/QUESTION，默认 ISSUE |
| priority | String | 否 | 优先级：HIGH/MEDIUM/LOW，默认 MEDIUM |
| position | String | 否 | 行内批注位置 |

---

### 更新评论

**PUT** `/api/v1/comments/{id}`

更新评论内容（仅作者可操作）。

**权限**: `comment:edit`

---

### 删除评论

**DELETE** `/api/v1/comments/{id}`

删除评论（仅作者可操作）。

**权限**: `comment:delete`

---

### 回复评论

**POST** `/api/v1/comments/{parentId}/reply`

回复指定评论。

**权限**: `comment:create`

**请求体**

```json
{
  "content": "感谢反馈，已修改"
}
```

---

## 归档管理 API

### 搜索归档文档

**GET** `/api/v1/archives`

搜索已归档的文档。

**权限**: `archive:list`

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页数量 |
| keyword | String | 否 | 关键词（标题、描述） |
| reviewType | String | 否 | 评审类型 |
| startDate | String | 否 | 开始日期（YYYY-MM-DD） |
| endDate | String | 否 | 结束日期（YYYY-MM-DD） |

---

### 归档文档

**POST** `/api/v1/archives/documents/{id}`

将文档归档。

**权限**: `archive:create`

---

### 取消归档

**DELETE** `/api/v1/archives/{id}`

取消文档归档状态。

**权限**: `archive:delete`

---

### 批量归档

**POST** `/api/v1/archives/batch`

批量归档多个文档。

**权限**: `archive:create`

**请求体**

```json
{
  "ids": [1, 2, 3]
}
```

---

### 导出评审报告

**GET** `/api/v1/archives/{id}/export`

导出文档的评审报告（Markdown格式）。

**权限**: `archive:export`

**响应**: Markdown文件下载

---

## 角色管理 API

### 获取角色列表

**GET** `/api/v1/roles`

分页获取角色列表。

**权限**: `sys:role:list`

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页数量 |
| roleName | String | 否 | 角色名称（模糊查询） |
| status | Integer | 否 | 状态过滤 |

---

### 获取所有角色

**GET** `/api/v1/roles/all`

获取所有启用的角色（用于下拉选择）。

---

### 创建角色

**POST** `/api/v1/roles`

创建新角色。

**权限**: `sys:role:create`

**请求体**

```json
{
  "roleName": "评审员",
  "roleCode": "reviewer",
  "description": "文档评审人员",
  "menuIds": [1, 2, 3],
  "permissionIds": [1, 2, 3]
}
```

---

### 更新角色

**PUT** `/api/v1/roles/{id}`

更新角色信息。

**权限**: `sys:role:edit`

---

### 删除角色

**DELETE** `/api/v1/roles/{id}`

删除角色。

**权限**: `sys:role:delete`

---

### 分配角色菜单

**PUT** `/api/v1/roles/{id}/menus`

为角色分配菜单权限。

**权限**: `sys:role:edit`

**请求体**

```json
{
  "menuIds": [1, 2, 3, 4, 5]
}
```

---

### 分配角色权限

**PUT** `/api/v1/roles/{id}/permissions`

为角色分配操作权限。

**权限**: `sys:role:edit`

**请求体**

```json
{
  "permissionIds": [1, 2, 3, 4, 5]
}
```

---

## 菜单管理 API

### 获取菜单树

**GET** `/api/v1/menus/tree`

获取完整菜单树结构。

**权限**: `sys:menu:list`

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "parentId": 0,
      "menuName": "系统管理",
      "menuCode": "system",
      "menuType": 1,
      "path": "/system",
      "icon": "setting",
      "sort": 1,
      "children": [
        {
          "id": 2,
          "parentId": 1,
          "menuName": "用户管理",
          "menuCode": "user",
          "menuType": 2,
          "path": "/system/user",
          "component": "views/system/UserManage",
          "perms": "sys:user:list",
          "sort": 1
        }
      ]
    }
  ]
}
```

---

### 获取当前用户菜单

**GET** `/api/v1/menus/user`

获取当前用户有权访问的菜单树。

---

### 创建菜单

**POST** `/api/v1/menus`

创建菜单项。

**权限**: `sys:menu:create`

**请求体**

```json
{
  "parentId": 0,
  "menuName": "新菜单",
  "menuCode": "new_menu",
  "menuType": 2,
  "path": "/new-menu",
  "component": "views/NewMenu",
  "perms": "new:menu:list",
  "icon": "menu",
  "sort": 1,
  "visible": 1
}
```

---

### 更新菜单

**PUT** `/api/v1/menus/{id}`

更新菜单信息。

**权限**: `sys:menu:edit`

---

### 删除菜单

**DELETE** `/api/v1/menus/{id}`

删除菜单（有子菜单时需先删除子菜单）。

**权限**: `sys:menu:delete`

---

## 系统配置 API

### 获取所有配置

**GET** `/api/v1/system/config`

获取所有系统配置项。

**权限**: `sys:config:query`

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "system.name": "文档评审系统",
    "system.version": "1.0.0",
    "upload.maxSize": "52428800",
    "upload.allowedTypes": "pdf,doc,docx,xls,xlsx,ppt,pptx"
  }
}
```

---

### 获取指定配置

**GET** `/api/v1/system/config/{key}`

获取指定配置项的值。

**权限**: `sys:config:query`

---

### 设置配置

**PUT** `/api/v1/system/config/{key}`

设置配置项值。

**权限**: `sys:config:edit`

**请求体**

```json
{
  "value": "新配置值"
}
```

---

### 获取LDAP配置

**GET** `/api/v1/system/ldap-config`

获取LDAP/AD域认证配置。

**权限**: `sys:config:query`

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "enabled": true,
    "url": "ldap://ad.example.com:389",
    "baseDn": "dc=example,dc=com",
    "bindDn": "cn=admin,dc=example,dc=com",
    "userAttribute": "sAMAccountName",
    "emailAttribute": "mail",
    "nameAttribute": "cn"
  }
}
```

---

### 更新LDAP配置

**PUT** `/api/v1/system/ldap-config`

更新LDAP/AD域配置。

**权限**: `sys:config:edit`

---

### 测试LDAP连接

**POST** `/api/v1/system/ldap-test`

测试LDAP/AD域连接是否正常。

**权限**: `sys:config:edit`

**响应示例**

```json
{
  "code": 200,
  "message": "连接成功",
  "data": true
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 40001 | 用户名或密码错误 |
| 40002 | 账号已被禁用 |
| 40003 | 无权限访问 |
| 40004 | 资源不存在 |
| 40005 | 参数验证失败 |
| 50001 | 服务器内部错误 |
| 50002 | 数据库操作失败 |
| 50003 | 文件操作失败 |
| 50004 | 外部服务调用失败 |

---

## 权限编码说明

### 文档权限

| 编码 | 说明 |
|------|------|
| doc:list | 查看文档列表 |
| doc:query | 查看文档详情 |
| doc:create | 创建文档 |
| doc:edit | 编辑文档 |
| doc:delete | 删除文档 |
| doc:submit | 提交评审 |
| doc:withdraw | 撤回评审 |
| doc:download | 下载文档 |

### 评审权限

| 编码 | 说明 |
|------|------|
| review:list | 查看评审列表 |
| review:query | 查看评审详情 |
| review:submit | 提交评审意见 |

### 评论权限

| 编码 | 说明 |
|------|------|
| comment:list | 查看评论 |
| comment:create | 创建评论 |
| comment:edit | 编辑评论 |
| comment:delete | 删除评论 |

### 归档权限

| 编码 | 说明 |
|------|------|
| archive:list | 搜索归档 |
| archive:create | 归档文档 |
| archive:delete | 取消归档 |
| archive:export | 导出报告 |

### 系统权限

| 编码 | 说明 |
|------|------|
| sys:user:list/query/create/edit/delete/resetPwd | 用户管理 |
| sys:role:list/query/create/edit/delete | 角色管理 |
| sys:menu:list/query/create/edit/delete | 菜单管理 |
| sys:config:query/edit | 系统配置 |

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-03-06 | 初始版本 |
