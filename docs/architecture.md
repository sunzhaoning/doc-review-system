# 文档评审系统 - 技术架构设计文档

**版本**: 2.0
**日期**: 2026-03-05
**作者**: fullstack-dev
**状态**: 待审核

**变更记录**:
- v2.0: 技术栈调整（Vue+MyBatis Plus+Sa-Token）、新增完整RBAC权限管理模块
- v1.0: 初稿

---

## 1. 技术选型

### 1.1 整体技术栈

| 层级 | 技术选型 | 版本 | 说明 |
|------|----------|------|------|
| **前端** | Vue 3 + TypeScript | 3.4.x / 5.x | 现代化响应式框架，组合式API |
| **UI组件库** | Element Plus | 2.5.x | Vue3生态企业级UI组件库 |
| **构建工具** | Vite | 5.x | 快速构建，热更新 |
| **状态管理** | Pinia | 2.x | Vue3官方推荐状态管理 |
| **后端框架** | Spring Boot | 3.2.x | Java生态，企业级应用 |
| **ORM** | MyBatis Plus | 3.5.x | 增强型MyBatis，简化CRUD |
| **认证框架** | Sa-Token | 1.37.x | 轻量级权限认证框架 |
| **数据库** | PostgreSQL | 15.x | 开源关系型数据库 |
| **文件存储** | MinIO | RELEASE.2024-x | 对象存储，S3兼容 |
| **缓存** | Redis | 7.x | 会话管理、热点数据缓存 |

### 1.2 选型理由

#### 前端选型

**Vue 3 + TypeScript + Element Plus**

- **Vue 3**: 组合式API，更好的TypeScript支持，性能优化
- **TypeScript**: 类型安全，减少运行时错误，提升代码质量
- **Element Plus**: Vue3生态最成熟的UI组件库，企业级组件完善
- **Pinia**: 轻量级状态管理，更好的TypeScript支持
- **Vite**: 构建速度快，开发体验好

#### 后端选型

**Spring Boot + MyBatis Plus + Sa-Token**

- **Spring Boot**: 约定优于配置，快速搭建企业级应用
- **MyBatis Plus**: 
  - 无侵入：MyBatis Plus在MyBatis基础上只做增强不做改变
  - 强大的CRUD操作：内置通用Mapper、Service，少量配置即可实现单表大部分CRUD操作
  - 支持Lambda表达式：方便编写各类查询条件
  - 内置分页插件：基于MyBatis物理分页，配置好插件后即可使用
  - 代码生成器：可快速生成Entity、Mapper、Service、Controller代码
- **Sa-Token**:
  - 轻量级：jar包仅100KB+，无复杂依赖
  - 功能强大：登录认证、权限认证、Session会话、单点登录、OAuth2.0
  - 简单易用：配置简单，API简洁
  - 支持多账号认证体系：多登录类型、多Token风格
  - 内置权限验证：支持角色、权限、菜单三级权限

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
│                            前端层 (Vue 3 SPA)                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │  文档管理   │  │  评审流程   │  │  归档检索   │  │  系统设置   │    │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                     状态管理 (Pinia)                             │   │
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
│  │  AuthService | DocumentService | ReviewService | PermissionService │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                     数据访问层 (MyBatis Plus)                    │   │
│  │  UserMapper | DocumentMapper | ReviewMapper | MenuMapper | ...  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                     安全层 (Sa-Token)                            │   │
│  │  登录认证 | RBAC权限 | 菜单权限 | 数据权限 | LDAP集成             │   │
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
│ - 用户数据        │  │ - 文档文件        │  │ - Sa-Token会话    │
│ - 文档元数据      │  │ - 附件            │  │ - 评审进度缓存    │
│ - 评审记录        │  │ - 导出报告        │  │ - 配置缓存        │
│ - 评论数据        │  │                   │  │ - 权限缓存        │
│ - RBAC权限数据    │  │                   │  │                   │
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

