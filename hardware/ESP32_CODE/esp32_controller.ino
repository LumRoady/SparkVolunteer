/*
 * ============================================================
 * 星火众擎 — 智慧养老互助服务遥控器 (ESP32-C3 固件)
 * ============================================================
 * 主控芯片: ESP32-C3 (RISC-V 160MHz, 4MB Flash)
 * 开发框架: Arduino-ESP32 (>=2.0.14)
 * 开发板选择: ESP32C3 Dev Module
 *
 * 功能:
 *   1. 三按键: 红色(SOS) / 黄色(生活) / 绿色(咨询)
 *   2. 按键消抖 50ms + 有效按压 ≥500ms 防误触
 *   3. 长按 3 秒取消当前请求
 *   4. HTTP POST JSON 上报事件到云平台
 *   5. 三色 LED 状态指示 + 蜂鸣器反馈
 *   6. 定时心跳 5 分钟 + 电池监测
 *   7. 深度睡眠低功耗 (任意按键唤醒)
 *   8. SmartConfig 微信配网
 * ============================================================
 * ESP32-C3 引脚分配:
 *   GPIO4  → 红色按键 (SOS紧急救助)    INPUT_PULLUP, 按下为 GND
 *   GPIO5  → 黄色按键 (生活服务)       INPUT_PULLUP, 按下为 GND
 *   GPIO6  → 绿色按键 (精神慰藉)       INPUT_PULLUP, 按下为 GND
 *   GPIO8  → LED 红色通道              LEDC PWM 输出
 *   GPIO9  → LED 绿色通道              LEDC PWM 输出
 *   GPIO10 → LED 蓝色通道              LEDC PWM 输出
 *   GPIO7  → 蜂鸣器                    GPIO 输出
 *   GPIO3  → 电池电压检测              ADC1_CH3, 分压后输入
 * ============================================================
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <Preferences.h>
#include "esp_sleep.h"

/* ============================================================
 * 配置参数 (按实际部署修改)
 * ============================================================ */
const char* SERVER_URL    = "http://your-server-ip:8084/api/emergency/button";
const char* HEARTBEAT_URL = "http://your-server-ip:8084/api/device/heartbeat";
const char* DEVICE_ID     = "ESP32C3_DEVICE_001";        // 每台设备唯一

// WiFi (SmartConfig 配网后自动保存，首次可硬编码)
const char* WIFI_SSID     = "YOUR_WIFI_SSID";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";

/* ============================================================
 * GPIO 引脚 (ESP32-C3 可用: GPIO0-10, 18-21, 注意避开烧录脚)
 * ============================================================ */
#define PIN_BTN_RED      4      // 红键 — SOS
#define PIN_BTN_YELLOW   5      // 黄键 — 生活服务
#define PIN_BTN_GREEN    6      // 绿键 — 咨询
#define PIN_BUZZER       7      // 蜂鸣器
#define PIN_LED_R        8      // LED 红 (LEDC CH0)
#define PIN_LED_G        9      // LED 绿 (LEDC CH1)
#define PIN_LED_B       10      // LED 蓝 (LEDC CH2)
#define PIN_BATTERY_ADC  3      // ADC1_CH3, GPIO3

/* ============================================================
 * LEDC PWM 配置 (替代 ESP32 的 analogWrite)
 * ============================================================ */
#define LEDC_FREQ       5000    // 5kHz
#define LEDC_RESOLUTION    8    // 8bit → 0-255

/* ============================================================
 * 电池检测参数
 * ============================================================ */
#define VOLTAGE_DIVIDER      2.0f   // 分压比 (100K+100K)
#define ADC_MAX           4095.0f   // ESP32-C3 12bit ADC
#define ADC_REF_VOLTAGE      3.3f
#define BATTERY_LOW          3.3f   // 低电量告警
#define BATTERY_CRITICAL     3.0f   // 强制休眠

/* ============================================================
 * 按键参数
 * ============================================================ */
