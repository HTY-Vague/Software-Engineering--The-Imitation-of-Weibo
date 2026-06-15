<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BoardSidebar from '../../components/layout/BoardSidebar.vue'
import PostCard from '../../components/post/PostCard.vue'
import { getBoards, getBoardPosts } from '../../services/modules/forum'

const route = useRoute()
const router = useRouter()

const boards = ref([])
const posts = ref([])
const loading = ref(false)
const boardsLoading = ref(false)

const currentBoardId = computed(() => Number(route.params.boardId))

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
    posts.value = normalizeList(await getBoardPosts({ boardId: currentBoardId.value }))
  } catch (error) {
    ElMessage.error(error?.message || '板块帖子加载失败')
    posts.value = []
  } finally {
    loading.value = false
  }
}

const handleBoardSelect = async (boardId) => {
  if (boardId === null) {
    await router.push('/')
    return
  }

  await router.push(`/board/${boardId}`)
}

watch(
  () => route.params.boardId,
  async () => {
    if (!Number.isNaN(currentBoardId.value)) {
      await loadPosts()
    }
  },
  { immediate: true }
)

onMounted(loadBoards)
</script>

<template>
  <main class="board-page">
    <section class="hero">
      <div>
        <p class="eyebrow">Board Timeline</p>
        <h1>板块帖子列表</h1>
        <p class="sub">当前板块 ID：{{ route.params.boardId }}</p>
      </div>
    </section>

    <section class="layout">
      <BoardSidebar
        :boards="boards"
        :active-board-id="currentBoardId"
        :loading="boardsLoading"
        @select="handleBoardSelect"
      />

      <section class="feed">
        <div class="feed-header">
          <h2>板块内帖子</h2>
          <span>{{ posts.length }} 条</span>
        </div>

        <div v-if="loading" class="state">正在加载板块帖子...</div>
        <div v-else-if="!posts.length" class="state">当前板块暂无帖子</div>

        <div v-else class="list">
          <PostCard v-for="post in posts" :key="post.postId" :post="post" />
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
.board-page {
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
</style>
