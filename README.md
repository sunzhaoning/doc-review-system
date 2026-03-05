# 文档评审系统

一个轻量级、高效的文档评审系统，支持文档上传、评审流程、意见收集和归档管理。

## 技术栈

### 后端
- Spring Boot 3.2.x
- MyBatis Plus 3.5.x
- Sa-Token 1.37.x（认证授权）
- Knife4j 4.3.x（API文档）
- PostgreSQL 15.x
- Redis 7.x
- MinIO（对象存储）

### 前端
- Vue 3.4.x
- TypeScript 5.x
- Element Plus 2.5.x
- Vue Router 4.x
- Pinia 2.x
- Vite 5.x

## 功能特性

### 核心功能
- ✅ 文档上传与管理
- ✅ 结构化评审流程
- ✅ 并行评审支持
- ✅ 意见收集与跟踪
- ✅ 归档与检索

### 权限管理
- ✅ 完整的RBAC权限模型
- ✅ 菜单权限控制
- ✅ 按钮权限控制
- ✅ 数据权限控制

### 集成
- ✅ AD域认证（可配置、可开关）
- ✅ LDAP用户同步

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+

### 开发环境启动

#### 1. 启动数据库和中间件

```bash
# 启动 PostgreSQL、Redis、MinIO
docker-compose up -d postgres redis minio

# 等待服务启动完成
docker-compose ps
```

#### 2. 初始化数据库

```bash
# 执行初始化脚本
psql -h localhost -U docreview -d docreview -f database/init.sql
psql -h localhost -U docreview -d docreview -f database/data.sql
```

#### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

访问 API 文档：http://localhost:8080/doc.html

#### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问系统：http://localhost:3000

### Docker 部署

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
```

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |

## 项目结构

```
doc-review-system/
├── backend/                    # 后端项目
│   ├── src/main/java/
│   │   └── com/docreview/
│   │       ├── config/         # 配置类
│   │       ├── controller/     # 控制器
│   │       ├── service/        # 服务层
│   │       ├── mapper/         # MyBatis Mapper
│   │       ├── entity/         # 实体类
│   │       ├── dto/            # 数据传输对象
│   │       ├── enums/          # 枚举类
│   │       ├── exception/      # 异常处理
│   │       ├── security/       # 安全模块
│   │       └── util/           # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml     # 配置文件
│   │   └── mapper/             # MyBatis XML
│   └── pom.xml
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/                # API 接口
│   │   ├── components/         # 组件
│   │   ├── views/              # 页面
│   │   ├── stores/             # 状态管理
│   │   ├── router/             # 路由配置
│   │   ├── types/              # TypeScript 类型
│   │   └── assets/             # 静态资源
│   ├── package.json
│   └── vite.config.ts
├── database/                   # 数据库脚本
│   ├── init.sql                # 表结构
│   └── data.sql                # 初始数据
├── docs/                       # 文档
│   ├── requirements.md         # 需求文档
│   ├── design.md               # 交互设计
│   ├── architecture.md         # 技术架构
│   └── development-plan.md     # 开发计划
├── docker-compose.yml          # Docker Compose 配置
└── README.md                   # 项目说明
```

## API 文档

启动后端服务后，访问：http://localhost:8080/doc.html

## 许可证

MIT License

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'feat: add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request
