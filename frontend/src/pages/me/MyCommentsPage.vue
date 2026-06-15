<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteComment } from '../../services/modules/comment'
import { getMyComments } from '../../services/modules/user'
import { formatDateTime } from '../../utils/time'

const router = useRouter()
const loading = ref(false)
const comments = ref([])

const total = computed(() => comments.value.length)

const normalizeList = (payload) => {
  if (Array.isArray(payload)) {
    return payload
  }

  return payload?.list || payload?.records || []
}

const loadComments = async () => {
  loading.value = true
  try {
    comments.value = normalizeList(await getMyComments())
  } catch (error) {
    ElMessage.error(error?.message || '我的评论加载失败')
    comments.value = []
  } finally {
    loading.value = false
  }
}

const handleGoPost = (postId) => {
  router.push(`/post/${postId}`)
}

const handleDelete = async (commentId) => {
  try {
    await ElMessageBox.confirm('确认删除这条评论吗？删除后无法恢复。', '删除评论', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteComment({ commentId })
    ElMessage.success('评论已删除')
    await loadComments()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.message || '删除失败')
    }
  }
}

onMounted(loadComments)
</script>

<template>
  <main class="comments-page">
    <section class="hero">
      <div>
        <p class="eyebrow">My Comments</p>
        <h1>我的评论</h1>
        <p class="sub">这里展示你发表过的评论，支持跳转到原帖和删除自己的评论。</p>
      </div>
    </section>

    <section v-if="loading" class="state">正在加载我的评论...</section>
    <section v-else-if="!comments.length" class="state">你还没有发表过评论</section>

    <section v-else class="list">
      <article v-for="comment in comments" :key="comment.commentId" class="panel comment-shell">
        <div class="comment-head">
          <div>
            <h3>帖子 #{{ comment.postId }}</h3>
            <p>{{ formatDateTime(comment.createTime) }}</p>
          </div>
          <span class="badge">{{ comment.likeCount || 0 }} 赞</span>
        </div>

        <p class="content">{{ comment.content }}</p>

        <div class="actions">
          <el-button plain @click="handleGoPost(comment.postId)">查看原帖</el-button>
          <el-button type="danger" plain @click="handleDelete(comment.commentId)">删除</el-button>
        </div>
      </article>
      <div class="footer">共 {{ total }} 条评论</div>
    </section>
  </main>
</template>

<style scoped>
.comments-page {
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

.comment-shell {
  display: grid;
  gap: 14px;
}

.comment-head {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: flex-start;
}

.comment-head h3 {
  margin: 0;
  font-size: 18px;
}

.comment-head p {
  margin: 6px 0 0;
  color: var(--muted);
}

.content {
  margin: 0;
  line-height: 1.8;
  color: var(--text);
  white-space: pre-wrap;
}

.badge {
  white-space: nowrap;
  color: var(--accent-strong);
  background: rgba(227, 76, 47, 0.1);
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
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
  .comment-head {
    flex-direction: column;
  }

  .actions {
    justify-content: stretch;
  }

  .actions .el-button {
    flex: 1;
  }
}
</style>
