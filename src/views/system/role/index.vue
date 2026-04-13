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
  NDrawer,
  NDrawerContent,
  NTree,
  useMessage
} from 'naive-ui';
import {
  fetchGetRoleList,
  fetchDeleteRole,
  fetchCreateRole,
  fetchUpdateRole,
  fetchGetRoleMenus,
  fetchAssignRoleMenus,
  fetchGetMenuTree
} from '@/service/api';
import type { DataTableColumns } from 'naive-ui';

defineOptions({
  name: 'SystemRole'
});

const message = useMessage();

const roleData = ref<Api.User.Role[]>([]);
const roleColumns: DataTableColumns<Api.User.Role> = [
  { title: '角色编码', key: 'roleCode', width: 150 },
  { title: '角色名称', key: 'roleName', width: 150 },
  { title: '角色类型', key: 'roleType', width: 120 },
  { title: '数据范围', key: 'dataScope', width: 120 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: Api.User.Role) =>
      h(NTag, { type: row.status === '1' ? 'success' : 'error', size: 'small' }, () =>
        row.status === '1' ? '启用' : '禁用'
      )
  },
  { title: '排序', key: 'sortOrder', width: 80 },
  { title: '创建时间', key: 'createTime', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: (row: Api.User.Role) => {
      return h(NSpace, { size: 'small' }, () => [
        h(NButton, { size: 'small', onClick: () => handleEdit(row) }, () => '编辑'),
        h(NButton, { size: 'small', onClick: () => handleAssignMenu(row) }, () => '权限分配'),
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
const editingData = ref<Api.User.Role | null>(null);

const form = ref({
  roleCode: '',
  roleName: '',
  roleType: 'SYSTEM',
  dataScope: 'AREA',
  status: '1',
  sortOrder: 0,
  remark: ''
} as Api.User.RoleForm);

// Permission drawer
const drawerVisible = ref(false);
const currentRoleId = ref('');
const currentRoleName = ref('');
const menuTreeData = ref<any[]>([]);
const expandedKeys = ref<string[]>([]);
const checkedKeys = ref<string[]>([]);

async function getRoleList() {
  const { data } = await fetchGetRoleList();
  roleData.value = data?.records || [];
}

function handleAdd() {
  operateType.value = 'add';
  editingData.value = null;
  form.value = {
    roleCode: '',
    roleName: '',
    roleType: 'SYSTEM',
    dataScope: 'AREA',
    status: '1',
    sortOrder: 0,
    remark: ''
  };
  modalVisible.value = true;
}

function handleEdit(row: Api.User.Role) {
  operateType.value = 'edit';
  editingData.value = row;
  form.value = {
    roleCode: row.roleCode,
    roleName: row.roleName,
    roleType: row.roleType || 'SYSTEM',
    dataScope: row.dataScope || 'AREA',
    status: row.status,
    sortOrder: row.sortOrder || 0,
    remark: ''
  };
  modalVisible.value = true;
}

async function handleDelete(id: string) {
  await fetchDeleteRole(id);
  message.success('删除成功');
  await getRoleList();
}

async function handleSubmit() {
  if (operateType.value === 'add') {
    await fetchCreateRole(form.value);
    message.success('添加成功');
  } else if (editingData.value) {
    await fetchUpdateRole(editingData.value.id, form.value);
    message.success('修改成功');
  }
  modalVisible.value = false;
  await getRoleList();
}

// Permission assignment
async function handleAssignMenu(row: Api.User.Role) {
  currentRoleId.value = row.id;
  currentRoleName.value = row.roleName;
  drawerVisible.value = true;
  const { data: menuTree } = await fetchGetMenuTree();
  const { data: checkedMenuIds } = await fetchGetRoleMenus(row.id);
  menuTreeData.value = transformMenuToTree(menuTree || []);
  checkedKeys.value = checkedMenuIds || [];
  expandedKeys.value = getAllMenuIds(menuTreeData.value);
}

function transformMenuToTree(menus: Api.User.Menu[]): any[] {
  return menus.map(menu => ({
    key: menu.permissionId,
    label: menu.permissionName,
    children: menu.children ? transformMenuToTree(menu.children) : undefined
  }));
}

function getAllMenuIds(menus: any[]): string[] {
  const ids: string[] = [];
  menus.forEach(menu => {
    ids.push(menu.key);
    if (menu.children) {
      ids.push(...getAllMenuIds(menu.children));
    }
  });
  return ids;
}

async function handleSaveMenus() {
  await fetchAssignRoleMenus(currentRoleId.value, checkedKeys.value);
  message.success('权限分配成功');
  drawerVisible.value = false;
}

onMounted(() => {
  getRoleList();
});
</script>

<template>
  <div>
    <NCard :bordered="false">
      <template #header>
        <NSpace justify="space-between" align="center">
          <span style="font-size: 16px; font-weight: 600;">角色管理</span>
          <NSpace>
            <NButton type="primary" @click="handleAdd">新增</NButton>
            <NButton @click="getRoleList">刷新</NButton>
          </NSpace>
        </NSpace>
      </template>
      <NDataTable :columns="roleColumns" :data="roleData" :scroll-x="1100" :row-key="(row: Api.User.Role) => row.id" />
    </NCard>

    <!-- Role Modal -->
    <NModal
      v-model:show="modalVisible"
      :title="operateType === 'add' ? '新增角色' : '编辑角色'"
      preset="card"
      style="width: 500px"
    >
      <NForm :model="form" label-placement="left" label-width="100">
        <NFormItem label="角色编码">
          <NInput v-model:value="form.roleCode" placeholder="请输入角色编码" />
        </NFormItem>
        <NFormItem label="角色名称">
          <NInput v-model:value="form.roleName" placeholder="请输入角色名称" />
        </NFormItem>
        <NFormItem label="角色类型">
          <NSpace>
            <NButton :type="form.roleType === 'SYSTEM' ? 'primary' : 'default'" @click="form.roleType = 'SYSTEM'">
              系统角色
            </NButton>
            <NButton :type="form.roleType === 'BUSINESS' ? 'primary' : 'default'" @click="form.roleType = 'BUSINESS'">
              业务角色
            </NButton>
          </NSpace>
        </NFormItem>
        <NFormItem label="数据范围">
          <NSpace vertical>
            <NButton :type="form.dataScope === 'ALL' ? 'primary' : 'default'" block @click="form.dataScope = 'ALL'">
              全部数据
            </NButton>
            <NButton :type="form.dataScope === 'AREA' ? 'primary' : 'default'" block @click="form.dataScope = 'AREA'">
              本区域数据
            </NButton>
            <NButton
              :type="form.dataScope === 'PROVIDER' ? 'primary' : 'default'"
              block
              @click="form.dataScope = 'PROVIDER'"
            >
              本服务商数据
            </NButton>
            <NButton :type="form.dataScope === 'SELF' ? 'primary' : 'default'" block @click="form.dataScope = 'SELF'">
              仅本人数据
            </NButton>
          </NSpace>
        </NFormItem>
        <NFormItem label="状态">
          <NSpace>
            <NButton :type="form.status === '1' ? 'primary' : 'default'" @click="form.status = '1'">启用</NButton>
            <NButton :type="form.status === '2' ? 'primary' : 'default'" @click="form.status = '2'">禁用</NButton>
          </NSpace>
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="form.sortOrder" :min="0" />
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

    <!-- Permission Drawer -->
    <NDrawer v-model:show="drawerVisible" :width="400" placement="right">
      <NDrawerContent :title="`权限分配 - ${currentRoleName}`" closable>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="drawerVisible = false">取消</NButton>
            <NButton type="primary" @click="handleSaveMenus">保存</NButton>
          </NSpace>
        </template>
        <NTree
          :data="menuTreeData"
          :expanded-keys="expandedKeys"
          :checked-keys="checkedKeys"
          checkable
          virtual-scroll
          @update:expanded-keys="keys => (expandedKeys = keys)"
          @update:checked-keys="keys => (checkedKeys = keys)"
        />
      </NDrawerContent>
    </NDrawer>
  </div>
</template>
