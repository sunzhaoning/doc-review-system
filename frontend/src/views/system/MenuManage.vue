<template>
  <div class="menu-manage">
    <el-card shadow="never">
      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button type="primary" @click="handleAdd()">
          <el-icon><Plus /></el-icon>
          新增菜单
        </el-button>
        <el-button @click="toggleExpandAll">{{ expandAll ? '收起全部' : '展开全部' }}</el-button>
      </div>

      <!-- 菜单树 -->
      <el-table
        v-loading="loading"
        :data="menuList"
        row-key="id"
        :tree-props="{ children: 'children' }"
        :default-expand-all="expandAll"
        stripe
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="200" />
        <el-table-column prop="menuType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.menuType)" size="small">
              {{ getTypeText(row.menuType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由地址" width="150" />
        <el-table-column prop="component" label="组件路径" width="200" />
        <el-table-column prop="perms" label="权限标识" width="150" />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleAdd(row)">新增</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" destroy-on-close>
      <el-form ref="menuFormRef" :model="menuForm" :rules="menuRules" label-width="100px">
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="menuForm.parentId"
            :data="menuTreeData"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            placeholder="请选择上级菜单"
            check-strictly
            clearable
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="menuForm.menuType">
            <el-radio :label="1">目录</el-radio>
            <el-radio :label="2">菜单</el-radio>
            <el-radio :label="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="menuForm.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="菜单编码" prop="menuCode">
          <el-input v-model="menuForm.menuCode" placeholder="请输入菜单编码" />
        </el-form-item>
        <el-form-item v-if="menuForm.menuType !== 3" label="路由地址" prop="path">
          <el-input v-model="menuForm.path" placeholder="请输入路由地址" />
        </el-form-item>
        <el-form-item v-if="menuForm.menuType === 2" label="组件路径" prop="component">
          <el-input v-model="menuForm.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="权限标识" prop="perms">
          <el-input v-model="menuForm.perms" placeholder="请输入权限标识" />
        </el-form-item>
        <el-form-item v-if="menuForm.menuType !== 3" label="图标" prop="icon">
          <el-input v-model="menuForm.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="menuForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item v-if="menuForm.menuType !== 3" label="是否可见" prop="visible">
          <el-radio-group v-model="menuForm.visible">
            <el-radio :label="1">显示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="menuForm.status">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { request } from '@/api/request'

interface Menu {
  id: number
  parentId: number
  menuName: string
  menuCode: string
  menuType: number
  path: string
  component: string
  perms: string
  icon: string
  sort: number
  visible: number
  status: number
  children?: Menu[]
}

const loading = ref(false)
const menuList = ref<Menu[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const expandAll = ref(true)

const menuFormRef = ref<FormInstance>()
const isEdit = ref(false)
const editId = ref<number>(0)

const menuForm = reactive({
  parentId: 0,
  menuName: '',
  menuCode: '',
  menuType: 1,
  path: '',
  component: '',
  perms: '',
  icon: '',
  sort: 0,
  visible: 1,
  status: 1
})

const menuRules: FormRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuCode: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑菜单' : '新增菜单')
const menuTreeData = computed(() => {
  const root: Menu = { id: 0, parentId: 0, menuName: '根目录', menuCode: '', menuType: 1, path: '', component: '', perms: '', icon: '', sort: 0, visible: 1, status: 1, children: menuList.value }
  return [root]
})

const getTypeTag = (type: number) => {
  const tags: Record<number, string> = { 1: 'primary', 2: 'success', 3: 'warning' }
  return tags[type] || 'info'
}

const getTypeText = (type: number) => {
  const texts: Record<number, string> = { 1: '目录', 2: '菜单', 3: '按钮' }
  return texts[type] || '未知'
}

const toggleExpandAll = () => {
  expandAll.value = !expandAll.value
}

const handleAdd = (parent?: Menu) => {
  isEdit.value = false
  editId.value = 0
  Object.assign(menuForm, {
    parentId: parent?.id || 0,
    menuName: '',
    menuCode: '',
    menuType: parent ? (parent.menuType === 1 ? 2 : 3) : 1,
    path: '',
    component: '',
    perms: '',
    icon: '',
    sort: 0,
    visible: 1,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (menu: Menu) => {
  isEdit.value = true
  editId.value = menu.id
  Object.assign(menuForm, {
    parentId: menu.parentId,
    menuName: menu.menuName,
    menuCode: menu.menuCode,
    menuType: menu.menuType,
    path: menu.path,
    component: menu.component,
    perms: menu.perms,
    icon: menu.icon,
    sort: menu.sort,
    visible: menu.visible,
    status: menu.status
  })
  dialogVisible.value = true
}

const handleDelete = async (menu: Menu) => {
  if (menu.children && menu.children.length > 0) {
    ElMessage.warning('请先删除子菜单')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除菜单"${menu.menuName}"吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/menus/${menu.id}`)
    ElMessage.success('删除成功')
    fetchMenus()
  } catch {
    // 用户取消
  }
}

const handleSubmit = async () => {
  if (!menuFormRef.value) return
  
  await menuFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await request.put(`/menus/${editId.value}`, menuForm)
        ElMessage.success('更新成功')
      } else {
        await request.post('/menus', menuForm)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchMenus()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const fetchMenus = async () => {
  loading.value = true
  try {
    const res = await request.get('/menus/tree')
    menuList.value = res.data || []
  } catch (error) {
    ElMessage.error('获取菜单列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchMenus()
})
</script>

<style scoped>
.menu-manage {
  padding: 20px;
}

.action-bar {
  margin-bottom: 16px;
}
</style>
