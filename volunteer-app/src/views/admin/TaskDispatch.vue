/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!-- 任务调度中心 — 专业接线台 -->
<template>
  <div class="dispatch">
    <!-- ====== 顶栏 ====== -->
    <header class="topbar">
      <div class="topbar-left">
        <span class="topbar-title">任务调度中心</span>
        <el-tag v-if="newTaskCount" type="danger" effect="dark" round size="small">{{ newTaskCount }} 条新任务</el-tag>
      </div>
      <div class="topbar-right">
        <span class="online-dot"></span>
        <span>在线 {{ onlineCount }} 人</span>
        <el-button size="small" @click="loadAll" :loading="refreshing">刷新</el-button>
        <el-button size="small" @click="$router.push('/dashboard')">数据大屏</el-button>
        <el-button size="small" @click="$router.push('/admin/users')">后台管理</el-button>
        <el-tag type="danger" effect="dark" size="small">接线员</el-tag>
        <span>{{ adminName }}</span>
        <el-button type="danger" text size="small" @click="handleLogout">退出</el-button>
      </div>
    </header>

    <!-- 统计卡片 -->
    <div class="stats-card">
      <div class="stat-block" v-for="s in topStats" :key="s.label">
        <span class="stat-num" :style="{color:s.color}">{{ s.value }}</span>
        <span class="stat-text">{{ s.label }}</span>
      </div>
    </div>

    <!-- ====== 主体三栏 ====== -->
    <div class="main-area">
      <!-- 左侧：任务队列 -->
      <div class="panel queue-panel">
        <div class="panel-head">
          <span>待接单</span>
          <el-tag type="danger" size="small" round>{{ pendingTasks.length }}</el-tag>
        </div>
        <div class="panel-body">
          <div
            v-for="t in pendingTasks"
            :key="t.id"
            class="queue-item"
            :class="{ active: selectedTask?.id === t.id, sos: t.type==='sos' }"
            @click="selectTask(t)"
          >
            <div class="qi-left">
              <span class="qi-type" :class="t.type">{{ typeLabel(t.type) }}</span>
            </div>
            <div class="qi-body">
              <div class="qi-title">{{ t.title }}</div>
              <div class="qi-loc">{{ t.location || '未填写地址' }}</div>
              <div class="qi-time">{{ formatTime(t.createTime) }}</div>
            </div>
            <div class="qi-badge" v-if="t.type==='sos'">!</div>
          </div>
          <div v-if="!pendingTasks.length" class="empty">暂无待接单任务</div>
        </div>
      </div>

      <!-- 中间：任务详情 + 操作 -->
      <div class="panel detail-panel">
        <template v-if="selectedTask">
          <div class="panel-head">
            <span>任务详情</span>
            <el-tag :type="selectedTask.type==='sos'?'danger':'warning'" size="small">{{ typeLabel(selectedTask.type) }}</el-tag>
          </div>
          <div class="panel-body detail-body">
            <div class="detail-field">
              <span class="df-label">任务标题</span>
              <span class="df-value">{{ selectedTask.title }}</span>
            </div>
            <div class="detail-field">
              <span class="df-label">任务描述</span>
              <span class="df-value">{{ selectedTask.content || '无' }}</span>
            </div>
            <div class="detail-field">
              <span class="df-label">发布者</span>
              <span class="df-value">{{ selectedTask.publisherName || '未知' }}</span>
            </div>
            <div class="detail-field">
              <span class="df-label">社区</span>
              <span class="df-value"><el-tag size="small">{{ selectedTask.community || '未知' }}</el-tag></span>
            </div>
            <div class="detail-field">
              <span class="df-label">地址</span>
              <span class="df-value">{{ selectedTask.location || '未填写' }}</span>
            </div>
            <div class="detail-field">
              <span class="df-label">联系电话</span>
              <span class="df-value" style="color:#ff6b6b;font-weight:700">{{ selectedTask.phone || '未填写' }}</span>
            </div>

            <!-- 同社区志愿者 -->
            <div class="volunteer-section" v-if="nearbyVolunteers.length">
              <div class="vs-title">可派单志愿者（同社区）</div>
              <div class="volunteer-list">
                <div class="vol-item" v-for="v in nearbyVolunteers" :key="v.id">
                  <span>{{ v.nickname || v.name }}</span>
                  <span class="vol-points">{{ v.points || 0 }}分</span>
                </div>
              </div>
            </div>
          </div>
          <div class="panel-actions">
            <el-button type="danger" @click="handleCancel(selectedTask.id)">取消任务</el-button>
          </div>
        </template>
        <div v-else class="empty-center">点击左侧任务查看详情</div>
      </div>

      <!-- 右侧：进行中 + 已完成 -->
      <div class="panel right-panels">
        <div class="sub-panel">
          <div class="panel-head">
            <span>进行中</span>
            <el-tag type="warning" size="small" round>{{ acceptedTasks.length }}</el-tag>
          </div>
          <div class="panel-body">
            <div class="queue-item" v-for="t in acceptedTasks" :key="t.id">
              <div class="qi-left">
                <span class="qi-type" :class="t.type">{{ typeLabel(t.type) }}</span>
              </div>
              <div class="qi-body">
                <div class="qi-title">{{ t.title }}</div>
                <div class="qi-volunteer">{{ t.volunteerName || '志愿者' }}</div>
                <div class="qi-time">接单 {{ formatTime(t.acceptTime) }}</div>
              </div>
            </div>
            <div v-if="!acceptedTasks.length" class="empty">-</div>
          </div>
        </div>
        <div class="sub-panel">
          <div class="panel-head">
            <span>已完成</span>
            <el-tag type="success" size="small" round>{{ completedTasks.length }}</el-tag>
          </div>
          <div class="panel-body">
            <div class="queue-item done" v-for="t in completedTasks.slice(0,8)" :key="t.id">
              <div class="qi-body">
                <div class="qi-title">{{ t.title }}</div>
                <div class="qi-time">完成 {{ formatTime(t.finishTime) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import http from '@/utils/request';
import { useAuthStore } from '@/stores/auth';
import { createWsClient } from '@/utils/websocket';

const router = useRouter();
const authStore = useAuthStore();
const adminName = computed(() => authStore.adminName || '接线员');
const onlineCount = ref(0);
const refreshing = ref(false);
const pendingTasks = ref([]);
const acceptedTasks = ref([]);
const completedTasks = ref([]);
const selectedTask = ref(null);
const allUsers = ref([]);
const newTaskCount = ref(0);
let prevPending = 0;
let timer = null;
let wsClient = null;

const topStats = computed(() => [
  { label:'今日求助', value: pendingTasks.value.length + acceptedTasks.value.length + completedTasks.value.length, color:'#ff6b6b' },
  { label:'待接单', value: pendingTasks.value.length, color:'#ffa502' },
  { label:'进行中', value: acceptedTasks.value.length, color:'#3742fa' },
  { label:'已完成', value: completedTasks.value.length, color:'#2ed573' },
]);

const nearbyVolunteers = computed(() => {
  if (!selectedTask.value) return [];
  return allUsers.value.filter(u =>
    u.role === 'VOLUNTEER' && u.community === selectedTask.value.community
  );
});

onMounted(() => {
  loadAll();
  timer = setInterval(loadAll, 8000);
  wsClient = createWsClient({
    onMessage: (data) => {
      if (data.type === 'new_task' || data.type === 'task_status_changed') {
        loadAll();
      }
    },
  });
  wsClient.connect();
});
onUnmounted(() => {
  clearInterval(timer);
  if (wsClient) wsClient.disconnect();
});

function typeLabel(t) { return {sos:'SOS',life_service:'生活',consultation:'咨询'}[t]||t; }
function formatTime(t) {
  if(!t) return '-';
  const d=new Date(t);
  return `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}:${d.getSeconds().toString().padStart(2,'0')}`;
}

async function loadAll() {
  refreshing.value = true;
  try {
    const [tasksRaw, statsData, usersData] = await Promise.all([
      http.get('/task/list').catch(() => []),
      http.get('/dashboard/stats').catch(() => ({})),
      http.get('/users', { params: { role: 'VOLUNTEER' } }).catch(() => []),
    ]);
    // tasksRaw 是分页对象，实际数组在 content 字段
    const rawList = Array.isArray(tasksRaw) ? tasksRaw : (tasksRaw?.content || []);
    const tasks = rawList.map(t=>({
      ...t,
      publisherName: t.publisherName || t.publisher || '',
      community: t.community || (allUsers.value.find(u=>u.id===t.userId)||{}).community || '',
      volunteerName: t.volunteerName || (allUsers.value.find(u=>u.id===t.receiverId)||{}).nickname || '',
    }));
    const prev = pendingTasks.value.length;
    pendingTasks.value = tasks.filter(t=>t.status==='PENDING'||t.status===0).sort((a,b)=> (b.type==='sos'?1:0) - (a.type==='sos'?1:0));
    acceptedTasks.value = tasks.filter(t=>t.status==='ACCEPTED'||t.status===1);
    completedTasks.value = tasks.filter(t=>t.status==='COMPLETED'||t.status===2);
    if (pendingTasks.value.length > prev) newTaskCount.value += pendingTasks.value.length - prev;

    onlineCount.value = statsData.onlineVolunteers || 0;
    allUsers.value = usersData || [];

    // 保持选中
    if (selectedTask.value) {
      const still = pendingTasks.value.find(t=>t.id===selectedTask.value.id);
      selectedTask.value = still || null;
    }
  } catch(e) { ElMessage.error('加载数据失败'); }
  refreshing.value = false;
}

function selectTask(t) {
  selectedTask.value = t;
  if (newTaskCount.value > 0) newTaskCount.value--;
}

async function handleCancel(id) {
  ElMessageBox.confirm('确定取消？','确认',{type:'warning'}).then(async ()=>{
    await http.post(`/task/cancel/${id}`);
    ElMessage.success('已取消');
    selectedTask.value = null;
    loadAll();
  }).catch(()=>{});
}

async function handleLogout() {
  ElMessageBox.confirm('确定退出？','提示',{type:'warning'}).then(()=>{
    authStore.logout();
    router.push('/login');
  }).catch(()=>{});
}
</script>

<style scoped>
.dispatch { height:100vh; display:flex; flex-direction:column; background:#f5f6fa; }

/* ====== 顶栏 ====== */
.topbar {
  height:56px; background:#fff; display:flex; align-items:center; justify-content:space-between;
  padding:0 20px; box-shadow:0 1px 4px rgba(0,0,0,0.04); flex-shrink:0; gap:16px;
}
.topbar-left { display:flex; align-items:center; gap:12px; }
.topbar-title { font-size:17px; font-weight:800; }
.topbar-right { display:flex; align-items:center; gap:10px; font-size:13px; color:#636e72; }
.online-dot { width:8px; height:8px; border-radius:50%; background:#2ed573; flex-shrink:0; }

/* ====== 统计卡片 ====== */
.stats-card {
  display: flex; justify-content: center; gap: 0;
  margin: 0 12px; background: #fff; border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); padding: 28px 0;
}
.stat-block {
  flex: 1; text-align: center;
  border-right: 1px solid #f0f0f0;
}
.stat-block:last-child { border-right: none; }
.stat-num {
  font-size: 48px; font-weight: 900; display: block; line-height: 1;
}
.stat-text {
  font-size: 14px; color: #999; margin-top: 10px; display: block; font-weight: 600;
}

/* ====== 三栏主体 ====== */
.main-area { flex:1; display:flex; gap:12px; padding:12px; overflow:hidden; }
.panel { background:#fff; border-radius:12px; display:flex; flex-direction:column; overflow:hidden; box-shadow:0 1px 4px rgba(0,0,0,0.04); }
.panel-head {
  display:flex; align-items:center; justify-content:space-between;
  padding:12px 16px; border-bottom:1px solid #f0f0f0; font-weight:800; font-size:14px; flex-shrink:0;
}
.panel-body { flex:1; overflow-y:auto; padding:8px; }
.panel-actions { padding:12px 16px; border-top:1px solid #f0f0f0; display:flex; gap:8px; flex-shrink:0; }

/* 左侧队列 */
.queue-panel { width:280px; flex-shrink:0; }
.queue-item {
  display:flex; align-items:flex-start; gap:10px; padding:12px; border-radius:8px;
  cursor:pointer; margin-bottom:6px; transition:all 0.15s; border-left:3px solid transparent;
}
.queue-item:hover { background:#fafafa; }
.queue-item.active { background:#fff5f5; border-left-color:#ff6b6b; }
.queue-item.sos { background:#fff8f8; }
.qi-type {
  width:40px; height:40px; border-radius:8px; display:flex; align-items:center; justify-content:center;
  font-size:12px; font-weight:800; color:#fff; flex-shrink:0;
}
.qi-type.sos { background:#ff6b6b; }
.qi-type.life_service { background:#ffa502; }
.qi-type.consultation { background:#3742fa; }
.qi-body { flex:1; min-width:0; }
.qi-title { font-size:13px; font-weight:700; color:#2d3436; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.qi-loc,.qi-volunteer,.qi-time { font-size:11px; color:#999; margin-top:2px; }
.qi-badge {
  width:24px; height:24px; border-radius:50%; background:#ff6b6b; color:#fff;
  font-size:12px; font-weight:700; display:flex; align-items:center; justify-content:center;
  flex-shrink:0; animation: blink 1s infinite;
}
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:0.4} }

/* 中间详情 */
.detail-panel { flex:1; }
.detail-body { padding:16px; }
.detail-field { display:flex; margin-bottom:14px; }
.df-label { width:70px; font-size:13px; color:#999; flex-shrink:0; font-weight:600; }
.df-value { font-size:13px; color:#2d3436; flex:1; font-weight:600; }
.volunteer-section { margin-top:16px; border-top:1px solid #f0f0f0; padding-top:16px; }
.vs-title { font-size:14px; font-weight:700; color:#ffa502; margin-bottom:10px; }
.volunteer-list { display:flex; flex-wrap:wrap; gap:8px; }
.vol-item {
  padding:6px 14px; background:#fff8f0; border-radius:20px; font-size:12px;
  display:flex; align-items:center; gap:8px;
}
.vol-points { color:#ffa502; font-weight:600; }

/* 右侧 */
.right-panels { width:260px; flex-shrink:0; display:flex; flex-direction:column; gap:12px; background:none; }
.sub-panel { background:#fff; border-radius:12px; flex:1; display:flex; flex-direction:column; overflow:hidden; box-shadow:0 1px 4px rgba(0,0,0,0.04); }
.queue-item.done { border-left:none; }

.empty { text-align:center; padding:30px; color:#ccc; font-size:13px; }
.empty-center { display:flex; align-items:center; justify-content:center; height:100%; color:#ccc; font-size:14px; }
</style>
