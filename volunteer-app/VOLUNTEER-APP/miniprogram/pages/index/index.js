const { API } = require('../../utils/config');
const { get } = require('../../utils/request');
const { requireLogin } = require('../../utils/auth');

Page({
  data: {
    role: '',
    viewMode: 'volunteer',   // 'volunteer' | 'elderly'  视角切换
    activeTab: 'accept',
    rankList: [],
    showAll: false,
    myTasks: [],
    banners: [
      { id: 1, src: '../../images/banner_1.jpg' },
      { id: 2, src: '../../images/banner_2.jpg' },
      { id: 3, src: '../../images/banner_3.jpg' }
    ]
  },

  onShow() {
    if (!requireLogin()) return;
    const role = wx.getStorageSync('userRole') || 'VOLUNTEER';
    const viewMode = role === 'ELDERLY' ? 'elderly' : 'volunteer';
    this.setData({ role, viewMode });
    this.loadCurrentView();
  },

  onPullDownRefresh() {
    this.loadCurrentView().then(() => wx.stopPullDownRefresh());
  },

  /** 切换视角 */
  switchViewMode(e) {
    const mode = e.currentTarget.dataset.mode;
    if (mode === this.data.viewMode) return;
    this.setData({ viewMode: mode });
    this.loadCurrentView();
  },

  /** 根据当前视角加载数据 */
  loadCurrentView() {
    if (this.data.viewMode === 'elderly') {
      return this.loadMyTasks();
    } else {
      return this.loadRanking();
    }
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab }, () => this.loadRanking());
  },

  loadRanking() {
    return get(API.ranking(this.data.activeTab), {}, { showLoading: false, showError: false })
      .then((data) => this.setData({ rankList: Array.isArray(data) ? data : [] }))
      .catch(() => this.setData({ rankList: [] }));
  },

  loadMyTasks() {
    const userId = wx.getStorageSync('userInfo')?.id;
    if (!userId) return Promise.resolve();
    return get(API.userHistory(userId), {}, { showLoading: false, showError: false })
      .then((data) => {
        const tasks = (data?.content || data || []).slice(0, 10).map(t => ({
          ...t,
          statusText: {PENDING:'待接单',ACCEPTED:'已接单',COMPLETED:'已完成',CANCELLED:'已取消',IN_PROGRESS:'进行中'}[t.status]||'未知',
          typeText: {sos:'紧急',emergency:'紧急',life_service:'生活',consultation:'咨询'}[t.type]||t.type
        }));
        this.setData({ myTasks: tasks });
      })
      .catch(() => this.setData({ myTasks: [] }));
  },

  goToTasks() { wx.navigateTo({ url: '/pages/tasks/tasks' }); },
  goToPublish() { wx.navigateTo({ url: '/pages/publish-task/publish-task' }); },
  goToMyTaskDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/task-detail/task-detail?id=${id}` });
  },
  quickPublish(e) {
    const type = e.currentTarget.dataset.type;
    const pageMap = {
      emergency: '/pages/emergency-publish/emergency-publish',
      life_service: '/pages/life-service-publish/life-service-publish',
      consultation: '/pages/consultation-publish/consultation-publish'
    };
    const url = pageMap[type] || '/pages/publish-task/publish-task?type=' + type;
    wx.navigateTo({ url });
  },
  toggleShowAll() { this.setData({ showAll: !this.data.showAll }); }
});
