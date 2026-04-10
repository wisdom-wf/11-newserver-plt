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
  fetchGetStaffList,
  fetchCreateStaff,
  fetchUpdateStaff,
  fetchDeleteStaff,
  fetchGetStaffStatistics
} from '@/service/api';

defineOptions({
  name: 'BusinessStaff'
});

const message = useMessage();

// Statistics
const statistics = ref<Api.Staff.Statistics>({
  total: 0,
  active: 0,
  pending: 0,
  inactive: 0,
  avgRating: 0
});

// Search
const searchName = ref('');
const searchPhone = ref('');
const searchServiceCategory = ref('');
const searchStatus = ref('');

// Table data
const loading = ref(false);
const tableData = ref<Api.Staff.Staff[]>([]);
const pagination = ref({ page: 1, pageSize: 10, total: 0 });

// Gender options
const genderOptions = [
  { label: '男', value: 'MALE' },
  { label: '女', value: 'FEMALE' },
  { label: '未知', value: 'UNKNOWN' }
];

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

function getGenderLabel(gender?: string): string {
  const option = genderOptions.find(o => o.value === gender);
  return option?.label || gender || '';
}

function getCategoryLabel(category?: string): string {
  const option = categoryOptions.find(o => o.value === category);
  return option?.label || category || '';
}

const columns: DataTableColumns<Api.Staff.Staff> = [
  { title: '工号', key: 'staffNo', width: 100 },
  { title: '姓名', key: 'name', width: 100 },
  { title: '性别', key: 'gender', width: 60, render: row => getGenderLabel(row.gender) },
  { title: '年龄', key: 'age', width: 60 },
  { title: '手机号', key: 'phone', width: 130 },
  { title: '所属服务商', key: 'providerName', width: 150 },
  { title: '服务类别', key: 'serviceCategory', width: 100, render: row => getCategoryLabel(row.serviceCategory) },
  { title: '接单数', key: 'orderCount', width: 80 },
  { title: '评分', key: 'rating', width: 80 },
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
    width: 150,
    fixed: 'right',
    render: row =>
      h(NSpace, { size: 'small' }, () => [
        h(NButton, { size: 'small', onClick: () => handleEdit(row) }, () => '编辑'),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.id) }, () => '删除')
      ])
  }
];

// Modal
const modalVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const editingData = ref<Api.Staff.Staff | null>(null);

const form = ref({
  name: '',
  gender: 'UNKNOWN' as Api.Staff.Gender,
  idCard: '',
  phone: '',
  serviceCategory: 'ELDER_CARE' as Api.Order.ServiceCategory,
  providerId: '',
  emergencyContact: '',
  emergencyPhone: '',
  description: '',
  status: '1' as Api.Common.EnableStatus
});

async function getStatistics() {
  try {
    const { data } = await fetchGetStaffStatistics();
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
    const params: Api.Staff.StaffQuery & Api.Common.PaginatingQueryParams = {
      current: pagination.value.page,
      pageSize: pagination.value.pageSize
    };
    if (searchName.value) params.name = searchName.value;
    if (searchPhone.value) params.phone = searchPhone.value;
    if (searchServiceCategory.value) params.serviceCategory = searchServiceCategory.value;
    if (searchStatus.value) params.status = searchStatus.value;

    const { data } = await fetchGetStaffList(params);
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
    gender: 'UNKNOWN',
    idCard: '',
    phone: '',
    serviceCategory: 'ELDER_CARE',
    providerId: '',
    emergencyContact: '',
    emergencyPhone: '',
    description: '',
    status: '1'
  };
  modalVisible.value = true;
}

function handleEdit(row: Api.Staff.Staff) {
  operateType.value = 'edit';
  editingData.value = row;
  form.value = {
    name: row.name,
    gender: row.gender || 'UNKNOWN',
    idCard: row.idCard || '',
    phone: row.phone || '',
    serviceCategory: row.serviceCategory,
    providerId: row.providerId,
    emergencyContact: row.emergencyContact || '',
    emergencyPhone: row.emergencyPhone || '',
    description: row.description || '',
    status: row.status
  };
  modalVisible.value = true;
}

async function handleDelete(id: string) {
  try {
    await fetchDeleteStaff(id);
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
      await fetchCreateStaff(form.value);
      message.success('添加成功');
    } else if (editingData.value) {
      await fetchUpdateStaff(editingData.value.id, form.value);
      message.success('修改成功');
    }
    modalVisible.value = false;
    await getTableData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to submit', e);
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
    <NCard title="服务人员统计" :bordered="false" style="margin-bottom: 16px">
      <NSpace :size="20" :wrap="true">
        <NStatistic label="总人数" :value="statistics.total" />
        <NStatistic label="在职" :value="statistics.active" />
        <NStatistic label="待上岗" :value="statistics.pending" />
        <NStatistic label="已离职" :value="statistics.inactive" />
        <NStatistic label="平均评分" :value="`${statistics.avgRating.toFixed(1)}分`" />
      </NSpace>
    </NCard>

    <!-- Table -->
    <NCard title="服务人员管理" :bordered="false">
      <template #header>
        <NSpace :wrap="true">
          <NInput v-model:value="searchName" placeholder="姓名" clearable style="width: 100px" />
          <NInput v-model:value="searchPhone" placeholder="手机号" clearable style="width: 130px" />
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
        :row-key="(row: Api.Staff.Staff) => row.id"
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

    <!-- Staff Modal -->
    <NModal
      v-model:show="modalVisible"
      :title="operateType === 'add' ? '新增服务人员' : '编辑服务人员'"
      preset="card"
      style="width: 500px"
    >
      <NForm :model="form" label-placement="left" label-width="100">
        <NFormItem label="姓名">
          <NInput v-model:value="form.name" placeholder="请输入姓名" />
        </NFormItem>
        <NFormItem label="性别">
          <NSelect v-model:value="form.gender" :options="genderOptions" style="width: 120px" />
        </NFormItem>
        <NFormItem label="身份证号">
          <NInput v-model:value="form.idCard" placeholder="请输入身份证号" />
        </NFormItem>
        <NFormItem label="手机号">
          <NInput v-model:value="form.phone" placeholder="请输入手机号" />
        </NFormItem>
        <NFormItem label="服务类别">
          <NSelect v-model:value="form.serviceCategory" :options="categoryOptions" style="width: 150px" />
        </NFormItem>
        <NFormItem label="服务商ID">
          <NInput v-model:value="form.providerId" placeholder="请输入服务商ID" />
        </NFormItem>
        <NFormItem label="紧急联系人">
          <NInput v-model:value="form.emergencyContact" placeholder="请输入紧急联系人" />
        </NFormItem>
        <NFormItem label="紧急联系电话">
          <NInput v-model:value="form.emergencyPhone" placeholder="请输入紧急联系电话" />
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
  </div>
</template>
