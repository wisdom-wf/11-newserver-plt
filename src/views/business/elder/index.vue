<script setup lang="ts">
import { ref, h, onMounted } from 'vue';
import { NButton, NCard, NTag, NSpace, NInput, NSelect, useMessage } from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import { useFormRules } from '@/hooks/common/form';
import {
  fetchGetElderList,
  fetchCreateElder,
  fetchUpdateElder,
  fetchDeleteElder,
  fetchGetElderStatistics
} from '@/service/api';
import { useNaivePaginatedTable, useTableOperate, defaultTransform } from '@/hooks/common/table';
import { useNaiveForm } from '@/hooks/common/form';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';

defineOptions({
  name: 'BusinessElder'
});

const message = useMessage();
const { patternRules, createRequiredRule } = useFormRules();
const { formRef, validate, restoreValidation } = useNaiveForm();

// Form validation rules
const rules = {
  name: [createRequiredRule('请输入姓名')],
  idCard: [createRequiredRule('请输入身份证号')],
  phone: [
    createRequiredRule('请输入手机号'),
    { pattern: patternRules.phone.pattern, message: patternRules.phone.message, trigger: 'change' }
  ]
};

// Statistics
const statistics = ref<Api.Elder.Statistics>({
  total: 0,
  registered: 0,
  suspended: 0,
  careTypeStats: { HOME: 0, COMMUNITY: 0, INSTITUTION: 0 },
  careLevelStats: { HIGH: 0, MEDIUM: 0, NORMAL: 0 },
  subsidyTypeStats: { FULL_SUBSIDY: 0, PARTIAL_SUBSIDY: 0, SELF_PAY: 0 }
});

// Search
const searchName = ref('');
const searchIdCard = ref('');
const searchPhone = ref('');
const searchCareType = ref('');
const searchStatus = ref('');

// Gender options
const genderOptions = [
  { label: '男', value: 'MALE' },
  { label: '女', value: 'FEMALE' },
  { label: '未知', value: 'UNKNOWN' }
];

// Care type options
const careTypeOptions = [
  { label: '居家养老', value: 'HOME' },
  { label: '社区养老', value: 'COMMUNITY' },
  { label: '机构养老', value: 'INSTITUTION' }
];

// Subsidy type options
const subsidyTypeOptions = [
  { label: '全额补贴', value: 'FULL_SUBSIDY' },
  { label: '部分补贴', value: 'PARTIAL_SUBSIDY' },
  { label: '自费', value: 'SELF_PAY' }
];

// Care level options
const careLevelOptions = [
  { label: '一级护理', value: 'HIGH' },
  { label: '二级护理', value: 'MEDIUM' },
  { label: '三级护理', value: 'NORMAL' }
];

// Status options
const statusOptions = [
  { label: '启用', value: 'ACTIVE' },
  { label: '禁用', value: 'SUSPENDED' }
];

function getGenderLabel(gender?: string): string {
  const option = genderOptions.find(o => o.value === gender);
  return option?.label || gender || '';
}

function getCareTypeLabel(careType?: string): string {
  const option = careTypeOptions.find(o => o.value === careType);
  return option?.label || careType || '';
}

function getSubsidyTypeLabel(subsidyType?: string): string {
  const option = subsidyTypeOptions.find(o => o.value === subsidyType);
  return option?.label || subsidyType || '';
}

function getCareLevelLabel(level?: string): string {
  const option = careLevelOptions.find(o => o.value === level);
  return option?.label || level || '';
}

// Table columns
const columns: DataTableColumns<Api.Elder.Elder> = [
  { title: '姓名', key: 'name', width: 100 },
  { title: '性别', key: 'gender', width: 60, render: row => getGenderLabel(row.gender) },
  { title: '年龄', key: 'age', width: 60 },
  { title: '身份证号', key: 'idCard', width: 180 },
  { title: '手机号', key: 'phone', width: 130 },
  { title: '区域', key: 'areaName', width: 120 },
  { title: '养老类型', key: 'careType', width: 100, render: row => getCareTypeLabel(row.careType) },
  { title: '补贴类型', key: 'subsidyType', width: 100, render: row => getSubsidyTypeLabel(row.subsidyType) },
  { title: '护理等级', key: 'careLevel', width: 100, render: row => getCareLevelLabel(row.careLevel) },
  { title: '服务商', key: 'providerName', width: 150 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: row =>
      h(NTag, { type: row.status === 'ACTIVE' ? 'success' : 'error', size: 'small' }, () =>
        row.status === 'ACTIVE' ? '正常' : '禁用'
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
        h(NButton, { size: 'small', onClick: () => handleOpenEdit(row.elderId) }, () => '编辑'),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.elderId) }, () => '删除')
      ])
  }
];

