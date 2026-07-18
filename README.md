# 星火众擎 — 智慧养老志愿服务平台

> **星星之火，可以燎原。众擎易举，大爱无疆。**

星火众擎是一套基于物联网、云计算与移动互联网的空巢老人互助服务系统。通过ESP32物理遥控器一键触发求助，云端智能匹配附近志愿者，结合微信小程序完成接单、服务与评价闭环，构建"15分钟养老服务圈"。

---

## 系统架构

```
+-------------------+       +--------------------+       +-------------------+
|                   |       |                    |       |                   |
|  ESP32 物联网遥控器 | ----> |   星火众擎云平台     | <---- |  微信小程序端      |
|  (红/黄/绿 三按键)  |  WiFi |   Spring Boot 2.7  |  API |  (老人端/志愿者端)  |
|                   |       |   MySQL + Redis    |       |                   |
+-------------------+       +--------------------+       +-------------------+
                                      |
                                      | WebSocket / HTTP
                                      v
                             +--------------------+
                             |                   |
                             |  Vue 3 管理后台    |
                             |  Element Plus     |
                             |                   |
                             +--------------------+
```

### 核心业务流程

```
老人按下遥控器按钮
      |
      v
ESP32 通过WiFi上报事件到云平台
      |
      v
云平台识别按钮类型 & 查询老人档案
      |
      +---> 红色(SOS): 紧急救助
      +---> 黄色: 生活服务(送餐/代购/打扫)
      +---> 绿色: 精神慰藉(陪聊/咨询)
      |
      v
云平台匹配算法: 就近匹配空闲志愿者
      |
      v
微信模板消息推送通知:
  - 志愿者收到任务推送
  - 老人家属收到通知
      |
      v
志愿者接单 -> 上门服务 -> 完成确认
      |
      v
老人/家属评价 -> 志愿者积分累积
      |
      v
积分排行榜 & 勋章成就系统
```

---

## 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 后端框架 | Spring Boot 2.7 + Java 8 | RESTful API, WebSocket |
| ORM | Spring Data JPA + Hibernate | 数据持久层 |
| 数据库 | MySQL 8.0 + Redis 7 | 业务数据 + 缓存/消息队列 |
| 管理后台 | Vue 3 + Vite + Element Plus | SPA 后台管理系统 |
| 小程序 | 微信原生小程序 | WXML + WXSS + JavaScript |
| 硬件 | ESP32-C3 + Arduino | Wi-Fi 物联网遥控器 |
| 消息推送 | 微信模板消息 + 短信 | 多渠道通知 |
| 部署 | Docker + Nginx | 容器化部署 |

---

## 目录结构

```
星火众擎/
├── README.md                      # 项目说明
├── server/                        # Spring Boot 后端
│   ├── src/main/java/com/xhzl/
│   │   ├── controller/            # 控制器层
│   │   ├── service/               # 业务逻辑层
│   │   ├── mapper/                # 数据访问层
│   │   ├── entity/                # 实体类
│   │   ├── dto/                   # 数据传输对象
│   │   ├── config/                # 配置类
│   │   ├── common/                # 公共工具类
│   │   └── XhzlApplication.java  # 启动类
│   ├── src/main/resources/
│   │   ├── application.yml        # 主配置
│   │   └── repository/            # Spring Data JPA
│   └── pom.xml
├── admin/                         # Vue3 管理后台
│   ├── src/
│   │   ├── views/                 # 页面组件
│   │   ├── components/            # 通用组件
│   │   ├── router/                # 路由配置
│   │   ├── stores/                # Pinia 状态管理
│   │   ├── api/                   # API 封装
│   │   └── utils/                 # 工具函数
│   └── package.json
├── miniapp/                       # 微信小程序
│   ├── pages/                     # 页面
│   ├── components/                # 组件
│   ├── utils/                     # 工具
│   └── app.json
├── hardware/                      # 硬件相关
│   ├── ESP32_CODE/                # ESP32 固件代码
│   │   └── esp32_controller.ino
│   └── 说明书.md                   # 硬件说明书
├── docs/                          # 项目文档
│   ├── 软件说明书.md
│   ├── 发明专利交底书.md
│   └── 实用新型专利说明书.md
├── demo/                          # 演示材料
│   ├── 演示脚本.md
│   ├── 演示数据生成器.sql
│   └── 答辩PPT大纲.md
└── docker-compose.yml             # 容器编排
```

---

## 功能特性

### 老人端（微信小程序）
- **一键求助**: 查看已绑定的物联网遥控器状态，手动发起服务请求
- **服务记录**: 查看历史服务记录与评价
- **家属绑定**: 绑定家属微信，服务通知实时推送
- **紧急联系人**: 设置紧急联系人，SOS 触发时同步通知

### 志愿者端（微信小程序）
- **任务大厅**: 查看待接取的服务任务列表
- **智能推荐**: 根据距离、技能标签推荐最合适的任务
- **接单/完成**: 一键接单，服务完成后上传确认
- **积分系统**: 服务积分、等级体系、勋章成就
- **排行榜**: 积分排行、服务时长排行

