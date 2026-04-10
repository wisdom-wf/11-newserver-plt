<script setup lang="ts">
import { ref, h, onMounted } from 'vue';
import { NButton, NCard, NDataTable, NGrid, NGi, NSpace, NTree, NModal, NPopconfirm, NTag, useMessage } from 'naive-ui';
import { fetchGetAreaTree, fetchDeleteArea, fetchCreateArea, fetchUpdateArea } from '@/service/api';
import type { DataTableColumns } from 'naive-ui';

defineOptions({
  name: 'SystemArea'
});

const message = useMessage();

const treeData = ref<Api.User.Area[]>([]);
const expandedKeys = ref<string[]>([]);

const columns: DataTableColumns<Api.User.Area> = [
  { title: '区域编码', key: 'areaCode', width: 120 },
  { title: '区域名称', key: 'areaName', width: 150 },
  {
    title: '区域层级',
    key: 'level',
    width: 100,
    render: (row: Api.User.Area) => {
      const map: Record<string, string> = {
        PROVINCE: '省/直辖市',
        CITY: '市/区',
        DISTRICT: '区县',
        STREET: '街道/乡镇',
        COMMUNITY: '社区'
      };
      const tagType = row.level === 'PROVINCE' ? 'success' : row.level === 'CITY' ? 'info' : 'default';
      return h(NTag, { type: tagType, size: 'small' }, () => map[row.level] || row.level);
    }
  },
  { title: '排序', key: 'sortOrder', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: Api.User.Area) =>
      h(NTag, { type: row.status === '1' ? 'success' : 'error', size: 'small' }, () =>
        row.status === '1' ? '启用' : '禁用'
      )
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right' as const,
    render: (row: Api.User.Area) => {
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
const editingData = ref<Api.User.Area | null>(null);
const formRef = ref<InstanceType<typeof import('./modules/AreaForm.vue').default> | null>(null);

function handleAdd() {
  operateType.value = 'add';
  editingData.value = null;
  modalVisible.value = true;
}

function handleEdit(row: Api.User.Area) {
  operateType.value = 'edit';
  editingData.value = row;
  modalVisible.value = true;
}

async function handleDelete(id: string) {
  await fetchDeleteArea(id);
  message.success('删除成功');
  await getTreeData();
}

async function handleModalSubmit() {
  const formModel = formRef.value?.model;
  if (!formModel) return;

  if (operateType.value === 'add') {
    await fetchCreateArea(formModel);
    message.success('添加成功');
  } else {
    await fetchUpdateArea(formModel.id!, formModel);
    message.success('修改成功');
  }
  modalVisible.value = false;
  await getTreeData();
}

async function getTreeData() {
  const { data } = await fetchGetAreaTree();
  treeData.value = data || [];
  expandedKeys.value = treeData.value.map(item => item.id);
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
                :scroll-x="800"
                :expanded-row-keys="expandedKeys"
                :row-key="(row: Api.User.Area) => row.id"
                row-expandable
              />
            </NCard>
          </NGi>
          <NGi span="2 m:6">
            <NCard title="区域树" :bordered="false">
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
      :title="operateType === 'add' ? '新增区域' : '编辑区域'"
      preset="card"
      style="width: 600px"
    >
      <AreaForm
        ref="formRef"
        :operate-type="operateType"
        :editing-data="editingData"
        @close="modalVisible = false"
        @submit="handleModalSubmit"
      />
    </NModal>
  </div>
</template>
