<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createComment, deleteComment, listComments } from '../../services/modules/comment'
import { useAuthStore } from '../../stores/auth'
import CommentItem from './CommentItem.vue'

const props = defineProps({
  postId: {
    type: [Number, String],
    required: true,
  },
})

const emit = defineEmits(['changed'])
const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const comments = ref([])
const content = ref('')

const canComment = computed(() => authStore.isAuthenticated)

const normalizeList = (payload) => {
  if (Array.isArray(payload)) {
    return payload
  }

  return payload?.list || payload?.records || []
}

const loadComments = async () => {
  loading.value = true
  try {
    comments.value = normalizeList(await listComments({ postId: Number(props.postId) }))
  } catch (error) {
    ElMessage.error(error?.message || '评论加载失败')
    comments.value = []
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!content.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    await createComment({
      postId: Number(props.postId),
      parentCommentId: null,
      replyUserId: null,
      content: content.value.trim(),
    })
    content.value = ''
    ElMessage.success('评论成功')
    await loadComments()
    emit('changed')
  } catch (error) {
    ElMessage.error(error?.message || '评论失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (commentId) => {
  try {
    await deleteComment({ commentId })
    ElMessage.success('评论已删除')
    await loadComments()
    emit('changed')
  } catch (error) {
    ElMessage.error(error?.message || '删除失败')
  }
}

defineExpose({ loadComments })
</script>

<template>
  <section class="comment-section">
    <div class="section-head">
      <h3>评论区</h3>
      <span>{{ comments.length }} 条评论</span>
    </div>

    <div v-if="canComment" class="composer">
      <el-input
        v-model="content"
        type="textarea"
        :rows="4"
        maxlength="500"
        show-word-limit
        placeholder="写下你的评论..."
      />
      <div class="composer-actions">
        <el-button :loading="submitting" type="primary" @click="handleSubmit">发表评论</el-button>
      </div>
    </div>

    <el-alert
      v-else
      title="登录后才能发表评论"
      type="info"
      :closable="false"
      show-icon
    />

    <div v-if="loading" class="state">正在加载评论...</div>
    <div v-else-if="!comments.length" class="state">暂无评论，来抢沙发吧</div>

    <div v-else class="list">
      <CommentItem v-for="comment in comments" :key="comment.commentId" :comment="comment" />
    </div>
  </section>
</template>

<style scoped>
.comment-section {
  display: grid;
  gap: 16px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-head h3 {
  margin: 0;
  font-size: 22px;
}

.section-head span {
  color: var(--muted);
}

.composer {
  display: grid;
  gap: 12px;
  padding: 18px;
  border: 1px solid var(--border);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.7);
}

.composer-actions {
  display: flex;
  justify-content: flex-end;
}

.list {
  display: grid;
  gap: 12px;
}

.state {
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 20px;
  padding: 20px;
  color: var(--muted);
  text-align: center;
}
</style>
