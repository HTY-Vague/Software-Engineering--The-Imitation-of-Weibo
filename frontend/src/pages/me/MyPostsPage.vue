<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PostCard from '../../components/post/PostCard.vue'
import { deleteMyPost, getMyPosts } from '../../services/modules/user'

const router = useRouter()
const loading = ref(false)
const posts = ref([])

const total = computed(() => posts.value.length)

const normalizeList = (payload) => {
  if (Array.isArray(payload)) {
    return payload
  }

  return payload?.list || payload?.records || []
}

const loadPosts = async () => {
  loading.value = true
  try {
    posts.value = normalizeList(await getMyPosts())
  } catch (error) {
    ElMessage.error(error?.message || '我的帖子加载失败')
    posts.value = []
  } finally {
    loading.value = false
  }
}

const handleEdit = (postId) => {
  router.push(`/post/${postId}/edit`)
}

const handleDelete = async (postId) => {
  try {
    await ElMessageBox.confirm('确认删除这篇帖子吗？删除后无法恢复。', '删除帖子', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteMyPost({ postId })
    ElMessage.success('帖子已删除')
    await loadPosts()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.message || '删除失败')
    }
  }
}

onMounted(loadPosts)
</script>

<template>
  <main class="posts-page">
    <section class="hero">
      <div>
        <p class="eyebrow">My Posts</p>
        <h1>我的帖子</h1>
        <p class="sub">这里显示你自己发布的帖子，可以继续编辑或删除。</p>
      </div>
      <el-button type="primary" @click="router.push('/post/create')">发布新帖</el-button>
    </section>

    <section v-if="loading" class="state">正在加载我的帖子...</section>
    <section v-else-if="!posts.length" class="state">你还没有发布过帖子</section>

    <section v-else class="list">
      <article v-for="post in posts" :key="post.postId" class="panel post-shell">
        <PostCard :post="post" />
        <div class="actions">
          <el-button plain @click="handleEdit(post.postId)">编辑</el-button>
          <el-button type="danger" plain @click="handleDelete(post.postId)">删除</el-button>
        </div>
      </article>
      <div class="footer">共 {{ total }} 篇帖子</div>
    </section>
  </main>
</template>

<style scoped>
.posts-page {
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

  .actions {
    justify-content: stretch;
  }

  .actions .el-button {
    flex: 1;
  }
}
</style>
