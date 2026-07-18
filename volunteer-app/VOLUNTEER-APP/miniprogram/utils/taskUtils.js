// 任务工具函数
// 提供通用的任务状态处理和格式化功能

// 任务状态常量
const TASK_STATUS = {
  PENDING: 'PENDING',      // 未参与/待处理
  ACCEPTED: 'ACCEPTED',    // 已参与/处理中
  COMPLETED: 'COMPLETED'   // 已完成
};

// 状态中文映射
const STATUS_TEXT_MAP = {
  [TASK_STATUS.PENDING]: '待处理',
  [TASK_STATUS.ACCEPTED]: '进行中',
  [TASK_STATUS.COMPLETED]: '已完成'
};

// 任务类型映射
const TASK_TYPE_MAP = {
  'emergency': {
    text: '紧急求助',
    icon: '🚨',
    color: '#ef4444'
  },
  'life_service': {
    text: '生活服务',
    icon: '🛒',
    color: '#f59e0b'
  },
  'consultation': {
    text: '日常咨询',
    icon: '💬',
    color: '#10b981'
  },
  'normal': {
    text: '普通任务',
    icon: '📋',
    color: '#3b82f6'
  }
};

/**
 * 格式化时间为相对时间
 * @param {Date} date - 日期对象
 * @returns {string} 相对时间字符串
 */
function formatTimeAgo(date) {
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString();
}

/**
 * 格式化时间为日期字符串
 * @param {Date} date - 日期对象
 * @returns {string} 日期字符串
 */
function formatDate(date) {
  const d = new Date(date);
  return `${d.getFullYear()}.${d.getMonth() + 1}.${d.getDate()}`;
}

/**
 * 统一任务状态为大写
 */
function normalizeStatus(status) {
  return (status || 'PENDING').toString().toUpperCase();
}

/**
 * 根据任务类型获取头像URL
 * @param {string} type - 任务类型
 * @returns {string} 头像URL
 */
/** 任务类型图标（emoji，不依赖外部网络） */
function getTypeIcon(type) {
  const icons = {
    emergency: '🚨',
    sos: '🚨',
    life_service: '🛒',
    consultation: '💬',
    normal: '📋'
  };
  return icons[type] || '📋';
}

function getAvatarByType(type) {
  return getTypeIcon(type);
}

/**
 * 获取任务类型信息
 * @param {string} type - 任务类型
 * @returns {Object} 任务类型信息
 */
function getTaskTypeInfo(type) {
  return TASK_TYPE_MAP[type] || TASK_TYPE_MAP.normal;
}

/**
 * 获取任务状态文本
 * @param {string} status - 任务状态
 * @returns {string} 状态文本
 */
function getStatusText(status) {
  return STATUS_TEXT_MAP[status] || status;
}

/**
 * 志愿者接单：PENDING → ACCEPTED → COMPLETED
 */
function getNextStatus(currentStatus) {
  const s = normalizeStatus(currentStatus);
  switch (s) {
    case TASK_STATUS.PENDING:
      return TASK_STATUS.ACCEPTED;
    case TASK_STATUS.ACCEPTED:
      return TASK_STATUS.COMPLETED;
    default:
      return null;
  }
}

/** 取消接单：ACCEPTED → PENDING */
function getCancelEngageStatus() {
  return TASK_STATUS.PENDING;
}

function getPrimaryActionText(status) {
  const s = normalizeStatus(status);
  if (s === TASK_STATUS.PENDING) return '立即参与';
  if (s === TASK_STATUS.ACCEPTED) return '标记完成';
  if (s === TASK_STATUS.COMPLETED) return '已完成';
  return '查看详情';
}

module.exports = {
  TASK_STATUS,
  STATUS_TEXT_MAP,
  TASK_TYPE_MAP,
  formatTimeAgo,
  formatDate,
  normalizeStatus,
  getTypeIcon,
  getAvatarByType,
  getTaskTypeInfo,
  getStatusText,
  getNextStatus,
  getCancelEngageStatus,
  getPrimaryActionText
};