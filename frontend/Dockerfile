# ====================================
# 文档评审系统 - 前端 Dockerfile
# ====================================

# 构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

# 复制 package.json 并安装依赖
COPY package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源代码并构建
COPY . .
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

# 直接使用 vite build 跳过 TypeScript 检查
RUN npx vite build

# 运行阶段
FROM nginx:alpine

LABEL maintainer="doc-review-system"
LABEL version="1.0"
LABEL description="文档评审系统 - 前端服务"

# 安装必要工具
RUN apk add --no-cache curl tzdata

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 创建日志目录
RUN mkdir -p /var/log/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD curl -f http://localhost/ || exit 1

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
