<!-- 使用 Vue 3 组合式 API 编写脚本 -->
<script setup>
// 引入 Vue 提供的响应式与生命周期钩子
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
// 引入 Element Plus 的消息提示组件
import { ElMessage } from 'element-plus'
// 引入 ECharts 核心（按需引入，减小打包体积）
import * as echarts from 'echarts/core'
// 引入需要用到的图表类型：柱状图、折线图、饼图
import { BarChart, LineChart, PieChart } from 'echarts/charts'
// 引入 ECharts 的辅助组件：网格、图例、标题、悬浮提示
import {
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
} from 'echarts/components'
// 使用 Canvas 渲染器（大数据量性能优于 SVG）
import { CanvasRenderer } from 'echarts/renderers'
// 引入统计相关的 API 请求方法
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
// 引入板块列表的 API 请求方法（用于通过 boardId 反查板块名称）
import { getBoards } from '../../services/modules/forum'

// 向 ECharts 核心注册上述按需引入的组件和渲染器
echarts.use([
  LineChart,
  BarChart,
  PieChart,
  GridComponent,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer,
])

// =================== 状态 ===================
// 页面加载中的标志（展示 loading 态）
const loading = ref(false)
// 手动刷新 Spark 跑批中的标志（按钮禁用态）
const refreshing = ref(false)
// Spark 跑批状态机：READY 待触发 / RUNNING 跑批中 / SUCCESS 成功 / FAILED 失败 / DISABLED 未启用
const sparkState = ref('READY') // READY / RUNNING / SUCCESS / FAILED / DISABLED
// 最近一次数据更新时间（从接口字段取，用于头部提示）
const lastUpdate = ref('')

// 概览统计（用户/帖子/评论/点赞等总数）
const overview = ref({})
// 热门帖子排行列表
const hotPosts = ref([])
// 各板块活跃度列表
const boardActive = ref([])
// 活跃用户排行列表
const activeUsers = ref([])
// 每日趋势数据（新增用户、帖子、评论、点赞）
const dailyTrend = ref([])
// 板块字典（用于根据 boardId 反查板块名称）
const boards = ref([])
// 内容质量分析列表（包含多种 qualityType）
const qualityData = ref([])

// 内容质量 Tab 当前选中项（默认选「高赞低评论」）
const qualityTab = ref('HIGH_LIKE_LOW_COMMENT')
// 内容质量 Tab 的可选项配置
const qualityTabs = [
  { key: 'HIGH_LIKE_LOW_COMMENT', label: '高赞低评论（优质干货）' },
  { key: 'HIGH_COMMENT_LOW_LIKE', label: '高评论低赞（争议帖）' },
  { key: 'HOT', label: '综合热门' },
]

// Spark 跑批轮询定时器（必须在卸载时清理，避免内存泄漏）
const sparkPollTimer = ref(null)
// 跑批超时截止时间戳（5 分钟兜底）
const sparkDeadline = ref(0) // 5 分钟超时兜底

// ECharts 图表容器的 DOM 引用（通过 ref 绑定到 template 中）
const trendChartRef = ref(null)
const pieChartRef = ref(null)
const barChartRef = ref(null)
// 三个 ECharts 实例（惰性初始化，首次渲染时创建）
let trendChart = null
let pieChart = null
let barChart = null
// ResizeObserver 实例（监听图表容器尺寸变化）
let resizeObserver = null

// 上一次点击「刷新统计」按钮的时间戳（用于防抖）
let lastTriggerAt = 0 // 防抖

