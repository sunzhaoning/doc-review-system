# 文档评审系统 - 技术架构设计文档

**版本**: 1.0
**日期**: 2026-03-05
**作者**: fullstack-dev
**状态**: 待审核

---

## 1. 技术选型

### 1.1 整体技术栈

| 层级 | 技术选型 | 版本 | 说明 |
|------|----------|------|------|
| **前端** | React + TypeScript | 18.x / 5.x | 现代化UI框架，组件化开发 |
| **UI组件库** | Ant Design | 5.x | 企业级UI组件，快速开发 |
| **构建工具** | Vite | 5.x | 快速构建，热更新 |
| **后端框架** | Spring Boot | 3.2.x | Java生态，企业级应用 |
| **ORM** | Spring Data JPA | 3.2.x | 简化数据访问层 |
| **数据库** | PostgreSQL | 15.x | 开源关系型数据库 |
| **文件存储** | MinIO | RELEASE.2024-x | 对象存储，S3兼容 |
| **缓存** | Redis | 7.x | 会话管理、热点数据缓存 |
| **认证** | Spring Security + LDAP | 3.2.x | 安全框架 + AD域集成 |

### 1.2 选型理由

#### 前端选型

**React + TypeScript + Ant Design**

- **React**: 社区活跃，组件复用性强，适合构建复杂交互界面
- **TypeScript**: 类型安全，减少运行时错误，提升代码质量
- **Ant Design**: 企业级UI组件库，设计规范完善，开发效率高
- **Vite**: 构建速度快，开发体验好

#### 后端选型

**Spring Boot + Spring Data JPA**

- **Spring Boot**: 约定优于配置，快速搭建企业级应用
- **Spring Data JPA**: 简化数据访问层开发，支持复杂查询
- **Spring Security**: 成熟的安全框架，支持多种认证方式
- **LDAP**: 标准AD域集成方案

#### 数据存储选型

**PostgreSQL + MinIO + Redis**

- **PostgreSQL**: 开源免费，功能强大，支持JSON类型，适合文档元数据存储
- **MinIO**: S3兼容，私有部署，适合文档文件存储
- **Redis**: 高性能缓存，适合会话管理和热点数据

---

## 2. 系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              用户层                                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │   Chrome    │  │   Firefox   │  │   Safari    │  │    Edge     │    │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTPS
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                            前端层 (React SPA)                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │  文档管理   │  │  评审流程   │  │  归档检索   │  │  系统设置   │    │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                     状态管理 (Zustand)                           │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                     API服务层 (Axios)                            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ REST API
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          后端层 (Spring Boot)                            │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                     API网关 / 控制器层                           │   │
│  │  AuthController | DocumentController | ReviewController | ...   │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                     服务层 (Business Logic)                      │   │
│  │  AuthService | DocumentService | ReviewService | ...            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                     数据访问层 (Repository)                      │   │
│  │  UserRepository | DocumentRepository | ReviewRepository | ...   │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                     安全层 (Spring Security)                     │   │
│  │  JWT认证 | RBAC权限 | LDAP集成 | 方法级安全                      │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
            ┌───────────────────────┼───────────────────────┐
            │                       │                       │
            ▼                       ▼                       ▼
┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐
│   PostgreSQL      │  │      MinIO        │  │      Redis        │
│   (元数据存储)    │  │   (文件存储)      │  │   (缓存/会话)     │
│                   │  │                   │  │                   │
│ - 用户数据        │  │ - 文档文件        │  │ - JWT会话         │
│ - 文档元数据      │  │ - 附件            │  │ - 评审进度缓存    │
│ - 评审记录        │  │ - 导出报告        │  │ - 配置缓存        │
│ - 评论数据        │  │                   │  │                   │
└───────────────────┘  └───────────────────┘  └───────────────────┘
            │
            │ LDAP协议
            ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          AD域服务器 (可选)                               │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  Active Directory / LDAP Server                                  │   │
│  │  - 用户认证                                                       │   │
│  │  - 用户信息同步                                                   │   │
│  │  - 组织架构                                                       │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 部署架构

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            Docker Compose                                │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                        Nginx (反向代理)                          │   │
│  │                         :80 / :443                              │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│            ┌───────────────────────┴───────────────────────┐           │
│            ▼                                               ▼           │
│  ┌───────────────────┐                          ┌───────────────────┐  │
│  │   Frontend        │                          │    Backend        │  │
│  │   (Nginx静态)     │                          │  (Spring Boot)    │  │
│  │    :3000          │                          │      :8080        │  │
│  └───────────────────┘                          └───────────────────┘  │
│            │                                               │           │
│            └───────────────────────┬───────────────────────┘           │
│                                    │                                    │
│         ┌──────────────────────────┼──────────────────────────┐        │
│         ▼                          ▼                          ▼        │
│  ┌─────────────┐          ┌─────────────┐          ┌─────────────┐    │
│  │ PostgreSQL  │          │    MinIO    │          │    Redis    │    │
│  │    :5432    │          │   :9000     │          │    :6379    │    │
│  └─────────────┘          └─────────────┘          └─────────────┘    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 3. 模块划分

