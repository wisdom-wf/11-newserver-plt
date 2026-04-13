<script setup lang="ts">
import { ref, h, onMounted } from 'vue';
import {
  NButton,
  NCard,
  NDataTable,
  NModal,
  NPopconfirm,
  NTag,
  NSpace,
  NInput,
  NSelect,
  NCheckbox,
  NCheckboxGroup,
  useMessage
} from 'naive-ui';
import {
  fetchGetUserList,
  fetchDeleteUser,
  fetchCreateUser,
  fetchUpdateUser,
  fetchGetAllRoles,
  fetchAssignRoles
} from '@/service/api';
import type { DataTableColumns } from 'naive-ui';

defineOptions({
  name: 'SystemUser'
});

const message = useMessage();

const userData = ref<Api.User.User[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);

// Search
const searchUsername = ref('');
const searchRealname = ref('');
const searchPhone = ref('');

const columns: DataTableColumns<Api.User.User> = [
  { title: '用户名', key: 'userName', width: 120 },
  { title: '真实姓名', key: 'realName', width: 120 },
  { title: '手机号', key: 'phone', width: 130 },
  { title: '邮箱', key: 'email', width: 180 },
  {
    title: '用户类型',
    key: 'userType',
    width: 120,
    render: (row: Api.User.User) => {
      const map: Record<string, string> = {
        SUPER_ADMIN: '超级管理员',
        SYS_ADMIN: '系统管理员',
        CITY_ADMIN: '市级管理员',
        DISTRICT_ADMIN: '区县管理员',
        STREET_ADMIN: '街道管理员',
        COMMUNITY_ADMIN: '社区管理员',
        PROVIDER_ADMIN: '服务商管理员',
        STAFF: '服务人员'
      };
      return map[row.userType] || row.userType;
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: Api.User.User) =>
      h(NTag, { type: row.status === '1' ? 'success' : 'error', size: 'small' }, () =>
        row.status === '1' ? '启用' : '禁用'
      )
  },
  { title: '创建时间', key: 'createTime', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    fixed: 'right',
    render: (row: Api.User.User) => {
      return h(NSpace, { size: 'small' }, () => [
        h(NButton, { size: 'small', onClick: () => handleEdit(row) }, () => '编辑'),
        h(NButton, { size: 'small', onClick: () => handleAssignRole(row) }, () => '分配角色'),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDelete(row.id) },
          {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'),
            default: () => '确认删除吗？'
          }
        )
      ]);
    }
  }
];

// Modal
const modalVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const editingData = ref<Api.User.User | null>(null);

const form = ref({
  id: '',
  userName: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  userType: 'STAFF',
  status: '1',
  areaId: '',
  providerId: '',
  remark: ''
});

const roleOptions = ref<{ label: string; value: string }[]>([]);

// Assign role modal
const roleModalVisible = ref(false);
const currentUserId = ref('');
const currentUserName = ref('');
const selectedRoleIds = ref<string[]>([]);

async function getUserList() {
  const { data } = await fetchGetUserList({
    current: page.value,
    pageSize: pageSize.value,
    userName: searchUsername.value || undefined,
    realName: searchRealname.value || undefined,
    phone: searchPhone.value || undefined
  });
  userData.value = data?.records || [];
  total.value = data?.total || 0;
}

async function getRoleOptions() {
  const { data } = await fetchGetAllRoles();
  roleOptions.value = (data || []).map((role: Api.User.Role) => ({
    label: role.roleName,
    value: role.id
  }));
}

function handleAdd() {
  operateType.value = 'add';
  editingData.value = null;
  form.value = {
    id: '',
    userName: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    userType: 'STAFF',
    status: '1',
    areaId: '',
    providerId: '',
    remark: ''
  };
  modalVisible.value = true;
}

function handleEdit(row: Api.User.User) {
  operateType.value = 'edit';
  editingData.value = row;
  form.value = {
    id: row.id,
    userName: row.userName,
    password: '',
    realName: row.realName,
    phone: row.phone || '',
    email: row.email || '',
    userType: row.userType,
    status: row.status,
    areaId: row.areaId || '',
    providerId: row.providerId || '',
    remark: ''
  };
  modalVisible.value = true;
}

async function handleDelete(id: string) {
  await fetchDeleteUser(id);
  message.success('删除成功');
  await getUserList();
}

async function handleSubmit() {
  if (operateType.value === 'add') {
    await fetchCreateUser(form.value);
    message.success('添加成功');
  } else {
    await fetchUpdateUser(form.value.id, form.value);
    message.success('修改成功');
  }
  modalVisible.value = false;
  await getUserList();
}

