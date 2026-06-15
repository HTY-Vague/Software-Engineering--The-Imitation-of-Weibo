<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getLikeCount, getLikeStatus, like, unlike } from '../../services/modules/like'
import { useAuthStore } from '../../stores/auth'

const props = defineProps({
  targetType: {
    type: Number,
    required: true,
  },
  targetId: {
    type: [Number, String],
    required: true,
  },
})

const emit = defineEmits(['changed'])
const authStore = useAuthStore()

const liked = ref(false)
const count = ref(0)
const loading = ref(false)

const isAuthenticated = computed(() => authStore.isAuthenticated)

const loadState = async () => {
  loading.value = true
  try {
    const [status, total] = await Promise.all([
      getLikeStatus({ targetType: props.targetType, targetId: Number(props.targetId) }),
      getLikeCount({ targetType: props.targetType, targetId: Number(props.targetId) }),
    ])
    liked.value = Boolean(status)
    count.value = Number(total) || 0
  } catch (error) {
    ElMessage.error(error?.message || '点赞状态加载失败')
  } finally {
    loading.value = false
  }
}

const handleToggle = async () => {
  if (!isAuthenticated.value) {
    ElMessage.warning('请先登录后再点赞')
    return
  }

  try {
    if (liked.value) {
      await unlike({ targetType: props.targetType, targetId: Number(props.targetId) })
      liked.value = false
      count.value = Math.max(0, count.value - 1)
    } else {
      await like({ targetType: props.targetType, targetId: Number(props.targetId) })
      liked.value = true
      count.value += 1
    }
    emit('changed')
  } catch (error) {
    ElMessage.error(error?.message || '点赞操作失败')
  }
}

watch(
  () => [props.targetType, props.targetId],
  () => {
    loadState()
  },
  { immediate: true }
)

defineExpose({ loadState })
</script>

<template>
  <el-button :loading="loading" :type="liked ? 'primary' : 'default'" plain @click="handleToggle">
    {{ liked ? '已点赞' : '点赞' }} · {{ count }}
  </el-button>
</template>
