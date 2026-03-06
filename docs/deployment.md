# 部署文档

## 概述

本文档介绍文档评审系统的部署方式，包括开发环境、测试环境和生产环境的部署配置。

---

## 环境要求

### 硬件要求

| 环境 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| 开发环境 | 2核 | 4GB | 20GB |
| 测试环境 | 4核 | 8GB | 50GB |
| 生产环境 | 8核+ | 16GB+ | 200GB+ |

### 软件要求

| 组件 | 版本要求 |
|------|----------|
| Docker | 20.10+ |
| Docker Compose | 2.0+ |
| JDK | 17+ |
| Node.js | 18+ |
| PostgreSQL | 15+ |
| Redis | 7+ |

---

## 部署方式

### 方式一：Docker Compose 部署（推荐）

Docker Compose 是推荐的部署方式，适合大多数场景。

#### 1. 准备环境

```bash
# 安装 Docker（以 Ubuntu 为例）
curl -fsSL https://get.docker.com | sh

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.23.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

#### 2. 获取代码

```bash
git clone https://github.com/sunzhaoning/doc-review-system.git
cd doc-review-system
```

#### 3. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑配置文件
vim .env
```

**关键配置项说明**：

```bash
# ==================== 通用配置 ====================
COMPOSE_PROJECT_NAME=doc-review-system
TZ=Asia/Shanghai

# ==================== PostgreSQL 配置 ====================
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
POSTGRES_DB=docreview
POSTGRES_USER=docreview
POSTGRES_PASSWORD=your_secure_password_here  # 请设置强密码

# ==================== Redis 配置 ====================
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password_here  # 请设置强密码

# ==================== MinIO 配置 ====================
MINIO_HOST=minio
MINIO_PORT=9000
MINIO_CONSOLE_PORT=9001
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=your_minio_password_here  # 请设置强密码
MINIO_BUCKET=doc-review

# ==================== 后端服务配置 ====================
BACKEND_PORT=8080
BACKEND_PROFILE=prod

# ==================== 前端服务配置 ====================
FRONTEND_PORT=3000
VITE_API_BASE_URL=http://your-domain.com:8080  # 替换为实际域名

# ==================== AD 域配置（可选） ====================
AD_ENABLED=false
AD_URL=ldap://ad.example.com:389
AD_BASE_DN=dc=example,dc=com
AD_USERNAME=cn=admin,dc=example,dc=com
AD_PASSWORD=your_ad_password
```

#### 4. 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

#### 5. 验证部署

```bash
# 检查后端健康状态
curl http://localhost:8080/actuator/health

# 检查前端
curl http://localhost:3000

# 访问系统
# 前端：http://localhost:3000
# API文档：http://localhost:8080/doc.html
# MinIO控制台：http://localhost:9001
```

---

### 方式二：手动部署

适合需要更多自定义控制的场景。

#### 1. 安装依赖服务

**PostgreSQL**

```bash
# Ubuntu
sudo apt install postgresql-15

# 创建数据库和用户
sudo -u postgres psql
CREATE DATABASE docreview;
CREATE USER docreview WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE docreview TO docreview;
```

**Redis**

```bash
# Ubuntu
sudo apt install redis-server

# 配置密码
sudo vim /etc/redis/redis.conf
# 添加：requirepass your_redis_password

sudo systemctl restart redis
```

**MinIO**

```bash
# 下载 MinIO
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio
sudo mv minio /usr/local/bin/

# 创建数据目录
sudo mkdir -p /data/minio

# 启动服务
minio server /data/minio --console-address ":9001"
```

#### 2. 初始化数据库

```bash
# 执行初始化脚本
psql -h localhost -U docreview -d docreview -f database/init.sql
psql -h localhost -U docreview -d docreview -f database/data.sql
```

#### 3. 部署后端

```bash
cd backend

# 构建项目
mvn clean package -DskipTests

# 创建配置文件
cp src/main/resources/application.yml application-prod.yml

# 编辑生产配置
vim application-prod.yml
```

**application-prod.yml 配置示例**：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/docreview
    username: docreview
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: your_minio_password
  bucket-name: doc-review

sa-token:
  token-name: Authorization
  timeout: 86400

logging:
  level:
    com.docreview: info
```

```bash
# 启动服务
java -jar -Dspring.profiles.active=prod target/doc-review-system-1.0.0.jar
```

#### 4. 部署前端

```bash
cd frontend

# 安装依赖
npm install

# 配置 API 地址
echo "VITE_API_BASE_URL=http://your-domain.com:8080" > .env.production

# 构建
npm run build

