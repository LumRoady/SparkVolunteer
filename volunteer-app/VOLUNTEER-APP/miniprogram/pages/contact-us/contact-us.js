const { requireLogin } = require('../../utils/auth');
const request = require('../../utils/request');
const { API } = require('../../utils/config');

Page({
  data: {
    feedback: ''
  },
  onLoad() {
    console.log('联系我们页面加载完成')
  },
  copyEmail() {
    // 复制邮箱
    wx.setClipboardData({
      data: 'contact@sparkvolunteer.com',
      success: function(res) {
        wx.showToast({
          title: '邮箱已复制',
          icon: 'success'
        })
      }
    })
  },
  makePhoneCall() {
    // 拨打电话
    wx.makePhoneCall({
      phoneNumber: '4001234567',
      success: function(res) {
        console.log('拨打电话成功', res)
      },
      fail: function(res) {
        console.log('拨打电话失败', res)
      }
    })
  },
  openOnlineService() {
    // 打开在线客服
    wx.showToast({
      title: '在线客服功能开发中',
      icon: 'none',
      duration: 2000
    })
  },
  onFeedbackChange(e) {
    // 意见反馈变化
    this.setData({
      feedback: e.detail.value
    })
  },
  submitFeedback() {
    // 提交意见反馈
    const { feedback } = this.data
    
    if (!feedback) {
      wx.showToast({
        title: '请输入您的意见或建议',
        icon: 'none'
      })
      return
    }
    
    // 调用后端提交反馈
    wx.showLoading({ title: '提交中...' })
    
    request.post(API.feedbackSubmit, { content: feedback }, { showLoading: false })
      .then(() => {
        wx.hideLoading()
        wx.showToast({ title: '反馈提交成功', icon: 'success' })
        this.setData({ feedback: '' })
      })
      .catch((err) => {
        wx.hideLoading()
        console.error('提交反馈失败:', err)
      })
  }
})