/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<template>
  <el-input
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :placeholder="placeholder"
    :style="{ width: width }"
    clearable
    @keyup.enter="$emit('search')"
  >
    <template #append>
      <el-button @click="$emit('search')" icon="Search">查询</el-button>
    </template>
  </el-input>
</template>

<script setup>
defineProps({
  modelValue: String,
  placeholder: String,
  width: { type: String, default: '200px' },
  size: { type: String, default: 'default' },
});
defineEmits(['update:modelValue', 'search']);
</script>
