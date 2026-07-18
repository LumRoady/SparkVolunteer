/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

/**
 * WebSocket 客户端 — 心跳保活 + 自动重连
 *
 * 用法：
 *   import { createWsClient } from '@/utils/websocket';
 *   const ws = createWsClient({
 *     onMessage: (data) => { ... },
 *     onOpen: () => { ... },
 *   });
 *   ws.connect();
 */
const HEARTBEAT_INTERVAL = 30000;
const RECONNECT_DELAY = 3000;
const MAX_RETRIES = 5;

export function createWsClient({ onMessage, onOpen, onClose } = {}) {
  let ws = null;
  let heartbeatTimer = null;
  let retries = 0;
  let destroyed = false;

  function getUrl() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${protocol}//${window.location.host}/ws`;
  }

  function startHeartbeat() {
    stopHeartbeat();
    heartbeatTimer = setInterval(() => {
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send('{"type":"PONG"}');
      }
    }, HEARTBEAT_INTERVAL);
  }

  function stopHeartbeat() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer);
      heartbeatTimer = null;
    }
  }

  function connect() {
    if (destroyed) return;
    try {
      ws = new WebSocket(getUrl());

      ws.onopen = () => {
        retries = 0;
        startHeartbeat();
        if (onOpen) onOpen();
      };

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          if (data.type === 'PING') {
            ws.send('{"type":"PONG"}');
            return;
          }
          if (onMessage) onMessage(data);
        } catch (e) {
          // 非 JSON 消息忽略
        }
      };

      ws.onclose = () => {
        stopHeartbeat();
        if (onClose) onClose();
        scheduleReconnect();
      };

      ws.onerror = () => {
        // onclose 会接着触发，不需要额外处理
      };
    } catch (e) {
      scheduleReconnect();
    }
  }

  function scheduleReconnect() {
    if (destroyed) return;
    if (retries >= MAX_RETRIES) {
      console.warn(`[WS] 已达最大重连次数(${MAX_RETRIES})，停止重连`);
      if (onClose) onClose('max_retries');
      return;
    }
    retries++;
    console.warn(`[WS] 连接断开，${RECONNECT_DELAY / 1000}s 后第 ${retries}/${MAX_RETRIES} 次重连`);
    setTimeout(connect, RECONNECT_DELAY);
  }

  function disconnect() {
    destroyed = true;
    stopHeartbeat();
    if (ws) {
      ws.close();
      ws = null;
    }
  }

  function send(data) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(typeof data === 'string' ? data : JSON.stringify(data));
    }
  }

  return { connect, disconnect, send };
}
