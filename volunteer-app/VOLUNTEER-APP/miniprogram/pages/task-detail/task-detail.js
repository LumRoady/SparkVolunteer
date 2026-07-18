const { API } = require('../../utils/config');
const { get, post, put } = require('../../utils/request');
const { formatTimeAgo, normalizeStatus, TASK_STATUS, getTaskTypeInfo, formatDate } = require('../../utils/taskUtils');
const { requireLogin, getUserId } = require('../../utils/auth');

Page({
  data: {
    task: null,
    publisher: {
      avatar: '',
      nickname: '',
      community: ''
    },
    loading: true,
    showContactModal: false
  },

  onLoad(options) {
    if (!requireLogin()) return;
    const taskId = options.no || options.id;
    if (!taskId || taskId === 'undefined') {
      wx.showToast({ title: '参数错误', icon: 'none' });
      this.setData({ loading: false });
      return;
    }
    this.loadTaskDetail(taskId);
  },

  onPullDownRefresh() {
    const pages = getCurrentPages();
    const options = pages[pages.length - 1].options;
    const taskId = options.no || options.id;
    if (taskId) {
      this.loadTaskDetail(taskId).then(() => wx.stopPullDownRefresh());
    } else {
      wx.stopPullDownRefresh();
    }
  },

  loadTaskDetail(id) {
    this.setData({ loading: true });
    return get(API.taskDetail(id), {}, { showLoading: false, showError: false })
      .then((task) => {
        if (!task) {
          this.setData({ task: null, loading: false });
          return;
        }
        const typeInfo = getTaskTypeInfo(task.type);
        const status = normalizeStatus(task.status);
        const statusTextMap = {
          PENDING: '待接单',
          ACCEPTED: '已接单',
          COMPLETED: '已完成'
        };
        const mappedTask = {
          id: task.id,
          title: task.title || '未命名任务',
          description: task.content || task.description || '暂无描述',
          type: task.type || 'normal',
          typeText: typeInfo.text,
          typeColor: typeInfo.color,
          location: task.location || '未知位置',
          createTime: formatDate(new Date(task.createTime || task.createdAt || new Date())),
          contactPhone: task.contactPhone || task.contact || '',
          status: status,
          statusText: statusTextMap[status] || '未知',
          urgency: task.urgency || '',
          needVisit: task.needVisit,
          requesterId: task.requesterId
        };
        this.setData({ task: mappedTask });
        // 加载发布者信息
        if (task.requesterId) {
          this.loadPublisher(task.requesterId);
        } else {
          this.setData({ loading: false });
        }
      })
      .catch(() => {
        this.setData({ task: null, loading: false });
      });
  },

  loadPublisher(requesterId) {
    get(API.userProfile(requesterId), {}, { showLoading: false, showError: false })
      .then((data) => {
        this.setData({
          publisher: {
            avatar: (data && data.avatar) || '',
            nickname: (data && (data.name || data.nickname)) || '老人',
            community: (data && data.community) || '未知社区'
          }
        });
      })
      .catch(() => {
        this.setData({
          publisher: { avatar: '', nickname: '老人', community: '未知社区' }
        });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  },

  handleAccept() {
    const task = this.data.task;
    if (!task) return;
    const userId = getUserId();
    if (!userId) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      setTimeout(() => wx.redirectTo({ url: '/pages/login/login' }), 1000);
      return;
    }
    post(API.taskAccept(task.id), { taskId: task.id, volunteerId: userId })
      .then(() => {
        // 更新状态
        this.setData({
          'task.status': TASK_STATUS.ACCEPTED,
          'task.statusText': '已接单',
          showContactModal: true
        });
      })
      .catch(() => {});
  },

  handleContact() {
    this.setData({ showContactModal: true });
  },

  handleComplete() {
    const task = this.data.task;
    if (!task) return;
    wx.showModal({
      title: '确认完成',
      content: '确认该任务已完成？',
      success: (res) => {
        if (!res.confirm) return;
        post(API.taskStatus, { taskId: task.id, status: TASK_STATUS.COMPLETED, volunteerId: getUserId() })
          .then(() => {
            this.setData({
              'task.status': TASK_STATUS.COMPLETED,
              'task.statusText': '已完成'
            });
            wx.showToast({ title: '任务已完成', icon: 'success' });
          })
          .catch(() => {});
      }
    });
  },

  makePhoneCall() {
    const phone = this.data.task && this.data.task.contactPhone;
    if (!phone) {
      wx.showToast({ title: '暂无手机号', icon: 'none' });
      return;
    }
    wx.makePhoneCall({ phoneNumber: phone });
  },

  closeModal() {
    this.setData({ showContactModal: false });
  }
});
