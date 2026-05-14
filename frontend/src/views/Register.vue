<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()
const form = ref({ username: '', password: '', nickname: '', email: '' })
const loading = ref(false)

async function handleRegister() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('用户名和密码不能为空')
    return
  }
  if (form.value.password.length < 6) {
    ElMessage.warning('密码长度至少6位')
    return
  }
  loading.value = true
  try {
    await api.post('/auth/register', form.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
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
        <p class="auth-subtitle">Create New Account</p>
      </div>

      <el-form @submit.prevent="handleRegister" class="auth-form">
        <div class="input-group">
          <label class="input-label">$ USERNAME *</label>
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </div>
        <div class="input-group">
          <label class="input-label">$ PASSWORD *</label>
          <el-input v-model="form.password" type="password" placeholder="密码(至少6位)" size="large" show-password />
        </div>
        <div class="input-group">
          <label class="input-label">$ NICKNAME</label>
          <el-input v-model="form.nickname" placeholder="昵称(可选)" size="large" />
        </div>
        <div class="input-group">
          <label class="input-label">$ EMAIL</label>
          <el-input v-model="form.email" placeholder="邮箱(可选)" size="large" />
        </div>
        <el-button type="success" native-type="submit" :loading="loading" size="large" class="auth-btn">
          &gt; 注 册
        </el-button>
      </el-form>

      <div class="auth-footer">
        <span class="prompt">guest@security:~$ </span>
        <router-link to="/login">login --existing</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page { height: 100vh; display: flex; align-items: center; justify-content: center; position: relative; overflow: hidden; }
.auth-bg { position: absolute; inset: 0; pointer-events: none; }
.scanlines { position: absolute; inset: 0; background: repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(0,0,0,0.06) 2px, rgba(0,0,0,0.06) 4px); }
.bg-grid { position: absolute; inset: 0; background-image: linear-gradient(rgba(0,212,255,0.03) 1px, transparent 1px), linear-gradient(90deg, rgba(0,212,255,0.03) 1px, transparent 1px); background-size: 40px 40px; }
.auth-card { position: relative; width: 440px; padding: 40px; background: var(--bg-card); border: 1px solid var(--border); border-radius: 8px; box-shadow: 0 0 60px rgba(0,255,65,0.05); }
.auth-header { text-align: center; margin-bottom: 32px; }
.auth-logo { font-family: var(--font-mono); font-size: 36px; font-weight: 700; letter-spacing: 2px; color: var(--text-primary); }
.logo-brackets { color: var(--accent-cyan); }
.logo-sl { color: var(--accent-green); }
.auth-subtitle { margin-top: 8px; font-family: var(--font-mono); font-size: 11px; color: var(--text-muted); letter-spacing: 1px; }
.auth-form { display: flex; flex-direction: column; gap: 16px; }
.input-group { display: flex; flex-direction: column; gap: 4px; }
.input-label { font-family: var(--font-mono); font-size: 11px; color: var(--accent-green); letter-spacing: 1px; }
.auth-btn { margin-top: 8px; width: 100%; height: 48px !important; font-family: var(--font-mono) !important; font-size: 14px !important; letter-spacing: 3px !important; }
.auth-footer { margin-top: 28px; text-align: center; font-family: var(--font-mono); font-size: 12px; }
.prompt { color: var(--accent-green); }
.auth-footer a { color: var(--accent-cyan); }
</style>
