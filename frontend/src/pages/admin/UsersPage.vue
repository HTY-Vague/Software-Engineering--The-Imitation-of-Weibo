<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getUsers, updateUserStatus } from '../../services/modules/admin'

const loading = ref(false)
const users = ref([])

const total = computed(() => users.value.length)

const normalizeList = (payload) => {
  if (Array.isArray(payload)) {
    return payload
  }

  return payload?.list || payload?.records || []
}

const loadUsers = async () => {
  loading.value = true
  try {
    users.value = normalizeList(await getUsers())
  } catch (error) {
    ElMessage.error(error?.message || '用户列表加载失败')
    users.value = []
  } finally {
    loading.value = false
  }
}

const statusLabel = (status) => {
  if (String(status) === '1') return '正常'
  if (String(status) === '0') return '禁用'
  return String(status ?? '-')
}

const statusTagType = (status) => (String(status) === '1' ? 'success' : 'danger')

const handleToggleStatus = async (user) => {
  const nextStatus = String(user.status) === '1' ? 0 : 1
  try {
    await updateUserStatus(user.userId, nextStatus)
    ElMessage.success('用户状态已更新')
    await loadUsers()
  } catch (error) {
    ElMessage.error(error?.message || '更新用户状态失败')
  }
}

onMounted(loadUsers)
</script>

<template>
  <main class="admin-page">
    <section class="hero">
      <div>
        <p class="eyebrow">Admin / Users</p>
        <h1>用户管理</h1>
        <p class="sub">查看全部用户并启用/禁用账号。</p>
      </div>
      <el-button type="primary" @click="loadUsers">刷新</el-button>
    </section>

    <section v-if="loading" class="state">正在加载用户列表...</section>

    <section v-else class="panel">
      <el-table :data="users" stripe border>
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column prop="role" label="角色" width="110" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :type="String(row.status) === '1' ? 'danger' : 'success'" plain @click="handleToggleStatus(row)">
              {{ String(row.status) === '1' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="footer">共 {{ total }} 位用户</div>
    </section>
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
}
</style>
