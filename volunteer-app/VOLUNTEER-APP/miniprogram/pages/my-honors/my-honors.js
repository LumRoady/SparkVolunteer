const { requireLogin } = require('../../utils/auth');
Page({
  data: {
    zoomed: false,
    avatar: '/images/logo.png',
    badges: [
      { icon: '❤️', title: '爱心奉献'   },
      { icon: '✨', title: '服务之星'   },
      { icon: '🚀', title: '进步之星'   },
      { icon: '☀️', title: '坚持奉献'   },
      { icon: '😊', title: '优秀志愿者' },
      { icon: '😎', title: '爱心大使'   }
    ]
  },

  onAvatarTap() {
    this.setData({ zoomed: true });
    setTimeout(() => this.setData({ zoomed: false }), 400);
  }
});
