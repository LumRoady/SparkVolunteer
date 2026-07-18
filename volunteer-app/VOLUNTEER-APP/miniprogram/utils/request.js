/**
 * 全局请求封装
 */
const { getApiBase } = require('./config');
const mock = require('./mock');

const ERROR_MESSAGES = {
  '400': '请求参数错误',
  '401': '未授权，请重新登录',
  '403': '没有权限访问',
  '404': '请求的资源不存在',
  '500': '服务器内部错误',
  NETWORK_ERROR: '网络连接失败，请检查网络与服务器地址',
  TIMEOUT: '请求超时，请稍后重试'
};

const showErrorToast = (message) => {
  wx.showToast({ title: message, icon: 'none', duration: 3000 });
};

const showSuccessToast = (message = '操作成功') => {
  wx.showToast({ title: message, icon: 'success', duration: 2000 });
};

const buildUrl = (url) => {
  if (url.startsWith('http')) return url;
  return getApiBase() + url;
};

const request = (options) => {
  // Mock 模式拦截
  if (mock.isEnabled()) {
    const mockPromise = mock.mockRequest(options);
    if (mockPromise) {
      return mockPromise.then(res => {
        if (options.showLoading !== false) wx.hideLoading();
        if (res.statusCode === 200) {
          const body = res.data;
          if (body && typeof body === 'object' && body.code !== undefined) {
            if (body.code === 200) {
              if (options.showSuccess) {
                showSuccessToast(options.successMessage || body.message || '操作成功');
              }
              return Promise.resolve(body.data);
            } else {
              const msg = body.message || '请求失败';
              if (options.showError !== false) showErrorToast(msg);
              return Promise.reject(new Error(msg));
            }
          }
          return Promise.resolve(body);
        }
        return Promise.reject(new Error('Mock 请求失败'));
      });
    }
  }

  const token = wx.getStorageSync('token');

  if (options.showLoading !== false) {
    wx.showLoading({
      title: options.loadingText || '加载中...',
      mask: options.loadingMask !== false
    });
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: buildUrl(options.url),
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.header
      },
      timeout: options.timeout || 30000,
      success: (res) => {
        if (options.showLoading !== false) {
          wx.hideLoading();
        }

        // 401 未授权 或 403 token无效/过期 → 跳转登录
        if (res.statusCode === 401 || res.statusCode === 403) {
          const { clearSession } = require('./auth');
          clearSession();
          showErrorToast('登录已过期，请重新登录');
          setTimeout(() => wx.redirectTo({ url: '/pages/login/login' }), 800);
          reject(new Error('未授权'));
          return;
        }

        if (res.statusCode === 200) {
          const body = res.data;
          if (body && typeof body === 'object' && body.code !== undefined) {
            if (body.code === 200) {
              if (options.showSuccess) {
                showSuccessToast(options.successMessage || body.message || '操作成功');
              }
              resolve(body.data);
            } else {
              const msg = body.message || '请求失败';
              if (options.showError !== false) showErrorToast(msg);
              reject(new Error(msg));
            }
          } else {
            resolve(body);
          }
        } else {
          const errorMsg = ERROR_MESSAGES[String(res.statusCode)] || `请求失败：${res.statusCode}`;
          if (options.showError !== false) showErrorToast(errorMsg);
          reject(new Error(errorMsg));
        }
      },
      fail: (err) => {
        if (options.showLoading !== false) {
          wx.hideLoading();
        }
        let errorMsg = ERROR_MESSAGES.NETWORK_ERROR;
        if (err.errMsg && err.errMsg.includes('timeout')) {
          errorMsg = ERROR_MESSAGES.TIMEOUT;
        }
        if (options.showError !== false) showErrorToast(errorMsg);
        reject(err);
      }
    });
  });
};

module.exports = {
  request,
  get: (url, data = {}, options = {}) => request({ url, method: 'GET', data, ...options }),
  post: (url, data = {}, options = {}) => request({ url, method: 'POST', data, ...options }),
  put: (url, data = {}, options = {}) => request({ url, method: 'PUT', data, ...options }),
  delete: (url, data = {}, options = {}) => request({ url, method: 'DELETE', data, ...options }),
  showErrorToast,
  showSuccessToast
};