# 使用 Nginx 托管
sudo apt install nginx
sudo cp -r dist/* /var/www/html/
```

**Nginx 配置示例**：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /var/www/html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 生产环境配置

### SSL/HTTPS 配置

#### 使用 Nginx 反向代理

```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;
    
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    location / {
        root /var/www/html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto https;
    }
}

server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

#### 使用 Let's Encrypt 获取免费证书

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

### 性能优化配置

#### JVM 调优

```bash
# 推荐生产环境 JVM 参数
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError"
```

#### PostgreSQL 调优

```sql
-- postgresql.conf 关键配置
shared_buffers = 256MB
effective_cache_size = 768MB
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
random_page_cost = 1.1
effective_io_concurrency = 200
work_mem = 4MB
min_wal_size = 1GB
max_wal_size = 4GB
```

#### Redis 调优

```bash
# redis.conf 关键配置
maxmemory 2gb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
```

---

## AD 域集成配置

### 配置步骤

1. **修改 .env 文件**

```bash
AD_ENABLED=true
AD_URL=ldap://ad.company.com:389
AD_BASE_DN=dc=company,dc=com
AD_USERNAME=cn=service_account,ou=ServiceAccounts,dc=company,dc=com
AD_PASSWORD=your_service_account_password
```

2. **重启后端服务**

```bash
docker-compose restart backend
```

3. **测试 AD 连接**

登录系统后，访问 **系统管理 > 系统配置 > LDAP配置**，点击"测试连接"按钮。

### AD 域用户登录流程

1. 用户使用 AD 域账号登录
2. 系统验证 AD 域凭据
3. 首次登录自动创建本地用户记录
4. 后续登录使用 AD 域认证

---

## 备份与恢复

### 数据库备份

```bash
# 手动备份
docker exec doc-review-postgres pg_dump -U docreview docreview > backup_$(date +%Y%m%d).sql

# 定时备份（crontab）
0 2 * * * docker exec doc-review-postgres pg_dump -U docreview docreview > /backup/db_$(date +\%Y\%m\%d).sql
```

### 数据库恢复

```bash
# 恢复数据库
cat backup_20260306.sql | docker exec -i doc-review-postgres psql -U docreview docreview
```

### 文件存储备份

```bash
# MinIO 数据备份
docker exec doc-review-minio mc mirror local/doc-review /backup/minio/

# 或直接备份数据目录
tar -czvf minio_backup_$(date +%Y%m%d).tar.gz /var/lib/docker/volumes/doc-review-minio-data
```

---

## 监控与日志

### 日志查看

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend

# 查看最近 100 行日志
docker-compose logs --tail=100 backend
```

### 日志轮转

系统已配置 logrotate，日志文件自动轮转：

```
# logrotate.conf
/var/log/doc-review/*.log {
    daily
    rotate 7
    compress
    missingok
    notifempty
    create 0644 root root
}
```

### 健康检查

```bash
# 检查所有服务健康状态
docker-compose ps

# 后端健康检查端点
curl http://localhost:8080/actuator/health

# 数据库健康检查
docker exec doc-review-postgres pg_isready -U docreview

# Redis 健康检查
docker exec doc-review-redis redis-cli -a your_password ping
```

---

## 故障排查

### 常见问题

#### 1. 后端无法连接数据库

```bash
# 检查数据库是否运行
docker-compose ps postgres

# 检查网络连通性
docker exec doc-review-backend ping postgres

# 查看后端日志
docker-compose logs backend | grep -i database
```

#### 2. 前端无法访问后端 API

```bash
# 检查后端是否运行
curl http://localhost:8080/actuator/health

# 检查跨域配置
# 确保 application.yml 中配置了正确的 CORS
```

#### 3. 文件上传失败

```bash
# 检查 MinIO 是否运行
docker-compose ps minio

# 检查 MinIO 控制台
# 访问 http://localhost:9001

# 检查 bucket 是否创建
docker exec doc-review-minio mc ls local/
```

#### 4. 内存不足

```bash
# 检查容器资源使用
docker stats

# 调整 JVM 内存
# 修改 .env 中的 JAVA_OPTS
JAVA_OPTS=-Xms512m -Xmx1024m

# 重启服务
docker-compose restart backend
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启单个服务
docker-compose restart backend

# 完全重建
docker-compose down
docker-compose up -d
```

---

## 升级指南

### 升级步骤

1. **备份数据**

```bash
# 备份数据库
docker exec doc-review-postgres pg_dump -U docreview docreview > backup_before_upgrade.sql

# 备份文件存储
cp -r /var/lib/docker/volumes/doc-review-minio-data /backup/
```

2. **拉取最新代码**

```bash
git pull origin main
```

3. **重新构建并启动**

```bash
docker-compose down
docker-compose build
docker-compose up -d
```

4. **验证升级**

```bash
# 检查服务状态
docker-compose ps

# 检查日志
docker-compose logs -f
```

---

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |

**⚠️ 生产环境请务必修改默认密码！**

---

## 联系支持

如有问题，请联系技术支持或提交 Issue：

- GitHub Issues: https://github.com/sunzhaoning/doc-review-system/issues
