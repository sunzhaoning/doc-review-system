<template>
  <div class="archive">
    <el-card shadow="never">
      <!-- 搜索表单 -->
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="标题/描述/作者" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="文档类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" clearable>
            <el-option label="技术文档" value="technical" />
            <el-option label="需求文档" value="requirement" />
            <el-option label="设计文档" value="design" />
            <el-option label="测试文档" value="test" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目">
          <el-input v-model="searchForm.project" placeholder="项目名称" clearable />
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
        <el-form-item label="标签">
          <el-select v-model="searchForm.tags" multiple placeholder="选择标签" clearable collapse-tags>
            <el-option label="重要" value="important" />
            <el-option label="紧急" value="urgent" />
            <el-option label="内部" value="internal" />
            <el-option label="公开" value="public" />
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
        <el-button @click="handleBatchExport" :disabled="!selectedRows.length">
          <el-icon><Download /></el-icon>
          批量导出
        </el-button>
        <el-button @click="handleBatchDownload" :disabled="!selectedRows.length">
          <el-icon><Download /></el-icon>
          批量下载
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
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="project" label="项目" width="120" />
        <el-table-column prop="authorName" label="作者" width="100" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="archivedAt" label="归档时间" width="180" />
        <el-table-column prop="tags" label="标签" width="150">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags" :key="tag" size="small" class="tag-item">
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDocument(row)">查看</el-button>
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
  type: string
  project: string
  authorName: string
  version: string
  archivedAt: string
  tags: string[]
}

const loading = ref(false)
const archiveList = ref<Archive[]>([])
const selectedRows = ref<Archive[]>([])

const searchForm = reactive({
  keyword: '',
  type: '',
  project: '',
  dateRange: [] as string[],
  tags: [] as string[]
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const getTypeText = (type: string) => {
  const texts: Record<string, string> = {
    technical: '技术文档',
    requirement: '需求文档',
    design: '设计文档',
    test: '测试文档',
    other: '其他'
  }
  return texts[type] || type
}

const handleSearch = () => {
  pagination.page = 1
  fetchArchives()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.type = ''
  searchForm.project = ''
  searchForm.dateRange = []
  searchForm.tags = []
  handleSearch()
}

const handleSelectionChange = (rows: Archive[]) => {
  selectedRows.value = rows
}

const viewDocument = (doc: Archive) => {
  window.open(`/api/v1/documents/${doc.id}`, '_blank')
}

const downloadDocument = (doc: Archive) => {
  window.open(`/api/v1/documents/${doc.id}/download`, '_blank')
}

const exportReport = (doc: Archive) => {
  window.open(`/api/v1/documents/${doc.id}/report`, '_blank')
}

const handleBatchExport = async () => {
  if (!selectedRows.value.length) return
  
  try {
    const ids = selectedRows.value.map(row => row.id)
    window.open(`/api/v1/archive/export?ids=${ids.join(',')}`, '_blank')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleBatchDownload = async () => {
  if (!selectedRows.value.length) return
  
  try {
    const ids = selectedRows.value.map(row => row.id)
    window.open(`/api/v1/archive/download?ids=${ids.join(',')}`, '_blank')
  } catch (error) {
    ElMessage.error('下载失败')
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
    if (searchForm.type) params.reviewType = searchForm.type
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

.tag-item {
  margin-right: 4px;
  margin-bottom: 2px;
}
</style>
