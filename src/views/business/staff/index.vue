<script setup lang="ts">
import { ref, h, onMounted, watch, computed } from 'vue';
import { NButton, NCard, NTag, NSpace, NInput, NSelect, useMessage } from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import { useNaiveForm, useFormRules } from '@/hooks/common/form';
import { useNaivePaginatedTable, useTableOperate, defaultTransform } from '@/hooks/common/table';
import { useAuth } from '@/hooks/business/auth';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';
import {
  fetchGetStaffList,
  fetchCreateStaff,
  fetchUpdateStaff,
  fetchDeleteStaff,
  fetchGetStaffStatistics,
  fetchGetProviderOptions
} from '@/service/api';

defineOptions({
  name: 'BusinessStaff'
});

const message = useMessage();
const { formRef, validate, restoreValidation } = useNaiveForm();
const { defaultRequiredRule } = useFormRules();
const { hasAuth } = useAuth();

// Form validation rules
const rules = {
  staffName: [defaultRequiredRule],
  phone: [defaultRequiredRule],
  providerId: [defaultRequiredRule]
};

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

// Gender options
const genderOptions = [
  { label: '女', value: 0 },
  { label: '男', value: 1 }
];

// Status options
const statusOptions = [
  { label: '待上岗', value: 'PENDING' },
  { label: '在职', value: 'ON_JOB' },
  { label: '离职', value: 'OFF_JOB' }
];

// Provider options for dropdown
const providerOptions = ref<{ label: string; value: string }[]>([]);

// Service category options
const categoryOptions = [
  { label: '养老服务', value: 'ELDER_CARE' },
  { label: '家政服务', value: 'HOME_CARE' }
];

function getGenderLabel(gender?: number): string {
  if (gender === 0) return '女';
  if (gender === 1) return '男';
  return '未知';
}

function getStatusType(status?: string): 'success' | 'warning' | 'error' | 'default' {
  if (status === 'PENDING') return 'warning';
  if (status === 'ON_JOB') return 'success';
  if (status === 'OFF_JOB') return 'error';
  return 'default';
}

function getStatusLabel(status?: string): string {
  if (status === 'PENDING') return '待上岗';
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
    render: row => h(NTag, { type: getStatusType(row.status), size: 'small' }, () => getStatusLabel(row.status))
  },
  { title: '入职日期', key: 'hireDate', width: 110 },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: row => {
      const buttons = [];
      if (hasAuth('staff:list:edit')) {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleEdit(row.staffId) }, () => '编辑'));
      }
      if (hasAuth('staff:list:delete')) {
        buttons.push(h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.staffId) }, () => '删除'));
      }
      return h(NSpace, { size: 'small' }, () => buttons);
    }
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
} = useNaivePaginatedTable<Api.Common.PaginatingQueryRecord<Api.Staff.Staff>, Api.Staff.Staff>({
  apiFn: async params => {
    const queryParams: any = {
      page: params.page,
      pageSize: params.pageSize
    };
    if (searchName.value) queryParams.staffName = searchName.value;
    if (searchPhone.value) queryParams.phone = searchPhone.value;
    if (searchServiceCategory.value) queryParams.serviceType = searchServiceCategory.value;
    if (searchStatus.value) queryParams.status = searchStatus.value;
    return fetchGetStaffList(queryParams);
  },
  apiParams: {
    page: 1,
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
} = useTableOperate(tableData, 'staffId', getData);

const form = ref({
  staffName: '',
  gender: 0 as 0 | 1,
  idCard: '',
  phone: '',
  providerId: '',
  emergencyContact: '',
  emergencyPhone: '',
  remark: '',
  status: 'PENDING' as 'PENDING' | 'ON_JOB' | 'OFF_JOB'
});

// Reset form to empty state
function resetForm() {
  form.value = {
    staffName: '',
    gender: 0,
    idCard: '',
    phone: '',
    providerId: '',
    emergencyContact: '',
    emergencyPhone: '',
    remark: '',
    status: 'PENDING'
  };
}

// Watch editingData to fill form when editing
watch(
  () => editingData.value,
  data => {
    if (data) {
      form.value = {
        staffName: data.staffName || '',
        gender: data.gender ?? 0,
        idCard: data.idCard || '',
        phone: data.phone || '',
        providerId: data.providerId || '',
        emergencyContact: data.emergencyContact || '',
        emergencyPhone: data.emergencyPhone || '',
        remark: data.remark || '',
        status: (data.status as 'PENDING' | 'ON_JOB' | 'OFF_JOB') || 'PENDING'
      };
    } else {
      resetForm();
    }
  },
  { immediate: true }
);

// Calculate age from ID card number (18-digit Chinese ID card)
function calculateAge(idCard: string): number {
  if (!idCard || idCard.length !== 18) return 0;
  const birthYear = parseInt(idCard.substring(6, 10));
  const birthMonth = parseInt(idCard.substring(10, 12));
  const birthDay = parseInt(idCard.substring(12, 14));
  const now = new Date();
  const birthDate = new Date(birthYear, birthMonth - 1, birthDay);
  let age = now.getFullYear() - birthYear;
  const monthDiff = now.getMonth() - birthDate.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < birthDate.getDate())) {
    age--;
  }
  return age;
}

