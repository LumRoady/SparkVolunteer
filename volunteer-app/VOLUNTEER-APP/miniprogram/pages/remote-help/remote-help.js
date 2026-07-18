const { requireLogin } = require('../../utils/auth');
// 远程帮助页面

Page({
  data: {
    logs: []
  },
  
  onLoad() {
    if (!requireLogin()) return;
    // 页面加载时执行
    console.log('远程帮助页面加载完成');
    this.addLog('页面加载完成');
  },
  
  goToTasks() {
    wx.navigateTo({ url: '/pages/tasks/tasks' });
  },
  
  addLog(message) {
    const log = {
      time: new Date().toLocaleTimeString(),
      message
    };
    let logs = this.data.logs;
    logs.unshift(log);
    if (logs.length > 20) {
      logs.pop();
    }
    this.setData({ logs });
    console.log('[日志]', message);
  }
});