<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const records = ref([])
const currentQuiz = ref(null)
const userAnswers = ref({})
const result = ref(null)

onMounted(async () => {
  const res = await api.get('/quizzes/records')
  records.value = res.data || []
})

async function startQuiz(quizId) {
  const res = await api.get(`/quizzes/${quizId}`)
  currentQuiz.value = res.data
  userAnswers.value = {}
  result.value = null
}

async function submitQuiz() {
  const res = await api.post(`/quizzes/${currentQuiz.value.id}/submit`, { answers: userAnswers.value })
  result.value = res.data
  ElMessage.success(`得分: ${res.data.score}/${res.data.totalScore}`)
}

const questionTypeMap = { 1: '单选题', 2: '多选题' }
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h1><span class="prompt">&gt;</span> 在线测验</h1>
    </div>

    <!-- History -->
    <div v-if="!currentQuiz" class="records-section">
      <h2>测验记录</h2>
      <el-table :data="records" stripe v-if="records.length">
        <el-table-column prop="quizTitle" label="测验名称" />
        <el-table-column prop="score" label="得分" width="100" />
        <el-table-column prop="submittedAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="startQuiz(row.quizId)">重新测试</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无测验记录" />
    </div>

    <!-- Quiz Active -->
    <div v-if="currentQuiz" class="quiz-active">
      <el-button text @click="currentQuiz = null; result = null" class="back-btn">&lt;- 返回记录</el-button>
      <h2 class="quiz-title">{{ currentQuiz.title }}</h2>
      <span class="quiz-total">总分: {{ currentQuiz.totalScore }}</span>

      <div v-for="q in currentQuiz.questions" :key="q.id" class="question-card">
        <div class="q-header">
          <span class="q-type">{{ questionTypeMap[q.questionType] }}</span>
          <span class="q-score">{{ q.score }}分</span>
        </div>
        <p class="q-content">{{ q.content }}</p>
        <div v-if="q.optionsJson" class="q-options">
          <el-radio-group v-if="q.questionType === 1" v-model="userAnswers[q.id]" class="opt-group">
            <el-radio v-for="(opt, i) in JSON.parse(q.optionsJson)" :key="i" :value="opt" class="opt-item">
              {{ opt }}
            </el-radio>
          </el-radio-group>
          <el-checkbox-group v-else v-model="userAnswers[q.id]" class="opt-group">
            <el-checkbox v-for="(opt, i) in JSON.parse(q.optionsJson)" :key="i" :value="opt" :label="opt" class="opt-item">
              {{ opt }}
            </el-checkbox>
          </el-checkbox-group>
        </div>
        <el-input v-else v-model="userAnswers[q.id]" placeholder="输入你的答案" />

        <div v-if="result" class="q-result" :class="{ correct: result.results?.find(r => r.questionId === q.id)?.correct }">
          <span>{{ result.results?.find(r => r.questionId === q.id)?.correct ? '[+]' : '[-]' }}</span>
          正确答案: {{ result.results?.find(r => r.questionId === q.id)?.correctAnswer }}
          <p v-if="result.results?.find(r => r.questionId === q.id)?.analysis" class="analysis">
            {{ result.results?.find(r => r.questionId === q.id)?.analysis }}
          </p>
        </div>
      </div>

      <el-button type="primary" @click="submitQuiz" size="large" :disabled="!!result" class="submit-btn">
        &gt; 提交答案
      </el-button>

      <div v-if="result" class="quiz-result">
        <div class="big-score">{{ result.score }} / {{ result.totalScore }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container { max-width: 800px; }
.page-header h1 { font-family: var(--font-mono); font-size: 18px; display: flex; align-items: center; gap: 8px; margin-bottom: 24px; }
.prompt { color: var(--accent-green); }
.records-section h2 { font-family: var(--font-mono); font-size: 14px; margin-bottom: 12px; }
.back-btn { margin-bottom: 12px; }
.quiz-title { font-size: 20px; margin-bottom: 4px; }
.quiz-total { font-family: var(--font-mono); font-size: 13px; color: var(--text-muted); }

.question-card {
  background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius);
  padding: 20px; margin-top: 16px;
}
.q-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.q-type { font-family: var(--font-mono); font-size: 11px; color: var(--accent-cyan); }
.q-score { font-family: var(--font-mono); font-size: 11px; color: var(--text-muted); }
.q-content { font-size: 15px; line-height: 1.6; margin-bottom: 14px; }
.opt-group { display: flex; flex-direction: column; gap: 8px; }
.opt-item { color: var(--text-secondary); }
.q-result { margin-top: 12px; padding: 10px 14px; border-radius: var(--radius); font-family: var(--font-mono); font-size: 13px; background: rgba(255,71,87,0.08); }
.q-result.correct { background: rgba(0,255,65,0.06); }
.analysis { margin-top: 4px; font-family: var(--font-sans); font-size: 13px; color: var(--text-secondary); }
.submit-btn { margin-top: 24px; width: 100%; height: 48px !important; font-family: var(--font-mono) !important; }
.quiz-result { text-align: center; margin-top: 20px; }
.big-score { font-family: var(--font-mono); font-size: 36px; color: var(--accent-green); }
</style>
