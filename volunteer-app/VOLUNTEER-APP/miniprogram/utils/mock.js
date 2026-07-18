/**
 * Mock 数据服务
 * 当后端不可用时，拦截 API 请求返回模拟数据，支持前端独立开发
 *
 * 使用方式：
 * 1. 在 app.js 中引入: const mock = require('./utils/mock')
 * 2. 开启 Mock: mock.enable()  /  关闭 Mock: mock.disable()
 * 3. Mock 状态存储在 storage 中，重启后保持
 *
 * 覆盖接口：
 * - POST /api/auth/login         用户登录
 * - POST /api/auth/register       用户注册
 * - GET  /api/task/list           任务列表
 * - GET  /api/task/detail/:id     任务详情
 * - POST /api/task/accept/:id     接单
 * - POST /api/task/complete/:id   完成任务
 * - GET  /api/notifications       通知列表
 * - POST /api/checkins            签到
 * - GET  /api/checkins/today      今日签到状态
 * - GET  /api/ranking             排行榜
 * - GET  /api/users/:id/stats     用户统计
 */

const MOCK_KEY = '__mock_enabled__';

/**
 * 判断当前是否为开发环境（仅开发者工具可用 Mock）
 * 生产环境和体验版强制禁用 Mock，防止返回假数据
 */
function isDevEnv() {
  try {
    const accountInfo = wx.getAccountInfoSync();
    return accountInfo.miniProgram.envVersion === 'develop';
  } catch (e) {
    return false;
  }
}

// ==================== Mock 数据 ====================

const mockUsers = [
  { id: 1, username: 'admin', nickname: '管理员', name: '系统管理员', phone: '13800000001', avatar: '', role: 'ADMIN', community: '星火社区', points: 500, completedTasks: 50, checkinStreak: 7, createTime: '2026-01-01T00:00:00' },
  { id: 2, username: 'volunteer1', nickname: '热心志愿者', name: '张三', phone: '13800000002', avatar: '', role: 'VOLUNTEER', community: '星火社区', points: 320, completedTasks: 32, checkinStreak: 14, createTime: '2026-01-15T00:00:00' },
  { id: 3, username: 'elderly1', nickname: '李奶奶', name: '李秀英', phone: '13800000003', avatar: '', role: 'ELDERLY', community: '星火社区', points: 50, completedTasks: 5, checkinStreak: 3, createTime: '2026-02-01T00:00:00' }
];

const mockTasks = [
  { id: 1, taskNo: 1001, title: '紧急求助-身体不适', content: '老人感到头晕，需要帮助', type: 'sos', status: 0, userId: 3, requesterId: 3, community: '星火社区', location: '星火社区3号楼', createTime: '2026-06-28T09:00:00' },
  { id: 2, taskNo: 1002, title: '帮忙买菜', content: '需要帮忙去超市买一些蔬菜和水果', type: 'life_service', status: 1, userId: 3, requesterId: 3, receiverId: 2, community: '星火社区', location: '星火社区5号楼', createTime: '2026-06-28T08:30:00' },
  { id: 3, taskNo: 1003, title: '智能手机使用咨询', content: '想学习如何使用微信视频通话', type: 'consultation', status: 2, userId: 3, requesterId: 3, receiverId: 2, community: '星火社区', location: '星火社区3号楼', createTime: '2026-06-27T14:00:00' },
  { id: 4, taskNo: 1004, title: '帮忙取快递', content: '有个快递在小区门口，需要帮忙取一下', type: 'life_service', status: 0, userId: 3, requesterId: 3, community: '星火社区', location: '星火社区东门', createTime: '2026-06-28T10:00:00' },
  { id: 5, taskNo: 1005, title: '量血压', content: '需要帮忙量一下血压', type: 'consultation', status: 0, userId: 3, requesterId: 3, community: '星火社区', location: '星火社区3号楼', createTime: '2026-06-28T09:30:00' }
];

