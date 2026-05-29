<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<template>
  <main class="not-found">
    <section class="card">
      <h1>Page not found</h1>
      <p>The page you are looking for does not exist.</p>
      <button type="button" class="back" @click="goHome">Back to home</button>
    </section>
  </main>
</template>

<style scoped>
.not-found {
  padding: 48px 24px;
}

.card {
  max-width: 560px;
  margin: 0 auto;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 32px;
  box-shadow: var(--shadow);
  text-align: center;
}

h1 {
  margin: 0 0 12px;
  font-size: 28px;
}

p {
  margin: 0 0 20px;
  color: var(--muted);
}

.back {
  border: none;
  border-radius: 999px;
  padding: 10px 18px;
  background: var(--accent);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

.back:hover {
  background: var(--accent-strong);
}
</style>