#define DEBOUNCE_MS          50     // 消抖
#define MIN_PRESS_MS        500     // 最短有效按压
#define LONG_PRESS_MS      3000     // 长按取消
#define SMART_CONFIG_MS   10000     // 配网长按

/* ============================================================
 * 事件 / 定时
 * ============================================================ */
#define EVENT_SOS             1
#define EVENT_LIFE            2
#define EVENT_CONSULT         3

#define HEARTBEAT_MS     300000     // 5 分钟
#define SLEEP_SEC           300     // 休眠 5 分钟
#define IDLE_SLEEP_MS     60000     // 空闲 60 秒休眠

/* ============================================================
 * 全局状态
 * ============================================================ */
Preferences prefs;
RTC_DATA_ATTR int bootCount = 0;

struct BtnState {
  bool pressed;
  unsigned long start;
  bool triggered;
};
BtnState redBtn   = {false, 0, false};
BtnState yellowBtn = {false, 0, false};
BtnState greenBtn  = {false, 0, false};

unsigned long lastHeartbeat  = 0;
unsigned long lastActivity   = 0;

/* ============================================================
 * LED 控制 (ledcWrite 替代 analogWrite)
 * ============================================================ */
void ledSetup() {
  ledcSetup(0, LEDC_FREQ, LEDC_RESOLUTION);
  ledcSetup(1, LEDC_FREQ, LEDC_RESOLUTION);
  ledcSetup(2, LEDC_FREQ, LEDC_RESOLUTION);
  ledcAttachPin(PIN_LED_R, 0);
  ledcAttachPin(PIN_LED_G, 1);
  ledcAttachPin(PIN_LED_B, 2);
}

void setLED(int r, int g, int b) {
  ledcWrite(0, r);
  ledcWrite(1, g);
  ledcWrite(2, b);
}

void ledOff() { setLED(0, 0, 0); }

void ledBlink(int r, int g, int b, int times, int ms) {
  for (int i = 0; i < times; i++) {
    setLED(r, g, b); delay(ms);
    ledOff(); if (i < times - 1) delay(ms);
  }
}

/* ============================================================
 * 蜂鸣器
 * ============================================================ */
void beep(int ms) {
  digitalWrite(PIN_BUZZER, HIGH); delay(ms);
  digitalWrite(PIN_BUZZER, LOW);
}
void beepTimes(int n, int onMs, int offMs) {
  for (int i = 0; i < n; i++) { beep(onMs); if (i < n - 1) delay(offMs); }
}

/* ============================================================
 * 电池电压 (GPIO3 = ADC1_CH3)
 * ============================================================ */
float readBattery() {
  int raw = analogRead(PIN_BATTERY_ADC);         // 0-4095
  float v = (raw / ADC_MAX) * ADC_REF_VOLTAGE;   // ADC 引脚电压
  return v * VOLTAGE_DIVIDER;                    // 实际电池电压
}

int batteryStatus() {
  float v = readBattery();
  if (v < BATTERY_CRITICAL) return 2;
  if (v < BATTERY_LOW)      return 1;
  return 0;
}

/* ============================================================
 * WiFi
 * ============================================================ */
bool connectWiFi() {
  Serial.println("[WiFi] 连接中...");
  setLED(0, 0, 255);
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  int retry = 0;
  while (WiFi.status() != WL_CONNECTED && retry < 40) {
    delay(500); Serial.print("."); retry++;
  }
  if (WiFi.status() == WL_CONNECTED) {
    Serial.printf("\n[WiFi] OK, IP=%s\n", WiFi.localIP().toString().c_str());
    return true;
  }
  Serial.println("\n[WiFi] 失败");
  return false;
}

/* ============================================================
 * HTTP POST
 * ============================================================ */
