<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updatePassword, updateProfile } from '../../services/modules/user'
import { useAuthStore } from '../../stores/auth'
import { useUserStore } from '../../stores/user'

const authStore = useAuthStore()
const userStore = useUserStore()

const profileLoading = ref(false)
const savingProfile = ref(false)
const savingPassword = ref(false)

const profileFormRef = ref()
const passwordFormRef = ref()

const profileForm = reactive({
  username: '',
  nickname: '',
  avatar: '',
  signature: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const profileRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
}

const validateConfirmPassword = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
    return
  }

  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }

  callback()
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
}

const hydrateProfile = (profile) => {
  profileForm.username = profile?.username || ''
  profileForm.nickname = profile?.nickname || ''
  profileForm.avatar = profile?.avatar || ''
  profileForm.signature = profile?.signature || ''
}

const loadProfile = async () => {
  profileLoading.value = true
  try {
    const profile = await getProfile()
    userStore.setProfile(profile)
    authStore.hydrate()
    if (authStore.user) {
      authStore.user = { ...authStore.user, ...profile, password: undefined }
    }
    hydrateProfile(profile)
  } catch (error) {
    ElMessage.error(error?.message || '个人资料加载失败')
  } finally {
    profileLoading.value = false
  }
}

const handleSaveProfile = async () => {
  savingProfile.value = true
  try {
    await profileFormRef.value?.validate()
    await updateProfile({
      username: profileForm.username.trim(),
      nickname: profileForm.nickname.trim(),
      avatar: profileForm.avatar.trim(),
      signature: profileForm.signature.trim(),
    })
    ElMessage.success('个人资料已更新')
    await loadProfile()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error?.message || '更新资料失败')
    }
  } finally {
    savingProfile.value = false
  }
}

const handleSavePassword = async () => {
  savingPassword.value = true
  try {
    await passwordFormRef.value?.validate()
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error?.message || '修改密码失败')
    }
  } finally {
    savingPassword.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <main class="profile-page">
    <section class="hero">
      <div>
        <p class="eyebrow">Personal Center</p>
        <h1>个人资料</h1>
        <p class="sub">查看和更新当前登录用户信息，修改密码后建议重新登录。</p>
      </div>
    </section>

    <section v-if="profileLoading" class="panel state">正在加载个人资料...</section>

    <section v-else class="grid">
      <section class="panel">
        <div class="panel-head">
          <h2>基本资料</h2>
        </div>

        <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="profileForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="头像地址">
            <el-input v-model="profileForm.avatar" placeholder="请输入头像 URL" />
          </el-form-item>
          <el-form-item label="个性签名">
            <el-input
              v-model="profileForm.signature"
              type="textarea"
              :rows="4"
              maxlength="200"
              show-word-limit
              placeholder="写一句介绍自己"
            />
          </el-form-item>

          <div class="actions">
            <el-button type="primary" :loading="savingProfile" @click="handleSaveProfile">保存资料</el-button>
          </div>
        </el-form>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h2>修改密码</h2>
        </div>

        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              show-password
              placeholder="再次输入新密码"
            />
          </el-form-item>

          <div class="actions">
            <el-button type="primary" :loading="savingPassword" @click="handleSavePassword">修改密码</el-button>
          </div>
        </el-form>
      </section>
    </section>
  </main>
</template>

<style scoped>
.profile-page {
  min-height: 100vh;
  padding: 32px 20px 48px;
  background:
    radial-gradient(circle at top left, rgba(227, 76, 47, 0.12), transparent 28%),
    radial-gradient(circle at bottom right, rgba(31, 26, 23, 0.08), transparent 36%),
    var(--bg);
}

.hero {
  width: min(1200px, 100%);
  margin: 0 auto 24px;
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 28px;
  padding: 28px 32px;
  box-shadow: var(--shadow);
}

.eyebrow {
  margin: 0 0 12px;
  color: var(--accent);
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

h1 {
  margin: 0;
  font-size: clamp(30px, 4vw, 44px);
}

.sub {
  margin: 14px 0 0;
  color: var(--muted);
}

.grid {
  width: min(1200px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.panel {
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 28px;
  padding: 28px;
  box-shadow: var(--shadow);
}

.state {
  width: min(1200px, 100%);
  margin: 0 auto;
  text-align: center;
  color: var(--muted);
}

.panel-head {
  margin-bottom: 20px;
}

.panel-head h2 {
  margin: 0;
  font-size: 22px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

@media (max-width: 960px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
