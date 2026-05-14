<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const courses = ref([])
const difficulty = ref(null)
const diffOptions = [
  { label: '全部', value: null },
  { label: '简单', value: 1 },
  { label: '中等', value: 2 },
  { label: '困难', value: 3 },
]

async function fetchCourses() {
  const res = await api.get('/courses', { params: { difficulty: difficulty.value } })
  courses.value = res.data
}

function diffTag(d) {
  return d === 1 ? '简单' : d === 2 ? '中等' : '困难'
}
function diffType(d) {
  return d === 1 ? 'success' : d === 2 ? 'warning' : 'danger'
}

onMounted(fetchCourses)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h1><span class="prompt">&gt;</span> 课程列表</h1>
      <el-radio-group v-model="difficulty" @change="fetchCourses" size="small">
        <el-radio-button v-for="o in diffOptions" :key="o.value" :value="o.value">{{ o.label }}</el-radio-button>
      </el-radio-group>
    </div>

    <div class="course-grid" v-if="courses.length">
      <div v-for="c in courses" :key="c.id" class="course-card" @click="router.push('/courses/'+c.id)">
        <div class="course-cover">
          <div class="cover-placeholder">
            <el-icon :size="36"><Reading /></el-icon>
          </div>
          <el-tag :type="diffType(c.difficulty)" size="small" effect="dark" class="diff-badge">
            {{ diffTag(c.difficulty) }}
          </el-tag>
        </div>
        <div class="course-body">
          <h3 class="course-title">{{ c.title }}</h3>
          <p class="course-desc">{{ c.description || '暂无描述' }}</p>
          <div class="course-meta">
            <span>{{ c.chapterCount }} 章节</span>
            <el-progress :percentage="Math.round(c.progress || 0)" :stroke-width="4" :show-text="false" />
          </div>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无课程" />
  </div>
</template>

<style scoped>
.page-container { max-width: 1000px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.page-header h1 { font-family: var(--font-mono); font-size: 18px; display: flex; align-items: center; gap: 8px; }
.prompt { color: var(--accent-green); }

.course-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; }

.course-card {
  background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius);
  cursor: pointer; overflow: hidden; transition: all var(--transition);
}
.course-card:hover { border-color: var(--accent-cyan); box-shadow: var(--glow-cyan); transform: translateY(-2px); }

.course-cover { height: 120px; background: var(--bg-secondary); position: relative; display: flex; align-items: center; justify-content: center; }
.cover-placeholder { color: var(--text-muted); }
.diff-badge { position: absolute; top: 12px; right: 12px; }

.course-body { padding: 16px 20px 20px; }
.course-title { font-size: 15px; margin-bottom: 8px; }
.course-desc { font-size: 13px; color: var(--text-secondary); line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.course-meta { display: flex; align-items: center; gap: 12px; margin-top: 12px; font-family: var(--font-mono); font-size: 12px; color: var(--text-muted); }
.course-meta .el-progress { flex: 1; }
</style>
