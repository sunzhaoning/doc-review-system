<template>
  <div class="not-found">
    <div class="content">
      <h1>404</h1>
      <h2>页面未找到</h2>
      <p>抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}

.content {
  text-align: center;
}

.content h1 {
  font-size: 120px;
  color: #409eff;
  margin-bottom: 0;
}

.content h2 {
  font-size: 32px;
  color: #333;
  margin: 20px 0;
}

.content p {
  font-size: 16px;
  color: #999;
  margin-bottom: 30px;
}
</style>
