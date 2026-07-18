const { API } = require('../../utils/config');
const { get } = require('../../utils/request');
const { requireLogin, getUserId } = require('../../utils/auth');

Page({
  data: {
    userInfo: {
      avatar: '',
      nickname: '志愿者',
      level: 1,
      region: ''
    },
    stats: {
      points: 0,
      completedTasks: 0,
      checkinDays: 0
    },
    badges: [
      { type: 'FIRST_TASK', name: '首次接单', icon: '🌟', unlocked: false },
      { type: 'TEN_TASKS', name: '累计10单', icon: '📋', unlocked: false },
      { type: 'SOS_RESPONSE', name: '紧急响应', icon: '🚨', unlocked: false },
      { type: 'LOVE_AMBASSADOR', name: '爱心大使', icon: '❤️', unlocked: false },
      { type: 'CHECKIN_30', name: '连续签到30天', icon: '🔥', unlocked: false },
      { type: 'COMMUNITY_STAR', name: '社区之星', icon: '🏆', unlocked: false }
    ]
  },

  onShow() {
    if (!requireLogin()) return;
    this.loadUserInfo();
    this.loadStats();
    this.loadAchievements();
    this.loadRegion();
  },

  loadRegion() {
    const { get } = require('../../utils/request');
    get('/api/location', {}, { showLoading: false, showError: false })
      .then((data) => {
        if (data && (data.regionName || data.city)) {
          const region = [data.regionName, data.city].filter(Boolean).join(' · ');
          this.setData({ 'userInfo.region': region });
        }
      })
      .catch(() => {});
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo') || {};
    this.setData({
      userInfo: {
        avatar: userInfo.avatar || '',
        nickname: userInfo.name || userInfo.nickname || '志愿者',
        level: userInfo.level || 1
      }
    });
  },

  loadStats() {
    const userId = getUserId();
    if (!userId) return;
    get(API.userStats(userId), {}, { showLoading: false, showError: false })
      .then((data) => {
        this.setData({
          stats: {
            points: data.points || 0,
            completedTasks: data.completedTasks || 0,
            checkinDays: data.checkinStreak || 0
          }
        });
      })
      .catch(() => {});
  },

  loadAchievements() {
    const userId = getUserId();
    if (!userId) return;
    get(API.userAchievements(userId), {}, { showLoading: false, showError: false })
      .then((data) => {
        const unlockedTypes = Array.isArray(data) ? data.map((a) => a.type) : [];
        const badges = this.data.badges.map((b) => ({
          ...b,
          unlocked: unlockedTypes.includes(b.type)
        }));
        this.setData({ badges });
      })
      .catch(() => {});
  },

});
