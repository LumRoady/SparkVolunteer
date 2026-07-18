const { requireLogin } = require('../../utils/auth');
const request = require('../../utils/request');
const { API } = require('../../utils/config');

Page({
  data: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
    hasNumber: false,
    hasLetter: false
  },
  onLoad() {
    if (!requireLogin()) return;
    console.log('修改密码页面加载完成')
  },
  onOldPasswordChange(e) {
    this.setData({ oldPassword: e.detail.value })
  },
  onNewPasswordChange(e) {
    const newPassword = e.detail.value;
    const hasNumber = /\d/.test(newPassword);
    const hasLetter = /[a-zA-Z]/.test(newPassword);
    this.setData({ newPassword, hasNumber, hasLetter })
  },
  onConfirmPasswordChange(e) {
    this.setData({ confirmPassword: e.detail.value })
  },
  confirmChangePassword() {
    const { oldPassword, newPassword, confirmPassword } = this.data
    
    if (!oldPassword) {
      wx.showToast({ title: '请输入原密码', icon: 'none' })
      return
    }
    if (!newPassword) {
      wx.showToast({ title: '请输入新密码', icon: 'none' })
      return
    }
    if (newPassword.length < 6) {
      wx.showToast({ title: '新密码长度至少6位', icon: 'none' })
      return
    }
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次输入的密码不一致', icon: 'none' })
      return
    }
    
    wx.showLoading({ title: '修改中...' })
    
    // 调用后端修改密码
    request.post(API.authChangePassword, {
      oldPassword: oldPassword,
      newPassword: newPassword
    }, { showLoading: false })
      .then(() => {
        wx.hideLoading()
        wx.showToast({ title: '密码修改成功', icon: 'success' })
        setTimeout(() => { wx.navigateBack() }, 1500)
      })
      .catch((err) => {
        wx.hideLoading()
        console.error('修改密码失败:', err)
      })
  }
})