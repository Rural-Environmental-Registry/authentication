import { createRouter, createWebHistory } from 'vue-router'
import AdminSettings from '../views/AdminSettings.vue'
import AdminLogin from '@/views/AdminLogin.vue'
import UserLogin from '@/views/UserLogin.vue'
import UserRegister from '@/views/UserRegister.vue'
import UserProfile from '@/views/UserProfile.vue'
import UserSSO from '@/views/UserSSO.vue'
import UserLogout from '@/views/UserLogout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.VITE_BASE_URL || "/"),
  routes: [
    {
      path: '/',
      name: 'home',
      component: AdminSettings,
      meta: {
        title: 'pageTitles.adminSettings'
      }
    },
    {
      path: '/sso',
      name: 'sso',
      component: UserSSO,
      meta: {
        title: ''
      }
    },
    {
      path: '/logout',
      name: 'logout',
      component: UserLogout,
      meta: {
        title: ''
      }
    },
    {
      path: '/admin-login',
      name: 'admin-login',
      component: AdminLogin,
      meta: {
        title: 'pageTitles.adminLogin'
      }
    },
    {
      path: '/login',
      name: 'user-login',
      component: UserLogin,
      meta: {
        title: 'pageTitles.userLogin'
      }
    },
    {
      path: '/profile',
      name: 'user-profile',
      component: UserProfile,
      meta: {
        title: 'pageTitles.userLogin'
      }
    },
    {
      path: '/register',
      name: 'register',
      component: UserRegister,
      meta: {
        title: 'pageTitles.userRegister'
      }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: { name: 'user-login' }
    }
  ],
})

export default router
