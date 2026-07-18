# VOLUNTEER-APP · 星火众擎

社区志愿互助平台：微信小程序 + Node 后端 + ESP32 硬件求助按钮。

## 目录结构

```
VOLUNTEER-APP/
├── miniprogram/          # 微信小程序（主应用）
│   ├── app.js / app.json / app.wxss
│   ├── pages/            # 页面
│   └── utils/            # 配置、请求、鉴权
├── backend/              # Node.js 后端
│   ├── pure-http-server.js   # 主服务（8084）
│   ├── package.json
│   └── legacy/           # 旧版/备用服务器
├── hardware/             # 硬件固件
│   ├── esp32-c3/         # ESP32 双板程序
│   └── button-test/      # 按钮测试
├── web/                  # Web 监控页（浏览器）
│   └── frontend.html
├── scripts/              # 启动脚本
│   └── start-all.bat
├── legacy-uniapp/        # 历史 UniApp/Vue 代码（可忽略）
└── project.config.json   # 微信开发者工具配置
```

## 快速开始

### 1. 重组目录（若文件还在上级目录）

在项目根目录 `volunteer-app` 下执行：

```powershell
powershell -ExecutionPolicy Bypass -File reorganize.ps1
```

### 2. 启动后端

```bash
cd backend
node pure-http-server.js
```

或双击 `scripts/start-all.bat`

### 3. 打开小程序

微信开发者工具 → 导入项目 → 选择 **VOLUNTEER-APP** 文件夹

勾选「不校验合法域名」

### 4. 测试账号

| 角色   | 手机号        | 密码   |
|--------|---------------|--------|
| 志愿者 | 13800138000   | 123456 |
| 求助者 | 13900139000   | 123456 |

### 5. Web 监控

浏览器打开 `web/frontend.html`

## 真机调试

修改 `miniprogram/utils/config.js` 中的 `DEV_HOST` 为电脑局域网 IP。
