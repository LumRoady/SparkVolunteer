const { API } = require('../../utils/config');
const { post } = require('../../utils/request');
const { saveSession } = require('../../utils/auth');

Page({
  data: {
    phone: '',
    password: '',
    submitting: false,
    canSubmit: false
  },

  onLoad() {
    // 如果已登录，直接跳转首页
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (token && userInfo && userInfo.id) {
      wx.switchTab({ url: '/pages/index/index' });
    }
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value }, () => this.checkCanSubmit());
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value }, () => this.checkCanSubmit());
  },

  checkCanSubmit() {
    const { phone, password } = this.data;
    this.setData({ canSubmit: phone.length === 11 && password.length >= 6 });
  },

  handleLogin() {
    const { phone, password } = this.data;

    if (!/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }
    if (password.length < 6) {
      wx.showToast({ title: '密码至少6位', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });

    // 后端 LoginRequest 字段名为 username，但支持手机号作为账号
    post(API.authLogin, { username: phone, password }, { showLoading: true, loadingText: '登录中...' })
      .then((data) => {
        saveSession(data);
        getApp().initWebSocket();
        wx.showToast({ title: '登录成功', icon: 'success' });
        setTimeout(() => {
          wx.switchTab({ url: '/pages/index/index' });
        }, 800);
      })
      .catch(() => {})
      .finally(() => this.setData({ submitting: false }));
  },

  goToRegister() {
    wx.navigateTo({ url: '/pages/register/register' });
  },

  goToForgotPassword() {
    wx.navigateTo({ url: '/pages/forgot-password/forgot-password' });
  },

  goToUserAgreement() {
    wx.navigateTo({ url: '/pages/user-agreement/user-agreement' });
  },

  goToPrivacyPolicy() {
    wx.navigateTo({ url: '/pages/privacy-policy/privacy-policy' });
  }
});
