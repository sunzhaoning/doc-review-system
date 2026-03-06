#!/bin/bash
# 文档评审系统 - Mac一键部署脚本
# 使用方法: curl -fsSL https://raw.githubusercontent.com/sunzhaoning/doc-review-system/main/deploy-mac.sh | bash

set -e

echo "================================"
echo "文档评审系统 - Mac一键部署"
echo "================================"
echo ""

# 检查Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装"
    echo ""
    echo "请先安装Docker Desktop:"
    echo "1. 访问 https://www.docker.com/products/docker-desktop"
    echo "2. 下载Mac版本（Intel或Apple Silicon）"
    echo "3. 安装并启动Docker Desktop"
    echo "4. 重新运行此脚本"
    exit 1
fi

echo "✅ Docker已安装: $(docker --version)"
echo ""

# 检查Docker是否运行
if ! docker info &> /dev/null; then
    echo "❌ Docker未运行，正在启动..."
    open -a Docker
    echo "等待Docker启动（30秒）..."
    sleep 30
fi

echo "✅ Docker运行正常"
echo ""

# 克隆项目
DEPLOY_DIR="$HOME/Documents/doc-review-system"

if [ -d "$DEPLOY_DIR" ]; then
    echo "📁 项目目录已存在，更新代码..."
    cd "$DEPLOY_DIR"
    git pull
else
    echo "📥 克隆项目仓库..."
    cd "$HOME/Documents"
    git clone https://github.com/sunzhaoning/doc-review-system.git
    cd "$DEPLOY_DIR"
fi

echo ""

# 配置环境
if [ ! -f ".env" ]; then
    echo "⚙️  配置环境变量..."
    cp .env.example .env

    # 生成随机密码
    DB_PASSWORD=$(openssl rand -base64 12)
    JWT_SECRET=$(openssl rand -base64 32)

    # macOS兼容的sed命令
    sed -i '' "s/POSTGRES_PASSWORD=.*/POSTGRES_PASSWORD=$DB_PASSWORD/" .env
    sed -i '' "s/JWT_SECRET=.*/JWT_SECRET=$JWT_SECRET/" .env

    echo "✅ 环境配置完成"
    echo "   数据库密码: $DB_PASSWORD"
    echo "   JWT密钥: 已生成"
    echo ""
else
    echo "✅ 环境配置已存在"
    echo ""
fi

# 设置脚本权限
chmod +x scripts/*.sh

# 停止旧容器（如果存在）
echo "🔄 清理旧容器..."
docker-compose down 2>/dev/null || true
echo ""

# 启动服务
echo "🚀 启动服务..."
echo "   这可能需要几分钟时间..."
echo ""

docker-compose up -d

# 等待服务启动
echo ""
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo ""
echo "📊 服务状态:"
docker-compose ps

echo ""
echo "================================"
echo "✅ 部署完成！"
echo "================================"
echo ""
echo "访问地址: http://localhost:3000"
echo ""
echo "默认账号:"
echo "  用户名: admin"
echo "  密码: admin123"
echo ""
echo "常用命令:"
echo "  查看日志: docker-compose logs -f"
echo "  停止服务: ./scripts/stop.sh"
echo "  健康检查: ./scripts/health-check.sh"
echo ""
echo "项目目录: $DEPLOY_DIR"
echo ""