### 2.2 RBAC权限模型架构

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          RBAC权限模型                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────┐       ┌─────────┐       ┌─────────┐       ┌─────────┐    │
│  │  用户   │──────→│ 用户角色│←──────│  角色   │──────→│角色菜单 │    │
│  │  User   │  N:N  │UserRole │  N:N  │  Role   │  N:N  │RoleMenu │    │
│  └─────────┘       └─────────┘       └─────────┘       └─────────┘    │
│       │                                    │                   │        │
│       │                                    │                   │        │
│       │                              ┌─────┴─────┐             │        │
│       │                              │           │             │        │
│       │                        ┌─────┴─────┐     │             │        │
│       │                        │           │     │             │        │
│       │                   ┌────┴────┐ ┌────┴────┐│             │        │
│       │                   │角色权限 │ │数据权限 ││             │        │
│       │                   │RolePerm │ │RoleData ││             │        │
│       │                   └────┬────┘ └────┬────┘│             │        │
│       │                        │           │     │             │        │
│       │                        │           │     ▼             │        │
│       │                        ▼           │  ┌─────────┐     │        │
│       │                   ┌─────────┐      │  │  菜单   │     │        │
│       │                   │  权限   │      │  │  Menu   │     │        │
│       │                   │Permission│     │  └─────────┘     │        │
│       │                   └─────────┘      │                   │        │
│       │                                    │                   │        │
│       └────────────────────────────────────┴───────────────────┘        │
│                                                                         │
│  权限粒度：                                                              │
│  1. 菜单权限：控制用户可见菜单                                          │
│  2. 按钮权限：控制页面按钮的显示/隐藏                                   │
│  3. 数据权限：控制用户可访问的数据范围（全部/本部门/本人等）            │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.3 部署架构

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
│   │   │   ├── SaTokenConfig.java             # Sa-Token配置
│   │   │   ├── MybatisPlusConfig.java         # MyBatis Plus配置
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
│   │   │   ├── RoleController.java            # 角色管理接口
│   │   │   ├── MenuController.java            # 菜单管理接口
│   │   │   ├── PermissionController.java      # 权限管理接口
│   │   │   └── SystemController.java          # 系统配置接口
│   │   ├── service/                            # 服务层
│   │   │   ├── AuthService.java               # 认证服务
│   │   │   ├── DocumentService.java           # 文档服务
│   │   │   ├── ReviewService.java             # 评审服务
│   │   │   ├── CommentService.java            # 评论服务
│   │   │   ├── ArchiveService.java            # 归档服务
│   │   │   ├── UserService.java               # 用户服务
│   │   │   ├── RoleService.java               # 角色服务
│   │   │   ├── MenuService.java               # 菜单服务
│   │   │   ├── PermissionService.java         # 权限服务
│   │   │   ├── DataScopeService.java          # 数据权限服务
│   │   │   ├── FileService.java               # 文件服务
│   │   │   ├── NotificationService.java       # 通知服务
│   │   │   └── LdapService.java               # LDAP服务
│   │   ├── mapper/                             # MyBatis Plus Mapper
│   │   │   ├── UserMapper.java
│   │   │   ├── RoleMapper.java
│   │   │   ├── MenuMapper.java
│   │   │   ├── PermissionMapper.java
│   │   │   ├── DocumentMapper.java
│   │   │   ├── ReviewMapper.java
│   │   │   ├── CommentMapper.java
│   │   │   └── SystemConfigMapper.java
│   │   ├── entity/                             # 实体类
│   │   │   ├── User.java
│   │   │   ├── Role.java
│   │   │   ├── Menu.java
│   │   │   ├── Permission.java
│   │   │   ├── UserRole.java
│   │   │   ├── RoleMenu.java
│   │   │   ├── RolePermission.java
│   │   │   ├── DataScope.java
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
│   │   │   │   ├── RoleRequest.java
│   │   │   │   ├── MenuRequest.java
│   │   │   │   └── ...
│   │   │   └── response/
│   │   │       ├── UserResponse.java
│   │   │       ├── DocumentResponse.java
│   │   │       ├── ReviewResponse.java
│   │   │       ├── MenuTreeResponse.java
│   │   │       └── ...
│   │   ├── enums/                              # 枚举类
│   │   │   ├── DocumentStatus.java            # 文档状态
│   │   │   ├── ReviewDecision.java            # 评审决定
│   │   │   ├── UserRole.java                  # 用户角色
│   │   │   ├── MenuType.java                  # 菜单类型
│   │   │   ├── DataScopeType.java             # 数据权限类型
│   │   │   └── NotificationType.java          # 通知类型
│   │   ├── exception/                          # 异常处理
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── BusinessException.java
│   │   │   └── ErrorCode.java
│   │   ├── security/                           # 安全模块
│   │   │   ├── StpInterfaceImpl.java          # Sa-Token权限接口实现
│   │   │   ├── StpUtil.java                   # Sa-Token工具类
│   │   │   ├── DataScopeAspect.java           # 数据权限切面
│   │   │   └── LdapAuthService.java           # LDAP认证服务
│   │   └── util/                               # 工具类
│   │       ├── FileUtils.java
│   │       └── DateUtils.java
│   └── src/main/resources/
│       ├── application.yml                     # 主配置
│       ├── application-dev.yml                 # 开发环境配置
│       ├── application-prod.yml                # 生产环境配置
│       └── mapper/                             # MyBatis XML映射文件
│           ├── UserMapper.xml
│           ├── RoleMapper.xml
│           ├── MenuMapper.xml
│           └── ...
├── frontend/
│   ├── src/
│   │   ├── main.ts                            # 入口文件
│   │   ├── App.vue                            # 根组件
│   │   ├── components/                         # 组件
│   │   │   ├── Layout/
│   │   │   │   ├── MainLayout.vue
│   │   │   │   ├── Header.vue
│   │   │   │   └── Sidebar.vue
│   │   │   ├── common/                        # 通用组件
│   │   │   │   ├── Button/
│   │   │   │   ├── Input/
│   │   │   │   ├── Modal/
│   │   │   │   └── Table/
│   │   │   └── business/                      # 业务组件
│   │   │       ├── DocumentUpload/
│   │   │       ├── ReviewProgress/
│   │   │       └── CommentList/
│   │   ├── views/                              # 页面
│   │   │   ├── Login.vue
│   │   │   ├── Dashboard.vue
│   │   │   ├── DocumentList.vue
│   │   │   ├── DocumentDetail.vue
│   │   │   ├── ReviewPage.vue
│   │   │   ├── Archive.vue
│   │   │   ├── system/                        # 系统管理
│   │   │   │   ├── UserManage.vue
│   │   │   │   ├── RoleManage.vue
│   │   │   │   ├── MenuManage.vue
│   │   │   │   └── PermissionManage.vue
│   │   │   └── SystemSettings.vue
│   │   ├── api/                                # API服务
│   │   │   ├── request.ts                     # Axios封装
│   │   │   ├── auth.ts
│   │   │   ├── document.ts
│   │   │   ├── review.ts
│   │   │   ├── user.ts
│   │   │   ├── role.ts
│   │   │   └── menu.ts
│   │   ├── stores/                             # Pinia状态管理
│   │   │   ├── authStore.ts
│   │   │   ├── documentStore.ts
│   │   │   ├── menuStore.ts
│   │   │   └── permissionStore.ts
│   │   ├── router/                             # 路由配置
│   │   │   ├── index.ts
│   │   │   └── guards.ts                      # 路由守卫
│   │   ├── hooks/                              # 自定义Hooks
│   │   │   ├── useAuth.ts
│   │   │   ├── usePermission.ts
│   │   │   └── useNotification.ts
│   │   ├── types/                              # TypeScript类型
│   │   │   ├── user.ts
│   │   │   ├── document.ts
│   │   │   ├── review.ts
│   │   │   ├── menu.ts
│   │   │   └── permission.ts
│   │   ├── directives/                         # 自定义指令
│   │   │   └── permission.ts                  # 权限指令 v-permission
│   │   └── utils/                              # 工具函数
│   │       ├── format.ts
│   │       └── storage.ts
│   └── package.json
├── docker-compose.yml
└── README.md
```

### 3.2 模块职责说明

| 模块 | 职责 | 依赖 |
|------|------|------|
| **auth** | 认证授权、Sa-Token管理、LDAP集成 | user, role, permission |
| **document** | 文档上传、元数据管理、版本控制 | file, user, review |
| **review** | 评审流程管理、并行评审、状态流转 | document, user, comment |
| **comment** | 评审意见、行内批注、意见讨论 | document, user, review |
| **archive** | 文档归档、全文检索、批量导出 | document, file |
| **user** | 用户管理、用户信息同步 | auth, role |
| **role** | 角色管理、角色权限分配 | menu, permission, data-scope |
| **menu** | 菜单管理、菜单树构建、权限控制 | permission |
| **permission** | 权限管理、权限标识、按钮权限 | - |
| **data-scope** | 数据权限、部门数据隔离 | user, role |
| **file** | 文件存储、上传下载、MinIO集成 | - |
| **notification** | 邮件通知、站内消息 | user, review |
| **system-config** | 系统配置、AD域配置、开关管理 | - |

---

## 4. RBAC权限管理设计

### 4.1 权限模型

本系统采用**RBAC（基于角色的访问控制）模型**，包含以下核心概念：

- **用户（User）**：系统使用者
- **角色（Role）**：权限的集合，如管理员、提交者、评审者等
- **菜单（Menu）**：系统菜单项，支持树形结构
- **权限（Permission）**：具体的操作权限，如"文档:创建"、"文档:删除"
- **数据权限（DataScope）**：控制用户可访问的数据范围

### 4.2 权限粒度

#### 4.2.1 菜单权限

控制用户可见的菜单项，支持树形结构：

```
系统管理
├── 用户管理
│   ├── 用户列表
│   └── 用户角色分配
├── 角色管理
│   ├── 角色列表
│   └── 角色权限分配
├── 菜单管理
└── 权限管理

