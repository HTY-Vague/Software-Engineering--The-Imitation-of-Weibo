import http from '../http'

export const getUsers = () => http.get('/admin/users')

export const updateUserStatus = (userId, payload) =>
  http.put(`/admin/users/${userId}/status`, payload)

export const getForums = () => http.get('/admin/forums')

export const createForum = (payload) => http.post('/admin/forums', payload)

export const updateForum = (forumId, payload) => http.put(`/admin/forums/${forumId}`, payload)

export const updateForumStatus = (forumId, payload) =>
  http.put(`/admin/forums/${forumId}/status`, payload)

export const sortForums = (payload) => http.put('/admin/forums/sort', payload)

export const deleteForum = (forumId) => http.delete(`/admin/forums/${forumId}`)

export const setPostTop = (postId, payload) => http.put(`/admin/posts/${postId}/top`, payload)

export const deletePost = (postId) => http.delete(`/admin/posts/${postId}`)

export const deleteComment = (commentId) => http.delete(`/admin/comments/${commentId}`)

export const getOverview = () => http.get('/admin/statistics/overview')
