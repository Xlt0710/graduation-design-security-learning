<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const lab = ref({})
const flag = ref('')
const submitting = ref(false)
const result = ref(null)

onMounted(async () => {
  const res = await api.get(`/labs/${route.params.id}`)
  lab.value = res.data
})

async function submitFlag() {
  if (!flag.value.trim()) { ElMessage.warning('请输入flag'); return }
  submitting.value = true
  try {
    const res = await api.post(`/labs/${route.params.id}/submit`, { flag: flag.value })
    result.value = res.data
    if (res.data.correct) {
      lab.value.solved = true
    }
  } finally { submitting.value = false }
}

async function getHint() {
  const res = await api.post(`/labs/${route.params.id}/hint`)
  ElMessage.info(res.data.hint || '暂无提示')
}

async function toggleFav() {
  if (lab.value.favorited) {
    await api.delete(`/labs/${route.params.id}/favorite`)
    lab.value.favorited = false
  } else {
    await api.post(`/labs/${route.params.id}/favorite`)
    lab.value.favorited = true
  }
}
</script>

<template>
  <div class="page-container">
    <router-link to="/labs" class="back-link">&lt;- 返回靶场列表</router-link>

    <div class="detail-header">
      <div class="header-top">
        <h1>{{ lab.title }}</h1>
        <el-button text @click="toggleFav">
          <el-icon :size="20" :color="lab.favorited ? 'var(--accent-yellow)' : 'var(--text-muted)'">
            <StarFilled v-if="lab.favorited" /><Star v-else />
          </el-icon>
        </el-button>
      </div>
      <div class="header-meta">
        <el-tag>{{ lab.vulnerabilityType }}</el-tag>
        <span>难度 {{ lab.difficulty }}/3</span>
        <span v-if="lab.solved" class="solved-tag">已通关</span>
        <span v-else-if="lab.attemptCount">{{ lab.attemptCount }} 次尝试</span>
      </div>
    </div>

    <div class="lab-desc" v-if="lab.description">
      <h3>题目描述</h3>
      <p>{{ lab.description }}</p>
    </div>

    <div class="flag-section">
      <h3><span class="prompt">&gt;</span> 提交 Flag</h3>
      <div class="flag-input">
        <el-input v-model="flag" placeholder="flag{...}" size="large" :disabled="lab.solved" />
        <el-button type="primary" @click="submitFlag" :loading="submitting" :disabled="lab.solved" size="large">
          提交
        </el-button>
        <el-button @click="getHint" :disabled="lab.hintUsed">查看提示</el-button>
      </div>

      <div v-if="result" class="result-box" :class="{ correct: result.correct }">
        <span class="result-icon">{{ result.correct ? '[+]' : '[-]' }}</span>
        {{ result.message }}
        <span class="result-count">(第{{ result.attemptCount }}次)</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container { max-width: 750px; }
.back-link { font-family: var(--font-mono); font-size: 12px; color: var(--text-muted); display: block; margin-bottom: 16px; }
.detail-header { margin-bottom: 24px; }
.header-top { display: flex; align-items: center; justify-content: space-between; }
.header-top h1 { font-size: 20px; }
.header-meta { display: flex; align-items: center; gap: 12px; margin-top: 10px; font-family: var(--font-mono); font-size: 12px; color: var(--text-muted); }
.solved-tag { color: var(--accent-green); }

.lab-desc { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); padding: 20px; margin-bottom: 24px; }
.lab-desc h3 { font-family: var(--font-mono); font-size: 14px; margin-bottom: 10px; }
.lab-desc p { font-size: 14px; color: var(--text-secondary); line-height: 1.8; }

.flag-section h3 { font-family: var(--font-mono); font-size: 14px; margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
.prompt { color: var(--accent-green); }
.flag-input { display: flex; gap: 10px; }
.flag-input .el-input { flex: 1; }
.result-box { margin-top: 16px; padding: 14px 18px; border-radius: var(--radius); font-family: var(--font-mono); font-size: 14px; background: rgba(255,71,87,0.1); border: 1px solid rgba(255,71,87,0.3); }
.result-box.correct { background: rgba(0,255,65,0.06); border-color: rgba(0,255,65,0.3); }
.result-icon { margin-right: 8px; }
.result-count { color: var(--text-muted); margin-left: 8px; font-size: 12px; }
</style>
