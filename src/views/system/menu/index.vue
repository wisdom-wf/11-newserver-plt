<script setup lang="ts">
import { ref, h, onMounted } from 'vue';
import { NButton, NCard, NDataTable, NGrid, NGi, NModal, NPopconfirm, NTree, NSpace, NTag, useMessage } from 'naive-ui';
import { fetchGetMenuTree, fetchDeleteMenu, fetchCreateMenu, fetchUpdateMenu } from '@/service/api';
import type { DataTableColumns } from 'naive-ui';

defineOptions({
  name: 'SystemMenu'
});

const message = useMessage();

const treeData = ref<Api.User.Menu[]>([]);
const expandedKeys = ref<string[]>([]);

const columns: DataTableColumns<Api.User.Menu> = [
  { title: '菜单名称', key: 'menuName', width: 150 },
  { title: '菜单编码', key: 'menuCode', width: 120 },
  {
    title: '菜单类型',
    key: 'menuType',
    width: 80,
    render: (row: Api.User.Menu) => {
      const map: Record<string, string> = { MENU: '菜单', BUTTON: '按钮', PERMISSION: '权限' };
      const tagType = row.menuType === 'MENU' ? 'success' : row.menuType === 'BUTTON' ? 'info' : 'warning';
      return h(NTag, { type: tagType, size: 'small' }, () => map[row.menuType] || row.menuType);
    }
  },
  { title: '路由路径', key: 'path', width: 150 },
  { title: '图标', key: 'icon', width: 100 },
  { title: '排序', key: 'sortOrder', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: Api.User.Menu) =>
      h(NTag, { type: row.status === '1' ? 'success' : 'error', size: 'small' }, () =>
        row.status === '1' ? '启用' : '禁用'
      )
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right' as const,
    render: (row: Api.User.Menu) => {
      return h(NSpace, { size: 'small' }, () => [
        h(NButton, { size: 'small', onClick: () => handleEdit(row) }, () => '编辑'),
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
    await fetchUpdateMenu(formModel.id!, formModel);
    message.success('修改成功');
  }
  modalVisible.value = false;
  await getTreeData();
}

async function getTreeData() {
  const { data } = await fetchGetMenuTree();
  treeData.value = data || [];
  expandedKeys.value = getAllKeys(treeData.value);
}

function getAllKeys(menus: Api.User.Menu[]): string[] {
  const keys: string[] = [];
  menus.forEach(menu => {
    keys.push(menu.id);
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
              :scroll-x="900"
              :expanded-row-keys="expandedKeys"
              :row-key="(row: Api.User.Menu) => row.id"
              row-expandable
            />
          </NCard>
        </NGi>
        <NGi span="2 m:6">
          <NCard title="菜单树" :bordered="false">
            <template #header-extra>
              <NButton size="small" @click="getTreeData">刷新</NButton>
            </template>
            <NTree
              :data="treeData"
              :expanded-keys="expandedKeys"
              :block-line="true"
              block-node
              virtual-scroll
              @update:expanded-keys="keys => (expandedKeys = keys)"
            />
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
</template>
