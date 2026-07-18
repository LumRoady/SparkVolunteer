/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!-- 签到记录 -->
<template>
  <div class="page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <SearchInput v-model="searchUserId" placeholder="输入用户ID查询" width="200px" size="small" @search="onFilterChange" />
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        @change="onFilterChange"
        size="small"
        style="width:260px"
      />
    </div>

    <el-card shadow="never">
      <el-table :data="checkins" v-loading="loading" stripe empty-text="请输入用户ID后查询">
        <el-table-column prop="id" label="编号" width="80" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column label="签到日期" width="130" align="center">
          <template #default="{row}">{{ formatDate(row.checkinDate) }}</template>
        </el-table-column>
        <el-table-column label="签到时间" width="170" align="center">
          <template #default="{row}">{{ formatDateTime(row.checkinTime) }}</template>
        </el-table-column>
        <el-table-column prop="continuousDays" label="连续天数" width="90" align="center" />
        <el-table-column label="创建时间" width="170" align="center">
          <template #default="{row}">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <PaginationFooter :page="page" :size="size" :total="total" @change="fetchCheckins" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import http from '@/utils/request';
import PaginationFooter from '@/components/PaginationFooter.vue';
import SearchInput from '@/components/SearchInput.vue';
import { formatDate } from '@/utils/format';
import { ElMessage } from 'element-plus';

const searchUserId = ref('');
const dateRange = ref([]);
const checkins = ref([]);
const loading = ref(false);
const page = ref(1);
const size = ref(10);
const total = ref(0);

function formatDateTime(t) {
  if (!t) return '-';
  const d = new Date(t);
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
}

async function fetchCheckins() {
  if (!searchUserId.value) {
    ElMessage.warning('请输入用户ID');
    return;
  }
  loading.value = true;
  try {
    const params = {
      userId: searchUserId.value,
      page: page.value - 1,
      size: size.value,
    };
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0];
      params.endDate = dateRange.value[1];
    }
    const res = await http.get('/checkins', { params });
    checkins.value = res.content || [];
    total.value = res.totalElements || 0;
  } catch {
    checkins.value = [];
    total.value = 0;
    ElMessage.error('加载签到记录失败');
  } finally {
    loading.value = false;
  }
}

function onFilterChange() {
  page.value = 1;
  fetchCheckins();
}
</script>

<style scoped>
.page { display:flex; flex-direction:column; gap:14px; }
.filter-bar { display:flex; gap:12px; align-items:center; }
.pager { display:flex; justify-content:flex-end; margin-top:14px; }
</style>