文档管理
├── 我的文档
├── 待评审
└── 归档检索
```

#### 4.2.2 按钮权限

控制页面按钮的显示/隐藏，使用权限标识：

```
文档列表页面：
- doc:create    → 创建文档按钮
- doc:edit      → 编辑文档按钮
- doc:delete    → 删除文档按钮
- doc:submit    → 提交评审按钮
- doc:withdraw  → 撤回评审按钮

评审页面：
- review:approve  → 通过按钮
- review:reject   → 拒绝按钮
- review:comment  → 评论按钮
```

#### 4.2.3 数据权限

控制用户可访问的数据范围：

| 数据权限类型 | 说明 | SQL条件 |
|--------------|------|---------|
| 全部数据 | 可访问所有数据 | 无限制 |
| 本部门及下级 | 可访问本部门及下属部门数据 | dept_id IN (本部门及下级部门ID列表) |
| 本部门 | 可访问本部门数据 | dept_id = 当前用户部门ID |
| 仅本人 | 只能访问自己的数据 | user_id = 当前用户ID |
| 自定义 | 自定义数据范围 | 自定义SQL条件 |

### 4.3 数据库设计 - RBAC表

#### 4.3.1 用户表 (sys_user)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| real_name | VARCHAR(50) | 真实姓名 |
| avatar | VARCHAR(255) | 头像URL |
| dept_id | BIGINT | 部门ID |
| status | TINYINT | 状态：0禁用 1启用 |
| ldap_dn | VARCHAR(255) | LDAP Distinguished Name |
| ldap_enabled | BOOLEAN | 是否LDAP用户 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | TINYINT | 逻辑删除：0未删除 1已删除 |

#### 4.3.2 角色表 (sys_role)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| role_name | VARCHAR(50) | 角色名称 |
| role_code | VARCHAR(50) | 角色编码，唯一 |
| description | VARCHAR(255) | 角色描述 |
| data_scope | TINYINT | 数据权限范围：1全部 2自定义 3本部门及下级 4本部门 5仅本人 |
| status | TINYINT | 状态：0禁用 1启用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

#### 4.3.3 菜单表 (sys_menu)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| parent_id | BIGINT | 父菜单ID，0为顶级菜单 |
| menu_name | VARCHAR(50) | 菜单名称 |
| menu_code | VARCHAR(50) | 菜单编码 |
| menu_type | TINYINT | 类型：1目录 2菜单 3按钮 |
| path | VARCHAR(255) | 路由地址 |
| component | VARCHAR(255) | 组件路径 |
| perms | VARCHAR(100) | 权限标识 |
| icon | VARCHAR(50) | 菜单图标 |
| sort | INT | 排序号 |
| visible | TINYINT | 是否可见：0隐藏 1显示 |
| status | TINYINT | 状态：0禁用 1启用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

#### 4.3.4 权限表 (sys_permission)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| permission_name | VARCHAR(50) | 权限名称 |
| permission_code | VARCHAR(100) | 权限标识，唯一 |
| resource_type | VARCHAR(20) | 资源类型：menu/button/api |
| resource_url | VARCHAR(255) | 资源URL |
| method | VARCHAR(10) | HTTP方法 |
| description | VARCHAR(255) | 权限描述 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 4.3.5 用户角色关联表 (sys_user_role)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID |
| role_id | BIGINT | 角色ID |
| created_at | TIMESTAMP | 创建时间 |

#### 4.3.6 角色菜单关联表 (sys_role_menu)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| role_id | BIGINT | 角色ID |
| menu_id | BIGINT | 菜单ID |
| created_at | TIMESTAMP | 创建时间 |

#### 4.3.7 角色权限关联表 (sys_role_permission)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| role_id | BIGINT | 角色ID |
| permission_id | BIGINT | 权限ID |
| created_at | TIMESTAMP | 创建时间 |

#### 4.3.8 部门表 (sys_dept)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| parent_id | BIGINT | 父部门ID |
| dept_name | VARCHAR(50) | 部门名称 |
| dept_code | VARCHAR(50) | 部门编码 |
| leader | VARCHAR(50) | 部门负责人 |
| phone | VARCHAR(20) | 联系电话 |
| sort | INT | 排序号 |
| status | TINYINT | 状态：0禁用 1启用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

#### 4.3.9 角色部门关联表 (sys_role_dept) - 数据权限

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| role_id | BIGINT | 角色ID |
| dept_id | BIGINT | 部门ID |
| created_at | TIMESTAMP | 创建时间 |

### 4.4 Sa-Token权限集成

#### 4.4.1 Sa-Token配置

```java
@Configuration
public class SaTokenConfig {
    
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogic("login") {
            @Override
            public String getTokenValue() {
                // 从请求头获取Token
                HttpServletRequest request = 
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
                return request.getHeader("Authorization");
            }
        };
    }
    
    @Bean
    public StpInterface stpInterface() {
        return new StpInterfaceImpl();
    }
}
```

#### 4.4.2 权限接口实现

```java
@Component
public class StpInterfaceImpl implements StpInterface {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private PermissionService permissionService;
    
    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        return permissionService.getPermissionCodesByUserId(userId);
    }
    
    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        return userService.getRoleCodesByUserId(userId);
    }
}
```

#### 4.4.3 数据权限切面

```java
@Aspect
@Component
public class DataScopeAspect {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DeptService deptService;
    
