const { API } = require('../../utils/config');
const { get, post } = require('../../utils/request');
const { getUserId, requireLogin } = require('../../utils/auth');

const app = getApp();

Page({
  data: {
    userInfo: { name: '志愿者', avatar: '', role: '志愿者', region: '', organizations: [] },
    stats: { completedTasks: 0, points: 0, checkinStreak: 0, level: 1 },
    userStats: { points: 0 },
    isCheckedIn: false,
    checkinStreak: 0,
    fontSizeClass: 'page-medium'
  },

  onLoad() {
    this.setData({
      fontSizeClass: app.getFontSizeClass(app.globalData.fontSize)
    });
  },

  onShow() {
    if (!requireLogin()) return;
    this.loadUserInfo();
    this.loadDashboard();
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo') || {};
    this.setData({
      userInfo: {
        name: userInfo.name || userInfo.nickname || '志愿者',
        avatar: userInfo.avatar || '',
        role: userInfo.role === 'ELDERLY' ? '求助者' : '志愿者',
        region: userInfo.community || userInfo.region || '',
        organizations: userInfo.organizations || []
      }
    });
  },

  loadDashboard() {
    const userId = getUserId();
    if (!userId) return;

    get(API.userStats(userId), {}, { showLoading: false })
      .then((data) => {
        this.setData({
          stats: {
            completedTasks: data.completedTasks || 0,
            points: data.points || 0,
            checkinStreak: data.checkinStreak || 0,
            level: data.level || 1
          },
          userStats: { points: data.points || 0 },
          checkinStreak: data.checkinStreak || 0
        });
        // 单独查今日签到状态
        get(API.checkinStatus(userId), {}, { showLoading: false, showError: false })
          .then((checkedIn) => {
            this.setData({ isCheckedIn: !!checkedIn });
          })
          .catch(() => {});
      })
      .catch(() => {});
  },

  checkIn() {
    if (this.data.isCheckedIn) {
      wx.showToast({ title: '今日已签到', icon: 'none' });
      return;
    }
    const userId = getUserId();
    if (!userId) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    this.setData({ isCheckedIn: true });
    post(`${API.checkin}?userId=${userId}`, {}, { showError: false })
      .then((data) => {
        wx.showToast({ title: '签到成功 +10积分', icon: 'success' });
        this.loadDashboard();
      })
      .catch(() => {
        this.setData({ isCheckedIn: false });
        wx.showToast({ title: '明天再来试试吧~', icon: 'none' });
      });
  },

  goToMyTasks() {
    wx.navigateTo({ url: '/pages/my-tasks/my-tasks' });
  },

  goToMyHonors() {
    wx.navigateTo({ url: '/pages/my-honors/my-honors' });
  },

  showPointsAchievements() {
    wx.navigateTo({ url: '/pages/points-achievements/points-achievements' });
  },

  goToSettings() {
    wx.navigateTo({ url: '/pages/settings/settings' });
  }
});
