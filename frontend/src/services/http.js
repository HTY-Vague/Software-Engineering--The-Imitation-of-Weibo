import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080'

const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && typeof payload.code !== 'undefined') {
      if (payload.code === 200) {
        return payload.data
      }

      const error = new Error(payload.message || 'Request failed')
      error.code = payload.code
      return Promise.reject(error)
    }
    return response.data
  },
  (error) => {
    // 401 统一处理：清登录态并跳登录页，避免在登录页循环跳转
    const status = error?.response?.status
    if (status === 401) {
      try {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
      } catch (_) {
        // localStorage 不可用时静默忽略
      }
      if (
        typeof window !== 'undefined' &&
        !window.location.pathname.startsWith('/login')
      ) {
        window.location.replace('/login')
      }
    }
    return Promise.reject(error)
  }
)

export default http
