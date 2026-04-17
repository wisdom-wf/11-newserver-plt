<script setup lang="ts">
import { ref, h, onMounted } from 'vue';
import {
  NButton,
  NCard,
  NDataTable,
  NGrid,
  NGi,
  NModal,
  NPopconfirm,
  NTree,
  NSpace,
  NTag,
  useMessage,
  NSpin
} from 'naive-ui';
import { fetchGetMenuTree, fetchDeleteMenu, fetchCreateMenu, fetchUpdateMenu } from '@/service/api';
import type { DataTableColumns } from 'naive-ui';

defineOptions({
  name: 'SystemMenu'
});

const message = useMessage();
const loading = ref(false);

const treeData = ref<Api.User.Menu[]>([]);
const expandedKeys = ref<string[]>([]);

const columns: DataTableColumns<Api.User.Menu> = [
  { title: '菜单名称', key: 'permissionName', width: 150 },
  { title: '菜单编码', key: 'permissionCode', width: 180 },
  {
    title: '菜单类型',
    key: 'permissionType',
    width: 80,
    render: (row: Api.User.Menu) => {
      const map: Record<string, string> = { MENU: '菜单', BUTTON: '按钮', API: '接口' };
      const tagType = row.permissionType === 'MENU' ? 'success' : row.permissionType === 'BUTTON' ? 'info' : 'warning';
      return h(NTag, { type: tagType, size: 'small' }, () => map[row.permissionType] || row.permissionType);
    }
  },
  { title: '路由路径', key: 'permissionUrl', width: 150 },
  { title: '请求方法', key: 'permissionMethod', width: 80 },
  { title: '图标', key: 'icon', width: 120 },
  { title: '排序', key: 'sortOrder', width: 60 },
  {
    title: '状态',
    key: 'status',
    width: 70,
    render: (row: Api.User.Menu) =>
      h(NTag, { type: row.status === 'NORMAL' ? 'success' : 'error', size: 'small' }, () =>
        row.status === 'NORMAL' ? '启用' : '禁用'
      )
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    fixed: 'right' as const,
    render: (row: Api.User.Menu) => {
      return h(NSpace, { size: 'small' }, () => [
        h(NButton, { size: 'small', onClick: () => handleEdit(row) }, () => '编辑'),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDelete(row.permissionId) },
          {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'),
            default: () => '确认删除吗？'
          }
        )
      ]);
    }
  }
];

const modalVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const editingData = ref<Api.User.Menu | null>(null);
const formRef = ref<InstanceType<typeof import('./modules/MenuForm.vue').default> | null>(null);

function handleAdd() {
  operateType.value = 'add';
  editingData.value = null;
  modalVisible.value = true;
}

function handleEdit(row: Api.User.Menu) {
  operateType.value = 'edit';
  editingData.value = row;
  modalVisible.value = true;
}

async function handleDelete(id: string) {
  await fetchDeleteMenu(id);
  message.success('删除成功');
  await getTreeData();
}

async function handleModalSubmit() {
  const formModel = formRef.value?.model;
  if (!formModel) return;

  if (operateType.value === 'add') {
    await fetchCreateMenu(formModel);
    message.success('添加成功');
  } else {
    await fetchUpdateMenu(formModel.permissionId!, formModel);
    message.success('修改成功');
  }
  modalVisible.value = false;
  await getTreeData();
}

async function getTreeData() {
  loading.value = true;
  try {
    const { data } = await fetchGetMenuTree();
    // 转换数据以适配前端显示
    treeData.value = (data || []).map((item: Api.User.Menu) => ({
      ...item,
      id: item.permissionId,
      label: item.permissionName
    }));
    expandedKeys.value = getAllKeys(treeData.value);
  } finally {
    loading.value = false;
  }
}

function getAllKeys(menus: Api.User.Menu[]): string[] {
  const keys: string[] = [];
  menus.forEach(menu => {
    keys.push(menu.permissionId);
    if (menu.children && menu.children.length > 0) {
      keys.push(...getAllKeys(menu.children));
    }
  });
  return keys;
}

onMounted(() => {
  getTreeData();
});
</script>

<template>
  <div>
    <NSpace vertical :size="16">
      <NCard :bordered="false">
        <NGrid :cols="2" :x-gap="16" responsive="screen" item-responsive>
          <NGi span="2 m:6">
            <NCard :bordered="false">
              <template #header>
                <NSpace>
                  <NButton type="primary" size="small" @click="handleAdd">新增</NButton>
                  <NButton size="small" @click="getTreeData">刷新</NButton>
                </NSpace>
              </template>
              <NDataTable
                :columns="columns"
                :data="treeData"
                :scroll-x="1200"
                :expanded-row-keys="expandedKeys"
                :row-key="(row: Api.User.Menu) => row.permissionId"
                row-expandable
              />
            </NCard>
          </NGi>
          <NGi span="2 m:6">
            <NCard title="菜单树" :bordered="false">
              <template #header-extra>
                <NButton size="small" @click="getTreeData">刷新</NButton>
              </template>
              <NSpin :show="loading">
                <NTree
                  v-if="treeData.length > 0"
                  :data="treeData"
                  :expanded-keys="expandedKeys"
                  :block-line="true"
                  block-node
                  virtual-scroll
                  label-field="permissionName"
                  @update:expanded-keys="keys => (expandedKeys = keys)"
                />
                <div v-else style="padding: 20px; text-align: center; color: #999">暂无数据</div>
              </NSpin>
            </NCard>
          </NGi>
        </NGrid>
      </NCard>
    </NSpace>

    <NModal
      v-model:show="modalVisible"
      :title="operateType === 'add' ? '新增菜单' : '编辑菜单'"
      preset="card"
      style="width: 600px"
    >
      <MenuForm
        ref="formRef"
        :operate-type="operateType"
        :editing-data="editingData"
        @close="modalVisible = false"
        @submit="handleModalSubmit"
      />
    </NModal>
  </div>
</template>
