/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!--
  设备绑定管理 — 串起遥控器→需求者→社区→志愿者的完整链路
-->
<template>
  <div class="page">
    <!-- 顶部统计 -->
    <StatCardGroup :items="stats" :cols="4" />

    <!-- 注册新设备 -->
    <el-card shadow="never" class="bind-card">
      <template #header><span class="card-title">注册新设备</span></template>
      <el-form :inline="true" :model="addForm" class="bind-form">
        <el-form-item label="设备编号"><el-input v-model="addForm.deviceId" placeholder="如 DEV002" style="width:160px" /></el-form-item>
        <el-form-item label="设备名称"><el-input v-model="addForm.name" placeholder="如 卧室遥控器" style="width:160px" /></el-form-item>
        <el-form-item>
          <el-button color="#ff6b6b" style="color:#fff" :loading="adding" @click="handleAdd">{{ adding?'注册中':'注册设备' }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 绑定操作区 -->
    <el-card shadow="never" class="bind-card">
      <template #header>
        <span class="card-title">🔗 绑定遥控器</span>
      </template>
      <el-form :inline="true" :model="bindForm" class="bind-form">
        <el-form-item label="设备编号">
          <el-input v-model="bindForm.deviceId" placeholder="如 DEV001" style="width:180px" />
        </el-form-item>
        <el-form-item label="选择需求者">
          <el-select v-model="bindForm.userId" placeholder="选择需求者" filterable style="width:200px">
            <el-option
              v-for="u in elderlyList"
              :key="u.id"
              :label="`${u.name || u.nickname} - ${u.community || '未知社区'}`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button color="#ff6b6b" :dark="false" style="color:#fff" @click="handleBind">确认绑定</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 设备列表 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">📡 设备列表</span>
      </template>
      <el-table :data="devices" stripe v-loading="loading" empty-text="暂无设备">
        <el-table-column prop="deviceId" label="设备编号" width="130" />
        <el-table-column prop="name" label="设备名称" min-width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="绑定需求者" min-width="140">
          <template #default="{ row }">
            <template v-if="row.boundUser">
              <span>{{ row.boundUser.name || row.boundUser.nickname }}</span>
              <el-tag size="small" style="margin-left:8px">{{ row.boundUser.community }}</el-tag>
            </template>
            <span v-else class="no-bind">未绑定</span>
          </template>
        </el-table-column>
        <el-table-column label="附近志愿者" min-width="200">
          <template #default="{ row }">
            <template v-if="row.nearbyVolunteers && row.nearbyVolunteers.length">
              <el-tag
                v-for="v in row.nearbyVolunteers.slice(0, 4)"
                :key="v.id"
                size="small"
                type="warning"
                style="margin-right:4px;margin-bottom:4px"
              >
                {{ v.nickname || v.name }}
              </el-tag>
              <span v-if="row.nearbyVolunteers.length > 4" class="more-tag">
                +{{ row.nearbyVolunteers.length - 4 }}人
              </span>
            </template>
            <span v-else class="no-bind">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="最后在线" width="160">
          <template #default="{ row }">
            {{ row.lastOnlineTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.boundUser"
              type="danger"
              size="small"
              text
              @click="handleUnbind(row)"
            >
              解绑
            </el-button>
            <span v-else class="no-bind">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import http from '@/utils/request';
import StatCardGroup from '@/components/StatCardGroup.vue';

const loading = ref(false);
const devices = ref([]);
const elderlyList = ref([]);

const bindForm = reactive({ deviceId: '', userId: null });
const addForm = reactive({ deviceId: '', name: '' });
const adding = ref(false);

const stats = ref([
  { label: '已注册设备', value: 0, color: '#ff6b6b' },
  { label: '在线设备',    value: 0, color: '#2ed573' },
  { label: '已绑定需求者',  value: 0, color: '#ffa502' },
  { label: '可响应志愿者', value: 0, color: '#3742fa' },
]);

onMounted(() => {
  loadDevices();
  loadElderly();
});

async function loadDevices() {
  loading.value = true;
  try {
    const allDevices = ((await http.get('/device')) || []).map((d) => ({
      ...d,
      boundUser: null,
      nearbyVolunteers: [],
    }));

    // 获取所有需求者和志愿者（带降级）
    const allUsers = await http.get('/users', { params: { role: 'ELDERLY' } }).catch(() => []);
    const volunteers = await http.get('/users', { params: { role: 'VOLUNTEER' } }).catch(() => []);

    // 关联绑定用户和附近志愿者
    for (const dev of allDevices) {
      if (dev.userId) {
        const user = allUsers.find((u) => u.id === dev.userId);
        if (user) {
          dev.boundUser = user;
          dev.nearbyVolunteers = volunteers.filter(
            (v) => v.community === user.community,
          );
        }
      }
    }

    devices.value = allDevices;

    // 更新统计
    stats.value[0].value = allDevices.length;
    stats.value[1].value = allDevices.filter((d) => d.status === 'ACTIVE').length;
    stats.value[2].value = allDevices.filter((d) => d.boundUser).length;
    stats.value[3].value = volunteers.length;
  } catch (e) {
    ElMessage.error('加载设备列表失败');
  }
  loading.value = false;
}

async function loadElderly() {
  try {
    elderlyList.value = await http.get('/users', { params: { role: 'ELDERLY' } }) || [];
  } catch (e) {
    ElMessage.error('加载用户列表失败');
  }
}

async function handleAdd() {
  if (!addForm.deviceId.trim()) { ElMessage.warning('填写设备编号'); return; }
  adding.value = true;
  try {
    await http.post('/device', {
      deviceId: addForm.deviceId.trim(),
      name: addForm.name.trim() || addForm.deviceId.trim(),
      deviceType: 'ESP32_C3',
    });
    ElMessage.success('设备注册成功');
    addForm.deviceId = ''; addForm.name = '';
    loadDevices();
  } catch (e) { ElMessage.error('注册失败'); }
  adding.value = false;
}

async function handleBind() {
  if (!bindForm.deviceId.trim() || !bindForm.userId) {
    ElMessage.warning('请选择设备和需求者');
    return;
  }
  try {
    await http.post('/device/bind', null, {
      params: { deviceId: bindForm.deviceId.trim(), userId: bindForm.userId },
    });
    ElMessage.success('绑定成功！遥控器 → 需求者 → 社区 → 志愿者链路已连通');
    bindForm.deviceId = '';
    bindForm.userId = null;
    loadDevices();
  } catch (e) {
    ElMessage.error('绑定失败');
  }
}

async function handleUnbind(row) {
  ElMessageBox.confirm(`确定解除 ${row.deviceId} 的绑定？`, '确认解绑', {
    type: 'warning',
  }).then(async () => {
    try {
      await http.post('/device/unbind', null, {
        params: { deviceId: row.deviceId },
      });
      ElMessage.success('解绑成功');
      loadDevices();
    } catch (e) {
      ElMessage.error('解绑失败');
    }
  });
}
</script>

<style scoped>
.page { padding: 0; }

/* 统计卡片 — 珊瑚红渐变 */
.stats-row { margin-bottom: 20px; }
.stat-card {
  text-align: center;
  border-top: 3px solid #ff6b6b;
  border-radius: 8px;
}
.stat-card :deep(.el-card__body) { padding: 24px 16px; }
.stat-num {
  font-size: 32px; font-weight: 800;
  background: linear-gradient(135deg, #ff6b6b, #e55a5a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.stat-label { font-size: 13px; color: #999; margin-top: 6px; }

/* 绑定区域 */
.bind-card {
  margin-bottom: 20px;
  border-left: 4px solid #ff6b6b;
}
.card-title { font-weight: 700; font-size: 15px; color: #ff6b6b; }
.bind-form { margin-top: 4px; }

.no-bind { color: #ccc; font-size: 13px; }
.more-tag { font-size: 12px; color: #999; }

/* 表格标签 — 用珊瑚红替代蓝色 */
:deep(.el-table th) { background: #fff5f5; color: #e55a5a; }
</style>
