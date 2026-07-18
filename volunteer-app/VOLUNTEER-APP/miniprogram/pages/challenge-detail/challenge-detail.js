const { requireLogin } = require('../../utils/auth');
Page({
  data: {
    challengeTitle: '',
    challengeDescription: '',
    challengeReward: 0,
    challengeCompleted: false,
    challengeIndex: 0,
    challengeContact: '',
    challengeAddress: ''
  },
  
  onLoad(options) {
    if (!requireLogin()) return;
    // 页面加载时执行
    console.log('挑战详情页加载完成', options);
    
    // 从URL参数获取挑战信息
    this.setData({
      challengeIndex: options.index,
      challengeTitle: decodeURIComponent(options.title),
      challengeDescription: decodeURIComponent(options.description),
      challengeReward: options.reward,
      challengeCompleted: options.completed === 'true',
      challengeContact: options.contact,
      challengeAddress: decodeURIComponent(options.address)
    });
  },
  
  cancelChallenge() {
    // 取消挑战
    wx.showModal({
      title: '取消挑战',
      content: '确定要取消这个挑战吗？',
      success: function(res) {
        if (res.confirm) {
          // 返回上一页
          wx.navigateBack({
            delta: 1
          });
        }
      }
    });
  },
  
  changeTask() {
    // 更换任务
    wx.navigateBack({
      delta: 1
    });
  },
  
  goBack() {
    // 返回上一页
    wx.navigateBack({
      delta: 1
    });
  }
});
