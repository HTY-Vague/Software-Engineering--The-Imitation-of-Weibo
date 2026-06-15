import { createRouter, createWebHistory } from 'vue-router'
import pinia from '../stores'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../pages/home/HomePage.vue'),
  },
  {
    path: '/auth/login',
    name: 'login',
    component: () => import('../pages/auth/LoginPage.vue'),
  },
  {
    path: '/auth/register',
    name: 'register',
    component: () => import('../pages/auth/RegisterPage.vue'),
  },
  {
    path: '/post/create',
    name: 'post-create',
    component: () => import('../pages/post/PostCreatePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/post/:postId',
    name: 'post-detail',
    component: () => import('../pages/post/PostDetailPage.vue'),
  },
  {
    path: '/post/:postId/edit',
    name: 'post-edit',
    component: () => import('../pages/post/PostEditPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/board/:boardId',
    name: 'board-posts',
    component: () => import('../pages/board/BoardPostsPage.vue'),
  },
  {
    path: '/me',
    name: 'me-profile',
    component: () => import('../pages/me/ProfilePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/me/posts',
    name: 'me-posts',
    component: () => import('../pages/me/MyPostsPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/me/comments',
    name: 'me-comments',
    component: () => import('../pages/me/MyCommentsPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: () => import('../pages/admin/UsersPage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/forums',
    name: 'admin-forums',
    component: () => import('../pages/admin/ForumsPage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/posts',
    name: 'admin-posts',
    component: () => import('../pages/admin/PostsPage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/comments',
    name: 'admin-comments',
    component: () => import('../pages/admin/CommentsPage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/statistics',
    name: 'admin-statistics',
    component: () => import('../pages/admin/StatisticsPage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('../pages/home/HomeNotFound.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const authStore = useAuthStore(pinia)
  authStore.hydrate()

  if ((to.name === 'login' || to.name === 'register') && authStore.isAuthenticated) {
    return { path: '/' }
  }

  if (to.meta?.requiresAuth && !authStore.isAuthenticated) {
    return {
      path: '/auth/login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta?.requiresAdmin && !authStore.isAdmin) {
    return { path: '/' }
  }

  return true
})

export default router