// =================== 工具 ===================
// 将时间戳/时间字符串格式化为「YYYY-MM-DD HH:mm」
const formatTime = (raw) => {
  // 空值直接返回空字符串
  if (!raw) return ''
  // 用原生 Date 解析（兼容 ISO 字符串和时间戳）
  const d = new Date(raw)
  // 若解析失败（例如非法字符串），原样返回
  if (Number.isNaN(d.getTime())) return raw
  // 内部工具函数：数字补零成两位
  const pad = (n) => String(n).padStart(2, '0')
  // 拼接年月日时分（注意月份从 0 开始，需 +1）
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// 只取日期部分「YYYY-MM-DD」，用于趋势图的 X 轴
const formatDate = (raw) => {
  if (!raw) return ''
  // 截取前 10 个字符（ISO 字符串的日期部分）
  return String(raw).slice(0, 10)
}

// 根据 boardId 从板块字典中查找板块名称（模板中多处使用）
const resolveBoardName = (boardId) => {
  // 强制转为数字，防止字符串和数字不一致
  const target = Number(boardId)
  // 在板块数组中查找匹配项，找不到返回空字符串
  const hit = boards.value.find((b) => Number(b.boardId) === target)
  return hit?.boardName || ''
}

// 将概览对象格式化为 6 张卡片的展示数组（响应式派生数据）
const stats = computed(() => [
  { label: '用户总数', value: overview.value.userCount || 0 },
  { label: '活跃用户', value: overview.value.activeUserCount || 0 },
  { label: '启用板块', value: overview.value.enabledBoardCount || 0 },
  { label: '帖子总数', value: overview.value.postCount || 0 },
  { label: '评论总数', value: overview.value.commentCount || 0 },
  { label: '点赞总数', value: overview.value.likeCount || 0 },
])

// 根据当前选中的 qualityTab 过滤内容质量列表（切换 Tab 时自动重算）
const qualityItems = computed(() => {
  // 防御性处理：确保 qualityData 是数组，防止接口异常时崩溃
  const list = Array.isArray(qualityData.value) ? qualityData.value : []
  // 只保留与当前 Tab 匹配的 qualityType 记录
  return list.filter((it) => it.qualityType === qualityTab.value)
})

// =================== 加载 ===================
// 并发拉取全部统计数据并刷新图表（页面加载时与 Spark 跑批完成后都会调用）
const loadAll = async () => {
  // 先打开 loading 状态，模板中据此显示「正在加载统计数据...」
  loading.value = true
  try {
    // 使用 Promise.all 并发请求 7 个接口：总耗时 = 最慢接口的耗时
    const [ov, hot, activeBoards, activeUsersData, quality, trend, boardsData] =
      await Promise.all([
        getOverview(),              // 概览总数
        getHotPosts({ limit: 10 }), // 热门帖子 TOP 10
        getBoardActive(),           // 各板块活跃度
        getActiveUsers({ limit: 10 }), // 活跃用户 TOP 10
        getContentQuality(),        // 内容质量分析
        getDailyTrend({ days: 7 }), // 近 7 天趋势
        getBoards(),                // 板块字典（用于反查名称）
      ])

    // 将接口结果写入各响应式变量，若返回空则以默认值兜底，防止渲染时异常
    overview.value = ov || {}
    hotPosts.value = Array.isArray(hot) ? hot : []
    boardActive.value = Array.isArray(activeBoards) ? activeBoards : []
    activeUsers.value = Array.isArray(activeUsersData) ? activeUsersData : []
    qualityData.value = Array.isArray(quality) ? quality : []
    dailyTrend.value = Array.isArray(trend) ? trend : []
    boards.value = Array.isArray(boardsData) ? boardsData : []

    // 尝试从各数据中取第一个可用的 updateTime 作为「最近更新时间」
    const candidates = [
      overview.value.updateTime,
      hotPosts.value[0]?.updateTime,
      boardActive.value[0]?.updateTime,
      activeUsers.value[0]?.updateTime,
      dailyTrend.value[0]?.updateTime,
    ].filter(Boolean) // 过滤掉 null/undefined
    // 取第一个有效值（若全为空则留空）
    lastUpdate.value = candidates[0] || ''

    // 等待 Vue 完成 DOM 更新，确保图表容器已有尺寸，再渲染图表
    await nextTick()
    renderCharts()
  } catch (error) {
    // 任意接口失败都弹错误提示（优先用后端返回的 message）
    ElMessage.error(error?.message || '统计数据加载失败')
  } finally {
    // 无论成功失败，都关闭 loading 态
    loading.value = false
  }
}

// =================== Spark 跑批 ===================
// 停止轮询 Spark 跑批状态，并可设置最终状态
const stopPolling = (newState) => {
  // 若定时器存在则清除并置空，防止重复清理
  if (sparkPollTimer.value) {
    clearInterval(sparkPollTimer.value)
    sparkPollTimer.value = null
  }
  // 关闭按钮的 loading 态
  refreshing.value = false
  // 若传入新状态，更新状态机（如 FAILED/SUCCESS）
  if (newState) sparkState.value = newState
}

// 启动轮询：每 2 秒查询一次 Spark 跑批状态，直到不再是 RUNNING 或超时
const pollSparkStatus = () => {
  // 幂等保护：已有定时器在跑则不再启动
  if (sparkPollTimer.value) return
  // 设置 5 分钟的超时兜底（防止后端死锁时前端永远轮询）
  sparkDeadline.value = Date.now() + 5 * 60 * 1000 // 5 分钟兜底

  // 定义单次轮询动作（内部函数，有闭包访问外层状态）
  const tick = async () => {
    // 若超过兜底时间仍未完成，则视为失败
    if (Date.now() > sparkDeadline.value) {
      stopPolling('FAILED')
      ElMessage.error('Spark 跑批超时（5 分钟未结束），请查看服务器日志')
      return
    }
    try {
      // 调用后端接口查询当前跑批状态
      const data = await getSparkStatus()
      // 更新状态机（默认回 READY）
      sparkState.value = data?.state || 'READY'
      // 若仍在跑批中：什么都不做，下一次 tick 再查
      if (sparkState.value === 'RUNNING') return
      // 已得到终态：停止轮询
      stopPolling()
      // 根据不同终态给用户反馈
      if (sparkState.value === 'SUCCESS') {
        ElMessage.success('Spark 跑批完成，已刷新统计')
        // 跑批成功：重新拉取数据刷新页面
        await loadAll()
      } else if (sparkState.value === 'FAILED') {
        ElMessage.error('Spark 跑批失败，请查看后端日志')
      } else if (sparkState.value === 'DISABLED') {
        ElMessage.warning('Spark 跑批未启用（缺少配置）')
      }
    } catch (err) {
      // 接口异常：停止轮询并提示
      stopPolling()
      ElMessage.error(err?.message || '查询跑批状态失败')
    }
  }
  // 注册定时器：每 2 秒执行一次 tick
  sparkPollTimer.value = setInterval(tick, 2000)
  // 立即执行一次（避免用户先空等 2 秒）
  tick()
}

// 用户点击「刷新统计」按钮时触发：请求后端启动 Spark 跑批
const onRefresh = async () => {
  // 已在跑批中，忽略重复点击
  if (refreshing.value) return
  // 记录当前时间戳，做 800ms 防抖（短时间内连点只发一次请求）
  const now = Date.now()
  if (now - lastTriggerAt < 800) return // 800ms 防抖
  lastTriggerAt = now

  // 打开按钮的 loading 态 + 更新状态机为 RUNNING
  refreshing.value = true
  sparkState.value = 'RUNNING'
  try {
    // 请求后端启动跑批
    const data = await triggerSpark()
    // 若后端返回 DISABLED（跑批功能未配置），直接提示并退出
    if (data?.state === 'DISABLED') {
      refreshing.value = false
      sparkState.value = 'DISABLED'
      ElMessage.warning(data?.message || 'Spark 跑批未启用')
      return
    }
    // 跑批已触发：提示用户耐心等待，并启动轮询
    ElMessage.info('已触发 Spark 跑批，等待结果…')
    pollSparkStatus()
  } catch (err) {
    // 请求失败：关闭 loading 并提示
    refreshing.value = false
    ElMessage.error(err?.message || '触发 Spark 跑批失败')
  }
}

// =================== 趋势图 ===================
// 构建每日趋势折线图的 ECharts option（4 条线：新增用户/帖子/评论/点赞）
const buildTrendOption = () => {
  // 复制一份 dailyTrend 并按日期字符串升序排序（保证 X 轴从早到晚）
  const ordered = [...dailyTrend.value].sort((a, b) =>
    String(a.statDate).localeCompare(String(b.statDate)),
  )
  // 提取 X 轴的日期标签数组
  const dates = ordered.map((d) => formatDate(d.statDate))
  // 返回 ECharts 配置对象
  return {
    tooltip: { trigger: 'axis' },         // 悬浮提示：以 X 轴为基准显示多系列数值
    legend: { top: 0 },                    // 图例放在顶部
    grid: { left: 40, right: 24, top: 40, bottom: 30 }, // 图表内边距（防止坐标轴被裁掉）
    xAxis: { type: 'category', boundaryGap: false, data: dates }, // 分类轴 + 贴边绘制
    yAxis: { type: 'value' },              // 数值轴（连续）
    // 4 条平滑折线，分别映射对应计数字段
    series: [
      { name: '新增用户', type: 'line', smooth: true, data: ordered.map((d) => d.newUserCount || 0) },
      { name: '新增帖子', type: 'line', smooth: true, data: ordered.map((d) => d.newPostCount || 0) },
      { name: '新增评论', type: 'line', smooth: true, data: ordered.map((d) => d.newCommentCount || 0) },
      { name: '新增点赞', type: 'line', smooth: true, data: ordered.map((d) => d.newLikeCount || 0) },
    ],
  }
}

// =================== 饼状图 ===================
// 构建内容质量分布环形饼图的 ECharts option
const buildPieOption = () => {
  // 第一步：按 qualityType 做聚合，统计每种类型的帖子数
  const groups = {}
  const list = Array.isArray(qualityData.value) ? qualityData.value : []
  list.forEach((item) => {
    const key = item.qualityType || '其他'
    // 若该类型尚未计数则初始化为 0
    if (!groups[key]) groups[key] = 0
    // 计数 +1
    groups[key]++
  })

  // 类型 key 到中文标签的映射
  const labels = { HIGH_LIKE_LOW_COMMENT: '高赞低评论', HIGH_COMMENT_LOW_LIKE: '高评论低赞', HOT: '综合热门' }
  // 三种类型对应的颜色（按顺序循环取）
  const colors = ['#67c23a', '#e6a23c', '#f56c6c']
  // 组装 ECharts pie 的 data 数组：{name, value, itemStyle.color}
  const pieData = Object.entries(groups).map(([key, value], i) => ({
    name: labels[key] || key,    // 展示中文标签，未知 key 则用 key 本身
    value,                        // 帖子数
    itemStyle: { color: colors[i % colors.length] }, // 按索引取色，循环使用
  }))

  // 返回 ECharts 配置对象
  return {
    title: {
      text: '内容质量分布',
      left: 'center',              // 标题水平居中
      textStyle: { fontSize: 14, color: '#303133' },
    },
    tooltip: { trigger: 'item', formatter: '{b}: {c} 篇（{d}%）' }, // 悬浮提示模板：{b}名称 {c}数值 {d}百分比
    legend: { bottom: 0 },        // 图例放在底部
    series: [
      {
        type: 'pie',
        radius: ['45%', '75%'],        // 内外半径差值：环形饼图
        center: ['50%', '48%'],
        avoidLabelOverlap: false,  // 允许标签重叠（由外层处理）
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 }, // 切片有圆角和白色间隙
        label: { show: true, formatter: '{b}\n{d}%' }, // 在饼图上显示名称和百分比
        emphasis: { scaleSize: 8, label: { fontSize: 16, fontWeight: 'bold' } }, // 悬浮时切片放大、字号加粗
        data: pieData.length ? pieData : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }], // 空数据兜底：灰色占位
      },
    ],
  }
}

