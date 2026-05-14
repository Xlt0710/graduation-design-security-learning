<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../stores/auth'
import api from '../api'

const router = useRouter()
const { login } = useAuth()
const username = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await login(username.value, password.value)
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
import { ElMessage } from 'element-plus'
</script>

<template>
  <div class="auth-page">
    <div class="auth-bg">
      <div class="scanlines"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">
          <span class="logo-brackets">[</span>
          <span class="logo-sl">SL</span>
          <span class="logo-brackets">]</span>
        </div>
        <p class="auth-subtitle">Web Security Learning Platform</p>
      </div>

      <el-form @submit.prevent="handleLogin" class="auth-form">
        <div class="input-group">
          <label class="input-label">$ USERNAME</label>
          <el-input v-model="username" placeholder="输入用户名" size="large" />
        </div>
        <div class="input-group">
          <label class="input-label">$ PASSWORD</label>
          <el-input v-model="password" type="password" placeholder="输入密码" size="large" show-password />
        </div>
        <el-button type="primary" native-type="submit" :loading="loading" size="large" class="auth-btn">
          &gt; 登 录
        </el-button>
      </el-form>

      <div class="auth-footer">
        <span class="prompt">guest@security:~$ </span>
        <router-link to="/register">register --new-account</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.auth-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.scanlines {
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(0,0,0,0.06) 2px, rgba(0,0,0,0.06) 4px);
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0,212,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,212,255,0.03) 1px, transparent 1px);
  background-size: 40px 40px;
}

.auth-card {
  position: relative;
  width: 420px;
  padding: 48px 40px 36px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 8px;
  box-shadow: 0 0 60px rgba(0,212,255,0.05), 0 20px 60px rgba(0,0,0,0.5);
}

.auth-header {
  text-align: center;
  margin-bottom: 36px;
}

.auth-logo {
  font-family: var(--font-mono);
  font-size: 36px;
  font-weight: 700;
  letter-spacing: 2px;
  color: var(--text-primary);
}

.logo-brackets {
  color: var(--accent-cyan);
}

.logo-sl {
  color: var(--accent-green);
}

.auth-subtitle {
  margin-top: 8px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
  letter-spacing: 1px;
  text-transform: uppercase;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-label {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--accent-green);
  letter-spacing: 1px;
}

.auth-btn {
  margin-top: 8px;
  width: 100%;
  height: 48px !important;
  font-family: var(--font-mono) !important;
  font-size: 14px !important;
  letter-spacing: 3px !important;
}

.auth-footer {
  margin-top: 32px;
  text-align: center;
  font-family: var(--font-mono);
  font-size: 12px;
}

.prompt {
  color: var(--accent-green);
}

.auth-footer a {
  color: var(--accent-cyan);
}
</style>
