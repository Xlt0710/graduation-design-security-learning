<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const isGuest = ref(route.path === '/login' || route.path === '/register')

watch(() => route.path, (path) => {
  isGuest.value = path === '/login' || path === '/register'
})

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

function isActive(path) {
  return route.path.startsWith(path) ? 'nav-active' : ''
}
</script>

<template>
  <div class="app-shell" v-if="isGuest">
    <router-view />
  </div>

  <el-container class="app-shell" v-else>
    <!-- Sidebar -->
    <el-aside width="220px" class="app-sidebar">
      <div class="logo" @click="$router.push('/dashboard')">
        <span class="logo-icon">&#9608;&#9617;</span>
        <span class="logo-text">Security<span class="logo-accent">Learn</span></span>
      </div>

      <el-menu :default-active="route.path" router class="nav-menu">
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/courses">
          <el-icon><Reading /></el-icon>
          <span>课程学习</span>
        </el-menu-item>
        <el-menu-item index="/labs">
          <el-icon><Aim /></el-icon>
          <span>靶场练习</span>
        </el-menu-item>
        <el-menu-item index="/quiz">
          <el-icon><EditPen /></el-icon>
          <span>在线测验</span>
        </el-menu-item>
        <el-menu-item index="/ai">
          <el-icon><Cpu /></el-icon>
          <span>AI 助手</span>
        </el-menu-item>
        <el-menu-item index="/reports">
          <el-icon><Document /></el-icon>
          <span>漏洞报告</span>
        </el-menu-item>
        <el-menu-item index="/admin/notices">
          <el-icon><Setting /></el-icon>
          <span>管理后台</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <span class="user-tag">{{ $route.query.username || 'User' }}</span>
        <el-button text @click="handleLogout" class="logout-btn">
          <el-icon><SwitchButton /></el-icon>
        </el-button>
      </div>
    </el-aside>

    <!-- Main -->
    <el-container>
      <el-header height="56px" class="app-header">
        <div class="header-breadcrumb">
          <span class="header-path">~{{ route.path }}</span>
          <span class="header-cursor">&#9608;</span>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-shell {
  height: 100vh;
  background: var(--bg-primary);
}

.app-sidebar {
  background: var(--bg-secondary);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo {
  padding: 20px 16px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.logo-icon {
  font-family: var(--font-mono);
  font-size: 20px;
  color: var(--accent-cyan);
}

.logo-text {
  font-family: var(--font-mono);
  font-size: 15px;
  font-weight: 600;
  letter-spacing: -0.5px;
  color: var(--text-primary);
}

.logo-accent {
  color: var(--accent-green);
}

.nav-menu {
  flex: 1;
  padding: 8px;
  background: transparent;
  border: none;
}

.nav-menu .el-menu-item {
  margin-bottom: 2px;
  border-radius: var(--radius);
  color: var(--text-secondary);
  font-family: var(--font-mono);
  font-size: 13px;
  height: 40px;
  line-height: 40px;
  transition: all var(--transition);
}

.nav-menu .el-menu-item:hover {
  background: rgba(0, 212, 255, 0.08);
  color: var(--accent-cyan);
}

.nav-menu .el-menu-item.is-active {
  background: rgba(0, 212, 255, 0.12);
  color: var(--accent-cyan);
  border-left: 2px solid var(--accent-cyan);
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-tag {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
}

.logout-btn {
  color: var(--text-muted) !important;
}

.logout-btn:hover {
  color: var(--accent-red) !important;
}

.app-header {
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.header-path {
  font-family: var(--font-mono);
  font-size: 13px;
  color: var(--text-secondary);
}

.header-cursor {
  font-family: var(--font-mono);
  font-size: 13px;
  color: var(--accent-green);
  margin-left: 4px;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.app-main {
  padding: 24px;
  overflow-y: auto;
}
</style>
