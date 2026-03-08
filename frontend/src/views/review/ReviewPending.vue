<template>
  <div class="review-pending">
    <el-card shadow="never">
      <!-- 待评审列表 -->
      <el-table v-loading="loading" :data="reviewList" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="documentTitle" label="文档标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reviewerName" label="评审人" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="decision" label="决定" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.decision" :type="getDecisionType(row.decision)" size="small">
              {{ getDecisionText(row.decision) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.submittedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDocument(row)" v-if="row.status === 'PENDING'">
              开始评审
            </el-button>
            <el-button type="info" link @click="viewDocument(row)" v-else>
              查看详情
            </el-button>
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
        @size-change="fetchReviews"
        @current-change="fetchReviews"
      />
    </el-card>

    <!-- 评审弹窗 -->
    <el-dialog v-model="reviewVisible" title="文档评审" width="700px" destroy-on-close>
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="100px">
        <el-form-item label="文档信息">
          <div class="doc-info">
            <p><strong>标题：</strong>{{ currentReview?.documentTitle }}</p>
            <p><strong>评审人：</strong>{{ currentReview?.reviewerName }}</p>
            <p><strong>状态：</strong>{{ getStatusText(currentReview?.status || '') }}</p>
          </div>
        </el-form-item>

        <el-form-item label="文档预览">
          <el-button type="primary" link @click="previewDocument">
            <el-icon><View /></el-icon>
            查看原文
          </el-button>
        </el-form-item>

        <el-form-item label="评审决定" prop="decision">
          <el-radio-group v-model="reviewForm.decision">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="总体评价" prop="overallComment">
          <el-input v-model="reviewForm.overallComment" type="textarea" :rows="3" 
                    placeholder="请输入总体评价" maxlength="2000" show-word-limit />
        </el-form-item>

        <el-form-item label="优点">
          <el-input v-model="reviewForm.pros" type="textarea" :rows="2" 
                    placeholder="文档的优点" />
        </el-form-item>

        <el-form-item label="不足">
          <el-input v-model="reviewForm.cons" type="textarea" :rows="2" 
                    placeholder="需要改进的地方" />
        </el-form-item>

        <el-form-item label="建议">
          <el-input v-model="reviewForm.suggestions" type="textarea" :rows="2" 
                    placeholder="修改建议" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReview">
          提交评审
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { request } from '@/api/request'

const route = useRoute()
const router = useRouter()

interface Review {
  id: number
  documentId: number
  documentTitle: string
  reviewerId: number
  reviewerName: string
  status: string
  decision: string | null
  overallComment: string | null
  pros: string | null
  cons: string | null
  suggestions: string | null
  submittedAt: string | null
  createdAt: string | null
}

const loading = ref(false)
const reviewList = ref<Review[]>([])
const reviewVisible = ref(false)
const currentReview = ref<Review | null>(null)
const submitting = ref(false)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const reviewFormRef = ref<FormInstance>()
const reviewForm = reactive({
  decision: '',
  overallComment: '',
  pros: '',
  cons: '',
  suggestions: ''
})

const reviewRules: FormRules = {
  decision: [
    { required: true, message: '请选择评审决定', trigger: 'change' }
  ],
  overallComment: [
    { required: true, message: '请输入总体评价', trigger: 'blur' }
  ]
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    PENDING: 'warning',
    IN_PROGRESS: 'primary',
    SUBMITTED: 'success',
    COMPLETED: 'success'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    PENDING: '待评审',
    IN_PROGRESS: '评审中',
    SUBMITTED: '已提交',
    COMPLETED: '已完成'
  }
  return texts[status] || status
}

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

const viewDocument = (review: Review) => {
  currentReview.value = review
  reviewForm.decision = review.decision || ''
  reviewForm.overallComment = review.overallComment || ''
  reviewForm.pros = review.pros || ''
  reviewForm.cons = review.cons || ''
  reviewForm.suggestions = review.suggestions || ''
  reviewVisible.value = true
}

const previewDocument = async () => {
  if (!currentReview.value) return
  try {
    const res = await request.get(`/documents/${currentReview.value.documentId}/preview`)
    if (res.data) {
      window.open(res.data, '_blank')
    }
  } catch (error) {
    ElMessage.error('获取预览链接失败')
  }
}

const submitReview = async () => {
  if (!reviewFormRef.value || !currentReview.value) return
  
  await reviewFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await request.post(`/reviews/submit/${currentReview.value.documentId}`, {
        decision: reviewForm.decision,
        overallComment: reviewForm.overallComment,
        pros: reviewForm.pros,
        cons: reviewForm.cons,
        suggestions: reviewForm.suggestions
      })
      
      ElMessage.success('评审提交成功')
      reviewVisible.value = false
      fetchReviews()
    } catch (error: any) {
      ElMessage.error(error.message || '提交失败')
    } finally {
      submitting.value = false
    }
  })
}

const fetchReviews = async () => {
  loading.value = true
  try {
    const res = await request.get('/reviews/pending', {
      current: pagination.page,
      size: pagination.pageSize
    })
    reviewList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取待评审列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.review-pending {
  padding: 20px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.doc-info {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.doc-info p {
  margin: 8px 0;
}
</style>
