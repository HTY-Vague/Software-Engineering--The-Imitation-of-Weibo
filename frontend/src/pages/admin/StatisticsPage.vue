<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getHotPosts, getOverview } from '../../services/modules/admin'
import { getBoards } from '../../services/modules/forum'
import PostCard from '../../components/post/PostCard.vue'

const loading = ref(false)
const overview = ref({})
const posts = ref([])
const boards = ref([])
const limit = ref(10)

const normalizeList = (payload) => {
  if (Array.isArray(payload)) {
    return payload
  }

  return payload?.list || payload?.records || []
}

const stats = computed(() => [
  { label: '用户总数', value: overview.value.userCount || 0 },
  { label: '活跃用户', value: overview.value.activeUserCount || 0 },
  { label: '启用板块', value: overview.value.enabledBoardCount || 0 },
  { label: '帖子总数', value: overview.value.postCount || 0 },
  { label: '评论总数', value: overview.value.commentCount || 0 },
  { label: '点赞总数', value: overview.value.likeCount || 0 },
])

const boardNameMap = computed(() => {
  const map = new Map()
  boards.value.forEach((item) => map.set(Number(item.boardId), item.boardName))
  return map
})

const resolveBoardName = (boardId) => boardNameMap.value.get(Number(boardId)) || ''

const loadData = async () => {
  loading.value = true
  try {
    const [overviewData, hotPostsData, boardsData] = await Promise.all([
      getOverview(),
      getHotPosts({ limit: limit.value }),
      getBoards(),
    ])
    overview.value = overviewData || {}
    posts.value = normalizeList(hotPostsData)
    boards.value = normalizeList(boardsData)
  } catch (error) {
    ElMessage.error(error?.message || '统计数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <main class="admin-page">
    <section class="hero">
      <div>
        <p class="eyebrow">Admin / Statistics</p>
        <h1>数据统计</h1>
        <p class="sub">查看概览指标和热门帖子。</p>
      </div>
      <div class="hero-actions">
        <el-input-number v-model="limit" :min="1" :max="50" controls-position="right" />
        <el-button type="primary" @click="loadData">刷新</el-button>
      </div>
    </section>

    <section v-if="loading" class="state">正在加载统计数据...</section>

    <section v-else class="stack">
      <section class="stats-grid">
        <article v-for="item in stats" :key="item.label" class="panel stat-card">
          <span class="label">{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </article>
      </section>

      <section class="panel">
        <div class="section-head">
          <h2>热门帖子</h2>
          <span>{{ posts.length }} 条</span>
        </div>

        <div v-if="!posts.length" class="empty">暂无热门帖子数据</div>
        <div v-else class="list">
          <PostCard
            v-for="post in posts"
            :key="post.postId"
            :post="post"
            :board-name="resolveBoardName(post.boardId)"
          />
        </div>
      </section>
    </section>
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
}

.hero {
  width: min(1200px, 100%);
  margin: 0 auto 24px;
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
}

.hero-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.stack {
  width: min(1200px, 100%);
  margin: 0 auto;
  display: grid;
  gap: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.panel {
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 28px;
  padding: 20px;
  box-shadow: var(--shadow);
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.label {
  color: var(--muted);
  font-size: 14px;
}

strong {
  font-size: 32px;
  color: var(--text);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-head h2 {
  margin: 0;
  font-size: 22px;
}

.section-head span,
.empty {
  color: var(--muted);
}

.list {
  display: grid;
  gap: 16px;
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

@media (max-width: 960px) {
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 720px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