    @Before("@annotation(dataScope)")
    public void doBefore(JoinPoint point, DataScope dataScope) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        
        // 获取用户角色
        List<Role> roles = userService.getRolesByUserId(userId);
        
        StringBuilder sql = new StringBuilder();
        
        for (Role role : roles) {
            Integer dataScopeType = role.getDataScope();
            
            if (dataScopeType == DataScopeType.ALL) {
                // 全部数据权限，不加限制
                return;
            } else if (dataScopeType == DataScopeType.DEPT_AND_CHILD) {
                // 本部门及下级
                List<Long> deptIds = deptService.getChildDeptIds(user.getDeptId());
                sql.append(" OR dept_id IN (").append(StringUtils.join(deptIds, ",")).append(")");
            } else if (dataScopeType == DataScopeType.DEPT) {
                // 本部门
                sql.append(" OR dept_id = ").append(user.getDeptId());
            } else if (dataScopeType == DataScopeType.SELF) {
                // 仅本人
                sql.append(" OR user_id = ").append(userId);
            } else if (dataScopeType == DataScopeType.CUSTOM) {
                // 自定义
                List<Long> deptIds = deptService.getDeptIdsByRoleId(role.getId());
                sql.append(" OR dept_id IN (").append(StringUtils.join(deptIds, ",")).append(")");
            }
        }
        
        // 将SQL条件注入到参数中
        Object params = point.getArgs()[0];
        if (params instanceof BaseEntity) {
            BaseEntity entity = (BaseEntity) params;
            entity.setDataScope("AND (" + sql.substring(4) + ")");
        }
    }
}
```

### 4.5 前端权限控制

#### 4.5.1 路由守卫

```typescript
// router/guards.ts
import { useAuthStore } from '@/stores/authStore';
import { useMenuStore } from '@/stores/menuStore';

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  const menuStore = useMenuStore();
  
  // 未登录，跳转登录页
  if (!authStore.isLoggedIn) {
    if (to.path === '/login') {
      next();
    } else {
      next('/login');
    }
    return;
  }
  
  // 已登录但未加载菜单
  if (menuStore.menuList.length === 0) {
    await menuStore.loadMenuList();
    next({ ...to, replace: true });
    return;
  }
  
  // 检查路由权限
  if (to.meta.permission) {
    const hasPermission = authStore.hasPermission(to.meta.permission);
    if (!hasPermission) {
      next('/403');
      return;
    }
  }
  
  next();
});
```

#### 4.5.2 权限指令

```typescript
// directives/permission.ts
import { useAuthStore } from '@/stores/authStore';

export const permission = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const authStore = useAuthStore();
    const permission = binding.value;
    
    if (!authStore.hasPermission(permission)) {
      el.parentNode?.removeChild(el);
    }
  }
};

// 注册全局指令
app.directive('permission', permission);
```

#### 4.5.3 使用示例

```vue
<template>
  <div>
    <!-- 按钮权限控制 -->
    <el-button v-permission="'doc:create'" type="primary">创建文档</el-button>
    <el-button v-permission="'doc:edit'" type="warning">编辑文档</el-button>
    <el-button v-permission="'doc:delete'" type="danger">删除文档</el-button>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();

