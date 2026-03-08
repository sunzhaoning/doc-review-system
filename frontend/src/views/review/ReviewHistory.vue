<template>
  <div class="review-history">
    <el-card shadow="never">
      <!-- 评审历史列表 -->
      <el-table v-loading="loading" :data="historyList" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="documentTitle" label="文档标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reviewerName" label="评审人" width="120" />
        <el-table-column prop="decision" label="评审结果" width="100">
          <template #default="{ row }">
            <el-tag :type="getDecisionType(row.decision)" size="small">
              {{ getDecisionText(row.decision) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overallComment" label="总体评价" min-width="200" show-overflow-tooltip />
        <el-table-column prop="submittedAt" label="评审时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.submittedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
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
    <el-dialog v-model="detailVisible" title="评审详情" width="600px" destroy-on-close>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="文档标题">{{ currentReview?.documentTitle }}</el-descriptions-item>
        <el-descriptions-item label="评审人">{{ currentReview?.reviewerName }}</el-descriptions-item>
        <el-descriptions-item label="评审结果">
          <el-tag :type="getDecisionType(currentReview?.decision || '')" size="small">
            {{ getDecisionText(currentReview?.decision || '') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总体评价">{{ currentReview?.overallComment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="优点">{{ currentReview?.pros || '-' }}</el-descriptions-item>
        <el-descriptions-item label="不足">{{ currentReview?.cons || '-' }}</el-descriptions-item>
        <el-descriptions-item label="建议">{{ currentReview?.suggestions || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评审时间">{{ formatDate(currentReview?.submittedAt || null) }}</el-descriptions-item>
      </el-descriptions>
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
  reviewerId: number
  reviewerName: string
  status: string
  decision: string
  overallComment: string | null
  pros: string | null
  cons: string | null
  suggestions: string | null
  submittedAt: string | null
  createdAt: string | null
}

const loading = ref(false)
const historyList = ref<ReviewHistory[]>([])
const detailVisible = ref(false)
const currentReview = ref<ReviewHistory | null>(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const getDecisionType = (decision: string) => {
  const types: Record<string, string> = {
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return types[decision] || 'info'
}

const getDecisionText = (decision: string) => {
  const texts: Record<string, string> = {
    APPROVED: '通过',
    REJECTED: '拒绝'
  }
  return texts[decision] || decision
}

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

const viewDetail = (review: ReviewHistory) => {
  currentReview.value = review
  detailVisible.value = true
}

const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await request.get('/reviews/history', {
      current: pagination.page,
      size: pagination.pageSize
    })
    historyList.value = res.data?.records || []
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

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