### 管理后台（Vue3 Web）
- **数据大屏**: 实时数据可视化（在线设备、进行中任务、志愿者分布热力图）
- **用户管理**: 老人、志愿者、家属信息管理
- **设备管理**: ESP32 设备注册、状态监控、OTA固件升级
- **任务调度**: 任务分配、调度监控、异常处理
- **积分管理**: 积分规则配置、兑换管理
- **数据统计**: 服务量统计、满意度分析、趋势报表

### 物联网遥控器（ESP32）
- **三色按键**: 红(SOS救助) / 黄(生活服务) / 绿(精神慰藉)
- **Wi-Fi 配网**: SmartConfig 一键配网
- **状态反馈**: LED 指示灯 + 蜂鸣器提示
- **心跳保活**: 定时上报设备在线状态
- **低功耗**: 深度睡眠模式，电池续航可达6个月
- **低电量告警**: 电池电压监测，低电量自动推送通知

---

## 快速开始

### 环境要求
- JDK 8+
- Maven 3.6+
- Node.js 18+（前端构建需要）
- MySQL 8.0+
- Redis 7+
- Arduino IDE 2.0+（ESP32 编译）

### 后端启动

```bash
cd server
# 修改 application.yml 中数据库连接信息
mvn clean package -DskipTests
java -jar target/xhzl-server-1.0.0.jar
```

### 管理后台启动

```bash
cd admin
npm install
npm run dev
# 访问 http://localhost:5173
```

### 微信小程序

1. 安装[微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 导入 `miniapp/` 目录
3. 修改 `utils/config.js` 中的 API 地址
4. 编译预览

### ESP32 固件烧录

1. 安装 Arduino IDE，添加 ESP32 开发板支持
2. 安装依赖库：`WiFi`, `HTTPClient`, `ArduinoJson`, `Preferences`
3. 打开 `hardware/ESP32_CODE/esp32_controller.ino`
4. 修改 WiFi 和服务器配置
5. 编译并烧录到 ESP32-C3 开发板

---

## 截图展示

> 以下为功能截图占位区域，请在项目完成后补充。

| 功能 | 截图位置 |
|------|---------|
| 管理后台数据大屏 | [运行后截图补充] |
| 任务调度中心 | [运行后截图补充] |
| 志愿者排行榜 | [运行后截图补充] |
| 小程序老人端首页 | [运行后截图补充] |
| 小程序志愿者任务大厅 | [运行后截图补充] |
| ESP32 实物照片 | [运行后拍照补充] |

---

## 核心创新点

1. **物联网+养老**: 将物理按钮与云平台结合，降低老人使用智能手机的门槛
2. **智能匹配算法**: 基于地理位置、技能标签、服务历史的多维匹配
3. **家属联动**: 服务过程家属实时可见，增强信任感
4. **积分激励闭环**: 服务→积分→排行榜→勋章，形成持续激励
5. **低功耗硬件**: ESP32深度睡眠+定时唤醒，超长续航

---

## 开源协议

本项目采用 Apache License 2.0 开源协议。

---

## 项目结构

```
星火/
├── spark-volunteer/              # Java Spring Boot 后端
│   ├── src/main/java/.../        # 业务代码
│   ├── src/main/resources/db/    # Flyway 数据库迁移脚本 (V1-V8)
│   ├── Dockerfile                # 后端容器化
│   └── nginx.conf                # 生产环境 Nginx 配置
├── volunteer-app/                # 前端项目根目录
│   ├── src/                      # Vue 3 管理后台
│   │   ├── views/                # 页面组件
│   │   ├── components/           # 共享组件
│   │   ├── stores/               # Pinia 状态管理
│   │   └── utils/                # 工具函数
│   └── VOLUNTEER-APP/            # 微信小程序
│       └── miniprogram/          # 小程序源码（唯一有效版本）
├── hardware/                     # ESP32 硬件
│   └── ESP32_CODE/               # Arduino 固件
├── docs/                         # 项目文档（软著/专利）
├── demo/                         # 演示材料
└── docker-compose.yml            # 一键部署
```

> ⚠️ **重要**: `spark-volunteer/` 根目录下的 `app.js`、`app.json`、`app.wxss` 以及 `utils/`、`pages/` 子目录中的文件均为旧版小程序代码，已标记 `@deprecated`。唯一有效的小程序代码在 `volunteer-app/VOLUNTEER-APP/miniprogram/` 下。

### API 版本说明

| 版本 | 路径前缀 | 状态 |
|------|---------|------|
| V2 | `/api/v2/` | **当前主力** |
| V1 | `/api/` | 计划废弃（下一版本移除），已有 V2 替代的接口请勿新增调用 |

---

## 联系我们

- 项目地址: 请联系项目负责人获取
- 联系邮箱: 请联系项目负责人获取
- 技术支持: 提交 Issue 或联系开发团队

---

<p align="center">
  <b>星火众擎 — 让每一位老人都能被温柔以待</b>
</p>