### 3.1 后端模块结构

```
doc-review-system/
├── backend/
│   ├── src/main/java/com/docreview/
│   │   ├── DocReviewApplication.java          # 启动类
│   │   ├── config/                             # 配置模块
│   │   │   ├── SecurityConfig.java            # 安全配置
│   │   │   ├── LdapConfig.java                # LDAP配置
│   │   │   ├── MinioConfig.java               # MinIO配置
│   │   │   ├── RedisConfig.java               # Redis配置
│   │   │   └── OpenApiConfig.java             # API文档配置
│   │   ├── controller/                         # 控制器层
│   │   │   ├── AuthController.java            # 认证接口
│   │   │   ├── DocumentController.java        # 文档接口
│   │   │   ├── ReviewController.java          # 评审接口
│   │   │   ├── CommentController.java         # 评论接口
│   │   │   ├── ArchiveController.java         # 归档接口
│   │   │   ├── UserController.java            # 用户接口
│   │   │   └── SystemController.java          # 系统配置接口
│   │   ├── service/                            # 服务层
│   │   │   ├── AuthService.java               # 认证服务
│   │   │   ├── DocumentService.java           # 文档服务
│   │   │   ├── ReviewService.java             # 评审服务
│   │   │   ├── CommentService.java            # 评论服务
│   │   │   ├── ArchiveService.java            # 归档服务
│   │   │   ├── UserService.java               # 用户服务
│   │   │   ├── FileService.java               # 文件服务
│   │   │   ├── NotificationService.java       # 通知服务
│   │   │   └── LdapService.java               # LDAP服务
│   │   ├── repository/                         # 数据访问层
│   │   │   ├── UserRepository.java
│   │   │   ├── DocumentRepository.java
│   │   │   ├── ReviewRepository.java
│   │   │   ├── CommentRepository.java
│   │   │   └── SystemConfigRepository.java
│   │   ├── entity/                             # 实体类
│   │   │   ├── User.java
│   │   │   ├── Document.java
│   │   │   ├── Review.java
│   │   │   ├── Comment.java
│   │   │   ├── ReviewerAssignment.java
│   │   │   └── SystemConfig.java
│   │   ├── dto/                                # 数据传输对象
│   │   │   ├── request/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── DocumentUploadRequest.java
│   │   │   │   ├── ReviewSubmitRequest.java
│   │   │   │   └── ...
│   │   │   └── response/
│   │   │       ├── UserResponse.java
│   │   │       ├── DocumentResponse.java
│   │   │       ├── ReviewResponse.java
│   │   │       └── ...
│   │   ├── enums/                              # 枚举类
│   │   │   ├── DocumentStatus.java            # 文档状态
│   │   │   ├── ReviewDecision.java            # 评审决定
│   │   │   ├── UserRole.java                  # 用户角色
│   │   │   └── NotificationType.java          # 通知类型
│   │   ├── exception/                          # 异常处理
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── BusinessException.java
│   │   │   └── ErrorCode.java
│   │   ├── security/                           # 安全模块
│   │   │   ├── JwtTokenProvider.java          # JWT提供者
│   │   │   ├── JwtAuthenticationFilter.java   # JWT过滤器
│   │   │   ├── CustomUserDetailsService.java  # 用户详情服务
│   │   │   └── LdapAuthenticationProvider.java # LDAP认证提供者
│   │   └── util/                               # 工具类
│   │       ├── FileUtils.java
│   │       └── DateUtils.java
│   └── src/main/resources/
│       ├── application.yml                     # 主配置
│       ├── application-dev.yml                 # 开发环境配置
│       ├── application-prod.yml                # 生产环境配置
│       └── db/migration/                       # 数据库迁移脚本
├── frontend/
│   ├── src/
│   │   ├── main.tsx                           # 入口文件
│   │   ├── App.tsx                            # 根组件
│   │   ├── components/                         # 组件
│   │   │   ├── Layout/
│   │   │   │   ├── MainLayout.tsx
│   │   │   │   ├── Header.tsx
│   │   │   │   └── Sidebar.tsx
│   │   │   ├── common/                        # 通用组件
│   │   │   │   ├── Button/
│   │   │   │   ├── Input/
│   │   │   │   ├── Modal/
│   │   │   │   └── Table/
│   │   │   └── business/                      # 业务组件
│   │   │       ├── DocumentUpload/
│   │   │       ├── ReviewProgress/
│   │   │       └── CommentList/
│   │   ├── pages/                              # 页面
│   │   │   ├── Login.tsx
│   │   │   ├── Dashboard.tsx
│   │   │   ├── DocumentList.tsx
│   │   │   ├── DocumentDetail.tsx
│   │   │   ├── ReviewPage.tsx
│   │   │   ├── Archive.tsx
│   │   │   └── SystemSettings.tsx
│   │   ├── services/                           # API服务
│   │   │   ├── api.ts                         # API客户端
│   │   │   ├── auth.ts
│   │   │   ├── document.ts
│   │   │   ├── review.ts
│   │   │   └── user.ts
│   │   ├── stores/                             # 状态管理
│   │   │   ├── authStore.ts
│   │   │   ├── documentStore.ts
│   │   │   └── notificationStore.ts
│   │   ├── hooks/                              # 自定义Hooks
│   │   │   ├── useAuth.ts
│   │   │   └── useNotification.ts
│   │   ├── types/                              # TypeScript类型
│   │   │   ├── user.ts
│   │   │   ├── document.ts
│   │   │   └── review.ts
│   │   └── utils/                              # 工具函数
│   │       ├── request.ts
│   │       └── format.ts
│   └── package.json
├── docker-compose.yml
└── README.md
```

