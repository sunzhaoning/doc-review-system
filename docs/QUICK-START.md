# 🚀 快速部署指南

## 一键部署（推荐）

### Mac / Linux

```bash
curl -fsSL https://raw.githubusercontent.com/sunzhaoning/doc-review-system/main/deploy-mac.sh | bash
```

### Windows (PowerShell)

```powershell
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/sunzhaoning/doc-review-system/main/deploy-mac.sh" -OutFile "deploy.sh"
bash deploy.sh
```

---

## 手动部署

### 1. 前置条件

- Docker Desktop 已安装并运行
  - 下载: https://www.docker.com/products/docker-desktop
- Git 已安装
- 8GB+ 可用内存
- 10GB+ 可用磁盘空间

### 2. 克隆项目

```bash
git clone https://github.com/sunzhaoning/doc-review-system.git
cd doc-review-system
```

### 3. 配置环境（可选）

```bash
cp .env.example .env
# 使用默认配置即可启动，如需自定义可编辑 .env
```

### 4. 启动服务

```bash
# 添加执行权限
chmod +x scripts/*.sh

# 启动所有服务
./scripts/start.sh
```

### 5. 访问系统

- **前端**: http://localhost:3000
- **后端API**: http://localhost:8080
- **API文档**: http://localhost:8080/doc.html
- **MinIO控制台**: http://localhost:9001 (minioadmin/minioadmin)

### 6. 默认账号

- **用户名**: admin
- **密码**: admin123

---

## 常用命令

```bash
# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 停止服务
./scripts/stop.sh

# 重启服务
docker-compose restart

# 健康检查
./scripts/health-check.sh

# 数据备份
./scripts/backup.sh

# 数据恢复
./scripts/restore.sh backup_20260306.tar.gz
```

---

## 端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 3000 | Web界面 |
| 后端 | 8080 | API服务 |
| PostgreSQL | 5432 | 数据库 |
| Redis | 6379 | 缓存 |
| MinIO API | 9000 | 文件存储API |
| MinIO Console | 9001 | 文件存储管理界面 |

---

## 故障排查

### 端口被占用

```bash
# 查看端口占用
lsof -i :3000
lsof -i :8080

# 修改端口（编辑 docker-compose.yml）
# 将 "3000:80" 改为 "3001:80"
```

### Docker未启动

```bash
# 检查Docker状态
docker info

# Mac: 启动Docker Desktop应用
# Linux: sudo systemctl start docker
```

### 权限问题

```bash
# 添加执行权限
chmod +x scripts/*.sh

# Linux可能需要sudo
sudo ./scripts/start.sh
```

### 查看详细日志

```bash
# 所有服务日志
docker-compose logs

# 实时跟踪
docker-compose logs -f --tail=100

# 特定服务
docker-compose logs backend
docker-compose logs frontend
docker-compose logs postgres
```

---

## 数据持久化

数据存储在Docker volumes中：

```bash
# 查看volumes
docker volume ls

# 备份数据
./scripts/backup.sh

# 恢复数据
./scripts/restore.sh backup_file.tar.gz
```

---

## 生产环境建议

1. **修改默认密码**
   ```bash
   # 编辑 .env 文件
   POSTGRES_PASSWORD=your_secure_password
   MINIO_ROOT_PASSWORD=your_secure_password
   ```

2. **配置HTTPS**
   - 使用Nginx反向代理
   - 配置SSL证书

3. **性能优化**
   - 调整JVM内存: `-Xms512m -Xmx1024m`
   - 数据库连接池配置
   - Redis缓存策略

4. **监控告警**
   - 配置日志收集
   - 设置性能监控
   - 配置告警通知

---

## 获取帮助

- **项目文档**: [docs/](docs/)
- **部署文档**: [docs/deployment.md](docs/deployment.md)
- **用户手册**: [docs/user-manual.md](docs/user-manual.md)
- **API文档**: [docs/api-documentation.md](docs/api-documentation.md)
- **GitHub Issues**: https://github.com/sunzhaoning/doc-review-system/issues

---

**祝您部署顺利！** 🎉
