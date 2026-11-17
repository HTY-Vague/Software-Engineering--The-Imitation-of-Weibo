<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logout as logoutApi } from '../../services/modules/auth'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

authStore.hydrate()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const isAdmin = computed(() => authStore.isAdmin)
const user = computed(() => authStore.user)

const handleLogout = async () => {
  try {
    await logoutApi()
  } catch {
    // 无状态 JWT 场景下，服务端失败不影响本地退出
  } finally {
    authStore.clearAuth()
    ElMessage.success('已退出登录')
    await router.push('/')
  }
}
</script>

<template>
  <header class="nav-shell">
    <div class="nav-inner">
      <router-link to="/" class="brand">
        <span class="mark">W</span>
        <span class="text">
          <strong>Weibo Forum</strong>
          <small>Spring Boot + Vue 3</small>
        </span>
      </router-link>

      <nav class="links" aria-label="Primary navigation">
        <router-link to="/">首页</router-link>
        <router-link v-if="isAuthenticated" to="/post/create">发帖</router-link>
        <router-link v-if="isAuthenticated" to="/me">个人中心</router-link>
        <router-link v-if="isAuthenticated" to="/me/posts">我的帖子</router-link>
        <router-link v-if="isAuthenticated" to="/me/comments">我的评论</router-link>
        <router-link v-if="isAdmin" to="/admin/users">管理后台</router-link>
      </nav>

      <div class="actions">
        <template v-if="isAuthenticated">
          <div class="user-chip">
            <span class="nickname">{{ user?.nickname || user?.username || '用户' }}</span>
            <span class="role">{{ user?.role || 'USER' }}</span>
          </div>
          <el-button plain @click="handleLogout">退出</el-button>
        </template>
        <template v-else>
          <el-button plain @click="router.push('/auth/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/auth/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.nav-shell {
  position: sticky;
  top: 0;
  z-index: 30;
  padding: 14px 18px 0;
  background: linear-gradient(to bottom, rgba(246, 243, 238, 0.98), rgba(246, 243, 238, 0.86));
  backdrop-filter: blur(14px);
}

.nav-inner {
  width: min(1200px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 20px;
  padding: 14px 18px;
  border: 1px solid rgba(225, 217, 209, 0.95);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: var(--shadow);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.mark {
  width: 40px;
  height: 40px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, var(--accent), #ff8b63);
  color: #fff;
  font-weight: 900;
  letter-spacing: 0.02em;
}

.text {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}

.text strong {
  font-size: 16px;
}

.text small {
  color: var(--muted);
  font-size: 12px;
}

.links {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 14px;
}

.links a {
  color: var(--muted);
  font-weight: 700;
  padding: 8px 10px;
  border-radius: 999px;
}

.links a.router-link-active {
  color: var(--accent-strong);
  background: rgba(227, 76, 47, 0.1);
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(31, 26, 23, 0.06);
  color: var(--text);
}

.nickname {
  font-weight: 800;
}

.role {
  font-size: 12px;
  color: var(--muted);
}

@media (max-width: 980px) {
  .nav-inner {
    grid-template-columns: 1fr;
    justify-items: stretch;
  }

  .links {
    justify-content: flex-start;
  }

  .actions {
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>
