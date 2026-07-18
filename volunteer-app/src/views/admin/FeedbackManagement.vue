/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!-- 评价管理 -->
<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>评价管理</span>
          <SearchInput v-model="searchTaskId" placeholder="按任务编号搜索" width="200px" size="small" @search="onFilterChange" />
        </div>
      </template>

      <el-table :data="feedbacks" v-loading="loading" stripe>
        <el-table-column prop="id" label="编号" width="80" align="center" />
        <el-table-column prop="taskId" label="任务编号" width="100" align="center" />
        <el-table-column label="评分" width="180" align="center">
          <template #default="{row}">
            <el-rate v-model="row.score" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评价内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="评价时间" width="170" align="center">
          <template #default="{row}">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{row}">
            <el-popconfirm title="确认删除？" @confirm="deleteFeedback(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <PaginationFooter :page="page" :size="size" :total="total" @change="fetchFeedbacks" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import http from '@/utils/request';
import PaginationFooter from '@/components/PaginationFooter.vue';
import SearchInput from '@/components/SearchInput.vue';
import { formatDate } from '@/utils/format';
import { ElMessage } from 'element-plus';

const feedbacks = ref([]);
const loading = ref(false);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const searchTaskId = ref('');

async function fetchFeedbacks() {
  loading.value = true;
  try {
    const params = { page: page.value - 1, size: size.value };
    const res = await http.get('/feedback/list', { params });
    feedbacks.value = res.content || [];
    total.value = res.totalElements || 0;
  } catch {
    feedbacks.value = [];
    ElMessage.error('加载评价列表失败');
  } finally {
    loading.value = false;
  }
}

function onFilterChange() {
  page.value = 1;
  fetchFeedbacks();
}

async function deleteFeedback(id) {
  try {
    await http.delete(`/feedback/${id}`);
    ElMessage.success('删除成功');
    fetchFeedbacks();
  } catch {
    ElMessage.error('删除失败');
  }
}

onMounted(fetchFeedbacks);
</script>

<style scoped>
.page { display:flex; flex-direction:column; gap:14px; }
.pager { display:flex; justify-content:flex-end; margin-top:14px; }
</style>
