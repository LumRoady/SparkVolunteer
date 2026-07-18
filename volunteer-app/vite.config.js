/**
 * @fileoverview Vite 构建配置
 *
 * - @ 别名指向 src/
 * - 开发时 /api 代理到后端 8084 端口
 * - 生产构建分包策略 + 压缩优化
 */

import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';

export default defineConfig({
  plugins: [vue()],

  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },

  server: {
    port: 5173,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://localhost:8084',
        ws: true,
        changeOrigin: true,
      },
    },
  },

  build: {
    // 输出目录
    outDir: 'dist',
    assetsDir: 'assets',
    // 资源内联阈值（小于此值的资源转为 base64）
    assetsInlineLimit: 4096,
    // 生成 sourcemap（生产环境关闭）
    sourcemap: false,
    // chunk 大小警告阈值
    chunkSizeWarningLimit: 500,
    // Terser 压缩配置
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,       // 移除 console.log
        drop_debugger: true,      // 移除 debugger
        pure_funcs: ['console.log'],
      },
    },
    rollupOptions: {
      output: {
        // 手动分包策略 — 将大型第三方库拆分为独立 chunk
        manualChunks: {
          // Vue 生态核心（vue + vue-router）
          'vendor-vue': ['vue', 'vue-router'],
          // Element Plus UI 库
          'vendor-element': ['element-plus', '@element-plus/icons-vue'],
          // ECharts 图表库（体量大，单独拆分）
          'vendor-echarts': ['echarts'],
          // 工具库（axios）
          'vendor-utils': ['axios'],
        },
        // 输出文件命名（带哈希，支持版本缓存）
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
      },
    },
  },
});
