/* Copyright (c) 2026 星火众擎 - 本软件受《中华人民共和国著作权法》保护 */

<!-- 用户管理 -->
<template>
  <div class="page">
    <!-- 筛选 -->
    <div class="filter-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索用户名/手机号" clearable @clear="fetchUsers" @keyup.enter="fetchUsers" style="width:240px" />
      <el-select v-model="searchForm.role" placeholder="全部角色" clearable @change="fetchUsers" style="width:140px">
        <el-option label="全部角色" value="" />
        <el-option label="需求者" value="ELDERLY" />
        <el-option label="志愿者" value="VOLUNTEER" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
      <el-button color="#ff6b6b" style="color:#fff" @click="fetchUsers">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="id" label="编号" width="70" align="center" />
        <el-table-column label="头像" width="70" align="center">
          <template #default="{row}">
            <el-avatar :size="36" :src="row.avatar">{{ (row.nickname||row.name||'?')[0] }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column label="昵称" min-width="100" show-overflow-tooltip>
          <template #default="{row}">{{ row.nickname || row.name || '-' }}</template>
        </el-table-column>
        <el-table-column label="角色" width="90" align="center">
          <template #default="{row}">
            <span class="role-tag" :class="row.role">{{ roleMap[row.role] || row.role }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" align="center" />
        <el-table-column prop="community" label="社区" min-width="100" show-overflow-tooltip />
        <el-table-column label="注册时间" width="100" align="center">
          <template #default="{row}">{{ shortDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{row}">
            <span class="status-dot" :class="row.isDeleted ? 'off' : 'on'">{{ row.isDeleted ? '禁用' : '正常' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{row}">
            <el-button type="danger" link size="small" @click="showDetail(row)">详情</el-button>
            <el-button link size="small" style="color:#ffa502" @click="showEdit(row)">编辑</el-button>
            <el-popconfirm :title="row.isDeleted?'确认启用？':'确认禁用？'" @confirm="toggleStatus(row)">
              <template #reference>
                <el-button link size="small" :style="{color:row.isDeleted?'#2ed573':'#999'}">{{ row.isDeleted?'启用':'禁用' }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <PaginationFooter :page="page" :size="size" :total="total" @change="fetchUsers" />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="600px" destroy-on-close>
      <template v-if="detailUser">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="编号">{{ detailUser.id }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detailUser.nickname||detailUser.name||'-' }}</el-descriptions-item>
          <el-descriptions-item label="角色"><span class="role-tag" :class="detailUser.role">{{ roleMap[detailUser.role] }}</span></el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailUser.phone||'-' }}</el-descriptions-item>
          <el-descriptions-item label="社区">{{ detailUser.community||'-' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ detailUser.address||'-' }}</el-descriptions-item>
          <el-descriptions-item label="积分">{{ detailUser.points||0 }}</el-descriptions-item>
          <el-descriptions-item label="完成任务">{{ detailUser.completedTasks||0 }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ fullDate(detailUser.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detailUser.isDeleted?'已禁用':'正常' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑用户" width="480px" destroy-on-close>
      <el-form v-if="editForm" :model="editForm" label-width="70px">
        <el-form-item label="昵称"><el-input v-model="editForm.nickname" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="editForm.phone" /></el-form-item>
        <el-form-item label="社区"><el-input v-model="editForm.community" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="editForm.address" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" style="width:100%">
            <el-option label="需求者" value="ELDERLY" />
            <el-option label="志愿者" value="VOLUNTEER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible=false">取消</el-button>
        <el-button color="#ff6b6b" style="color:#fff" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import http from '@/utils/request';
import PaginationFooter from '@/components/PaginationFooter.vue';
const userList = ref([]);
const loading = ref(false);
const page = ref(1);
const size = ref(20);
const total = ref(0);
const detailVisible = ref(false);
const detailUser = ref(null);
const editVisible = ref(false);
const editForm = ref(null);
const saving = ref(false);

const searchForm = reactive({ keyword:'', role:'' });
const roleMap = { ELDERLY:'需求者', VOLUNTEER:'志愿者', ADMIN:'管理员' };

onMounted(() => fetchUsers());

function shortDate(t){ if(!t) return '-'; const d=new Date(t); return `${d.getMonth()+1}/${d.getDate()} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`; }
function fullDate(t){ if(!t) return ''; const d=new Date(t); return `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`; }

async function fetchUsers(){
  loading.value=true;
  try{
    const params = { page:page.value-1, size:size.value };
    if(searchForm.keyword) params.keyword = searchForm.keyword;
    if(searchForm.role) params.role = searchForm.role;
    const data = await http.get('/users', { params });
    userList.value = data || [];
    total.value = userList.value.length;
  }catch(e){ userList.value=[]; ElMessage.error('加载用户列表失败'); }
  loading.value=false;
}

function resetSearch(){ searchForm.keyword=''; searchForm.role=''; page.value=1; fetchUsers(); }

function showDetail(row){ detailUser.value=row; detailVisible.value=true; }

function showEdit(row){
  editForm.value = {
    id:row.id, nickname:row.nickname||row.name||'',
    phone:row.phone||'', community:row.community||'',
    address:row.address||'', role:row.role||'ELDERLY'
  };
  editVisible.value=true;
}

async function saveEdit(){
  saving.value=true;
  try{
    await http.put('/users/update', editForm.value);
    ElMessage.success('保存成功');
    editVisible.value=false;
    fetchUsers();
  }catch(e){ ElMessage.error('保存失败'); }
  saving.value=false;
}

async function toggleStatus(row){
  try{
    const newDeleted = row.isDeleted ? 0 : 1;
    await http.put('/users/update', { id:row.id, isDeleted:newDeleted });
    ElMessage.success(newDeleted?'已禁用':'已启用');
    fetchUsers();
  }catch(e){ ElMessage.error('操作失败'); }
}
</script>

<style scoped>
.page { display:flex; flex-direction:column; gap:14px; }
.filter-bar { display:flex; gap:10px; align-items:center; }

.role-tag {
  display:inline-block; padding:2px 10px; border-radius:4px;
  font-size:12px; font-weight:700; color:#fff;
}
.role-tag.ADMIN { background:#ff6b6b; }
.role-tag.VOLUNTEER { background:#2ed573; }
.role-tag.ELDERLY { background:#ffa502; }

.status-dot { font-size:12px; font-weight:600; }
.status-dot.on { color:#2ed573; }
.status-dot.off { color:#999; }

.pager { display:flex; justify-content:flex-end; margin-top:14px; }
</style>
