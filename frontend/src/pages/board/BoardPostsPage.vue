<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <main class="page">
    <section class="card">
      <h1>Board Posts</h1>
      <p>Board ID: {{ route.params.boardId }}</p>
      <p>TODO: Connect /forum/board/posts</p>
    </section>
  </main>
</template>

<style scoped>
.page {
  padding: 48px 24px;
}

.card {
  max-width: 860px;
  margin: 0 auto;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 32px;
  box-shadow: var(--shadow);
}

h1 {
  margin: 0 0 12px;
  font-size: 28px;
}

p {
  margin: 0 0 8px;
  color: var(--muted);
}
</style>