const mockNotifications = [
  { id: 1, userId: 2, title: '新任务通知', content: '有新的紧急求助任务等待接单', type: 'TASK', isRead: false, createTime: '2026-06-28T09:01:00' },
  { id: 2, userId: 2, title: '任务完成', content: '您完成的任务已获得好评', type: 'SYSTEM', isRead: true, createTime: '2026-06-27T15:00:00' },
  { id: 3, userId: 2, title: '积分奖励', content: '连续签到7天，获得额外积分奖励', type: 'POINTS', isRead: false, createTime: '2026-06-28T07:00:00' },
  { id: 4, userId: 3, title: '志愿者已接单', content: '您的求助已被志愿者接单', type: 'TASK', isRead: false, createTime: '2026-06-28T08:31:00' }
];

// ==================== 工具函数 ====================

function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms || 300));
}

function success(data) {
  return { code: 200, message: 'success', data: data, timestamp: Date.now() };
}

function error(code, msg) {
  return { code: code, message: msg, timestamp: Date.now() };
}

function paginate(list, page, size) {
  const start = page * size;
  const end = Math.min(start + size, list.length);
  return {
    content: list.slice(start, end),
    page: page,
    size: size,
    totalElements: list.length,
    totalPages: Math.ceil(list.length / size),
    first: page === 0,
    last: start + size >= list.length
  };
}

// ==================== Mock 处理器 ====================

const handlers = {
  // 用户登录
  'POST /api/auth/login': function(body) {
    const user = mockUsers.find(u => u.username === body.username);
    if (!user) return error(401, '用户名或密码错误');
    return success({
      token: 'mock_token_' + user.id,
      user: { id: user.id, username: user.username, nickname: user.nickname, role: user.role, avatar: user.avatar }
    });
  },

  // 用户注册
  'POST /api/auth/register': function(body) {
    const newUser = {
      id: mockUsers.length + 1,
      username: body.username,
      nickname: body.nickname || body.username,
      role: body.role || 'ELDERLY',
      avatar: '',
      createTime: new Date().toISOString()
    };
    return success(newUser);
  },

  // 任务列表
  'GET /api/task/list': function(params) {
    let tasks = [...mockTasks];
    if (params.status !== undefined) tasks = tasks.filter(t => t.status === parseInt(params.status));
    if (params.type) tasks = tasks.filter(t => t.type === params.type);
    tasks.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
    const page = parseInt(params.page) || 0;
    const size = parseInt(params.size) || 10;
    return success(paginate(tasks, page, size));
  },

  // 任务详情
  'GET /api/task/detail/': function(params, path) {
    const id = parseInt(path.split('/').pop());
    const task = mockTasks.find(t => t.id === id);
    if (!task) return error(404, '任务不存在');
    return success(task);
  },

  // 接单
  'POST /api/task/accept/': function(body, path) {
    const id = parseInt(path.split('/').pop());
    const task = mockTasks.find(t => t.id === id);
    if (!task) return error(404, '任务不存在');
    task.status = 1;
    task.receiverId = body.volunteerId || 2;
    return success(task);
  },

  // 完成任务
  'POST /api/task/complete/': function(body, path) {
    const id = parseInt(path.split('/').pop());
    const task = mockTasks.find(t => t.id === id);
    if (!task) return error(404, '任务不存在');
    task.status = 2;
    return success(task);
  },

  // 取消任务
  'POST /api/task/cancel/': function(body, path) {
    const id = parseInt(path.split('/').pop());
    const task = mockTasks.find(t => t.id === id);
    if (!task) return error(404, '任务不存在');
    task.status = 3;
    return success(task);
  },

  // 通知列表
  'GET /api/notifications': function(params) {
    let list = [...mockNotifications];
    if (params.userId) list = list.filter(n => n.userId === parseInt(params.userId));
    if (params.type) list = list.filter(n => n.type === params.type);
    list.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
    const page = parseInt(params.page) || 0;
    const size = parseInt(params.size) || 10;
    return success(paginate(list, page, size));
  },

  // 签到
  'POST /api/checkins': function(params) {
    return success({
      id: Date.now(),
      userId: parseInt(params.userId),
      checkinDate: new Date().toISOString().split('T')[0],
      checkinTime: new Date().toISOString(),
      status: 'SUCCESS'
    });
  },

  // 今日签到状态
  'GET /api/checkins/today': function(params) {
    return success(false);
  },

  // 排行榜
  'GET /api/ranking': function(params) {
    const ranking = mockUsers
      .filter(u => u.role !== 'ADMIN')
      .sort((a, b) => b.points - a.points)
      .map((u, i) => ({ rank: i + 1, userId: u.id, name: u.nickname || u.name, points: u.points, completedTasks: u.completedTasks }));
    return success(ranking);
  },

  // 用户统计
  'GET /api/users/': function(params, path) {
    const parts = path.split('/');
    const userId = parseInt(parts[parts.indexOf('users') + 1]);
    const user = mockUsers.find(u => u.id === userId);
    if (!user) return error(404, '用户不存在');
    if (path.endsWith('/stats')) {
      return success({
        points: user.points,
        completedTasks: user.completedTasks,
        checkinStreak: user.checkinStreak,
        level: user.points >= 1000 ? 4 : user.points >= 300 ? 3 : user.points >= 100 ? 2 : 1
      });
    }
    if (path.endsWith('/profile')) {
      return success(user);
    }
    return error(404, '接口不存在');
  },

  // 数据大屏
  'GET /api/dashboard/stats': function() {
    return success({
      todayHelpCount: 5,
      todayCompletedCount: 3,
      responseRate: 80.0,
      avgResponseMinutes: 3.5,
      onlineVolunteers: 12,
      totalElderly: 1,
      totalVolunteers: 1,
      communityCount: 1,
      emergencyPending: 1,
      recentTasks: mockTasks.slice(0, 3),
      weeklyTrend: [
        { date: '06-22', count: 3 }, { date: '06-23', count: 5 },
        { date: '06-24', count: 2 }, { date: '06-25', count: 7 },
        { date: '06-26', count: 4 }, { date: '06-27', count: 6 },
        { date: '06-28', count: 5 }
      ]
    });
  }
};

