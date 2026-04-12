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
  { label: '女', value: 0 },
  { label: '男', value: 1 }
];

// Status options
const statusOptions = [
  { label: '在职', value: 'ON_JOB' },
  { label: '离职', value: 'OFF_JOB' }
];

function getGenderLabel(gender?: number): string {
  if (gender === 0) return '女';
  if (gender === 1) return '男';
  return '未知';
}

function getStatusType(status?: string): 'success' | 'warning' | 'error' | 'default' {
  if (status === 'ON_JOB') return 'success';
  if (status === 'OFF_JOB') return 'error';
  return 'default';
}

function getStatusLabel(status?: string): string {
  if (status === 'ON_JOB') return '在职';
  if (status === 'OFF_JOB') return '离职';
  return status || '';
}

const columns: DataTableColumns<Api.Staff.Staff> = [
  { title: '工号', key: 'staffNo', width: 110 },
  { title: '姓名', key: 'staffName', width: 90 },
  { title: '性别', key: 'gender', width: 60, render: row => getGenderLabel(row.gender) },
  { title: '年龄', key: 'age', width: 60 },
  { title: '手机号', key: 'phone', width: 120 },
  { title: '所属服务商', key: 'providerName', width: 180 },
  { title: '身份证号', key: 'idCard', width: 170 },
  { title: '紧急联系人', key: 'emergencyContact', width: 100 },
  { title: '紧急联系电话', key: 'emergencyPhone', width: 130 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: row =>
      h(NTag, { type: getStatusType(row.status), size: 'small' }, () => getStatusLabel(row.status))
  },
  { title: '入职日期', key: 'hireDate', width: 110 },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: row =>
      h(NSpace, { size: 'small' }, () => [
        h(NButton, { size: 'small', onClick: () => handleEdit(row) }, () => '编辑'),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.staffId) }, () => '删除')
      ])
  }
];

// Modal
const modalVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const editingData = ref<Api.Staff.Staff | null>(null);

const form = ref({
  staffName: '',
  gender: 0,
  idCard: '',
  phone: '',
  providerId: '',
  emergencyContact: '',
  emergencyPhone: '',
  remark: '',
  status: 'ON_JOB'
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
    if (searchName.value) params.staffName = searchName.value;
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
    staffName: '',
    gender: 0,
    idCard: '',
    phone: '',
    providerId: '',
    emergencyContact: '',
    emergencyPhone: '',
    remark: '',
    status: 'ON_JOB'
  };
  modalVisible.value = true;
}

function handleEdit(row: Api.Staff.Staff) {
  operateType.value = 'edit';
  editingData.value = row;
  form.value = {
    staffName: row.staffName || '',
    gender: row.gender ?? 0,
    idCard: row.idCard || '',
    phone: row.phone || '',
    providerId: row.providerId || '',
    emergencyContact: row.emergencyContact || '',
    emergencyPhone: row.emergencyPhone || '',
    remark: row.remark || '',
    status: row.status || 'ON_JOB'
  };
  modalVisible.value = true;
}

async function handleDelete(staffId: string) {
  try {
    await fetchDeleteStaff(staffId);
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
      await fetchUpdateStaff(editingData.value.staffId, form.value);
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
      <div class="statistics-grid">
        <div class="stat-card stat-primary">
          <div class="stat-label">总人数</div>
          <div class="stat-value">{{ statistics.total }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">在职</div>
          <div class="stat-value">{{ statistics.active }}</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-label">待上岗</div>
          <div class="stat-value">{{ statistics.pending }}</div>
        </div>
        <div class="stat-card stat-error">
          <div class="stat-label">已离职</div>
          <div class="stat-value">{{ statistics.inactive }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">平均评分</div>
          <div class="stat-value">{{ Number(statistics.avgRating || 0).toFixed(1) }}</div>
        </div>
      </div>
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
        :row-key="(row: Api.Staff.Staff) => row.staffId"
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
          <NInput v-model:value="form.staffName" placeholder="请输入姓名" />
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
        <NFormItem label="服务商ID">
          <NInput v-model:value="form.providerId" placeholder="请输入服务商ID" />
        </NFormItem>
        <NFormItem label="紧急联系人">
          <NInput v-model:value="form.emergencyContact" placeholder="请输入紧急联系人" />
        </NFormItem>
        <NFormItem label="紧急联系电话">
          <NInput v-model:value="form.emergencyPhone" placeholder="请输入紧急联系电话" />
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
  </div>
</template>

<style scoped>
.statistics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 16px;
  border-radius: 8px;
  text-align: center;
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  line-height: 1.2;
}

.stat-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-primary .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-info {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-info .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-success {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.stat-success .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-warning {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-warning .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-error {
  background: linear-gradient(135deg, #ff0844 0%, #ffb199 100%);
  color: white;
}

.stat-error .stat-label {
  color: rgba(255, 255, 255, 0.85);
}
</style>
