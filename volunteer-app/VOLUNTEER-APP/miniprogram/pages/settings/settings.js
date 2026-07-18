const { requireLogin } = require('../../utils/auth');
const app = getApp();
const { clearSession } = require('../../utils/auth');
Page({
  data: {
    fontSizeIndex: 1,
    fontSizes: ['小', '中', '大']
  },

  onLoad() {
    if (!requireLogin()) return;
    const fontSize = app.globalData.fontSize;
    this.setData({
      fontSizeIndex: fontSize,
      fontSizeClass: app.getFontSizeClass(fontSize)
    });
  },

  toggleNotifications(e) {
    // 切换通知设置
    const enabled = e.detail.value
    console.log('通知设置:', enabled)
    wx.showToast({
      title: enabled ? '通知已开启' : '通知已关闭',
      icon: 'none'
    })
  },
  toggleHelpAlerts(e) {
    // 切换求助提醒设置
    const enabled = e.detail.value
    console.log('求助提醒设置:', enabled)
    wx.showToast({
      title: enabled ? '求助提醒已开启' : '求助提醒已关闭',
      icon: 'none'
    })
  },
  goToProfile() {
    // 跳转到个人信息页面
    console.log('点击了个人信息')
    wx.navigateTo({
      url: '/pages/profile/profile',
      success: function(res) {
        console.log('成功跳转到个人信息页面', res)
      },
      fail: function(res) {
        console.log('跳转到个人信息页面失败', res)
      }
    })
  },
  goToChangePassword() {
    // 跳转到修改密码页面
    console.log('点击了修改密码')
    wx.navigateTo({
      url: '/pages/change-password/change-password',
      success: function(res) {
        console.log('成功跳转到修改密码页面', res)
      },
      fail: function(res) {
        console.log('跳转到修改密码页面失败', res)
      }
    })
  },
  goToBindPhone() {
    // 跳转到绑定手机页面
    console.log('点击了绑定手机')
    wx.navigateTo({
      url: '/pages/bind-phone/bind-phone',
      success: function(res) {
        console.log('成功跳转到绑定手机页面', res)
      },
      fail: function(res) {
        console.log('跳转到绑定手机页面失败', res)
      }
    })
  },
  goToLanguage() {
    // 跳转到语言设置页面
    console.log('点击了语言设置')
    wx.navigateTo({
      url: '/pages/language/language',
      success: function(res) {
        console.log('成功跳转到语言设置页面', res)
      },
      fail: function(res) {
        console.log('跳转到语言设置页面失败', res)
      }
    })
  },
  changeFontSize(e) {
    // 切换字体大小
    const index = e.detail.value
    const fontSize = parseInt(index)
    console.log('字体大小索引:', fontSize)
    
    // 更新字体大小设置
    this.setData({
      fontSizeIndex: fontSize
    })
    
    // 更新全局字体大小设置
    app.globalData.fontSize = fontSize
    
    // 保存到本地存储
    wx.setStorage({
      key: 'fontSize',
      data: fontSize,
      success: () => {
        console.log('字体大小已保存')
        // 调用全局字体大小设置函数
        app.setPageFontSize(fontSize)
      },
      fail: (err) => {
        console.error('保存字体大小失败:', err)
      }
    })
    
    // 显示提示
    wx.showToast({
      title: '字体大小已设置',
      icon: 'success',
      duration: 1500
    })
  },
  clearCache() {
    // 清除缓存
    console.log('点击了清除缓存')
    wx.showModal({
      title: '清除缓存',
      content: '确定要清除缓存吗？',
      success: function(res) {
        if (res.confirm) {
          // 模拟清除缓存
          setTimeout(() => {
            wx.showToast({
              title: '缓存已清除',
              icon: 'success',
              duration: 2000
            })
          }, 1000)
        }
      }
    })
  },
  goToUserAgreement() {
    // 跳转到用户协议页面
    console.log('点击了用户协议')
    wx.navigateTo({
      url: '/pages/user-agreement/user-agreement',
      success: function(res) {
        console.log('成功跳转到用户协议页面', res)
      },
      fail: function(res) {
        console.log('跳转到用户协议页面失败', res)
      }
    })
  },
  goToPrivacyPolicy() {
    // 跳转到隐私政策页面
    console.log('点击了隐私政策')
    wx.navigateTo({
      url: '/pages/privacy-policy/privacy-policy',
      success: function(res) {
        console.log('成功跳转到隐私政策页面', res)
      },
      fail: function(res) {
        console.log('跳转到隐私政策页面失败', res)
      }
    })
  },
  goToAboutUs() {
    // 跳转到关于我们页面
    console.log('点击了关于我们')
    wx.navigateTo({
      url: '/pages/about/about',
      success: function(res) {
        console.log('成功跳转到关于我们页面', res)
      },
      fail: function(res) {
        console.log('跳转到关于我们页面失败', res)
      }
    })
  },
  logout() {
    // 退出登录
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          clearSession();
          wx.showToast({ title: '已退出登录', icon: 'success' });
          setTimeout(() => {
            wx.redirectTo({ url: '/pages/login/login' });
          }, 800);
        }
      }
    })
  }
})