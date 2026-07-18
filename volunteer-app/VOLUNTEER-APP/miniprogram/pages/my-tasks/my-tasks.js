const { API } = require('../../utils/config');
const { get, post } = require('../../utils/request');
const { getUserId, requireLogin } = require('../../utils/auth');
const { normalizeStatus, formatDate } = require('../../utils/taskUtils');

Page({
  data: {
    activeTab: 'participated',
    participatedTasks: [],
    publishedTasks: [],
    loading: true
  },

  onShow() {
    if (!requireLogin()) return;
    this.loadTasks();
  },

  onPullDownRefresh() {
    this.loadTasks();
    setTimeout(() => wx.stopPullDownRefresh(), 800);
  },

  loadTasks() {
    this.setData({ loading: true });
    const userId = getUserId();
    if (!userId) {
      this.setData({ loading: false });
      return;
    }

    get(API.userParticipated(userId))
      .then((participatedData) => {
        const partArr = Array.isArray(participatedData) ? participatedData : (participatedData && participatedData.content ? participatedData.content : []);
        const participatedTasks = partArr.map((task) => this.mapTask(task));
        return get(API.userHistory(userId)).then((publishedData) => {
          const pubArr = Array.isArray(publishedData) ? publishedData : (publishedData && publishedData.content ? publishedData.content : []);
          const publishedTasks = pubArr.map((task) => this.mapTask(task));
          this.setData({ participatedTasks, publishedTasks, loading: false });
        });
      })
      .catch(() => this.setData({ loading: false }));
  },

  mapTask(task) {
    const status = normalizeStatus(task.status);
    const statusTextMap = {
      PENDING: '待接取',
      ACCEPTED: '进行中',
      COMPLETED: '已完成',
      CANCELLED: '已取消'
    };
    return {
      id: task.id,
      title: task.title,
      description: task.content,
      location: task.location || '未知位置',
      time: formatDate(new Date(task.createTime)),
      status: status.toLowerCase(),
      statusText: statusTextMap[status] || status
    };
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab });
  },

  viewTaskDetail(e) {
    const { id } = e.currentTarget.dataset;
    if (!id || id === 'undefined') {
      wx.showToast({ title: '任务ID无效', icon: 'none' });
      return;
    }
    wx.navigateTo({ url: `/pages/task-detail/task-detail?no=${id}` });
  },

  cancelTask(e) {
    const { id } = e.currentTarget.dataset;
    wx.showModal({
      title: '取消任务',
      content: '确定要取消发布这个任务吗？',
      success: (res) => {
        if (!res.confirm) return;
        post(API.taskCancel(id), {})
          .then(() => {
            wx.showToast({ title: '任务已取消', icon: 'success' });
            this.loadTasks();
          });
      }
    });
  }
});
