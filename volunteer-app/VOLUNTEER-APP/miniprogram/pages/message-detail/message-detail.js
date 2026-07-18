const { requireLogin } = require('../../utils/auth');
const request = require('../../utils/request');
const { API } = require('../../utils/config');
const { getUserId } = require('../../utils/auth');

Page({
  data: {
    message: {
      id: 1,
      avatar: '',
      sender: '',
      content: '',
      time: '',
      unread: false,
      unreadCount: 0
    },
    messageHistory: [],
    inputMessage: '',
    showContactModal: false,
    contact: '',
    isVoiceMode: false,
    recording: false,
    conversationId: null,
    peerUserId: null
  },
  
  onLoad(options) {
    if (!requireLogin()) return;
    console.log('消息详情页面加载完成', options);
    
    if (options.id) {
      this.setData({ conversationId: options.id, peerUserId: options.id });
      this.loadMessageHistory(options.id);
    }
    
    if (options.contact) {
      this.setData({ contact: options.contact, showContactModal: true });
    }

    if (options.name) {
      wx.setNavigationBarTitle({ title: options.name });
      this.setData({ 'message.sender': options.name, 'message.avatar': options.name.charAt(0) });
    }
  },
  
  loadMessageHistory(conversationId) {
    const userId = getUserId();
    if (!userId) return;
    
    request.get(API.messageList(conversationId, userId), {}, { showLoading: false })
      .then((data) => {
        const content = Array.isArray(data) ? data : (data && data.content ? data.content : []);
        const history = content.map((msg) => {
          const isSent = msg.senderId == userId;
          const time = msg.createTime ? new Date(msg.createTime) : new Date();
          const timeStr = `${String(time.getHours()).padStart(2,'0')}:${String(time.getMinutes()).padStart(2,'0')}`;
          return {
            id: msg.id,
            senderAvatar: isSent ? '我' : (msg.senderNickname || '').charAt(0) || '?',
            content: msg.content || '',
            time: timeStr,
            type: isSent ? 'sent' : 'received'
          };
        });
        this.setData({ messageHistory: history });
      })
      .catch((err) => {
        console.error('加载消息历史失败:', err);
        // 降级显示空列表
        this.setData({ messageHistory: [] });
      });
  },
  
  getMessageDetail(id) {
    // 兼容旧调用，转接到 loadMessageHistory
    this.loadMessageHistory(id);
  },
  
  handleInput(e) {
    this.setData({ inputMessage: e.detail.value });
  },
  
  sendMessage() {
    const message = this.data.inputMessage.trim();
    if (!message) return;
    
    const userId = getUserId();
    const peerId = this.data.peerUserId || this.data.conversationId;
    if (!userId || !peerId) {
      wx.showToast({ title: '无法确定聊天对象', icon: 'none' });
      return;
    }
    
    // 调用后端发送消息
    request.post(API.messageSend, {
      senderId: userId,
      receiverId: parseInt(peerId),
      content: message
    }, { showLoading: false })
      .then((data) => {
        const now = new Date();
        const time = `${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`;
        const newMessage = {
          id: data ? data.id : Date.now(),
          senderAvatar: '我',
          content: message,
          time: time,
          type: 'sent'
        };
        this.setData({
          messageHistory: [...this.data.messageHistory, newMessage],
          inputMessage: ''
        });
      })
      .catch((err) => {
        console.error('发送消息失败:', err);
        // 降级：本地显示消息
        const now = new Date();
        const time = `${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`;
        this.setData({
          messageHistory: [...this.data.messageHistory, {
            id: Date.now(), senderAvatar: '我', content: message, time, type: 'sent'
          }],
          inputMessage: ''
        });
      });
  },
  
  makePhoneCall() {
    const { contact } = this.data
    wx.makePhoneCall({
      phoneNumber: contact,
      success: () => console.log('拨打电话成功'),
      fail: (err) => console.log('拨打电话失败', err)
    })
  },
  
  closeContactModal() {
    this.setData({ showContactModal: false })
  },
  
  toggleVoiceMode() {
    this.setData({ isVoiceMode: !this.data.isVoiceMode });
  },
  
  startRecording() {
    console.log('开始录音');
    this.setData({ recording: true });
    wx.showToast({ title: '开始录音', icon: 'none', duration: 1000 });
  },
  
  stopRecording() {
    console.log('停止录音');
    this.setData({ recording: false });
    wx.showToast({ title: '录音完成', icon: 'success', duration: 1000 });
    
    const now = new Date();
    const time = `${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`;
    this.setData({
      messageHistory: [...this.data.messageHistory, {
        id: Date.now(), senderAvatar: '我', content: '[语音消息]', time, type: 'sent'
      }]
    });
  }
});