### 3.2 模块职责说明

| 模块 | 职责 | 依赖 |
|------|------|------|
| **auth** | 认证授权、JWT管理、LDAP集成 | user, system-config |
| **document** | 文档上传、元数据管理、版本控制 | file, user, review |
| **review** | 评审流程管理、并行评审、状态流转 | document, user, comment |
| **comment** | 评审意见、行内批注、意见讨论 | document, user, review |
| **archive** | 文档归档、全文检索、批量导出 | document, file |
| **user** | 用户管理、角色权限、用户信息同步 | auth |
| **file** | 文件存储、上传下载、MinIO集成 | - |
| **notification** | 邮件通知、站内消息 | user, review |
| **system-config** | 系统配置、AD域配置、开关管理 | - |

---

## 4. API设计概要

### 4.1 API风格

- **RESTful API**: 遵循REST设计原则
- **统一响应格式**: 标准化响应结构
- **版本控制**: URL路径版本（/api/v1/）
- **认证方式**: JWT Bearer Token

### 4.2 统一响应格式

```json
// 成功响应
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": "2026-03-05T12:00:00Z"
}

// 错误响应
{
  "code": 40001,
  "message": "参数校验失败",
  "errors": [
    {"field": "title", "message": "标题不能为空"}
  ],
  "timestamp": "2026-03-05T12:00:00Z"
}

// 分页响应
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0
  },
  "timestamp": "2026-03-05T12:00:00Z"
}
```

### 4.3 核心API列表

#### 4.3.1 认证模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/v1/auth/login | 用户登录 | 公开 |
| POST | /api/v1/auth/logout | 用户登出 | 登录用户 |
| GET | /api/v1/auth/me | 获取当前用户信息 | 登录用户 |
| POST | /api/v1/auth/refresh | 刷新Token | 登录用户 |
| POST | /api/v1/auth/ldap-test | 测试LDAP连接 | 管理员 |

#### 4.3.2 文档模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/v1/documents | 上传文档 | 提交者、管理员 |
| GET | /api/v1/documents | 获取文档列表 | 所有用户 |
| GET | /api/v1/documents/{id} | 获取文档详情 | 所有用户 |
| PUT | /api/v1/documents/{id} | 更新文档信息 | 提交者、管理员 |
| DELETE | /api/v1/documents/{id} | 删除文档 | 提交者、管理员 |
| GET | /api/v1/documents/{id}/file | 下载文档文件 | 所有用户 |
| POST | /api/v1/documents/{id}/submit | 提交评审 | 提交者、管理员 |
| POST | /api/v1/documents/{id}/withdraw | 撤回评审 | 提交者、管理员 |

#### 4.3.3 评审模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/reviews/pending | 获取待评审列表 | 评审者、管理员 |
| GET | /api/v1/reviews/{id} | 获取评审详情 | 所有用户 |
| POST | /api/v1/reviews/{id}/submit | 提交评审意见 | 评审者、管理员 |
| GET | /api/v1/documents/{docId}/reviews | 获取文档评审记录 | 所有用户 |
| GET | /api/v1/documents/{docId}/progress | 获取评审进度 | 所有用户 |

#### 4.3.4 评论模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/documents/{docId}/comments | 获取文档评论列表 | 所有用户 |
| POST | /api/v1/documents/{docId}/comments | 添加评论 | 评审者、管理员 |
| PUT | /api/v1/comments/{id} | 更新评论 | 评论作者 |
| DELETE | /api/v1/comments/{id} | 删除评论 | 评论作者、管理员 |
| POST | /api/v1/comments/{id}/reply | 回复评论 | 所有用户 |

#### 4.3.5 归档模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/archives | 搜索归档文档 | 所有用户 |
| POST | /api/v1/documents/{id}/archive | 归档文档 | 管理员 |
| DELETE | /api/v1/archives/{id} | 取消归档 | 管理员 |
| GET | /api/v1/archives/{id}/export | 导出评审报告 | 所有用户 |

#### 4.3.6 用户模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/users | 获取用户列表 | 管理员 |
| GET | /api/v1/users/{id} | 获取用户详情 | 管理员 |
| PUT | /api/v1/users/{id} | 更新用户信息 | 管理员 |
| PUT | /api/v1/users/{id}/role | 更新用户角色 | 管理员 |
| POST | /api/v1/users/sync-ldap | 同步LDAP用户 | 管理员 |

