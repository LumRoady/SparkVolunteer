/**
 * 登录态管理
 */
const PUBLIC_PAGES = [
  'pages/login/login',
  'pages/register/register',
  'pages/forgot-password/forgot-password',
  'pages/user-agreement/user-agreement',
  'pages/privacy-policy/privacy-policy'
];

/**
 * 安全获取 App 实例，兼容初始化早期 getApp() 返回 undefined 的情况
 */
function safeGetApp() {
  try {
    return getApp();
  } catch (e) {
    return null;
  }
}

function saveSession(data) {
  const user = data.user || data;
  const token = data.token;

  if (token) {
    wx.setStorageSync('token', token);
  }
  wx.setStorageSync('userInfo', user);
  if (user && user.role) {
    wx.setStorageSync('userRole', user.role);
  }

  const app = safeGetApp();
  if (!app) return;
  app.globalData.token = token || '';
  app.globalData.userId = user.id;
  app.globalData.userInfo = user;
}

function restoreSession(app) {
  if (!app) return false;
  const token = wx.getStorageSync('token');
  const userInfo = wx.getStorageSync('userInfo');

  if (userInfo && userInfo.id) {
    app.globalData.token = token || '';
    app.globalData.userId = userInfo.id;
    app.globalData.userInfo = userInfo;
    return true;
  }
  return false;
}

function clearSession() {
  wx.removeStorageSync('token');
  wx.removeStorageSync('userInfo');

  const app = safeGetApp();
  if (!app) return;
  app.globalData.token = '';
  app.globalData.userId = null;
  app.globalData.userInfo = null;
  if (app.globalData.socketTask) {
    try {
      app.globalData.socketTask.close({ fail: function() {} });
    } catch (e) { /* ignore */ }
    app.globalData.socketTask = null;
  }
  app.globalData.wsConnected = false;
}

function isLoggedIn(app) {
  if (!app) app = safeGetApp();
  if (!app || !app.globalData) return false;
  // 必须同时存在 userId 和 token 才算已登录
  const token = wx.getStorageSync('token');
  return !!(app.globalData.userId && token);
}

function isPublicRoute(route) {
  return PUBLIC_PAGES.indexOf(route) !== -1;
}

/** 全局页面守卫：非公开页未登录则跳转登录 */
function guardPage(app) {
  if (!app) app = safeGetApp();
  const pages = getCurrentPages();
  if (!pages.length) return true;

  const route = pages[pages.length - 1].route;
  if (isPublicRoute(route)) return true;

  if (!isLoggedIn(app)) {
    wx.redirectTo({ url: '/pages/login/login' });
    return false;
  }
  return true;
}

function requireLogin(app) {
  if (!app) app = safeGetApp();
  if (!isLoggedIn(app)) {
    wx.showToast({ title: '请先登录', icon: 'none' });
    setTimeout(function () {
      wx.redirectTo({ url: '/pages/login/login' });
    }, 600);
    return false;
  }
  return true;
}

function getUserId(app) {
  if (!app) app = safeGetApp();
  if (!app || !app.globalData) return null;
  return app.globalData.userId;
}

function isVolunteer(app) {
  if (!app) app = safeGetApp();
  if (!app || !app.globalData) return false;
  var user = app.globalData.userInfo;
  if (!user) return false;
  return user.role === 'volunteer' || user.role === '爱心志愿者' || user.role === 'VOLUNTEER';
}

function isRequester(app) {
  if (!app) app = safeGetApp();
  if (!app || !app.globalData) return false;
  var user = app.globalData.userInfo;
  if (!user) return false;
  return user.role === 'requester' || user.role === '求助者' || user.role === 'ELDERLY';
}

module.exports = {
  PUBLIC_PAGES: PUBLIC_PAGES,
  saveSession: saveSession,
  restoreSession: restoreSession,
  clearSession: clearSession,
  isLoggedIn: isLoggedIn,
  isPublicRoute: isPublicRoute,
  guardPage: guardPage,
  requireLogin: requireLogin,
  getUserId: getUserId,
  isVolunteer: isVolunteer,
  isRequester: isRequester
};
