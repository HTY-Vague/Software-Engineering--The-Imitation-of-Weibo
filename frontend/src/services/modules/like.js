import http from '../http'

export const like = (params) => http.post('/likes/like', null, { params })

export const unlike = (params) => http.post('/likes/unlike', null, { params })

export const getLikeStatus = (params) => http.post('/likes/status', null, { params })

export const getLikeCount = (params) => http.post('/likes/count', null, { params })
