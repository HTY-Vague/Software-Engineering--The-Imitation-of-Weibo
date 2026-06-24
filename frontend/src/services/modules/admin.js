import http from '../http'

export const getUsers = () => http.get('/admin/users')

export const updateUserStatus = (userId, status) =>
  http.put(`/admin/users/${userId}/status`, status)

export const getForums = () => http.get('/admin/forums')

export const createForum = (payload) => http.post('/admin/forums', payload)

export const updateForum = (forumId, payload) => http.put(`/admin/forums/${forumId}`, payload)

export const updateForumStatus = (forumId, status) =>
  http.put(`/admin/forums/${forumId}/status`, status)

export const sortForums = (payload) => http.put('/admin/forums/sort', payload)

export const deleteForum = (forumId) => http.delete(`/admin/forums/${forumId}`)

export const setPostTop = (postId, isTop) => http.put(`/admin/posts/${postId}/top`, isTop)

export const deletePost = (postId) => http.delete(`/admin/posts/${postId}`)

export const deleteComment = (commentId) => http.delete(`/admin/comments/${commentId}`)

// 注意：getOverview / getHotPosts 已迁移到 services/modules/statistics.js
// 旧 import 路径已废弃，保留注释以便历史引用排查。
//
// export const getOverview = () => http.get('/admin/statistics/overview')
// export const getHotPosts = (params) => http.get('/admin/statistics/hot-posts', { params })

export const getComments = () => http.get('/admin/comments')