// =================== 柱状图 ===================
// 构建板块活跃度 TOP 10 的分组柱状图 option（三种指标并列：帖子/点赞/评论）
const buildBarOption = () => {
  // 按活跃度 activeScore 降序取前 10 个板块（先复制数组再排序，避免修改原数据）
  const top10 = [...boardActive.value].sort((a, b) => (b.activeScore || 0) - (a.activeScore || 0)).slice(0, 10)
  // 提取板块名称作为 X 轴分类标签（没名称时显示 #boardId）
  const names = top10.map((b) => b.boardName || `#${b.boardId}`)
  // 返回 ECharts 配置对象
  return {
    title: {
      text: '板块活跃度对比（TOP 10）',
      left: 'center',
      textStyle: { fontSize: 14, color: '#303133' },
    },
    tooltip: {
      trigger: 'axis',             // X 轴维度触发提示
      axisPointer: { type: 'shadow' }, // 显示阴影指示条
    },
    legend: { top: 24 },            // 图例放在标题下方
    grid: { left: 40, right: 24, top: 56, bottom: 30 },
    xAxis: {
      type: 'category',             // 分类轴：每个板块一个分类
      data: names,
      // 板块超过 5 个时标签倾斜 30°，避免横向拥挤
      axisLabel: { rotate: names.length > 5 ? 30 : 0, fontSize: 11 },
    },
    yAxis: { type: 'value' },       // 数值轴
    // 三组柱：帖子数、点赞数、评论数（颜色与 Element Plus 主题色一致）
    series: [
      {
        name: '帖子数',
        type: 'bar',
        data: top10.map((b) => b.postCount || 0),
        itemStyle: { color: '#409eff' }, // 蓝色
        barMaxWidth: 36,              // 单柱最大宽度，避免太粗
      },
      {
        name: '点赞数',
        type: 'bar',
        data: top10.map((b) => b.likeSum || 0),
        itemStyle: { color: '#67c23a' }, // 绿色
        barMaxWidth: 36,
      },
      {
        name: '评论数',
        type: 'bar',
        data: top10.map((b) => b.commentSum || 0),
        itemStyle: { color: '#e6a23c' }, // 橙色
        barMaxWidth: 36,
      },
    ],
  }
}

