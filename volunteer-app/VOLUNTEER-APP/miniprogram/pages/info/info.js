const { API } = require('../../utils/config');
const { get, post, put, delete: del } = require('../../utils/request');
const { getUserId, requireLogin } = require('../../utils/auth');

Page({
  data: {
    activeTab: 'all',
    notifications: [],
    filteredList: [],
    batchMode: false,
    selectedIds: [],
    isAllSelected: false
  },

  onShow() {
    if (!requireLogin()) return;
    this.loadNotifications();
  },

  onWebSocketMessage(data) {
    if (data.type === 'notification' || data.type === 'new_task') {
      this.loadNotifications();
    }
  },

  loadNotifications() {
    const userId = getUserId();
    if (!userId) return;

    get(API.notifications(userId), {}, { showLoading: false, showError: false })
      .then((list) => {
        const arr = Array.isArray(list) ? list : (list && list.content ? list.content : []);
        const notifications = arr.map((n) => ({
          ...n,
          icon: this.getIcon(n.type || n.category),
          timeText: this.formatTime(n.createTime || n.createdAt)
        }));
        this.setData({ notifications }, () => this.applyFilter());
      })
      .catch(() => {
        this.setData({ notifications: [], filteredList: [] });
      });
  },

  getIcon(type) {
    const map = {
      task: '📋', system: '🔔', activity: '🎉',
      TASK_CREATED: '📋', TASK_STATUS_CHANGED: '✅', TASK_COMPLETED: '🏆',
      SYSTEM: '🔔', ACTIVITY: '🎉'
    };
    return map[type] || '📢';
  },

  formatTime(iso) {
    if (!iso) return '';
    const d = new Date(iso);
    const now = new Date();
    const diff = now - d;
    if (diff < 60000) return '刚刚';
    if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`;
    const m = d.getMonth() + 1;
    const day = d.getDate();
    return `${m}/${day}`;
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ activeTab: tab }, () => this.applyFilter());
  },

  applyFilter() {
    const { activeTab, notifications } = this.data;
    let filtered;
    switch (activeTab) {
      case 'task':
        filtered = notifications.filter((n) =>
          n.type === 'task' || n.type === 'TASK_CREATED' ||
          n.type === 'TASK_STATUS_CHANGED' || n.type === 'TASK_COMPLETED');
        break;
      case 'system':
        filtered = notifications.filter((n) =>
          n.type === 'system' || n.type === 'SYSTEM');
        break;
      case 'activity':
        filtered = notifications.filter((n) =>
          n.type === 'activity' || n.type === 'ACTIVITY');
        break;
      default:
        filtered = notifications;
    }
    this.setData({ filteredList: filtered });
  },

  handleTapCard(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    if (this.data.batchMode) {
      this.toggleSelect(id);
    } else {
      put(API.notificationRead(id), {}, { showLoading: false, showError: false })
        .then(() => this.loadNotifications());
    }
  },

  toggleSelect(id) {
    const selected = this.data.selectedIds.slice();
    const idx = selected.indexOf(id);
    if (idx > -1) {
      selected.splice(idx, 1);
    } else {
      selected.push(id);
    }
    const isAllSelected = selected.length === this.data.filteredList.length && this.data.filteredList.length > 0;
    this.setData({ selectedIds: selected, isAllSelected });
  },

  toggleSelectAll() {
    const { isAllSelected, filteredList } = this.data;
    if (isAllSelected) {
      this.setData({ selectedIds: [], isAllSelected: false });
    } else {
      const ids = filteredList.map(n => n.id);
      this.setData({ selectedIds: ids, isAllSelected: true });
    }
  },

  exitBatchMode() {
    this.setData({ batchMode: false, selectedIds: [], isAllSelected: false });
  },

  handleMarkAllRead() {
    const userId = getUserId();
    put(API.notificationsReadAll(userId), {}, { showSuccess: true, successMessage: '已全部已读' })
      .then(() => this.loadNotifications());
  },

  handleBatchAction() {
    if (!this.data.batchMode) {
      if (this.data.filteredList.length === 0) {
        wx.showToast({ title: '暂无通知可操作', icon: 'none' });
        return;
      }
      this.setData({ batchMode: true, selectedIds: [], isAllSelected: false });
    } else {
      this.handleBatchDelete();
    }
  },

  handleBatchDelete() {
    const { selectedIds } = this.data;
    if (selectedIds.length === 0) {
      wx.showToast({ title: '请先选择通知', icon: 'none' });
      return;
    }
    wx.showModal({
      title: '确认删除',
      content: `确定删除选中的 ${selectedIds.length} 条通知吗？`,
      success: (res) => {
        if (res.confirm) {
          del(API.notificationBatchDelete, selectedIds, { showLoading: true })
            .then(() => {
              wx.showToast({ title: '已删除', icon: 'success' });
              this.setData({ batchMode: false, selectedIds: [], isAllSelected: false });
              this.loadNotifications();
            })
            .catch(() => {
              wx.showToast({ title: '删除失败', icon: 'none' });
            });
        }
      }
    });
  },

  handleBatchMarkRead() {
    const { selectedIds } = this.data;
    if (selectedIds.length === 0) {
      wx.showToast({ title: '请先选择通知', icon: 'none' });
      return;
    }
    const promises = selectedIds.map(id => put(API.notificationRead(id), {}, { showLoading: false, showError: false }));
    Promise.all(promises)
      .then(() => {
        wx.showToast({ title: '已标记已读', icon: 'success' });
        this.setData({ batchMode: false, selectedIds: [], isAllSelected: false });
        this.loadNotifications();
      })
      .catch(() => {
        wx.showToast({ title: '操作失败', icon: 'none' });
      });
  }
});
