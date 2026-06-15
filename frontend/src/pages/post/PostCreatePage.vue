<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import RichTextEditor from '../../components/editor/RichTextEditor.vue'
import { getBoards } from '../../services/modules/forum'
import { createPost } from '../../services/modules/post'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const boardsLoading = ref(false)
const boards = ref([])

const form = reactive({
  boardId: null,
  title: '',
  content: '',
})

const rules = {
  boardId: [{ required: true, message: '请选择板块', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }],
}

const boardOptions = computed(() => boards.value || [])

const normalizeList = (payload) => {
  if (Array.isArray(payload)) {
    return payload
  }

  return payload?.list || payload?.records || []
}

const loadBoards = async () => {
  boardsLoading.value = true
  try {
    boards.value = normalizeList(await getBoards())
    const presetBoardId = typeof route.query.boardId === 'string' ? Number(route.query.boardId) : null
    if (presetBoardId) {
      form.boardId = presetBoardId
    }
  } catch (error) {
    ElMessage.error(error?.message || '板块列表加载失败')
  } finally {
    boardsLoading.value = false
  }
}

const handleSubmit = async () => {
  loading.value = true

  try {
    await formRef.value?.validate()
    const payload = {
      boardId: form.boardId,
      title: form.title.trim(),
      content: form.content,
    }

    const result = await createPost(payload)
    const postId = result?.postId || result?.id
    ElMessage.success('帖子发布成功')

    if (postId) {
      await router.replace(`/post/${postId}`)
      return
    }

    await router.replace('/')
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error?.message || '发布失败，请检查填写内容')
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadBoards)
</script>

<template>
  <main class="post-form-page">
    <section class="panel">
      <div class="header">
        <div>
          <p class="eyebrow">New Post</p>
          <h1>发布帖子</h1>
          <p class="sub">选择板块、填写标题并使用富文本编辑器编写正文。</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="板块" prop="boardId">
          <el-select v-model="form.boardId" placeholder="请选择板块" :loading="boardsLoading" filterable>
            <el-option
              v-for="board in boardOptions"
              :key="board.boardId"
              :label="board.boardName"
              :value="board.boardId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="120" show-word-limit placeholder="请输入帖子标题" />
        </el-form-item>

        <el-form-item label="正文" prop="content">
          <RichTextEditor v-model="form.content" placeholder="写下你的帖子内容" :height="460" />
        </el-form-item>

        <div class="actions">
          <el-button plain @click="router.back()">取消</el-button>
          <el-button type="primary" :loading="loading" @click="handleSubmit">发布帖子</el-button>
        </div>
      </el-form>
    </section>
  </main>
</template>

<style scoped>
.post-form-page {
  min-height: 100vh;
  padding: 32px 20px 48px;
  background:
    radial-gradient(circle at top left, rgba(227, 76, 47, 0.12), transparent 28%),
    radial-gradient(circle at bottom right, rgba(31, 26, 23, 0.08), transparent 36%),
    var(--bg);
}

.panel {
  width: min(980px, 100%);
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 28px;
  padding: 28px;
  box-shadow: var(--shadow);
}

.header {
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0 0 12px;
  color: var(--accent);
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

h1 {
  margin: 0;
  font-size: clamp(30px, 4vw, 44px);
}

.sub {
  margin: 14px 0 0;
  color: var(--muted);
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

:deep(.el-select) {
  width: 100%;
}

@media (max-width: 640px) {
  .panel {
    padding: 22px;
  }

  .actions {
    flex-direction: column-reverse;
  }

  .actions .el-button {
    width: 100%;
  }
}
</style>
