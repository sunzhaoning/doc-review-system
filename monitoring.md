# ====================================
# 文档评审系统 - 监控配置
# ====================================

# 此文件定义了各服务的健康检查端点和监控配置

## 服务健康检查端点

### 后端服务 (Spring Boot Actuator)
- 健康检查: http://localhost:8080/actuator/health
- 详细信息: http://localhost:8080/actuator/info
- 指标数据: http://localhost:8080/actuator/metrics
- Prometheus 格式: http://localhost:8080/actuator/prometheus

### PostgreSQL
- TCP 端口: 5432
- 健康检查: pg_isready -U docreview -d docreview

### Redis
- TCP 端口: 6379
- 健康检查: redis-cli -a <password> ping

### MinIO
- API 端口: 9000
- 控制台端口: 9001
- 健康检查: http://localhost:9000/minio/health/live

### 前端服务
- HTTP 端口: 3000 (或 80)
- 健康检查: http://localhost:3000/

---

## Prometheus 配置示例

# 在 prometheus.yml 中添加以下配置:

```yaml
scrape_configs:
  - job_name: 'doc-review-backend'
    static_configs:
      - targets: ['backend:8080']
    metrics_path: '/actuator/prometheus'
    
  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres-exporter:9187']
    
  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']
    
  - job_name: 'minio'
    static_configs:
      - targets: ['minio:9000']
    metrics_path: '/minio/v2/metrics/cluster'
```

---

## Grafana Dashboard 推荐

1. JVM Dashboard (Spring Boot): ID 4701
2. PostgreSQL Dashboard: ID 9628
3. Redis Dashboard: ID 11835
4. MinIO Dashboard: ID 13502

---

## 告警规则示例 (Prometheus AlertManager)

```yaml
groups:
  - name: doc-review-alerts
    rules:
      # 后端服务告警
      - alert: BackendDown
        expr: up{job="doc-review-backend"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "后端服务不可用"
          description: "后端服务已停止响应超过 1 分钟"
      
      - alert: BackendHighMemory
        expr: jvm_memory_used_bytes{job="doc-review-backend"} / jvm_memory_max_bytes{job="doc-review-backend"} > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "后端内存使用过高"
          description: "JVM 内存使用率超过 90%"
      
      # 数据库告警
      - alert: PostgresDown
        expr: up{job="postgres"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "PostgreSQL 不可用"
          description: "PostgreSQL 数据库已停止响应"
      
      - alert: PostgresTooManyConnections
        expr: pg_stat_activity_count > 100
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "PostgreSQL 连接数过多"
          description: "数据库连接数超过 100"
      
      # Redis 告警
      - alert: RedisDown
        expr: up{job="redis"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Redis 不可用"
          description: "Redis 服务已停止响应"
      
      - alert: RedisHighMemory
        expr: redis_memory_used_bytes / redis_memory_max_bytes > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Redis 内存使用过高"
          description: "Redis 内存使用率超过 90%"
      
      # MinIO 告警
      - alert: MinIODown
        expr: up{job="minio"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "MinIO 不可用"
          description: "MinIO 对象存储服务已停止响应"
      
      - alert: MinIODiskSpaceLow
        expr: minio_cluster_disk_free_bytes / minio_cluster_disk_total_bytes < 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "MinIO 磁盘空间不足"
          description: "MinIO 可用磁盘空间不足 10%"
```
