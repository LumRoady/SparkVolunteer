const { API } = require('../../utils/config');
const { post } = require('../../utils/request');
const { getUserId, requireLogin } = require('../../utils/auth');

Page({
  data: {
    title: '',
    description: '',
    location: '',
    locating: false,
    needVisit: true,
    contact: '',
    submitting: false,
    canSubmit: false
  },

  onShow() {
    if (!requireLogin()) return;
    this.prefillUserInfo();
  },

  prefillUserInfo() {
    const userInfo = wx.getStorageSync('userInfo') || {};
    if (userInfo.address && !this.data.location) {
      this.setData({ location: userInfo.address });
    }
    if (userInfo.phone && !this.data.contact) {
      this.setData({ contact: userInfo.phone });
    }
  },

  onInput(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({ [field]: e.detail.value }, () => this.checkCanSubmit());
  },

  onSwitchChange(e) {
    this.setData({ needVisit: e.detail.value });
  },

  getLocation() {
    this.setData({ locating: true });
    wx.chooseLocation({
      success: (res) => {
        if (res.address) {
          this.setData({ location: res.address }, () => this.checkCanSubmit());
        }
      },
      fail: (err) => {
        if (err.errMsg && !err.errMsg.includes('cancel')) {
          wx.showToast({ title: '定位失败，请手动输入', icon: 'none' });
        }
      },
      complete: () => this.setData({ locating: false })
    });
  },

  checkCanSubmit() {
    const { title, description, location, contact } = this.data;
    this.setData({ canSubmit: title.trim() && description.trim() && location.trim() && contact.trim() });
  },

  submitTask() {
    const { title, description, location, contact, needVisit } = this.data;
    if (!title.trim() || !description.trim() || !location.trim() || !contact.trim()) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' });
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(contact)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    const userInfo = wx.getStorageSync('userInfo') || {};
    post(API.taskCreate, {
      title: title.trim(),
      content: description.trim(),
      location: location.trim() || userInfo.address || '未知地址',
      contactPhone: contact.trim() || userInfo.phone || '',
      type: 'sos',
      urgency: 2,
      needVisit: needVisit ? 1 : 0,
      requesterId: getUserId(),
      userId: getUserId(),
    }, { showSuccess: true, successMessage: '紧急求助已发布' })
      .then(() => {
        setTimeout(() => wx.navigateBack({ delta: 1 }), 1200);
      })
      .catch(() => wx.showToast({ title: '发布失败，请重试', icon: 'none' }))
      .finally(() => this.setData({ submitting: false }));
  }
});
