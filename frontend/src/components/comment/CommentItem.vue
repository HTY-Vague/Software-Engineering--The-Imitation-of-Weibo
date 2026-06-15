<script setup>
import { computed } from 'vue'
import { formatDateTime } from '../../utils/time'

const props = defineProps({
  comment: {
    type: Object,
    required: true,
  },
})

const previewChildren = computed(() => props.comment?.children || [])
</script>

<template>
  <article class="comment-item">
    <div class="comment-head">
      <div>
        <strong>{{ comment.nickname || comment.username || `用户 #${comment.userId}` }}</strong>
        <span>{{ formatDateTime(comment.createTime) }}</span>
      </div>
      <span class="like-badge">{{ comment.likeCount || 0 }} 赞</span>
    </div>

    <p class="content">{{ comment.content }}</p>

    <div v-if="previewChildren.length" class="children">
      <CommentItem v-for="child in previewChildren" :key="child.commentId" :comment="child" />
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

.content {
  margin: 12px 0 0;
  color: var(--text);
  line-height: 1.75;
  white-space: pre-wrap;
}

.like-badge {
  white-space: nowrap;
  font-size: 12px;
  color: var(--accent-strong);
  background: rgba(227, 76, 47, 0.1);
  padding: 4px 10px;
  border-radius: 999px;
}

.children {
  margin-top: 14px;
  padding-left: 16px;
  border-left: 2px solid rgba(227, 76, 47, 0.14);
  display: grid;
  gap: 12px;
}
</style>
