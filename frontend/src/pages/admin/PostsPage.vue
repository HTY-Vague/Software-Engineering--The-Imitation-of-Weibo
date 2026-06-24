<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deletePost, setPostTop } from '../../services/modules/admin'
import { getHotPosts } from '../../services/modules/statistics'
import { getBoards } from '../../services/modules/forum'
import PostCard from '../../components/post/PostCard.vue'

const router = useRouter()
const loading = ref(false)
const boardsLoading = ref(false)
const posts = ref([])
const boards = ref([])

const total = computed(() => posts.value.length)

const normalizeList = (payload) => {
  if (Array.isArray(payload)) {
    return payload
  }

  return payload?.list || payload?.records || []
}

const loadBoards = async () => {
  boardsLoading.value = true
  try {
    boards.value = normalizeList(await getBoards())
  } catch {
    boards.value = []
  } finally {
    boardsLoading.value = false
  }
}

const loadPosts = async () => {
  loading.value = true
  try {
    posts.value = normalizeList(await getHotPosts({ limit: 50 }))
  } catch (error) {
    ElMessage.error(error?.message || '帖子列表加载失败')
    posts.value = []
  } finally {
    loading.value = false
  }
}

const boardNameMap = computed(() => {
  const map = new Map()
  boards.value.forEach((item) => map.set(Number(item.boardId), item.boardName))
  return map
})

const resolveBoardName = (boardId) => boardNameMap.value.get(Number(boardId)) || ''

const handleToggleTop = async (post) => {
  const nextTop = Number(post.isTop) === 1 ? 0 : 1
  try {
    await setPostTop(post.postId, nextTop)
    ElMessage.success('置顶状态已更新')
    await loadPosts()
  } catch (error) {
    ElMessage.error(error?.message || '更新置顶失败')
  }
}

const handleDelete = async (post) => {
  try {
    await ElMessageBox.confirm(`确认删除帖子「${post.title}」吗？`, '删除帖子', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deletePost(post.postId)
    ElMessage.success('帖子已删除')
    await loadPosts()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.message || '删除失败')
    }
  }
}

const handleOpen = (postId) => router.push(`/post/${postId}`)

onMounted(async () => {
  await Promise.all([loadBoards(), loadPosts()])
})
</script>

<template>
  <main class="admin-page">
    <section class="hero">
      <div>
        <p class="eyebrow">Admin / Posts</p>
        <h1>内容管理</h1>
        <p class="sub">查看热门帖子并进行置顶或删除操作。</p>
      </div>
      <el-button type="primary" @click="loadPosts">刷新</el-button>
    </section>

    <section v-if="loading || boardsLoading" class="state">正在加载内容列表...</section>

    <section v-else class="list">
      <article v-for="post in posts" :key="post.postId" class="panel post-shell">
        <PostCard :post="post" :board-name="resolveBoardName(post.boardId)" />
        <div class="actions">
          <el-button plain @click="handleOpen(post.postId)">查看</el-button>
          <el-button plain @click="router.push(`/post/${post.postId}/edit`)">编辑</el-button>
          <el-button :type="Number(post.isTop) === 1 ? 'warning' : 'primary'" plain @click="handleToggleTop(post)">
            {{ Number(post.isTop) === 1 ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button type="danger" plain @click="handleDelete(post)">删除</el-button>
        </div>
      </article>
      <div class="footer">共 {{ total }} 篇热门帖子</div>
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

.list {
  width: min(1200px, 100%);
  margin: 0 auto;
  display: grid;
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

.post-shell {
  display: grid;
  gap: 16px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
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

.footer {
  color: var(--muted);
  text-align: center;
  padding: 8px 0;
}

@media (max-width: 720px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
