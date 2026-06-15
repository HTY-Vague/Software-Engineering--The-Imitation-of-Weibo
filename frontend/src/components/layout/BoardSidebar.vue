<script setup>
const props = defineProps({
  boards: {
    type: Array,
    default: () => [],
  },
  activeBoardId: {
    type: [Number, String, null],
    default: null,
  },
})

const emit = defineEmits(['select'])

const handleSelect = (boardId) => {
  emit('select', boardId)
}
</script>

<template>
  <aside class="sidebar">
    <div class="header">
      <h3>板块列表</h3>
      <span>{{ boards.length }}</span>
    </div>

    <button
      type="button"
      class="board-item"
      :class="{ active: activeBoardId === null }"
      @click="handleSelect(null)"
    >
      <span class="name">全部板块</span>
      <span class="desc">查看全部帖子</span>
    </button>

    <button
      v-for="board in boards"
      :key="board.boardId"
      type="button"
      class="board-item"
      :class="{ active: String(activeBoardId) === String(board.boardId) }"
      @click="handleSelect(board.boardId)"
    >
      <span class="name">{{ board.boardName }}</span>
      <span class="desc">{{ board.description || '暂无简介' }}</span>
    </button>
  </aside>
</template>

<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 24px;
  padding: 20px;
  box-shadow: var(--shadow);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h3 {
  margin: 0;
  font-size: 18px;
}

.header span {
  color: var(--muted);
  font-size: 13px;
}

.board-item {
  border: 1px solid transparent;
  background: #f7f4f0;
  border-radius: 18px;
  text-align: left;
  padding: 14px 16px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: all 0.18s ease;
}

.board-item:hover,
.board-item.active {
  border-color: rgba(227, 76, 47, 0.22);
  background: rgba(227, 76, 47, 0.08);
}

.name {
  font-weight: 800;
  color: var(--text);
}

.desc {
  color: var(--muted);
  font-size: 13px;
  line-height: 1.5;
}
</style>
