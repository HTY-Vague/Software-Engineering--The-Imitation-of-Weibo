<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import {
  getActiveUsers,
  getBoardActive,
  getContentQuality,
  getDailyTrend,
  getHotPosts,
  getOverview,
  getSparkStatus,
  triggerSpark,
} from '../../services/modules/statistics'
import { getBoards } from '../../services/modules/forum'

echarts.use([
  LineChart,
  GridComponent,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer,
])

// =================== 状态 ===================
const loading = ref(false)
const refreshing = ref(false)
const sparkState = ref('READY') // READY / RUNNING / SUCCESS / FAILED / DISABLED
const lastUpdate = ref('')

const overview = ref({})
const hotPosts = ref([])
const boardActive = ref([])
const activeUsers = ref([])
const dailyTrend = ref([])
const boards = ref([])
const qualityData = ref([])

const qualityTab = ref('HIGH_LIKE_LOW_COMMENT')
const qualityTabs = [
  { key: 'HIGH_LIKE_LOW_COMMENT', label: '高赞低评论（优质干货）' },
  { key: 'HIGH_COMMENT_LOW_LIKE', label: '高评论低赞（争议帖）' },
  { key: 'HOT', label: '综合热门' },
]

const sparkPollTimer = ref(null)
const sparkDeadline = ref(0) // 5 分钟超时兜底

const trendChartRef = ref(null)
let trendChart = null
let resizeObserver = null

let lastTriggerAt = 0 // 防抖

