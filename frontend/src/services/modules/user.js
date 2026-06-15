import http from '../http'

export const getProfile = () => http.post('/user/profile')

export const updateProfile = (payload) => http.post('/user/update/profile', payload)

export const updatePassword = (payload) => http.post('/user/update/password', payload)

export const getMyPosts = () => http.post('/user/posts')

export const deleteMyPost = (params) => http.post('/user/delete/post', null, { params })

export const getMyComments = () => http.post('/user/comments')
