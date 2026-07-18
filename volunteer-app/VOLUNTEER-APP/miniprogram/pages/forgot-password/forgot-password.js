const request = require('../../utils/request');
const { API } = require('../../utils/config');

Page({
  data: {
    forgotForm: {
      phone: '',
      verificationCode: '',
      newPassword: '',
      confirmPassword: ''
    },
    loading: false,
    countdown: 0,
    timer: null
  },
  
  onLoad() {
    console.log('忘记密码页面加载完成')
  },
  
  handleInput(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({
      [`forgotForm.${field}`]: value
    })
  },
  
  sendVerificationCode() {
    const { phone } = this.data.forgotForm
    
    if (!phone) {
      wx.showToast({ title: '请输入手机号', icon: 'none' })
      return
    }
    
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号码', icon: 'none' })
      return
    }
    
    // 调用后端发送验证码
    request.post(API.authSendResetCode, { phone }, { showLoading: false })
      .then(() => {
        wx.showToast({ title: '验证码已发送', icon: 'success' })
        // 开始倒计时
        this.setData({ countdown: 60 })
        this.data.timer = setInterval(() => {
          if (this.data.countdown > 0) {
            this.setData({ countdown: this.data.countdown - 1 })
          } else {
            clearInterval(this.data.timer)
          }
        }, 1000)
      })
      .catch((err) => {
        console.error('发送验证码失败:', err)
      })
  },
  
  resetPassword() {
    const { phone, verificationCode, newPassword, confirmPassword } = this.data.forgotForm
    
    if (!phone) {
      wx.showToast({ title: '请输入手机号', icon: 'none' })
      return
    }
    
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号码', icon: 'none' })
      return
    }
    
    if (!verificationCode) {
      wx.showToast({ title: '请输入验证码', icon: 'none' })
      return
    }
    
    if (!newPassword) {
      wx.showToast({ title: '请输入新密码', icon: 'none' })
      return
    }
    
    if (newPassword.length < 6) {
      wx.showToast({ title: '新密码长度不能少于6位', icon: 'none' })
      return
    }
    
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次输入的密码不一致', icon: 'none' })
      return
    }
    
    this.setData({ loading: true })
    
    // 调用后端重置密码
    request.post(API.authResetPassword, {
      phone: phone,
      code: verificationCode,
      newPassword: newPassword
    }, { showLoading: false })
      .then(() => {
        this.setData({ loading: false })
        wx.showToast({ title: '密码重置成功', icon: 'success' })
        setTimeout(() => {
          wx.navigateTo({ url: '/pages/login/login' })
        }, 1500)
      })
      .catch((err) => {
        this.setData({ loading: false })
        console.error('重置密码失败:', err)
      })
  },
  
  goToLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },
  
  onUnload() {
    if (this.data.timer) {
      clearInterval(this.data.timer)
    }
  }
})
