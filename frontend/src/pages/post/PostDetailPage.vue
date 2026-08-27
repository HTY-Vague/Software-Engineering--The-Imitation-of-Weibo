<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import LikeButton from '../../components/like/LikeButton.vue'
import CommentSection from '../../components/comment/CommentSection.vue'
import { getBoards } from '../../services/modules/forum'
import { getPostDetail } from '../../services/modules/post'
import { formatDateTime } from '../../utils/time'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const boards = ref([])
const post = ref(null)

const currentPostId = computed(() => Number(route.params.postId))

const normalizeList = (payload) => {
  if (Array.isArray(payload)) {
    return payload
  }

  return payload?.list || payload?.records || []
}

const boardName = computed(() => {
  const board = boards.value.find((item) => Number(item.boardId) === Number(post.value?.boardId))
  return board?.boardName || '未分类'
})

const loadMeta = async () => {
  try {
    boards.value = normalizeList(await getBoards())
  } catch {
    // 板块名失败不影响主内容展示
  }
}

const loadDetail = async () => {
  loading.value = true
  try {
    post.value = await getPostDetail({ postId: currentPostId.value })
  } catch (error) {
    ElMessage.error(error?.message || '帖子详情加载失败')
    post.value = null
  } finally {
    loading.value = false
  }
}

const handleCommentsChanged = async () => {
  await loadDetail()
}

watch(
  () => route.params.postId,
  async () => {
    await loadDetail()
  },
  { immediate: true }
)

onMounted(loadMeta)
</script>

<template>
  <main class="post-page">
    <section v-if="loading" class="panel state">正在加载帖子详情...</section>

    <template v-else-if="post">
      <section class="panel article">
        <div class="top-row">
          <span class="tag board">{{ boardName }}</span>
          <span v-if="post.isTop" class="tag hot">置顶</span>
          <span class="tag">#{{ post.postId }}</span>
        </div>

        <h1>{{ post.title }}</h1>

        <div class="meta">
          <span>作者 {{ post.authorNickname || post.authorUsername || `用户 #${post.authorId}` }}</span>
          <span>发布时间 {{ formatDateTime(post.createTime) }}</span>
          <span>{{ post.viewCount || 0 }} 浏览</span>
          <span>{{ post.commentCount || 0 }} 评论</span>
          <span>{{ post.likeCount || 0 }} 点赞</span>
        </div>

        <div class="actions">
          <LikeButton :target-type="1" :target-id="post.postId" @changed="loadDetail" />
          <el-button plain @click="router.push({ path: '/board/' + post.boardId })">查看板块</el-button>
        </div>

        <article class="content" v-html="post.content"></article>
      </section>

      <section class="panel">
        <CommentSection :post-id="post.postId" @changed="handleCommentsChanged" />
      </section>
    </template>

    <section v-else class="panel state">
      <h2>帖子不存在或已被删除</h2>
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </section>
  </main>
</template>

<style scoped>
.post-page {
  min-height: 100vh;
  padding: 32px 20px 48px;
  background:
    radial-gradient(circle at top left, rgba(227, 76, 47, 0.12), transparent 28%),
    radial-gradient(circle at bottom right, rgba(31, 26, 23, 0.08), transparent 36%),
    var(--bg);
}

.panel {
  width: min(980px, 100%);
  margin: 0 auto 18px;
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 28px;
  padding: 28px;
  box-shadow: var(--shadow);
}

.state {
  text-align: center;
  color: var(--muted);
}

.top-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.tag {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 4px 10px;
  background: #f4efe9;
  color: var(--muted);
  font-size: 12px;
  font-weight: 700;
}

.tag.hot {
  background: rgba(227, 76, 47, 0.12);
  color: var(--accent-strong);
}

.tag.board {
  background: rgba(31, 26, 23, 0.08);
  color: var(--text);
}

h1 {
  margin: 0;
  font-size: clamp(30px, 4vw, 44px);
  line-height: 1.15;
}

.meta {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: var(--muted);
  font-size: 13px;
}

.actions {
  margin-top: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.content {
  margin-top: 22px;
  padding-top: 22px;
  border-top: 1px solid var(--border);
  line-height: 1.9;
  color: var(--text);
  overflow-wrap: anywhere;
}

:deep(.content img) {
  max-width: 100%;
  border-radius: 16px;
}

@media (max-width: 640px) {
  .panel {
    padding: 22px;
  }
}
</style>
