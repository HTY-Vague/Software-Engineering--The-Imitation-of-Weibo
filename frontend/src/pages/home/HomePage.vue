<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BoardSidebar from '../../components/layout/BoardSidebar.vue'
import PostCard from '../../components/post/PostCard.vue'
import { getBoards } from '../../services/modules/forum'
import { listPosts } from '../../services/modules/post'
import { usePostStore } from '../../stores/post'

const route = useRoute()
const router = useRouter()
const postStore = usePostStore()

const loading = ref(false)
const boardsLoading = ref(false)
const boards = ref([])

const filters = reactive({
  keyword: typeof route.query.keyword === 'string' ? route.query.keyword : '',
})

const posts = computed(() => postStore.list)

const normalizeList = (payload) => {
  if (Array.isArray(payload)) {
    return payload
  }

  return payload?.list || payload?.records || payload?.rows || []
}

const loadBoards = async () => {
  boardsLoading.value = true
  try {
    boards.value = normalizeList(await getBoards())
  } catch (error) {
    ElMessage.error(error?.message || '板块列表加载失败')
  } finally {
    boardsLoading.value = false
  }
}

const loadPosts = async () => {
  loading.value = true
  try {
    const payload = {
      keyword: filters.keyword || undefined,
      page: 1,
      size: 20,
    }

    const boardId = route.query.boardId
    if (boardId !== undefined && boardId !== '') {
      payload.boardId = Number(boardId)
    }

    postStore.setFilters({
      keyword: filters.keyword,
      boardId: payload.boardId || null,
      page: 1,
      size: 20,
    })
    postStore.setList({ list: normalizeList(await listPosts(payload)) })
  } catch (error) {
    ElMessage.error(error?.message || '帖子列表加载失败')
    postStore.setList({ list: [] })
  } finally {
    loading.value = false
  }
}

const handleBoardSelect = async (boardId) => {
  const query = { ...route.query }
  if (boardId === null) {
    delete query.boardId
  } else {
    query.boardId = String(boardId)
  }

  await router.push({ path: '/', query })
}

const handleSearch = async () => {
  const query = { ...route.query }
  if (filters.keyword) {
    query.keyword = filters.keyword
  } else {
    delete query.keyword
  }
  await router.push({ path: '/', query })
}

watch(
  () => [route.query.boardId, route.query.keyword],
  async () => {
    await loadPosts()
  },
  { immediate: true }
)

onMounted(async () => {
  await loadBoards()
})
</script>

<template>
  <main class="home">
    <section class="hero">
      <div>
        <p class="eyebrow">Forum Timeline</p>
        <h1>浏览帖子、按板块筛选、搜索关键词。</h1>
        <p class="sub">首页已连接后端 `/posts/list` 和 `/forum/boards`，左侧可切换板块，右侧展示最新帖子。</p>
      </div>

      <div class="search-bar">
        <el-input
          v-model="filters.keyword"
          size="large"
          placeholder="搜索帖子标题或内容"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" size="large" :loading="loading" @click="handleSearch">搜索</el-button>
      </div>
    </section>

    <section class="layout">
      <BoardSidebar
        :boards="boards"
        :active-board-id="route.query.boardId ? Number(route.query.boardId) : null"
        :loading="boardsLoading"
        @select="handleBoardSelect"
      />

      <section class="feed">
        <div class="feed-header">
          <h2>帖子列表</h2>
          <span>{{ posts.length }} 条</span>
        </div>

        <div v-if="loading" class="state">正在加载帖子...</div>
        <div v-else-if="!posts.length" class="state">暂无帖子数据</div>

        <div v-else class="list">
          <PostCard v-for="post in posts" :key="post.postId" :post="post" />
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
.home {
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
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 28px;
  padding: 30px 32px;
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
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
  font-size: clamp(30px, 4vw, 48px);
  line-height: 1.08;
}

.sub {
  margin: 14px 0 0;
  color: var(--muted);
  line-height: 1.75;
}

.search-bar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
}

.layout {
  width: min(1200px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.feed {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feed-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
}

.feed-header h2 {
  margin: 0;
  font-size: 22px;
}

.feed-header span {
  color: var(--muted);
}

.list {
  display: grid;
  gap: 16px;
}

.state {
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 20px;
  padding: 24px;
  color: var(--muted);
  text-align: center;
}

@media (max-width: 1024px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .hero {
    padding: 24px;
  }

  .search-bar {
    grid-template-columns: 1fr;
  }
}
</style>
