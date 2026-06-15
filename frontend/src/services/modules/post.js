import http from '../http'

export const createPost = (payload) => http.post('/posts/create', payload)

export const getPostDetail = (params) => http.post('/posts/detail', null, { params })

export const listPosts = (params) => http.post('/posts/list', null, { params })

export const updatePost = (payload) => http.post('/posts/update', payload)

export const deletePost = (params) => http.post('/posts/delete', null, { params })
