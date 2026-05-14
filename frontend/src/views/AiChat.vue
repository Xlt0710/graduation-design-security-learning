<script setup>
import { ref, nextTick, onMounted } from 'vue'
import api from '../api'

const messages = ref([])
const input = ref('')
const loading = ref(false)
const chatBox = ref(null)

onMounted(async () => {
  try {
    const res = await api.get('/ai/conversations')
    const history = (res.data || []).reverse()
    for (const h of history) {
      messages.value.push({ role: 'user', text: h.prompt, time: h.createdAt })
      messages.value.push({ role: 'ai', text: h.response, time: h.createdAt })
    }
  } catch (e) { /* ignore */ }
})

async function send() {
  const text = input.value.trim()
  if (!text) return
  messages.value.push({ role: 'user', text, time: new Date() })
  input.value = ''
  loading.value = true
  await nextTick()
  chatBox.value?.scrollTo({ top: chatBox.value.scrollHeight, behavior: 'smooth' })

  try {
    const res = await api.post('/ai/chat', { message: text })
    messages.value.push({ role: 'ai', text: res.data.reply, time: new Date() })
  } catch (e) { /* handled */ }
  finally {
    loading.value = false
    await nextTick()
    chatBox.value?.scrollTo({ top: chatBox.value.scrollHeight, behavior: 'smooth' })
  }
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <div class="chat-container">
    <div class="chat-header">
      <h1><span class="prompt">&gt;</span> AI 安全助手</h1>
      <span class="status">online</span>
    </div>

    <div class="chat-messages" ref="chatBox">
      <div v-if="!messages.length" class="chat-welcome">
        <p class="welcome-icon">[AI]</p>
        <p>你好！我是AI安全学习助手。可以问我SQL注入、XSS、CSRF等Web安全相关问题。</p>
        <div class="quick-prompts">
          <span @click="input='SQL注入如何防护？'; send()">SQL注入防护</span>
          <span @click="input='XSS是什么？'; send()">XSS原理</span>
          <span @click="input='推荐学习路线'; send()">学习路线</span>
        </div>
      </div>

      <div v-for="(m, i) in messages" :key="i" class="msg-row" :class="m.role">
        <div class="msg-bubble">
          <div class="msg-text">{{ m.text }}</div>
          <div class="msg-time">{{ formatTime(m.time) }}</div>
        </div>
      </div>

      <div v-if="loading" class="msg-row ai">
        <div class="msg-bubble">
          <span class="typing">█</span>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <el-input v-model="input" placeholder="$ 输入你的问题..." size="large" @keyup.enter="send" :disabled="loading" />
      <el-button type="primary" @click="send" :loading="loading" size="large">&gt; 发送</el-button>
    </div>
  </div>
</template>

<style scoped>
.chat-container { max-width: 800px; height: calc(100vh - 140px); display: flex; flex-direction: column; }
.chat-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.chat-header h1 { font-family: var(--font-mono); font-size: 18px; display: flex; align-items: center; gap: 8px; }
.prompt { color: var(--accent-green); }
.status { font-family: var(--font-mono); font-size: 11px; color: var(--accent-green); }
.status::before { content: ''; display: inline-block; width: 6px; height: 6px; background: var(--accent-green); border-radius: 50%; margin-right: 6px; }

.chat-messages { flex: 1; overflow-y: auto; padding-right: 8px; }

.chat-welcome { text-align: center; padding: 40px 20px; }
.welcome-icon { font-family: var(--font-mono); font-size: 24px; color: var(--accent-cyan); margin-bottom: 12px; }
.chat-welcome p { font-size: 14px; color: var(--text-secondary); }
.quick-prompts { margin-top: 20px; display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
.quick-prompts span { font-family: var(--font-mono); font-size: 12px; color: var(--accent-cyan); padding: 6px 14px; border: 1px solid var(--border); border-radius: 20px; cursor: pointer; transition: var(--transition); }
.quick-prompts span:hover { border-color: var(--accent-cyan); background: rgba(0,212,255,0.08); }

.msg-row { margin-bottom: 16px; display: flex; }
.msg-row.user { justify-content: flex-end; }
.msg-bubble { max-width: 75%; padding: 12px 16px; border-radius: var(--radius); }
.msg-row.user .msg-bubble { background: rgba(0,212,255,0.1); border: 1px solid rgba(0,212,255,0.3); border-bottom-right-radius: 2px; }
.msg-row.ai .msg-bubble { background: var(--bg-card); border: 1px solid var(--border); border-bottom-left-radius: 2px; }
.msg-text { font-size: 14px; line-height: 1.7; white-space: pre-wrap; }
.msg-time { font-family: var(--font-mono); font-size: 10px; color: var(--text-muted); margin-top: 6px; }
.typing { font-family: var(--font-mono); color: var(--accent-green); animation: blink 1s step-end infinite; }
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:0} }

.chat-input { display: flex; gap: 10px; padding-top: 16px; border-top: 1px solid var(--border); margin-top: 16px; }
.chat-input .el-input { flex: 1; }
</style>
