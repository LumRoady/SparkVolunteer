const { API } = require('../../utils/config');
const { post } = require('../../utils/request');
const { getUserId, requireLogin } = require('../../utils/auth');

Page({
  data: {
    // 页面主题（由入口按钮决定）
    taskType: 'emergency',
    theme: null,       // { name, color, gradient, icon, title, hint }

    // 表单
    title: '',
    description: '',
    location: '',
    urgencyIndex: 1,
    urgencyOptions: ['不紧急', '一般', '紧急', '非常紧急'],
    needVisit: false,
    contact: '',

    submitting: false,
    canSubmit: false,
  },

  onLoad(options) {
    const type = options.type || 'life_service';
    const themes = {
      emergency: {
        name: '紧急求助',
        color: '#ff4757',
        gradient: 'linear-gradient(135deg, #ff4757 0%, #e84151 100%)',
        icon: '🚨',
        title: '紧急求助',
        hint: 'SOS 紧急呼救，志愿者将优先响应',
      },
      life_service: {
        name: '生活服务',
        color: '#ff6b6b',
        gradient: 'linear-gradient(135deg, #ff6b6b 0%, #e55a5a 100%)',
        icon: '🛒',
        title: '生活服务',
        hint: '买菜、送药、维修等日常帮助',
      },
      consultation: {
        name: '日常咨询',
        color: '#2ed573',
        gradient: 'linear-gradient(135deg, #2ed573 0%, #1eaa5a 100%)',
        icon: '💬',
        title: '日常咨询',
        hint: '聊天陪伴、手机教学、健康咨询',
      },
    };
    this.setData({ taskType: type, theme: themes[type] || themes.life_service });
  },

  onShow() {
    if (!requireLogin()) return;
    this.checkCanSubmit();
  },

  // ---------- 输入处理 ----------
  onInput(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({ [field]: e.detail.value }, () => this.checkCanSubmit());
  },

  onUrgencyChange(e) {
    this.setData({ urgencyIndex: parseInt(e.detail.value) });
  },

  onSwitchChange(e) {
    this.setData({ needVisit: e.detail.value });
  },

  // ---------- 表单校验 ----------
  checkCanSubmit() {
    const { title, description, location, contact } = this.data;
    const can = title.trim() && description.trim() && location.trim() && contact.trim();
    this.setData({ canSubmit: can });
  },

  // ---------- 提交任务 ----------
  submitTask() {
    const { title, description, location, taskType, needVisit, contact, urgencyIndex, urgencyOptions } = this.data;

    if (!title.trim() || !description.trim() || !location.trim() || !contact.trim()) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' });
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(contact)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    const urgencyMap = { 0: 0, 1: 0, 2: 1, 3: 2 };
    const userInfo = wx.getStorageSync('userInfo') || {};

    post(API.taskCreate, {
      title: title.trim(),
      content: description.trim(),
      location: location.trim() || userInfo.address || '未知地址',
      contactPhone: contact.trim() || userInfo.phone || '',
      type: taskType,
      urgency: urgencyMap[urgencyIndex] || 0,
      needVisit: needVisit ? 1 : 0,
      requesterId: getUserId(),
      userId: getUserId(),
    }, { showSuccess: true, successMessage: '发布成功' })
      .then(() => {
        setTimeout(() => wx.switchTab({ url: '/pages/tasks/tasks' }), 1200);
      })
      .catch(() => {
        wx.showToast({ title: '发布失败，请重试', icon: 'none' });
      })
      .finally(() => this.setData({ submitting: false }));
  },
});
