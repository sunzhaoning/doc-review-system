import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { request } from '@/api/request'
import type { UserInfo } from '@/types/user'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.realName || userInfo.value?.username || '')
  const roles = computed(() => userInfo.value?.roles || [])
  const permissions = computed(() => userInfo.value?.permissions || [])
  
  // 方法
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
  }
  
  async function login(username: string, password: string) {
    const res = await request.post('/auth/login', { username, password })
    setToken(res.data.token)
    setUserInfo(res.data.userInfo)
    return res
  }
  
  async function logout() {
    try {
      await request.post('/auth/logout')
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
    }
  }
  
  async function getUserInfo() {
    const res = await request.get('/auth/me')
    setUserInfo(res.data)
    return res
  }
  
  function hasPermission(permission: string) {
    return permissions.value.includes(permission)
  }
  
  function hasRole(role: string) {
    return roles.value.includes(role)
  }
  
  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    roles,
    permissions,
    setToken,
    setUserInfo,
    login,
    logout,
    getUserInfo,
    hasPermission,
    hasRole
  }
})