#### 4.3.7 系统配置模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/system/config | 获取系统配置 | 管理员 |
| PUT | /api/v1/system/config | 更新系统配置 | 管理员 |
| GET | /api/v1/system/ldap-config | 获取LDAP配置 | 管理员 |
| PUT | /api/v1/system/ldap-config | 更新LDAP配置 | 管理员 |
| POST | /api/v1/system/ldap-test | 测试LDAP连接 | 管理员 |

### 4.4 API认证流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          认证流程                                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. 用户登录                                                             │
│     ┌──────────┐     POST /api/v1/auth/login      ┌──────────┐         │
│     │  前端    │ ───────────────────────────────→ │  后端    │         │
│     └──────────┘                                   └──────────┘         │
│                                                          │              │
│                                    ┌─────────────────────┘              │
│                                    ▼                                    │
│                           ┌──────────────────┐                         │
│                           │ AD域开启？       │                         │
│                           └──────────────────┘                         │
│                              │         │                               │
│                          是  │         │ 否                            │
│                              ▼         ▼                               │
│                      ┌──────────┐ ┌──────────┐                         │
│                      │ LDAP认证 │ │ 本地认证 │                         │
│                      └──────────┘ └──────────┘                         │
│                              │         │                               │
│                              └────┬────┘                               │
│                                   ▼                                    │
│                           ┌──────────────────┐                         │
│                           │ 生成JWT Token    │                         │
│                           └──────────────────┘                         │
│                                   │                                    │
│                                   ▼                                    │
│     ┌──────────┐     JWT Token         ┌──────────┐                  │
│     │  前端    │ ←───────────────────── │  后端    │                  │
│     └──────────┘                        └──────────┘                  │
│                                                                         │
│  2. 请求API                                                              │
│     ┌──────────┐  Authorization: Bearer <token>  ┌──────────┐         │
│     │  前端    │ ──────────────────────────────→ │  后端    │         │
│     └──────────┘                                  └──────────┘         │
│                                                          │              │
│                                    ┌─────────────────────┘              │
│                                    ▼                                    │
│                           ┌──────────────────┐                         │
│                           │ JWT验证 + 权限   │                         │
│                           └──────────────────┘                         │
│                                   │                                    │
│                                   ▼                                    │
│     ┌──────────┐     响应数据          ┌──────────┐                  │
│     │  前端    │ ←───────────────────── │  后端    │                  │
│     └──────────┘                        └──────────┘                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 5. 数据库设计概要

### 5.1 ER图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          数据库ER图                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐       │
│  │    users    │         │  documents  │         │   reviews   │       │
│  ├─────────────┤         ├─────────────┤         ├─────────────┤       │
│  │ id (PK)     │←──┐     │ id (PK)     │←──┐     │ id (PK)     │       │
│  │ username    │   │     │ title       │   │     │ document_id │──→┘   │
│  │ password    │   │     │ description │   │     │ reviewer_id │──→┐   │
│  │ email       │   │     │ file_path   │   │     │ status      │   │   │
│  │ display_name│   │     │ file_size   │   │     │ decision    │   │   │
│  │ role        │   │     │ file_type   │   │     │ comment     │   │   │
│  │ department  │   │     │ review_type │   │     │ created_at  │   │   │
│  │ ldap_dn     │   │     │ status      │   │     │ updated_at  │   │   │
│  │ created_at  │   │     │ submitter_id│──→┘   │     │ submitted_at │   │   │
│  │ updated_at  │   │     │ created_at  │       └─────────────┘   │   │
│  └─────────────┘   │     │ updated_at  │                         │   │
│        │           │     │ deadline    │                         │   │
│        │           │     │ version     │                         │   │
│        │           │     │ archived    │                         │   │
│        │           │     │ archived_at │                         │   │
│        │           │     └─────────────┘                         │   │
│        │           │           │                                 │   │
│        │           │           │                                 │   │
│        │           │           ▼                                 │   │
│        │           │     ┌─────────────┐                         │   │
│        │           │     │reviewer_    │                         │   │
│        │           │     │assignments  │                         │   │
│        │           │     ├─────────────┤                         │   │
│        │           │     │ id (PK)     │                         │   │
│        │           │     │ document_id │──→│                     │   │
│        │           └────→│ reviewer_id │   │                     │   │
│        │                 │ status      │   │                     │   │
│        │                 │ created_at  │   │                     │   │
│        │                 └─────────────┘   │                     │   │
│        │                                   │                     │   │
│        │           ┌─────────────┐         │                     │   │
│        │           │  comments   │         │                     │   │
│        │           ├─────────────┤         │                     │   │
│        │           │ id (PK)     │         │                     │   │
│        │           │ document_id │─────────┘                     │   │
│        │           │ review_id   │─────────────────────────────→│   │
│        └──────────→│ author_id   │                               │   │
│                      │ content     │                               │   │
│                      │ type        │                               │   │
│                      │ priority    │                               │   │
│                      │ position    │         ┌─────────────┐     │   │
│                      │ parent_id   │         │system_config│     │   │
│                      │ created_at  │         ├─────────────┤     │   │
│                      │ updated_at  │         │ id (PK)     │     │   │
│                      └─────────────┘         │ config_key  │     │   │
│                                              │ config_value│     │   │
│                                              │ description │     │   │
│                                              │ updated_at  │     │   │
│                                              └─────────────┘     │   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 5.2 核心表结构