// 代码中检查权限
if (authStore.hasPermission('doc:create')) {
  // 有权限，执行操作
}
</script>
```

---

## 5. API设计概要

### 5.1 API风格

- **RESTful API**: 遵循REST设计原则
- **统一响应格式**: 标准化响应结构
- **版本控制**: URL路径版本（/api/v1/）
- **认证方式**: Sa-Token（Token放在请求头Authorization）

### 5.2 统一响应格式

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
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": "2026-03-05T12:00:00Z"
}
```

### 5.3 核心API列表

#### 5.3.1 认证模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/v1/auth/login | 用户登录 | 公开 |
| POST | /api/v1/auth/logout | 用户登出 | 登录用户 |
| GET | /api/v1/auth/me | 获取当前用户信息 | 登录用户 |
| POST | /api/v1/auth/refresh | 刷新Token | 登录用户 |
| POST | /api/v1/auth/ldap-test | 测试LDAP连接 | sys:config:edit |

#### 5.3.2 用户管理模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/users | 获取用户列表（分页） | sys:user:list |
| GET | /api/v1/users/{id} | 获取用户详情 | sys:user:query |
| POST | /api/v1/users | 创建用户 | sys:user:create |
| PUT | /api/v1/users/{id} | 更新用户 | sys:user:edit |
| DELETE | /api/v1/users/{id} | 删除用户 | sys:user:delete |
| PUT | /api/v1/users/{id}/status | 更新用户状态 | sys:user:edit |
| PUT | /api/v1/users/{id}/reset-password | 重置密码 | sys:user:resetPwd |
| GET | /api/v1/users/{id}/roles | 获取用户角色 | sys:user:query |
| PUT | /api/v1/users/{id}/roles | 分配用户角色 | sys:user:edit |

#### 5.3.3 角色管理模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/roles | 获取角色列表 | sys:role:list |
| GET | /api/v1/roles/{id} | 获取角色详情 | sys:role:query |
| POST | /api/v1/roles | 创建角色 | sys:role:create |
| PUT | /api/v1/roles/{id} | 更新角色 | sys:role:edit |
| DELETE | /api/v1/roles/{id} | 删除角色 | sys:role:delete |
| PUT | /api/v1/roles/{id}/status | 更新角色状态 | sys:role:edit |
| GET | /api/v1/roles/{id}/menus | 获取角色菜单 | sys:role:query |
| PUT | /api/v1/roles/{id}/menus | 分配角色菜单 | sys:role:edit |
| GET | /api/v1/roles/{id}/permissions | 获取角色权限 | sys:role:query |
| PUT | /api/v1/roles/{id}/permissions | 分配角色权限 | sys:role:edit |
| GET | /api/v1/roles/{id}/data-scope | 获取角色数据权限 | sys:role:query |
| PUT | /api/v1/roles/{id}/data-scope | 设置角色数据权限 | sys:role:edit |

#### 5.3.4 菜单管理模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/menus | 获取菜单列表 | sys:menu:list |
| GET | /api/v1/menus/tree | 获取菜单树 | sys:menu:query |
| GET | /api/v1/menus/{id} | 获取菜单详情 | sys:menu:query |
| POST | /api/v1/menus | 创建菜单 | sys:menu:create |
| PUT | /api/v1/menus/{id} | 更新菜单 | sys:menu:edit |
| DELETE | /api/v1/menus/{id} | 删除菜单 | sys:menu:delete |
| GET | /api/v1/menus/user | 获取当前用户菜单 | 登录用户 |

#### 5.3.5 权限管理模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/permissions | 获取权限列表 | sys:permission:list |
| GET | /api/v1/permissions/{id} | 获取权限详情 | sys:permission:query |
| POST | /api/v1/permissions | 创建权限 | sys:permission:create |
| PUT | /api/v1/permissions/{id} | 更新权限 | sys:permission:edit |
| DELETE | /api/v1/permissions/{id} | 删除权限 | sys:permission:delete |

#### 5.3.6 部门管理模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/depts | 获取部门列表 | sys:dept:list |
| GET | /api/v1/depts/tree | 获取部门树 | sys:dept:query |
| GET | /api/v1/depts/{id} | 获取部门详情 | sys:dept:query |
| POST | /api/v1/depts | 创建部门 | sys:dept:create |
| PUT | /api/v1/depts/{id} | 更新部门 | sys:dept:edit |
| DELETE | /api/v1/depts/{id} | 删除部门 | sys:dept:delete |

#### 5.3.7 文档模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/v1/documents | 上传文档 | doc:create |
| GET | /api/v1/documents | 获取文档列表（分页） | doc:list |
| GET | /api/v1/documents/{id} | 获取文档详情 | doc:query |
| PUT | /api/v1/documents/{id} | 更新文档信息 | doc:edit |
| DELETE | /api/v1/documents/{id} | 删除文档 | doc:delete |
| GET | /api/v1/documents/{id}/file | 下载文档文件 | doc:download |
| POST | /api/v1/documents/{id}/submit | 提交评审 | doc:submit |
| POST | /api/v1/documents/{id}/withdraw | 撤回评审 | doc:withdraw |

#### 5.3.8 评审模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/reviews/pending | 获取待评审列表 | review:list |
| GET | /api/v1/reviews/{id} | 获取评审详情 | review:query |
| POST | /api/v1/reviews/{id}/submit | 提交评审意见 | review:submit |
| GET | /api/v1/documents/{docId}/reviews | 获取文档评审记录 | review:query |
| GET | /api/v1/documents/{docId}/progress | 获取评审进度 | review:query |

