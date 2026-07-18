/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!-- 任务管理 -->
<template>
  <div class="page">
    <!-- 筛选 -->
    <div class="filter-bar">
      <div class="filter-tabs">
        <span
          v-for="tab in statusTabs" :key="tab.value"
          class="ftab"
          :class="{ active: searchForm.status === tab.value }"
          @click="searchForm.status = tab.value; onFilterChange()"
        >{{ tab.label }}</span>
      </div>
      <el-select v-model="searchForm.type" placeholder="任务类型" clearable @change="onFilterChange" style="width:140px">
        <el-option label="全部类型" value="" />
        <el-option label="紧急求助" value="sos" />
        <el-option label="生活服务" value="life_service" />
        <el-option label="日常咨询" value="consultation" />
      </el-select>
    </div>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table :data="taskList" v-loading="loading" stripe>
        <el-table-column prop="id" label="编号" width="70" align="center" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{row}">
            <span class="type-tag" :class="row.type">{{ typeMap[row.type] || row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{row}">
            <span class="status-tag" :class="'s'+statusNum(row.status)">{{ statusLabel(row.status) || row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="120" show-overflow-tooltip />
        <el-table-column label="时间" width="100" align="center">
          <template #default="{row}">{{ shortTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{row}">
            <el-button type="danger" link size="small" @click="showDetail(row)">详情</el-button>
            <el-popconfirm v-if="row.status===0||row.status===1" title="确认取消？" @confirm="cancelTask(row)">
              <template #reference><el-button link size="small" style="color:#999">取消</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <PaginationFooter :page="page" :size="size" :total="total" @change="fetchTasks" />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="任务详情" width="560px" destroy-on-close>
      <template v-if="detailTask">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="编号">{{ detailTask.id }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <span class="type-tag" :class="detailTask.type">{{ typeMap[detailTask.type] }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ detailTask.title }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detailTask.content || '无' }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ detailTask.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <span class="status-tag" :class="'s'+detailTask.status">{{ statusMap[detailTask.status] }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <div class="timeline" v-if="detailTimeline.length">
          <div class="tl-title">操作记录</div>
          <div class="tl-item" v-for="(t,i) in detailTimeline" :key="i">
            <span class="tl-dot" :style="{background:t.color}"></span>
            <span class="tl-label">{{ t.label }}</span>
            <span class="tl-time">{{ t.time }}</span>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import http from '@/utils/request';
import PaginationFooter from '@/components/PaginationFooter.vue';
const taskList = ref([]);
const loading = ref(false);
const page = ref(1);
const size = ref(20);
const total = ref(0);
const detailVisible = ref(false);
const detailTask = ref(null);

const searchForm = reactive({ status: '', type: '' });
const statusTabs = [
  { label:'全部', value:'' },
  { label:'待接单', value:'0' },
  { label:'已接单', value:'1' },
  { label:'已完成', value:'2' },
  { label:'已取消', value:'3' },
];
const typeMap = { sos:'SOS', life_service:'生活', consultation:'咨询' };
const statusMap = { 0:'待接单', 1:'已接单', 2:'已完成', 3:'已取消', PENDING:'待接单', ACCEPTED:'已接单', COMPLETED:'已完成', CANCELLED:'已取消' };
function statusNum(status) {
  if (typeof status === 'number') return status;
  return ({PENDING:0, ACCEPTED:1, COMPLETED:2, CANCELLED:3})[status] || 0;
}
function statusLabel(status) {
  if (status === null || status === undefined) return '-';
  if (typeof status === 'number') return statusMap[status] || status;
  return statusMap[String(status).toUpperCase()] || status;
}

const detailTimeline = computed(() => {
  if(!detailTask.value) return [];
  const t = detailTask.value;
  const items = [];
  if(t.createTime) items.push({ label:'任务创建', time:fmt(t.createTime), color:'#ff6b6b' });
  if(t.acceptTime) items.push({ label:'已接单', time:fmt(t.acceptTime), color:'#ffa502' });
  if(t.finishTime||t.status===2) items.push({ label:'已完成', time:fmt(t.finishTime||t.updateTime), color:'#2ed573' });
  if(t.status===3) items.push({ label:'已取消', time:fmt(t.updateTime), color:'#999' });
  return items;
});

onMounted(() => fetchTasks());

function shortTime(t){ if(!t) return '-'; const d=new Date(t); return `${d.getMonth()+1}/${d.getDate()} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`; }
function fmt(t){ if(!t) return ''; const d=new Date(t); return `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`; }

async function fetchTasks(){
  loading.value=true;
  try{
    const params = { page:page.value-1, size:size.value };
    if(searchForm.status!=='') params.status = Number(searchForm.status);
    if(searchForm.type) params.type = searchForm.type;
    const data = await http.get('/task/tasks-page', { params });
    taskList.value = data.content || [];
    total.value = data.totalElements || 0;
  }catch(e){ taskList.value=[]; ElMessage.error('加载任务列表失败'); }
  loading.value=false;
}

function onFilterChange(){ page.value=1; fetchTasks(); }

function showDetail(row){ detailTask.value=row; detailVisible.value=true; }

async function cancelTask(row){
  try{
    await http.post(`/task/cancel/${row.id}`);
    ElMessage.success('已取消');
    fetchTasks();
  }catch(e){ ElMessage.error('取消失败'); }
}
</script>

<style scoped>
.page { display:flex; flex-direction:column; gap:14px; }
.filter-bar { display:flex; justify-content:space-between; align-items:center; }
.filter-tabs { display:flex; gap:4px; }
.ftab {
  padding:8px 18px; border-radius:6px; font-size:13px; font-weight:600;
  color:#666; cursor:pointer; background:#f5f5f5; transition:all 0.15s;
}
.ftab.active { background:#ff6b6b; color:#fff; }
.ftab:hover:not(.active) { background:#eee; }

.type-tag {
  display:inline-block; padding:2px 10px; border-radius:4px;
  font-size:12px; font-weight:700; color:#fff;
}
.type-tag.sos { background:#ff6b6b; }
.type-tag.life_service { background:#ffa502; }
.type-tag.consultation { background:#2ed573; }

.status-tag {
  display:inline-block; padding:2px 10px; border-radius:4px;
  font-size:12px; font-weight:700;
}
.status-tag.s0 { background:#fff3e0; color:#e65100; }
.status-tag.s1 { background:#e3f2fd; color:#1565c0; }
.status-tag.s2 { background:#e8f5e9; color:#2e7d32; }
.status-tag.s3 { background:#f5f5f5; color:#999; }

.pager { display:flex; justify-content:flex-end; margin-top:14px; }

.timeline { margin-top:20px; border-top:1px solid #f0f0f0; padding-top:16px; }
.tl-title { font-size:14px; font-weight:700; color:#2d3436; margin-bottom:12px; }
.tl-item { display:flex; align-items:center; gap:10px; margin-bottom:10px; font-size:13px; }
.tl-dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.tl-label { flex:1; color:#2d3436; }
.tl-time { color:#999; font-size:12px; }
</style>