#### 5.2.1 用户表 (users)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | 密码（本地用户） |
| email | VARCHAR(100) | 邮箱 |
| display_name | VARCHAR(100) | 显示名称 |
| role | VARCHAR(20) | 角色：SUBMITTER/REVIEWER/ADMIN/OBSERVER |
| department | VARCHAR(100) | 部门 |
| ldap_dn | VARCHAR(255) | LDAP Distinguished Name |
| ldap_enabled | BOOLEAN | 是否LDAP用户 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 5.2.2 文档表 (documents)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| title | VARCHAR(255) | 文档标题 |
| description | TEXT | 文档描述 |
| file_path | VARCHAR(500) | MinIO文件路径 |
| file_name | VARCHAR(255) | 原始文件名 |
| file_size | BIGINT | 文件大小（字节） |
| file_type | VARCHAR(50) | 文件类型 |
| review_type | VARCHAR(20) | 评审类型：TECHNICAL/DESIGN/CODE |
| status | VARCHAR(20) | 状态：DRAFT/PENDING/REVIEWING/REVISION/APPROVED/REJECTED/ARCHIVED |
| submitter_id | BIGINT | 提交者ID |
| deadline | TIMESTAMP | 评审截止时间 |
| version | VARCHAR(20) | 版本号 |
| archived | BOOLEAN | 是否已归档 |
| archived_at | TIMESTAMP | 归档时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 5.2.3 评审表 (reviews)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| document_id | BIGINT | 文档ID |
| reviewer_id | BIGINT | 评审者ID |
| status | VARCHAR(20) | 状态：PENDING/IN_PROGRESS/SUBMITTED |
| decision | VARCHAR(20) | 决定：APPROVED/REJECTED/REVISION_REQUIRED |
| overall_comment | TEXT | 总体评价 |
| pros | TEXT | 优点 |
| cons | TEXT | 问题 |
| suggestions | TEXT | 建议 |
| submitted_at | TIMESTAMP | 提交时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 5.2.4 评审者分配表 (reviewer_assignments)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| document_id | BIGINT | 文档ID |
| reviewer_id | BIGINT | 评审者ID |
| status | VARCHAR(20) | 状态：PENDING/REVIEWING/COMPLETED |
| created_at | TIMESTAMP | 创建时间 |

#### 5.2.5 评论表 (comments)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| document_id | BIGINT | 文档ID |
| review_id | BIGINT | 评审ID（可选） |
| author_id | BIGINT | 作者ID |
| content | TEXT | 评论内容 |
| type | VARCHAR(20) | 类型：ISSUE/SUGGESTION/QUESTION |
| priority | VARCHAR(10) | 优先级：HIGH/MEDIUM/LOW |
| position | VARCHAR(100) | 行内批注位置 |
| parent_id | BIGINT | 父评论ID |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 5.2.6 系统配置表 (system_config)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| config_key | VARCHAR(100) | 配置键，唯一 |
| config_value | TEXT | 配置值 |
| description | VARCHAR(500) | 配置描述 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

**预置配置项**:

| config_key | 说明 | 默认值 |
|------------|------|--------|
| ldap.enabled | LDAP认证开关 | false |
| ldap.url | LDAP服务器地址 | ldap://localhost:389 |
| ldap.base-dn | Base DN | dc=company,dc=com |
| ldap.bind-dn | 绑定账号 | cn=admin,dc=company,dc=com |
| ldap.bind-password | 绑定密码 | - |
| ldap.user-attribute | 用户名属性 | sAMAccountName |
| review.pass-condition | 通过条件 | ALL |

---

## 6. AD域集成方案

### 6.1 整体设计

AD域集成采用**可配置、可开关**的设计，支持两种认证模式：

1. **AD域认证模式**：用户通过企业AD账号登录
2. **本地认证模式**：用户通过本地账号登录

