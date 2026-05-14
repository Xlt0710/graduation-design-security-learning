<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = ref(!!route.params.id && route.params.id !== 'create')
const generating = ref(false)

const form = ref({
  title: '', vulnerabilityType: '', riskLevel: '中危',
  description: '', reproduceSteps: '', impact: '', repairSuggestion: ''
})
const genForm = ref({ labId: null, notes: '' })

onMounted(async () => {
  if (isEdit.value) {
    const res = await api.get(`/reports/${route.params.id}`)
    Object.assign(form.value, res.data)
  }
})

async function save() {
  try {
    if (isEdit.value) {
      await api.put(`/reports/${route.params.id}`, form.value)
    } else {
      await api.post('/reports', form.value)
    }
    ElMessage.success('保存成功')
    router.push('/reports')
  } catch (e) { /* handled */ }
}

async function generateAI() {
  generating.value = true
  try {
    const res = await api.post('/ai/reports/generate', genForm.value)
    Object.assign(form.value, res.data)
    ElMessage.success('AI报告已生成')
  } catch (e) { /* handled */ }
  finally { generating.value = false }
}
</script>

<template>
  <div class="page-container">
    <router-link to="/reports" class="back-link">&lt;- 返回报告列表</router-link>
    <div class="page-header">
      <h1><span class="prompt">&gt;</span> {{ isEdit ? '编辑报告' : '新建报告' }}</h1>
    </div>

    <!-- AI Generate -->
    <div class="ai-gen-box" v-if="!isEdit">
      <h3>AI 快速生成</h3>
      <div class="gen-row">
        <el-input v-model="genForm.labId" placeholder="靶场ID (可选)" size="small" style="width:160px" />
        <el-input v-model="genForm.notes" placeholder="补充备注 (可选)" size="small" style="flex:1" />
        <el-button type="success" @click="generateAI" :loading="generating" size="small">
          AI 生成报告
        </el-button>
      </div>
    </div>

    <div class="form-grid">
      <div class="form-group">
        <label>$ TITLE</label>
        <el-input v-model="form.title" placeholder="报告标题" />
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>$ TYPE</label>
          <el-select v-model="form.vulnerabilityType" placeholder="漏洞类型">
            <el-option label="SQL注入" value="SQL_INJECTION" />
            <el-option label="XSS" value="XSS" />
            <el-option label="CSRF" value="CSRF" />
          </el-select>
        </div>
        <div class="form-group">
          <label>$ RISK</label>
          <el-select v-model="form.riskLevel" placeholder="风险等级">
            <el-option label="高危" value="高危" />
            <el-option label="中危" value="中危" />
            <el-option label="低危" value="低危" />
          </el-select>
        </div>
      </div>
      <div class="form-group">
        <label>$ DESCRIPTION</label>
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="漏洞描述" />
      </div>
      <div class="form-group">
        <label>$ REPRODUCE_STEPS</label>
        <el-input v-model="form.reproduceSteps" type="textarea" :rows="4" placeholder="复现步骤" />
      </div>
      <div class="form-group">
        <label>$ IMPACT</label>
        <el-input v-model="form.impact" type="textarea" :rows="3" placeholder="影响分析" />
      </div>
      <div class="form-group">
        <label>$ REPAIR_SUGGESTION</label>
        <el-input v-model="form.repairSuggestion" type="textarea" :rows="4" placeholder="修复建议" />
      </div>
    </div>

    <el-button type="primary" @click="save" size="large" class="save-btn">&gt; 保存报告</el-button>
  </div>
</template>

<style scoped>
.page-container { max-width: 800px; }
.back-link { font-family: var(--font-mono); font-size: 12px; color: var(--text-muted); display: block; margin-bottom: 16px; }
.page-header h1 { font-family: var(--font-mono); font-size: 18px; display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
.prompt { color: var(--accent-green); }

.ai-gen-box { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); padding: 16px 20px; margin-bottom: 24px; }
.ai-gen-box h3 { font-family: var(--font-mono); font-size: 13px; margin-bottom: 10px; color: var(--accent-green); }
.gen-row { display: flex; gap: 10px; }

.form-grid { display: flex; flex-direction: column; gap: 16px; margin-bottom: 24px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-group label { font-family: var(--font-mono); font-size: 11px; color: var(--accent-green); letter-spacing: 1px; }
.save-btn { width: 100%; height: 48px !important; font-family: var(--font-mono) !important; }
</style>
