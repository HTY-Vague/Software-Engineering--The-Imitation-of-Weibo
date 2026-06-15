<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register as registerApi } from '../../services/modules/auth'

const router = useRouter()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }

  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }

  callback()
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
}

const handleSubmit = async () => {
  loading.value = true

  try {
    await formRef.value?.validate()
    await registerApi({
      username: form.username,
      nickname: form.nickname,
      password: form.password,
    })

    ElMessage.success('注册成功，请登录')
    await router.replace({
      path: '/auth/login',
      query: { username: form.username },
    })
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error?.message || '注册失败，请检查填写内容')
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
        <p class="eyebrow">Join the forum</p>
        <h1>注册一个账号，参与发帖、评论和点赞。</h1>
        <p class="sub">
          注册后即可使用个人中心、发帖编辑、评论互动和管理员后台权限（取决于角色）。
        </p>
      </aside>

      <section class="card">
        <div class="header">
          <h2>注册</h2>
          <p>创建你的论坛账号</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleSubmit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" autocomplete="nickname" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              autocomplete="new-password"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              autocomplete="new-password"
              show-password
            />
          </el-form-item>

          <el-button type="primary" size="large" class="submit" :loading="loading" @click="handleSubmit">
            注册
          </el-button>
        </el-form>

        <div class="footer">
          <span>已有账号？</span>
          <router-link to="/auth/login">去登录</router-link>
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
