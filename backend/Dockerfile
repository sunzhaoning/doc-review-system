# ====================================
# 文档评审系统 - 后端 Dockerfile
# ====================================

# 构建阶段 - 使用支持多架构的镜像
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制 pom.xml
COPY pom.xml .

# 下载依赖 (利用 Docker 缓存)
RUN mvn dependency:go-offline -B || mvn dependency:resolve -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B -Dmaven.test.skip=true

# 运行阶段 - 使用支持多架构的镜像 (ARM/AMD64)
FROM eclipse-temurin:17-jre-jammy

LABEL maintainer="doc-review-system"
LABEL version="1.0"
LABEL description="文档评审系统 - 后端服务"

# 安装必要工具
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    tzdata \
    && rm -rf /var/lib/apt/lists/*

# 创建非 root 用户
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /var/log/doc-review && chown -R appuser:appgroup /var/log/doc-review

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8080

# JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
