/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

/**
 * @fileoverview 后台管理 — 通用格式化与映射工具
 *
 * 所有标签/状态 → Element Plus type 的映射集中维护于此，
 * 避免在多个组件中重复定义。
 *
 * @module utils/format
 */

// ===========================================================================
// 日期格式化
// ===========================================================================

/**
 * 将日期字符串或时间戳格式化为 YYYY-MM-DD HH:mm
 *
 * @param {string | number | Date} dateStr - 日期值，非法值返回 '-'
 * @returns {string}
 */
export function formatDate(dateStr) {
  if (!dateStr) return '-';

  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return String(dateStr);

  const pad = (n) => String(n).padStart(2, '0');
  return [
    d.getFullYear(),
    '-',
    pad(d.getMonth() + 1),
    '-',
    pad(d.getDate()),
    ' ',
    pad(d.getHours()),
    ':',
    pad(d.getMinutes()),
  ].join('');
}

// ===========================================================================
// 角色映射
// ===========================================================================

const ROLE_TAG_MAP = {
  elderly: 'warning',
  volunteer: 'success',
  admin: 'danger',
};

const ROLE_LABEL_MAP = {
  elderly: '需求者',
  volunteer: '志愿者',
  admin: '管理员',
  requester: '需求者',
};

/** 角色 → Element Plus Tag type */
export function roleTag(role) {
  return ROLE_TAG_MAP[role] || 'info';
}

/** 角色 → 中文标签 */
export function roleLabel(role) {
  return ROLE_LABEL_MAP[role] || role || '未知';
}

// ===========================================================================
// 任务类型映射
// ===========================================================================

const TASK_TYPE_TAG_MAP = {
  emergency: 'danger',
  life_service: 'warning',
  consultation: 'success',
};

const TASK_TYPE_LABEL_MAP = {
  emergency: '紧急求助',
  life_service: '生活服务',
  consultation: '日常咨询',
};

/** 任务类型 → Element Plus Tag type */
export function taskTypeTag(type) {
  return TASK_TYPE_TAG_MAP[type] || 'info';
}

/** 任务类型 → 中文标签 */
export function taskTypeLabel(type) {
  return TASK_TYPE_LABEL_MAP[type] || type || '普通';
}

// ===========================================================================
// 任务状态映射
// ===========================================================================

const TASK_STATUS_TAG_MAP = {
  PENDING: 'warning',
  ACCEPTED: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'info',
};

const TASK_STATUS_LABEL_MAP = {
  PENDING: '待接单',
  ACCEPTED: '已接单',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
};

/** 任务状态 → Element Plus Tag type */
export function taskStatusTag(status) {
  return TASK_STATUS_TAG_MAP[status] || 'info';
}

/** 任务状态 → 中文标签 */
export function taskStatusLabel(status) {
  return TASK_STATUS_LABEL_MAP[status] || status || '未知';
}
