<script setup>
</script>

<template>
  <main class="page">
    <section class="card">
      <h1>Admin - Posts</h1>
      <p>TODO: Connect /admin/posts/{postId} and /admin/posts/{postId}/top</p>
    </section>
  </main>
</template>

<style scoped>
.page {
  padding: 48px 24px;
}

.card {
  max-width: 920px;
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
  margin: 0;
  color: var(--muted);
}
</style>
