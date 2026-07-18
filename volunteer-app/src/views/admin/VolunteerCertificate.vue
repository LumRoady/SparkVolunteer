/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!-- 志愿者证书 -->
<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>志愿者证书</span>
          <SearchInput v-model="searchUserId" placeholder="输入用户ID查询" width="220px" @search="fetchCertificate" />
        </div>
      </template>

      <el-empty v-if="!cert" description="请输入用户ID查询证书" />

      <div v-else class="cert-card">
        <div class="cert-header">
          <div class="cert-badge">🌟</div>
          <div class="cert-title">星火志愿服务证书</div>
          <div class="cert-subtitle">CERTIFICATE OF VOLUNTEER SERVICE</div>
        </div>

        <div class="cert-body">
          <div class="cert-row">
            <span class="cert-label">志愿者姓名</span>
            <span class="cert-value">{{ cert.volunteerName }}</span>
          </div>
          <div class="cert-row">
            <span class="cert-label">志愿者等级</span>
            <span class="cert-value">
              <el-tag :type="levelTag(cert.level)" size="small">{{ cert.level }}</el-tag>
            </span>
          </div>
          <div class="cert-row">
            <span class="cert-label">累计服务次数</span>
            <span class="cert-value">{{ cert.totalServices }} 次</span>
          </div>
          <div class="cert-row">
            <span class="cert-label">累计服务时长</span>
            <span class="cert-value">{{ cert.totalHours }} 小时</span>
          </div>
          <div class="cert-row">
            <span class="cert-label">平均评分</span>
            <span class="cert-value">
              <el-rate v-model="cert.avgRating" disabled show-score text-color="#ff9900" />
            </span>
          </div>
          <div class="cert-row">
            <span class="cert-label">加入日期</span>
            <span class="cert-value">{{ formatDate(cert.joinDate) }}</span>
          </div>
          <div class="cert-row">
            <span class="cert-label">手机号</span>
            <span class="cert-value">{{ cert.phone || '-' }}</span>
          </div>
          <div class="cert-footer">
            <span>证书编号：{{ cert.certNumber }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import http from '@/utils/request';
import SearchInput from '@/components/SearchInput.vue';
import { formatDate } from '@/utils/format';
import { ElMessage } from 'element-plus';

const searchUserId = ref('');
const cert = ref(null);

function levelTag(level) {
  const map = { '初级': 'info', '中级': 'success', '高级': 'warning', '资深': 'danger' };
  return map[level] || 'info';
}

async function fetchCertificate() {
  if (!searchUserId.value) {
    ElMessage.warning('请输入用户ID');
    return;
  }
  try {
    cert.value = await http.get(`/volunteer/certificate/${searchUserId.value}`);
  } catch {
    cert.value = null;
    ElMessage.error('获取证书失败');
  }
}
</script>

<style scoped>
.cert-card {
  max-width: 500px;
  margin: 0 auto;
  border: 2px solid #ff6b6b;
  border-radius: 16px;
  overflow: hidden;
}
.cert-header {
  background: linear-gradient(135deg, #ff6b6b, #e55a5a);
  padding: 24px;
  text-align: center;
  color: #fff;
}
.cert-badge {
  font-size: 48px;
  margin-bottom: 8px;
}
.cert-title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 4px;
}
.cert-subtitle {
  font-size: 11px;
  letter-spacing: 2px;
  opacity: 0.8;
  margin-top: 4px;
}
.cert-body {
  padding: 24px;
}
.cert-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
}
.cert-label {
  color: #909399;
  font-size: 14px;
}
.cert-value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}
.cert-footer {
  text-align: center;
  padding-top: 16px;
  font-size: 12px;
  color: #c0c4cc;
}
</style>