### 6.2 配置界面设计

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        AD域配置界面                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  AD域认证开关                                                            │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │  □ 启用AD域认证                                                 │    │
│  │  启用后，用户将使用企业AD账号登录                               │    │
│  └────────────────────────────────────────────────────────────────┘    │
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │  AD域服务器配置                                                 │    │
│  │  ─────────────────────────────────────────────────────────────  │    │
│  │                                                                 │    │
│  │  服务器地址: [ldap://ad.company.com                      ]      │    │
│  │  端口:       [389                                        ]      │    │
│  │  使用SSL:    □ (LDAPS)                                          │    │
│  │                                                                 │    │
│  │  Base DN:    [dc=company,dc=com                          ]      │    │
│  │  绑定账号:   [cn=admin,dc=company,dc=com                  ]      │    │
│  │  绑定密码:   [••••••••                                   ]      │    │
│  │                                                                 │    │
│  │  用户名属性: [sAMAccountName                              ]      │    │
│  │  邮箱属性:   [mail                                        ]      │    │
│  │  姓名属性:   [cn                                          ]      │    │
│  │                                                                 │    │
│  │  ─────────────────────────────────────────────────────────────  │    │
│  │                                                                 │    │
│  │  [测试连接]    [保存配置]    [重置]                            │    │
│  │                                                                 │    │
│  └────────────────────────────────────────────────────────────────┘    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6.3 认证流程

#### 6.3.1 AD域认证流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        AD域认证流程                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  用户输入企业邮箱/用户名 + 密码                                          │
│                    │                                                    │
│                    ▼                                                    │
│           ┌──────────────────┐                                         │
│           │ 检查AD域是否开启 │                                         │
│           └──────────────────┘                                         │
│                    │                                                    │
│         ┌──────────┴──────────┐                                        │
│         │                     │                                        │
│     开启│                     │关闭                                    │
│         ▼                     ▼                                        │
│  ┌─────────────┐      ┌─────────────┐                                 │
│  │ LDAP认证    │      │ 本地认证    │                                 │
│  └─────────────┘      └─────────────┘                                 │
│         │                     │                                        │
│         ▼                     │                                        │
│  ┌─────────────┐              │                                        │
│  │连接AD服务器 │              │                                        │
│  └─────────────┘              │                                        │
│         │                     │                                        │
│         ▼                     │                                        │
│  ┌─────────────┐              │                                        │
│  │验证用户凭证 │              │                                        │
│  └─────────────┘              │                                        │
│         │                     │                                        │
│    ┌────┴────┐               │                                        │
│    │         │               │                                        │
│ 成功│       失败│               │                                        │
│    ▼         ▼               │                                        │
│ ┌───────┐ ┌───────┐         │                                        │
│ │获取用户│ │返回错误│         │                                        │
│ │信息    │ │       │         │                                        │
│ └───────┘ └───────┘         │                                        │
│    │                        │                                        │
│    ▼                        │                                        │
│ ┌─────────────┐             │                                        │
│ │同步用户信息 │             │                                        │
│ │到本地数据库 │             │                                        │
│ └─────────────┘             │                                        │
│    │                        │                                        │
│    └────────────┬───────────┘                                        │
│                 ▼                                                    │
│         ┌─────────────┐                                             │
│         │生成JWT Token│                                             │
│         └─────────────┘                                             │
│                 │                                                    │
│                 ▼                                                    │
│         ┌─────────────┐                                             │
│         │返回登录成功 │                                             │
│         └─────────────┘                                             │
│                                                                      │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6.4 Spring Security配置

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private LdapAuthenticationProvider ldapAuthenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/system/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 动态启用/禁用LDAP认证
    @Bean
    @ConditionalOnProperty(name = "ldap.enabled", havingValue = "true")
    public LdapAuthenticationProvider ldapAuthenticationProvider() {
        return ldapAuthenticationProvider;
    }
}
```

### 6.5 LDAP配置类

```java
@Configuration
@ConfigurationProperties(prefix = "ldap")
@Data
public class LdapConfig {
    private boolean enabled = false;
    private String url;
    private String baseDn;
    private String bindDn;
    private String bindPassword;
    private String userAttribute = "sAMAccountName";
    private String emailAttribute = "mail";
    private String nameAttribute = "cn";
}

@Service
public class LdapService {
    
    @Autowired
    private LdapConfig ldapConfig;
    
    @Autowired
    private UserRepository userRepository;
    
    public boolean authenticate(String username, String password) {
        if (!ldapConfig.isEnabled()) {
            return false;
        }
        
        try {
            LdapContextSource contextSource = new LdapContextSource();
            contextSource.setUrl(ldapConfig.getUrl());
            contextSource.setBase(ldapConfig.getBaseDn());
            contextSource.setUserDn(ldapConfig.getBindDn());
            contextSource.setPassword(ldapConfig.getBindPassword());
            contextSource.afterPropertiesSet();
            
            LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
            ldapTemplate.authenticate(
                LdapQueryBuilder.query()
                    .where(ldapConfig.getUserAttribute()).is(username),
                password
            );
            
            // 认证成功，同步用户信息
            syncUserFromLdap(username);
            return true;
        } catch (Exception e) {
            log.error("LDAP认证失败", e);
            return false;
        }
    }
    
    private void syncUserFromLdap(String username) {
        // 从AD域获取用户信息并同步到本地数据库
        // ...
    }
}
```

### 6.6 动态配置实现

系统配置存储在数据库中，支持运行时修改：

```java
@Service
public class SystemConfigService {
    
    @Autowired
    private SystemConfigRepository configRepository;
    
    @Autowired
    private LdapConfig ldapConfig;
    
