import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { guest: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { guest: true },
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
  },
  {
    path: '/courses',
    name: 'Courses',
    component: () => import('../views/Courses.vue'),
  },
  {
    path: '/courses/:id',
    name: 'CourseDetail',
    component: () => import('../views/CourseDetail.vue'),
  },
  {
    path: '/labs',
    name: 'Labs',
    component: () => import('../views/Labs.vue'),
  },
  {
    path: '/labs/:id',
    name: 'LabDetail',
    component: () => import('../views/LabDetail.vue'),
  },
  {
    path: '/quiz',
    name: 'Quiz',
    component: () => import('../views/Quiz.vue'),
  },
  {
    path: '/ai',
    name: 'AiChat',
    component: () => import('../views/AiChat.vue'),
  },
  {
    path: '/reports',
    name: 'Reports',
    component: () => import('../views/Reports.vue'),
  },
  {
    path: '/reports/create',
    name: 'ReportCreate',
    component: () => import('../views/ReportEdit.vue'),
  },
  {
    path: '/reports/:id',
    name: 'ReportEdit',
    component: () => import('../views/ReportEdit.vue'),
  },
  {
    path: '/admin/notices',
    name: 'AdminNotices',
    component: () => import('../views/admin/Notices.vue'),
  },
  {
    path: '/admin/tags',
    name: 'AdminTags',
    component: () => import('../views/admin/Tags.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.guest && token) {
    return next('/dashboard')
  }
  if (!to.meta.guest && !token) {
    return next('/login')
  }
  next()
})

export default router
