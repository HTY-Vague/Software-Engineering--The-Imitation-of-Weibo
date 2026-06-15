import http from '../http'

export const getProfile = () => http.post('/user/profile')

export const updateProfile = (payload) => http.post('/user/update/profile', payload)

export const updatePassword = (payload) => http.post('/user/update/password', payload)

export const getMyPosts = (payload) => http.post('/user/posts', payload)

export const deleteMyPost = (payload) => http.post('/user/delete/post', payload)

export const getMyComments = (payload) => http.post('/user/comments', payload)
