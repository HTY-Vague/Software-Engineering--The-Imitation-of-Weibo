import http from '../http'

export const listComments = (params) => http.post('/comments/list', null, { params })

export const createComment = (payload) => http.post('/comments/create', payload)

export const deleteComment = (params) => http.post('/comments/delete', null, { params })
