<template>
  <div v-loading="loading" class="document-detail">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="文档标题" :span="2">{{ docInfo.title }}</el-descriptions-item>
      <el-descriptions-item label="文件名">{{ docInfo.fileName }}</el-descriptions-item>
      <el-descriptions-item label="文件类型">{{ docInfo.fileType?.toUpperCase() }}</el-descriptions-item>
      <el-descriptions-item label="文件大小">{{ formatSize(docInfo.fileSize) }}</el-descriptions-item>
      <el-descriptions-item label="版本">{{ docInfo.version }}</el-descriptions-item>
      <el-descriptions-item label="评审类型">{{ getReviewTypeText(docInfo.reviewType) }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="getStatusType(docInfo.status)" size="small">
          {{ getStatusText(docInfo.status) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="描述" :span="2">{{ docInfo.description || '-' }}</el-descriptions-item>
      <el-descriptions-item label="提交人">{{ docInfo.submitterName }}</el-descriptions-item>
      <el-descriptions-item label="截止日期">{{ formatDate(docInfo.deadline) }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ formatDate(docInfo.createdAt) }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ formatDate(docInfo.updatedAt) }}</el-descriptions-item>
    </el-descriptions>

    <!-- 评审进度 -->
    <div v-if="docInfo.reviewProgress" class="review-progress">
      <h4>评审进度</h4>
      <el-progress 
        :percentage="reviewProgressPercent" 
        :format="() => `${docInfo.reviewProgress.completed}/${docInfo.reviewProgress.total}`"
      />
      <div class="progress-detail">
        <span>通过: {{ docInfo.reviewProgress.approved }}</span>
        <span>拒绝: {{ docInfo.reviewProgress.rejected }}</span>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="actions">
      <el-button type="primary" @click="handleDownload">
        <el-icon><Download /></el-icon>
        下载文档
      </el-button>
      <el-button @click="handlePreview">
        <el-icon><View /></el-icon>
        预览
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { request } from '@/api/request'

const props = defineProps<{
  documentId: number
}>()

const loading = ref(false)
const docInfo = ref<any>({})

const reviewProgressPercent = computed(() => {
  if (!docInfo.value.reviewProgress || docInfo.value.reviewProgress.total === 0) return 0
  return Math.round((docInfo.value.reviewProgress.completed / docInfo.value.reviewProgress.total) * 100)
})

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    DRAFT: 'info',
    PENDING: 'warning',
    REVIEWING: 'primary',
    APPROVED: 'success',
    REJECTED: 'danger',
    ARCHIVED: ''
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    DRAFT: '草稿',
    PENDING: '待评审',
    REVIEWING: '评审中',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    ARCHIVED: '已归档',
    REVISION: '待修改'
  }
  return texts[status] || status
}

const getReviewTypeText = (type: string) => {
  const texts: Record<string, string> = {
    internal: '内部评审',
    external: '外部评审',
    cross: '交叉评审'
  }
  return texts[type] || type || '-'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

const formatSize = (size: number) => {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / 1024 / 1024).toFixed(2) + ' MB'
}

const handleDownload = async () => {
  try {
    const res = await request.get(`/documents/${props.documentId}/download`, {}, {
      responseType: 'blob'
    })
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const link = window.document.createElement('a')
    link.href = url
    link.download = docInfo.value.fileName || 'document'
    window.document.body.appendChild(link)
    link.click()
    window.document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error: any) {
    ElMessage.error(error.message || '下载失败')
  }
}

const handlePreview = async () => {
  try {
    const res = await request.get(`/documents/${props.documentId}/preview`)
    if (res.data) {
      window.open(res.data, '_blank')
    } else {
      ElMessage.warning('暂不支持预览此文件类型')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取预览链接失败')
  }
}

const fetchDocument = async () => {
  loading.value = true
  try {
    const res = await request.get(`/documents/${props.documentId}`)
    docInfo.value = res.data || {}
  } catch (error) {
    ElMessage.error('获取文档详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDocument()
})
</script>

<style scoped>
.document-detail {
  padding: 10px;
}

.review-progress {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.review-progress h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

.progress-detail {
  margin-top: 10px;
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}
</style>
