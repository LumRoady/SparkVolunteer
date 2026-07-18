/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!-- 亲属管理 -->
<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>亲属关系管理</span>
          <el-button type="primary" size="small" @click="showBindDialog">绑定亲属</el-button>
        </div>
      </template>

      <!-- 查询老人亲属 -->
      <div style="margin-bottom:16px">
        <SearchInput v-model="searchElderlyId" placeholder="输入老人ID查询其亲属" width="280px" @search="fetchMembers" />
      </div>

      <el-table :data="members" v-loading="loading" stripe>
        <el-table-column prop="id" label="亲属ID" width="80" align="center" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="relation" label="关系" width="100" align="center" />
        <el-table-column label="微信绑定" width="100" align="center">
          <template #default="{row}">
            <el-tag :type="row.wechatOpenid?'success':'info'">{{ row.wechatOpenid ? '已绑定' : '未绑定' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{row}">
            <el-popconfirm title="确认解绑？" @confirm="unbindFamily(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">解绑</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 绑定弹窗 -->
    <el-dialog v-model="bindVisible" title="绑定亲属关系" width="420px">
      <el-form :model="bindForm" label-width="100px">
        <el-form-item label="亲属用户ID">
          <el-input v-model="bindForm.familyUserId" placeholder="子女/亲属的用户ID" />
        </el-form-item>
        <el-form-item label="老人用户ID">
          <el-input v-model="bindForm.elderlyUserId" placeholder="老人的用户ID" />
        </el-form-item>
        <el-form-item label="关系">
          <el-select v-model="bindForm.relation" style="width:100%">
            <el-option label="子女" value="子女" />
            <el-option label="配偶" value="配偶" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindVisible=false">取消</el-button>
        <el-button type="primary" @click="doBind" :loading="binding">确认绑定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import http from '@/utils/request';
import SearchInput from '@/components/SearchInput.vue';
import { ElMessage } from 'element-plus';

const searchElderlyId = ref('');
const members = ref([]);
const loading = ref(false);

const bindVisible = ref(false);
const binding = ref(false);
const bindForm = ref({ familyUserId: '', elderlyUserId: '', relation: '子女' });

async function fetchMembers() {
  if (!searchElderlyId.value) {
    ElMessage.warning('请输入老人ID');
    return;
  }
  loading.value = true;
  try {
    members.value = await http.get(`/family/elderly/${searchElderlyId.value}/members`);
  } finally {
    loading.value = false;
  }
}

async function unbindFamily(familyUserId) {
  try {
    await http.post('/family/unbind', { familyUserId });
    ElMessage.success('解绑成功');
    fetchMembers();
  } catch {
    ElMessage.error('解绑失败');
  }
}

function showBindDialog() {
  bindForm.value = { familyUserId: '', elderlyUserId: '', relation: '子女' };
  bindVisible.value = true;
}

async function doBind() {
  if (!bindForm.value.familyUserId || !bindForm.value.elderlyUserId) {
    ElMessage.warning('请填写完整信息');
    return;
  }
  binding.value = true;
  try {
    await http.post('/family/bind', bindForm.value);
    ElMessage.success('绑定成功');
    bindVisible.value = false;
  } catch {
    ElMessage.error('绑定失败');
  } finally {
    binding.value = false;
  }
}
</script>
