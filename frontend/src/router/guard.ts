import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

export function setupRouterGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()
    
    // 不需要认证的页面
    const whiteList = ['/login']
    
    if (whiteList.includes(to.path)) {
      next()
      return
    }
    
    // 检查是否有 token
    if (!authStore.token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    
    // 已有用户信息，直接进入
    if (authStore.userInfo) {
      next()
      return
    }
    
    // 获取用户信息
    try {
      await authStore.getUserInfo()
      next()
    } catch (error) {
      // token 失效，跳转登录
      authStore.token = ''
      localStorage.removeItem('token')
      next({ path: '/login', query: { redirect: to.fullPath } })
    }
  })
}
