/**
 * 全局 API 配置
 *
 * 开发模式：自动检测开发者工具/真机，连接本地后端
 *    - 开发者工具：默认 localhost
 *    - 真机调试：在「设置 → 服务器地址」填写电脑局域网 IP，如 192.168.1.100
 *
 * 生产模式：连接线上服务器（微信小程序要求 HTTPS + 已备案域名）
 *    - 发布前将 IS_PRODUCTION 改为 true，并填入实际生产域名
 */
const API_PORT = 8084;

// ==================== 生产环境配置 ====================
// 发布前修改：
//   1. IS_PRODUCTION 改为 true
//   2. PROD_API_DOMAIN 改为实际域名（需在微信后台配置为合法 request/ws 域名）
const IS_PRODUCTION = false;
const PROD_API_DOMAIN = 'https://your-domain.com';    // 替换为实际生产域名
const PROD_WS_DOMAIN = 'wss://your-domain.com';       // 替换为实际生产 WebSocket 域名
// ==================== 生产环境配置结束 ====================

function getDevHost() {
  if (IS_PRODUCTION) return null; // 生产环境不使用 dev host
  const saved = wx.getStorageSync('apiHost');
  if (saved && String(saved).trim()) {
    return String(saved).trim();
  }
  try {
    const sys = wx.getDeviceInfo();
    if (sys.platform === 'devtools') {
      return 'localhost';
    }
  } catch (e) { /* ignore */ }
  // 真机（含 Windows 微信客户端）统一用 127.0.0.1 避免 localhost 解析问题
  return '127.0.0.1';
}

function getApiBase() {
  if (IS_PRODUCTION) return PROD_API_DOMAIN;
  return `http://${getDevHost()}:${API_PORT}`;
}

function getWsUrl() {
  if (IS_PRODUCTION) return `${PROD_WS_DOMAIN}/ws`;
  return `ws://${getDevHost()}:${API_PORT}/ws`;
}

function setDevHost(host) {
  wx.setStorageSync('apiHost', host);
}

const API = {
  hello: '/api/hello',

  authRegister: '/api/auth/register',
  authLogin: '/api/auth/login',
  authSendResetCode: '/api/auth/send-reset-code',
  authResetPassword: '/api/auth/reset-password',
  authChangePassword: '/api/auth/change-password',
  authSendBindCode: '/api/auth/send-bind-code',
  authBindPhone: '/api/auth/bind-phone',

  taskList: '/api/task/list',
  taskCreate: '/api/task/create',
  taskDetail: (id) => `/api/task/detail/${id}`,
  taskAccept: (id) => `/api/task/accept/${id}`,
  taskStatus: '/api/task/updateStatus',
  taskCancel: (id) => `/api/task/cancel/${id}`,

  userHistory: (userId) => `/api/users/${userId}/history`,
  userParticipated: (userId) => `/api/users/${userId}/participated`,
  userStats: (userId) => `/api/users/${userId}/stats`,
  userProfile: (userId) => `/api/users/${userId}/profile`,
  userUpdate: (userId) => `/api/users/${userId}`,
  userAchievements: (userId) => `/api/users/${userId}/achievements`,

  ranking: (type) => `/api/ranking?type=${type}`,

  notifications: (userId) => `/api/notifications?userId=${userId}`,
  notificationRead: (id) => `/api/notifications/${id}/read`,
  notificationsReadAll: (userId) => `/api/notifications/read?userId=${userId}`,
  notificationDelete: (id) => `/api/notifications/${id}`,
  notificationBatchDelete: '/api/notifications',

  checkinStatus: (userId) => `/api/checkins/today?userId=${userId}`,
  checkin: '/api/checkins',

  challengeList: '/api/challenges',
  challengeDelete: (id) => `/api/challenges/${id}`,
  feedbackList: '/api/feedback/list',
  feedbackSubmit: '/api/feedback',

  certificates: (userId) => `/api/users/${userId}/certificates`,
  devices: (userId) => `/api/users/${userId}/devices`,

  messageConversations: (userId) => `/api/messages/conversations?userId=${userId}`,
  messageList: (conversationId, userId) => `/api/messages/${conversationId}?userId=${userId}`,
  messageSend: '/api/messages',
  messageUnreadCount: (userId) => `/api/messages/unread-count?userId=${userId}`
};

module.exports = {
  API_PORT,
  getDevHost,
  getApiBase,
  getWsUrl,
  setDevHost,
  API
};
