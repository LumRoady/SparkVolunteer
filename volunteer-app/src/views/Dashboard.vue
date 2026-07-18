/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!--
  数据大屏 — 星火众擎智慧养老指挥中心

  布局（全屏 1920×1080 深色主题）：
    顶部：标题 + 实时时钟
    第二行：4 × 玻璃态指标卡片
    第三行：ECharts 图表（7天趋势折线图 + 类型分布饼图）
    底部：实时求助动态滚动列表

  数据来源：GET /api/dashboard/stats（每 30 秒轮询）
-->
<template>
  <div class="dashboard">
    <!-- ==================== 顶部标题栏 ==================== -->
    <header class="header">
      <div class="header-left">
<h1 class="title">星火众擎智慧养老指挥中心</h1>
      </div>
      <div class="header-right">
        <span class="clock">{{ currentTime }}</span>
        <span class="status-dot"></span>
        <span class="status-text">系统运行中</span>
      </div>
    </header>

    <!-- ==================== 核心指标卡片 ==================== -->
    <section class="stats-row">
      <div class="stat-card card-orange">
        <div class="stat-icon">📋</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.todayHelpCount }}</span>
          <span class="stat-label">今日求助</span>
        </div>
      </div>

      <div class="stat-card card-blue">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <span class="stat-value">
            {{ stats.responseRate }}<small>%</small>
          </span>
          <span class="stat-label">响应率</span>
        </div>
      </div>

      <div class="stat-card card-green">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.onlineVolunteers }}</span>
          <span class="stat-label">在线志愿者</span>
        </div>
      </div>

      <div class="stat-card card-purple">
        <div class="stat-icon">🏘️</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.communityCount }}</span>
          <span class="stat-label">覆盖社区</span>
        </div>
      </div>
    </section>

    <!-- ==================== 图表区域 ==================== -->
    <section class="charts-row">
      <div class="chart-panel">
<div ref="trendChartRef" class="chart-box"></div>
      </div>
      <div class="chart-panel">
<div ref="pieChartRef" class="chart-box"></div>
      </div>
    </section>

    <!-- ==================== 实时动态 ==================== -->
    <section class="feed-section">
      <div class="feed-header">
        <h3 class="feed-title">
          <span class="pulse-dot"></span>
          实时求助动态
        </h3>
        <span class="feed-count">共 {{ stats.recentTasks?.length || 0 }} 条</span>
      </div>

      <div ref="feedScrollRef" class="feed-scroll">
        <TransitionGroup name="task-item" tag="div" class="feed-list">
          <div
            v-for="(task, index) in stats.recentTasks"
            :key="task.id || index"
            class="feed-item"
          >
            <span class="feed-tag" :class="task.type">
              {{ TYPE_MAP[task.type] || task.type }}
            </span>
            <span class="feed-title-text">{{ task.title }}</span>
            <span class="feed-community">{{ task.community }}</span>
            <span class="feed-time">{{ task.time }}</span>
            <span class="feed-status" :class="task.status">
              {{ STATUS_MAP[task.status] || task.status }}
            </span>
          </div>
        </TransitionGroup>

        <div v-if="!stats.recentTasks?.length" class="feed-empty">
          暂无求助动态
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
/**
 * @component Dashboard
 * @description 智慧养老数据大屏 — 全屏深色主题，实时数据监控
 */
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import * as echarts from 'echarts';
import request from '@/utils/request';
import { createWsClient } from '@/utils/websocket';

// ========================================================================
// 常量
// ========================================================================

/** 任务类型 → 中文显示名 */
const TYPE_MAP = {
  emergency: '紧急',
  sos: '紧急',
  life_service: '生活',
  consultation: '咨询',
};

/** 任务状态 → 中文显示名 */
const STATUS_MAP = {
  PENDING: '待处理',
  ACCEPTED: '进行中',
  COMPLETED: '已完成',
};

/** 数据轮询间隔（毫秒） */
const POLL_INTERVAL = 30_000;

// ========================================================================
// 响应式数据
// ========================================================================

const stats = reactive({
  todayHelpCount: 0,
  todayCompletedCount: 0,
  responseRate: 0,
  avgResponseMinutes: 0,
  onlineVolunteers: 0,
  totalElderly: 0,
  totalVolunteers: 0,
  communityCount: 0,
  emergencyPending: 0,
  recentTasks: [],
  weeklyTrend: [],
});

const currentTime = ref('');

