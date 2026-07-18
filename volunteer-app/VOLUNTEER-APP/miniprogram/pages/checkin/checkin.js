const { API } = require('../../utils/config');
const { get, post, delete: httpDelete } = require('../../utils/request');
const { getUserId, requireLogin } = require('../../utils/auth');

Page({
  data: {
    challenges: [],
    allChallenges: [],
    completedCount: 0,
    totalCount: 3,
    userPoints: 0,
    feedbackType: '',
    description: '',
    feedbackHistory: []
  },

  onShow() {
    if (!requireLogin()) return;
    this.loadChallenges();
    this.loadUserStats();
    this.loadFeedbackHistory();
  },

  async loadChallenges() {
    const userId = getUserId();
    if (!userId) return;
    
    try {
      const data = await get(API.challengeList, {}, { showLoading: false, showError: false });
      // 兼容 PageResponse（含 content）和直接数组两种格式
      const all = Array.isArray(data) ? data : (data && data.content ? data.content : []);
      if (all.length > 0) {
        // 首页只展示最多3个未完成
        const display = all.filter(c => !c.completed).slice(0, 3);
        const displayList = display.length > 0 ? display : all.slice(0, 3);
        const completedCount = displayList.filter(c => c.completed).length;
        this.setData({
          allChallenges: all,
          challenges: displayList,
          completedCount,
          totalCount: displayList.length
        });
      } else {
        this.setData({ allChallenges: [], challenges: [], completedCount: 0, totalCount: 0 });
      }
    } catch (error) {
      console.error('加载挑战列表失败:', error);
      this.setData({
        challenges: [
          { id: 1, title: '帮助老人打扫房间', description: '为社区内的独居老人打扫房间，保持环境整洁', reward: 20, completed: false },
          { id: 2, title: '陪伴老人聊天', description: '与社区内的老人聊天，给予情感支持', reward: 15, completed: false },
          { id: 3, title: '教老人使用智能手机', description: '帮助老人学习使用智能手机的基本功能', reward: 25, completed: false }
        ],
        completedCount: 0,
        totalCount: 3
      });
    }
  },

  async loadUserStats() {
    const userId = getUserId();
    if (!userId) return;
    
    try {
      const data = await get(API.userStats(userId), {}, { showLoading: false, showError: false });
      this.setData({
        userPoints: data.points || 0
      });
    } catch (error) {
      console.error('加载用户信息失败:', error);
      this.setData({
        userPoints: 0
      });
    }
  },

  async loadFeedbackHistory() {
    try {
      if (!API.feedbackList) {
        // 接口已移除，使用默认 mock 数据
        return;
      }
      const data = await get(API.feedbackList, {}, { showLoading: false, showError: false });
      if (data && Array.isArray(data)) {
        this.setData({ feedbackHistory: data });
      }
    } catch (error) {
      console.error('加载反馈历史失败:', error);
      this.setData({
        feedbackHistory: [
          { id: 1, type: '生活服务', description: '希望社区能增加更多的健身设施', date: '2026-02-11', status: '已处理' },
          { id: 2, type: '健康医疗', description: '建议社区定期组织健康讲座', date: '2026-02-10', status: '处理中' }
        ]
      });
    }
  },

  handleChangeTasks() {
    const all = this.data.allChallenges || [];
    if (all.length <= 3) {
      wx.showToast({ title: '暂无更多任务可更换', icon: 'none' });
      return;
    }
    // 随机抽3个
    const shuffled = all.slice().sort(() => Math.random() - 0.5);
    const picked = shuffled.slice(0, 3);
    const completedCount = picked.filter(c => c.completed).length;
    this.setData({
      challenges: picked,
      completedCount,
      totalCount: picked.length
    });
    wx.showToast({ title: '已更换任务', icon: 'success' });
  },

  handleViewDetail(e) {
    const challenge = this.data.challenges[e.currentTarget.dataset.index];
    if (!challenge) return;
    
    wx.navigateTo({
      url: `/pages/challenge-detail/challenge-detail?index=${e.currentTarget.dataset.index}&title=${encodeURIComponent(challenge.title)}&description=${encodeURIComponent(challenge.description)}&reward=${challenge.reward}&completed=${challenge.completed || false}&contact=${challenge.contact || ''}&address=${encodeURIComponent(challenge.address || '')}`
    });
  },

  async handleCancelChallenge(e) {
    const id = e.currentTarget.dataset.id;
    
    const that = this;
    wx.showModal({
      title: '取消挑战',
      content: '确定要取消这个挑战吗？',
      success(res) {
        if (res.confirm) {
          httpDelete(API.challengeDelete(id), {}, { showSuccess: true })
            .then(() => {
              that.loadChallenges();
            })
            .catch(error => {
              console.error('取消挑战失败:', error);
            });
        }
      }
    });
  },

  handleTypeInput(e) {
    this.setData({
      feedbackType: e.detail.value
    });
  },

  handleDescInput(e) {
    this.setData({
      description: e.detail.value
    });
  },

  handleSubmitFeedback() {
    const { feedbackType, description } = this.data;
    
    if (!feedbackType || feedbackType.trim() === '') {
      wx.showToast({
        title: '请填写需求类型',
        icon: 'none'
      });
      return;
    }

    if (!description || description.trim() === '') {
      wx.showToast({
        title: '请填写需求描述',
        icon: 'none'
      });
      return;
    }

    const userId = getUserId();
    post(API.feedbackSubmit, {
      userId,
      type: feedbackType,
      description
    }).then(() => {
      wx.showToast({
        title: '提交成功',
        icon: 'success'
      });
      this.setData({
        feedbackType: '',
        description: ''
      });
      this.loadFeedbackHistory();
    }).catch(() => {
      wx.showToast({
        title: '提交失败',
        icon: 'none'
      });
    });
  }
});