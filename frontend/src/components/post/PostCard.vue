<script setup>
import { computed } from 'vue'
import { formatDateTime } from '../../utils/time'

const props = defineProps({
  post: {
    type: Object,
    required: true,
  },
  boardName: {
    type: String,
    default: '',
  },
})

const preview = computed(() => {
  const content = props.post?.content || ''
  return content.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim().slice(0, 140)
})
</script>

<template>
  <article class="post-card">
    <div class="top-row">
      <span v-if="post.isTop" class="tag hot">置顶</span>
      <span v-if="boardName" class="tag board">{{ boardName }}</span>
      <span class="tag">#{{ post.postId }}</span>
    </div>

    <RouterLink class="title" :to="`/post/${post.postId}`">
      {{ post.title }}
    </RouterLink>

    <p class="excerpt">
      {{ preview || '暂无正文预览' }}
    </p>

    <div class="meta">
      <span>{{ formatDateTime(post.createTime) }}</span>
      <span>{{ post.viewCount || 0 }} 浏览</span>
      <span>{{ post.commentCount || 0 }} 评论</span>
      <span>{{ post.likeCount || 0 }} 点赞</span>
    </div>
  </article>
</template>

<style scoped>
.post-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 22px 24px;
  box-shadow: 0 12px 30px rgba(31, 26, 23, 0.05);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 36px rgba(31, 26, 23, 0.09);
  border-color: rgba(227, 76, 47, 0.25);
}

.top-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
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

.title {
  display: inline-block;
  font-size: 20px;
  font-weight: 800;
  line-height: 1.35;
  margin-bottom: 10px;
  color: var(--text);
}

.title:hover {
  color: var(--accent);
}

.excerpt {
  margin: 0;
  color: var(--muted);
  line-height: 1.8;
  min-height: 56px;
}

.meta {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: var(--muted);
  font-size: 13px;
}
</style>