// ========================================================================
// ECharts 实例 & 模板引用
// ========================================================================

const trendChartRef = ref(null);
const pieChartRef = ref(null);
const feedScrollRef = ref(null);

let trendChart = null;
let pieChart = null;

// ========================================================================
// ECharts 配置（computed — 数据变化时自动 setOption）
// ========================================================================

/**
 * 近 7 天求助趋势 — 面积折线图
 */
const trendOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderColor: '#1a3a5c',
    textStyle: { color: '#2d3436', fontSize: 13 },
  },
  grid: {
    left: '3%',
    right: '4%',
    top: 20,
    bottom: 10,
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: stats.weeklyTrend.map((d) => d.date),
    axisLine: { lineStyle: { color: '#1a3050' } },
    axisTick: { show: false },
    axisLabel: { color: '#999999', fontSize: 12 },
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#1a3050', type: 'dashed' } },
    axisLabel: { color: '#999999', fontSize: 12 },
  },
  series: [
    {
      type: 'line',
      data: stats.weeklyTrend.map((d) => d.count),
      smooth: true,
      showSymbol: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: '#ff6b6b', width: 3 },
      itemStyle: {
        color: '#ff6b6b',
        borderColor: '#0a1628',
        borderWidth: 2,
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(255,107,107,0.35)' },
          { offset: 1, color: 'rgba(255,107,107,0.02)' },
        ]),
      },
    },
  ],
}));

/**
 * 求助类型分布 — 环形饼图
 */
const pieOption = computed(() => {
  const counts = { emergency: 0, life_service: 0, consultation: 0 };

  stats.recentTasks.forEach((t) => {
    if (counts[t.type] !== undefined) counts[t.type]++;
  });

  const data = [
    { value: counts.emergency, name: '紧急求助', itemStyle: { color: '#ff4757' } },
    { value: counts.life_service, name: '生活服务', itemStyle: { color: '#ffa502' } },
    { value: counts.consultation, name: '日常咨询', itemStyle: { color: '#2ed573' } },
  ].filter((d) => d.value > 0);

  // 全部为 0 时显示占位
  if (!data.length) {
    data.push({ value: 1, name: '暂无数据', itemStyle: { color: '#334455' } });
  }

  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#1a3a5c',
      textStyle: { color: '#2d3436', fontSize: 13 },
      formatter: '{b}: {c} 条 ({d}%)',
    },
    legend: {
      bottom: 0,
      textStyle: { color: '#999999', fontSize: 12 },
      itemWidth: 12,
      itemHeight: 12,
    },
    series: [
      {
        type: 'pie',
        radius: ['55%', '78%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#0a1628',
          borderWidth: 3,
        },
        label: { show: true, color: '#999999', fontSize: 11 },
        emphasis: {
          label: { fontSize: 18, fontWeight: 'bold' },
          scaleSize: 12,
        },
        data,
      },
    ],
  };
});

// ========================================================================
// ECharts 生命周期
// ========================================================================

/** 初始化两个图表实例 */
function initCharts() {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value);
    trendChart.setOption(trendOption.value);
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value);
    pieChart.setOption(pieOption.value);
  }
}

/** 销毁图表实例 */
function disposeCharts() {
  trendChart?.dispose();
  pieChart?.dispose();
  trendChart = null;
  pieChart = null;
}

/** 窗口 resize 时重绘 */
function handleResize() {
  trendChart?.resize();
  pieChart?.resize();
}

// 数据变化 → 图表更新
watch(trendOption, (opt) => trendChart?.setOption(opt, { notMerge: true }), {
  deep: true,
});
watch(pieOption, (opt) => pieChart?.setOption(opt, { notMerge: true }), {
  deep: true,
});

// ========================================================================
// 数据获取
// ========================================================================

/**
 * 从后端拉取大屏数据
 */
async function fetchDashboard() {
  try {
    const data = await request.get('/dashboard/stats');
    if (!data) return;

    Object.assign(stats, {
      todayHelpCount: data.todayHelpCount ?? 0,
      todayCompletedCount: data.todayCompletedCount ?? 0,
      responseRate: data.responseRate ?? 0,
      avgResponseMinutes: data.avgResponseMinutes ?? 0,
      onlineVolunteers: data.onlineVolunteers ?? 0,
      totalElderly: data.totalElderly ?? 0,
      totalVolunteers: data.totalVolunteers ?? 0,
      communityCount: data.communityCount ?? 0,
      emergencyPending: data.emergencyPending ?? 0,
      recentTasks: Array.isArray(data.recentTasks) ? data.recentTasks : [],
      weeklyTrend: Array.isArray(data.weeklyTrend) ? data.weeklyTrend : [],
    });

    await nextTick();
    scrollToBottom();
  } catch (e) {
    console.error('获取大屏数据失败', e);
  }
}

