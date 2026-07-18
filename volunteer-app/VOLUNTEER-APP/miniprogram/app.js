var getWsUrl = require('./utils/config').getWsUrl;
var auth = require('./utils/auth');
var restoreSession = auth.restoreSession;
var guardPage = auth.guardPage;
var isLoggedIn = auth.isLoggedIn;

App({
  onLaunch: function () {
    var app = this;
    restoreSession(app);

    wx.getStorage({
      key: 'userLanguage',
      success: function (res) { app.globalData.language = res.data; },
      fail: function () { app.globalData.language = 'zh-CN'; }
    });

    wx.getStorage({
      key: 'fontSize',
      success: function (res) {
        app.globalData.fontSize = res.data;
        app.setPageFontSize(res.data);
      },
      fail: function () {
        app.globalData.fontSize = 1;
        app.setPageFontSize(1);
      }
    });

    if (isLoggedIn(app)) {
      app.initWebSocket();
    }
  },

  onShow: function () {
    var app = this;
    app.setPageFontSize(app.globalData.fontSize);
    guardPage(app);
    if (isLoggedIn(app)) {
      app.initWebSocket();
    }
  },

  setPageFontSize: function (fontSize) {
    var app = this;
    getCurrentPages().forEach(function (page) {
      if (page.setData) {
        page.setData({ fontSizeClass: app.getFontSizeClass(fontSize) });
      }
    });
  },

  getFontSizeClass: function (fontSize) {
    switch (fontSize) {
      case 0: return 'page-small';
      case 2: return 'page-large';
      default: return 'page-medium';
    }
  },

  initWebSocket: function () {
    var app = this;
    if (!isLoggedIn(app)) return;

    // 已有连接且未断开，不重复创建
    if (app.globalData.socketTask && app.globalData.wsConnected) {
      return;
    }

    // 关闭残留的旧连接
    if (app.globalData.socketTask) {
      try {
        app.globalData.socketTask.close({ fail: function() {} });
      } catch (e) { /* ignore */ }
      app.globalData.socketTask = null;
    }

    if (app.globalData.wsReconnectTimer) {
      clearTimeout(app.globalData.wsReconnectTimer);
      app.globalData.wsReconnectTimer = null;
    }

    app.globalData.socketTask = wx.connectSocket({
      url: getWsUrl(),
      fail: function (err) {
        console.error('WebSocket连接失败', err);
        app.scheduleWsReconnect();
      }
    });

    app.globalData.socketTask.onOpen(function () {
      console.log('WebSocket已连接');
      app.globalData.wsConnected = true;
      app.globalData.wsRetryCount = 0;
      // 注册用户ID，让后端知道这个连接属于谁
      var userId = app.globalData.userId;
      if (userId) {
        app.globalData.socketTask.send({
          data: JSON.stringify({ type: 'REGISTER', userId: userId })
        });
      }
    });

    app.globalData.socketTask.onMessage(function (res) {
      try {
        var message = JSON.parse(res.data);
        // 心跳响应：服务端发PING，客户端回PONG
        if (message.type === 'PING') {
          app.globalData.socketTask.send({ data: '{"type":"PONG"}' });
          return;
        }
        if (message.type === 'new_task') {
          wx.showToast({ title: '收到新任务', icon: 'none', duration: 2000 });
          app.notifyPage('pages/tasks/tasks', 'onWebSocketMessage', message);
        } else if (message.type === 'task_updated') {
          app.notifyPage('pages/tasks/tasks', 'onWebSocketMessage', message);
        } else if (message.type === 'notification') {
          app.notifyPage('pages/info/info', 'onWebSocketMessage', message);
        }
      } catch (error) {
        console.error('WebSocket消息解析失败', error);
      }
    });

    app.globalData.socketTask.onClose(function () {
      console.log('WebSocket已断开');
      app.globalData.wsConnected = false;
      app.globalData.socketTask = null;
      if (isLoggedIn(app)) app.scheduleWsReconnect();
    });

    app.globalData.socketTask.onError(function () {
      app.globalData.wsConnected = false;
      app.globalData.socketTask = null;
      if (isLoggedIn(app)) app.scheduleWsReconnect();
    });
  },

  scheduleWsReconnect: function () {
    var app = this;
    if (app.globalData.wsReconnectTimer) return;
    var retry = app.globalData.wsRetryCount || 0;
    if (retry >= 5) return;
    app.globalData.wsRetryCount = retry + 1;
    app.globalData.wsReconnectTimer = setTimeout(function () {
      app.globalData.wsReconnectTimer = null;
      if (isLoggedIn(app)) app.initWebSocket();
    }, 3000);
  },

  notifyPage: function (route, method, message) {
    getCurrentPages().forEach(function (page) {
      if (page.route === route && typeof page[method] === 'function') {
        page[method](message);
      }
    });
  },

  globalData: {
    userInfo: null,
    userId: null,
    token: '',
    language: 'zh-CN',
    fontSize: 1,
    socketTask: null,
    wsConnected: false,
    wsReconnectTimer: null,
    wsRetryCount: 0
  }
});
