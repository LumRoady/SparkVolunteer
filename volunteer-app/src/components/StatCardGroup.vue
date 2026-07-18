/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<template>
  <el-row :gutter="16" class="stats-row">
    <el-col :span="24 / cols" v-for="item in items" :key="item.label">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-num" :style="{ color: item.color || '#ff6b6b' }">{{ item.value }}</div>
        <div class="stat-label">{{ item.label }}</div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
defineProps({
  items: { type: Array, required: true },
  cols: { type: Number, default: 4 },
});
</script>

<style scoped>
.stats-row { margin-bottom: 16px; }
.stat-card { text-align: center; border-top: 3px solid #ff6b6b; }
.stat-num { font-size: 32px; font-weight: 700; }
.stat-label { font-size: 13px; color: #606266; margin-top: 4px; }
</style>
