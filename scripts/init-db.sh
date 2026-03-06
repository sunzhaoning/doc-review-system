#!/bin/bash
# ====================================
# 文档评审系统 - 数据库初始化脚本
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
DATABASE_DIR="$PROJECT_DIR/database"

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

# 加载环境变量
load_env() {
    if [ -f ".env" ]; then
        print_info "加载环境变量..."
        export $(grep -v '^#' .env | xargs)
    else
        print_error ".env 文件不存在"
        exit 1
    fi
}

# 检查数据库连接
check_connection() {
    print_info "检查数据库连接..."
    
    local max_retries=10
    local retry=0
    
    while [ $retry -lt $max_retries ]; do
        if docker exec doc-review-postgres pg_isready -U "${POSTGRES_USER:-docreview}" -d "${POSTGRES_DB:-docreview}" &> /dev/null; then
            print_info "数据库连接成功"
            return 0
        fi
        retry=$((retry + 1))
        echo -n "."
        sleep 2
    done
    echo ""
    
    print_error "无法连接到数据库"
    return 1
}

# 初始化数据库
init_database() {
    print_info "初始化数据库..."
    
    # 检查初始化脚本是否存在
    if [ ! -f "$DATABASE_DIR/init.sql" ]; then
        print_error "数据库初始化脚本不存在: $DATABASE_DIR/init.sql"
        exit 1
    fi
    
    # 执行初始化脚本
    print_info "执行表结构初始化..."
    docker exec -i doc-review-postgres psql \
        -U "${POSTGRES_USER:-docreview}" \
        -d "${POSTGRES_DB:-docreview}" \
        < "$DATABASE_DIR/init.sql"
    
    print_info "表结构初始化完成"
}

# 导入初始数据
import_data() {
    print_info "导入初始数据..."
    
    if [ -f "$DATABASE_DIR/data.sql" ]; then
        docker exec -i doc-review-postgres psql \
            -U "${POSTGRES_USER:-docreview}" \
            -d "${POSTGRES_DB:-docreview}" \
            < "$DATABASE_DIR/data.sql"
        print_info "初始数据导入完成"
    else
        print_warn "初始数据文件不存在: $DATABASE_DIR/data.sql"
    fi
}

# 创建 MinIO bucket
init_minio() {
    print_info "初始化 MinIO bucket..."
    
    # 等待 MinIO 就绪
    sleep 5
    
    # 创建 bucket
    docker exec doc-review-minio mc alias set local http://localhost:9000 \
        "${MINIO_ROOT_USER:-minioadmin}" \
        "${MINIO_ROOT_PASSWORD}" 2>/dev/null || true
    
    docker exec doc-review-minio mc mb local/${MINIO_BUCKET:-doc-review} 2>/dev/null || true
    
    print_info "MinIO bucket 初始化完成"
}

# 验证初始化
verify() {
    print_info "验证数据库初始化..."
    
    # 检查表是否存在
    local tables=$(docker exec doc-review-postgres psql \
        -U "${POSTGRES_USER:-docreview}" \
        -d "${POSTGRES_DB:-docreview}" \
        -t -c "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public';")
    
    print_info "数据库中包含 $tables 张表"
    
    # 检查管理员用户
    local admin=$(docker exec doc-review-postgres psql \
        -U "${POSTGRES_USER:-docreview}" \
        -d "${POSTGRES_DB:-docreview}" \
        -t -c "SELECT username FROM sys_user WHERE username = 'admin' LIMIT 1;" 2>/dev/null | tr -d ' ')
    
    if [ "$admin" = "admin" ]; then
        print_info "管理员用户已创建"
    else
        print_warn "未找到管理员用户"
    fi
}

# 显示帮助
show_help() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  (无参数)    完整初始化 (结构 + 数据)"
    echo "  --schema    仅初始化表结构"
    echo "  --data      仅导入初始数据"
    echo "  --minio     仅初始化 MinIO"
    echo "  --verify    验证初始化结果"
    echo "  -h, --help  显示此帮助信息"
    echo ""
}

# 主函数
main() {
    load_env
    
    case "${1:-}" in
        --schema)
            check_connection
            init_database
            ;;
        --data)
            check_connection
            import_data
            ;;
        --minio)
            init_minio
            ;;
        --verify)
            check_connection
            verify
            ;;
        -h|--help)
            show_help
            ;;
        "")
            print_info "开始完整初始化..."
            check_connection
            init_database
            import_data
            init_minio
            verify
            print_info "初始化完成！"
            ;;
        *)
            print_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