    public void updateLdapConfig(LdapConfigDto dto) {
        // 更新数据库配置
        saveConfig("ldap.enabled", String.valueOf(dto.isEnabled()));
        saveConfig("ldap.url", dto.getUrl());
        saveConfig("ldap.base-dn", dto.getBaseDn());
        saveConfig("ldap.bind-dn", dto.getBindDn());
        saveConfig("ldap.bind-password", encryptPassword(dto.getBindPassword()));
        saveConfig("ldap.user-attribute", dto.getUserAttribute());
        
        // 更新运行时配置
        ldapConfig.setEnabled(dto.isEnabled());
        ldapConfig.setUrl(dto.getUrl());
        // ...
    }
    
    public boolean testLdapConnection(LdapConfigDto dto) {
        // 测试LDAP连接
        try {
            LdapContextSource contextSource = new LdapContextSource();
            contextSource.setUrl(dto.getUrl());
            contextSource.setBase(dto.getBaseDn());
            contextSource.setUserDn(dto.getBindDn());
            contextSource.setPassword(dto.getBindPassword());
            contextSource.afterPropertiesSet();
            contextSource.getReadOnlyContext();
            return true;
        } catch (Exception e) {
            log.error("LDAP连接测试失败", e);
            return false;
        }
    }
}
```

---

## 7. 关键技术实现

### 7.1 并行评审实现

```java
@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private ReviewerAssignmentRepository assignmentRepository;
    
    /**
     * 提交评审意见（支持并行评审）
     */
    @Transactional
    public Review submitReview(Long documentId, Long reviewerId, ReviewRequest request) {
        // 1. 获取评审者分配记录
        ReviewerAssignment assignment = assignmentRepository
            .findByDocumentIdAndReviewerId(documentId, reviewerId)
            .orElseThrow(() -> new BusinessException("未找到评审分配记录"));
        
        // 2. 创建评审记录
        Review review = new Review();
        review.setDocumentId(documentId);
        review.setReviewerId(reviewerId);
        review.setDecision(request.getDecision());
        review.setOverallComment(request.getOverallComment());
        review.setSubmittedAt(LocalDateTime.now());
        review = reviewRepository.save(review);
        
        // 3. 更新分配状态
        assignment.setStatus(ReviewerStatus.COMPLETED);
        assignmentRepository.save(assignment);
        
        // 4. 更新文档状态（使用乐观锁保证一致性）
        updateDocumentStatus(documentId);
        
        return review;
    }
    
    /**
     * 更新文档状态（基于评审结果）
     */
    @Transactional
    public void updateDocumentStatus(Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new BusinessException("文档不存在"));
        
        // 获取所有评审分配
        List<ReviewerAssignment> assignments = assignmentRepository
            .findByDocumentId(documentId);
        
        long totalCount = assignments.size();
        long completedCount = assignments.stream()
            .filter(a -> a.getStatus() == ReviewerStatus.COMPLETED)
            .count();
        long approvedCount = assignments.stream()
            .filter(a -> a.getStatus() == ReviewerStatus.COMPLETED)
            .filter(a -> {
                Review r = reviewRepository.findByDocumentIdAndReviewerId(
                    documentId, a.getReviewerId()).orElse(null);
                return r != null && r.getDecision() == Decision.APPROVED;
            })
            .count();
        
        // 更新状态
        if (completedCount == 0) {
            // 首位评审者开始
            document.setStatus(DocumentStatus.REVIEWING);
        } else if (completedCount == totalCount) {
            // 所有评审完成，判断结果
            String passCondition = configService.getConfig("review.pass-condition");
            boolean passed = evaluatePassCondition(passCondition, approvedCount, totalCount);
            document.setStatus(passed ? DocumentStatus.APPROVED : DocumentStatus.REJECTED);
        }
        
        documentRepository.save(document);
    }
    
    /**
     * 评估通过条件
     */
    private boolean evaluatePassCondition(String condition, long approved, long total) {
        switch (condition) {
            case "ALL":
                return approved == total;
            case "MAJORITY":
                return approved > total / 2;
            case "THREE_QUARTERS":
                return approved >= total * 0.75;
            default:
                return approved == total;
        }
    }
}
```

### 7.2 文件上传实现

```java
@Service
public class FileService {
    
    @Autowired
    private MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    /**
     * 上传文件到MinIO
     */
    public String uploadFile(MultipartFile file, String documentId) {
        try {
            String fileName = UUID.randomUUID().toString();
            String objectName = String.format("documents/%s/%s", documentId, fileName);
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            return objectName;
        } catch (Exception e) {
            throw new BusinessException("文件上传失败", e);
        }
    }
    
