// 认证相关 API 模块：登录、注册、登出
// 内部复用项目统一封装的 axios 实例（../http），集中处理 baseURL、拦截器、错误提示等
import http from '../http'

/**
 * 用户登录
 * @param {Object} payload      请求体（通常包含 username、password 等字段）
 * @returns {Promise}           返回后端登录响应（一般含 token / 用户信息）
 */
export const login = (payload) => http.post('/auth/login', payload)

/**
 * 用户注册
 * @param {Object} payload      请求体（通常包含 username、password、email 等字段）
 * @returns {Promise}           返回后端注册响应
 */
export const register = (payload) => http.post('/auth/register', payload)

/**
 * 用户登出（通知后端清理 session / 使 token 失效）
 * @returns {Promise}           返回后端登出响应
 */
export const logout = () => http.post('/auth/logout')