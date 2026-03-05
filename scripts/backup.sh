#!/bin/bash
# ====================================
# 文档评审系统 - 备份脚本
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
BACKUP_RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-7}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_NAME="doc-review-backup-${TIMESTAMP}"

# 创建备份目录
create_backup_dir() {
    mkdir -p "$BACKUP_DIR/$BACKUP_NAME"
    print_info "创建备份目录: $BACKUP_DIR/$BACKUP_NAME"
}

# 备份 PostgreSQL
backup_postgres() {
    print_info "备份 PostgreSQL 数据库..."
    
    local backup_file="$BACKUP_DIR/$BACKUP_NAME/postgres_backup.sql"
    
    docker exec doc-review-postgres pg_dump \
        -U "${POSTGRES_USER:-docreview}" \
        -d "${POSTGRES_DB:-docreview}" \
        --format=plain \
        --no-owner \
        --no-acl \
        > "$backup_file"
    
    # 压缩备份
    gzip "$backup_file"
    
    print_info "PostgreSQL 备份完成: ${backup_file}.gz"
}

# 备份 Redis
backup_redis() {
    print_info "备份 Redis 数据..."
    
    local backup_file="$BACKUP_DIR/$BACKUP_NAME/redis_backup.rdb"
    
    # 触发 Redis 保存
    docker exec doc-review-redis redis-cli -a "${REDIS_PASSWORD}" BGSAVE
    
    # 等待保存完成
    sleep 5
    
    # 复制备份文件
    docker cp doc-review-redis:/data/dump.rdb "$backup_file"
    
    print_info "Redis 备份完成: $backup_file"
}

# 备份 MinIO
backup_minio() {
    print_info "备份 MinIO 数据..."
    
    local backup_dir="$BACKUP_DIR/$BACKUP_NAME/minio_backup"
    mkdir -p "$backup_dir"
    
    # 使用 mc mirror 备份
    docker exec doc-review-minio mc alias set local http://localhost:9000 \
        "${MINIO_ROOT_USER:-minioadmin}" \
        "${MINIO_ROOT_PASSWORD}"
    
    # 导出所有 buckets
    for bucket in $(docker exec doc-review-minio mc ls local/ | awk '{print $NF}' | tr -d '/'); do
        print_info "备份 bucket: $bucket"
        docker exec doc-review-minio mc mirror "local/$bucket" "/backup/$bucket" 2>/dev/null || true
    done
    
    # 直接从容器复制数据
    docker cp doc-review-minio:/data "$backup_dir/data"
    
    print_info "MinIO 备份完成: $backup_dir"
}

# 备份配置文件
backup_configs() {
    print_info "备份配置文件..."
    
    local backup_dir="$BACKUP_DIR/$BACKUP_NAME/configs"
    mkdir -p "$backup_dir"
    
    # 备份 .env 文件
    if [ -f ".env" ]; then
        cp .env "$backup_dir/"
    fi
    
    # 备份 docker-compose.yml
    if [ -f "docker-compose.yml" ]; then
        cp docker-compose.yml "$backup_dir/"
    fi
    
    print_info "配置文件备份完成: $backup_dir"
}

# 创建备份清单
create_manifest() {
    print_info "创建备份清单..."
    
    local manifest="$BACKUP_DIR/$BACKUP_NAME/manifest.json"
    
    cat > "$manifest" << EOF
{
    "timestamp": "$(date -Iseconds)",
    "version": "1.0",
    "components": {
        "postgres": {
            "file": "postgres_backup.sql.gz",
            "type": "pg_dump"
        },
        "redis": {
            "file": "redis_backup.rdb",
            "type": "rdb"
        },
        "minio": {
            "directory": "minio_backup",
            "type": "mc_mirror"
        },
        "configs": {
            "directory": "configs",
            "type": "copy"
        }
    },
    "retention_days": $BACKUP_RETENTION_DAYS
}
EOF
    
    print_info "备份清单已创建: $manifest"
}

# 压缩备份
compress_backup() {
    print_info "压缩备份文件..."
    
    cd "$BACKUP_DIR"
    tar -czf "${BACKUP_NAME}.tar.gz" "$BACKUP_NAME"
    rm -rf "$BACKUP_NAME"
    
    print_info "备份压缩完成: $BACKUP_DIR/${BACKUP_NAME}.tar.gz"
    
    # 计算备份大小
    local size=$(du -h "${BACKUP_NAME}.tar.gz" | cut -f1)
    print_info "备份大小: $size"
}

# 清理旧备份
cleanup_old_backups() {
    print_info "清理超过 $BACKUP_RETENTION_DAYS 天的旧备份..."
    
    find "$BACKUP_DIR" -name "doc-review-backup-*.tar.gz" -type f -mtime +$BACKUP_RETENTION_DAYS -delete
    
    local count=$(find "$BACKUP_DIR" -name "doc-review-backup-*.tar.gz" -type f | wc -l)
    print_info "当前保留备份数量: $count"
}

# 显示帮助
show_help() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  (无参数)    完整备份 (数据库 + Redis + MinIO + 配置)"
    echo "  --db        仅备份数据库"
    echo "  --redis     仅备份 Redis"
    echo "  --minio     仅备份 MinIO"
    echo "  --config    仅备份配置文件"
    echo "  --clean     仅清理旧备份"
    echo "  -h, --help  显示此帮助信息"
    echo ""
    echo "环境变量:"
    echo "  BACKUP_DIR           备份目录 (默认: ./backup)"
    echo "  BACKUP_RETENTION_DAYS 备份保留天数 (默认: 7)"
    echo ""
}

# 主函数
main() {
    load_env
    
    case "${1:-}" in
        --db)
            create_backup_dir
            backup_postgres
            ;;
        --redis)
            create_backup_dir
            backup_redis
            ;;
        --minio)
            create_backup_dir
            backup_minio
            ;;
        --config)
            create_backup_dir
            backup_configs
            ;;
        --clean)
            cleanup_old_backups
            ;;
        -h|--help)
            show_help
            ;;
        "")
            print_info "开始完整备份..."
            create_backup_dir
            backup_postgres
            backup_redis
            backup_minio
            backup_configs
            create_manifest
            compress_backup
            cleanup_old_backups
            print_info "备份完成！"
            ;;
        *)
            print_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
