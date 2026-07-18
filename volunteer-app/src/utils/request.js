/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

/**
 * @fileoverview Axios 实例封装 — 统一请求/响应拦截
 *
 * 特性：
 * - 自动从 localStorage 读取 Token 并在请求头携带
 * - 兼容后端统一响应格式 { code: 200, data: ... }
 * - 401 自动跳转登录页并清除登录态
 * - 403/500 统一错误提示
 * - 可配置 baseURL（通过 VITE_API_BASE_URL 环境变量）
 *
 * @module utils/request
 */

import axios from 'axios';
import { ElMessage } from 'element-plus';

// ---------------------------------------------------------------------------
// Axios 实例
// ---------------------------------------------------------------------------

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
});

// ---------------------------------------------------------------------------
// 请求拦截器
// ---------------------------------------------------------------------------

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// ---------------------------------------------------------------------------
// 响应拦截器
// ---------------------------------------------------------------------------

http.interceptors.response.use(
  (response) => {
    const { data } = response;

    // 兼容后端统一响应格式 { code, message, data }
    if (data && typeof data === 'object' && data.code !== undefined) {
      if (data.code === 200) {
        return data.data;
      }
      return Promise.reject(new Error(data.message || '请求失败'));
    }

    return data;
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      switch (status) {
        case 401:
          // Token 过期或无效 → 清除登录态并跳转登录页
          localStorage.removeItem('token');
          localStorage.removeItem('adminName');
          localStorage.removeItem('userRole');
          ElMessage.warning('登录已过期，请重新登录');
          // 避免重复跳转
          if (window.location.hash !== '#/login') {
            window.location.href = '#/login';
          }
          break;
        case 403:
          ElMessage.error('权限不足，无法执行此操作');
          break;
        case 429:
          ElMessage.warning('请求过于频繁，请稍后再试');
          break;
        case 500:
          ElMessage.error(data?.message || '服务器内部错误，请稍后重试');
          break;
        default:
          if (status >= 500) {
            ElMessage.error('服务器异常，请稍后重试');
          }
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.warning('请求超时，请检查网络连接');
    }
    return Promise.reject(error);
  },
);

export default http;
