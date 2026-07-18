const { API } = require('../../utils/config');
const { get } = require('../../utils/request');
const { getUserId, requireLogin } = require('../../utils/auth');

Page({
  data: {
    userPoints: 0,
    completedTasks: 0,
    streak: 0,
    level: 1,
    levelName: '普通志愿者',
    nextLevelName: '热心志愿者',
    pointsToNext: 100,
    levelProgress: 0,

    achievements: [
      { key: 'FIRST_TASK',     title: '首次接单',   icon: '🌟', desc: '完成第1次志愿服务',  unlocked: false },
      { key: 'TEN_TASKS',      title: '累计10单',   icon: '📋', desc: '累计完成10次服务',    unlocked: false },
      { key: 'SOS_RESPONSE',   title: '紧急响应',   icon: '🚨', desc: '响应过SOS紧急求助',   unlocked: false },
      { key: 'LOVE_AMBASSADOR',title: '爱心大使',   icon: '❤️', desc: '累计积分达到500分',  unlocked: false },
      { key: 'CHECKIN_30',     title: '签到达人',   icon: '🔥', desc: '连续签到30天',        unlocked: false },
      { key: 'COMMUNITY_STAR', title: '社区之星',   icon: '🏆', desc: '积分1000+且完成50单', unlocked: false }
    ]
  },

  onShow() {
    if (!requireLogin()) return;
    this.loadStats();
  },

  loadStats() {
    const userId = getUserId();
    if (!userId) return;

    get(API.userStats(userId), {}, { showLoading: false, showError: false })
      .then((data) => {
        const points = data.points || 0;
        const tasks = data.completedTasks || 0;
        const streak = data.checkinStreak || 0;
        const level = this.calcLevel(points);
        const levelInfo = this.getLevelInfo(level);

        // 解锁判定
        const achievements = this.data.achievements.map((a) => {
          let unlocked = false;
          switch (a.key) {
            case 'FIRST_TASK':     unlocked = tasks >= 1; break;
            case 'TEN_TASKS':      unlocked = tasks >= 10; break;
            case 'SOS_RESPONSE':   unlocked = tasks >= 1; break;
            case 'LOVE_AMBASSADOR':unlocked = points >= 500; break;
            case 'CHECKIN_30':     unlocked = streak >= 30; break;
            case 'COMMUNITY_STAR': unlocked = points >= 1000 && tasks >= 50; break;
          }
          return { ...a, unlocked };
        });

        this.setData({
          userPoints: points,
          completedTasks: tasks,
          streak,
          level,
          levelName: levelInfo.name,
          nextLevelName: levelInfo.nextName,
          pointsToNext: levelInfo.next - points,
          levelProgress: levelInfo.progress,
          achievements
        });
      })
      .catch(() => {});
  },

  calcLevel(points) {
    if (points >= 1000) return 4;
    if (points >= 300)  return 3;
    if (points >= 100)  return 2;
    return 1;
  },

  getLevelInfo(points) {
    const levels = [
      { min: 0,    max: 99,   name: '普通志愿者', nextName: '热心志愿者', next: 100  },
      { min: 100,  max: 299,  name: '热心志愿者', nextName: '优秀志愿者', next: 300  },
      { min: 300,  max: 999,  name: '优秀志愿者', nextName: '星火志愿者', next: 1000 },
      { min: 1000, max: 9999, name: '星火志愿者', nextName: '已达最高等级', next: 9999 }
    ];
    for (const lv of levels) {
      if (points >= lv.min && points <= lv.max) {
        const range = lv.next - lv.min;
        const progress = range > 0 ? Math.floor(((points - lv.min) / range) * 100) : 100;
        return { ...lv, progress: Math.min(progress, 100) };
      }
    }
    return { name: '普通志愿者', nextName: '热心志愿者', next: 100, progress: 0 };
  }
});
