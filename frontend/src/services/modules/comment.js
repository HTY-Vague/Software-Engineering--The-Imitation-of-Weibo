import http from '../http'

export const listComments = (payload) => http.post('/comments/list', payload)

export const createComment = (payload) => http.post('/comments/create', payload)

export const deleteComment = (payload) => http.post('/comments/delete', payload)
