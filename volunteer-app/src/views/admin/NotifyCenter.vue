/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!-- 通知推送 -->
<template>
  <div class="page">
    <!-- 推送表单 -->
    <el-card shadow="never" class="send-card">
      <template #header><span class="card-title">推送通知</span></template>
      <el-form :model="form" label-width="80px">
        <el-form-item label="通知标题">
          <el-input v-model="form.title" placeholder="如：本周义诊通知" />
        </el-form-item>
        <el-form-item label="通知内容">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="通知内容..." />
        </el-form-item>
        <el-form-item label="通知类型">
          <el-select v-model="form.type" style="width:200px">
            <el-option label="系统通知" value="SYSTEM" />
            <el-option label="任务提醒" value="TASK" />
            <el-option label="紧急通知" value="EMERGENCY" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标用户">
          <el-radio-group v-model="form.target">
            <el-radio value="all">系统广播</el-radio>
            <el-radio value="user">指定用户</el-radio>
          </el-radio-group>
          <el-input v-if="form.target==='user'" v-model="form.targetUserId" placeholder="用户ID" style="width:180px;margin-left:10px" />
        </el-form-item>
        <el-form-item>
          <el-button class="capsule-btn" color="#ff6b6b" style="color:#fff" :loading="sending" @click="handleSend">
            {{ sending?'发送中':'立即推送' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 推送历史 -->
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="card-title">推送历史</span>
          <el-select v-model="filterType" placeholder="类型筛选" @change="onFilterChange" size="small" clearable style="width:140px">
            <el-option label="全部类型" value="" />
            <el-option label="系统通知" value="SYSTEM" />
            <el-option label="任务提醒" value="TASK" />
            <el-option label="紧急通知" value="EMERGENCY" />
          </el-select>
        </div>
      </template>
      <el-table :data="history" stripe empty-text="暂无推送记录" v-loading="loading">
        <el-table-column label="类型" width="90" align="center">
          <template #default="{row}">
            <el-tag :type="typeTag(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="目标用户" width="100" align="center">
          <template #default="{row}">{{ row.userId || '系统广播' }}</template>
        </el-table-column>
        <el-table-column label="时间" width="160" align="center">
          <template #default="{row}">{{ fmtTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <PaginationFooter :page="page" :size="size" :total="total" @change="loadHistory" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import http from '@/utils/request';
import PaginationFooter from '@/components/PaginationFooter.vue';

const sending = ref(false);
const loading = ref(false);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const filterType = ref('SYSTEM');
const history = ref([]);
const form = reactive({ title:'', content:'', type:'SYSTEM', target:'all', targetUserId:'' });

onMounted(() => loadHistory());

function fmtTime(t) {
  if (!t) return '-';
  const d = new Date(t);
  const now = new Date();
  const diff = now - d;
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff/60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff/3600000) + '小时前';
  return `${d.getMonth()+1}/${d.getDate()} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
}

function typeLabel(type) {
  return { SYSTEM:'系统', TASK:'任务', EMERGENCY:'紧急' }[type] || type;
}
function typeTag(type) {
  return { SYSTEM:'info', TASK:'warning', EMERGENCY:'danger' }[type] || 'info';
}

async function handleSend() {
  if(!form.title.trim()||!form.content.trim()){ ElMessage.warning('请填写标题和内容'); return; }
  sending.value = true;
  try {
    const payload = {
      title: form.title,
      content: form.content,
      type: form.type,
      userId: form.target === 'user' && form.targetUserId ? Number(form.targetUserId) : null,
    };
    await http.post('/notifications', payload);
    ElMessage.success('推送成功');
    form.title = ''; form.content = '';
    loadHistory();
  } catch(e) { ElMessage.error('推送失败'); }
  sending.value = false;
}

async function loadHistory() {
  loading.value = true;
  try {
    const params = { page: page.value - 1, size: size.value };
    if (filterType.value) params.type = filterType.value;
    // 管理员查看所有系统通知:传 userId=0（后端应处理为全部用户）
    params.userId = 0;
    const res = await http.get('/notifications', { params });
    history.value = res.content || [];
    total.value = res.totalElements || 0;
  } catch(e) { history.value = []; total.value = 0; ElMessage.error('加载通知历史失败'); }
  loading.value = false;
}

function onFilterChange() {
  page.value = 1;
  loadHistory();
}
</script>

<style scoped>
.page { padding:0; display:flex; flex-direction:column; gap:20px; }
.send-card { border-left:4px solid #ff6b6b; border-radius:12px; }
.card-title { font-weight:700; font-size:15px; color:#ff6b6b; }
.capsule-btn {
  border-radius: 999px !important;
  padding: 8px 32px !important;
  font-weight: 700;
}
.pager { display:flex; justify-content:flex-end; margin-top:14px; }
</style>