// =================== 工具 ===================
const formatTime = (raw) => {
  if (!raw) return ''
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return raw
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const formatDate = (raw) => {
  if (!raw) return ''
  return String(raw).slice(0, 10)
}

const resolveBoardName = (boardId) => {
  const target = Number(boardId)
  const hit = boards.value.find((b) => Number(b.boardId) === target)
  return hit?.boardName || ''
}

const stats = computed(() => [
  { label: '用户总数', value: overview.value.userCount || 0 },
  { label: '活跃用户', value: overview.value.activeUserCount || 0 },
  { label: '启用板块', value: overview.value.enabledBoardCount || 0 },
  { label: '帖子总数', value: overview.value.postCount || 0 },
  { label: '评论总数', value: overview.value.commentCount || 0 },
  { label: '点赞总数', value: overview.value.likeCount || 0 },
])

const qualityItems = computed(() => {
  const list = Array.isArray(qualityData.value) ? qualityData.value : []
  return list.filter((it) => it.qualityType === qualityTab.value)
})

// =================== 加载 ===================
const loadAll = async () => {
  loading.value = true
  try {
    const [ov, hot, activeBoards, activeUsersData, quality, trend, boardsData] =
      await Promise.all([
        getOverview(),
        getHotPosts({ limit: 10 }),
        getBoardActive(),
        getActiveUsers({ limit: 10 }),
        getContentQuality(),
        getDailyTrend({ days: 7 }),
        getBoards(),
      ])

    overview.value = ov || {}
    hotPosts.value = Array.isArray(hot) ? hot : []
    boardActive.value = Array.isArray(activeBoards) ? activeBoards : []
    activeUsers.value = Array.isArray(activeUsersData) ? activeUsersData : []
    qualityData.value = Array.isArray(quality) ? quality : []
    dailyTrend.value = Array.isArray(trend) ? trend : []
    boards.value = Array.isArray(boardsData) ? boardsData : []

    const candidates = [
      overview.value.updateTime,
      hotPosts.value[0]?.updateTime,
      boardActive.value[0]?.updateTime,
      activeUsers.value[0]?.updateTime,
      dailyTrend.value[0]?.updateTime,
    ].filter(Boolean)
    lastUpdate.value = candidates[0] || ''

    await nextTick()
    renderTrendChart()
  } catch (error) {
    ElMessage.error(error?.message || '统计数据加载失败')
  } finally {
    loading.value = false
  }
}

// =================== Spark 跑批 ===================
const stopPolling = (newState) => {
  if (sparkPollTimer.value) {
    clearInterval(sparkPollTimer.value)
    sparkPollTimer.value = null
  }
  refreshing.value = false
  if (newState) sparkState.value = newState
}

const pollSparkStatus = () => {
  if (sparkPollTimer.value) return
  sparkDeadline.value = Date.now() + 5 * 60 * 1000 // 5 分钟兜底

  const tick = async () => {
    if (Date.now() > sparkDeadline.value) {
      stopPolling('FAILED')
      ElMessage.error('Spark 跑批超时（5 分钟未结束），请查看服务器日志')
      return
    }
    try {
      const data = await getSparkStatus()
      sparkState.value = data?.state || 'READY'
      if (sparkState.value === 'RUNNING') return
      stopPolling()
      if (sparkState.value === 'SUCCESS') {
        ElMessage.success('Spark 跑批完成，已刷新统计')
        await loadAll()
      } else if (sparkState.value === 'FAILED') {
        ElMessage.error('Spark 跑批失败，请查看后端日志')
      } else if (sparkState.value === 'DISABLED') {
        ElMessage.warning('Spark 跑批未启用（缺少配置）')
      }
    } catch (err) {
      stopPolling()
      ElMessage.error(err?.message || '查询跑批状态失败')
    }
  }
  sparkPollTimer.value = setInterval(tick, 2000)
  tick()
}

const onRefresh = async () => {
  if (refreshing.value) return
  const now = Date.now()
  if (now - lastTriggerAt < 800) return // 800ms 防抖
  lastTriggerAt = now

  refreshing.value = true
  sparkState.value = 'RUNNING'
  try {
    const data = await triggerSpark()
    if (data?.state === 'DISABLED') {
      refreshing.value = false
      sparkState.value = 'DISABLED'
      ElMessage.warning(data?.message || 'Spark 跑批未启用')
      return
    }
    ElMessage.info('已触发 Spark 跑批，等待结果…')
    pollSparkStatus()
  } catch (err) {
    refreshing.value = false
    ElMessage.error(err?.message || '触发 Spark 跑批失败')
  }
}

// =================== 趋势图 ===================
const buildTrendOption = () => {
  const ordered = [...dailyTrend.value].sort((a, b) =>
    String(a.statDate).localeCompare(String(b.statDate)),
  )
  const dates = ordered.map((d) => formatDate(d.statDate))
  return {
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 40, right: 24, top: 40, bottom: 30 },
    xAxis: { type: 'category', boundaryGap: false, data: dates },
    yAxis: { type: 'value' },
    series: [
      { name: '新增用户', type: 'line', smooth: true, data: ordered.map((d) => d.newUserCount || 0) },
      { name: '新增帖子', type: 'line', smooth: true, data: ordered.map((d) => d.newPostCount || 0) },
      { name: '新增评论', type: 'line', smooth: true, data: ordered.map((d) => d.newCommentCount || 0) },
      { name: '新增点赞', type: 'line', smooth: true, data: ordered.map((d) => d.newLikeCount || 0) },
    ],
  }
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  trendChart.setOption(buildTrendOption(), true)
}

const handleResize = () => trendChart && trendChart.resize()

const attachChartResize = () => {
  if (typeof ResizeObserver === 'undefined' || !trendChartRef.value) return
  resizeObserver = new ResizeObserver(() => trendChart?.resize())
  resizeObserver.observe(trendChartRef.value)
}

// =================== 生命周期 ===================
onMounted(async () => {
  await loadAll()
  attachChartResize()
  if (typeof window !== 'undefined') {
    window.addEventListener('resize', handleResize)
  }
})

onBeforeUnmount(() => {
  stopPolling()
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
  if (typeof window !== 'undefined') {
    window.removeEventListener('resize', handleResize)
  }
})

const sparkStateText = computed(() => {
  switch (sparkState.value) {
    case 'RUNNING':
      return '跑批中…'
    case 'SUCCESS':
      return '最近跑批：成功'
    case 'FAILED':
      return '最近跑批：失败'
    case 'DISABLED':
      return '跑批未启用'
    default:
      return '未触发'
  }
})
</script>

