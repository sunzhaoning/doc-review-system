#!/bin/bash
# ====================================
# 文档评审系统 - 健康检查脚本
# ====================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

# 加载环境变量
load_env() {
    if [ -f ".env" ]; then
        export $(grep -v '^#' .env | xargs)
    fi
}

# 状态标记
PASS=0
FAIL=0
WARN=0

# 检查结果
check_pass() {
    echo -e "${GREEN}[✓]${NC} $1"
    PASS=$((PASS + 1))
}

check_fail() {
    echo -e "${RED}[✗]${NC} $1"
    FAIL=$((FAIL + 1))
}

check_warn() {
    echo -e "${YELLOW}[!]${NC} $1"
    WARN=$((WARN + 1))
}

# 检查容器状态
check_containers() {
    echo ""
    echo -e "${BLUE}=== 容器状态 ===${NC}"
    
    local containers=("doc-review-postgres" "doc-review-redis" "doc-review-minio" "doc-review-backend" "doc-review-frontend")
    
    for container in "${containers[@]}"; do
        if docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
            local status=$(docker inspect --format='{{.State.Status}}' "$container" 2>/dev/null)
            if [ "$status" = "running" ]; then
                check_pass "$container: 运行中"
            else
                check_fail "$container: $status"
            fi
        else
            check_fail "$container: 未运行"
        fi
    done
}

# 检查 PostgreSQL
check_postgres() {
    echo ""
    echo -e "${BLUE}=== PostgreSQL ===${NC}"
    
    # 连接检查
    if docker exec doc-review-postgres pg_isready -U "${POSTGRES_USER:-docreview}" -d "${POSTGRES_DB:-docreview}" &> /dev/null; then
        check_pass "数据库连接正常"
    else
        check_fail "数据库连接失败"
        return
    fi
    
    # 数据库大小
    local db_size=$(docker exec doc-review-postgres psql -U "${POSTGRES_USER:-docreview}" -d "${POSTGRES_DB:-docreview}" -t -c "SELECT pg_size_pretty(pg_database_size('${POSTGRES_DB:-docreview}'));" | tr -d ' ')
    check_pass "数据库大小: $db_size"
    
    # 连接数
    local connections=$(docker exec doc-review-postgres psql -U "${POSTGRES_USER:-docreview}" -d "${POSTGRES_DB:-docreview}" -t -c "SELECT count(*) FROM pg_stat_activity WHERE datname = '${POSTGRES_DB:-docreview}';" | tr -d ' ')
    check_pass "活动连接数: $connections"
}

# 检查 Redis
check_redis() {
    echo ""
    echo -e "${BLUE}=== Redis ===${NC}"
    
    # 连接检查
    if docker exec doc-review-redis redis-cli -a "${REDIS_PASSWORD}" ping 2>/dev/null | grep -q "PONG"; then
        check_pass "Redis 连接正常"
    else
        check_fail "Redis 连接失败"
        return
    fi
    
    # 内存使用
    local used_memory=$(docker exec doc-review-redis redis-cli -a "${REDIS_PASSWORD}" INFO memory 2>/dev/null | grep "used_memory_human" | cut -d: -f2 | tr -d '\r')
    check_pass "内存使用: $used_memory"
    
    # 键数量
    local keys=$(docker exec doc-review-redis redis-cli -a "${REDIS_PASSWORD}" DBSIZE 2>/dev/null | cut -d: -f2 | tr -d '\r')
    check_pass "键数量: $keys"
}

# 检查 MinIO
check_minio() {
    echo ""
    echo -e "${BLUE}=== MinIO ===${NC}"
    
    # 健康检查
    if curl -sf "http://localhost:${MINIO_PORT:-9000}/minio/health/live" &> /dev/null; then
        check_pass "MinIO 服务正常"
    else
        check_fail "MinIO 服务异常"
        return
    fi
    
    # 磁盘使用
    local disk_info=$(docker exec doc-review-minio df -h /data 2>/dev/null | tail -1)
    local disk_used=$(echo "$disk_info" | awk '{print $3}')
    local disk_avail=$(echo "$disk_info" | awk '{print $4}')
    local disk_percent=$(echo "$disk_info" | awk '{print $5}')
    check_pass "磁盘使用: $disk_used / $disk_avail ($disk_percent)"
}

