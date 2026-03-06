<template>
  <div class="review-history">
    <el-card shadow="never">
      <!-- 搜索表单 -->
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="文档标题">
          <el-input v-model="searchForm.title" placeholder="请输入文档标题" clearable />
        </el-form-item>
        <el-form-item label="评审结果">
          <el-select v-model="searchForm.result" placeholder="请选择结果" clearable>
            <el-option label="通过" value="approved" />
            <el-option label="拒绝" value="rejected" />
            <el-option label="需修改" value="revision" />
          </el-select>
        </el-form-item>
        <el-form-item label="评审时间">
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

      <!-- 评审历史列表 -->
      <el-table v-loading="loading" :data="historyList" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="documentTitle" label="文档标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="documentType" label="文档类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.documentType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitterName" label="提交人" width="120" />
        <el-table-column prop="result" label="评审结果" width="100">
          <template #default="{ row }">
            <el-tag :type="getResultType(row.result)" size="small">
              {{ getResultText(row.result) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewedAt" label="评审时间" width="180" />
        <el-table-column prop="comment" label="评审意见" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @size-change="fetchHistory"
        @current-change="fetchHistory"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="评审详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentReview">
        <el-descriptions-item label="文档标题" :span="2">{{ currentReview.documentTitle }}</el-descriptions-item>
        <el-descriptions-item label="文档类型">{{ currentReview.documentType }}</el-descriptions-item>
        <el-descriptions-item label="提交人">{{ currentReview.submitterName }}</el-descriptions-item>
        <el-descriptions-item label="评审结果">
          <el-tag :type="getResultType(currentReview.result)">
            {{ getResultText(currentReview.result) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="评审时间">{{ currentReview.reviewedAt }}</el-descriptions-item>
        <el-descriptions-item label="评审意见" :span="2">{{ currentReview.comment }}</el-descriptions-item>
        <el-descriptions-item label="修改建议" :span="2" v-if="currentReview.suggestions">
          {{ currentReview.suggestions }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="viewDocument">查看文档</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { request } from '@/api/request'

interface ReviewHistory {
  id: number
  documentId: number
  documentTitle: string
  documentType: string
  submitterName: string
  result: string
  reviewedAt: string
  comment: string
  suggestions: string
}

const loading = ref(false)
const historyList = ref<ReviewHistory[]>([])
const detailVisible = ref(false)
const currentReview = ref<ReviewHistory | null>(null)

const searchForm = reactive({
  title: '',
  result: '',
  dateRange: [] as string[]
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const getResultType = (result: string) => {
  const types: Record<string, string> = {
    approved: 'success',
    rejected: 'danger',
    revision: 'warning'
  }
  return types[result] || 'info'
}

const getResultText = (result: string) => {
  const texts: Record<string, string> = {
    approved: '通过',
    rejected: '拒绝',
    revision: '需修改'
  }
  return texts[result] || result
}

const handleSearch = () => {
  pagination.page = 1
  fetchHistory()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.result = ''
  searchForm.dateRange = []
  handleSearch()
}

const viewDetail = (review: ReviewHistory) => {
  currentReview.value = review
  detailVisible.value = true
}

const viewDocument = () => {
  if (currentReview.value) {
    window.open(`/api/v1/documents/${currentReview.value.documentId}`, '_blank')
  }
}

const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await request.get('/reviews/history', {
      ...searchForm,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    historyList.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取评审历史失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.review-history {
  padding: 20px;
}

.search-form {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
