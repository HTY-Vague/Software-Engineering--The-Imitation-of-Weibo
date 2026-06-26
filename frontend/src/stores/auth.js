import { defineStore } from 'pinia'

const TOKEN_KEY = 'token'
const USER_KEY = 'user'

const sanitizeUser = (user) => {
  if (!user) {
    return null
  }

  const { password, ...safeUser } = user
  return safeUser
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    user: null,
    hydrated: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.role === 'ADMIN',
  },
  actions: {
    // 从 localStorage 中恢复认证状态，确保页面刷新后仍保持登录状态
    hydrate() {
      // 防止重复执行：已经初始化过则直接返回
      if (this.hydrated) {
        return
      }

      // 从 localStorage 读取 token，若不存在则设为空字符串
      this.token = localStorage.getItem(TOKEN_KEY) || ''

      // 从 localStorage 读取用户信息并解析为对象，若不存在则设为 null
      const rawUser = localStorage.getItem(USER_KEY)
      this.user = rawUser ? JSON.parse(rawUser) : null
      // 标记为已初始化，避免重复读取
      this.hydrated = true
    },
    setAuth(payload) {
      this.token = payload?.token || ''
      this.user = sanitizeUser(payload?.user)
      localStorage.setItem(TOKEN_KEY, this.token)
      if (this.user) {
        localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      } else {
        localStorage.removeItem(USER_KEY)
      }
    },
    clearAuth() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },
  },
})