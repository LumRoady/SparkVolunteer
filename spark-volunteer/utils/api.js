/** @deprecated 此文件已废弃，请使用 volunteer-app/VOLUNTEER-APP/miniprogram/ 下的新版代码 */
// utils/api.js
// API 接口定义模块 —— 统一通过 request.js 发起所有请求

const request = require('./request');

// 获取全局 baseUrl（用于兼容不需要 request 封装的场景）
const getBaseUrl = () => {
  const app = getApp();
  return app ? app.globalData.baseUrl : require('./config').baseUrl;
};

const api = {
  // ==================== 任务相关 API ====================
  tasks: {
    // 获取紧急待处理任务
    getEmergencyPendingTasks() {
      return request({ url: '/mini-program/tasks/emergency/pending', method: 'GET' });
    },

    // 获取附近任务
    getNearbyTasks(latitude = 0, longitude = 0, radius = 10) {
      return request({
        url: '/mini-program/tasks/nearby',
        method: 'GET',
        data: { latitude, longitude, radius }
      });
    },

    // 获取志愿者的任务
    getVolunteerTasks(volunteerId) {
      return request({ url: `/tasks/volunteer/${volunteerId}`, method: 'GET' });
    },

    // 获取老人发布的任务
    getElderlyTasks(elderlyId) {
      return request({ url: `/tasks/elderly/${elderlyId}`, method: 'GET' });
    },

    // 志愿者接单
    acceptTask(taskId, volunteerId) {
      return request({
        url: `/tasks/${taskId}/accept`,
        method: 'POST',
        data: { volunteerId }
      });
    },

    // 获取任务详情
    getTaskDetail(taskId) {
      return request({ url: `/tasks/${taskId}`, method: 'GET' });
    },

    // 完成任务
    completeTask(taskId) {
      return request({ url: `/tasks/${taskId}/complete`, method: 'POST' });
    },

    // 取消任务
    cancelTask(taskId) {
      return request({ url: `/tasks/${taskId}/cancel`, method: 'POST' });
    }
  },

  // ==================== 发布任务相关 API ====================
  publish: {
    // 快速发布任务（简化版）
    createSimpleTask(elderlyId, taskType) {
      return request({
        url: `/mini-program/elderly/${elderlyId}/simple-task`,
        method: 'POST',
        data: { taskType }
      });
    },

    // 快速发布任务（优化版）
    createQuickTask(elderlyId, taskType, description) {
      return request({
        url: `/mini-program/elderly/${elderlyId}/quick-task`,
        method: 'POST',
        data: { taskType, description }
      });
    }
  },

  // ==================== 设备相关 API ====================
  devices: {
    // 获取老人绑定的设备
    getElderlyDevices(elderlyId) {
      return request({ url: `/mini-program/elderly/${elderlyId}/devices`, method: 'GET' });
    },

    // 老人绑定设备
    bindDevice(elderlyId, deviceId) {
      return request({
        url: `/mini-program/elderly/${elderlyId}/bind-device`,
        method: 'POST',
        data: { deviceId }
      });
    }
  },

  // ==================== 用户相关 API ====================
  user: {
    // 登录
    login(username, password) {
      return request({
        url: '/users/login',
        method: 'POST',
        data: { username, password }
      });
    },

    // 获取用户信息
    getUserInfo(userId) {
      return request({ url: `/users/${userId}`, method: 'GET' });
    },

    // 获取用户历史求助记录
    getUserHistory(userId, page = 0, size = 10) {
      if (!userId) {
        return Promise.reject(new Error('用户ID不能为空'));
      }
      return request({
        url: `/users/${userId}/history`,
        method: 'GET',
        data: { page, size }
      });
    }
  }
};

module.exports = api;