// ========================================================================
// 时钟
// ========================================================================

/** 每秒更新时间显示 */
function updateClock() {
  const now = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  const ymd = [
    now.getFullYear(),
    '-',
    pad(now.getMonth() + 1),
    '-',
    pad(now.getDate()),
  ].join('');
  const hms = [
    pad(now.getHours()),
    ':',
    pad(now.getMinutes()),
    ':',
    pad(now.getSeconds()),
  ].join('');
  currentTime.value = `${ymd} ${hms}`;
}

// ========================================================================
// 滚动
// ========================================================================

/** 实时动态列表滚动到底部 */
function scrollToBottom() {
  const el = feedScrollRef.value;
  if (el) el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' });
}

// ========================================================================
// 生命周期
// ========================================================================

let clockTimer = null;
let dataTimer = null;
let wsClient = null;

onMounted(async () => {
  updateClock();
  clockTimer = setInterval(updateClock, 1000);

  await fetchDashboard();
  await nextTick();
  initCharts();

  // 轮询作为降级备份（30 秒间隔）
  dataTimer = setInterval(fetchDashboard, POLL_INTERVAL);
  window.addEventListener('resize', handleResize);

  // WebSocket 实时推送（数据变更时立即刷新，无需等轮询）
  wsClient = createWsClient({
    onMessage: (data) => {
      if (data.type === 'dashboard_update' || data.type === 'new_task' || data.type === 'task_status_changed') {
        fetchDashboard();
      }
    },
  });
  wsClient.connect();
});

onUnmounted(() => {
  clearInterval(clockTimer);
  clearInterval(dataTimer);
  if (wsClient) wsClient.disconnect();
  window.removeEventListener('resize', handleResize);
  disposeCharts();
});
</script>

<style scoped>
/* ========================================================================
   ｜  全屏容器
   ======================================================================== */

.dashboard {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #fff5f5;
  background-image:
    radial-gradient(ellipse at 20% 50%, rgba(255, 107, 107, 0.06) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(255, 142, 142, 0.05) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 80%, rgba(229, 90, 90, 0.04) 0%, transparent 50%);
}

/* ========================================================================
   ｜  顶部标题栏  80px
   ======================================================================== */

