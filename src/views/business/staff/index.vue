<script setup lang="ts">
import { ref, h, onMounted, watch, computed } from 'vue';
import { NButton, NCard, NTag, NSpace, NInput, NSelect, NDrawer, NDrawerContent, useMessage, NImage, NUpload, NGrid, NGi, NPopconfirm, NTabs, NTabPane, NAvatar, NRate, NEmpty, NSpin, NModal, NAlert, NDescriptions, NDescriptionsItem } from 'naive-ui';
import PersonCard from '@/components/common/person-card.vue';
import type { DataTableColumns } from 'naive-ui';
import { useNaiveForm, useFormRules } from '@/hooks/common/form';
import { useNaivePaginatedTable, useTableOperate, defaultTransform } from '@/hooks/common/table';
import { useAuth } from '@/hooks/business/auth';
import { useRouterPush } from '@/hooks/common/router';
import { useRoute, useRouter } from 'vue-router';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';
import {
  fetchGetStaffList,
  fetchCreateStaff,
  fetchUpdateStaff,
  fetchDeleteStaff,
  fetchBatchDeleteStaff,
  fetchGetStaffStatistics,
  fetchGetProviderOptions,
  fetchGetStaff,
  fetchGetStaffServiceLogs,
  fetchResetStaffPassword
} from '@/service/api';

defineOptions({
  name: 'BusinessStaff'
});

const message = useMessage();
const { formRef, validate, restoreValidation } = useNaiveForm();
const { defaultRequiredRule } = useFormRules();
const { hasAuth } = useAuth();
const { routerPushByKeyWithMetaQuery } = useRouterPush();
const route = useRoute();
const router = useRouter();

// Detail drawer state
const detailVisible = ref(false);
const detailData = ref<Api.Staff.Staff | null>(null);
const detailLoading = ref(false);
const MAX_IMAGE_COUNT = 6;

// View type (list or card)
const viewType = ref<'list' | 'card'>('list');

// Service logs for detail drawer
const serviceLogs = ref<Api.Staff.StaffServiceLog[]>([]);
const serviceLogsLoading = ref(false);
const detailActiveTab = ref('basic');

async function showDetail(row: Api.Staff.Staff) {
  detailLoading.value = true;
  try {
    const { data, error } = await fetchGetStaff(row.staffId);
    if (error) {
      message.error(error.message || '获取详情失败');
      return;
    }
    if (data) {
      detailData.value = data;
      detailVisible.value = true;
      detailActiveTab.value = 'basic';
      // Load service logs
      loadServiceLogs(row.staffId);
    }
  } finally {
    detailLoading.value = false;
  }
}

async function loadServiceLogs(staffId: string) {
  serviceLogsLoading.value = true;
  try {
    const { data, error } = await fetchGetStaffServiceLogs(staffId, 20);
    if (error) {
      console.error('Failed to load service logs', error);
      return;
    }
    serviceLogs.value = data || [];
  } catch (e) {
    console.error('Failed to load service logs', e);
  } finally {
    serviceLogsLoading.value = false;
  }
}

function parsePhotos(photos: string | string[] | undefined): string[] {
  if (!photos) return [];
  if (Array.isArray(photos)) return photos;
  if (typeof photos === 'string') {
    try {
      return JSON.parse(photos);
    } catch {
      return photos.split(',').filter(Boolean);
    }
  }
  return [];
}

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

