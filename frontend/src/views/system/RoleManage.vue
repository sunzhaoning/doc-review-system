<template>
  <div class="role-manage">
    <el-card shadow="never">
      <!-- 搜索表单 -->
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
          新增角色
        </el-button>
      </div>

      <!-- 角色列表 -->
      <el-table v-loading="loading" :data="roleList" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="dataScope" label="数据权限" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ getDataScopeText(row.dataScope) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleAssignMenu(row)">分配菜单</el-button>
            <el-button type="primary" link @click="handleAssignPermission(row)">分配权限</el-button>
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
        @size-change="fetchRoles"
        @current-change="fetchRoles"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="数据权限" prop="dataScope">
          <el-select v-model="roleForm.dataScope" placeholder="请选择数据权限">
            <el-option label="全部数据" :value="1" />
            <el-option label="自定义数据" :value="2" />
            <el-option label="本部门及下级" :value="3" />
            <el-option label="本部门数据" :value="4" />
            <el-option label="仅本人数据" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配菜单弹窗 -->
    <el-dialog v-model="menuDialogVisible" title="分配菜单" width="500px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="角色名称">
          <el-input :value="currentRole?.roleName" disabled />
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-tree
            ref="menuTreeRef"
            :data="menuTree"
            :props="{ label: 'menuName', children: 'children' }"
            show-checkbox
            node-key="id"
            default-expand-all
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSaveMenus">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="600px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="角色名称">
          <el-input :value="currentRole?.roleName" disabled />
        </el-form-item>
        <el-form-item label="权限">
          <el-checkbox-group v-model="selectedPermIds">
            <el-checkbox v-for="perm in allPermissions" :key="perm.id" :label="perm.id">
              {{ perm.permName }} ({{ perm.permCode }})
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSavePermissions">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { request } from '@/api/request'

interface Role {
  id: number
  roleName: string
  roleCode: string
  description: string
  dataScope: number
  status: number
  createdAt: string
}

interface Menu {
  id: number
  menuName: string
  children?: Menu[]
}

interface Permission {
  id: number
  permName: string
  permCode: string
}

const loading = ref(false)
const roleList = ref<Role[]>([])
const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const permDialogVisible = ref(false)
const submitting = ref(false)
const currentRole = ref<Role | null>(null)
const menuTree = ref<Menu[]>([])
const allPermissions = ref<Permission[]>([])
const selectedPermIds = ref<number[]>([])

const searchForm = reactive({
  roleName: '',
  status: undefined as number | undefined
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const roleFormRef = ref<FormInstance>()
const menuTreeRef = ref()
const isEdit = ref(false)
const editId = ref<number>(0)

const roleForm = reactive({
  roleName: '',
  roleCode: '',
  description: '',
  dataScope: 1,
  status: 1
})

const roleRules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  dataScope: [{ required: true, message: '请选择数据权限', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')

const getDataScopeText = (scope: number) => {
  const texts: Record<number, string> = {
    1: '全部数据',
    2: '自定义',
    3: '本部门及下级',
    4: '本部门',
    5: '仅本人'
  }
  return texts[scope] || '未知'
}

const handleSearch = () => {
  pagination.page = 1
  fetchRoles()
}

const handleReset = () => {
  searchForm.roleName = ''
  searchForm.status = undefined
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  editId.value = 0
  Object.assign(roleForm, {
    roleName: '',
    roleCode: '',
    description: '',
    dataScope: 1,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (role: Role) => {
  isEdit.value = true
  editId.value = role.id
  Object.assign(roleForm, {
    roleName: role.roleName,
    roleCode: role.roleCode,
    description: role.description,
    dataScope: role.dataScope,
    status: role.status
  })
  dialogVisible.value = true
}

const handleDelete = async (role: Role) => {
  try {
    await ElMessageBox.confirm(`确定要删除角色"${role.roleName}"吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/roles/${role.id}`)
    ElMessage.success('删除成功')
    fetchRoles()
  } catch {
    // 用户取消
  }
}

const handleStatusChange = async (role: Role) => {
  try {
    await request.put(`/roles/${role.id}/status`, { status: role.status })
    ElMessage.success('状态更新成功')
  } catch {
    role.status = role.status === 1 ? 0 : 1
  }
}

const handleSubmit = async () => {
  if (!roleFormRef.value) return
  
  await roleFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await request.put(`/roles/${editId.value}`, roleForm)
        ElMessage.success('更新成功')
      } else {
        await request.post('/roles', roleForm)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchRoles()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleAssignMenu = async (role: Role) => {
  currentRole.value = role
  menuDialogVisible.value = true
  
  // 获取菜单树
  try {
    const res = await request.get('/menus/tree')
    menuTree.value = res.data || []
  } catch {
    ElMessage.error('获取菜单树失败')
    return
  }
  
  // 获取角色当前菜单（需要等待树组件渲染完成）
  try {
    const res = await request.get(`/roles/${role.id}/menus`)
    // 使用 nextTick 确保树组件已渲染
    await new Promise(resolve => setTimeout(resolve, 100))
    if (menuTreeRef.value) {
      menuTreeRef.value.setCheckedKeys(res.data || [])
    }
  } catch {
    // ignore
  }
}

const handleSaveMenus = async () => {
  if (!currentRole.value || !menuTreeRef.value) return
  
  submitting.value = true
  try {
    const menuIds = menuTreeRef.value.getCheckedKeys()
    await request.put(`/roles/${currentRole.value.id}/menus`, menuIds)
    ElMessage.success('菜单分配成功')
    menuDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleAssignPermission = async (role: Role) => {
  currentRole.value = role
  
  // 获取所有权限
  try {
    const res = await request.get('/permissions', { size: 1000 })
    allPermissions.value = res.data?.records || []
  } catch {
    ElMessage.error('获取权限列表失败')
    return
  }
  
  // 获取角色当前权限
  try {
    const res = await request.get(`/roles/${role.id}/permissions`)
    selectedPermIds.value = res.data || []
  } catch {
    selectedPermIds.value = []
  }
  
  permDialogVisible.value = true
}

const handleSavePermissions = async () => {
  if (!currentRole.value) return
  
  submitting.value = true
  try {
    await request.put(`/roles/${currentRole.value.id}/permissions`, selectedPermIds.value)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const fetchRoles = async () => {
  loading.value = true
  try {
    const res = await request.get('/roles', {
      ...searchForm,
      current: pagination.page,
      size: pagination.pageSize
    })
    roleList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRoles()
})
</script>

<style scoped>
.role-manage {
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
