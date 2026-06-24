import http from '../http'

/**
 * 统计模块接口封装
 *
 * - 概览 / 热门帖子：Spark 离线分析产出
 * - 板块活跃度 / 用户活跃度 / 内容质量 / 每日趋势：分析维度扩展
 * - 触发跑批 / 跑批状态：演示用，触发后需轮询直到非 RUNNING
 */

export const getOverview = () => http.get('/admin/statistics/overview')

export const getHotPosts = (params) => http.get('/admin/statistics/hot-posts', { params })

export const getBoardActive = () => http.get('/admin/statistics/board-active')

export const getActiveUsers = (params) => http.get('/admin/statistics/active-users', { params })

export const getContentQuality = (params) => http.get('/admin/statistics/content-quality', { params })

export const getDailyTrend = (params) => http.get('/admin/statistics/daily-trend', { params })

export const triggerSpark = () => http.post('/admin/statistics/trigger-spark')

export const getSparkStatus = () => http.get('/admin/statistics/spark-status')
