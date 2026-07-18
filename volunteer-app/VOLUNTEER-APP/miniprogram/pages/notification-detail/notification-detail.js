const { requireLogin } = require('../../utils/auth');
const request = require('../../utils/request');
const { API } = require('../../utils/config');

Page({
  data: {
    notification: {
      id: null,
      icon: '📢',
      title: '',
      content: '',
      time: '',
      read: false,
      relatedTask: false
    }
  },
  
  onLoad(options) {
    if (!requireLogin()) return;
    console.log('通知详情页面加载完成', options);
    
    if (options.id) {
      this.getNotificationDetail(options.id);
    }
  },
  
  getNotificationDetail(id) {
    // 从后端 API 获取通知详情
    const url = `/api/notifications/${id}`;
    request.get(url, {}, { showLoading: false })
      .then((data) => {
        if (!data) return;
        const createTime = data.createTime ? new Date(data.createTime) : new Date();
        const timeStr = `${String(createTime.getHours()).padStart(2,'0')}:${String(createTime.getMinutes()).padStart(2,'0')}`;
        
        // 根据通知类型选择图标
        let icon = '📢';
        if (data.type === 'task_accepted' || data.type === 'task_completed') icon = '✅';
        if (data.type === 'ranking') icon = '🏆';
        if (data.type === 'sos') icon = '🚨';
        
        this.setData({
          notification: {
            id: data.id,
            icon: icon,
            title: data.title || '系统通知',
            content: data.content || data.message || '',
            time: timeStr,
            read: data.read || data.isRead || false,
            relatedTask: !!data.taskId
          }
        });

        wx.setNavigationBarTitle({ title: data.title || '通知详情' });

        // 自动标记已读
        if (!data.read && !data.isRead) {
          this.markAsRead();
        }
      })
      .catch((err) => {
        console.error('获取通知详情失败:', err);
      });
  },
  
  markAsRead() {
    const { id } = this.data.notification;
    if (!id) return;
    
    request.put(`/api/notifications/${id}/read`, {}, { showLoading: false, showError: false })
      .then(() => {
        this.setData({ 'notification.read': true });
      })
      .catch(() => {
        // 静默处理
      });
  },
  
  goToRelatedTask() {
    console.log('查看相关任务');
    wx.navigateTo({
      url: '/pages/task-detail/task-detail?no=1',
      success: function(res) {
        console.log('成功跳转到任务详情页面', res);
      },
      fail: function(res) {
        console.log('跳转到任务详情页面失败', res);
      }
    });
  }
});