// 重置密码
async function resetPassword() {
  if (!detailData.value?.staffId) return;
  try {
    await fetchResetStaffPassword(detailData.value.staffId);
    message.success('密码已重置为 mima123');
  } catch (e) {
    console.error('Failed to reset password', e);
  }
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
  { type: 'selection' },
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
    width: 200,
    fixed: 'right',
    render: row => {
      const buttons = [];
      buttons.push(h(NButton, { size: 'small', type: 'info', onClick: () => showDetail(row), style: { marginRight: '8px' } }, () => '详情'));
      if (hasAuth('staff:list:edit')) {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleEdit(row.staffId), style: { marginRight: '8px' } }, () => '编辑'));
      }
      if (hasAuth('staff:list:delete')) {
        buttons.push(
          h(
            NPopconfirm,
            {
              onPositiveClick: () => handleDelete(row.staffId)
            },
            {
              trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
              default: () => '确认删除?'
            }
          )
        );
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

async function handleBatchDelete() {
  if (!checkedRowKeys.value.length) return;
  try {
    await fetchBatchDeleteStaff(checkedRowKeys.value);
    message.success('批量删除成功');
    await getData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to batch delete', e);
  }
}

// 照片上传处理
async function handlePhotoUpload(staffId: string, file: File) {
  const reader = new FileReader();
  reader.onload = async (e) => {
    if (e.target?.result) {
      const base64Data = e.target.result as string;
      try {
        const { error } = await fetchUpdateStaff(staffId, { avatarUrl: base64Data } as any);
        if (error) {
          message.error(error.message || '上传失败');
          return;
        }
        message.success('上传成功');
        await getData();
        await getStatistics();
      } catch (err) {
        message.error('上传失败');
      }
    }
  };
  reader.readAsDataURL(file);
}

// 跳转到服务日志页面
function handleViewServiceLogs(staff: Api.Staff.Staff) {
  routerPushByKeyWithMetaQuery('business_service-log', { staffId: staff.staffId });
}

// 账户信息弹窗
const accountModalVisible = ref(false);
const accountInfo = ref<{ username: string; password: string; staffName: string } | null>(null);

async function handleSubmit() {
  try {
    await validate();
  } catch {
    return;
  }
  try {
    if (operateType.value === 'add') {
      const result = await fetchCreateStaff(form.value);
      message.success('添加成功');
      // 如果创建了账户，显示账户信息
      if (result?.accountCreated && result.username && result.password) {
        accountInfo.value = {
          username: result.username,
          password: result.password,
          staffName: form.value.staffName || ''
        };
        accountModalVisible.value = true;
      }
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

// 复制账户信息
async function copyAccountInfo() {
  if (accountInfo.value) {
    const text = `用户名: ${accountInfo.value.username}\n密码: ${accountInfo.value.password}`;
    await navigator.clipboard.writeText(text);
    message.success('已复制到剪贴板');
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

onMounted(async () => {
  // 接收服务人员详情跳转参数（订单详情→服务人员详情），直接打开该服务人员详情抽屉
  if (route.query.staffId) {
    const staffId = String(route.query.staffId);
    try {
      const { data } = await fetchGetStaff(staffId);
      if (data) {
        detailData.value = data;
        detailVisible.value = true;
        detailActiveTab.value = 'basic';
        loadServiceLogs(staffId);
      }
    } catch (e) {
      console.error('Failed to load staff detail from route', e);
    }
  }
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
        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%">
          <span>服务人员管理</span>
          <NSpace>
            <NButton
              :type="viewType === 'list' ? 'primary' : 'default'"
              size="small"
              quaternary
              @click="viewType = 'list'"
            >
              列表
            </NButton>
            <NButton
              :type="viewType === 'card' ? 'primary' : 'default'"
              size="small"
              quaternary
              @click="viewType = 'card'"
            >
              卡片
            </NButton>
          </NSpace>
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
        @delete="handleBatchDelete"
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

      <!-- List View -->
      <NDataTable
        v-if="viewType === 'list'"
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1400"
        :row-key="(row: Api.Staff.Staff) => row.staffId"
        v-model:checked-row-keys="checkedRowKeys"
        remote
        :pagination="mobilePagination"
      />

      <!-- Card View - 使用PersonCard组件 -->
      <div v-else style="display: flex; flex-wrap: wrap; gap: 12px">
        <PersonCard
          v-for="staff in tableData"
          :key="staff.staffId"
          :photo-url="staff.avatarUrl"
          :name="staff.staffName || '未知'"
          :subtitle="staff.phone || '-'"
          :extra-info="[
            { label: '状态', value: getStatusLabel(staff.status) },
            ...(staff.serviceTypes ? [{ label: '服务类型', value: staff.serviceTypes.split(',')[0] }] : [])
          ]"
          :index-value="staff.rating"
          index-label="评分"
          photo-width="70"
          scale="0.78"
          :show-upload-btn="true"
          @click="showDetail(staff)"
          @photo-upload="(file) => handlePhotoUpload(staff.staffId, file)"
        />
        <NEmpty v-if="tableData.length === 0" description="暂无数据" style="width: 100%; margin-top: 40px" />
      </div>
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

    <!-- Staff Detail Drawer -->
    <NDrawer v-model:show="detailVisible" :width="550" placement="right" closable>
      <NDrawerContent title="服务人员详情" closable>
        <NTabs v-model:value="detailActiveTab" type="line" style="margin-top: 8px">
          <NTabPane name="basic" tab="基本信息">
            <div style="max-height: calc(100vh - 220px); overflow-y: auto">
              <!-- Avatar and Basic Info -->
              <div style="display: flex; gap: 16px; margin-bottom: 24px; align-items: flex-start">
                <div style="width: 80px; height: 80px; border-radius: 8px; overflow: hidden; flex-shrink: 0">
                  <NImage v-if="detailData.avatarUrl" :src="detailData.avatarUrl" width="80" height="80" object-fit="cover" />
                  <NAvatar v-else :size="80" round style="background: #5394ec; color: #fff; font-size: 24px">{{ detailData.staffName?.charAt(0) || '?' }}</NAvatar>
                </div>
                <div style="flex: 1">
                  <div style="font-size: 18px; font-weight: 600; margin-bottom: 4px">{{ detailData.staffName }}</div>
                  <div style="color: #666; font-size: 13px; margin-bottom: 8px">{{ detailData.phone || '-' }}</div>
                  <NTag :type="getStatusType(detailData.status)" size="small">{{ getStatusLabel(detailData.status) }}</NTag>
                </div>
              </div>

              <!-- Basic Info Grid -->
              <div style="margin-bottom: 24px">
                <NGrid :cols="2" :x-gap="16" :y-gap="8">
                  <NGi><div style="color: #999; font-size: 13px">工号</div><div style="margin-top: 4px">{{ detailData.staffNo || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">年龄</div><div style="margin-top: 4px">{{ detailData.age || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">性别</div><div style="margin-top: 4px">{{ getGenderLabel(detailData.gender) }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">身份证号</div><div style="margin-top: 4px">{{ detailData.idCard || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">服务类型</div><div style="margin-top: 4px">{{ detailData.serviceTypes || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">评分</div><div style="margin-top: 4px"><NRate v-if="detailData.rating" :value="detailData.rating" readonly size="small" /><span v-else>-</span></div></NGi>
                </NGrid>
              </div>

              <!-- Employment Info -->
              <div style="margin-bottom: 24px">
                <div style="font-size: 14px; font-weight: 600; margin-bottom: 12px; color: #333">任职信息</div>
                <NGrid :cols="2" :x-gap="16">
                  <NGi><div style="color: #999; font-size: 13px">所属服务商</div><div style="margin-top: 4px">{{ detailData.providerName || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">入职日期</div><div style="margin-top: 4px">{{ detailData.hireDate || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">接单数</div><div style="margin-top: 4px">{{ detailData.orderCount || 0 }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">状态</div><div style="margin-top: 4px">
                    <NTag :type="getStatusType(detailData.status)" size="small">{{ getStatusLabel(detailData.status) }}</NTag>
                  </div></NGi>
                </NGrid>
              </div>

              <!-- Account Info -->
              <div style="margin-bottom: 24px">
                <div style="font-size: 14px; font-weight: 600; margin-bottom: 12px; color: #333">系统账户</div>
                <NGrid :cols="2" :x-gap="16">
                  <NGi><div style="color: #999; font-size: 13px">用户名</div><div style="margin-top: 4px">
                    <NTag v-if="detailData.username" type="info">{{ detailData.username }}</NTag>
                    <span v-else>-</span>
                  </div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">账户状态</div><div style="margin-top: 4px">
                    <NTag v-if="detailData.hasAccount" type="success">已开通</NTag>
                    <NTag v-else type="default">未开通</NTag>
                  </div></NGi>
                </NGrid>
                <div v-if="detailData.username" style="margin-top: 12px">
                  <NButton size="small" type="warning" @click="resetPassword">重置密码</NButton>
                </div>
              </div>

              <!-- Emergency Contact -->
              <div style="margin-bottom: 24px">
                <div style="font-size: 14px; font-weight: 600; margin-bottom: 12px; color: #333">紧急联系人</div>
                <NGrid :cols="2" :x-gap="16">
                  <NGi><div style="color: #999; font-size: 13px">联系人</div><div style="margin-top: 4px">{{ detailData.emergencyContact || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">联系电话</div><div style="margin-top: 4px">{{ detailData.emergencyPhone || '-' }}</div></NGi>
                </NGrid>
              </div>

              <!-- Other Info -->
              <div style="margin-bottom: 24px">
                <div style="font-size: 14px; font-weight: 600; margin-bottom: 12px; color: #333">其他信息</div>
                <div style="color: #999; font-size: 13px">备注</div>
                <div style="margin-top: 4px">{{ detailData.remark || '-' }}</div>
                <div style="color: #999; font-size: 13px; margin-top: 8px">创建时间</div>
                <div style="margin-top: 4px">{{ detailData.createTime || '-' }}</div>
              </div>
            </div>
          </NTabPane>
          <NTabPane name="logs" tab="服务记录">
            <div style="max-height: calc(100vh - 220px); overflow-y: auto">
              <div v-if="serviceLogsLoading" style="text-align: center; padding: 40px 0">
                <NSpin size="medium" />
              </div>
              <div v-else-if="serviceLogs.length === 0" style="text-align: center; padding: 40px 0">
                <NEmpty description="暂无服务记录" />
              </div>
              <div v-else style="display: flex; flex-direction: column; gap: 12px">
                <div v-for="log in serviceLogs" :key="log.serviceLogId" style="background: #f8f8f8; border-radius: 8px; padding: 12px">
                  <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px">
                    <div style="font-weight: 600">{{ log.serviceCategory || log.serviceType || '-' }}</div>
                    <NTag :type="log.hasAnomaly ? 'error' : 'success'" size="small">{{ log.hasAnomaly ? '有异常' : '正常' }}</NTag>
                  </div>
                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 4px; font-size: 13px; color: #666">
                    <div>客户：{{ log.elderName || '-' }}</div>
                    <div>日期：{{ log.serviceDate || '-' }}</div>
                    <div>时长：{{ log.actualDuration ? log.actualDuration + '分钟' : '-' }}</div>
                    <div>评分：{{ log.serviceComment ? '⭐'.repeat(Math.min(5, parseInt(log.serviceComment))) : '-' }}</div>
                  </div>
                  <div v-if="log.serviceContent" style="margin-top: 8px; font-size: 13px; color: #666">{{ log.serviceContent }}</div>
                </div>
              </div>
            </div>
          </NTabPane>
        </NTabs>
      </NDrawerContent>
    </NDrawer>

    <!-- 账户信息弹窗 -->
    <NModal
      v-model:show="accountModalVisible"
      preset="card"
      title="系统账户信息"
      :style="{ width: '400px' }"
    >
      <NAlert type="warning" :bordered="false" style="margin-bottom: 16px">
        请将以下信息告知服务人员，建议首次登录后修改密码
      </NAlert>
      <NDescriptions label-placement="top">
        <NDescriptionsItem label="服务人员">
          {{ accountInfo?.staffName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="用户名">
          <NTag type="info">{{ accountInfo?.username }}</NTag>
        </NDescriptionsItem>
        <NDescriptionsItem label="密码">
          <NTag type="warning">{{ accountInfo?.password }}</NTag>
        </NDescriptionsItem>
      </NDescriptions>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="accountModalVisible = false">关闭</NButton>
          <NButton type="primary" @click="copyAccountInfo">复制信息</NButton>
        </NSpace>
      </template>
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
