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
  NTabPane,
  NTabs,
  NInput,
  useMessage
} from 'naive-ui';
import {
  fetchGetDictTypeList,
  fetchDeleteDictType,
  fetchCreateDictType,
  fetchUpdateDictType,
  fetchGetDictItemList,
  fetchDeleteDictItem,
  fetchCreateDictItem,
  fetchUpdateDictItem
} from '@/service/api';
import type { DataTableColumns } from 'naive-ui';

defineOptions({
  name: 'SystemDict'
});

const message = useMessage();

// Tab control
const activeTab = ref('type');

// Search
const searchName = ref('');
const searchCode = ref('');

// Dict Type data
const typeData = ref<Api.System.DictType[]>([]);
const typeColumns: DataTableColumns<Api.System.DictType> = [
  { title: '字典编码', key: 'dictCode', width: 150 },
  { title: '字典名称', key: 'dictName', width: 150 },
  { title: '描述', key: 'dictDesc', width: 200 },
  {
    title: '系统字典',
    key: 'isSystem',
    width: 100,
    render: (row: Api.System.DictType) =>
      h(NTag, { type: row.isSystem ? 'success' : 'default', size: 'small' }, () => (row.isSystem ? '是' : '否'))
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: Api.System.DictType) =>
      h(NTag, { type: row.status === '1' ? 'success' : 'error', size: 'small' }, () =>
        row.status === '1' ? '启用' : '禁用'
      )
  },
  { title: '排序', key: 'sortOrder', width: 80 },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row: Api.System.DictType) => {
      return h(NSpace, { size: 'small' }, () => [
        h(NButton, { size: 'small', onClick: () => handleEditType(row) }, () => '编辑'),
        h(NButton, { size: 'small', onClick: () => handleViewItems(row) }, () => '项管理'),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDeleteType(row.id) },
          {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'),
            default: () => '确认删除吗？'
          }
        )
      ]);
    }
  }
];

// Dict Item data
const currentDictTypeId = ref('');
const currentDictTypeName = ref('');
const currentDictTypeCode = ref('');
const itemData = ref<Api.System.DictItem[]>([]);
const itemColumns: DataTableColumns<Api.System.DictItem> = [
  { title: '字典项编码', key: 'itemCode', width: 150 },
  { title: '字典项名称', key: 'itemName', width: 150 },
  { title: '描述', key: 'itemDesc', width: 200 },
  { title: '排序', key: 'sortOrder', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row: Api.System.DictItem) =>
      h(NTag, { type: row.status === '1' ? 'success' : 'error', size: 'small' }, () =>
        row.status === '1' ? '启用' : '禁用'
      )
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row: Api.System.DictItem) => {
      return h(NSpace, { size: 'small' }, () => [
        h(NButton, { size: 'small', onClick: () => handleEditItem(row) }, () => '编辑'),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDeleteItem(row.id) },
          {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'),
            default: () => '确认删除吗？'
          }
        )
      ]);
    }
  }
];

// Type modal
const typeModalVisible = ref(false);
const typeOperateType = ref<'add' | 'edit'>('add');
const typeEditingData = ref<Api.System.DictType | null>(null);

// Type form
const typeForm = ref({
  dictCode: '',
  dictName: '',
  dictDesc: '',
  status: '1',
  sortOrder: 0
});

// Item modal
const itemModalVisible = ref(false);
const itemOperateType = ref<'add' | 'edit'>('add');
const itemEditingData = ref<Api.System.DictItem | null>(null);

// Item form
const itemForm = ref({
  dictTypeId: '',
  itemCode: '',
  itemName: '',
  itemDesc: '',
  status: '1',
  sortOrder: 0
});

async function getTypeList() {
  const { data } = await fetchGetDictTypeList({
    current: 1,
    pageSize: 100,
    dictName: searchName.value || undefined,
    dictCode: searchCode.value || undefined
  });
  typeData.value = data?.records || [];
}

async function getItemList(dictTypeCode: string) {
  const { data } = await fetchGetDictItemList({ dictTypeCode });
  itemData.value = data || [];
}

function handleAddType() {
  typeOperateType.value = 'add';
  typeEditingData.value = null;
  typeForm.value = { dictCode: '', dictName: '', dictDesc: '', status: '1', sortOrder: 0 };
  typeModalVisible.value = true;
}

function handleEditType(row: Api.System.DictType) {
  typeOperateType.value = 'edit';
  typeEditingData.value = row;
  typeForm.value = {
    dictCode: row.dictCode,
    dictName: row.dictName,
    dictDesc: row.dictDesc || '',
    status: row.status,
    sortOrder: row.sortOrder || 0
  };
  typeModalVisible.value = true;
}

async function handleDeleteType(id: string) {
  await fetchDeleteDictType(id);
  message.success('删除成功');
  await getTypeList();
}

async function handleViewItems(row: Api.System.DictType) {
  currentDictTypeId.value = row.id;
  currentDictTypeName.value = row.dictName;
  currentDictTypeCode.value = row.dictCode;
  activeTab.value = 'item';
  await getItemList(row.dictCode);
}

async function handleSubmitType() {
  if (typeOperateType.value === 'add') {
    await fetchCreateDictType(typeForm.value);
    message.success('添加成功');
  } else if (typeEditingData.value) {
    await fetchUpdateDictType(typeEditingData.value.id, typeForm.value);
    message.success('修改成功');
  }
  typeModalVisible.value = false;
  await getTypeList();
}