// Use framework's table hook
const {
  data: tableData,
  loading,
  pagination,
  mobilePagination,
  getData,
  getDataByPage,
  columnChecks
} = useNaivePaginatedTable<Api.Common.PaginatingQueryRecord<Api.Elder.Elder>, Api.Elder.Elder>({
  apiFn: async params => {
    const queryParams: Api.Elder.ElderQuery & Api.Common.PaginatingQueryParams = {
      current: params.page,
      pageSize: params.pageSize
    };
    if (searchName.value) queryParams.name = searchName.value;
    if (searchIdCard.value) queryParams.idCard = searchIdCard.value;
    if (searchPhone.value) queryParams.phone = searchPhone.value;
    if (searchCareType.value) queryParams.careType = searchCareType.value;
    if (searchStatus.value) queryParams.status = searchStatus.value;
    return fetchGetElderList(queryParams);
  },
  apiParams: {
    current: 1,
    pageSize: 10
  },
  transform: defaultTransform,
  columns: () => columns
});

// Use framework's table operate hook
const {
  drawerVisible,
  operateType,
  handleAdd,
  editingData,
  handleEdit,
  checkedRowKeys,
  onBatchDeleted,
  onDeleted,
  closeDrawer
} = useTableOperate(tableData, 'elderId', getData);

// Form data
const form = ref({
  name: '',
  gender: 'UNKNOWN' as Api.Elder.Gender,
  idCard: '',
  phone: '',
  birthDate: '',
  age: 0,
  careType: 'HOME' as Api.Elder.CareType,
  subsidyType: 'SELF_PAY' as Api.Elder.SubsidyType,
  careLevel: 'NORMAL' as Api.Elder.CareLevel,
  address: '',
  emergencyContact: '',
  emergencyPhone: '',
  healthStatus: 'GOOD' as Api.Elder.HealthStatus,
  remark: '',
  status: '1' as Api.Common.EnableStatus
});

async function getStatistics() {
  try {
    const { data } = await fetchGetElderStatistics();
    if (data) {
      statistics.value = data;
    }
  } catch (e) {
    console.error('Failed to get statistics', e);
  }
}

function resetForm() {
  form.value = {
    name: '',
    gender: 'UNKNOWN',
    idCard: '',
    phone: '',
    birthDate: '',
    age: 0,
    careType: 'HOME',
    subsidyType: 'SELF_PAY',
    careLevel: 'LEVEL_3',
    address: '',
    emergencyContact: '',
    emergencyPhone: '',
    healthStatus: 'GOOD',
    remark: '',
    status: 'ENABLED' as Api.Common.EnableStatus
  };
}

function handleOpenAdd() {
  resetForm();
  handleAdd();
}

async function handleOpenEdit(id: string) {
  const row = tableData.value.find(item => String(item.elderId) === String(id));
  if (row) {
    form.value = {
      name: row.name,
      gender: (row.gender || 'UNKNOWN') as Api.Elder.Gender,
      idCard: row.idCard,
      phone: row.phone || '',
      birthDate: row.birthDate || '',
      age: row.age || 0,
      careType: (row.careType || 'HOME') as Api.Elder.CareType,
      subsidyType: (row.subsidyType || 'SELF_PAY') as Api.Elder.SubsidyType,
      careLevel: (row.careLevel || 'NORMAL') as Api.Elder.CareLevel,
      address: row.address || '',
      emergencyContact: row.emergencyContact || '',
      emergencyPhone: row.emergencyPhone || '',
      healthStatus: (row.healthStatus || 'GOOD') as Api.Elder.HealthStatus,
      remark: row.remark || '',
      status: (row.status || 'ENABLED') as Api.Common.EnableStatus
    };
    handleEdit(id);
  }
}

async function handleDelete(id: string) {
  try {
    await fetchDeleteElder(id);
    message.success('删除成功');
    await onDeleted();
    await getStatistics();
  } catch (e) {
    console.error('Failed to delete', e);
  }
}

async function handleSubmit() {
  try {
    await validate();
  } catch {
    return;
  }
  try {
    if (operateType.value === 'add') {
      await fetchCreateElder(form.value);
      message.success('添加成功');
    } else if (editingData.value) {
      await fetchUpdateElder(editingData.value.elderId, form.value);
      message.success('修改成功');
    }
    closeDrawer();
    await getData();
    await getStatistics();
    restoreValidation();
  } catch (e) {
    console.error('Failed to submit', e);
  }
}

