/** @deprecated 此文件已废弃，请使用 volunteer-app/VOLUNTEER-APP/miniprogram/ 下的新版代码 */
// utils/util.js

// 工具类
const util = {
  // 格式化时间
  formatTime(date) {
    if (!date) return '';
    
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hour = String(d.getHours()).padStart(2, '0');
    const minute = String(d.getMinutes()).padStart(2, '0');
    const second = String(d.getSeconds()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
  },

  // 格式化任务状态
  formatTaskStatus(status) {
    switch (status) {
      case 'PENDING':
        return '待处理';
      case 'ACCEPTED':
        return '已接单';
      case 'COMPLETED':
        return '已完成';
      case 'CANCELLED':
        return '已取消';
      default:
        return status;
    }
  },

  // 格式化任务类型
  formatTaskType(taskType) {
    switch (taskType) {
      case 'EMERGENCY_HELP':
        return '紧急求助';
      case 'MEDICAL_HELP':
        return '医疗求助';
      case 'LIFE_HELP':
        return '生活求助';
      case 'DAILY_HELP':
        return '日常求助';
      default:
        return taskType;
    }
  },

  // 格式化紧急程度
  formatUrgency(urgency) {
    switch (urgency) {
      case 'LOW':
        return '低';
      case 'MEDIUM':
        return '中';
      case 'HIGH':
        return '高';
      default:
        return urgency;
    }
  },

  // 验证手机号
  validatePhone(phone) {
    const reg = /^1[3-9]\d{9}$/;
    return reg.test(phone);
  },

  // 显示加载提示
  showLoading(title = '加载中...') {
    wx.showLoading({
      title,
      mask: true
    });
  },

  // 隐藏加载提示
  hideLoading() {
    wx.hideLoading();
  },

  // 显示成功提示
  showSuccess(title = '操作成功') {
    wx.showToast({
      title,
      icon: 'success'
    });
  },

  // 显示错误提示
  showError(title = '操作失败') {
    wx.showToast({
      title,
      icon: 'none'
    });
  },

  // 显示确认对话框
  showConfirm(title, content, success) {
    wx.showModal({
      title,
      content,
      success
    });
  },

  // 存储数据到本地
  setStorage(key, value) {
    wx.setStorageSync(key, value);
  },

  // 从本地获取数据
  getStorage(key) {
    return wx.getStorageSync(key);
  },

  // 从本地删除数据
  removeStorage(key) {
    wx.removeStorageSync(key);
  },

  // 清空本地存储
  clearStorage() {
    wx.clearStorageSync();
  }
};

module.exports = util;
