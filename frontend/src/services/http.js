import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://122.51.87.179:8080'

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
  (error) => Promise.reject(error)
)

export default http