<template>
  <main class="admin-page">
    <section class="hero">
      <div>
        <p class="eyebrow">Admin / Statistics</p>
        <h1>数据统计</h1>
        <p class="sub">
          概览、排行与趋势由 Spark 离线分析每日 03:00 刷新。
          <span v-if="lastUpdate">最近更新于 {{ formatTime(lastUpdate) }}</span>
          <span v-else>暂无更新记录</span>
        </p>
      </div>
      <div class="hero-actions">
        <span class="spark-state" :data-state="sparkState">{{ sparkStateText }}</span>
        <el-button
          type="primary"
          :loading="refreshing"
          :disabled="refreshing"
          @click="onRefresh"
        >
          刷新统计
        </el-button>
      </div>
    </section>

    <section v-if="loading" class="state">正在加载统计数据...</section>

    <template v-else>
      <!-- 概览卡片 -->
      <section class="panel stats-grid">
        <article v-for="item in stats" :key="item.label" class="stat-card">
          <span class="label">{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </article>
      </section>

      <!-- 热门帖子 + 板块活跃度 -->
      <section class="grid-2">
        <article class="panel">
          <header class="section-head">
            <h2>热门帖子排行</h2>
            <span>共 {{ hotPosts.length }} 条</span>
          </header>
          <div v-if="!hotPosts.length" class="empty">暂无热门帖子数据</div>
          <table v-else class="data-table">
            <thead>
              <tr>
                <th style="width: 56px">排名</th>
                <th>标题</th>
                <th style="width: 80px">浏览</th>
                <th style="width: 80px">点赞</th>
                <th style="width: 80px">评论</th>
                <th style="width: 100px">热度分</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in hotPosts" :key="row.postId">
                <td><span class="rank">#{{ row.rankNum }}</span></td>
                <td class="title-cell">
                  <div class="title">{{ row.title }}</div>
                  <div class="meta-text">
                    {{ resolveBoardName(row.boardId) || `板块#${row.boardId}` }}
                  </div>
                </td>
                <td>{{ row.viewCount }}</td>
                <td>{{ row.likeCount }}</td>
                <td>{{ row.commentCount }}</td>
                <td><strong>{{ Number(row.hotScore || 0).toFixed(1) }}</strong></td>
              </tr>
            </tbody>
          </table>
        </article>

        <article class="panel">
          <header class="section-head">
            <h2>板块活跃度</h2>
            <span>共 {{ boardActive.length }} 个</span>
          </header>
          <div v-if="!boardActive.length" class="empty">暂无板块活跃度数据</div>
          <table v-else class="data-table">
            <thead>
              <tr>
                <th>板块</th>
                <th style="width: 80px">帖子</th>
                <th style="width: 80px">点赞</th>
                <th style="width: 80px">评论</th>
                <th style="width: 100px">活跃度</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in boardActive" :key="row.boardId">
                <td>{{ row.boardName || `#${row.boardId}` }}</td>
                <td>{{ row.postCount }}</td>
                <td>{{ row.likeSum }}</td>
                <td>{{ row.commentSum }}</td>
                <td><strong>{{ Number(row.activeScore || 0).toFixed(1) }}</strong></td>
              </tr>
            </tbody>
          </table>
        </article>
      </section>

      <!-- 用户活跃度 -->
      <section class="panel">
        <header class="section-head">
          <h2>用户活跃度排行</h2>
          <span>共 {{ activeUsers.length }} 位</span>
        </header>
        <div v-if="!activeUsers.length" class="empty">暂无用户活跃度数据</div>
        <table v-else class="data-table">
          <thead>
            <tr>
              <th style="width: 56px">排名</th>
              <th>用户</th>
              <th style="width: 80px">发帖</th>
              <th style="width: 80px">评论</th>
              <th style="width: 80px">获赞</th>
              <th style="width: 100px">活跃度</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in activeUsers" :key="row.userId">
              <td><span class="rank">#{{ row.rankNum }}</span></td>
              <td>
                <div class="title">{{ row.nickname || row.username }}</div>
                <div class="meta-text">@{{ row.username }}</div>
              </td>
              <td>{{ row.postCount }}</td>
              <td>{{ row.commentCount }}</td>
              <td>{{ row.likeCount }}</td>
              <td><strong>{{ Number(row.activeScore || 0).toFixed(1) }}</strong></td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- 内容质量分析 -->
      <section class="panel">
        <header class="section-head">
          <h2>内容质量分析</h2>
          <span>共 {{ qualityData.length }} 条</span>
        </header>
        <el-tabs v-model="qualityTab">
          <el-tab-pane
            v-for="t in qualityTabs"
            :key="t.key"
            :label="t.label"
            :name="t.key"
          />
        </el-tabs>
        <div v-if="!qualityItems.length" class="empty">该分类暂无内容</div>
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>帖子</th>
              <th style="width: 80px">浏览</th>
              <th style="width: 80px">点赞</th>
              <th style="width: 80px">评论</th>
              <th style="width: 140px">分类</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in qualityItems" :key="row.postId">
              <td>
                <div class="title">{{ row.title }}</div>
              </td>
              <td>{{ row.viewCount }}</td>
              <td>{{ row.likeCount }}</td>
              <td>{{ row.commentCount }}</td>
              <td>
                <el-tag
                  :type="row.qualityType === 'HOT' ? 'danger' : (row.qualityType === 'HIGH_LIKE_LOW_COMMENT' ? 'success' : 'warning')"
                  size="small"
                >
                  {{ row.qualityType }}
                </el-tag>
              </td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- 每日趋势 -->
      <section class="panel">
        <header class="section-head">
          <h2>每日趋势</h2>
          <span>近 {{ dailyTrend.length }} 天</span>
        </header>
        <div ref="trendChartRef" class="trend-chart"></div>
      </section>
    </template>
  </main>
</template>

<style scoped>
.admin-page {
  min-height: 100vh;
  padding: 32px 20px 48px;
  background:
    radial-gradient(circle at top left, rgba(227, 76, 47, 0.12), transparent 28%),
    radial-gradient(circle at bottom right, rgba(31, 26, 23, 0.08), transparent 36%),
    var(--bg);
  display: grid;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.hero {
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 28px;
  padding: 28px 32px;
  box-shadow: var(--shadow);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0 0 12px;
  color: var(--accent);
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

h1 {
  margin: 0;
  font-size: clamp(30px, 4vw, 44px);
}

.sub {
  margin: 14px 0 0;
  color: var(--muted);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.spark-state {
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.04);
  color: var(--muted);
}
.spark-state[data-state='RUNNING'] {
  background: rgba(64, 158, 255, 0.12);
  color: #1d6fd1;
}
.spark-state[data-state='SUCCESS'] {
  background: rgba(103, 194, 58, 0.16);
  color: #2f7d23;
}
.spark-state[data-state='FAILED'] {
  background: rgba(245, 108, 108, 0.18);
  color: #b13a3a;
}

.panel {
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 28px;
  padding: 20px 24px;
  box-shadow: var(--shadow);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 8px 4px;
}

.label {
  color: var(--muted);
  font-size: 13px;
}

.stat-card strong {
  font-size: 28px;
  color: var(--text);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.section-head h2 {
  margin: 0;
  font-size: 20px;
}

.section-head span,
.empty {
  color: var(--muted);
  font-size: 13px;
}

.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.data-table th,
.data-table td {
  padding: 10px 8px;
  text-align: left;
  border-bottom: 1px solid rgba(225, 217, 209, 0.7);
}

.data-table th {
  font-weight: 600;
  color: var(--muted);
  font-size: 13px;
  background: rgba(0, 0, 0, 0.02);
}

.data-table tr:last-child td {
  border-bottom: none;
}

.rank {
  display: inline-block;
  min-width: 28px;
  padding: 2px 8px;
  background: rgba(227, 76, 47, 0.12);
  color: var(--accent);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  text-align: center;
}

.title-cell .title,
.title {
  font-weight: 600;
  color: var(--text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 360px;
}

.title-cell .meta-text,
.meta-text {
  font-size: 12px;
  color: var(--muted);
  margin-top: 2px;
}

.state {
  width: min(1200px, 100%);
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 24px;
  padding: 24px;
  box-shadow: var(--shadow);
  color: var(--muted);
  text-align: center;
}

.trend-chart {
  width: 100%;
  height: 320px;
}

@media (max-width: 1080px) {
  .stats-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .grid-2 {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
