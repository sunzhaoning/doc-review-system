<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff;">
              <el-icon size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalDocuments }}</div>
              <div class="stat-label">文档总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c;">
              <el-icon size="28"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingReviews }}</div>
              <div class="stat-label">待评审</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a;">
              <el-icon size="28"><Finished /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completedReviews }}</div>
              <div class="stat-label">已完成评审</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #909399;">
              <el-icon size="28"><FolderOpened /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.archivedDocuments }}</div>
              <div class="stat-label">已归档</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <span>快捷操作</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-button type="primary" size="large" class="action-btn" @click="goTo('/document/upload')">
            <el-icon><Upload /></el-icon>
            上传文档
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="warning" size="large" class="action-btn" @click="goTo('/review/pending')">
            <el-icon><Edit /></el-icon>
            待评审
            <el-badge :value="stats.pendingReviews" class="badge-dot" />
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button size="large" class="action-btn" @click="goTo('/document/list')">
            <el-icon><Files /></el-icon>
            我的文档
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button size="large" class="action-btn" @click="goTo('/archive')">
            <el-icon><Search /></el-icon>
            归档检索
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 最近文档 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <span>最近文档</span>
          <el-button type="primary" link @click="goTo('/document/list')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentDocuments" stripe style="width: 100%">
        <el-table-column prop="title" label="文档标题" min-width="200" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDocument(row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 待办评审 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <span>待办评审</span>
          <el-button type="primary" link @click="goTo('/review/pending')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="pendingReviews" stripe style="width: 100%">
        <el-table-column prop="documentTitle" label="文档标题" min-width="200" />
        <el-table-column prop="submitter" label="提交人" width="120" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)">{{ getPriorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日期" width="150" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="startReview(row.id)">开始评审</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/api/request'

const router = useRouter()

interface Stats {
  totalDocuments: number
  pendingReviews: number
  completedReviews: number
  archivedDocuments: number
}

interface Document {
  id: number
  title: string
  author: string
  status: string
  createdAt: string
}

interface Review {
  id: number
  documentTitle: string
  submitter: string
  priority: string
  dueDate: string
}

const stats = ref<Stats>({
  totalDocuments: 0,
  pendingReviews: 0,
  completedReviews: 0,
  archivedDocuments: 0
})

const recentDocuments = ref<Document[]>([])
const pendingReviews = ref<Review[]>([])

const goTo = (path: string) => {
  router.push(path)
}

const viewDocument = (id: number) => {
  router.push(`/document/list?id=${id}`)
}

const startReview = (id: number) => {
  router.push(`/review/pending?id=${id}`)
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    draft: 'info',
    pending: 'warning',
    reviewing: 'primary',
    approved: 'success',
    rejected: 'danger',
    archived: ''
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    draft: '草稿',
    pending: '待评审',
    reviewing: '评审中',
    approved: '已通过',
    rejected: '已拒绝',
    archived: '已归档'
  }
  return texts[status] || status
}

const getPriorityType = (priority: string) => {
  const types: Record<string, string> = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return types[priority] || 'info'
}

const getPriorityText = (priority: string) => {
  const texts: Record<string, string> = {
    high: '高',
    medium: '中',
    low: '低'
  }
  return texts[priority] || priority
}

const fetchDashboardData = async () => {
  try {
    const [statsRes, docsRes, reviewsRes] = await Promise.all([
      request.get('/documents/stats'),
      request.get('/documents', { limit: 5 }),
      request.get('/reviews/pending', { limit: 5 })
    ])
    
    stats.value = statsRes.data
    recentDocuments.value = docsRes.data?.list || []
    pendingReviews.value = reviewsRes.data?.list || []
  } catch (error) {
    console.error('Failed to fetch dashboard data:', error)
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  margin-left: 16px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.section-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-btn {
  width: 100%;
  height: 60px;
  font-size: 16px;
}

.badge-dot {
  margin-left: 8px;
}
</style>
