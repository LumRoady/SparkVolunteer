const { API } = require('../../utils/config');
const { get, post } = require('../../utils/request');
const { normalizeStatus, getNextStatus, TASK_STATUS, getPrimaryActionText } = require('../../utils/taskUtils');
const { getUserId, requireLogin } = require('../../utils/auth');

Page({
  data: {
    task: null,
    countdown: '00:00',
    loading: true
  },

  onLoad(options) {
    if (!requireLogin()) return;
    const taskId = options.no || options.id;
    if (!taskId || taskId === 'undefined') {
      wx.showToast({ title: '参数错误', icon: 'none' });
      return;
    }
    wx.vibrateShort({ type: 'medium' });
    this.loadTaskDetail(taskId);
  },

  loadTaskDetail(taskId) {
    get(API.taskDetail(taskId))
      .then((task) => {
        const status = normalizeStatus(task.status);
        this.setData({
          task: { ...task, status, actionText: getPrimaryActionText(status) }
        });
        this.startCountdown(task.createTime);
      })
      .catch(() => {
        setTimeout(() => wx.navigateBack({ delta: 1 }), 1500);
      })
      .finally(() => this.setData({ loading: false }));
  },

  startCountdown(createTime) {
    const startTime = new Date(createTime).getTime();
    const updateCountdown = () => {
      const diff = Math.floor((Date.now() - startTime) / 1000);
      const minutes = Math.floor(diff / 60);
      const seconds = diff % 60;
      this.setData({
        countdown: `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
      });
    };
    updateCountdown();
    this.countdownTimer = setInterval(updateCountdown, 1000);
  },

  onUnload() {
    if (this.countdownTimer) clearInterval(this.countdownTimer);
  },

  copyAddress() {
    if (!this.data.task) return;
    wx.setClipboardData({
      data: this.data.task.location || '',
      success: () => wx.showToast({ title: '地址已复制', icon: 'success' })
    });
  },

  callFamily() {
    const phone = this.data.task && this.data.task.contactPhone;
    if (!phone) {
      wx.showToast({ title: '暂无联系电话', icon: 'none' });
      return;
    }
    wx.makePhoneCall({ phoneNumber: phone });
  },

  navigateTo() {
    if (!this.data.task) return;
    const { latitude, longitude, location, title } = this.data.task;
    wx.openLocation({
      latitude: latitude || 39.9042,
      longitude: longitude || 116.4074,
      name: title,
      address: location,
      scale: 18
    });
  },

  handlePrimaryAction() {
    if (!this.data.task) return;
    const status = normalizeStatus(this.data.task.status);
    const next = getNextStatus(status);
    if (!next) {
      wx.showToast({ title: '任务已结束', icon: 'none' });
      return;
    }
    if (next === TASK_STATUS.COMPLETED) {
      wx.showModal({
        title: '确认完成',
        content: '确认紧急任务已处理完成？',
        success: (res) => {
          if (res.confirm) this.applyStatus(next);
        }
      });
      return;
    }
    this.applyStatus(next);
  },

  applyStatus(status) {
    const userId = getUserId();
    if (!userId) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      setTimeout(() => wx.redirectTo({ url: '/pages/login/login' }), 1000);
      return;
    }
    post(API.taskStatus, {
      taskId: this.data.task.id,
      status,
      volunteerId: userId
    }).then((updated) => {
      const task = updated || { ...this.data.task, status };
      task.actionText = getPrimaryActionText(status);
      this.setData({ task });
      wx.showToast({
        title: status === TASK_STATUS.ACCEPTED ? '已接单' : '任务已完成',
        icon: 'success'
      });
    });
  },

  acceptTask() {
    this.handlePrimaryAction();
  },

  call120() {
    wx.showModal({
      title: '确认呼叫120',
      content: '确定要拨打120急救电话吗？',
      confirmColor: '#ef4444',
      success: (res) => {
        if (res.confirm) wx.makePhoneCall({ phoneNumber: '120' });
      }
    });
  }
});
