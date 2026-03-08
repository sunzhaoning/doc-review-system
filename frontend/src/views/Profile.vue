<template>
  <div class="profile">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <el-form :model="userForm" label-width="100px" class="form-container">
            <el-form-item label="用户名">
              <el-input v-model="userForm.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="userForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag v-for="role in userInfo.roles" :key="role" class="role-tag">
                {{ getRoleName(role) }}
              </el-tag>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSaveInfo">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" class="form-container">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="changingPwd" @click="handleChangePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/authStore'
import { request } from '@/api/request'

const authStore = useAuthStore()
const activeTab = ref('info')
const saving = ref(false)
const changingPwd = ref(false)
const pwdFormRef = ref<FormInstance>()

const userInfo = computed(() => authStore.userInfo)

const userForm = reactive({
  username: '',
  realName: '',
  email: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const roleNames: Record<string, string> = {
  admin: '超级管理员',
  manager: '管理员',
  submitter: '提交者',
  reviewer: '评审者',
  observer: '观察者'
}

const getRoleName = (code: string) => {
  return roleNames[code] || code
}

const handleSaveInfo = async () => {
  saving.value = true
  try {
    await request.put(`/users/${userInfo.value?.id}`, {
      realName: userForm.realName,
      email: userForm.email,
      phone: userForm.phone
    })
    ElMessage.success('保存成功')
    // 刷新用户信息
    await authStore.getUserInfo()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleChangePassword = async () => {
  if (!pwdFormRef.value) return
  
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    changingPwd.value = true
    try {
      await request.put(`/users/${userInfo.value?.id}/password`, {
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      // 清空表单
      pwdFormRef.value?.resetFields()
      // 登出
      setTimeout(() => {
        authStore.logout()
        window.location.href = '/login'
      }, 1500)
    } catch (error: any) {
      ElMessage.error(error.message || '修改失败')
    } finally {
      changingPwd.value = false
    }
  })
}

onMounted(() => {
  if (userInfo.value) {
    userForm.username = userInfo.value.username
    userForm.realName = userInfo.value.realName || ''
    // email和phone需要从API获取
  }
  
  // 获取完整用户信息
  request.get(`/users/${userInfo.value?.id}`).then((res: any) => {
    userForm.email = res.data?.email || ''
    userForm.phone = res.data?.phone || ''
  }).catch(() => {})
})
</script>

<style scoped>
.profile {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-container {
  max-width: 500px;
  margin-top: 20px;
}

.role-tag {
  margin-right: 8px;
}
</style>
