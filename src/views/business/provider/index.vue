<script setup lang="ts">
import { ref, h, onMounted } from 'vue';
import {
  NButton,
  NCard,
  NDataTable,
  NModal,
  NTag,
  NSpace,
  NInput,
  NSelect,
  useMessage,
  NPagination,
  NStatistic
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import {
  fetchGetProviderList,
  fetchCreateProvider,
  fetchUpdateProvider,
  fetchDeleteProvider,
  fetchAuditProvider,
  fetchGetProviderStatistics
} from '@/service/api';

defineOptions({
  name: 'BusinessProvider'
});

const message = useMessage();

// Statistics
const statistics = ref<Api.Provider.Statistics>({
  total: 0,
  certified: 0,
  certifying: 0,
  suspended: 0
});

// Search
const searchName = ref('');
const searchCreditCode = ref('');
const searchServiceCategory = ref('');
const searchStatus = ref('');

// Table data
const loading = ref(false);
const tableData = ref<Api.Provider.Provider[]>([]);
const pagination = ref({ page: 1, pageSize: 10, total: 0 });

// Service category options
const categoryOptions = [
  { label: '养老服务', value: 'ELDER_CARE' },
  { label: '家政服务', value: 'HOME_CARE' }
];

// Status options
const statusOptions = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '2' }
];

// Audit status options
const auditStatusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' }
];

function getCategoryLabel(category?: string): string {
  const option = categoryOptions.find(o => o.value === category);
  return option?.label || category || '';
}

function getAuditStatusType(status?: string): 'warning' | 'success' | 'error' | 'default' {
  const map: Record<string, 'warning' | 'success' | 'error' | 'default'> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'error'
  };
  return map[status || ''] || 'default';
}

function getAuditStatusLabel(status?: string): string {
  const option = auditStatusOptions.find(o => o.value === status);
  return option?.label || status || '';
}

const columns: DataTableColumns<Api.Provider.Provider> = [
  { title: '服务商名称', key: 'name', width: 180 },
  { title: '信用代码', key: 'creditCode', width: 180 },
  { title: '法人', key: 'legalPerson', width: 100 },
  { title: '联系电话', key: 'phone', width: 130 },
  { title: '区域', key: 'areaName', width: 120 },
  { title: '服务类别', key: 'serviceCategory', width: 100, render: row => getCategoryLabel(row.serviceCategory) },
  {
    title: '审核状态',
    key: 'auditStatus',
    width: 100,
    render: row =>
      h(NTag, { type: getAuditStatusType(row.auditStatus), size: 'small' }, () => getAuditStatusLabel(row.auditStatus))
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: row =>
      h(NTag, { type: row.status === '1' ? 'success' : 'error', size: 'small' }, () =>
        row.status === '1' ? '启用' : '禁用'
      )
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: row =>
      h(NSpace, { size: 'small' }, () => [
        h(NButton, { size: 'small', onClick: () => handleEdit(row) }, () => '编辑'),
        h(NButton, { size: 'small', onClick: () => handleAudit(row) }, () => '审核'),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.id) }, () => '删除')
      ])
  }
];

// Modal
const modalVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const editingData = ref<Api.Provider.Provider | null>(null);

const form = ref({
  name: '',
  creditCode: '',
  legalPerson: '',
  phone: '',
  address: '',
  serviceCategory: 'ELDER_CARE' as Api.Order.ServiceCategory,
  description: '',
  status: '1' as Api.Common.EnableStatus
});

// Audit modal
const auditModalVisible = ref(false);
const auditForm = ref({ auditStatus: 'APPROVED' as Api.Provider.AuditStatus, auditRemark: '' });
const currentAuditId = ref('');

async function getStatistics() {
  try {
    const { data } = await fetchGetProviderStatistics();
    if (data) {
      statistics.value = data;
    }
  } catch (e) {
    console.error('Failed to get statistics', e);
  }
}

async function getTableData() {
  loading.value = true;
  try {
    const params: Api.Provider.ProviderQuery & Api.Common.PaginatingQueryParams = {
      current: pagination.value.page,
      pageSize: pagination.value.pageSize
    };
    if (searchName.value) params.name = searchName.value;
    if (searchCreditCode.value) params.creditCode = searchCreditCode.value;
    if (searchServiceCategory.value) params.serviceCategory = searchServiceCategory.value;
    if (searchStatus.value) params.status = searchStatus.value;

    const { data } = await fetchGetProviderList(params);
    tableData.value = data?.records || [];
    pagination.value.total = data?.total || 0;
  } catch (e) {
    console.error('Failed to get table data', e);
  } finally {
    loading.value = false;
  }
}

function handleAdd() {
  operateType.value = 'add';
  editingData.value = null;
  form.value = {
    name: '',
    creditCode: '',
    legalPerson: '',
    phone: '',
    address: '',
    serviceCategory: 'ELDER_CARE',
    description: '',
    status: '1'
  };
  modalVisible.value = true;
}

function handleEdit(row: Api.Provider.Provider) {
  operateType.value = 'edit';
  editingData.value = row;
  form.value = {
    name: row.name,
    creditCode: row.creditCode || '',
    legalPerson: row.legalPerson || '',
    phone: row.phone || '',
    address: row.address || '',
    serviceCategory: row.serviceCategory,
    description: row.description || '',
    status: row.status
  };
  modalVisible.value = true;
}