// ==================== 匹配逻辑 ====================

function findHandler(method, url) {
  // 精确匹配
  const exactKey = method + ' ' + url;
  if (handlers[exactKey]) return { handler: handlers[exactKey], path: url };

  // 前缀匹配（路径参数）
  for (const key of Object.keys(handlers)) {
    const [hMethod, hPath] = key.split(' ');
    if (hMethod === method && url.startsWith(hPath)) {
      return { handler: handlers[key], path: url };
    }
  }

  return null;
}

// ==================== 公共 API ====================

/**
 * 开启 Mock 模式
 */
function enable() {
  if (!isDevEnv()) {
    console.warn('[Mock] 非开发环境，禁止开启 Mock 模式');
    return;
  }
  wx.setStorageSync(MOCK_KEY, true);
  console.log('[Mock] Mock 模式已开启（仅开发环境有效）');
}

/**
 * 关闭 Mock 模式
 */
function disable() {
  wx.setStorageSync(MOCK_KEY, false);
  console.log('[Mock] Mock 模式已关闭');
}

/**
 * 判断 Mock 模式是否开启（生产环境和体验版强制返回 false）
 */
function isEnabled() {
  // 安全限制：生产环境/体验版禁止使用 Mock，忽略 Storage 中的开关值
  if (!isDevEnv()) return false;
  return wx.getStorageSync(MOCK_KEY) === true;
}

/**
 * 模拟请求（返回 Promise，可直接替换 wx.request）
 * @param {Object} options - { url, method, data, success, fail }
 * @returns {Promise|null} 匹配到时返回 Promise，否则返回 null
 */
function mockRequest(options) {
  if (!isEnabled()) return null;

  const method = (options.method || 'GET').toUpperCase();
  let url = options.url;

  // 去掉 base URL 前缀，提取路径
  const apiIndex = url.indexOf('/api');
  if (apiIndex > 0) {
    url = url.substring(apiIndex);
  }

  const result = findHandler(method, url);
  if (!result) {
    console.warn('[Mock] 未匹配到 Mock 处理器:', method, url);
    return null;
  }

  return delay(200 + Math.random() * 300).then(() => {
    const mockResult = result.handler(options.data || {}, result.path);
    return Promise.resolve({ statusCode: 200, data: mockResult });
  });
}

/**
 * 获取所有 Mock 接口列表
 */
function listApis() {
  return Object.keys(handlers);
}

module.exports = {
  enable,
  disable,
  isEnabled,
  mockRequest,
  listApis
};
