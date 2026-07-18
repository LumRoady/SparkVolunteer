/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!--
  应用根组件
  - Dashboard 大屏自行管理暗色背景
  - Admin 后台使用 Element Plus 默认浅色主题
-->
<template>
  <router-view />
</template>

<script setup></script>

<style>
/* ------------------------------------------------------------------ */
/* 全局 Reset & 字体                                                    */
/* ------------------------------------------------------------------ */

@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&display=swap');

*,
*::before,
*::after {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  background: #f0f2f5;
  color: #333;
}

/* Element Plus 全局圆角变量 */
:root {
  --el-border-radius-base: 6px;
  --el-border-radius-small: 4px;
  --el-border-radius-round: 999px;
}

/* 卡片圆角 */
.el-card { border-radius: 12px !important; }
.el-card__header { border-radius: 12px 12px 0 0 !important; }

/* 输入框圆角 */
.el-input__wrapper { border-radius: 6px !important; }
.el-textarea__inner { border-radius: 6px !important; }

/* 表格外层圆角 */
.el-table { border-radius: 12px; overflow: hidden; }

/* 弹窗圆角 */
.el-dialog { border-radius: 16px; }
.el-dialog__header { border-radius: 16px 16px 0 0; }

/* 标签圆角 */
.el-tag { border-radius: 6px; }

a {
  text-decoration: none;
  color: inherit;
}
</style>
