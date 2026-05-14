<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'
import { ElMessage } from 'element-plus'

const notices = ref([])
const dialogVisible = ref(false)
const editing = ref(null)
const form = ref({ title: '', content: '', status: 1 })

onMounted(fetchNotices)
async function fetchNotices() {
  const res = await api.get('/admin/notices')
  notices.value = res.data || []
}

function openCreate() {
  editing.value = null
  form.value = { title: '', content: '', status: 1 }
  dialogVisible.value = true
}

function openEdit(n) {
  editing.value = n.id
  form.value = { title: n.title, content: n.content, status: n.status }
  dialogVisible.value = true
}

async function save() {
  try {
    if (editing.value) {
      await api.put(`/admin/notices/${editing.value}`, form.value)
    } else {
      await api.post('/admin/notices', form.value)
    }
    dialogVisible.value = false
    ElMessage.success('保存成功')
    fetchNotices()
  } catch (e) { /* handled */ }
}

async function remove(id) {
  try {
    await api.delete(`/admin/notices/${id}`)
    ElMessage.success('已删除')
    fetchNotices()
  } catch (e) { /* handled */ }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h1><span class="prompt">&gt;</span> 通知管理</h1>
      <el-button type="primary" @click="openCreate">+ 新建通知</el-button>
    </div>

    <el-table :data="notices" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button text type="danger" size="small" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑通知' : '新建通知'" width="550px">
      <div class="dialog-form">
        <label>$ TITLE</label>
        <el-input v-model="form.title" placeholder="通知标题" />
        <label>$ CONTENT</label>
        <el-input v-model="form.content" type="textarea" :rows="5" placeholder="通知内容" />
        <label>$ STATUS</label>
        <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="发布" inactive-text="草稿" />
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { max-width: 1000px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.page-header h1 { font-family: var(--font-mono); font-size: 18px; display: flex; align-items: center; gap: 8px; }
.prompt { color: var(--accent-green); }
.dialog-form { display: flex; flex-direction: column; gap: 12px; }
.dialog-form label { font-family: var(--font-mono); font-size: 11px; color: var(--accent-green); letter-spacing: 1px; }
</style>
