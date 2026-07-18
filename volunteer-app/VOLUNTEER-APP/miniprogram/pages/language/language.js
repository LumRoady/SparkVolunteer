const app = getApp()

Page({
  data: {
    selectedLanguage: 'zh-CN'
  },
  onLoad() {
    console.log('语言设置页面加载完成')
    // 从全局数据加载语言设置
    const savedLanguage = app.globalData.language || 'zh-CN'
    this.setData({
      selectedLanguage: savedLanguage
    })
  },
  selectLanguage(e) {
    // 选择语言
    const lang = e.currentTarget.dataset.lang
    this.setData({
      selectedLanguage: lang
    })
    
    // 更新全局语言设置
    app.globalData.language = lang
    
    // 保存到本地存储
    wx.setStorage({
      key: 'userLanguage',
      data: lang,
      success: () => {
        console.log('语言设置已保存')
      },
      fail: (err) => {
        console.error('保存语言设置失败:', err)
      }
    })
    
    // 显示切换成功提示
    wx.showToast({
      title: '语言已切换',
      icon: 'success'
    })
    
    // 延迟返回上一页
    setTimeout(() => {
      wx.navigateBack()
    }, 1500)
  }
})