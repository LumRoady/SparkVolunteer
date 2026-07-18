const { requireLogin } = require('../../utils/auth');
const request = require('../../utils/request');
const { API } = require('../../utils/config');

Page({
  data: {
    phone: '',
    code: '',
    countdown: 0
  },
  onLoad() {
    if (!requireLogin()) return;
    console.log('绑定手机页面加载完成')
  },
  onPhoneChange(e) {
    this.setData({ phone: e.detail.value })
  },
  onCodeChange(e) {
    this.setData({ code: e.detail.value })
  },
  getVerificationCode() {
    const { phone } = this.data
    
    if (!phone) {
      wx.showToast({ title: '请输入手机号码', icon: 'none' })
      return
    }
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号码', icon: 'none' })
      return
    }
    
    // 调用后端发送绑定验证码
    request.post(API.authSendBindCode, { phone }, { showLoading: false })
      .then(() => {
        wx.showToast({ title: '验证码已发送', icon: 'success' })
        this.setData({ countdown: 60 })
        const timer = setInterval(() => {
          this.setData({ countdown: this.data.countdown - 1 })
          if (this.data.countdown <= 0) {
            clearInterval(timer)
          }
        }, 1000)
      })
      .catch((err) => {
        console.error('发送验证码失败:', err)
      })
  },
  confirmBindPhone() {
    const { phone, code } = this.data
    
    if (!phone) {
      wx.showToast({ title: '请输入手机号码', icon: 'none' })
      return
    }
    if (!code) {
      wx.showToast({ title: '请输入验证码', icon: 'none' })
      return
    }
    if (code.length !== 6) {
      wx.showToast({ title: '请输入6位验证码', icon: 'none' })
      return
    }
    
    wx.showLoading({ title: '绑定中...' })
    
    // 调用后端绑定手机
    request.post(API.authBindPhone, { phone, code }, { showLoading: false })
      .then(() => {
        wx.hideLoading()
        wx.showToast({ title: '绑定成功', icon: 'success' })
        setTimeout(() => { wx.navigateBack() }, 1500)
      })
      .catch((err) => {
        wx.hideLoading()
        console.error('绑定手机失败:', err)
      })
  }
})