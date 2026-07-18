/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!-- 社区管理 -->
<template>
  <div class="page">
    <el-row :gutter="16" class="stats-row">
      <el-col :span="8" v-for="s in summary" :key="s.label">
        <el-card shadow="hover" class="sum-card">
          <div class="sum-num">{{ s.value }}</div>
          <div class="sum-label">{{ s.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header><span class="card-title">🏘️ 社区列表</span></template>
      <el-table :data="list" stripe empty-text="暂无社区数据" v-loading="loading">
        <el-table-column prop="name" label="社区名称" min-width="140" />
        <el-table-column prop="elderly" label="需求者" width="80" align="center">
          <template #default="{row}"><el-tag type="danger" effect="plain">{{ row.elderly }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="volunteers" label="志愿者" width="80" align="center">
          <template #default="{row}"><el-tag type="success" effect="plain">{{ row.volunteers }}</el-tag></template>
        </el-table-column>
        <el-table-column label="供需比" width="140" align="center">
          <template #default="{row}">
            <span style="font-weight:700;color:#ff6b6b">
              {{ row.elderly ? (row.volunteers/row.elderly).toFixed(1) : '-' }}
            </span>
            <span style="font-size:12px;color:#999"> 志愿者/需求者</span>
          </template>
        </el-table-column>
        <el-table-column prop="total" label="总人数" width="80" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import http from '@/utils/request';
const loading = ref(false);
const list = ref([]);
const summary = reactive([
  { label:'覆盖社区', value:0 },
  { label:'总需求者',   value:0 },
  { label:'总志愿者', value:0 },
]);

onMounted(() => loadStats());

async function loadStats() {
  loading.value=true;
  try {
    const data = await http.get('/community/stats');
    list.value = data||[];
    summary[0].value = list.value.length;
    summary[1].value = list.value.reduce((s,i)=>s+(i.elderly||0),0);
    summary[2].value = list.value.reduce((s,i)=>s+(i.volunteers||0),0);
  } catch(e) { list.value=[]; ElMessage.error('加载社区数据失败'); }
  loading.value=false;
}
</script>

<style scoped>
.page { padding:0; }
.stats-row { margin-bottom:20px; }
.sum-card { text-align:center; border-top:3px solid #ff6b6b; }
.sum-num { font-size:28px; font-weight:800; color:#ff6b6b; }
.sum-label { font-size:13px; color:#999; margin-top:6px; }
.card-title { font-weight:700; font-size:15px; color:#ff6b6b; }
</style>