// =================== 图表渲染 ===================
// 渲染趋势图：若 DOM 引用存在且实例未初始化，则创建 ECharts 实例，再应用配置
const renderTrendChart = () => {
  if (!trendChartRef.value) return
  // 惰性初始化：第一次调用时才创建 ECharts 实例
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)
  // setOption 的第二个参数 true：表示不合并旧配置，直接替换（保证数据更新时干净）
  trendChart.setOption(buildTrendOption(), true)
}

// 渲染饼状图（同上模式）
const renderPieChart = () => {
  if (!pieChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption(buildPieOption(), true)
}

// 渲染柱状图（同上模式）
const renderBarChart = () => {
  if (!barChartRef.value) return
  if (!barChart) barChart = echarts.init(barChartRef.value)
  barChart.setOption(buildBarOption(), true)
}

// 统一入口：依次渲染三个图表
const renderCharts = () => {
  renderTrendChart()
  renderPieChart()
  renderBarChart()
}

// 浏览器窗口尺寸变化时的回调：让三个图表都自适应
const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
  barChart?.resize()
}

// 使用 ResizeObserver 监听容器 DOM 的尺寸变化（比 window.resize 更精细，可感知侧边栏收起等场景）
const attachChartResize = () => {
  // 浏览器不支持 ResizeObserver 则直接跳过（已由 window.resize 兜底）
  if (typeof ResizeObserver === 'undefined') return
  // 创建观察器，每次容器变化时强制三个图表重绘
  resizeObserver = new ResizeObserver(() => {
    trendChart?.resize()
    pieChart?.resize()
    barChart?.resize()
  })
  // 将三个图表的容器 DOM 都注册为观察目标
  if (trendChartRef.value) resizeObserver.observe(trendChartRef.value)
  if (pieChartRef.value) resizeObserver.observe(pieChartRef.value)
  if (barChartRef.value) resizeObserver.observe(barChartRef.value)
}

