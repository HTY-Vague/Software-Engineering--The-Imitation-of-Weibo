import http from '../http'

export const createPost = (payload) => http.post('/posts/create', payload)

export const getPostDetail = (payload) => http.post('/posts/detail', payload)

export const listPosts = (payload) => http.post('/posts/list', payload)

export const updatePost = (payload) => http.post('/posts/update', payload)

export const deletePost = (payload) => http.post('/posts/delete', payload)