#### 5.3.9 评论模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/documents/{docId}/comments | 获取文档评论列表 | comment:list |
| POST | /api/v1/documents/{docId}/comments | 添加评论 | comment:create |
| PUT | /api/v1/comments/{id} | 更新评论 | comment:edit |
| DELETE | /api/v1/comments/{id} | 删除评论 | comment:delete |
| POST | /api/v1/comments/{id}/reply | 回复评论 | comment:create |

#### 5.3.10 归档模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/archives | 搜索归档文档 | archive:list |
| POST | /api/v1/documents/{id}/archive | 归档文档 | archive:create |
| DELETE | /api/v1/archives/{id} | 取消归档 | archive:delete |
| GET | /api/v1/archives/{id}/export | 导出评审报告 | archive:export |

#### 5.3.11 系统配置模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/v1/system/config | 获取系统配置 | sys:config:query |
| PUT | /api/v1/system/config | 更新系统配置 | sys:config:edit |
| GET | /api/v1/system/ldap-config | 获取LDAP配置 | sys:config:query |
| PUT | /api/v1/system/ldap-config | 更新LDAP配置 | sys:config:edit |
| POST | /api/v1/system/ldap-test | 测试LDAP连接 | sys:config:edit |

---

## 6. 数据库设计概要

### 6.1 ER图（包含RBAC）

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              数据库ER图（RBAC部分）                              │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐              │
│  │  sys_user   │         │ sys_user_role│        │  sys_role   │              │
│  ├─────────────┤         ├─────────────┤         ├─────────────┤              │
│  │ id (PK)     │←──────→│ user_id (FK)│←──────→│ id (PK)     │              │
│  │ username    │         │ role_id (FK)│         │ role_name   │              │
│  │ password    │         └─────────────┘         │ role_code   │              │
│  │ real_name   │                                   │ data_scope  │              │
│  │ dept_id(FK)│←──┐                               └─────────────┘              │
│  └─────────────┘   │                                      │                    │
│        │           │                                      │                    │
│        │           │              ┌───────────────────────┴─────────────┐      │
│        │           │              │                                       │      │
│        │           │       ┌─────────────┐                     ┌─────────────┐ │
│        │           │       │sys_role_menu│                     │sys_role_perm│ │
│        │           │       ├─────────────┤                     ├─────────────┤ │
│        │           │       │ role_id (FK)│                     │ role_id (FK)│ │
│        │           │       │ menu_id (FK)│                     │ perm_id(FK) │ │
│        │           │       └─────────────┘                     └─────────────┘ │
│        │           │              │                                    │       │
│        │           │              ▼                                    ▼       │
│        │           │       ┌─────────────┐                     ┌─────────────┐ │
│        │           │       │  sys_menu   │                     │sys_permission│
│        │           │       ├─────────────┤                     ├─────────────┤ │
│        │           │       │ id (PK)     │                     │ id (PK)     │ │
│        │           │       │ parent_id   │                     │ perm_name   │ │
│        │           │       │ menu_name   │                     │ perm_code   │ │
│        │           │       │ menu_type   │                     │ resource_url│ │
│        │           │       │ path        │                     │ method      │ │
│        │           │       │ perms       │                     └─────────────┘ │
│        │           │       └─────────────┘                                     │
│        │           │                                                           │
│        └──────────→│ ┌─────────────┐         ┌─────────────┐                  │
│                    │ │  sys_dept   │←──────→│sys_role_dept│                  │
│                    │ ├─────────────┤         ├─────────────┤                  │
│                    │ │ id (PK)     │         │ role_id (FK)│                  │
│                    │ │ parent_id   │         │ dept_id (FK)│                  │
│                    │ │ dept_name   │         └─────────────┘                  │
│                    │ │ dept_code   │                                          │
│                    │ └─────────────┘                                          │
│                    │                                                           │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 6.2 核心表结构（业务表）

#### 6.2.1 文档表 (doc_document)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| title | VARCHAR(255) | 文档标题 |
| description | TEXT | 文档描述 |
| file_path | VARCHAR(500) | MinIO文件路径 |
| file_name | VARCHAR(255) | 原始文件名 |
| file_size | BIGINT | 文件大小（字节） |
| file_type | VARCHAR(50) | 文件类型 |
| review_type | VARCHAR(20) | 评审类型 |
| status | VARCHAR(20) | 状态 |
| submitter_id | BIGINT | 提交者ID |
| dept_id | BIGINT | 部门ID（数据权限） |
| deadline | TIMESTAMP | 评审截止时间 |
| version | VARCHAR(20) | 版本号 |
| archived | BOOLEAN | 是否已归档 |
| archived_at | TIMESTAMP | 归档时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

#### 6.2.2 评审表 (doc_review)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| document_id | BIGINT | 文档ID |
| reviewer_id | BIGINT | 评审者ID |
| status | VARCHAR(20) | 状态 |
| decision | VARCHAR(20) | 决定 |
| overall_comment | TEXT | 总体评价 |
| pros | TEXT | 优点 |
| cons | TEXT | 问题 |
| suggestions | TEXT | 建议 |
| submitted_at | TIMESTAMP | 提交时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

#### 6.2.3 评审者分配表 (doc_reviewer_assignment)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| document_id | BIGINT | 文档ID |
| reviewer_id | BIGINT | 评审者ID |
| status | VARCHAR(20) | 状态 |
| created_at | TIMESTAMP | 创建时间 |

#### 6.2.4 评论表 (doc_comment)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| document_id | BIGINT | 文档ID |
| review_id | BIGINT | 评审ID |
| author_id | BIGINT | 作者ID |
| content | TEXT | 评论内容 |
| type | VARCHAR(20) | 类型 |
| priority | VARCHAR(10) | 优先级 |
| position | VARCHAR(100) | 行内批注位置 |
| parent_id | BIGINT | 父评论ID |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

