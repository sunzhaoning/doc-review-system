<template>
  <div class="archive">
    <el-card shadow="never">
      <!-- 搜索表单 -->
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="标题/描述" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="评审类型">
          <el-select v-model="searchForm.reviewType" placeholder="全部类型" clearable>
            <el-option label="内部评审" value="internal" />
            <el-option label="外部评审" value="external" />
            <el-option label="交叉评审" value="cross" />
          </el-select>
        </el-form-item>
        <el-form-item label="归档时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
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
        <el-button @click="handleBatchArchive" :disabled="!selectedRows.length">
          <el-icon><FolderOpened /></el-icon>
          批量归档
        </el-button>
      </div>

      <!-- 归档列表 -->
      <el-table
        v-loading="loading"
        :data="archiveList"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="title" label="文档标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.fileType || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewType" label="评审类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ getReviewTypeText(row.reviewType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitterName" label="提交人" width="100" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDocument(row)">预览</el-button>
            <el-button type="primary" link @click="downloadDocument(row)">下载</el-button>
            <el-button type="primary" link @click="exportReport(row)">导出报告</el-button>
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
        @size-change="fetchArchives"
        @current-change="fetchArchives"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { request } from '@/api/request'

interface Archive {
  id: number
  title: string
  fileName: string
  fileType: string
  reviewType: string
  submitterName: string
  version: string
  archived: boolean
  createdAt: string
  updatedAt: string
}

const loading = ref(false)
const archiveList = ref<Archive[]>([])
const selectedRows = ref<Archive[]>([])

const searchForm = reactive({
  keyword: '',
  reviewType: '',
  dateRange: [] as string[]
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

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
  fetchArchives()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.reviewType = ''
  searchForm.dateRange = []
  handleSearch()
}

const handleSelectionChange = (rows: Archive[]) => {
  selectedRows.value = rows
}

const viewDocument = (doc: Archive) => {
  // 获取预览URL
  request.get(`/documents/${doc.id}/preview`).then((res: any) => {
    if (res.data) {
      window.open(res.data, '_blank')
    }
  }).catch(() => {
    ElMessage.error('获取预览链接失败')
  })
}

const downloadDocument = async (doc: Archive) => {
  try {
    const res = await request.get(`/documents/${doc.id}/download`, {
      responseType: 'blob'
    })
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = doc.title || 'document'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const exportReport = async (doc: Archive) => {
  try {
    const res = await request.get(`/archives/${doc.id}/export`, {
      responseType: 'blob'
    })
    const blob = new Blob([res], { type: 'text/markdown' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${doc.title}_评审报告.md`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleBatchArchive = async () => {
  if (!selectedRows.value.length) return
  
  try {
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/archives/batch', { ids })
    ElMessage.success('批量归档成功')
    fetchArchives()
  } catch (error) {
    ElMessage.error('批量归档失败')
  }
}

const fetchArchives = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      current: pagination.page,
      size: pagination.pageSize
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.reviewType) params.reviewType = searchForm.reviewType
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const res = await request.get('/archives', { params })
    archiveList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取归档列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchArchives()
})
</script>

<style scoped>
.archive {
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
