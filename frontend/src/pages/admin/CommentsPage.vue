<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteComment, getComments } from '../../services/modules/admin'
import { formatDateTime } from '../../utils/time'

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
    comments.value = normalizeList(await getComments())
  } catch (error) {
    ElMessage.error(error?.message || '评论列表加载失败')
    comments.value = []
  } finally {
    loading.value = false
  }
}

const handleDelete = async (comment) => {
  try {
    await ElMessageBox.confirm(`确认删除评论 #${comment.commentId} 吗？`, '删除评论', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteComment(comment.commentId)
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
  <main class="admin-page">
    <section class="hero">
      <div>
        <p class="eyebrow">Admin / Comments</p>
        <h1>评论管理</h1>
        <p class="sub">查看所有评论并删除违规内容。</p>
      </div>
      <el-button type="primary" @click="loadComments">刷新</el-button>
    </section>

    <section v-if="loading" class="state">正在加载评论列表...</section>

    <section v-else class="list">
      <article v-for="comment in comments" :key="comment.commentId" class="panel comment-shell">
        <div class="comment-head">
          <div>
            <h3>评论 #{{ comment.commentId }} · 帖子 #{{ comment.postId }}</h3>
            <p>{{ formatDateTime(comment.createTime) }} · 用户 #{{ comment.userId }}</p>
          </div>
          <span class="badge">{{ comment.likeCount || 0 }} 赞</span>
        </div>

        <p class="content">{{ comment.content }}</p>

        <div class="actions">
          <el-button type="danger" plain @click="handleDelete(comment)">删除</el-button>
        </div>
      </article>
      <div class="footer">共 {{ total }} 条评论</div>
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
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .comment-head {
    flex-direction: column;
  }
}
</style>