// Item operations
function handleAddItem() {
  itemOperateType.value = 'add';
  itemEditingData.value = null;
  itemForm.value = {
    dictTypeId: currentDictTypeId.value,
    itemCode: '',
    itemName: '',
    itemDesc: '',
    status: '1',
    sortOrder: 0
  };
  itemModalVisible.value = true;
}

function handleEditItem(row: Api.System.DictItem) {
  itemOperateType.value = 'edit';
  itemEditingData.value = row;
  itemForm.value = {
    dictTypeId: row.dictTypeId,
    itemCode: row.itemCode,
    itemName: row.itemName,
    itemDesc: row.itemDesc || '',
    status: row.status,
    sortOrder: row.sortOrder || 0
  };
  itemModalVisible.value = true;
}

async function handleDeleteItem(id: string) {
  await fetchDeleteDictItem(id);
  message.success('删除成功');
  await getItemList(currentDictTypeCode.value);
}

async function handleSubmitItem() {
  if (itemOperateType.value === 'add') {
    await fetchCreateDictItem(itemForm.value);
    message.success('添加成功');
  } else if (itemEditingData.value) {
    await fetchUpdateDictItem(itemEditingData.value.id, itemForm.value);
    message.success('修改成功');
  }
  itemModalVisible.value = false;
  await getItemList(currentDictTypeCode.value);
}

onMounted(() => {
  getTypeList();
});
</script>

<template>
  <div>
    <NCard title="字典管理" :bordered="false">
      <NTabs v-model:value="activeTab" type="line">
        <NTabPane name="type" tab="字典类型">
          <NCard :bordered="false">
            <template #header>
              <NSpace>
                <NInput v-model:value="searchName" placeholder="字典名称" clearable style="width: 150px" />
                <NInput v-model:value="searchCode" placeholder="字典编码" clearable style="width: 150px" />
                <NButton type="primary" @click="getTypeList">搜索</NButton>
                <NButton type="primary" @click="handleAddType">新增</NButton>
              </NSpace>
            </template>
            <NDataTable
              :columns="typeColumns"
              :data="typeData"
              :scroll-x="900"
              :row-key="(row: Api.System.DictType) => row.id"
            />
          </NCard>
        </NTabPane>
        <NTabPane name="item" tab="字典项" :disabled="!currentDictTypeId">
          <NCard :bordered="false">
            <template #header>
              <NSpace align="center">
                <span>当前字典：{{ currentDictTypeName }}</span>
                <NButton type="primary" @click="handleAddItem">新增</NButton>
              </NSpace>
            </template>
            <NDataTable
              :columns="itemColumns"
              :data="itemData"
              :scroll-x="800"
              :row-key="(row: Api.System.DictItem) => row.id"
            />
          </NCard>
        </NTabPane>
      </NTabs>
    </NCard>

    <!-- Type Modal -->
    <NModal
      v-model:show="typeModalVisible"
      :title="typeOperateType === 'add' ? '新增字典类型' : '编辑字典类型'"
      preset="card"
      style="width: 500px"
    >
      <NForm :model="typeForm" label-placement="left" label-width="100">
        <NFormItem label="字典编码">
          <NInput v-model:value="typeForm.dictCode" placeholder="请输入字典编码" />
        </NFormItem>
        <NFormItem label="字典名称">
          <NInput v-model:value="typeForm.dictName" placeholder="请输入字典名称" />
        </NFormItem>
        <NFormItem label="描述">
          <NInput v-model:value="typeForm.dictDesc" placeholder="请输入描述" />
        </NFormItem>
        <NFormItem label="状态">
          <NSpace>
            <NButton :type="typeForm.status === '1' ? 'primary' : 'default'" @click="typeForm.status = '1'">
              启用
            </NButton>
            <NButton :type="typeForm.status === '2' ? 'primary' : 'default'" @click="typeForm.status = '2'">
              禁用
            </NButton>
          </NSpace>
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="typeForm.sortOrder" :min="0" />
        </NFormItem>
        <NSpace justify="end">
          <NButton @click="typeModalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleSubmitType">确认</NButton>
        </NSpace>
      </NForm>
    </NModal>

    <!-- Item Modal -->
    <NModal
      v-model:show="itemModalVisible"
      :title="itemOperateType === 'add' ? '新增字典项' : '编辑字典项'"
      preset="card"
      style="width: 500px"
    >
      <NForm :model="itemForm" label-placement="left" label-width="100">
        <NFormItem label="字典项编码">
          <NInput v-model:value="itemForm.itemCode" placeholder="请输入字典项编码" />
        </NFormItem>
        <NFormItem label="字典项名称">
          <NInput v-model:value="itemForm.itemName" placeholder="请输入字典项名称" />
        </NFormItem>
        <NFormItem label="描述">
          <NInput v-model:value="itemForm.itemDesc" placeholder="请输入描述" />
        </NFormItem>
        <NFormItem label="状态">
          <NSpace>
            <NButton :type="itemForm.status === '1' ? 'primary' : 'default'" @click="itemForm.status = '1'">
              启用
            </NButton>
            <NButton :type="itemForm.status === '2' ? 'primary' : 'default'" @click="itemForm.status = '2'">
              禁用
            </NButton>
          </NSpace>
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="itemForm.sortOrder" :min="0" />
        </NFormItem>
        <NSpace justify="end">
          <NButton @click="itemModalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleSubmitItem">确认</NButton>
        </NSpace>
      </NForm>
    </NModal>
  </div>
</template>
