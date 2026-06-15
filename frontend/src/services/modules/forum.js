import http from '../http'

export const getBoards = () => http.post('/forum/boards')

export const getBoardPosts = (payload) => http.post('/forum/board/posts', payload)