function handleResetSearch() {
  searchName.value = '';
  searchIdCard.value = '';
  searchPhone.value = '';
  searchCareType.value = '';
  searchStatus.value = '';
  getDataByPage(1);
}

onMounted(() => {
  getStatistics();
  getData();
});
</script>

<template>
  <div>
    <!-- Statistics Cards -->
    <NCard title="老人统计" :bordered="false" style="margin-bottom: 16px">
      <div class="statistics-grid">
        <div class="stat-card stat-primary">
          <div class="stat-label">总人数</div>
          <div class="stat-value">{{ statistics.total }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">在册</div>
          <div class="stat-value">{{ statistics.registered }}</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-label">暂停服务</div>
          <div class="stat-value">{{ statistics.suspended }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">居家养老</div>
          <div class="stat-value">{{ statistics.careTypeStats.HOME || 0 }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">社区养老</div>
          <div class="stat-value">{{ statistics.careTypeStats.COMMUNITY || 0 }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">机构养老</div>
          <div class="stat-value">{{ statistics.careTypeStats.INSTITUTION || 0 }}</div>
        </div>
      </div>
    </NCard>

    <!-- Table -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>老人档案管理</span>
        </div>
      </template>
      <div style="background: #f5f5f5; padding: 12px; margin-bottom: 12px; border-radius: 4px">
        <NSpace :wrap="true" align="center">
          <NInput v-model:value="searchName" placeholder="姓名" clearable style="width: 100px" />
          <NInput v-model:value="searchIdCard" placeholder="身份证号" clearable style="width: 180px" />
          <NInput v-model:value="searchPhone" placeholder="手机号" clearable style="width: 130px" />
          <NSelect
            v-model:value="searchCareType"
            :options="careTypeOptions"
            placeholder="养老类型"
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
          <NButton type="primary" @click="getData">搜索</NButton>
          <NButton @click="handleResetSearch">重置</NButton>
        </NSpace>
      </div>

      <!-- Use framework's TableHeaderOperation component -->
      <TableHeaderOperation
        v-model:columns="columnChecks"
        :disabled-delete="checkedRowKeys.length === 0"
        :loading="loading"
        @add="handleOpenAdd"
        @refresh="getData"
      />

      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1500"
        :row-key="(row: Api.Elder.Elder) => row.elderId"
        v-model:checked-row-keys="checkedRowKeys"
        remote
        :pagination="mobilePagination"
      />
    </NCard>

    <!-- Elder Drawer -->
    <NDrawer v-model:show="drawerVisible" :width="600" placement="right" closable>
      <NDrawerContent :title="operateType === 'add' ? '新增老人' : '编辑老人'" closable>
        <NForm ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100">
          <NFormItem label="姓名" path="name">
            <NInput v-model:value="form.name" placeholder="请输入姓名" />
          </NFormItem>
          <NFormItem label="性别">
            <NSelect v-model:value="form.gender" :options="genderOptions" style="width: 120px" />
          </NFormItem>
          <NFormItem label="身份证号" path="idCard">
            <NInput v-model:value="form.idCard" placeholder="请输入身份证号" />
          </NFormItem>
          <NFormItem label="手机号" path="phone">
            <NInput v-model:value="form.phone" placeholder="请输入手机号" />
          </NFormItem>
          <NFormItem label="养老类型">
            <NSelect v-model:value="form.careType" :options="careTypeOptions" style="width: 150px" />
          </NFormItem>
          <NFormItem label="补贴类型">
            <NSelect v-model:value="form.subsidyType" :options="subsidyTypeOptions" style="width: 150px" />
          </NFormItem>
          <NFormItem label="护理等级">
            <NSelect v-model:value="form.careLevel" :options="careLevelOptions" style="width: 150px" />
          </NFormItem>
          <NFormItem label="地址">
            <NInput v-model:value="form.address" placeholder="请输入地址" />
          </NFormItem>
          <NFormItem label="紧急联系人">
            <NInput v-model:value="form.emergencyContact" placeholder="请输入紧急联系人" />
          </NFormItem>
          <NFormItem label="紧急联系电话">
            <NInput v-model:value="form.emergencyPhone" placeholder="请输入紧急联系电话" />
          </NFormItem>
          <NFormItem label="状态">
            <NSelect v-model:value="form.status" :options="statusOptions" style="width: 100px" />
          </NFormItem>
          <NFormItem label="备注">
            <NInput v-model:value="form.remark" type="textarea" placeholder="请输入备注" />
          </NFormItem>
        </NForm>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="closeDrawer">取消</NButton>
            <NButton type="primary" @click="handleSubmit">确认</NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>
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
