/** @deprecated 此文件已废弃，请使用 volunteer-app/VOLUNTEER-APP/miniprogram/ 下的新版代码 */
/**
 * 全局配置文件
 * 统一管理 API 地址、WebSocket 地址等环境相关配置
 *
 * 切换环境：修改 currentEnv 的值即可
 * - 'dev'  : 本地开发环境（通过环境变量或以下配置指定地址）
 * - 'prod' : 生产环境
 *
 * 开发环境地址优先级：环境变量 > 默认配置
 */
const ENV = {
  dev: {
    // 本地开发时修改为你的服务器地址
    // 或在命令行设置环境变量：set BASE_URL=http://your-ip:8084/api
    baseUrl: 'http://localhost:8084/api',
    wsUrl: 'ws://localhost:8084/ws'
  },
  prod: {
    baseUrl: 'https://your-domain.com/api',
    wsUrl: 'wss://your-domain.com/ws'
  }
};

// 支持通过环境变量覆盖（小程序中可用自定义编译参数）
const currentEnv = 'dev';

module.exports = ENV[currentEnv];
