<template>
  <div class="document-upload">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>上传文档</span>
        </div>
      </template>

      <el-form
        ref="uploadFormRef"
        :model="uploadForm"
        :rules="uploadRules"
        label-width="100px"
        class="upload-form"
      >
        <el-form-item label="文档标题" prop="title">
          <el-input v-model="uploadForm.title" placeholder="请输入文档标题" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="评审类型" prop="reviewType">
          <el-select v-model="uploadForm.reviewType" placeholder="请选择评审类型">
            <el-option label="内部评审" value="internal" />
            <el-option label="外部评审" value="external" />
            <el-option label="交叉评审" value="cross" />
          </el-select>
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="uploadForm.description" type="textarea" :rows="3" 
                    placeholder="请输入文档描述" maxlength="500" show-word-limit />
        </el-form-item>

        <el-form-item label="文档文件">
          <el-upload
            ref="uploadRef"
            class="upload-area"
            drag
            :auto-upload="false"
            :limit="1"
            v-model:file-list="fileList"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
            accept=".pdf,.doc,.docx,.md,.txt"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF、DOC、DOCX、MD、TXT 格式，单个文件不超过 50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="评审人" prop="reviewerIds">
          <el-select v-model="uploadForm.reviewerIds" multiple placeholder="请选择评审人" 
                     filterable :loading="userLoading" @focus="searchUsers('')">
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.realName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            v-model="uploadForm.deadline"
            type="datetime"
            placeholder="选择截止日期"
            :disabled-date="disabledDate"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="uploading" @click="handleSubmit">
            <el-icon><Upload /></el-icon>
            提交上传
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules, type UploadFile, type UploadInstance, type UploadProps } from 'element-plus'
import { request } from '@/api/request'

const router = useRouter()

interface User {
  id: number
  realName: string
  username: string
}

const uploadFormRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const uploading = ref(false)
const fileList = ref<UploadFile[]>([])
const userList = ref<User[]>([])
const userLoading = ref(false)

const uploadForm = reactive({
  title: '',
  reviewType: '',
  description: '',
  reviewerIds: [] as number[],
  deadline: ''
})

const uploadRules: FormRules = {
  title: [
    { required: true, message: '请输入文档标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  reviewType: [
    { required: true, message: '请选择评审类型', trigger: 'change' }
  ]
}

const handleFileChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  // 只保留最新的文件
  fileList.value = uploadFiles.slice(-1)
  
  // 验证文件
  if (uploadFile.raw) {
    const allowedTypes = ['application/pdf', 'application/msword', 
                          'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                          'text/markdown', 'text/plain']
    const isAllowed = allowedTypes.includes(uploadFile.raw.type) || 
                      /\.(pdf|doc|docx|md|txt)$/i.test(uploadFile.name)
    
    if (!isAllowed) {
      ElMessage.error('只支持 PDF、DOC、DOCX、MD、TXT 格式的文件')
      fileList.value = []
      return
    }
    
    const isLt50M = (uploadFile.raw.size / 1024 / 1024) < 50
    if (!isLt50M) {
      ElMessage.error('文件大小不能超过 50MB')
      fileList.value = []
      return
    }
  }
  
  // 自动填充标题
  if (!uploadForm.title && uploadFile.name) {
    const nameWithoutExt = uploadFile.name.replace(/\.[^/.]+$/, '')
    uploadForm.title = nameWithoutExt
  }
}

const handleExceed: UploadProps['onExceed'] = (files) => {
  ElMessage.warning('最多只能上传1个文件')
  // 替换为新文件
  fileList.value = []
  if (files.length > 0) {
    const file = files[0]
    fileList.value = [{
      name: file.name,
      size: file.size,
      raw: file,
      status: 'ready'
    } as UploadFile]
  }
}

const disabledDate = (date: Date) => {
  return date.getTime() < Date.now() - 86400000
}

const searchUsers = async (query: string) => {
  userLoading.value = true
  try {
    const res = await request.get('/users/search', { 
      keyword: query || '',
      limit: 20 
    })
    userList.value = res.data || []
  } catch (error) {
    console.error('Failed to search users:', error)
  } finally {
    userLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!uploadFormRef.value) return
  
  await uploadFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (fileList.value.length === 0) {
      ElMessage.error('请选择要上传的文件')
      return
    }
    
    const file = fileList.value[0]
    if (!file.raw) {
      ElMessage.error('文件数据异常，请重新选择')
      return
    }
    
    uploading.value = true
    
    try {
      const formData = new FormData()
      formData.append('file', file.raw)
      formData.append('title', uploadForm.title)
      formData.append('reviewType', uploadForm.reviewType)
      if (uploadForm.description) {
        formData.append('description', uploadForm.description)
      }
      if (uploadForm.reviewerIds.length > 0) {
        formData.append('reviewerIds', JSON.stringify(uploadForm.reviewerIds))
      }
      if (uploadForm.deadline) {
        formData.append('deadline', uploadForm.deadline)
      }
      
      await request.post('/documents', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      
      ElMessage.success('文档上传成功')
      router.push('/document/list')
    } catch (error: any) {
      ElMessage.error(error.message || '上传失败')
    } finally {
      uploading.value = false
    }
  })
}

const handleReset = () => {
  uploadFormRef.value?.resetFields()
  fileList.value = []
}

const handleCancel = () => {
  router.back()
}

// 加载用户列表
onMounted(() => {
  searchUsers('')
})
</script>

<style scoped>
.document-upload {
  padding: 20px;
}

.upload-form {
  max-width: 800px;
}

.upload-area {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
}

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}
</style>