// =================== 生命周期 ===================
// 组件挂载完成后的初始化：拉数据 → 渲染图表 → 注册尺寸观察者
onMounted(async () => {
  // 并发拉取所有统计数据并渲染图表（内部含 nextTick）
  await loadAll()
  // 注册 ResizeObserver 监听图表容器
  attachChartResize()
  // 兼容 SSR/非浏览器环境：若在浏览器中，兜底监听 window.resize 事件
  if (typeof window !== 'undefined') {
    window.addEventListener('resize', handleResize)
  }
})

// 组件卸载前的清理工作：必须清理所有定时器、观察者和 ECharts 实例，否则会内存泄漏
onBeforeUnmount(() => {
  // 1. 停止 Spark 跑批轮询（清理 setInterval）
  stopPolling()
  // 2. 断开 ResizeObserver 并置空
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  // 3. 销毁三个 ECharts 实例（try/catch 防止 dispose 报错阻塞后续清理）
  const dispose = (inst) => { try { inst?.dispose() } catch (_) { /* noop */ } }
  dispose(trendChart)
  dispose(pieChart)
  dispose(barChart)
  // 4. 把引用置空，帮助 GC 回收
  trendChart = null
  pieChart = null
  barChart = null
  // 5. 移除 window 的 resize 事件监听
  if (typeof window !== 'undefined') {
    window.removeEventListener('resize', handleResize)
  }
})

