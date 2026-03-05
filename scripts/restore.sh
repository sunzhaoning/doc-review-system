#!/bin/bash
# ====================================
# 文档评审系统 - 恢复脚本
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

# 加载环境变量
load_env() {
    if [ -f ".env" ]; then
        export $(grep -v '^#' .env | xargs)
    fi
}

# 配置
BACKUP_DIR="${BACKUP_DIR:-$PROJECT_DIR/backup}"

# 列出可用备份
list_backups() {
    print_info "可用的备份文件:"
    echo ""
    
    local count=0
    for backup in $(ls -t "$BACKUP_DIR"/doc-review-backup-*.tar.gz 2>/dev/null); do
        count=$((count + 1))
        local filename=$(basename "$backup")
        local size=$(du -h "$backup" | cut -f1)
        local date=$(stat -c %y "$backup" | cut -d'.' -f1)
        echo "  [$count] $filename ($size, $date)"
    done
    
    if [ $count -eq 0 ]; then
        print_warn "没有找到备份文件"
    fi
    
    echo ""
}

# 解压备份
extract_backup() {
    local backup_file="$1"
    
    if [ ! -f "$backup_file" ]; then
        print_error "备份文件不存在: $backup_file"
        exit 1
    fi
    
    print_info "解压备份文件: $backup_file"
    
    cd "$BACKUP_DIR"
    tar -xzf "$backup_file"
    
    # 获取解压后的目录名
    BACKUP_NAME=$(basename "$backup_file" .tar.gz)
    
    print_info "备份已解压到: $BACKUP_DIR/$BACKUP_NAME"
}

# 恢复 PostgreSQL
restore_postgres() {
    print_info "恢复 PostgreSQL 数据库..."
    
    local backup_file="$BACKUP_DIR/$BACKUP_NAME/postgres_backup.sql.gz"
    
    if [ ! -f "$backup_file" ]; then
        print_warn "PostgreSQL 备份文件不存在，跳过"
        return
    fi
    
    # 解压 SQL 文件
    gunzip -k "$backup_file" 2>/dev/null || true
    local sql_file="${backup_file%.gz}"
    
    # 恢复数据库
    docker exec -i doc-review-postgres psql \
        -U "${POSTGRES_USER:-docreview}" \
        -d "${POSTGRES_DB:-docreview}" \
        < "$sql_file"
    
    # 清理解压的文件
    rm -f "$sql_file"
    
    print_info "PostgreSQL 恢复完成"
}

# 恢复 Redis
restore_redis() {
    print_info "恢复 Redis 数据..."
    
    local backup_file="$BACKUP_DIR/$BACKUP_NAME/redis_backup.rdb"
    
    if [ ! -f "$backup_file" ]; then
        print_warn "Redis 备份文件不存在，跳过"
        return
    fi
    
    # 停止 Redis
    docker stop doc-review-redis
    
    # 复制备份文件
    docker cp "$backup_file" doc-review-redis:/data/dump.rdb
    
    # 启动 Redis
    docker start doc-review-redis
    
    print_info "Redis 恢复完成"
}

# 恢复 MinIO
restore_minio() {
    print_info "恢复 MinIO 数据..."
    
    local backup_dir="$BACKUP_DIR/$BACKUP_NAME/minio_backup/data"
    
    if [ ! -d "$backup_dir" ]; then
        print_warn "MinIO 备份目录不存在，跳过"
        return
    fi
    
    # 停止 MinIO
    docker stop doc-review-minio
    
    # 复制备份数据
    docker cp "$backup_dir/." doc-review-minio:/data/
    
    # 启动 MinIO
    docker start doc-review-minio
    
    print_info "MinIO 恢复完成"
}

# 恢复配置文件
restore_configs() {
    print_info "恢复配置文件..."
    
    local backup_dir="$BACKUP_DIR/$BACKUP_NAME/configs"
    
    if [ ! -d "$backup_dir" ]; then
        print_warn "配置备份目录不存在，跳过"
        return
    fi
    
    # 恢复配置文件
    if [ -f "$backup_dir/.env" ]; then
        cp "$backup_dir/.env" "$PROJECT_DIR/"
        print_info ".env 文件已恢复"
    fi
    
    if [ -f "$backup_dir/docker-compose.yml" ]; then
        cp "$backup_dir/docker-compose.yml" "$PROJECT_DIR/"
        print_info "docker-compose.yml 文件已恢复"
    fi
    
    print_info "配置文件恢复完成"
}

# 清理解压文件
cleanup() {
    print_info "清理临时文件..."
    rm -rf "$BACKUP_DIR/$BACKUP_NAME"
}

# 完整恢复
full_restore() {
    local backup_file="$1"
    
    if [ -z "$backup_file" ]; then
        print_error "请指定备份文件"
        list_backups
        exit 1
    fi
    
    # 如果只提供数字，转换为文件名
    if [[ "$backup_file" =~ ^[0-9]+$ ]]; then
        local files=($(ls -t "$BACKUP_DIR"/doc-review-backup-*.tar.gz 2>/dev/null))
        local index=$((backup_file - 1))
        if [ $index -ge 0 ] && [ $index -lt ${#files[@]} ]; then
            backup_file="${files[$index]}"
        else
            print_error "无效的备份编号: $backup_file"
            exit 1
        fi
    fi
    
    print_warn "警告：恢复操作将覆盖当前数据！"
    read -p "确认继续? (yes/NO): " confirm
    if [ "$confirm" != "yes" ]; then
        print_info "操作已取消"
        exit 0
    fi
    
    print_info "开始恢复..."
    
    extract_backup "$backup_file"
    restore_postgres
    restore_redis
    restore_minio
    restore_configs
    cleanup
    
    print_info "恢复完成！"
    print_info "建议重启所有服务: ./scripts/start.sh"
}

# 显示帮助
show_help() {
    echo "用法: $0 [选项] [备份文件]"
    echo ""
    echo "选项:"
    echo "  -l, --list    列出所有可用备份"
    echo "  --db FILE     仅恢复数据库"
    echo "  --redis FILE  仅恢复 Redis"
    echo "  --minio FILE  仅恢复 MinIO"
    echo "  --config FILE 仅恢复配置文件"
    echo "  -h, --help    显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 -l                           # 列出备份"
    echo "  $0                              # 交互式恢复"
    echo "  $0 backup.tar.gz                # 恢复指定备份"
    echo "  $0 1                            # 恢复编号为 1 的备份"
    echo ""
}

# 主函数
main() {
    load_env
    
    case "${1:-}" in
        -l|--list)
            list_backups
            ;;
        --db)
            extract_backup "$2"
            restore_postgres
            cleanup
            ;;
        --redis)
            extract_backup "$2"
            restore_redis
            cleanup
            ;;
        --minio)
            extract_backup "$2"
            restore_minio
            cleanup
            ;;
        --config)
            extract_backup "$2"
            restore_configs
            cleanup
            ;;
        -h|--help)
            show_help
            ;;
        *)
            if [ -n "${1:-}" ]; then
                full_restore "$1"
            else
                list_backups
                read -p "请输入要恢复的备份编号或文件名: " backup_input
                full_restore "$backup_input"
            fi
            ;;
    esac
}

main "$@"
