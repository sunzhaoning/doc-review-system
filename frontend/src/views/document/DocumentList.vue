<template>
  <div class="document-list">
    <el-card shadow="never">
      <!-- 搜索表单 -->
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="文档标题">
          <el-input v-model="searchForm.title" placeholder="请输入文档标题" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待评审" value="PENDING" />
            <el-option label="评审中" value="REVIEWING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>
        <el-form-item label="评审类型">
          <el-select v-model="searchForm.reviewType" placeholder="请选择类型" clearable>
            <el-option label="内部评审" value="internal" />
            <el-option label="外部评审" value="external" />
            <el-option label="交叉评审" value="cross" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button type="primary" @click="goToUpload">
          <el-icon><Upload /></el-icon>
          上传文档
        </el-button>
        <el-button @click="handleBatchDelete" :disabled="!selectedRows.length">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>

      <!-- 文档列表 -->
      <el-table
        v-loading="loading"
        :data="documentList"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="文档标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ row.fileType?.toUpperCase() || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewType" label="评审类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ getReviewTypeText(row.reviewType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="submitterName" label="提交人" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDocument(row.id)">查看</el-button>
            <el-button type="primary" link @click="editDocument(row)" 
                       v-if="row.status === 'DRAFT'">编辑</el-button>
            <el-button type="primary" link @click="submitReview(row.id)"
                       v-if="row.status === 'DRAFT'">提交评审</el-button>
            <el-button type="danger" link @click="deleteDocument(row)"
                       v-if="row.status === 'DRAFT'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="fetchDocuments"
        @current-change="fetchDocuments"
      />
    </el-card>

    <!-- 文档详情弹窗 -->
    <el-dialog v-model="detailVisible" title="文档详情" width="800px" destroy-on-close>
      <document-detail v-if="detailVisible" :document-id="currentDocId" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/api/request'

const router = useRouter()
const route = useRoute()

interface Document {
  id: number
  title: string
  fileType: string
  reviewType: string
  status: string
  version: string
  submitterName: string
  createdAt: string
  updatedAt: string
  archived: boolean
}

const loading = ref(false)
const documentList = ref<Document[]>([])
const selectedRows = ref<Document[]>([])
const detailVisible = ref(false)
const currentDocId = ref<number>(0)

const searchForm = reactive({
  title: '',
  status: '',
  reviewType: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
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
    ARCHIVED: '已归档'
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

const handleSearch = () => {
  pagination.page = 1
  fetchDocuments()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.status = ''
  searchForm.reviewType = ''
  handleSearch()
}

const handleSelectionChange = (rows: Document[]) => {
  selectedRows.value = rows
}

const goToUpload = () => {
  router.push('/document/upload')
}

const viewDocument = (id: number) => {
  currentDocId.value = id
  detailVisible.value = true
}

const editDocument = (doc: Document) => {
  // 跳转到编辑页面或打开编辑弹窗
  ElMessage.info('编辑功能开发中')
}

const submitReview = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要提交评审吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await request.post(`/documents/${id}/submit`, { reviewerIds: [] })
    ElMessage.success('已提交评审')
    fetchDocuments()
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  }
}

const deleteDocument = async (doc: Document) => {
  try {
    await ElMessageBox.confirm(`确定要删除文档"${doc.title}"吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/documents/${doc.id}`)
    ElMessage.success('删除成功')
    fetchDocuments()
  } catch (error) {
    // 用户取消或请求失败
  }
}

const handleBatchDelete = async () => {
  if (!selectedRows.value.length) return
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个文档吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const ids = selectedRows.value.map(row => row.id)
    await request.delete('/documents/batch', { ids })
    ElMessage.success('批量删除成功')
    fetchDocuments()
  } catch (error) {
    // 用户取消或请求失败
  }
}

const fetchDocuments = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      current: pagination.page,
      size: pagination.pageSize
    }
    if (searchForm.title) params.title = searchForm.title
    if (searchForm.status) params.status = searchForm.status
    if (searchForm.reviewType) params.reviewType = searchForm.reviewType
    
    const res = await request.get('/documents/my', { params })
    documentList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取文档列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 检查URL参数
  const docId = route.query.id as string
  if (docId) {
    viewDocument(parseInt(docId))
  }
  
  fetchDocuments()
})
</script>

<style scoped>
.document-list {
  padding: 20px;
}

.search-form {
  margin-bottom: 16px;
}

.action-bar {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
