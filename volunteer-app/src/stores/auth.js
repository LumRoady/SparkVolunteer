/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '');
  const adminName = ref(localStorage.getItem('adminName') || '');
  const userRole = ref(localStorage.getItem('userRole') || '');

  const isLoggedIn = computed(() => !!token.value);
  const isAdmin = computed(() => userRole.value === 'ADMIN');

  function login(tokenVal, user) {
    token.value = tokenVal;
    adminName.value = user.name || user.nickname || user.username || '';
    userRole.value = user.role || '';
    localStorage.setItem('token', tokenVal);
    localStorage.setItem('adminName', adminName.value);
    localStorage.setItem('userRole', userRole.value);
  }

  function logout() {
    token.value = '';
    adminName.value = '';
    userRole.value = '';
    localStorage.removeItem('token');
    localStorage.removeItem('adminName');
    localStorage.removeItem('userRole');
  }

  return { token, adminName, userRole, isLoggedIn, isAdmin, login, logout };
});
