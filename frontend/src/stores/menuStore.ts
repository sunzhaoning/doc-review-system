import { defineStore } from 'pinia'
import { ref } from 'vue'
import { request } from '@/api/request'

export interface MenuItem {
  id: number
  parentId: number
  menuName: string
  menuCode: string
  menuType: number // 1=目录, 2=菜单, 3=按钮
  path: string
  component: string
  perms: string
  icon: string
  sort: number
  visible: number
  status: number
  children?: MenuItem[]
}

export const useMenuStore = defineStore('menu', () => {
  const menuList = ref<MenuItem[]>([])
  const loaded = ref(false)

  // 加载菜单
  const loadMenus = async () => {
    if (loaded.value) return menuList.value
    
    try {
      const res = await request.get('/menus/user')
      // 只保留菜单类型（1=目录，2=菜单），过滤掉按钮（3）
      const filterMenus = (menus: MenuItem[]): MenuItem[] => {
        return menus
          .filter(m => m.menuType !== 3 && m.status === 1 && m.visible === 1)
          .map(m => ({
            ...m,
            children: m.children ? filterMenus(m.children) : undefined
          }))
          .sort((a, b) => a.sort - b.sort)
      }
      menuList.value = filterMenus(res.data || [])
      loaded.value = true
      return menuList.value
    } catch (error) {
      console.error('加载菜单失败:', error)
      return []
    }
  }

  // 强制刷新菜单
  const refreshMenus = async () => {
    loaded.value = false
    return loadMenus()
  }

  // 清除菜单
  const clearMenus = () => {
    menuList.value = []
    loaded.value = false
  }

  return {
    menuList,
    loaded,
    loadMenus,
    refreshMenus,
    clearMenus
  }
})
