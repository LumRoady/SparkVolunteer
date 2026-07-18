const { API } = require('../../utils/config');
const { get, post } = require('../../utils/request');
const { normalizeStatus, getNextStatus, TASK_STATUS, getPrimaryActionText } = require('../../utils/taskUtils');
const { getUserId, requireLogin } = require('../../utils/auth');

function formatTime(date) {
  const d = new Date(date);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

Page({
  data: {
    task: null,
    hasArrived: false,
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

  loadTaskDetail(id) {
    get(API.taskDetail(id))
      .then((task) => {
        const status = normalizeStatus(task.status);
        this.setData({
          task: {
            id: task.id,
            title: task.title,
            description: task.content,
            location: task.location || '未知位置',
            time: formatTime(task.createTime),
            contact: task.contactPhone || '暂无联系方式',
            engaged: status === 'ACCEPTED',
            status,
            type: task.type,
            latitude: task.latitude,
            longitude: task.longitude,
            actionText: getPrimaryActionText(status)
          },
          hasArrived: status === 'ACCEPTED'
        });
      })
      .catch(() => setTimeout(() => wx.navigateBack({ delta: 1 }), 1500))
      .finally(() => this.setData({ loading: false }));
  },

  callElder() {
    const phone = this.data.task && this.data.task.contact;
    if (!phone || phone === '暂无联系方式') {
      wx.showToast({ title: '暂无联系电话', icon: 'none' });
      return;
    }
    wx.makePhoneCall({ phoneNumber: phone });
  },

  openMap() {
    if (!this.data.task) return;
    wx.openLocation({
      latitude: this.data.task.latitude || 39.9042,
      longitude: this.data.task.longitude || 116.4074,
      name: this.data.task.title,
      address: this.data.task.location,
      scale: 18
    });
  },

  navigateTo() {
    this.openMap();
  },

  toggleArrive() {
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
        content: '确认该服务已完成？',
        success: (res) => {
          if (res.confirm) this.applyStatus(next);
        }
      });
      return;
    }
    this.applyStatus(next);
  },

  applyStatus(status) {
    post(API.taskStatus, {
      taskId: this.data.task.id,
      status,
      volunteerId: getUserId()
    }).then(() => {
      const task = {
        ...this.data.task,
        status,
        engaged: status === TASK_STATUS.ACCEPTED,
        actionText: getPrimaryActionText(status)
      };
      this.setData({
        task,
        hasArrived: status === TASK_STATUS.ACCEPTED
      });
      const msg = status === TASK_STATUS.ACCEPTED ? '已到达现场' : '服务已完成';
      wx.showToast({ title: msg, icon: 'success' });
    });
  }
});
