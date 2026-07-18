/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!-- 排行榜 -->
<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>志愿者排行榜</span>
          <div style="display:flex;gap:12px;align-items:center">
            <el-select v-model="topN" @change="fetchRanking" size="small" style="width:100px">
              <el-option label="前10名" :value="10" />
              <el-option label="前20名" :value="20" />
              <el-option label="前50名" :value="50" />
              <el-option label="全部" :value="0" />
            </el-select>
            <el-radio-group v-model="rankType" @change="fetchRanking" size="small">
              <el-radio-button value="accept">接单榜</el-radio-button>
              <el-radio-button value="points">积分榜</el-radio-button>
              <el-radio-button value="rating">好评榜</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <el-table :data="displayList" v-loading="loading" stripe>
        <el-table-column prop="rank" label="排名" width="80" align="center">
          <template #default="{row, $index}">
            <span v-if="$index < 3" class="rank-medal">{{ ['🥇','🥈','🥉'][$index] }}</span>
            <span v-else class="rank-num">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="志愿者" min-width="120" />
        <el-table-column prop="community" label="社区" width="120" show-overflow-tooltip />
        <el-table-column prop="completedTasks" label="完成次数" width="100" align="center" />
        <el-table-column prop="points" label="积分" width="90" align="center" />
        <el-table-column label="等级" width="120" align="center">
          <template #default="{row}">
            <el-tag :type="levelTag(row.level)">{{ levelLabel(row.level) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import http from '@/utils/request';

const rankType = ref('accept');
const topN = ref(10);
const rankingList = ref([]);
const loading = ref(false);

const LEVEL_MAP = { 1: '热心邻居', 2: '社区守护者', 3: '金牌守护者', 4: '星火使者' };
const LEVEL_TAG = { 1: 'info', 2: 'success', 3: 'warning', 4: 'danger' };

const displayList = computed(() => {
  if (topN.value > 0 && rankingList.value.length > topN.value) {
    return rankingList.value.slice(0, topN.value);
  }
  return rankingList.value;
});

function levelLabel(lv) { return LEVEL_MAP[lv] || '热心邻居'; }
function levelTag(lv) { return LEVEL_TAG[lv] || 'info'; }

async function fetchRanking() {
  loading.value = true;
  try {
    rankingList.value = await http.get('/ranking', { params: { type: rankType.value } });
  } catch {
    rankingList.value = [];
    ElMessage.error('加载排行榜失败');
  } finally {
    loading.value = false;
  }
}

onMounted(fetchRanking);
</script>

<style scoped>
.page { display:flex; flex-direction:column; gap:14px; }
.rank-medal { font-size:22px; }
.rank-num { font-weight:700; color:#666; }
</style>
