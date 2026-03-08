<template>
  <div class="settings">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>系统设置</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 基础设置 -->
        <el-tab-pane label="基础设置" name="basic">
          <el-form :model="basicForm" label-width="150px" class="form-container">
            <el-form-item label="评审通过条件">
              <el-select v-model="basicForm['review.pass-condition']" placeholder="请选择">
                <el-option label="全部通过" value="ALL" />
                <el-option label="多数通过" value="MAJORITY" />
                <el-option label="四分之三通过" value="THREE_QUARTERS" />
              </el-select>
              <div class="form-tip">定义文档评审通过的条件</div>
            </el-form-item>
            <el-form-item label="文件最大大小">
              <el-input-number v-model="maxSizeMB" :min="1" :max="500" />
              <span class="unit">MB</span>
              <div class="form-tip">单个文件最大上传大小（1-500MB）</div>
            </el-form-item>
            <el-form-item label="允许的文件类型">
              <el-input v-model="basicForm['file.allowed-types']" placeholder="例如：pdf,docx,doc,txt" />
              <div class="form-tip">多个类型用英文逗号分隔</div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSaveBasic">
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- LDAP设置 -->
        <el-tab-pane label="LDAP设置" name="ldap">
          <el-form :model="ldapForm" label-width="150px" class="form-container">
            <el-form-item label="启用LDAP">
              <el-switch v-model="ldapEnabled" />
            </el-form-item>
            <template v-if="ldapEnabled">
              <el-form-item label="LDAP服务器地址">
                <el-input v-model="ldapForm['ldap.url']" placeholder="ldap://localhost:389" />
              </el-form-item>
              <el-form-item label="Base DN">
                <el-input v-model="ldapForm['ldap.base-dn']" placeholder="dc=company,dc=com" />
              </el-form-item>
              <el-form-item label="绑定DN">
                <el-input v-model="ldapForm['ldap.bind-dn']" placeholder="cn=admin,dc=company,dc=com" />
              </el-form-item>
              <el-form-item label="绑定密码">
                <el-input v-model="ldapForm['ldap.bind-password']" type="password" show-password />
              </el-form-item>
              <el-form-item label="用户名属性">
                <el-input v-model="ldapForm['ldap.user-attribute']" placeholder="sAMAccountName" />
              </el-form-item>
              <el-form-item>
                <el-button :loading="testing" @click="handleTestLdap">
                  测试连接
                </el-button>
              </el-form-item>
            </template>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSaveLdap">
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 系统信息 -->
        <el-tab-pane label="系统信息" name="info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="系统名称">文档评审系统</el-descriptions-item>
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="技术栈">
              Spring Boot 3 + Vue 3 + PostgreSQL
            </el-descriptions-item>
            <el-descriptions-item label="开发团队">Fullstack Team</el-descriptions-item>
            <el-descriptions-item label="数据库">
              {{ dbInfo.type || 'PostgreSQL' }}
            </el-descriptions-item>
            <el-descriptions-item label="存储服务">
              MinIO
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { request } from '@/api/request'

const activeTab = ref('basic')
const saving = ref(false)
const testing = ref(false)

const basicForm = reactive({
  'review.pass-condition': 'ALL',
  'file.max-size': '52428800',
  'file.allowed-types': 'pdf,docx,doc,xlsx,xls,pptx,ppt,txt,md'
})

const ldapForm = reactive({
  'ldap.enabled': 'false',
  'ldap.url': 'ldap://localhost:389',
  'ldap.base-dn': 'dc=company,dc=com',
  'ldap.bind-dn': 'cn=admin,dc=company,dc=com',
  'ldap.bind-password': '',
  'ldap.user-attribute': 'sAMAccountName'
})

const ldapEnabled = computed({
  get: () => ldapForm['ldap.enabled'] === 'true',
  set: (val) => { ldapForm['ldap.enabled'] = val ? 'true' : 'false' }
})

const maxSizeMB = computed({
  get: () => Math.round(parseInt(basicForm['file.max-size']) / 1024 / 1024) || 50,
  set: (val) => { basicForm['file.max-size'] = String(val * 1024 * 1024) }
})

const dbInfo = reactive({
  type: 'PostgreSQL'
})

const handleSaveBasic = async () => {
  saving.value = true
  try {
    await Promise.all([
      request.put('/system/config/review.pass-condition', { value: basicForm['review.pass-condition'] }),
      request.put('/system/config/file.max-size', { value: basicForm['file.max-size'] }),
      request.put('/system/config/file.allowed-types', { value: basicForm['file.allowed-types'] })
    ])
    ElMessage.success('保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleSaveLdap = async () => {
  saving.value = true
  try {
    await Promise.all([
      request.put('/system/config/ldap.enabled', { value: ldapForm['ldap.enabled'] }),
      request.put('/system/config/ldap.url', { value: ldapForm['ldap.url'] }),
      request.put('/system/config/ldap.base-dn', { value: ldapForm['ldap.base-dn'] }),
      request.put('/system/config/ldap.bind-dn', { value: ldapForm['ldap.bind-dn'] }),
      request.put('/system/config/ldap.bind-password', { value: ldapForm['ldap.bind-password'] }),
      request.put('/system/config/ldap.user-attribute', { value: ldapForm['ldap.user-attribute'] })
    ])
    ElMessage.success('保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleTestLdap = async () => {
  testing.value = true
  try {
    await request.post('/system/ldap-test', {
      url: ldapForm['ldap.url'],
      baseDn: ldapForm['ldap.base-dn'],
      bindDn: ldapForm['ldap.bind-dn'],
      bindPassword: ldapForm['ldap.bind-password'],
      userAttribute: ldapForm['ldap.user-attribute']
    })
    ElMessage.success('LDAP连接测试成功')
  } catch (error: any) {
    ElMessage.error(error.message || 'LDAP连接测试失败')
  } finally {
    testing.value = false
  }
}

onMounted(async () => {
  try {
    const res = await request.get('/system/config')
    const config = res.data || {}
    
    // 填充基础设置
    basicForm['review.pass-condition'] = config['review.pass-condition'] || 'ALL'
    basicForm['file.max-size'] = config['file.max-size'] || '52428800'
    basicForm['file.allowed-types'] = config['file.allowed-types'] || 'pdf,docx,doc,xlsx,xls,pptx,ppt,txt,md'
    
    // 填充LDAP设置
    ldapForm['ldap.enabled'] = config['ldap.enabled'] || 'false'
    ldapForm['ldap.url'] = config['ldap.url'] || 'ldap://localhost:389'
    ldapForm['ldap.base-dn'] = config['ldap.base-dn'] || 'dc=company,dc=com'
    ldapForm['ldap.bind-dn'] = config['ldap.bind-dn'] || 'cn=admin,dc=company,dc=com'
    ldapForm['ldap.bind-password'] = config['ldap.bind-password'] || ''
    ldapForm['ldap.user-attribute'] = config['ldap.user-attribute'] || 'sAMAccountName'
  } catch (error) {
    console.error('Failed to load config:', error)
  }
})
</script>

<style scoped>
.settings {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-container {
  max-width: 600px;
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.unit {
  margin-left: 8px;
  color: #606266;
}
</style>
