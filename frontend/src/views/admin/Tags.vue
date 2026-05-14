<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'
import { ElMessage } from 'element-plus'

const tags = ref([])
const dialogVisible = ref(false)
const form = ref({ tagName: '', tagType: '' })

onMounted(fetchTags)
async function fetchTags() {
  const res = await api.get('/admin/tags')
  tags.value = res.data || []
}

async function save() {
  try {
    await api.post('/admin/tags', form.value)
    dialogVisible.value = false
    ElMessage.success('创建成功')
    fetchTags()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  }
}

async function remove(id) {
  try {
    await api.delete(`/admin/tags/${id}`)
    ElMessage.success('已删除')
    fetchTags()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '删除失败')
  }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h1><span class="prompt">&gt;</span> 标签管理</h1>
      <el-button type="primary" @click="dialogVisible = true">+ 新建标签</el-button>
    </div>

    <el-table :data="tags" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="tagName" label="标签名" />
      <el-table-column prop="tagType" label="类型" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button text type="danger" size="small" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新建标签" width="420px">
      <div class="dialog-form">
        <label>$ TAG_NAME</label>
        <el-input v-model="form.tagName" placeholder="标签名" />
        <label>$ TAG_TYPE</label>
        <el-input v-model="form.tagType" placeholder="标签类型" />
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { max-width: 800px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.page-header h1 { font-family: var(--font-mono); font-size: 18px; display: flex; align-items: center; gap: 8px; }
.prompt { color: var(--accent-green); }
.dialog-form { display: flex; flex-direction: column; gap: 12px; }
.dialog-form label { font-family: var(--font-mono); font-size: 11px; color: var(--accent-green); letter-spacing: 1px; }
</style>
