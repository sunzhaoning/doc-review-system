<template>
  <div class="review-pending">
    <el-card shadow="never">
      <!-- 搜索表单 -->
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="文档标题">
          <el-input v-model="searchForm.title" placeholder="请输入文档标题" clearable />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="searchForm.priority" placeholder="请选择优先级" clearable>
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
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

      <!-- 待评审列表 -->
      <el-table v-loading="loading" :data="reviewList" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="documentTitle" label="文档标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="documentType" label="文档类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.documentType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitterName" label="提交人" width="120" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="提交时间" width="180" />
        <el-table-column prop="dueDate" label="截止日期" width="150">
          <template #default="{ row }">
            <span :class="{ 'overdue': isOverdue(row.dueDate) }">
              {{ row.dueDate || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" @click="startReview(row)">开始评审</el-button>
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
    <el-dialog v-model="reviewVisible" title="文档评审" width="900px" destroy-on-close>
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="100px">
        <el-form-item label="文档信息">
          <div class="doc-info">
            <p><strong>标题：</strong>{{ currentReview?.documentTitle }}</p>
            <p><strong>提交人：</strong>{{ currentReview?.submitterName }}</p>
            <p><strong>提交时间：</strong>{{ currentReview?.submittedAt }}</p>
          </div>
        </el-form-item>

        <el-form-item label="文档预览">
          <el-button type="primary" link @click="previewDocument">
            <el-icon><View /></el-icon>
            查看原文
          </el-button>
        </el-form-item>

        <el-form-item label="评审意见" prop="comment">
          <el-input v-model="reviewForm.comment" type="textarea" :rows="4" 
                    placeholder="请输入评审意见" maxlength="2000" show-word-limit />
        </el-form-item>

        <el-form-item label="评审结果" prop="result">
          <el-radio-group v-model="reviewForm.result">
            <el-radio label="approved">通过</el-radio>
            <el-radio label="rejected">拒绝</el-radio>
            <el-radio label="revision">需修改</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="reviewForm.result === 'revision'" label="修改建议" prop="suggestions">
          <el-input v-model="reviewForm.suggestions" type="textarea" :rows="3" 
                    placeholder="请输入修改建议" />
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
import { useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { request } from '@/api/request'

const route = useRoute()

interface Review {
  id: number
  documentId: number
  documentTitle: string
  documentType: string
  submitterName: string
  priority: string
  submittedAt: string
  dueDate: string
  progress: number
}

const loading = ref(false)
const reviewList = ref<Review[]>([])
const reviewVisible = ref(false)
const currentReview = ref<Review | null>(null)
const submitting = ref(false)

const searchForm = reactive({
  title: '',
  priority: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const reviewFormRef = ref<FormInstance>()
const reviewForm = reactive({
  comment: '',
  result: '',
  suggestions: ''
})

const reviewRules: FormRules = {
  comment: [
    { required: true, message: '请输入评审意见', trigger: 'blur' }
  ],
  result: [
    { required: true, message: '请选择评审结果', trigger: 'change' }
  ]
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

const isOverdue = (dueDate: string) => {
  if (!dueDate) return false
  return new Date(dueDate) < new Date()
}

const handleSearch = () => {
  pagination.page = 1
  fetchReviews()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.priority = ''
  handleSearch()
}

const startReview = (review: Review) => {
  currentReview.value = review
  reviewForm.comment = ''
  reviewForm.result = ''
  reviewForm.suggestions = ''
  reviewVisible.value = true
}

const previewDocument = () => {
  if (currentReview.value) {
    window.open(`/api/v1/documents/${currentReview.value.documentId}/preview`, '_blank')
  }
}

const submitReview = async () => {
  if (!reviewFormRef.value || !currentReview.value) return
  
  await reviewFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await request.post('/reviews', {
        assignmentId: currentReview.value?.id,
        documentId: currentReview.value?.documentId,
        comment: reviewForm.comment,
        result: reviewForm.result,
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
      ...searchForm,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    reviewList.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取待评审列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 检查URL参数
  const reviewId = route.query.id as string
  if (reviewId) {
    // 如果有指定ID，打开对应的评审
    const review = reviewList.value.find(r => r.id === parseInt(reviewId))
    if (review) {
      startReview(review)
    }
  }
  
  fetchReviews()
})
</script>

<style scoped>
.review-pending {
  padding: 20px;
}

.search-form {
  margin-bottom: 16px;
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

.overdue {
  color: #f56c6c;
}
</style>
