#!/bin/bash
# ====================================
# 文档评审系统 - 停止脚本
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

# 停止服务
stop_services() {
    print_info "停止所有服务..."
    
    if docker compose version &> /dev/null; then
        docker compose down
    else
        docker-compose down
    fi
}

# 停止并清理
stop_and_clean() {
    print_warn "这将删除所有容器和网络，但保留数据卷"
    read -p "确认继续? (y/N): " confirm
    if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
        print_info "停止并清理服务..."
        if docker compose version &> /dev/null; then
            docker compose down --remove-orphans
        else
            docker-compose down --remove-orphans
        fi
        print_info "清理完成"
    else
        print_info "操作已取消"
    fi
}

# 完全清理 (包括数据)
full_cleanup() {
    print_error "警告：这将删除所有容器、网络和数据卷！"
    print_error "所有数据将被永久删除！"
    read -p "确认继续? (yes/NO): " confirm
    if [ "$confirm" = "yes" ]; then
        print_info "执行完全清理..."
        if docker compose version &> /dev/null; then
            docker compose down -v --remove-orphans
        else
            docker-compose down -v --remove-orphans
        fi
        print_info "完全清理完成"
    else
        print_info "操作已取消"
    fi
}

# 显示帮助
show_help() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  (无参数)    停止所有服务 (保留数据)"
    echo "  --clean     停止服务并清理容器和网络 (保留数据卷)"
    echo "  --full      完全清理 (包括数据卷，慎用！)"
    echo "  -h, --help  显示此帮助信息"
    echo ""
}

# 主函数
main() {
    case "${1:-}" in
        --clean)
            stop_and_clean
            ;;
        --full)
            full_cleanup
            ;;
        -h|--help)
            show_help
            ;;
        "")
            stop_services
            print_info "服务已停止"
            ;;
        *)
            print_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
