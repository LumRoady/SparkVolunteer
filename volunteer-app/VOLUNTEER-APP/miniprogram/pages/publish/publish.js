const { API } = require('../../utils/config');
const { post } = require('../../utils/request');
const { requireLogin, getUserId } = require('../../utils/auth');

Page({
  data: {
    formData: {
      title: '',
      description: '',
      location: '',
      contact: '',
      taskType: 'normal',
      reward: ''
    },
    submitting: false,
    canSubmit: false
  },

  onLoad() {
    requireLogin();
  },

  handleInput(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({ [`formData.${field}`]: e.detail.value }, () => this.checkCanSubmit());
  },

  handleTaskTypeChange(e) {
    this.setData({ 'formData.taskType': e.detail.value }, () => this.checkCanSubmit());
  },

  checkCanSubmit() {
    const { title, description, location, contact, taskType, reward } = this.data.formData;
    let canSubmit = title.trim() && description.trim() && location.trim() && contact.trim();
    if (taskType === 'challenge') {
      canSubmit = canSubmit && reward.trim();
    }
    this.setData({ canSubmit });
  },

  publishTask() {
    const { title, description, location, contact, taskType } = this.data.formData;

    if (!title.trim() || !description.trim() || !location.trim() || !contact.trim()) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' });
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(contact)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });

    post(API.taskCreate, {
      title,
      content: description,
      location,
      contactPhone: contact,
      type: taskType === 'challenge' ? 'normal' : taskType,
      requesterId: getUserId(),
      userId: getUserId()
    })
      .then(() => {
        wx.showToast({ title: '发布成功', icon: 'success' });
        setTimeout(() => wx.navigateBack({ delta: 1 }), 1200);
      })
      .catch(() => {})
      .finally(() => this.setData({ submitting: false }));
  }
});
