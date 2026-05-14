<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const stats = ref({})
const recommendations = ref({ courses: [], labs: [], suggestion: '' })

onMounted(async () => {
  try {
    const [s, r] = await Promise.all([
      api.get('/users/statistics'),
      api.get('/ai/recommendations'),
    ])
    stats.value = s.data
    recommendations.value = r.data
  } catch (e) { /* ignore */ }
})
</script>

<template>
  <div class="dashboard">
    <!-- Stats Grid -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-value">{{ stats.courseCount || 0 }}</div>
        <div class="stat-label">学习课程</div>
        <div class="stat-sub">{{ stats.completedCourseCount || 0 }} 已完成</div>
      </div>
      <div class="stat-card">
        <div class="stat-value accent-green">{{ stats.completedChapterCount || 0 }}</div>
        <div class="stat-label">完成章节</div>
        <div class="stat-sub">持续进步</div>
      </div>
      <div class="stat-card">
        <div class="stat-value accent-cyan">{{ stats.correctLabCount || 0 }}/{{ stats.labCount || 0 }}</div>
        <div class="stat-label">靶场通关</div>
        <div class="stat-sub">正确率</div>
      </div>
      <div class="stat-card">
        <div class="stat-value accent-purple">{{ stats.averageQuizScore || 0 }}</div>
        <div class="stat-label">测验均分</div>
        <div class="stat-sub">{{ stats.quizCount || 0 }} 次测验</div>
      </div>
    </div>

    <!-- Recommendations -->
    <div class="dash-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="prompt-mark">&gt;</span> AI 为你推荐
        </h2>
        <el-button text type="primary" @click="router.push('/ai')">
          查看详情 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <p class="suggestion-text">{{ recommendations.suggestion || '加载中...' }}</p>

      <div class="rec-grid" v-if="recommendations.courses?.length || recommendations.labs?.length">
        <div v-for="item in recommendations.courses?.slice(0,3)" :key="'c'+item.id" class="rec-item" @click="router.push('/courses/'+item.id)">
          <el-tag size="small" type="primary" effect="dark">课程</el-tag>
          <span class="rec-title">{{ item.title }}</span>
          <span class="rec-score">{{ item.score }}分</span>
        </div>
        <div v-for="item in recommendations.labs?.slice(0,3)" :key="'l'+item.id" class="rec-item" @click="router.push('/labs/'+item.id)">
          <el-tag size="small" type="success" effect="dark">靶场</el-tag>
          <span class="rec-title">{{ item.title }}</span>
          <span class="rec-score">{{ item.score }}分</span>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="dash-section">
      <div class="section-header">
        <h2 class="section-title"><span class="prompt-mark">&gt;</span> 快速入口</h2>
      </div>
      <div class="quick-actions">
        <div class="qa-card" @click="router.push('/courses')">
          <el-icon :size="24"><Reading /></el-icon>
          <span>继续学习</span>
        </div>
        <div class="qa-card" @click="router.push('/labs')">
          <el-icon :size="24"><Aim /></el-icon>
          <span>靶场挑战</span>
        </div>
        <div class="qa-card" @click="router.push('/quiz')">
          <el-icon :size="24"><EditPen /></el-icon>
          <span>在线测验</span>
        </div>
        <div class="qa-card" @click="router.push('/ai')">
          <el-icon :size="24"><Cpu /></el-icon>
          <span>AI 助手</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard { max-width: 1100px; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 20px;
  transition: all var(--transition);
}

.stat-card:hover {
  border-color: var(--border-active);
  box-shadow: var(--glow-cyan);
}

.stat-value {
  font-family: var(--font-mono);
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-value.accent-green { color: var(--accent-green); }
.stat-value.accent-cyan { color: var(--accent-cyan); }
.stat-value.accent-purple { color: var(--accent-purple); }

.stat-label {
  margin-top: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.stat-sub {
  margin-top: 2px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
}

.dash-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-family: var(--font-mono);
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.prompt-mark {
  color: var(--accent-green);
}

.suggestion-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: 16px;
  padding: 16px;
  background: var(--bg-secondary);
  border-left: 3px solid var(--accent-cyan);
  border-radius: 0 var(--radius) var(--radius) 0;
}

.rec-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rec-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all var(--transition);
}

.rec-item:hover {
  background: rgba(0,212,255,0.06);
  border-left: 2px solid var(--accent-cyan);
}

.rec-title {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
}

.rec-score {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.qa-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  cursor: pointer;
  color: var(--text-secondary);
  transition: all var(--transition);
}

.qa-card:hover {
  border-color: var(--accent-cyan);
  color: var(--accent-cyan);
  box-shadow: var(--glow-cyan);
}

.qa-card span {
  font-family: var(--font-mono);
  font-size: 13px;
}

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .quick-actions { grid-template-columns: repeat(2, 1fr); }
}
</style>
