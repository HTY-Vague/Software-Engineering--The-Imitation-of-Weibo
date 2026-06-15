<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login as loginApi } from '../../services/modules/auth'
import { useAuthStore } from '../../stores/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

onMounted(() => {
  const presetUsername = typeof route.query.username === 'string' ? route.query.username : ''
  if (presetUsername) {
    form.username = presetUsername
  }
})

const handleSubmit = async () => {
  loading.value = true

  try {
    await formRef.value?.validate()
    const data = await loginApi({
      username: form.username,
      password: form.password,
    })

    authStore.setAuth(data)
    userStore.setProfile(data?.user || null)
    ElMessage.success('登录成功')

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect)
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error?.message || '登录失败，请检查账号密码')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <section class="shell">
      <aside class="hero">
        <p class="eyebrow">Software Engineering Forum</p>
        <h1>登录后继续浏览、发帖与互动。</h1>
        <p class="sub">
          统一接入后端 JWT 认证，登录后即可访问个人中心、帖子管理和管理员后台。
        </p>
      </aside>

      <section class="card">
        <div class="header">
          <h2>登录</h2>
          <p>使用你的账号进入系统</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleSubmit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
              show-password
            />
          </el-form-item>

          <el-button type="primary" size="large" class="submit" :loading="loading" @click="handleSubmit">
            登录
          </el-button>
        </el-form>

        <div class="footer">
          <span>还没有账号？</span>
          <router-link to="/auth/register">去注册</router-link>
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  padding: 32px 20px;
  display: grid;
  place-items: center;
  background:
    radial-gradient(circle at top left, rgba(227, 76, 47, 0.15), transparent 30%),
    radial-gradient(circle at bottom right, rgba(31, 26, 23, 0.08), transparent 36%),
    var(--bg);
}

.shell {
  width: min(1120px, 100%);
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(360px, 0.9fr);
  gap: 28px;
  align-items: stretch;
}

.hero,
.card {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 28px;
  box-shadow: var(--shadow);
}

.hero {
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.eyebrow {
  margin: 0 0 16px;
  color: var(--accent);
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 12px;
}

h1 {
  margin: 0;
  font-size: clamp(34px, 5vw, 58px);
  line-height: 1.05;
}

.sub {
  margin: 18px 0 0;
  max-width: 56ch;
  color: var(--muted);
  font-size: 16px;
  line-height: 1.7;
}

.card {
  padding: 36px;
}

.header {
  margin-bottom: 24px;
}

.header h2 {
  margin: 0 0 8px;
  font-size: 28px;
}

.header p {
  margin: 0;
  color: var(--muted);
}

.submit {
  width: 100%;
  margin-top: 8px;
}

.footer {
  margin-top: 18px;
  display: flex;
  justify-content: center;
  gap: 8px;
  color: var(--muted);
}

.footer a {
  color: var(--accent);
  font-weight: 700;
}

@media (max-width: 960px) {
  .shell {
    grid-template-columns: 1fr;
  }

  .hero {
    padding: 28px;
  }

  .card {
    padding: 28px;
  }
}
</style>
