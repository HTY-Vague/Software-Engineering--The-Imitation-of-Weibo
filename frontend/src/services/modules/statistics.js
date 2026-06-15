import http from '../http'

export const getHotPosts = (params) => http.get('/admin/statistics/hot-posts', { params })
