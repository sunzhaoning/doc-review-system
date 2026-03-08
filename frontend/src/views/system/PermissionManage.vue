<template>
  <div class="permission-manage">
    <el-card shadow="never">
      <!-- 搜索表单 -->
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="权限名称">
          <el-input v-model="searchForm.permName" placeholder="请输入权限名称" clearable />
        </el-form-item>
        <el-form-item label="权限编码">
          <el-input v-model="searchForm.permCode" placeholder="请输入权限编码" clearable />
        </el-form-item>
        <el-form-item label="资源类型">
          <el-select v-model="searchForm.resourceType" placeholder="请选择类型" clearable>
            <el-option label="菜单" value="menu" />
            <el-option label="按钮" value="button" />
            <el-option label="API" value="api" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增权限
        </el-button>
      </div>

      <!-- 权限列表 -->
      <el-table v-loading="loading" :data="permissionList" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="permName" label="权限名称" width="150" />
        <el-table-column prop="permCode" label="权限编码" width="200" />
        <el-table-column prop="resourceType" label="资源类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.resourceType)" size="small">
              {{ getTypeText(row.resourceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resourceUrl" label="资源URL" min-width="200" />
        <el-table-column prop="method" label="HTTP方法" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.method" size="small">{{ row.method }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" width="200" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
        @size-change="fetchPermissions"
        @current-change="fetchPermissions"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="permFormRef" :model="permForm" :rules="permRules" label-width="100px">
        <el-form-item label="权限名称" prop="permName">
          <el-input v-model="permForm.permName" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="permCode">
          <el-input v-model="permForm.permCode" placeholder="请输入权限编码，如：user:create" />
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="permForm.resourceType" placeholder="请选择资源类型">
            <el-option label="菜单" value="menu" />
            <el-option label="按钮" value="button" />
            <el-option label="API" value="api" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源URL" prop="resourceUrl">
          <el-input v-model="permForm.resourceUrl" placeholder="请输入资源URL，如：/api/users" />
        </el-form-item>
        <el-form-item v-if="permForm.resourceType === 'api'" label="HTTP方法" prop="method">
          <el-select v-model="permForm.method" placeholder="请选择HTTP方法" clearable>
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
            <el-option label="PATCH" value="PATCH" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="permForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { request } from '@/api/request'

interface Permission {
  id: number
  permName: string
  permCode: string
  resourceType: string
  resourceUrl: string
  method: string
  description: string
  createdAt: string
}

const loading = ref(false)
const permissionList = ref<Permission[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)

const searchForm = reactive({
  permName: '',
  permCode: '',
  resourceType: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const permFormRef = ref<FormInstance>()
const isEdit = ref(false)
const editId = ref<number>(0)

const permForm = reactive({
  permName: '',
  permCode: '',
  resourceType: 'api',
  resourceUrl: '',
  method: '',
  description: ''
})

const permRules: FormRules = {
  permName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permCode: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑权限' : '新增权限')

const getTypeTag = (type: string) => {
  const tags: Record<string, string> = { menu: 'primary', button: 'warning', api: 'success' }
  return tags[type] || 'info'
}

const getTypeText = (type: string) => {
  const texts: Record<string, string> = { menu: '菜单', button: '按钮', api: 'API' }
  return texts[type] || type
}

const handleSearch = () => {
  pagination.page = 1
  fetchPermissions()
}

const handleReset = () => {
  searchForm.permName = ''
  searchForm.permCode = ''
  searchForm.resourceType = ''
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  editId.value = 0
  Object.assign(permForm, {
    permName: '',
    permCode: '',
    resourceType: 'api',
    resourceUrl: '',
    method: '',
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (perm: Permission) => {
  isEdit.value = true
  editId.value = perm.id
  Object.assign(permForm, {
    permName: perm.permName,
    permCode: perm.permCode,
    resourceType: perm.resourceType,
    resourceUrl: perm.resourceUrl,
    method: perm.method,
    description: perm.description
  })
  dialogVisible.value = true
}

const handleDelete = async (perm: Permission) => {
  try {
    await ElMessageBox.confirm(`确定要删除权限"${perm.permName}"吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/permissions/${perm.id}`)
    ElMessage.success('删除成功')
    fetchPermissions()
  } catch {
    // 用户取消
  }
}

const handleSubmit = async () => {
  if (!permFormRef.value) return
  
  await permFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await request.put(`/permissions/${editId.value}`, permForm)
        ElMessage.success('更新成功')
      } else {
        await request.post('/permissions', permForm)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchPermissions()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const fetchPermissions = async () => {
  loading.value = true
  try {
    const res = await request.get('/permissions', {
      ...searchForm,
      current: pagination.page,
      size: pagination.pageSize
    })
    permissionList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取权限列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchPermissions()
})
</script>

<style scoped>
.permission-manage {
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
