const { API } = require('../../utils/config');
const { get, post, put } = require('../../utils/request');
const { formatTimeAgo, normalizeStatus, TASK_STATUS, getTaskTypeInfo } = require('../../utils/taskUtils');
const { requireLogin, getUserId } = require('../../utils/auth');

Page({
  data: {
    tasks: [],
    activeFilter: 'all',
    filteredTasks: [],
    loading: false,
    loadingMore: false,
    page: 1,
    size: 10,
    hasMore: true
  },

  onLoad() {
    if (!requireLogin()) return;
  },

  onShow() {
    if (!requireLogin()) return;
    this.setData({ page: 1, hasMore: true, tasks: [] }, () => {
      this.loadTasks();
    });
  },

  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true, tasks: [] }, () => {
      this.loadTasks().then(() => wx.stopPullDownRefresh());
    });
  },

  onReachBottom() {
    if (this.data.loadingMore || !this.data.hasMore) return;
    this.loadMore();
  },

  mapTask(t) {
    const typeInfo = getTaskTypeInfo(t.type);
    const status = normalizeStatus(t.status);
    const statusTextMap = {
      PENDING: '待接单',
      ACCEPTED: '已接单',
      COMPLETED: '已完成'
    };
    return {
      id: t.id,
      title: t.title || '未命名任务',
      type: t.type || 'normal',
      typeText: typeInfo.text,
      typeColor: typeInfo.color,
      location: t.location || '未知位置',
      timeAgo: formatTimeAgo(new Date(t.createTime || t.createdAt || new Date())),
      status: status,
      statusText: statusTextMap[status] || '未知'
    };
  },

  loadTasks() {
    this.setData({ loading: true });
    const params = { page: this.data.page, size: this.data.size };
    return get(API.taskList, params, { showLoading: false, showError: false })
      .then((data) => {
        const list = Array.isArray(data) ? data : (data && data.content ? data.content : []);
        const tasks = list.map((t) => this.mapTask(t));
        if (this.data.page === 1) {
          this.setData({ tasks });
        } else {
          this.setData({ tasks: [...this.data.tasks, ...tasks] });
        }
        this.setData({ hasMore: list.length >= this.data.size });
        this.applyFilter();
      })
      .catch(() => {
        wx.showToast({ title: '加载失败', icon: 'none' });
      })
      .finally(() => {
        this.setData({ loading: false, loadingMore: false });
      });
  },

  loadMore() {
    this.setData({ loadingMore: true, page: this.data.page + 1 }, () => {
      this.loadTasks();
    });
  },

  switchFilter(e) {
    const filter = e.currentTarget.dataset.filter;
    this.setData({ activeFilter: filter }, () => {
      this.applyFilter();
    });
  },

  applyFilter() {
    let filtered = this.data.tasks;
    if (this.data.activeFilter !== 'all') {
      filtered = this.data.tasks.filter((t) => t.type === this.data.activeFilter);
    }
    this.setData({ filteredTasks: filtered });
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset;
    if (!id) return;
    wx.navigateTo({ url: `/pages/task-detail/task-detail?no=${id}` });
  },

  acceptTask(e) {
    const { id } = e.currentTarget.dataset;
    const userId = getUserId();
    if (!userId) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      setTimeout(() => wx.redirectTo({ url: '/pages/login/login' }), 1000);
      return;
    }
    post(API.taskAccept(id), { taskId: id, volunteerId: userId })
      .then(() => {
        wx.showToast({ title: '接单成功', icon: 'success' });
        this.refreshTaskStatus(id, TASK_STATUS.ACCEPTED);
      })
      .catch(() => {});
  },

  completeTask(e) {
    const { id } = e.currentTarget.dataset;
    wx.showModal({
      title: '确认完成',
      content: '确认该任务已完成？',
      success: (res) => {
        if (!res.confirm) return;
        post(API.taskStatus, { taskId: id, status: TASK_STATUS.COMPLETED, volunteerId: getUserId() })
          .then(() => {
            wx.showToast({ title: '任务已完成', icon: 'success' });
            this.refreshTaskStatus(id, TASK_STATUS.COMPLETED);
          })
          .catch(() => {});
      }
    });
  },

  refreshTaskStatus(id, newStatus) {
    const tasks = this.data.tasks.map((t) => {
      if (t.id === id) {
        const statusTextMap = { PENDING: '待接单', ACCEPTED: '已接单', COMPLETED: '已完成' };
        return { ...t, status: newStatus, statusText: statusTextMap[newStatus] || '未知' };
      }
      return t;
    });
    this.setData({ tasks }, () => this.applyFilter());
  }
});
