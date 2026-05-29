import { defineStore } from 'pinia'

const TOKEN_KEY = 'token'
const USER_KEY = 'user'

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
    hydrate() {
      if (this.hydrated) {
        return
      }

      this.token = localStorage.getItem(TOKEN_KEY) || ''

      const rawUser = localStorage.getItem(USER_KEY)
      this.user = rawUser ? JSON.parse(rawUser) : null
      this.hydrated = true
    },
    setAuth(payload) {
      this.token = payload?.token || ''
      this.user = payload?.user || null
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
