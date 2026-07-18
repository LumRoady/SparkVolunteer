/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

/**
 * @fileoverview Vue Router 配置（Hash 模式）
 *
 * 路由表：
 *   /                    → 重定向到 /dispatch
 *   /login               → 登录页
 *   /dispatch            → 任务调度中心（首页）
 *   /dashboard           → 数据大屏
 *   /admin/*             → 后台管理（嵌套路由）
 *
 * 路由守卫：
 *   1. 未登录 → 跳转 /login
 *   2. Token 过期 → 清除登录态 → 跳转 /login
 */

import { createRouter, createWebHashHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

// ---------------------------------------------------------------------------
// 路由定义
// ---------------------------------------------------------------------------

const routes = [
  /* ---- 根路径 ---- */
  {
    path: '/',
    redirect: '/dispatch',
  },

  /* ---- 登录页 ---- */
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/admin/Login.vue'),
    meta: { title: '管理员登录', noAuth: true },
  },

  /* ---- 任务调度中心（首页） ---- */
  {
    path: '/dispatch',
    name: 'TaskDispatch',
    component: () => import('@/views/admin/TaskDispatch.vue'),
    meta: { title: '任务调度中心' },
  },

  /* ---- 数据大屏 ---- */
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '数据大屏' },
  },

  /* ---- 后台管理（嵌套路由） ---- */
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    redirect: '/admin/users',
    meta: { title: '后台管理' },
    children: [
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'tasks',
        name: 'TaskManagement',
        component: () => import('@/views/admin/TaskManagement.vue'),
        meta: { title: '任务管理' },
      },
      {
        path: 'devices',
        name: 'DeviceBinding',
        component: () => import('@/views/admin/DeviceBinding.vue'),
        meta: { title: '设备绑定' },
      },
      {
        path: 'notify',
        name: 'NotifyCenter',
        component: () => import('@/views/admin/NotifyCenter.vue'),
        meta: { title: '通知推送' },
      },
      {
        path: 'communities',
        name: 'CommunityManagement',
        component: () => import('@/views/admin/CommunityManagement.vue'),
        meta: { title: '社区管理' },
      },
      {
        path: 'feedback',
        name: 'FeedbackManagement',
        component: () => import('@/views/admin/FeedbackManagement.vue'),
        meta: { title: '评价管理' },
      },
      {
        path: 'ranking',
        name: 'Ranking',
        component: () => import('@/views/admin/Ranking.vue'),
        meta: { title: '排行榜' },
      },
      {
        path: 'checkins',
        name: 'CheckinRecords',
        component: () => import('@/views/admin/CheckinRecords.vue'),
        meta: { title: '签到记录' },
      },
      {
        path: 'family',
        name: 'FamilyManagement',
        component: () => import('@/views/admin/FamilyManagement.vue'),
        meta: { title: '亲属管理' },
      },
      {
        path: 'certificate',
        name: 'VolunteerCertificate',
        component: () => import('@/views/admin/VolunteerCertificate.vue'),
        meta: { title: '志愿者证书' },
      },
    ],
  },

  /* ---- 404 兜底 ---- */
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dispatch',
  },
];

// ---------------------------------------------------------------------------
// Router 实例
// ---------------------------------------------------------------------------

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

// ---------------------------------------------------------------------------
// JWT 工具函数
// ---------------------------------------------------------------------------

/**
 * 解码 JWT payload（不验证签名，仅读取内容）
 */
function decodeJwtPayload(token) {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    // payload 是第二部分，base64 解码
    const payload = parts[1];
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decoded);
  } catch {
    return null;
  }
}

/**
 * 检查 Token 是否已过期
 * @returns {boolean} true = 已过期
 */
function isTokenExpired(token) {
  const payload = decodeJwtPayload(token);
  if (!payload || !payload.exp) return false; // 无法解析时不过度拦截
  // exp 是秒级时间戳，需与当前秒数比较（留 60 秒缓冲）
  const nowSec = Math.floor(Date.now() / 1000);
  return payload.exp < nowSec + 60;
}

// ---------------------------------------------------------------------------
// 全局守卫 — 鉴权 + 标题 + Token 过期检测
// ---------------------------------------------------------------------------

router.beforeEach((to, _from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title;
  }

  // 登录页不需要鉴权
  if (to.meta.noAuth) {
    // 已登录用户访问登录页 → 直接跳到首页
    const authStore = useAuthStore();
    const token = authStore.token;
    if (token && !isTokenExpired(token)) {
      next('/dispatch');
      return;
    }
    next();
    return;
  }

  // 检查 token 是否存在
  const authStore = useAuthStore();
  const token = authStore.token;
  if (!token) {
    // 未登录 → 跳转登录页（携带目标路径供登录后回跳）
    next({ path: '/login', query: { redirect: to.fullPath } });
    return;
  }

  // 检查 token 是否过期
  if (isTokenExpired(token)) {
    authStore.logout();
    next({ path: '/login', query: { redirect: to.fullPath } });
    return;
  }

  next();
});

export default router;
