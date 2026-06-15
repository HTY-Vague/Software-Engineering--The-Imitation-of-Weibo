<script setup>
import { computed } from 'vue'
import { RouterView } from 'vue-router'
import { useRoute } from 'vue-router'
import NavHeader from './components/layout/NavHeader.vue'

const route = useRoute()
const showHeader = computed(() => route.name !== 'login' && route.name !== 'register')
</script>

<template>
  <div class="app-shell">
    <NavHeader v-if="showHeader" />
    <RouterView />
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
}
</style>