---

## 7. AD域集成方案

### 7.1 整体设计

AD域集成采用**可配置、可开关**的设计，支持两种认证模式：

1. **AD域认证模式**：用户通过企业AD账号登录
2. **本地认证模式**：用户通过本地账号登录

### 7.2 认证流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        认证流程                                          │
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
│                           │ Sa-Token登录     │                         │
│                           │ 生成Token        │                         │
│                           └──────────────────┘                         │
│                                   │                                    │
│                                   ▼                                    │
│     ┌──────────┐     Token              ┌──────────┐                  │
│     │  前端    │ ←───────────────────── │  后端    │                  │
│     └──────────┘                        └──────────┘                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 7.3 Sa-Token + LDAP配置

```java
@Service
public class AuthService {
    
    @Autowired
    private LdapService ldapService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SystemConfigService configService;
    
    /**
     * 用户登录
     */
    public String login(String username, String password) {
        User user = null;
        
        // 检查是否启用LDAP
        if (configService.isLdapEnabled()) {
            // LDAP认证
            if (ldapService.authenticate(username, password)) {
                // 同步用户信息
                user = ldapService.syncUserFromLdap(username);
            } else {
                throw new BusinessException("用户名或密码错误");
            }
        } else {
            // 本地认证
            user = userService.getByUsername(username);
            if (user == null || !userService.checkPassword(user, password)) {
                throw new BusinessException("用户名或密码错误");
            }
        }
        
        // Sa-Token登录
        StpUtil.login(user.getId());
        
        // 返回Token
        return StpUtil.getTokenValue();
    }
    
    /**
     * 用户登出
     */
    public void logout() {
        StpUtil.logout();
    }
}
```

---

## 8. 关键技术实现

### 8.1 MyBatis Plus配置

```java
@Configuration
public class MybatisPlusConfig {
    
    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }
    
    /**
     * 逻辑删除
     */
    @Bean
    public ISqlInjector sqlInjector() {
        return new DefaultSqlInjector();
    }
}
```

### 8.2 数据权限实现

```java
/**
 * 数据权限注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    String deptAlias() default "d";
    String userAlias() default "u";
}

/**
 * 数据权限Mapper
 */
public interface BaseMapper<T> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T> {
    
    /**
     * 根据数据权限查询
     */
    List<T> selectListByDataScope(@Param("ew") Wrapper<T> queryWrapper, @Param("dataScope") String dataScope);
}
```

### 8.3 并行评审实现

```java
@Service
public class ReviewService {
    
    @Autowired
    private ReviewMapper reviewMapper;
    
    @Autowired
    private ReviewerAssignmentMapper assignmentMapper;
    
    @Autowired
    private DocumentMapper documentMapper;
    
    /**
     * 提交评审意见（支持并行评审）
     */
    @Transactional
    public Review submitReview(Long documentId, Long reviewerId, ReviewRequest request) {
        // 1. 获取评审者分配记录
        ReviewerAssignment assignment = assignmentMapper.selectOne(
            new LambdaQueryWrapper<ReviewerAssignment>()
                .eq(ReviewerAssignment::getDocumentId, documentId)
                .eq(ReviewerAssignment::getReviewerId, reviewerId)
        );
        
        if (assignment == null) {
            throw new BusinessException("未找到评审分配记录");
        }
        
        // 2. 创建评审记录
        Review review = new Review();
        review.setDocumentId(documentId);
        review.setReviewerId(reviewerId);
        review.setDecision(request.getDecision());
        review.setOverallComment(request.getOverallComment());
        review.setSubmittedAt(LocalDateTime.now());
        reviewMapper.insert(review);
        
        // 3. 更新分配状态
        assignment.setStatus(ReviewerStatus.COMPLETED);
        assignmentMapper.updateById(assignment);
        
        // 4. 更新文档状态（使用乐观锁保证一致性）
        updateDocumentStatus(documentId);
        
        return review;
    }
    
    /**
     * 更新文档状态（基于评审结果）
     */
    @Transactional
    public void updateDocumentStatus(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        
        // 获取所有评审分配
        List<ReviewerAssignment> assignments = assignmentMapper.selectList(
            new LambdaQueryWrapper<ReviewerAssignment>()
                .eq(ReviewerAssignment::getDocumentId, documentId)
        );
        
        long totalCount = assignments.size();
        long completedCount = assignments.stream()
            .filter(a -> a.getStatus() == ReviewerStatus.COMPLETED)
            .count();
        long approvedCount = 0;
        
        for (ReviewerAssignment a : assignments) {
            if (a.getStatus() == ReviewerStatus.COMPLETED) {
                Review r = reviewMapper.selectOne(
                    new LambdaQueryWrapper<Review>()
                        .eq(Review::getDocumentId, documentId)
                        .eq(Review::getReviewerId, a.getReviewerId())
                );
                if (r != null && r.getDecision() == Decision.APPROVED) {
                    approvedCount++;
                }
            }
        }
        
        // 更新状态
        if (completedCount == 0) {
            document.setStatus(DocumentStatus.REVIEWING);
        } else if (completedCount == totalCount) {
            String passCondition = configService.getConfig("review.pass-condition");
            boolean passed = evaluatePassCondition(passCondition, approvedCount, totalCount);
            document.setStatus(passed ? DocumentStatus.APPROVED : DocumentStatus.REJECTED);
        }
        
        documentMapper.updateById(document);
    }
}
```

---

## 9. 部署方案

