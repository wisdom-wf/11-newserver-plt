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
  fetchGetElderList,
  fetchCreateElder,
  fetchUpdateElder,
  fetchDeleteElder,
  fetchGetElderStatistics
} from '@/service/api';

defineOptions({
  name: 'BusinessElder'
});

const message = useMessage();

// Statistics
const statistics = ref<Api.Elder.Statistics>({
  total: 0,
  registered: 0,
  suspended: 0,
  careTypeStats: { HOME: 0, COMMUNITY: 0, INSTITUTION: 0 },
  careLevelStats: { LEVEL_1: 0, LEVEL_2: 0, LEVEL_3: 0, LEVEL_4: 0, LEVEL_5: 0 },
  subsidyTypeStats: { FULL_SUBSIDY: 0, PARTIAL_SUBSIDY: 0, SELF_PAY: 0 }
});

// Search
const searchName = ref('');
const searchIdCard = ref('');
const searchPhone = ref('');
const searchCareType = ref('');
const searchStatus = ref('');

// Table data
const loading = ref(false);
const tableData = ref<Api.Elder.Elder[]>([]);
const pagination = ref({ page: 1, pageSize: 10, total: 0 });

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
  { label: '一级护理', value: 'LEVEL_1' },
  { label: '二级护理', value: 'LEVEL_2' },
  { label: '三级护理', value: 'LEVEL_3' },
  { label: '四级护理', value: 'LEVEL_4' },
  { label: '五级护理', value: 'LEVEL_5' }
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
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.elderId) }, () => '删除')
      ])
  }
];

// Modal
const modalVisible = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const editingData = ref<Api.Elder.Elder | null>(null);

const form = ref({
  name: '',
  gender: 'UNKNOWN' as Api.Elder.Gender,
  idCard: '',
  phone: '',
  birthDate: '',
  age: 0,
  careType: 'HOME' as Api.Elder.CareType,
  subsidyType: 'SELF_PAY' as Api.Elder.SubsidyType,
  careLevel: 'LEVEL_3' as Api.Elder.CareLevel,
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

async function getTableData() {
  loading.value = true;
  try {
    const params: Api.Elder.ElderQuery & Api.Common.PaginatingQueryParams = {
      current: pagination.value.page,
      pageSize: pagination.value.pageSize
    };
    if (searchName.value) params.name = searchName.value;
    if (searchIdCard.value) params.idCard = searchIdCard.value;
    if (searchPhone.value) params.phone = searchPhone.value;
    if (searchCareType.value) params.careType = searchCareType.value;
    if (searchStatus.value) params.status = searchStatus.value;

    const { data } = await fetchGetElderList(params);
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
    status: '1'
  };
  modalVisible.value = true;
}

function handleEdit(row: Api.Elder.Elder) {
  operateType.value = 'edit';
  editingData.value = row;
  form.value = {
    name: row.name,
    gender: row.gender || 'UNKNOWN',
    idCard: row.idCard,
    phone: row.phone || '',
    birthDate: row.birthDate || '',
    age: row.age || 0,
    careType: row.careType || 'HOME',
    subsidyType: row.subsidyType || 'SELF_PAY',
    careLevel: row.careLevel || 'LEVEL_3',
    address: row.address || '',
    emergencyContact: row.emergencyContact || '',
    emergencyPhone: row.emergencyPhone || '',
    healthStatus: row.healthStatus || 'GOOD',
    remark: row.remark || '',
    status: row.status || '1'
  };
  modalVisible.value = true;
}

async function handleDelete(id: string) {
  try {
    await fetchDeleteElder(id);
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
      await fetchCreateElder(form.value);
      message.success('添加成功');
    } else if (editingData.value) {
      await fetchUpdateElder(editingData.value.elderId, form.value);
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
    <NCard title="老人档案管理" :bordered="false">
      <template #header>
        <NSpace :wrap="true">
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
          <NButton type="primary" @click="getTableData">搜索</NButton>
          <NButton type="primary" @click="handleAdd">新增</NButton>
        </NSpace>
      </template>
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1500"
        :row-key="(row: Api.Elder.Elder) => row.id"
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

    <!-- Elder Modal -->
    <NModal
      v-model:show="modalVisible"
      :title="operateType === 'add' ? '新增老人' : '编辑老人'"
      preset="card"
      style="width: 600px"
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
