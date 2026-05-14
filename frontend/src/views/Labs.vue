<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const labs = ref([])
const filterType = ref('')
const filterDiff = ref(null)

async function fetchLabs() {
  const res = await api.get('/labs', { params: { vulnerabilityType: filterType.value || undefined, difficulty: filterDiff.value } })
  labs.value = res.data
}

function diffTag(d) { return d === 1 ? '简单' : d === 2 ? '中等' : d === 3 ? '困难' : '' }
function diffType(d) { return d === 1 ? 'success' : d === 2 ? 'warning' : 'danger' }

onMounted(fetchLabs)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h1><span class="prompt">&gt;</span> 靶场练习</h1>
      <div class="filters">
        <el-select v-model="filterType" placeholder="漏洞类型" clearable @change="fetchLabs" size="small" style="width:140px">
          <el-option label="SQL注入" value="SQL_INJECTION" />
          <el-option label="XSS" value="XSS" />
          <el-option label="CSRF" value="CSRF" />
        </el-select>
        <el-select v-model="filterDiff" placeholder="难度" clearable @change="fetchLabs" size="small" style="width:100px">
          <el-option label="简单" :value="1" />
          <el-option label="中等" :value="2" />
          <el-option label="困难" :value="3" />
        </el-select>
      </div>
    </div>

    <div class="lab-grid" v-if="labs.length">
      <div v-for="l in labs" :key="l.id" class="lab-card" @click="router.push('/labs/'+l.id)">
        <div class="lab-header">
          <el-tag :type="diffType(l.difficulty)" size="small" effect="dark">{{ diffTag(l.difficulty) }}</el-tag>
          <div class="lab-status" v-if="l.solved"><el-icon color="var(--accent-green)"><CircleCheckFilled /></el-icon> 已通关</div>
          <div class="lab-status trying" v-else-if="l.attemptCount > 0">尝试 {{ l.attemptCount }} 次</div>
        </div>
        <h3 class="lab-title">{{ l.title }}</h3>
        <div class="lab-meta">
          <span>{{ l.vulnerabilityType }}</span>
          <el-icon v-if="l.favorited" color="var(--accent-yellow)" :size="14"><StarFilled /></el-icon>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无靶场" />
  </div>
</template>

<style scoped>
.page-container { max-width: 1000px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.page-header h1 { font-family: var(--font-mono); font-size: 18px; display: flex; align-items: center; gap: 8px; }
.prompt { color: var(--accent-green); }
.filters { display: flex; gap: 8px; }

.lab-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.lab-card {
  background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius);
  padding: 20px; cursor: pointer; transition: all var(--transition);
}
.lab-card:hover { border-color: var(--accent-green); box-shadow: var(--glow-green); transform: translateY(-2px); }
.lab-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.lab-status { font-family: var(--font-mono); font-size: 11px; color: var(--accent-green); display: flex; align-items: center; gap: 4px; }
.lab-status.trying { color: var(--text-muted); }
.lab-title { font-size: 15px; margin-bottom: 12px; }
.lab-meta { display: flex; align-items: center; justify-content: space-between; font-family: var(--font-mono); font-size: 12px; color: var(--text-muted); }
</style>
