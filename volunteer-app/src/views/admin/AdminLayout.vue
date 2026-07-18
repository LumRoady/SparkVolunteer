/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!--
  后台管理 — 布局框架

  结构：
    左侧：暗色侧边栏（Logo + 导航菜单）
    右侧：顶栏（面包屑 + 管理员信息 + 退出）+ 内容区（<router-view />）
-->
<template>
  <el-container class="admin-layout">
    <!-- ==================== 侧边栏 ==================== -->
    <el-aside width="220px" class="sidebar">
      <el-menu
        :default-active="activeMenu"
        background-color="#fff5f5"
        text-color="#e55a5a"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item index="/dispatch">
          <el-icon><Monitor /></el-icon>
          <span>调度中心</span>
        </el-menu-item>

        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/tasks">
          <el-icon><List /></el-icon>
          <span>任务管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/devices">
          <el-icon><Connection /></el-icon>
          <span>设备绑定</span>
        </el-menu-item>

        <el-menu-item index="/admin/notify">
          <el-icon><Bell /></el-icon>
          <span>通知推送</span>
        </el-menu-item>

        <el-menu-item index="/admin/communities">
          <el-icon><OfficeBuilding /></el-icon>
          <span>社区管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/feedback">
          <el-icon><Star /></el-icon>
          <span>评价管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/ranking">
          <el-icon><Trophy /></el-icon>
          <span>排行榜</span>
        </el-menu-item>

        <el-menu-item index="/admin/checkins">
          <el-icon><Calendar /></el-icon>
          <span>签到记录</span>
        </el-menu-item>

        <el-menu-item index="/admin/family">
          <el-icon><UserFilled /></el-icon>
          <span>亲属管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/certificate">
          <el-icon><Medal /></el-icon>
          <span>志愿者证书</span>
        </el-menu-item>

        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据大屏</span>
        </el-menu-item>

      </el-menu>
    </el-aside>

    <!-- ==================== 右侧主体 ==================== -->
    <el-container>
      <!-- 顶栏 -->
      <el-header class="topbar">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/admin' }">后台管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
        </el-breadcrumb>

        <div class="topbar-actions">
          <el-tag type="danger" size="small" effect="dark">管理员</el-tag>
          <span class="admin-name">{{ adminName }}</span>
          <el-button type="danger" text size="small" @click="handleLogout">
            退出登录
          </el-button>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-area">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
/**
 * @component AdminLayout
 * @description 后台管理母版页 — 侧边栏 + 顶栏 + 子路由出口
 */
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessageBox } from 'element-plus';
import { User, List, Connection, Bell, OfficeBuilding, Monitor, DataAnalysis, Star, Trophy, Calendar, UserFilled, Medal } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';

// ------------------------------------------------------------------
// 路由 & 状态
// ------------------------------------------------------------------

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activeMenu = computed(() => route.path);
const currentTitle = computed(() => route.meta.title || '');
const adminName = computed(() => authStore.adminName || '管理员');

// ------------------------------------------------------------------
// 退出登录
// ------------------------------------------------------------------

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      authStore.logout();
      router.push('/login');
    })
    .catch(() => {});
}
</script>

<style scoped>
/* ================================================================== */
/* Layout                                                              */
/* ================================================================== */

.admin-layout {
  height: 100vh;
}

/* ================================================================== */
/* 侧边栏                                                              */
/* ================================================================== */

.sidebar {
  background: #fff5f5;
  overflow-y: auto;
}

.el-menu {
  border-right: none;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #ff6b6b, #e55a5a) !important;
  border-radius: 0 20px 20px 0;
  margin-right: 12px;
}

:deep(.el-menu-item:hover) {
  background: rgba(255,107,107,0.08) !important;
}

/* ================================================================== */
/* 顶栏                                                                */
/* ================================================================== */

.topbar {
  height: 56px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  z-index: 10;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-name {
  font-size: 14px;
  color: #333;
}

/* ================================================================== */
/* 内容区                                                              */
/* ================================================================== */

.main-area {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