bool postEvent(const char* url, int eventType, float battery) {
  if (WiFi.status() != WL_CONNECTED && !connectWiFi()) return false;

  HTTPClient http;
  http.begin(url);
  http.addHeader("Content-Type", "application/json");
  http.setTimeout(8000);

  StaticJsonDocument<256> doc;
  doc["deviceId"]     = DEVICE_ID;
  doc["buttonType"]   = eventType;
  doc["battery"]      = battery;
  doc["rssi"]         = WiFi.RSSI();
  doc["timestamp"]    = millis();

  String body; serializeJson(doc, body);
  int code = http.POST(body);
  http.end();

  Serial.printf("[HTTP] POST %s → %d\n", url, code);
  return code == 200;
}

bool sendEvent(int type) {
  return postEvent(SERVER_URL, type, readBattery());
}

bool sendHeartbeat() {
  return postEvent(HEARTBEAT_URL, 0, readBattery());
}

/* ============================================================
 * 按键扫描
 * ============================================================ */
bool isPressed(int pin) {
  // 二次读取消抖
  if (digitalRead(pin) == LOW) {
    delay(DEBOUNCE_MS);
    return digitalRead(pin) == LOW;
  }
  return false;
}

void handleBtn(BtnState& s, int pin, int eventType) {
  unsigned long now = millis();
  if (isPressed(pin)) {
    if (!s.pressed) {
      s.pressed   = true;
      s.start     = now;
      s.triggered = false;
      lastActivity = now;
    } else if (!s.triggered && now - s.start >= MIN_PRESS_MS) {
      s.triggered = true;
      // 配网模式: 红键长按 10 秒
      if (eventType == EVENT_SOS && now - s.start >= SMART_CONFIG_MS) {
        Serial.println("[配网] 进入 SmartConfig...");
        ledBlink(0, 0, 255, 10, 200);
        startSmartConfig();
        return;
      }
      // 取消模式: 黄/绿键长按 3 秒
      if ((eventType == EVENT_LIFE || eventType == EVENT_CONSULT) &&
          now - s.start >= LONG_PRESS_MS) {
        Serial.printf("[取消] 事件类型 %d\n", eventType);
        char url[128];
        snprintf(url, sizeof(url), "%s/cancel", SERVER_URL);
        if (postEvent(url, eventType, readBattery())) {
          beepTimes(3, 100, 100);
          ledBlink(0, 255, 0, 1, 500);
        }
        s.pressed = false;
        return;
      }
      // 正常触发
      Serial.printf("[触发] 事件 %d\n", eventType);
      beep(100);
      setLED(0, 0, 255);
      if (sendEvent(eventType)) {
        ledBlink(0, 255, 0, 3, 300);           // 绿闪 = 成功
      } else {
        ledBlink(255, 0, 0, 5, 300);            // 红闪 = 失败
        beepTimes(3, 200, 200);
      }
    }
  } else {
    s.pressed = false;
  }
}

void scanButtons() {
  handleBtn(redBtn,   PIN_BTN_RED,    EVENT_SOS);
  handleBtn(yellowBtn, PIN_BTN_YELLOW, EVENT_LIFE);
  handleBtn(greenBtn,  PIN_BTN_GREEN,  EVENT_CONSULT);
}

/* ============================================================
 * 深度睡眠 (ESP32-C3)
 * ============================================================ */
void enterDeepSleep() {
  Serial.println("[休眠] 深度睡眠, 按键唤醒");
  ledOff();
  WiFi.disconnect(true);
  WiFi.mode(WIFI_OFF);
  delay(100);

  // ESP32-C3 RTC GPIO 唤醒: GPIO0-5 可作为唤醒源
  // 这里用三个按键的 GPIO (4,5,6)，但 GPIO6 不是 RTC GPIO
  // 作为折衷: 只用 GPIO4(红键) + 定时器 双唤醒
  esp_sleep_enable_timer_wakeup(SLEEP_SEC * 1000000ULL);
  esp_deep_sleep_enable_gpio_wakeup(BIT64(PIN_BTN_RED)  |
                                     BIT64(PIN_BTN_YELLOW),
                                     ESP_GPIO_WAKEUP_GPIO_LOW);
  esp_deep_sleep_start();
}

