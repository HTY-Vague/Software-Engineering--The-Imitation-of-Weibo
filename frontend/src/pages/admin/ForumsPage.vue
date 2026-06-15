<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createForum, deleteForum, getForums, sortForums, updateForum, updateForumStatus } from '../../services/modules/admin'

const loading = ref(false)
const saving = ref(false)
const forums = ref([])
const dialogVisible = ref(false)
const editingBoardId = ref(null)
const formRef = ref()

const form = reactive({
  boardName: '',
  description: '',
  sortNum: 0,
  status: 1,
})

const rules = {
  boardName: [{ required: true, message: '请输入板块名称', trigger: 'blur' }],
}

const total = computed(() => forums.value.length)

const normalizeList = (payload) => {
  if (Array.isArray(payload)) {
    return payload
  }

  return payload?.list || payload?.records || []
}

const resetForm = () => {
  editingBoardId.value = null
  form.boardName = ''
  form.description = ''
  form.sortNum = 0
  form.status = 1
}

const loadForums = async () => {
  loading.value = true
  try {
    forums.value = normalizeList(await getForums())
  } catch (error) {
    ElMessage.error(error?.message || '板块列表加载失败')
    forums.value = []
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (board) => {
  editingBoardId.value = board.boardId
  form.boardName = board.boardName || ''
  form.description = board.description || ''
  form.sortNum = board.sortNum ?? 0
  form.status = Number(board.status ?? 1)
  dialogVisible.value = true
}

const handleSave = async () => {
  saving.value = true
  try {
    await formRef.value?.validate()
    const payload = {
      boardName: form.boardName.trim(),
      description: form.description.trim(),
      sortNum: Number(form.sortNum) || 0,
      status: Number(form.status) || 1,
    }
    if (editingBoardId.value) {
      await updateForum(editingBoardId.value, payload)
      ElMessage.success('板块已更新')
    } else {
      await createForum(payload)
      ElMessage.success('板块已新增')
    }
    dialogVisible.value = false
    await loadForums()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error?.message || '保存板块失败')
    }
  } finally {
    saving.value = false
  }
}

const handleToggleStatus = async (board) => {
  const nextStatus = String(board.status) === '1' ? 0 : 1
  try {
    await updateForumStatus(board.boardId, nextStatus)
    ElMessage.success('板块状态已更新')
    await loadForums()
  } catch (error) {
    ElMessage.error(error?.message || '更新板块状态失败')
  }
}

const handleDelete = async (board) => {
  try {
    await ElMessageBox.confirm(`确认删除板块「${board.boardName}」吗？`, '删除板块', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteForum(board.boardId)
    ElMessage.success('板块已删除')
    await loadForums()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.message || '删除失败')
    }
  }
}

const moveBoard = (index, delta) => {
  const nextIndex = index + delta
  if (nextIndex < 0 || nextIndex >= forums.value.length) {
    return
  }
  const updated = [...forums.value]
  const [moved] = updated.splice(index, 1)
  updated.splice(nextIndex, 0, moved)
  forums.value = updated
}

const handleSaveSort = async () => {
  try {
    await sortForums(forums.value.map((item) => Number(item.boardId)))
    ElMessage.success('板块顺序已保存')
    await loadForums()
  } catch (error) {
    ElMessage.error(error?.message || '保存排序失败')
  }
}

onMounted(loadForums)
</script>

<template>
  <main class="admin-page">
    <section class="hero">
      <div>
        <p class="eyebrow">Admin / Forums</p>
        <h1>板块管理</h1>
        <p class="sub">新增、编辑、启停、删除和排序板块。</p>
      </div>
      <div class="hero-actions">
        <el-button plain @click="handleSaveSort">保存排序</el-button>
        <el-button type="primary" @click="openCreateDialog">新增板块</el-button>
      </div>
    </section>

    <section v-if="loading" class="state">正在加载板块列表...</section>

    <section v-else class="panel">
      <el-table :data="forums" stripe border>
        <el-table-column prop="boardId" label="ID" width="80" />
        <el-table-column prop="boardName" label="板块名称" min-width="160" />
        <el-table-column prop="description" label="简介" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sortNum" label="排序" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="String(row.status) === '1' ? 'success' : 'danger'">
              {{ String(row.status) === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="250" fixed="right">
          <template #default="{ row, $index }">
            <el-button size="small" plain @click="moveBoard($index, -1)">上移</el-button>
            <el-button size="small" plain @click="moveBoard($index, 1)">下移</el-button>
            <el-button size="small" plain @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" :type="String(row.status) === '1' ? 'danger' : 'success'" plain @click="handleToggleStatus(row)">
              {{ String(row.status) === '1' ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="footer">共 {{ total }} 个板块</div>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingBoardId ? '编辑板块' : '新增板块'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="板块名称" prop="boardName">
          <el-input v-model="form.boardName" placeholder="请输入板块名称" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入板块简介" />
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="form.sortNum" :min="0" :step="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<style scoped>
.admin-page {
  min-height: 100vh;
  padding: 32px 20px 48px;
  background:
    radial-gradient(circle at top left, rgba(227, 76, 47, 0.12), transparent 28%),
    radial-gradient(circle at bottom right, rgba(31, 26, 23, 0.08), transparent 36%),
    var(--bg);
}

.hero {
  width: min(1200px, 100%);
  margin: 0 auto 24px;
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 28px;
  padding: 28px 32px;
  box-shadow: var(--shadow);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
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

.hero-actions {
  display: flex;
  gap: 12px;
}

.panel {
  width: min(1200px, 100%);
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 28px;
  padding: 20px;
  box-shadow: var(--shadow);
}

.state {
  width: min(1200px, 100%);
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(225, 217, 209, 0.9);
  border-radius: 24px;
  padding: 24px;
  box-shadow: var(--shadow);
  color: var(--muted);
  text-align: center;
}

.footer {
  color: var(--muted);
  text-align: center;
  padding-top: 12px;
}

@media (max-width: 720px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
