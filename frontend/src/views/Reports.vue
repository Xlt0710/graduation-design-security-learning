<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const reports = ref([])

onMounted(async () => {
  const res = await api.get('/reports')
  reports.value = res.data || []
})

async function deleteReport(id) {
  try {
    await api.delete(`/reports/${id}`)
    reports.value = reports.value.filter(r => r.id !== id)
    ElMessage.success('已删除')
  } catch (e) { /* handled */ }
}

function riskTag(level) {
  return level === '高危' ? 'danger' : level === '中危' ? 'warning' : 'info'
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h1><span class="prompt">&gt;</span> 漏洞报告</h1>
      <div class="header-actions">
        <el-button type="primary" @click="router.push('/reports/create')">+ 新建报告</el-button>
      </div>
    </div>

    <div v-if="reports.length" class="report-list">
      <div v-for="r in reports" :key="r.id" class="report-item" @click="router.push('/reports/'+r.id)">
        <div class="report-info">
          <h3>{{ r.title }}</h3>
          <div class="report-meta">
            <el-tag :type="riskTag(r.riskLevel)" size="small" effect="dark">{{ r.riskLevel || '未知' }}</el-tag>
            <span>{{ r.vulnerabilityType }}</span>
            <el-tag v-if="r.aiGenerated" size="small" type="success" effect="plain">AI生成</el-tag>
            <span class="report-date">{{ r.createdAt }}</span>
          </div>
        </div>
        <div class="report-actions">
          <el-button text type="danger" size="small" @click.stop="deleteReport(r.id)">删除</el-button>
        </div>
      </div>
    </div>

    <el-empty v-else description="暂无报告">
      <el-button type="primary" @click="router.push('/reports/create')">创建第一份报告</el-button>
    </el-empty>
  </div>
</template>

<style scoped>
.page-container { max-width: 900px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.page-header h1 { font-family: var(--font-mono); font-size: 18px; display: flex; align-items: center; gap: 8px; }
.prompt { color: var(--accent-green); }

.report-list { display: flex; flex-direction: column; gap: 10px; }
.report-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 18px 20px; background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius); cursor: pointer; transition: var(--transition);
}
.report-item:hover { border-color: var(--accent-cyan); }
.report-info h3 { font-size: 15px; margin-bottom: 8px; }
.report-meta { display: flex; align-items: center; gap: 10px; font-family: var(--font-mono); font-size: 12px; color: var(--text-muted); }
.report-date { margin-left: auto; }
</style>
