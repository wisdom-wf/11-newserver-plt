<script setup lang="ts">
import { ref, h, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { NButton, NCard, NTag, NSpace, NInput, NSelect, NDrawer, NDrawerContent, useMessage, NImage, NImageGroup, NUpload, NInputNumber, useDialog, NGrid, NGi, NPopconfirm } from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import { useFormRules } from '@/hooks/common/form';
import {
  fetchGetElderList,
  fetchCreateElder,
  fetchUpdateElder,
  fetchDeleteElder,
  fetchBatchDeleteElder,
  fetchGetElderStatistics,
  fetchGetElder,
  fetchGetElderHealth,
  fetchSaveElderHealth
} from '@/service/api';
import { useNaivePaginatedTable, useTableOperate, defaultTransform } from '@/hooks/common/table';
import { useNaiveForm } from '@/hooks/common/form';
import { useAuth } from '@/hooks/business/auth';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';
import LazyImage from '@/components/common/lazy-image.vue';

defineOptions({
  name: 'BusinessElder'
});

const message = useMessage();
const dialog = useDialog();
const route = useRoute();
const router = useRouter();
const { patternRules, createRequiredRule } = useFormRules();
const { formRef, validate, restoreValidation } = useNaiveForm();
const { hasAuth } = useAuth();

// Detail drawer state
const detailVisible = ref(false);
const detailData = ref<Api.Elder.Elder | null>(null);
const detailLoading = ref(false);

// Image upload for detail
const MAX_IMAGE_COUNT = 6;
const uploadingFiles = new Set<string>();

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

function handleDetailUploadRequest({ file, data }: { file: UploadFile; data?: Record<string, string> }) {
  if (!file.file) return;

  const reader = new FileReader();
  reader.onload = e => {
    if (e.target?.result) {
      const base64Data = e.target.result as string;
      const field = data?.field || 'healthPhotos';
      if (field === 'healthPhotos' && detailData.value) {
        const photos = parsePhotos(detailData.value.healthPhotos);
        if (!photos.includes(base64Data) && photos.length < MAX_IMAGE_COUNT) {
          detailData.value.healthPhotos = [...photos, base64Data];
        }
      }
      message.success('上传成功');
    }
  };
  reader.readAsDataURL(file.file);
  return false;
}

function removeDetailPhoto(photo: string) {
  if (detailData.value?.healthPhotos) {
    detailData.value.healthPhotos = detailData.value.healthPhotos.filter(p => p !== photo);
  }
}

async function showDetail(row: Api.Elder.Elder) {
  detailLoading.value = true;
  try {
    const { data, error } = await fetchGetElder(row.elderId);
    if (error) {
      message.error(error.message || '获取详情失败');
      return;
    }
    if (data) {
      detailData.value = data;
      detailVisible.value = true;
    }
  } finally {
    detailLoading.value = false;
  }
}

// 创建健康档案
async function handleCreateHealthArchive() {
  if (!detailData.value) return;
  const elderId = detailData.value.elderId;

  try {
    // 检查是否已有健康档案
    const { data: health } = await fetchGetElderHealth(elderId);
    if (health) {
      message.info('该客户已有健康档案');
      router.push({ path: '/business/health-archive', query: { elderId } });
      return;
    }
  } catch {
    // 404 = 没有健康档案，继续创建
  }

  try {
    await fetchSaveElderHealth(elderId, {
      healthStatus: detailData.value.healthStatus || 'GOOD',
      medicalHistory: detailData.value.medicalHistory || '',
      allergyHistory: detailData.value.allergies || ''
    });
    message.success('健康档案已创建');
    router.push({ path: '/business/health-archive', query: { elderId } });
  } catch (e: any) {
    message.error(e?.message || '创建健康档案失败');
  }
}

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
  { type: 'selection' },
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
    width: 200,
    fixed: 'right',
    render: row => {
      const buttons = [];
      buttons.push(h(NButton, { size: 'small', type: 'info', onClick: () => showDetail(row), style: { marginRight: '8px' } }, () => '详情'));
      if (hasAuth('elder:list:edit')) {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleOpenEdit(row.elderId), style: { marginRight: '8px' } }, () => '编辑'));
      }
      if (hasAuth('elder:list:delete')) {
        buttons.push(
          h(
            NPopconfirm,
            {
              onPositiveClick: () => handleDelete(row.elderId)
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
} = useNaivePaginatedTable<Api.Common.PaginatingQueryRecord<Api.Elder.Elder>, Api.Elder.Elder>({
  apiFn: async params => {
    const queryParams: any = {
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

async function handleBatchDelete() {
  if (!checkedRowKeys.value.length) return;
  try {
    await fetchBatchDeleteElder(checkedRowKeys.value);
    message.success('批量删除成功');
    await onBatchDeleted();
    await getStatistics();
  } catch (e) {
    console.error('Failed to batch delete', e);
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
  searchStatus.value = null;
  getDataByPage(1);
}

function handleStatusPillClick(statusValue: string | null) {
  searchStatus.value = statusValue;
  pagination.page = 1;
  getData();
}

onMounted(async () => {
  // 接收老人档案详情跳转参数（订单详情→老人档案详情），直接打开该老人详情抽屉
  if (route.query.elderId) {
    const elderId = String(route.query.elderId);
    try {
      const { data } = await fetchGetElder(elderId);
      if (data) {
        detailData.value = data;
        detailVisible.value = true;
      }
    } catch (e) {
      console.error('Failed to load elder detail from route', e);
    }
  }
  getStatistics();
  getData();
});
</script>

<template>
  <div>
    <!-- Statistics Cards -->
    <NCard title="客户统计" :bordered="false" style="margin-bottom: 16px">
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
          <span>客户档案管理</span>
        </div>
      </template>
      <div style="background: #f5f5f5; padding: 12px; margin-bottom: 12px; border-radius: 8px">
        <!-- 适老化：状态快捷筛选 Pill -->
        <div style="margin-bottom: 14px">
          <div style="font-size: 14px; font-weight: 600; color: #666; margin-bottom: 10px">按状态快速筛选</div>
          <div style="display: flex; flex-wrap: wrap; gap: 8px">
            <button
              v-for="opt in statusOptions"
              :key="opt.value"
              :class="searchStatus === opt.value ? 'status-pill active' : 'status-pill'"
              :style="searchStatus === opt.value ? '' : 'background:#fff;border-color:#d1d5db'"
              @click="handleStatusPillClick(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>
        <!-- 搜索条件行 -->
        <div style="display: flex; flex-wrap: wrap; gap: 10px; align-items: center">
          <NInput v-model:value="searchName" placeholder="姓名搜索" clearable style="width: 160px" size="medium" />
          <NInput v-model:value="searchIdCard" placeholder="身份证号" clearable style="width: 180px" size="medium" />
          <NInput v-model:value="searchPhone" placeholder="手机号" clearable style="width: 140px" size="medium" />
          <NSelect
            v-model:value="searchCareType"
            :options="careTypeOptions"
            placeholder="养老类型"
            clearable
            style="width: 140px"
          />
          <NButton type="primary" @click="() => { getData(); pagination.page = 1; }" style="height: 40px; font-size: 15px; font-weight: 600">搜索</NButton>
          <NButton @click="handleResetSearch" style="height: 40px; font-size: 15px">重置</NButton>
        </div>
      </div>

      <!-- Use framework's TableHeaderOperation component -->
      <TableHeaderOperation
        v-model:columns="columnChecks"
        :disabled-delete="checkedRowKeys.length === 0"
        :loading="loading"
        @add="handleOpenAdd"
        @refresh="getData"
        @delete="handleBatchDelete"
      >
      </TableHeaderOperation>

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
      <NDrawerContent :title="operateType === 'add' ? '新增客户' : '编辑客户'" closable>
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

    <!-- Elder Detail Drawer -->
    <NDrawer v-model:show="detailVisible" :width="600" placement="right" closable>
      <NDrawerContent title="客户档案详情" closable>
        <div v-if="detailData" style="padding: 0 8px">
          <!-- Basic Info -->
          <div style="margin-bottom: 24px">
            <div style="font-size: 16px; font-weight: 600; margin-bottom: 12px; color: #333">基本信息</div>
            <NGrid :cols="2" :x-gap="16" :y-gap="8">
              <NGi><div style="color: #999; font-size: 13px">姓名</div><div style="margin-top: 4px">{{ detailData.name }}</div></NGi>
              <NGi><div style="color: #999; font-size: 13px">性别</div><div style="margin-top: 4px">{{ getGenderLabel(detailData.gender) }}</div></NGi>
              <NGi><div style="color: #999; font-size: 13px">年龄</div><div style="margin-top: 4px">{{ detailData.age || '-' }}</div></NGi>
              <NGi><div style="color: #999; font-size: 13px">身份证号</div><div style="margin-top: 4px">{{ detailData.idCard }}</div></NGi>
              <NGi><div style="color: #999; font-size: 13px">手机号</div><div style="margin-top: 4px">{{ detailData.phone || '-' }}</div></NGi>
              <NGi><div style="color: #999; font-size: 13px">养老类型</div><div style="margin-top: 4px">{{ getCareTypeLabel(detailData.careType) }}</div></NGi>
              <NGi><div style="color: #999; font-size: 13px">护理等级</div><div style="margin-top: 4px">{{ getCareLevelLabel(detailData.careLevel) }}</div></NGi>
              <NGi><div style="color: #999; font-size: 13px">补贴类型</div><div style="margin-top: 4px">{{ getSubsidyTypeLabel(detailData.subsidyType) }}</div></NGi>
            </NGrid>
          </div>

          <!-- Address -->
          <div style="margin-bottom: 24px">
            <div style="font-size: 16px; font-weight: 600; margin-bottom: 12px; color: #333">地址信息</div>
            <div style="color: #999; font-size: 13px">区域</div>
            <div style="margin-top: 4px">{{ detailData.areaName || '-' }}</div>
            <div style="color: #999; font-size: 13px; margin-top: 8px">详细地址</div>
            <div style="margin-top: 4px">{{ detailData.address || '-' }}</div>
          </div>

          <!-- Emergency Contact -->
          <div style="margin-bottom: 24px">
            <div style="font-size: 16px; font-weight: 600; margin-bottom: 12px; color: #333">紧急联系人</div>
            <NGrid :cols="2" :x-gap="16">
              <NGi><div style="color: #999; font-size: 13px">联系人</div><div style="margin-top: 4px">{{ detailData.emergencyContact || '-' }}</div></NGi>
              <NGi><div style="color: #999; font-size: 13px">联系电话</div><div style="margin-top: 4px">{{ detailData.emergencyPhone || '-' }}</div></NGi>
            </NGrid>
          </div>

          <!-- Health Info -->
          <div style="margin-bottom: 24px">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px">
              <div style="font-size: 16px; font-weight: 600; color: #333">健康信息</div>
              <NButton size="small" type="primary" @click="handleCreateHealthArchive">创建健康档案</NButton>
            </div>
            <div style="color: #999; font-size: 13px">健康状况</div>
            <div style="margin-top: 4px">{{ detailData.healthStatus || '-' }}</div>
            <div style="color: #999; font-size: 13px; margin-top: 8px">既往病史</div>
            <div style="margin-top: 4px">{{ detailData.medicalHistory || '-' }}</div>
            <div style="color: #999; font-size: 13px; margin-top: 8px">过敏信息</div>
            <div style="margin-top: 4px">{{ detailData.allergies || '-' }}</div>
          </div>

          <!-- Photos -->
          <div style="margin-bottom: 24px">
            <div style="font-size: 16px; font-weight: 600; margin-bottom: 12px; color: #333">档案照片</div>
            <div style="color: #999; font-size: 13px; margin-bottom: 8px">健康档案照片</div>
            <div style="display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px">
              <template v-if="detailData.healthPhotos && detailData.healthPhotos.length > 0">
                <div v-for="(photo, index) in detailData.healthPhotos" :key="index" style="position: relative; width: 80px; height: 80px">
                  <LazyImage :src="photo" :width="80" :height="80" fit="cover" />
                  <div style="position: absolute; top: -8px; right: -8px; width: 20px; height: 20px; background: #ff4d4f; border-radius: 50%; color: white; font-size: 12px; display: flex; align-items: center; justify-content: center; cursor: pointer" @click="removeDetailPhoto(photo)">×</div>
                </div>
              </template>
              <NUpload
                :show-file-list="false"
                :max="MAX_IMAGE_COUNT - (detailData.healthPhotos?.length || 0)"
                multiple
                accept="image/*"
                :custom-request="handleDetailUploadRequest"
              >
                <div style="width: 80px; height: 80px; border: 1px dashed #ddd; border-radius: 4px; display: flex; align-items: center; justify-content: center; cursor: pointer; color: #999">
                  <span style="font-size: 24px">+</span>
                </div>
              </NUpload>
            </div>
          </div>

          <!-- Other Info -->
          <div style="margin-bottom: 24px">
            <div style="font-size: 16px; font-weight: 600; margin-bottom: 12px; color: #333">其他信息</div>
            <div style="color: #999; font-size: 13px">服务商</div>
            <div style="margin-top: 4px">{{ detailData.providerName || '-' }}</div>
            <div style="color: #999; font-size: 13px; margin-top: 8px">备注</div>
            <div style="margin-top: 4px">{{ detailData.remark || '-' }}</div>
            <div style="color: #999; font-size: 13px; margin-top: 8px">创建时间</div>
            <div style="margin-top: 4px">{{ detailData.createTime || '-' }}</div>
          </div>
        </div>
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
