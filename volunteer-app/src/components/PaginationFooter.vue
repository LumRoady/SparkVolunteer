/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<template>
  <div class="pager">
    <el-pagination
      :current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @update:current-page="$emit('update:page', $event)"
      @current-change="$emit('change', $event)"
    />
  </div>
</template>

<script setup>
defineProps({
  page: { type: Number, required: true },
  size: { type: Number, required: true },
  total: { type: Number, required: true },
});
defineEmits(['update:page', 'change']);
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 14px; }
</style>