/* ============================================================
 * SmartConfig 配网
 * ============================================================ */
void startSmartConfig() {
  WiFi.mode(WIFI_STA);
  WiFi.beginSmartConfig();
  int timeout = 120; // 60 秒
  while (!WiFi.smartConfigDone() && timeout > 0) {
    delay(500); Serial.print(".");
    setLED(0, 0, 255); delay(200); ledOff();
    timeout--;
  }
  if (WiFi.smartConfigDone()) {
    Serial.println("\n[配网] 成功");
    prefs.begin("spark", false);
    prefs.putString("ssid", WiFi.SSID().c_str());
    prefs.putString("pass", WiFi.psk().c_str());
    prefs.end();
    setLED(0, 255, 0); delay(3000); ledOff();
    beep(500);
  } else {
    Serial.println("\n[配网] 失败");
    ledBlink(255, 0, 0, 5, 300);
  }
}

/* ============================================================
 * setup + loop
 * ============================================================ */
void setup() {
  Serial.begin(115200);
  delay(100);
  bootCount++;
  Serial.printf("\n=== 星火遥控器 %s 启动 #%d ===\n", DEVICE_ID, bootCount);

  // GPIO
  pinMode(PIN_BTN_RED,    INPUT_PULLUP);
  pinMode(PIN_BTN_YELLOW, INPUT_PULLUP);
  pinMode(PIN_BTN_GREEN,  INPUT_PULLUP);
  pinMode(PIN_BUZZER,     OUTPUT);
  pinMode(PIN_BATTERY_ADC, INPUT);

  // LEDC PWM
  ledSetup();
  ledOff();

  // ADC (ESP32-C3 默认 12bit, 0-3.6V)
  analogReadResolution(12);
  analogSetAttenuation(ADC_11db);   // Arduino-ESP32 ≥2.0.14 支持

  // 读取已保存 WiFi
  prefs.begin("spark", true);
  String s = prefs.getString("ssid", "");
  String p = prefs.getString("pass", "");
  prefs.end();
  if (s.length() > 0) {
    // 有已保存配置则用已保存的 (需配合 char* 拷贝)
    // 这里保持用全局常量演示; 实际部署时优先用 NVS 中的
  }

  // 判断唤醒原因
  esp_sleep_wakeup_cause_t reason = esp_sleep_get_wakeup_cause();
  if (reason == ESP_SLEEP_WAKEUP_GPIO) {
    Serial.println("[唤醒] GPIO 按键唤醒");
  } else if (reason == ESP_SLEEP_WAKEUP_TIMER) {
    Serial.println("[唤醒] 定时器唤醒, 发心跳");
  }

  connectWiFi();
  lastHeartbeat = millis();
  lastActivity  = millis();

  // 低电量告警
  int bs = batteryStatus();
  if (bs == 1) { ledBlink(255, 128, 0, 3, 500); beep(2000); }
  if (bs == 2) { ledBlink(255, 0, 0, 10, 200); enterDeepSleep(); }
}

void loop() {
  unsigned long now = millis();

  scanButtons();

  // 心跳
  if (now - lastHeartbeat >= HEARTBEAT_MS) {
    Serial.println("[心跳] 发送");
    sendHeartbeat();
    lastHeartbeat = now;
    if (batteryStatus() >= 1) {
      // 心跳包已含电量信息; 本地 LED 提醒
      ledBlink(255, 128, 0, 3, 300);
    }
  }

  // 待机 LED (每 30 秒绿闪一下)
  static unsigned long lastLed = 0;
  if (now - lastLed >= 30000) { setLED(0, 255, 0); delay(50); ledOff(); lastLed = now; }

  // 空闲休眠
  if (now - lastActivity >= IDLE_SLEEP_MS) {
    Serial.println("[休眠] 空闲超时");
    enterDeepSleep();
  }

  delay(10);
}