// Display age computed from ID card
const displayAge = computed(() => {
  if (!form.value.idCard || form.value.idCard.length !== 18) return '';
  return calculateAge(form.value.idCard);
});

// Watch idCard changes to auto-fill gender
watch(
  () => form.value.idCard,
  idCard => {
    if (idCard && idCard.length === 18) {
      // Extract gender from digit 17 (index 16): odd=male(1), even=female(0)
      const genderDigit = parseInt(idCard.charAt(16));
      form.value.gender = genderDigit % 2 === 0 ? 0 : 1;
    }
  }
);

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

async function handleDelete(staffId: string) {
  try {
    await fetchDeleteStaff(staffId);
    message.success('删除成功');
    await getData();
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
      await fetchCreateStaff(form.value);
      message.success('添加成功');
    } else if (editingData.value) {
      await fetchUpdateStaff(editingData.value.staffId, form.value);
      message.success('修改成功');
    }
    closeDrawer();
    await getData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to submit', e);
  }
}

function handleResetSearch() {
  searchName.value = '';
  searchPhone.value = '';
  searchServiceCategory.value = '';
  searchStatus.value = '';
  getDataByPage(1);
}

async function getProviderOptions() {
  try {
    const data = await fetchGetProviderOptions();
    if (data) {
      providerOptions.value = data.map((p: { id: string; name: string }) => ({
        label: p.name,
        value: p.id
      }));
    }
  } catch (e) {
    console.error('Failed to get provider options', e);
  }
}

onMounted(() => {
  getStatistics();
  getData();
  getProviderOptions();
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
    <NCard :bordered="false" style="margin-bottom: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>服务人员管理</span>
        </div>
      </template>
      <div style="background: #f5f5f5; padding: 12px; margin-bottom: 12px; border-radius: 4px">
        <NSpace :wrap="true" align="center">
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
          <NButton type="primary" @click="getData">搜索</NButton>
          <NButton @click="handleResetSearch">重置</NButton>
        </NSpace>
      </div>

      <!-- Use framework's TableHeaderOperation component -->
      <TableHeaderOperation
        v-model:columns="columnChecks"
        :disabled-delete="checkedRowKeys.length === 0"
        :loading="loading"
        @refresh="getData"
      >
        <template #default>
          <NButton v-if="hasAuth('staff:list:add')" size="small" ghost type="primary" @click="handleAdd">
            <template #icon>
              <icon-ic-round-plus class="text-icon" />
            </template>
            {{ $t('common.add') }}
          </NButton>
        </template>
      </TableHeaderOperation>

      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1400"
        :row-key="(row: Api.Staff.Staff) => row.staffId"
        remote
        :pagination="mobilePagination"
      />
    </NCard>

    <!-- Staff Drawer -->
    <NDrawer v-model:show="drawerVisible" :width="500" placement="right" closable>
      <NDrawerContent :title="operateType === 'add' ? '新增服务人员' : '编辑服务人员'" closable>
        <NForm ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100">
          <NFormItem label="姓名" path="staffName">
            <NInput v-model:value="form.staffName" placeholder="请输入姓名" />
          </NFormItem>
          <NFormItem label="身份证号">
            <NInput v-model:value="form.idCard" placeholder="请输入身份证号，填写后自动计算年龄和性别" />
          </NFormItem>
          <NFormItem label="年龄">
            <NInput :value="displayAge ? String(displayAge) : ''" disabled placeholder="自动计算" />
          </NFormItem>
          <NFormItem label="性别">
            <NSelect v-model:value="form.gender" :options="genderOptions" style="width: 120px" />
          </NFormItem>
          <NFormItem label="手机号" path="phone">
            <NInput v-model:value="form.phone" placeholder="请输入手机号" />
          </NFormItem>
          <NFormItem label="服务商" path="providerId">
            <NSelect
              v-model:value="form.providerId"
              :options="providerOptions"
              placeholder="请选择服务商"
              style="width: 100%"
            />
          </NFormItem>
          <NFormItem label="紧急联系人">
            <NInput v-model:value="form.emergencyContact" placeholder="请输入紧急联系人" />
          </NFormItem>
          <NFormItem label="紧急联系电话">
            <NInput v-model:value="form.emergencyPhone" placeholder="请输入紧急联系电话" />
          </NFormItem>
          <NFormItem label="状态">
            <NSelect v-model:value="form.status" :options="statusOptions" style="width: 150px" />
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
