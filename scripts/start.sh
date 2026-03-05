#!/bin/bash
# ====================================
# 文档评审系统 - 启动脚本
# ====================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

# 打印带颜色的消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 .env 文件
check_env() {
    if [ ! -f ".env" ]; then
        print_warn ".env 文件不存在，从模板创建..."
        if [ -f ".env.example" ]; then
            cp .env.example .env
            print_info "已创建 .env 文件，请编辑并填写实际配置值"
            print_info "编辑命令: nano .env 或 vim .env"
            exit 1
        else
            print_error ".env.example 模板文件不存在"
            exit 1
        fi
    fi
}

# 检查 Docker 和 Docker Compose
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
}

# 创建必要的目录
create_directories() {
    print_info "创建必要的目录..."
    mkdir -p backup logs
}

# 拉取最新镜像
pull_images() {
    print_info "拉取 Docker 镜像..."
    if docker compose version &> /dev/null; then
        docker compose pull
    else
        docker-compose pull
    fi
}

# 启动服务
start_services() {
    print_info "启动服务..."
    
    local compose_cmd="docker-compose"
    if docker compose version &> /dev/null; then
        compose_cmd="docker compose"
    fi
    
    # 启动基础设施服务
    print_info "启动基础设施服务 (PostgreSQL, Redis, MinIO)..."
    $compose_cmd up -d postgres redis minio
    
    print_info "等待基础设施服务就绪..."
    sleep 10
    
    # 检查数据库健康状态
    local max_retries=30
    local retry=0
    while [ $retry -lt $max_retries ]; do
        if $compose_cmd ps postgres | grep -q "healthy"; then
            print_info "PostgreSQL 已就绪"
            break
        fi
        retry=$((retry + 1))
        echo -n "."
        sleep 2
    done
    echo ""
    
    if [ $retry -eq $max_retries ]; then
        print_warn "PostgreSQL 健康检查超时，继续启动..."
    fi
    
    # 启动应用服务
    print_info "启动应用服务 (Backend, Frontend)..."
    $compose_cmd up -d backend frontend
    
    print_info "所有服务启动完成！"
}

# 显示服务状态
show_status() {
    print_info "服务状态:"
    if docker compose version &> /dev/null; then
        docker compose ps
    else
        docker-compose ps
    fi
}

# 显示访问信息
show_info() {
    echo ""
    echo "===================================="
    print_info "文档评审系统已启动"
    echo "===================================="
    echo ""
    echo "访问地址:"
    echo "  - 前端: http://localhost:${FRONTEND_PORT:-3000}"
    echo "  - 后端 API: http://localhost:${BACKEND_PORT:-8080}"
    echo "  - API 文档: http://localhost:${BACKEND_PORT:-8080}/doc.html"
    echo "  - MinIO 控制台: http://localhost:${MINIO_CONSOLE_PORT:-9001}"
    echo ""
    echo "默认账号:"
    echo "  - 用户名: admin"
    echo "  - 密码: admin123"
    echo ""
    echo "查看日志:"
    echo "  docker-compose logs -f backend"
    echo "  docker-compose logs -f frontend"
    echo ""
}

# 主函数
main() {
    print_info "开始启动文档评审系统..."
    
    check_docker
    check_env
    create_directories
    pull_images
    start_services
    show_status
    show_info
}

main "$@"
