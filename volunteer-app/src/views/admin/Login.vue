/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!-- 管理员登录页 — 参考云班课风格 -->
<template>
  <div class="login-page">
    <div class="login-card">
      <div class="logo-area">
        <div class="logo-img-box">
          <img src="/logo.webp" alt="logo" class="logo-img" />
        </div>
        <span class="logo-text">星火众擎</span>
        <span class="logo-sub">智慧养老志愿服务平台</span>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="管理员账号"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="管理员密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            class="login-btn"
            :loading="loading"
            size="large"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';
import http from '@/utils/request';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const formRef = ref(null);
const loading = ref(false);

const form = reactive({ username: '', password: '' });
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    const data = await http.post('/auth/login', {
      username: form.username, password: form.password,
    });
    if (data.user && data.user.role !== 'ADMIN') {
      ElMessage.error('仅管理员账号可登录后台');
      loading.value = false;
      return;
    }
    authStore.login(data.token, data.user);
    ElMessage.success('登录成功');
    const redirect = route.query.redirect || '/dispatch';
    router.push(redirect);
  } catch (e) {
    ElMessage.error(e.message || '登录失败，请检查账号密码');
  }
  loading.value = false;
}
</script>

<style scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 30%, #f5f6fa 100%);
}

.login-card {
  width: 380px;
  padding: 40px 36px 32px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 107, 107, 0.15);
  border-radius: 24px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08);
}

.logo-area {
  text-align: center;
  margin-bottom: 36px;
}

.logo-img-box {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  padding: 8px;
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo-text {
  font-size: 24px;
  font-weight: 800;
  color: #2d3436;
  display: block;
  letter-spacing: 6px;
}

.logo-sub {
  font-size: 13px;
  color: #b2bec3;
  display: block;
  margin-top: 6px;
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 6px;
  color: #fff;
  background: linear-gradient(135deg, #ff6b6b, #e55a5a);
  border: none;
  border-radius: 25px;
  box-shadow: 0 4px 16px rgba(255, 107, 107, 0.4);
  margin-top: 8px;
}

.login-btn:hover {
  box-shadow: 0 6px 24px rgba(255, 107, 107, 0.55);
  transform: translateY(-1px);
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  height: 50px;
  box-shadow: none;
  background: #f8f9fa;
}
:deep(.el-input__wrapper:hover) {
  background: #f0f1f3;
}
:deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1px #ff6b6b inset;
}
:deep(.el-form-item) {
  margin-bottom: 18px;
}
</style>