// 根据 sparkState 状态机计算头部显示的中文状态文本
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

<!-- 模板根：整个管理后台数据统计页面 -->
<template>
  <!-- 页面主容器（使用 CSS Grid 分块布局） -->
  <main class="admin-page">
    <!-- 头部 hero 区：面包屑 + 标题 + 跑批状态 + 刷新按钮 -->
    <section class="hero">
      <div>
        <p class="eyebrow">Admin / Statistics</p>
        <h1>数据统计</h1>
        <p class="sub">
          概览、排行与趋势由 Spark 离线分析每日 03:00 刷新。
          <!-- 若有更新时间则显示，否则提示暂无记录 -->
          <span v-if="lastUpdate">最近更新于 {{ formatTime(lastUpdate) }}</span>
          <span v-else>暂无更新记录</span>
        </p>
      </div>
      <div class="hero-actions">
        <!-- 跑批状态徽章（用 data-state 属性驱动 CSS 颜色） -->
        <span class="spark-state" :data-state="sparkState">{{ sparkStateText }}</span>
        <!-- 手动触发 Spark 跑批的按钮：跑批中自动禁用 + 显示 loading -->
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

    <!-- 加载态：loading 为 true 时只显示此提示，隐藏下方内容 -->
    <section v-if="loading" class="state">正在加载统计数据...</section>

    <!-- 数据加载完成后的主内容区（用 v-else 的 template 分组，不产生额外 DOM） -->
    <template v-else>
      <!-- 概览卡片（6 项总数）：用户、活跃用户、板块、帖子、评论、点赞 -->
      <section class="panel stats-grid">
        <!-- v-for 渲染卡片：用 item.label 作为 key（因 label 唯一） -->
        <article v-for="item in stats" :key="item.label" class="stat-card">
          <span class="label">{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </article>
      </section>

      <!-- 左右两栏：热门帖子排行 + 板块活跃度 -->
      <section class="grid-2">
        <!-- 热门帖子排行（TOP 10） -->
        <article class="panel">
          <header class="section-head">
            <h2>热门帖子排行</h2>
            <span>共 {{ hotPosts.length }} 条</span>
          </header>
          <!-- 空数据提示：无数据时显示此 div -->
          <div v-if="!hotPosts.length" class="empty">暂无热门帖子数据</div>
          <!-- 有数据时显示表格：排名、标题(含板块)、浏览、点赞、评论、热度分 -->
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
              <!-- 用 postId 作为 key 保证行 DOM 复用 -->
              <tr v-for="row in hotPosts" :key="row.postId">
                <td><span class="rank">#{{ row.rankNum }}</span></td>
                <td class="title-cell">
                  <div class="title">{{ row.title }}</div>
                  <!-- 板块名称：从 boards 字典反查，查不到时显示「板块#boardId」 -->
                  <div class="meta-text">
                    {{ resolveBoardName(row.boardId) || `板块#${row.boardId}` }}
                  </div>
                </td>
                <td>{{ row.viewCount }}</td>
                <td>{{ row.likeCount }}</td>
                <td>{{ row.commentCount }}</td>
                <!-- Number().toFixed(1) 保证为数值且保留 1 位小数（防止接口返回字符串） -->
                <td><strong>{{ Number(row.hotScore || 0).toFixed(1) }}</strong></td>
              </tr>
            </tbody>
          </table>
        </article>

        <!-- 板块活跃度（各板块帖子数、点赞、评论 + 综合活跃度得分） -->
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

      <!-- 用户活跃度排行（TOP 10 活跃用户） -->
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
                <!-- 昵称优先显示，否则 fallback 到 username -->
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

      <!-- 内容质量分析：按 qualityType 用 Tab 切换展示 -->
      <section class="panel">
        <header class="section-head">
          <h2>内容质量分析</h2>
          <span>共 {{ qualityData.length }} 条</span>
        </header>
        <!-- Element Plus Tabs：v-model 绑定 qualityTab，切换时 computed qualityItems 自动重过滤 -->
        <el-tabs v-model="qualityTab">
          <el-tab-pane
            v-for="t in qualityTabs"
            :key="t.key"
            :label="t.label"
            :name="t.key"
          />
        </el-tabs>
        <!-- Tab 下无数据时的空态 -->
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
                <!-- el-tag 的颜色由 qualityType 决定：HOT=红、高赞低评=绿、其他=橙 -->
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

      <!-- 图表区：内容质量饼图 + 板块活跃度柱状图（双栏布局） -->
      <section class="grid-2">
        <article class="panel">
          <!-- 饼图容器：ref="pieChartRef" 绑定到脚本中的 DOM 引用，ECharts 将在此初始化 -->
          <div ref="pieChartRef" class="chart-box"></div>
        </article>
        <article class="panel">
          <!-- 柱状图容器（同上） -->
          <div ref="barChartRef" class="chart-box"></div>
        </article>
      </section>

      <!-- 每日趋势折线图（单独一张图，跨满宽度） -->
      <section class="panel">
        <header class="section-head">
          <h2>每日趋势</h2>
          <span>近 {{ dailyTrend.length }} 天</span>
        </header>
        <!-- 趋势图容器（由 ECharts 绘制） -->
        <div ref="trendChartRef" class="trend-chart"></div>
      </section>
    </template>
  </main>
</template>

<!-- scoped 样式：仅作用于本组件，不污染全局 -->
<style scoped>
/* 页面主容器：占满视口高度 + 背景辐射渐变 + CSS Grid 纵向排列 */
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

/* 头部 hero：毛玻璃卡片 + 横向 flex 布局（标题左、按钮右） */
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
  flex-wrap: wrap; /* 窄屏自动换行 */
}

