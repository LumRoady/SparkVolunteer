/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

/**
 * @fileoverview Vue 应用入口
 *
 * 全局注册：
 * - Vue Router（Hash 模式）
 * - Element Plus（中文 locale）
 * - Pinia（状态管理）
 */

import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import 'element-plus/dist/index.css';

import App from './App.vue';
import router from './router';

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(ElementPlus, { locale: zhCn });

app.mount('#app');
