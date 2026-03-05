import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/Layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      // 文档管理
      {
        path: 'document',
        name: 'Document',
        redirect: '/document/list',
        meta: { title: '文档管理', icon: 'Document' },
        children: [
          {
            path: 'list',
            name: 'DocumentList',
            component: () => import('@/views/document/DocumentList.vue'),
            meta: { title: '我的文档', icon: 'Files' }
          },
          {
            path: 'upload',
            name: 'DocumentUpload',
            component: () => import('@/views/document/DocumentUpload.vue'),
            meta: { title: '上传文档', icon: 'Upload' }
          }
        ]
      },
      // 评审管理
      {
        path: 'review',
        name: 'Review',
        redirect: '/review/pending',
        meta: { title: '评审管理', icon: 'Edit' },
        children: [
          {
            path: 'pending',
            name: 'ReviewPending',
            component: () => import('@/views/review/ReviewPending.vue'),
            meta: { title: '待评审', icon: 'Clock' }
          },
          {
            path: 'history',
            name: 'ReviewHistory',
            component: () => import('@/views/review/ReviewHistory.vue'),
            meta: { title: '评审历史', icon: 'Finished' }
          }
        ]
      },
      // 归档检索
      {
        path: 'archive',
        name: 'Archive',
        component: () => import('@/views/archive/Archive.vue'),
        meta: { title: '归档检索', icon: 'FolderOpened' }
      },
      // 系统管理
      {
        path: 'system',
        name: 'System',
        redirect: '/system/user',
        meta: { title: '系统管理', icon: 'Setting' },
        children: [
          {
            path: 'user',
            name: 'UserManage',
            component: () => import('@/views/system/UserManage.vue'),
            meta: { title: '用户管理', icon: 'User' }
          },
          {
            path: 'role',
            name: 'RoleManage',
            component: () => import('@/views/system/RoleManage.vue'),
            meta: { title: '角色管理', icon: 'UserFilled' }
          },
          {
            path: 'menu',
            name: 'MenuManage',
            component: () => import('@/views/system/MenuManage.vue'),
            meta: { title: '菜单管理', icon: 'Menu' }
          },
          {
            path: 'permission',
            name: 'PermissionManage',
            component: () => import('@/views/system/PermissionManage.vue'),
            meta: { title: '权限管理', icon: 'Lock' }
          }
        ]
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