# 检查后端服务
check_backend() {
    echo ""
    echo -e "${BLUE}=== 后端服务 ===${NC}"
    
    # 健康检查端点
    if curl -sf "http://localhost:${BACKEND_PORT:-8080}/actuator/health" &> /dev/null; then
        check_pass "后端健康检查正常"
    else
        check_warn "后端健康检查失败 (可能未启用 actuator)"
    fi
    
    # API 响应
    local response_time=$(curl -o /dev/null -s -w '%{time_total}' "http://localhost:${BACKEND_PORT:-8080}/doc.html" 2>/dev/null || echo "N/A")
    if [ "$response_time" != "N/A" ]; then
        check_pass "API 文档响应时间: ${response_time}s"
    else
        check_warn "API 文档不可访问"
    fi
}

# 检查前端服务
check_frontend() {
    echo ""
    echo -e "${BLUE}=== 前端服务 ===${NC}"
    
    # HTTP 检查
    local http_code=$(curl -o /dev/null -s -w '%{http_code}' "http://localhost:${FRONTEND_PORT:-3000}" 2>/dev/null || echo "000")
    if [ "$http_code" = "200" ]; then
        check_pass "前端服务正常 (HTTP $http_code)"
    else
        check_fail "前端服务异常 (HTTP $http_code)"
    fi
}

# 检查磁盘空间
check_disk_space() {
    echo ""
    echo -e "${BLUE}=== 系统资源 ===${NC}"
    
    # Docker 磁盘使用
    local docker_disk=$(docker system df 2>/dev/null | head -2 | tail -1)
    if [ -n "$docker_disk" ]; then
        check_pass "Docker 存储: $docker_disk"
    fi
    
    # 主机磁盘
    local host_disk=$(df -h / 2>/dev/null | tail -1 | awk '{print $3 " / " $2 " (" $5 " used)"}')
    check_pass "主机磁盘: $host_disk"
}

# 生成报告
generate_report() {
    echo ""
    echo -e "${BLUE}=====================================${NC}"
    echo -e "${BLUE}       健康检查报告                  ${NC}"
    echo -e "${BLUE}=====================================${NC}"
    echo ""
    echo -e "时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo ""
    echo -e "${GREEN}通过: $PASS${NC}"
    echo -e "${YELLOW}警告: $WARN${NC}"
    echo -e "${RED}失败: $FAIL${NC}"
    echo ""
    
    if [ $FAIL -eq 0 ]; then
        echo -e "${GREEN}系统状态: 健康 ✓${NC}"
        return 0
    else
        echo -e "${RED}系统状态: 异常 ✗${NC}"
        return 1
    fi
}

# 显示帮助
show_help() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  (无参数)     完整健康检查"
    echo "  --quick      快速检查 (仅容器状态)"
    echo "  --container  检查容器状态"
    echo "  --postgres   检查 PostgreSQL"
    echo "  --redis      检查 Redis"
    echo "  --minio      检查 MinIO"
    echo "  --backend    检查后端服务"
    echo "  --frontend   检查前端服务"
    echo "  -h, --help   显示此帮助信息"
    echo ""
}

# 主函数
main() {
    load_env
    
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════╗"
    echo "║     文档评审系统 - 健康检查          ║"
    echo "╚═══════════════════════════════════════╝"
    echo -e "${NC}"
    
    case "${1:-}" in
        --quick|--container)
            check_containers
            generate_report
            ;;
        --postgres)
            check_postgres
            generate_report
            ;;
        --redis)
            check_redis
            generate_report
            ;;
        --minio)
            check_minio
            generate_report
            ;;
        --backend)
            check_backend
            generate_report
            ;;
        --frontend)
            check_frontend
            generate_report
            ;;
        -h|--help)
            show_help
            ;;
        *)
            check_containers
            check_postgres
            check_redis
            check_minio
            check_backend
            check_frontend
            check_disk_space
            generate_report
            ;;
    esac
}

main "$@"