async function handleAssignRole(row: Api.User.User) {
  currentUserId.value = row.id;
  currentUserName.value = row.realName || row.userName;
  selectedRoleIds.value = row.roles?.map(r => r.id) || [];
  roleModalVisible.value = true;
}

async function handleSaveRoles() {
  await fetchAssignRoles(currentUserId.value, { roleIds: selectedRoleIds.value });
  message.success('角色分配成功');
  roleModalVisible.value = false;
}

function handlePageChange(p: number) {
  page.value = p;
  getUserList();
}

function handlePageSizeChange(s: number) {
  pageSize.value = s;
  getUserList();
}

onMounted(() => {
  getUserList();
  getRoleOptions();
});
</script>

<template>
  <div>
    <NCard :bordered="false">
      <template #header>
        <NSpace justify="space-between" align="center" :wrap="true">
          <span style="font-size: 16px; font-weight: 600;">用户管理</span>
          <NSpace align="center" :wrap="true">
            <NInput v-model:value="searchUsername" placeholder="用户名" clearable style="width: 120px" />
            <NInput v-model:value="searchRealname" placeholder="真实姓名" clearable style="width: 120px" />
            <NInput v-model:value="searchPhone" placeholder="手机号" clearable style="width: 130px" />
            <NButton type="primary" @click="getUserList">搜索</NButton>
            <NButton type="primary" @click="handleAdd">新增</NButton>
          </NSpace>
        </NSpace>
      </template>
      <NDataTable :columns="columns" :data="userData" :scroll-x="1200" :row-key="(row: Api.User.User) => row.id" />
      <div style="padding: 12px 0">
        <NSpace justify="end">
          <NSelect
            v-model:value="pageSize"
            :options="[10, 20, 50].map(s => ({ label: `${s}条/页`, value: s }))"
            style="width: 120px"
            @update:value="handlePageSizeChange"
          />
          <NPagination v-model:page="page" :page-count="Math.ceil(total / pageSize)" @update:page="handlePageChange" />
        </NSpace>
      </div>
    </NCard>

    <!-- User Modal -->
    <NModal
      v-model:show="modalVisible"
      :title="operateType === 'add' ? '新增用户' : '编辑用户'"
      preset="card"
      style="width: 600px"
    >
      <NForm :model="form" label-placement="left" label-width="100">
        <NFormItem v-if="operateType === 'add'" label="用户名">
          <NInput v-model:value="form.userName" placeholder="请输入用户名" />
        </NFormItem>
        <NFormItem v-if="operateType === 'add'" label="密码">
          <NInput v-model:value="form.password" type="password" placeholder="请输入密码" />
        </NFormItem>
        <NFormItem label="真实姓名">
          <NInput v-model:value="form.realName" placeholder="请输入真实姓名" />
        </NFormItem>
        <NFormItem label="手机号">
          <NInput v-model:value="form.phone" placeholder="请输入手机号" />
        </NFormItem>
        <NFormItem label="邮箱">
          <NInput v-model:value="form.email" placeholder="请输入邮箱" />
        </NFormItem>
        <NFormItem label="用户类型">
          <NSelect
            v-model:value="form.userType"
            :options="[
              { label: '超级管理员', value: 'SUPER_ADMIN' },
              { label: '系统管理员', value: 'SYS_ADMIN' },
              { label: '市级管理员', value: 'CITY_ADMIN' },
              { label: '区县管理员', value: 'DISTRICT_ADMIN' },
              { label: '街道管理员', value: 'STREET_ADMIN' },
              { label: '社区管理员', value: 'COMMUNITY_ADMIN' },
              { label: '服务商管理员', value: 'PROVIDER_ADMIN' },
              { label: '服务人员', value: 'STAFF' }
            ]"
            style="width: 200px"
          />
        </NFormItem>
        <NFormItem label="状态">
          <NSpace>
            <NButton :type="form.status === '1' ? 'primary' : 'default'" @click="form.status = '1'">启用</NButton>
            <NButton :type="form.status === '2' ? 'primary' : 'default'" @click="form.status = '2'">禁用</NButton>
          </NSpace>
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="form.remark" type="textarea" placeholder="请输入备注" />
        </NFormItem>
        <NSpace justify="end">
          <NButton @click="modalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleSubmit">确认</NButton>
        </NSpace>
      </NForm>
    </NModal>

    <!-- Assign Role Modal -->
    <NModal v-model:show="roleModalVisible" :title="`分配角色 - ${currentUserName}`" preset="card" style="width: 400px">
      <NCheckboxGroup v-model:value="selectedRoleIds">
        <NSpace vertical>
          <NCheckbox v-for="role in roleOptions" :key="role.value" :value="role.value" :label="role.label" />
        </NSpace>
      </NCheckboxGroup>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="roleModalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleSaveRoles">保存</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
