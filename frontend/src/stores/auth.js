import { reactive, computed } from 'vue'
import api from '../api'

const state = reactive({
  user: JSON.parse(localStorage.getItem('user') || 'null'),
  token: localStorage.getItem('token') || '',
})

export function useAuth() {
  const isLoggedIn = computed(() => !!state.token)
  const user = computed(() => state.user)
  const isAdmin = computed(() => {
    const roles = state.user?.roles
    return Array.isArray(roles) && roles.includes('ADMIN')
  })

  async function login(username, password) {
    const res = await api.post('/auth/login', { username, password })
    if (res.code !== 200) {
      throw new Error(res.message || '登录失败')
    }
    state.token = res.data.token
    state.user = { id: res.data.userId, username: res.data.username, nickname: res.data.nickname, roles: res.data.roles || [] }
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(state.user))
    return res.data
  }

  async function register(data) {
    await api.post('/auth/register', data)
  }

  async function fetchUser() {
    const res = await api.get('/auth/me')
    state.user = res.data
    localStorage.setItem('user', JSON.stringify(res.data))
  }

  function logout() {
    state.token = ''
    state.user = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { state, isLoggedIn, user, login, register, fetchUser, logout }
}
