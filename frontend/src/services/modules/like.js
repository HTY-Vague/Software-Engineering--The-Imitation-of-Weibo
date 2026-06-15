import http from '../http'

export const like = (payload) => http.post('/likes/like', payload)

export const unlike = (payload) => http.post('/likes/unlike', payload)

export const getLikeStatus = (payload) => http.post('/likes/status', payload)

export const getLikeCount = (payload) => http.post('/likes/count', payload)
