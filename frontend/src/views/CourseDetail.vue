<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const course = ref({})
const chapters = ref([])
const progress = ref(0)

onMounted(async () => {
  const res = await api.get(`/courses/${route.params.id}`)
  course.value = res.data
  chapters.value = res.data.chapters || []
  progress.value = Math.round(res.data.progress || 0)
})

async function completeChapter(chapterId) {
  try {
    await api.post(`/chapters/${chapterId}/complete`)
    chapters.value = chapters.value.map(c => c.id === chapterId ? { ...c, completed: true } : c)
    const res = await api.get(`/courses/${route.params.id}`)
    progress.value = Math.round(res.data.progress || 0)
    ElMessage.success('章节已完成')
  } catch (e) { /* handled by interceptor */ }
}
</script>

<template>
  <div class="page-container">
    <div class="detail-header">
      <router-link to="/courses" class="back-link">&lt;- 返回课程列表</router-link>
      <h1>{{ course.title }}</h1>
      <p class="course-desc">{{ course.description }}</p>
      <div class="progress-bar">
        <el-progress :percentage="progress" :stroke-width="8" color="var(--accent-green)" />
        <span class="progress-text">{{ progress }}%</span>
      </div>
    </div>

    <div class="chapter-list">
      <h2><span class="prompt">&gt;</span> 章节列表</h2>
      <div v-for="ch in chapters" :key="ch.id" class="chapter-item" :class="{ completed: ch.completed }">
        <div class="chapter-info">
          <span class="chapter-order">{{ String(ch.sortOrder).padStart(2, '0') }}</span>
          <span class="chapter-title">{{ ch.title }}</span>
          <el-tag v-if="ch.completed" type="success" size="small" effect="dark">已完成</el-tag>
        </div>
        <el-button v-if="!ch.completed" type="primary" size="small" @click="completeChapter(ch.id)">
          标记完成
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container { max-width: 800px; }
.detail-header { margin-bottom: 32px; }
.back-link { font-family: var(--font-mono); font-size: 12px; color: var(--text-muted); display: block; margin-bottom: 12px; }
.detail-header h1 { font-size: 22px; margin-bottom: 8px; }
.course-desc { font-size: 14px; color: var(--text-secondary); line-height: 1.8; }
.progress-bar { display: flex; align-items: center; gap: 12px; margin-top: 16px; }
.progress-bar .el-progress { flex: 1; }
.progress-text { font-family: var(--font-mono); font-size: 14px; color: var(--accent-green); }

.chapter-list h2 { font-family: var(--font-mono); font-size: 16px; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }
.prompt { color: var(--accent-green); }

.chapter-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius); margin-bottom: 8px; transition: var(--transition);
}
.chapter-item.completed { border-color: rgba(0,255,65,0.15); background: rgba(0,255,65,0.03); }
.chapter-info { display: flex; align-items: center; gap: 12px; }
.chapter-order { font-family: var(--font-mono); font-size: 13px; color: var(--text-muted); }
.chapter-title { font-size: 14px; }
</style>
