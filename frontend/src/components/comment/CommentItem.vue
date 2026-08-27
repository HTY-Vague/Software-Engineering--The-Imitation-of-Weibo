<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createComment } from '../../services/modules/comment'
import { useAuthStore } from '../../stores/auth'
import { formatDateTime } from '../../utils/time'
import LikeButton from '../like/LikeButton.vue'

const props = defineProps({
  comment: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['replied'])

const authStore = useAuthStore()

const previewChildren = computed(() => props.comment?.children || [])

const displayName = computed(
  () => props.comment?.nickname || props.comment?.username || `用户 #${props.comment?.userId}`,
)

const rootParentId = computed(() =>
  props.comment?.parentCommentId ? props.comment.parentCommentId : props.comment?.commentId,
)

const replying = ref(false)
const replyContent = ref('')
const submitting = ref(false)

const handleReply = () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录后再回复')
    return
  }
  replyContent.value = `@${displayName.value} `
  replying.value = true
}

const cancelReply = () => {
  replying.value = false
  replyContent.value = ''
}

const submitReply = async () => {
  const text = replyContent.value.replace(/^@\S+\s*/, '').trim()
  if (!text) {
    ElMessage.warning('请输入回复内容')
    return
  }
  submitting.value = true
  try {
    await createComment({
      postId: Number(props.comment.postId),
      parentCommentId: Number(rootParentId.value),
      replyUserId: Number(props.comment.userId),
      content: text,
    })
    ElMessage.success('回复成功')
    cancelReply()
    emit('replied')
  } catch (error) {
    ElMessage.error(error?.message || '回复失败')
  } finally {
    submitting.value = false
  }
}

const handleChildReplied = () => emit('replied')
</script>

<template>
  <article class="comment-item">
    <div class="comment-head">
      <div>
        <strong>{{ displayName }}</strong>
        <span v-if="comment.replyUsername || comment.replyNickname" class="reply-to">
          回复 @{{ comment.replyNickname || comment.replyUsername }}
        </span>
        <span>{{ formatDateTime(comment.createTime) }}</span>
      </div>
      <LikeButton :target-type="2" :target-id="comment.commentId" />
    </div>

    <p class="content">{{ comment.content }}</p>

    <div class="comment-actions">
      <el-button link size="small" @click="handleReply">回复</el-button>
    </div>

    <div v-if="replying" class="reply-composer">
      <el-input
        v-model="replyContent"
        type="textarea"
        :rows="3"
        maxlength="500"
        show-word-limit
        :placeholder="`回复 ${displayName}...`"
      />
      <div class="reply-actions">
        <el-button size="small" @click="cancelReply">取消</el-button>
        <el-button size="small" type="primary" :loading="submitting" @click="submitReply">发送</el-button>
      </div>
    </div>

    <div v-if="previewChildren.length" class="children">
      <CommentItem
        v-for="child in previewChildren"
        :key="child.commentId"
        :comment="child"
        @replied="handleChildReplied"
      />
    </div>
  </article>
</template>

<style scoped>
.comment-item {
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 16px 18px;
  background: #fff;
}

.comment-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.comment-head strong {
  display: block;
  font-size: 15px;
}

.comment-head span {
  display: inline-block;
  margin-top: 4px;
  color: var(--muted);
  font-size: 12px;
}

.reply-to {
  margin-left: 6px;
  color: var(--accent-strong);
}

.content {
  margin: 12px 0 0;
  color: var(--text);
  line-height: 1.75;
  white-space: pre-wrap;
}

.comment-actions {
  margin-top: 10px;
}

.reply-composer {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.children {
  margin-top: 14px;
  padding-left: 16px;
  border-left: 2px solid rgba(227, 76, 47, 0.14);
  display: grid;
  gap: 12px;
}
</style>