    /**
     * 下载文件
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            throw new BusinessException("文件下载失败", e);
        }
    }
    
    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            throw new BusinessException("文件删除失败", e);
        }
    }
}
```

---

## 8. 部署方案

### 8.1 Docker Compose配置

```yaml
version: '3.8'

services:
  # 前端服务
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "3000:80"
    depends_on:
      - backend
    networks:
      - app-network

  # 后端服务
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/docreview
      - SPRING_DATASOURCE_USERNAME=docreview
      - SPRING_DATASOURCE_PASSWORD=docreview123
      - MINIO_ENDPOINT=http://minio:9000
      - MINIO_ACCESS_KEY=minioadmin
      - MINIO_SECRET_KEY=minioadmin
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    depends_on:
      - postgres
      - minio
      - redis
    networks:
      - app-network

  # PostgreSQL数据库
  postgres:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=docreview
      - POSTGRES_USER=docreview
      - POSTGRES_PASSWORD=docreview123
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - app-network

  # MinIO对象存储
  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      - MINIO_ROOT_USER=minioadmin
      - MINIO_ROOT_PASSWORD=minioadmin
    volumes:
      - minio-data:/data
    ports:
      - "9000:9000"
      - "9001:9001"
    networks:
      - app-network

  # Redis缓存
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - app-network

volumes:
  postgres-data:
  minio-data:
  redis-data:

networks:
  app-network:
    driver: bridge
```

### 8.2 环境配置

```yaml
# application-prod.yml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}

minio:
  endpoint: ${MINIO_ENDPOINT}
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
  bucket-name: doc-review

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

ldap:
  enabled: false
  url: ${LDAP_URL:ldap://localhost:389}
  base-dn: ${LDAP_BASE_DN:dc=company,dc=com}
  bind-dn: ${LDAP_BIND_DN:cn=admin,dc=company,dc=com}
  bind-password: ${LDAP_BIND_PASSWORD:}
  user-attribute: ${LDAP_USER_ATTRIBUTE:sAMAccountName}
```

---

## 9. 安全设计

### 9.1 认证授权

- **JWT Token**: 无状态认证，Token过期时间24小时
- **RBAC**: 基于角色的访问控制（SUBMITTER/REVIEWER/ADMIN/OBSERVER）
- **方法级安全**: 使用@PreAuthorize注解控制接口权限

### 9.2 数据安全

- **传输加密**: HTTPS/TLS
- **密码加密**: BCrypt
- **敏感配置**: 环境变量注入，不存储在代码中
- **LDAP密码**: AES加密存储

### 9.3 审计日志

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    private String username;
    private String action;
    private String resource;
    private String details;
    
    @CreatedDate
    private LocalDateTime createdAt;
}
```

---

## 10. 性能优化

### 10.1 缓存策略

| 数据类型 | 缓存方式 | 过期时间 |
|----------|----------|----------|
| 用户信息 | Redis | 30分钟 |
| 系统配置 | Redis | 10分钟 |
| 文档列表 | Redis | 5分钟 |
| 评审进度 | Redis | 1分钟 |

### 10.2 数据库优化

- 索引设计：文档状态、提交者、评审者
- 分页查询：使用Pageable避免全表扫描
- 连接池：HikariCP，最大连接数20

### 10.3 文件上传优化

- 大文件分片上传
- 断点续传
- 上传进度显示

---

## 11. 开发计划

### 11.1 开发阶段划分

| 阶段 | 内容 | 工期 |
|------|------|------|
| **阶段1** | 项目骨架搭建 | 0.5天 |
| **阶段2** | 用户认证、AD域集成 | 1天 |
| **阶段3** | 文档上传、文件存储 | 1天 |
| **阶段4** | 评审流程、并行评审 | 1.5天 |
| **阶段5** | 评论、通知功能 | 1天 |
| **阶段6** | 归档检索、系统配置 | 1天 |
| **阶段7** | 测试、文档、优化 | 1天 |

### 11.2 里程碑

| 里程碑 | 内容 | 预计完成 |
|--------|------|----------|
| M1 | 项目骨架、基础框架 | Day 1 |
| M2 | 认证模块完成 | Day 2 |
| M3 | 文档模块完成 | Day 3 |
| M4 | 评审模块完成 | Day 5 |
| M5 | 全部功能完成 | Day 6 |
| M6 | 测试通过、文档完成 | Day 7 |

---

## 12. 附录

### 12.1 技术栈版本详情

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 17 LTS | 长期支持版本 |
| Spring Boot | 3.2.x | 最新稳定版 |
| Spring Security | 6.2.x | 随Spring Boot版本 |
| Spring Data JPA | 3.2.x | 随Spring Boot版本 |
| PostgreSQL | 15.x | LTS版本 |
| MinIO | RELEASE.2024-x | 最新稳定版 |
| Redis | 7.x | 最新稳定版 |
| React | 18.x | 最新稳定版 |
| TypeScript | 5.x | 最新稳定版 |
| Ant Design | 5.x | 最新稳定版 |
| Vite | 5.x | 最新稳定版 |

### 12.2 参考文档

- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Spring Security LDAP](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/ldap.html)
- [MinIO Java SDK](https://min.io/docs/minio/linux/developers/java/minio-java.html)
- [Ant Design组件库](https://ant.design/components/overview-cn/)
- [React官方文档](https://react.dev/)

---

**文档变更记录**

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|----------|
| 1.0 | 2026-03-05 | fullstack-dev | 初稿 |

---

*本文档由 fullstack-dev 编写，供团队评审*