/* 面包屑小字号文字：全大写 + 字母间距 */
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
  font-size: clamp(30px, 4vw, 44px); /* 响应式字号：30px ~ 44px */
}

.sub {
  margin: 14px 0 0;
  color: var(--muted);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 头部右侧操作区：跑批状态徽章 + 刷新按钮 */
.hero-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 跑批状态徽章的基础样式（圆角胶囊） */
.spark-state {
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.04);
  color: var(--muted);
}
/* 用 data-state 属性驱动不同状态的配色（语义化 CSS，无需多写 class） */
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

/* 通用面板样式：复用给所有卡片（数据表格、图表容器都用） */
.panel {
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 28px;
  padding: 20px 24px;
  box-shadow: var(--shadow);
}

/* 概览卡片：6 列等宽 Grid */
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

/* 面板的标题 + 计数区 */
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

/* 左右两栏布局（热门帖子 + 板块活跃度、饼图 + 柱状图） */
.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

/* 通用数据表格：紧凑、带分隔线 */
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
  background: rgba(0, 0, 0, 0.02); /* 表头浅灰底 */
}

/* 去掉最后一行的底线（视觉干净） */
.data-table tr:last-child td {
  border-bottom: none;
}

/* 排名徽章：圆形胶囊 + 主题色背景 */
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

/* 标题文字：单行省略（防止溢出破坏布局） */
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

/* loading 态的卡片：单独居中显示 */
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

/* 趋势图容器高度（比普通图表稍高） */
.trend-chart {
  width: 100%;
  height: 320px;
}

/* 饼图/柱状图的容器高度 */
.chart-box {
  width: 100%;
  height: 340px;
}

/* 响应式断点 1：中等屏幕。概览卡片变 3 列，左右两栏变单栏 */
@media (max-width: 1080px) {
  .stats-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .grid-2 {
    grid-template-columns: 1fr;
  }
}

/* 响应式断点 2：小屏幕。hero 变纵向，概览卡片变 2 列 */
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