.header {
  height: 80px;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(255, 107, 107, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-icon {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  border-radius: 12px;
  background: rgba(255, 107, 107, 0.15);
}

.title {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 3px;
  background: linear-gradient(90deg, #2d3436, #e55a5a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.clock {
  font-family: 'Orbitron', 'Courier New', monospace;
  font-size: 20px;
  font-weight: 700;
  color: #ff6b6b;
  letter-spacing: 2px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #2ed573;
  box-shadow: 0 0 10px rgba(46, 213, 115, 0.6);
  animation: status-pulse 2s infinite;
}

.status-text {
  font-size: 14px;
  color: #999999;
}

@keyframes status-pulse {
  0%, 100% { opacity: 1; }
  50%      { opacity: 0.4; }
}

/* ========================================================================
   ｜  核心指标卡片  ~140px
   ======================================================================== */

.stats-row {
  display: flex;
  gap: 20px;
  padding: 24px 40px;
  flex-shrink: 0;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 28px 30px;
  border-radius: 12px;
  border: 1px solid rgba(255, 107, 107, 0.1);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
}

/* 左侧色条 */
.stat-card.card-orange { border-left: 3px solid #ff6b6b; }
.stat-card.card-blue   { border-left: 3px solid #ff8e8e; }
.stat-card.card-green  { border-left: 3px solid #e55a5a; }
.stat-card.card-purple { border-left: 3px solid #ffa502; }

.stat-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  border-radius: 12px;
  background: rgba(255, 107, 107, 0.04);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-family: 'Orbitron', 'Courier New', monospace;
  font-size: 48px;
  font-weight: 900;
  line-height: 1.1;
}

.stat-value small {
  font-size: 22px;
  font-weight: 400;
  opacity: 0.7;
}

.card-orange .stat-value { color: #ff6b6b; }
.card-blue   .stat-value { color: #ff8e8e; }
.card-green  .stat-value { color: #e55a5a; }
.card-purple .stat-value { color: #ffa502; }

.stat-label {
  margin-top: 4px;
  font-size: 14px;
  color: #999;
  letter-spacing: 1px;
}

/* ========================================================================
   ｜  图表区域  flex:1
   ======================================================================== */

.charts-row {
  flex: 1;
  display: flex;
  gap: 20px;
  padding: 0 40px;
  min-height: 0;
}

.chart-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid rgba(255, 107, 107, 0.08);
  background: rgba(255, 107, 107, 0.03);
}

.panel-title {
  margin-bottom: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #ff6b6b;
  flex-shrink: 0;
}

.chart-box {
  flex: 1;
  min-height: 0;
}

/* ========================================================================
   ｜  实时动态  180px
   ======================================================================== */

.feed-section {
  height: 180px;
  margin: 20px 40px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  border-radius: 12px;
  border: 1px solid rgba(255, 107, 107, 0.08);
  background: rgba(255, 107, 107, 0.03);
}

.feed-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 24px 8px;
  flex-shrink: 0;
}

.feed-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #ff6b6b;
}

.pulse-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ff6b6b;
  box-shadow: 0 0 8px rgba(255, 107, 107, 0.3);
  animation: pulse-glow 1.5s infinite;
}

.feed-count {
  font-size: 13px;
  color: #999999;
}

@keyframes pulse-glow {
  0%, 100% { opacity: 1;    transform: scale(1); }
  50%      { opacity: 0.5;  transform: scale(1.3); }
}

/* ---- 滚动列表 ---- */

.feed-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 0 24px 12px;
}

.feed-scroll::-webkit-scrollbar       { width: 4px; }
.feed-scroll::-webkit-scrollbar-track { background: transparent; }
.feed-scroll::-webkit-scrollbar-thumb {
  background: #fff;
  border-radius: 2px;
}

.feed-list {
  display: flex;
  flex-direction: column;
}

.feed-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 16px;
  margin-bottom: 6px;
  border-radius: 8px;
  background: rgba(255, 107, 107, 0.03);
  transition: background 0.3s ease;
}

.feed-item:hover {
  background: rgba(55, 66, 250, 0.08);
}

/* ---- 列表列 ---- */

.feed-tag {
  flex-shrink: 0;
  padding: 3px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.feed-tag.emergency,
.feed-tag.sos {
  background: rgba(255, 107, 107, 0.2);
  color: #ff4757;
}

.feed-tag.life_service {
  background: rgba(255, 165, 2, 0.2);
  color: #ffa502;
}

.feed-tag.consultation {
  background: rgba(46, 213, 115, 0.2);
  color: #2ed573;
}

.feed-title-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: #999;
}

.feed-community,
.feed-time {
  flex-shrink: 0;
  font-size: 12px;
  color: #999;
}

.feed-status {
  flex-shrink: 0;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.feed-status.PENDING {
  background: rgba(255, 165, 2, 0.15);
  color: #ffa502;
}

.feed-status.ACCEPTED {
  background: rgba(55, 66, 250, 0.15);
  color: #ff6b6b;
}

.feed-status.COMPLETED {
  background: rgba(46, 213, 115, 0.15);
  color: #2ed573;
}

.feed-empty {
  padding: 30px 0;
  text-align: center;
  font-size: 14px;
  color: #999;
}

/* ========================================================================
   ｜  TransitionGroup 动画
   ======================================================================== */

.task-item-enter-active {
  transition: all 0.6s ease;
}

.task-item-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

/* ========================================================================
   ｜  响应式适配
   ======================================================================== */

@media (max-width: 1440px) {
  .header     { height: 66px;  padding: 0 26px; }
  .title      { font-size: 22px; }
  .clock      { font-size: 17px; }
  .stats-row  { padding: 18px 26px; gap: 14px; }
  .stat-card  { padding: 20px 22px; }
  .stat-value { font-size: 38px; }
  .stat-icon  { width: 52px; height: 52px; font-size: 32px; }
  .charts-row { padding: 0 26px; gap: 14px; }
  .feed-section { margin: 14px 26px; height: 150px; }
}

@media (max-width: 1200px) {
  .title      { font-size: 18px; letter-spacing: 1px; }
  .clock      { font-size: 15px; }
  .stat-value { font-size: 32px; }
  .stat-icon  { width: 44px; height: 44px; font-size: 28px; }
}
</style>