### 9.1 Docker Compose配置

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
      - SA_TOKEN_TOKEN_NAME=Authorization
      - SA_TOKEN_TIMEOUT=86400
      - SA_TOKEN_ACTIVE_TIMEOUT=-1
      - SA_TOKEN_IS_CONCURRENT=true
      - SA_TOKEN_IS_SHARE=true
      - SA_TOKEN_TOKEN_STYLE=uuid
      - SA_TOKEN_IS_LOG=false
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

### 9.2 环境配置

```yaml
# application-prod.yml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver
  
mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.docreview.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}

minio:
  endpoint: ${MINIO_ENDPOINT}
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
  bucket-name: doc-review

sa-token:
  token-name: ${SA_TOKEN_TOKEN_NAME}
  timeout: ${SA_TOKEN_TIMEOUT}
  active-timeout: ${SA_TOKEN_ACTIVE_TIMEOUT}
  is-concurrent: ${SA_TOKEN_IS_CONCURRENT}
  is-share: ${SA_TOKEN_IS_SHARE}
  token-style: ${SA_TOKEN_TOKEN_STYLE}
  is-log: ${SA_TOKEN_IS_LOG}

ldap:
  enabled: false
  url: ${LDAP_URL:ldap://localhost:389}
  base-dn: ${LDAP_BASE_DN:dc=company,dc=com}
  bind-dn: ${LDAP_BIND_DN:cn=admin,dc=company,dc=com}
  bind-password: ${LDAP_BIND_PASSWORD:}
  user-attribute: ${LDAP_USER_ATTRIBUTE:sAMAccountName}
```

---

## 10. 安全设计

### 10.1 认证授权

- **Sa-Token**: 轻量级权限认证框架，支持多账号登录
- **RBAC**: 基于角色的访问控制（用户-角色-权限-菜单-数据权限）
- **权限注解**: 使用@SaCheckPermission注解控制接口权限

### 10.2 数据安全

- **传输加密**: HTTPS/TLS
- **密码加密**: BCrypt
- **敏感配置**: 环境变量注入，不存储在代码中
- **LDAP密码**: AES加密存储

### 10.3 审计日志

```java
@Entity
@TableName("sys_audit_log")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String username;
    private String operation;
    private String method;
    private String params;
    private String ip;
    private Long duration;
    private Boolean success;
    private String errorMsg;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

---

## 11. 性能优化

### 11.1 缓存策略

| 数据类型 | 缓存方式 | 过期时间 |
|----------|----------|----------|
| 用户权限 | Redis | 30分钟 |
| 角色菜单 | Redis | 30分钟 |
| 系统配置 | Redis | 10分钟 |
| 文档列表 | Redis | 5分钟 |
| 评审进度 | Redis | 1分钟 |

### 11.2 数据库优化

- 索引设计：用户名、角色编码、菜单编码、文档状态
- 分页查询：使用MyBatis Plus分页插件
- 逻辑删除：避免物理删除，提高查询性能
- 连接池：HikariCP，最大连接数20

### 11.3 前端优化

- 路由懒加载
- 组件按需加载
- 图片懒加载
- 打包优化（Vite）

---

## 12. 开发计划

### 12.1 开发阶段划分

| 阶段 | 内容 | 工期 |
|------|------|------|
| **阶段1** | 项目骨架搭建、RBAC框架搭建 | 1天 |
| **阶段2** | 用户管理、角色管理、菜单管理、权限管理 | 1天 |
| **阶段3** | 用户认证、AD域集成 | 1天 |
| **阶段4** | 文档上传、文件存储 | 1天 |
| **阶段5** | 评审流程、并行评审 | 1.5天 |
| **阶段6** | 评论、通知功能 | 1天 |
| **阶段7** | 归档检索、系统配置 | 1天 |
| **阶段8** | 测试、文档、优化 | 1天 |

### 12.2 里程碑

| 里程碑 | 内容 | 预计完成 |
|--------|------|----------|
| M1 | 项目骨架、RBAC框架 | Day 1 |
| M2 | 权限管理模块完成 | Day 2 |
| M3 | 认证模块完成 | Day 3 |
| M4 | 文档模块完成 | Day 4 |
| M5 | 评审模块完成 | Day 6 |
| M6 | 全部功能完成 | Day 7 |
| M7 | 测试通过、文档完成 | Day 8 |

---

## 13. 附录

### 13.1 技术栈版本详情

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 17 LTS | 长期支持版本 |
| Spring Boot | 3.2.x | 最新稳定版 |
| MyBatis Plus | 3.5.x | 最新稳定版 |
| Sa-Token | 1.37.x | 最新稳定版 |
| PostgreSQL | 15.x | LTS版本 |
| MinIO | RELEASE.2024-x | 最新稳定版 |
| Redis | 7.x | 最新稳定版 |
| Vue | 3.4.x | 最新稳定版 |
| TypeScript | 5.x | 最新稳定版 |
| Element Plus | 2.5.x | 最新稳定版 |
| Pinia | 2.x | 最新稳定版 |
| Vite | 5.x | 最新稳定版 |

### 13.2 参考文档

- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [MyBatis Plus官方文档](https://baomidou.com/)
- [Sa-Token官方文档](https://sa-token.cc/)
- [Vue 3官方文档](https://cn.vuejs.org/)
- [Element Plus官方文档](https://element-plus.org/zh-CN/)
- [MinIO Java SDK](https://min.io/docs/minio/linux/developers/java/minio-java.html)

---

**文档变更记录**

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|----------|
| 1.0 | 2026-03-05 | fullstack-dev | 初稿 |
| 2.0 | 2026-03-05 | fullstack-dev | 技术栈调整（Vue+MyBatis Plus+Sa-Token）、新增完整RBAC权限管理模块 |

---

*本文档由 fullstack-dev 编写，供团队评审*
