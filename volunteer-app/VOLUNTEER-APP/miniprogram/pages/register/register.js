const { API } = require('../../utils/config');
const { post } = require('../../utils/request');

Page({
  data: {
    registerForm: {
      phone: '',
      password: '',
      confirmPassword: '',
      role: 'requester'
    },
    loading: false
  },

  handleInput(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({ [`registerForm.${field}`]: e.detail.value });
  },

  selectRole(e) {
    this.setData({ 'registerForm.role': e.currentTarget.dataset.role });
  },

  handleRegister() {
    const { phone, password, confirmPassword, role } = this.data.registerForm;

    if (!phone.trim()) {
      wx.showToast({ title: '请输入手机号', icon: 'none' });
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号码', icon: 'none' });
      return;
    }
    if (!password.trim() || password.length < 6 || password.length > 20) {
      wx.showToast({ title: '密码长度应在6-20个字符', icon: 'none' });
      return;
    }
    if (password !== confirmPassword) {
      wx.showToast({ title: '两次输入的密码不一致', icon: 'none' });
      return;
    }

    this.setData({ loading: true });

    post(API.authRegister, { username: phone, password: password }, { showLoading: false })
      .then(() => {
        wx.showToast({ title: '注册成功，请登录', icon: 'success' });
        setTimeout(() => wx.redirectTo({ url: '/pages/login/login' }), 1500);
      })
      .catch(() => {})
      .finally(() => this.setData({ loading: false }));
  },

  goToLogin() {
    wx.redirectTo({ url: '/pages/login/login' });
  },

  goToAgreement() {
    wx.navigateTo({ url: '/pages/user-agreement/user-agreement' });
  },

  goToPrivacy() {
    wx.navigateTo({ url: '/pages/privacy-policy/privacy-policy' });
  }
});