function handleAudit(row: Api.Provider.Provider) {
  currentAuditId.value = row.id;
  auditForm.value = { auditStatus: row.auditStatus || 'PENDING', auditRemark: row.auditRemark || '' };
  auditModalVisible.value = true;
}

async function handleDelete(id: string) {
  try {
    await fetchDeleteProvider(id);
    message.success('删除成功');
    await getTableData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to delete', e);
  }
}

async function handleSubmit() {
  try {
    if (operateType.value === 'add') {
      await fetchCreateProvider(form.value);
      message.success('添加成功');
    } else if (editingData.value) {
      await fetchUpdateProvider(editingData.value.id, form.value);
      message.success('修改成功');
    }
    modalVisible.value = false;
    await getTableData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to submit', e);
  }
}

async function handleAuditSubmit() {
  try {
    await fetchAuditProvider(currentAuditId.value, auditForm.value);
    message.success('审核成功');
    auditModalVisible.value = false;
    await getTableData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to audit', e);
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page;
  getTableData();
}

function handlePageSizeChange(pageSize: number) {
  pagination.value.pageSize = pageSize;
  pagination.value.page = 1;
  getTableData();
}

onMounted(() => {
  getStatistics();
  getTableData();
});
</script>

<template>
  <div>
    <!-- Statistics Cards -->
    <NCard title="服务商统计" :bordered="false" style="margin-bottom: 16px">
      <NSpace :size="20" :wrap="true">
        <NStatistic label="总数" :value="statistics.total" />
        <NStatistic label="已认证" :value="statistics.certified" />
        <NStatistic label="认证中" :value="statistics.certifying" />
        <NStatistic label="已暂停" :value="statistics.suspended" />
      </NSpace>
    </NCard>

    <!-- Table -->
    <NCard title="服务商管理" :bordered="false">
      <template #header>
        <NSpace :wrap="true">
          <NInput v-model:value="searchName" placeholder="服务商名称" clearable style="width: 150px" />
          <NInput v-model:value="searchCreditCode" placeholder="信用代码" clearable style="width: 180px" />
          <NSelect
            v-model:value="searchServiceCategory"
            :options="categoryOptions"
            placeholder="服务类别"
            clearable
            style="width: 120px"
          />
          <NSelect
            v-model:value="searchStatus"
            :options="statusOptions"
            placeholder="状态"
            clearable
            style="width: 100px"
          />
          <NButton type="primary" @click="getTableData">搜索</NButton>
          <NButton type="primary" @click="handleAdd">新增</NButton>
        </NSpace>
      </template>
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1400"
        :row-key="(row: Api.Provider.Provider) => row.id"
      />
      <div style="padding: 12px 0">
        <NSpace justify="end">
          <NSelect
            v-model:value="pagination.pageSize"
            :options="[10, 20, 50].map(s => ({ label: `${s}条/页`, value: s }))"
            style="width: 120px"
            @update:value="handlePageSizeChange"
          />
          <NPagination
            v-model:page="pagination.page"
            :page-count="Math.ceil(pagination.total / pagination.pageSize)"
            @update:page="handlePageChange"
          />
        </NSpace>
      </div>
    </NCard>

    <!-- Provider Modal -->
    <NModal
      v-model:show="modalVisible"
      :title="operateType === 'add' ? '新增服务商' : '编辑服务商'"
      preset="card"
      style="width: 500px"
    >
      <NForm :model="form" label-placement="left" label-width="100">
        <NFormItem label="服务商名称">
          <NInput v-model:value="form.name" placeholder="请输入服务商名称" />
        </NFormItem>
        <NFormItem label="信用代码">
          <NInput v-model:value="form.creditCode" placeholder="请输入统一社会信用代码" />
        </NFormItem>
        <NFormItem label="法人">
          <NInput v-model:value="form.legalPerson" placeholder="请输入法人姓名" />
        </NFormItem>
        <NFormItem label="联系电话">
          <NInput v-model:value="form.phone" placeholder="请输入联系电话" />
        </NFormItem>
        <NFormItem label="地址">
          <NInput v-model:value="form.address" placeholder="请输入地址" />
        </NFormItem>
        <NFormItem label="服务类别">
          <NSelect v-model:value="form.serviceCategory" :options="categoryOptions" style="width: 150px" />
        </NFormItem>
        <NFormItem label="简介">
          <NInput v-model:value="form.description" type="textarea" placeholder="请输入简介" />
        </NFormItem>
        <NSpace justify="end">
          <NButton @click="modalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleSubmit">确认</NButton>
        </NSpace>
      </NForm>
    </NModal>

    <!-- Audit Modal -->
    <NModal v-model:show="auditModalVisible" title="审核服务商" preset="card" style="width: 500px">
      <NForm :model="auditForm" label-placement="left" label-width="100">
        <NFormItem label="审核结果">
          <NSelect v-model:value="auditForm.auditStatus" :options="auditStatusOptions" style="width: 150px" />
        </NFormItem>
        <NFormItem label="审核备注">
          <NInput v-model:value="auditForm.auditRemark" type="textarea" placeholder="请输入审核备注" />
        </NFormItem>
        <NSpace justify="end">
          <NButton @click="auditModalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleAuditSubmit">确认</NButton>
        </NSpace>
      </NForm>
    </NModal>
  </div>
</template>
