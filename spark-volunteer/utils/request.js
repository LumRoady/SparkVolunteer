/** @deprecated 此文件已废弃，请使用 volunteer-app/VOLUNTEER-APP/miniprogram/ 下的新版代码 */
// utils/request.js
// 全局请求封装工具

const config = require('./config');

// 基础URL配置（优先从全局配置读取，降级到 config.js）
const getBaseUrl = () => {
  const app = getApp();
  return app ? app.globalData.baseUrl : config.baseUrl;
};

/**
 * 封装wx.request请求
 * @param {Object} options - 请求配置选项
 * @returns {Promise} - 返回Promise对象
 */
const request = (options) => {
  // 显示加载动画
  wx.showLoading({
    title: '加载中...',
    mask: true
  });

  // 返回Promise
  return new Promise((resolve, reject) => {
    wx.request({
      // 合并基础URL（动态获取，保证与其他页面一致）
      url: getBaseUrl() + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: options.header || {
        'content-type': 'application/json'
      },
      success: (res) => {
        // 判断HTTP状态码
        if (res.statusCode === 200) {
          // 判断业务状态码
          if (res.data.code === 200) {
            // 业务成功
            resolve(res.data.data);
          } else {
            // 业务失败
            const errorMessage = res.data.message || '请求失败';
            
            // 特殊处理"任务不存在"的情况
            if (errorMessage.includes('任务不存在')) {
              wx.showToast({
                title: errorMessage,
                icon: 'none',
                duration: 2000
              });
              reject(new Error(errorMessage));
            } else {
              // 其他错误显示通用提示
              wx.showToast({
                title: errorMessage,
                icon: 'none',
                duration: 2000
              });
              reject(new Error(errorMessage));
            }
          }
        } else {
          // HTTP错误
          const errorMessage = `请求失败(${res.statusCode})`;
          wx.showToast({
            title: errorMessage,
            icon: 'none',
            duration: 2000
          });
          reject(new Error(errorMessage));
        }
      },
      fail: (err) => {
        // 网络错误
        const errorMessage = '网络错误，请检查网络连接';
        wx.showToast({
          title: errorMessage,
          icon: 'none',
          duration: 2000
        });
        reject(err);
      },
      complete: () => {
        // 无论成功失败，隐藏加载动画
        wx.hideLoading();
      }
    });
  });
};

// 导出request方法
module.exports = request;
