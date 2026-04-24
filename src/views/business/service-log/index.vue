<script setup lang="ts">
import { ref, h, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NModal,
  NTag,
  NSpace,
  NInput,
  NSelect,
  NDatePicker,
  NStatistic,
  NUpload,
  NInputNumber,
  NText,
  useMessage,
  NImage,
  NImageGroup,
  NPopconfirm,
  NImagePreview,
  NDrawer,
  NDrawerContent
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import {
  fetchGetServiceLogList,
  fetchGetServiceLogStatistics,
  fetchGetElder,
  fetchGetStaff,
  fetchSubmitServiceLog,
  fetchUpdateServiceLog,
  fetchSubmitServiceLogForReview,
  fetchDeleteServiceLog,
  fetchReviewServiceLog
} from '@/service/api';
import { useNaivePaginatedTable, defaultTransform } from '@/hooks/common/table';
import { useAuth } from '@/hooks/business/auth';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';

defineOptions({
  name: 'BusinessServiceLog'
});

const message = useMessage();
const { hasAuth } = useAuth();

// Constants
const MAX_IMAGE_COUNT = 6;
const MAX_IMAGE_SIZE = 3 * 1024 * 1024; // 3M

const route = useRoute();
const router = useRouter();

// Statistics
const statistics = ref<Api.ServiceLog.Statistics>({
  total: 0,
  today: 0,
  month: 0,
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  approvalRate: 0,
  pendingRate: 0,
  avgDuration: 0,
  avgScore: 0,
  anomalyCount: 0,
  anomalyRate: 0,
  avgReviewTime: 0,
  staffRankings: []
});

// Search
const searchOrderNo = ref('');
const searchElderName = ref('');
const searchStaffName = ref('');
const searchServiceCategory = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Add/Edit Modal state
const drawerVisible = ref(false);
const drawerLoading = ref(false);
const operateType = ref<'add' | 'edit'>('add');
const currentLogId = ref('');
const formData = ref({
  orderId: '',
  serviceStartTime: null as number | null,
  serviceEndTime: null as number | null,
  serviceDuration: 0,
  serviceContent: '',
  servicePhotos: [] as string[],
  healthObservations: '',
  medicationGiven: ''
});

// Helper: convert timestamp to LocalDateTime string format
function formatDateTime(timestamp: number | null): string | null {
  if (!timestamp) return null;
  const date = new Date(timestamp);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

// Submit for review dialog state
const reviewDialogVisible = ref(false);
const reviewRemarks = ref('');
const reviewLogId = ref('');

// Quality review modal state (for quality check staff)
const qualityReviewModalVisible = ref(false);
const qualityReviewData = ref<Api.ServiceLog.ServiceLog | null>(null);
const qualityReviewResult = ref<'APPROVED' | 'REJECTED'>('APPROVED');
const qualityReviewComment = ref('');

// Detail modal state
const detailDrawerVisible = ref(false);
const detailData = ref<Api.ServiceLog.ServiceLog | null>(null);
const previewVisible = ref(false);
const previewImages = ref<string[]>([]);

// Service category options
const categoryOptions = [
  { label: '养老服务', value: 'ELDER_CARE' },
  { label: '家政服务', value: 'HOME_CARE' }
];

// Log status options
const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已提交', value: 'SUBMITTED' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '已完成', value: 'COMPLETED' }
];

function getCategoryLabel(category?: string): string {
  const option = categoryOptions.find(o => o.value === category);
  return option?.label || category || '';
}

function getStatusType(status: Api.ServiceLog.LogStatus): 'warning' | 'success' | 'info' | 'error' | 'default' {
  const map: Record<string, 'warning' | 'success' | 'info' | 'error' | 'default'> = {
    DRAFT: 'warning',
    PENDING: 'warning',
    SUBMITTED: 'info',
    APPROVED: 'success',
    REJECTED: 'error',
    COMPLETED: 'success'
  };
  return map[status] || 'default';
}

function getStatusLabel(status: string): string {
  const option = statusOptions.find(o => o.value === status);
  return option?.label || status;
}

const columns: DataTableColumns<Api.ServiceLog.ServiceLog> = [
  { title: '日志编号', key: 'logNo', width: 160 },
  { title: '订单号', key: 'orderNo', width: 160 },
  {
    title: '客户姓名',
    key: 'elderName',
    width: 100,
    render: row =>
      h('a', { style: { color: '#18a058', cursor: 'pointer' }, onClick: () => showElderDetail(row) }, row.elderName)
  },
  {
    title: '服务人员',
    key: 'staffName',
    width: 100,
    render: row =>
      row.staffName
        ? h('a', { style: { color: '#18a058', cursor: 'pointer' }, onClick: () => showStaffDetail(row) }, row.staffName)
        : '-'
  },
  { title: '联系电话', key: 'staffPhone', width: 120 },
  { title: '服务商', key: 'providerName', width: 120 },
  { title: '服务类别', key: 'serviceCategory', width: 100, render: row => getCategoryLabel(row.serviceCategory) },
  { title: '服务类型', key: 'serviceType', width: 120 },
  {
    title: '服务时长',
    key: 'serviceDuration',
    width: 100,
    render: row => (row.serviceDuration ? `${row.serviceDuration}分钟` : '-')
  },
  {
    title: '照片',
    key: 'servicePhotos',
    width: 80,
    render: (row: Api.ServiceLog.ServiceLog) => {
      const photos = row.servicePhotos || [];
      const count = photos.length;
      if (count === 0) return '-';
      return h(
        'span',
        {
          style: 'color: #18a058; cursor: pointer;',
          onClick: () => showDetail(row)
        },
        `[${count}张]`
      );
    }
  },
  {
    title: '异常',
    key: 'hasAnomaly',
    width: 80,
    render: row =>
      h(NTag, { type: row.hasAnomaly ? 'error' : 'success', size: 'small' }, () => (row.hasAnomaly ? '有' : '无'))
  },
  {
    title: '状态',
    key: 'auditStatus',
    width: 100,
    render: row => h(NTag, { type: getStatusType(row.auditStatus), size: 'small' }, () => getStatusLabel(row.auditStatus))
  },
  { title: '提交时间', key: 'submitTime', width: 170 },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 320,
    fixed: 'right',
    render: (row: Api.ServiceLog.ServiceLog) => {
      const buttons = [];
      buttons.push(h(NButton, { size: 'small', onClick: () => showDetail(row) }, { default: () => '详情' }));
      if (!row.auditStatus || row.auditStatus === 'DRAFT') {
        if (hasAuth('service-log:list:edit')) {
          buttons.push(
            h(NButton, { size: 'small', type: 'default', onClick: () => handleUpdate(row) }, { default: () => '更新' }),
          );
        }
        buttons.push(
          h(
            NPopconfirm,
            {
              onPositiveClick: () => handleDelete(row)
            },
            {
              trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
              default: () => '确认删除?'
            }
          ),
          h(
            NButton,
            { size: 'small', type: 'primary', onClick: () => handleSubmitReview(row) },
            { default: () => '提交审核' }
          )
        );
      }
      // 审核按钮 - SUBMITTED状态显示（质检角色）
      if (row.auditStatus === 'SUBMITTED' && hasAuth('service-log:list:review')) {
        buttons.push(
          h(
            NButton,
            { size: 'small', type: 'warning', onClick: () => showReviewModal(row) },
            { default: () => '审核' }
          )
        );
      }
      // 已审核通过的服务日志可发起质检和评价
      if (row.auditStatus === 'APPROVED' || row.auditStatus === 'COMPLETED') {
        buttons.push(
          h(
            NButton,
            { size: 'small', type: 'info', onClick: () => goToQualityCheck(row), style: { marginLeft: '4px' } },
            { default: () => '去质检' }
          )
        );
        buttons.push(
          h(
            NButton,
            { size: 'small', type: 'success', onClick: () => goToEvaluation(row), style: { marginLeft: '4px' } },
            { default: () => '去评价' }
          )
        );
      }
      return h(NSpace, { size: 'small' }, buttons);
    }
  }
];

// Use framework's table hook
const tableHookResult = useNaivePaginatedTable<
  Api.Common.PaginatingQueryRecord<Api.ServiceLog.ServiceLog>,
  Api.ServiceLog.ServiceLog
>({
  apiFn: async params => {
    const queryParams: any = {
      current: params.page,
      pageSize: params.pageSize
    };
    if (searchOrderNo.value) queryParams.orderNo = searchOrderNo.value;
    if (searchElderName.value) queryParams.elderName = searchElderName.value;
    if (searchStaffName.value) queryParams.staffName = searchStaffName.value;
    if (searchServiceCategory.value) queryParams.serviceType = searchServiceCategory.value;
    if (searchDateRange.value) {
      queryParams.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      queryParams.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }
    // Handle staffId from staff page
    if (route.query.staffId) {
      queryParams.staffId = route.query.staffId as string;
    }
    return fetchGetServiceLogList(queryParams);
  },
  apiParams: {
    page: 1,
    pageSize: 10
  },
  transform: defaultTransform,
  columns: () => columns
});

const {
  data: tableData,
  loading,
  pagination,
  mobilePagination,
  getData,
  getDataByPage,
  columnChecks: rawColumnChecks
} = tableHookResult;

// Ensure columnChecks is always an array (writable ref for v-model)
const columnChecks = ref<Array<{ prop: string; label: string; checked: boolean }>>([]);

// Watch rawColumnChecks and sync to columnChecks
watch(
  () => rawColumnChecks.value,
  val => {
    if (val && val.length > 0) {
      columnChecks.value = val;
    }
  },
  { immediate: true, deep: true }
);

// Elder detail modal
const elderDetailVisible = ref(false);
const elderDetailData = ref<Api.Elder.Elder | null>(null);

// Staff detail modal
const staffDetailVisible = ref(false);
const staffDetailData = ref<Api.Staff.Staff | null>(null);

async function showElderDetail(row: Api.ServiceLog.ServiceLog) {
  if (!row.elderId) return;
  try {
    const { data } = await fetchGetElder(row.elderId);
    if (data) {
      elderDetailData.value = data;
      elderDetailVisible.value = true;
    }
  } catch (e) {
    console.error('Failed to get elder detail', e);
  }
}

async function showStaffDetail(row: Api.ServiceLog.ServiceLog) {
  if (!row.staffId) return;
  try {
    const { data } = await fetchGetStaff(row.staffId);
    if (data) {
      staffDetailData.value = data;
      staffDetailVisible.value = true;
    }
  } catch (e) {
    console.error('Failed to get staff detail', e);
  }
}

function showDetail(row: Api.ServiceLog.ServiceLog) {
  detailData.value = row;
  detailDrawerVisible.value = true;
}

function handleUpdateFromDetail() {
  if (!detailData.value) return;
  detailDrawerVisible.value = false;
  handleUpdate(detailData.value);
}

function handleSubmitReviewFromDetail() {
  if (!detailData.value) return;
  detailDrawerVisible.value = false;
  handleSubmitReview(detailData.value);
}

function openPreview(photos: string[]) {
  previewImages.value = photos;
  previewVisible.value = true;
}

function handleResetSearch() {
  searchOrderNo.value = '';
  searchElderName.value = '';
  searchStaffName.value = '';
  searchServiceCategory.value = '';
  searchDateRange.value = null;
  getDataByPage(1);
}

async function handleDelete(row: Api.ServiceLog.ServiceLog) {
  try {
    await fetchDeleteServiceLog(row.serviceLogId);
    message.success('删除成功');
    getData();
  } catch (e: any) {
    message.error(e.message || '删除失败');
  }
}

async function getStatistics() {
  try {
    const { data } = await fetchGetServiceLogStatistics();
    if (data) {
      statistics.value = data;
    }
  } catch (e) {
    console.error('Failed to get statistics', e);
  }
}

// Image upload handlers
const uploadingFiles = new Set<string>();

function handleUploadRequest({ file }: { file: UploadFile }) {
  if (!file.file) return;

  // Ensure servicePhotos is always an array
  if (!Array.isArray(formData.value.servicePhotos)) {
    formData.value.servicePhotos = [];
  }

  // Prevent duplicate uploads
  const fileKey = `${file.name}-${file.size}`;
  if (uploadingFiles.has(fileKey)) {
    return;
  }

  if (file.file.size > MAX_IMAGE_SIZE) {
    message.error(`图片大小不能超过3M`);
    file.onError?.();
    return;
  }
  if (formData.value.servicePhotos.length >= MAX_IMAGE_COUNT) {
    message.error(`最多只能上传${MAX_IMAGE_COUNT}张图片`);
    file.onError?.();
    return;
  }

  uploadingFiles.add(fileKey);

  const reader = new FileReader();
  reader.onload = e => {
    uploadingFiles.delete(fileKey);
    if (e.target?.result) {
      const base64Data = e.target.result as string;
      if (!formData.value.servicePhotos.includes(base64Data)) {
        formData.value.servicePhotos.push(base64Data);
      }
      file.onSuccess?.();
    }
  };
  reader.onerror = () => {
    uploadingFiles.delete(fileKey);
    file.onError?.();
  };
  reader.readAsDataURL(file.file);
}

function removePhoto(index: number) {
  formData.value.servicePhotos.splice(index, 1);
}

// Modal functions
function showAddModal() {
  operateType.value = 'add';
  currentLogId.value = '';
  formData.value = {
    orderId: '',
    serviceStartTime: null,
    serviceEndTime: null,
    serviceDuration: 0,
    serviceContent: '',
    servicePhotos: [],
    healthObservations: '',
    medicationGiven: ''
  };
  drawerVisible.value = true;
}

function handleUpdate(row: Api.ServiceLog.ServiceLog) {
  operateType.value = 'edit';
  currentLogId.value = row.serviceLogId;
  // Convert ISO date string to timestamp for NDatePicker
  const parseToTimestamp = (val: string | null | undefined): number | null => {
    if (!val) return null;
    const date = new Date(val);
    return isNaN(date.getTime()) ? null : date.getTime();
  };
  formData.value = {
    orderId: row.orderId || '',
    serviceStartTime: parseToTimestamp(row.serviceStartTime),
    serviceEndTime: parseToTimestamp(row.serviceEndTime),
    serviceDuration: row.serviceDuration || 0,
    serviceContent: row.serviceContent || '',
    servicePhotos: Array.isArray(row.servicePhotos)
      ? row.servicePhotos
      : row.servicePhotos
        ? [row.servicePhotos]
        : [],
    healthObservations: (row as any).healthObservations || '',
    medicationGiven: (row as any).medicationGiven || ''
  };
  drawerVisible.value = true;
}

function handleSubmitReview(row: Api.ServiceLog.ServiceLog) {
  reviewLogId.value = row.serviceLogId;
  reviewRemarks.value = '';
  reviewDialogVisible.value = true;
}

function showReviewModal(row: Api.ServiceLog.ServiceLog) {
  qualityReviewData.value = row;
  qualityReviewResult.value = 'APPROVED';
  qualityReviewComment.value = '';
  qualityReviewModalVisible.value = true;
}

function goToQualityCheck(row: Api.ServiceLog.ServiceLog) {
  router.push({ path: '/business/quality', query: { orderNo: row.orderNo || '' } });
}

function goToEvaluation(row: Api.ServiceLog.ServiceLog) {
  router.push({ path: '/business/evaluation', query: { orderNo: row.orderNo || '' } });
}

async function handleQualityReview() {
  if (!qualityReviewData.value) return;
  try {
    await fetchReviewServiceLog(qualityReviewData.value.serviceLogId, qualityReviewResult.value, qualityReviewComment.value);
    message.success(qualityReviewResult.value === 'APPROVED' ? '审核通过' : '已驳回');
    qualityReviewModalVisible.value = false;
    getData();
  } catch (e: any) {
    message.error(e.message || '审核失败');
  }
}

async function submitReview() {
  try {
    await fetchSubmitServiceLogForReview(reviewLogId.value, reviewRemarks.value);
    message.success('提交审核成功');
    reviewDialogVisible.value = false;
    getData();
  } catch (e: any) {
    message.error(e.message || '提交审核失败');
  }
}

async function handleSubmitForm() {
  if (!formData.value.orderId) {
    message.error('请选择关联订单');
    return;
  }
  drawerLoading.value = true;
  try {
    // Use currentLogId to determine if it's update or create
    const logId = currentLogId.value;
    if (logId) {
      // Update existing log
      await fetchUpdateServiceLog(logId, {
        id: logId,
        orderId: formData.value.orderId,
        serviceStartTime: formatDateTime(formData.value.serviceStartTime),
        serviceEndTime: formatDateTime(formData.value.serviceEndTime),
        serviceDuration: formData.value.serviceDuration,
        serviceContent: formData.value.serviceContent,
        servicePhotos: formData.value.servicePhotos,
        healthObservations: formData.value.healthObservations,
        medicationGiven: formData.value.medicationGiven,
        status: 'DRAFT'
      } as any);
      message.success('更新成功');
    } else {
      // Create new log
      await fetchSubmitServiceLog({
        orderId: formData.value.orderId,
        serviceStartTime: formatDateTime(formData.value.serviceStartTime),
        serviceEndTime: formatDateTime(formData.value.serviceEndTime),
        serviceDuration: formData.value.serviceDuration,
        serviceContent: formData.value.serviceContent,
        servicePhotos: formData.value.servicePhotos,
        healthObservations: formData.value.healthObservations,
        medicationGiven: formData.value.medicationGiven
      } as any);
      message.success('添加成功');
    }
    drawerVisible.value = false;
    getData();
  } catch (e: any) {
    message.error(e.message || '操作失败');
  } finally {
    drawerLoading.value = false;
  }
}

function closeModal() {
  drawerVisible.value = false;
}

function closeReviewDialog() {
  reviewDialogVisible.value = false;
}

onMounted(() => {
  // Handle query parameters from other pages
  if (route.query.orderId) {
    searchOrderNo.value = (route.query.orderNo as string) || (route.query.orderId as string);
  }
  getData();
});
</script>

<template>
  <div>
    <!-- Statistics Cards -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <template #header>
        <div style="display: flex; align-items: center; gap: 12px">
          <div
            style="
              width: 4px;
              height: 20px;
              background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
              border-radius: 2px;
            "
          ></div>
          <span style="font-size: 16px; font-weight: 600">服务日志概览</span>
        </div>
      </template>
      <!-- 第一行：核心数量 -->
      <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px">
        <div class="stat-card stat-primary">
          <div class="stat-label">总日志数</div>
          <div class="stat-value">{{ statistics.total }}</div>
          <div class="stat-sub">本月新增 {{ statistics.month }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">今日新增</div>
          <div class="stat-value">{{ statistics.today }}</div>
          <div class="stat-sub">草稿 {{ statistics.draftCount }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">已提交</div>
          <div class="stat-value">{{ statistics.submittedCount }}</div>
          <div class="stat-sub">提交率 {{ Number(statistics.submissionRate || 0).toFixed(1) }}%</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-label">已核实</div>
          <div class="stat-value">{{ statistics.verifiedCount }}</div>
          <div class="stat-sub">
            异常 {{ statistics.anomalyCount }} (异常率 {{ Number(statistics.anomalyRate || 0).toFixed(1) }}%)
          </div>
        </div>
      </div>
      <!-- 第二行：质量指标 -->
      <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px">
        <div class="stat-card-mini">
          <div class="stat-mini-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%)">
            <span style="font-size: 20px">⏱</span>
          </div>
          <div class="stat-mini-content">
            <div class="stat-mini-value">{{ Number(statistics.avgDuration || 0).toFixed(0) }}分钟</div>
            <div class="stat-mini-label">平均服务时长</div>
          </div>
        </div>
        <div class="stat-card-mini">
          <div class="stat-mini-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
            <span style="font-size: 20px">⭐</span>
          </div>
          <div class="stat-mini-content">
            <div class="stat-mini-value">{{ Number(statistics.avgScore || 0).toFixed(1) }}分</div>
            <div class="stat-mini-label">平均服务评分</div>
          </div>
        </div>
        <div class="stat-card-mini">
          <div class="stat-mini-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
            <span style="font-size: 20px">⏱</span>
          </div>
          <div class="stat-mini-content">
            <div class="stat-mini-value">{{ Number(statistics.avgReviewTime || 0).toFixed(1) }}h</div>
            <div class="stat-mini-label">平均审核耗时</div>
          </div>
        </div>
        <div class="stat-card-mini">
          <div class="stat-mini-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
            <span style="font-size: 20px">📊</span>
          </div>
          <div class="stat-mini-content">
            <div class="stat-mini-value">{{ Number(statistics.pendingRate || 0).toFixed(1) }}%</div>
            <div class="stat-mini-label">待审核率</div>
          </div>
        </div>
      </div>
      <!-- 第三行：服务人员排名 -->
      <div
        v-if="statistics.staffRankings && statistics.staffRankings.length > 0"
        style="background: #f8f9fa; border-radius: 12px; padding: 16px"
      >
        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 12px">
          <span style="font-size: 14px; font-weight: 600; color: #667eea">🏆 服务标兵排行榜</span>
        </div>
        <div style="display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px">
          <div
            v-for="(rank, index) in statistics.staffRankings.slice(0, 5)"
            :key="rank.staffId"
            style="
              background: white;
              border-radius: 8px;
              padding: 12px;
              text-align: center;
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
            "
          >
            <div
              :style="
                'width: 28px; height: 28px; border-radius: 50%; margin: 0 auto 8px; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; color: white; background: ' +
                (index === 0 ? '#ffd700' : index === 1 ? '#c0c0c0' : index === 2 ? '#cd7f32' : '#667eea') +
                ';'
              "
            >
              {{ index + 1 }}
            </div>
            <div style="font-weight: 600; font-size: 13px; color: #333; margin-bottom: 4px">
              {{ rank.staffName || '未知' }}
            </div>
            <div style="font-size: 11px; color: #999; margin-bottom: 6px">{{ rank.providerName || '-' }}</div>
            <div style="display: flex; justify-content: center; gap: 8px; font-size: 11px">
              <span style="color: #18a058">通过 {{ rank.approvedCount }}</span>
              <span style="color: #ff4d4f">驳回 {{ rank.rejectedCount }}</span>
            </div>
            <div
              style="
                margin-top: 6px;
                padding: 4px 8px;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                border-radius: 12px;
                color: white;
                font-size: 12px;
                font-weight: 600;
              "
            >
              {{ Number(rank.approvalRate || 0).toFixed(1) }}% 通过率
            </div>
          </div>
        </div>
      </div>
    </NCard>

    <!-- Table -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <template #header>
        <span>服务日志管理</span>
      </template>
      <div style="background: #f5f5f5; padding: 12px; margin-bottom: 12px; border-radius: 4px">
        <NSpace :wrap="true" align="center">
          <NInput v-model:value="searchOrderNo" placeholder="订单号" clearable style="width: 150px" />
          <NInput v-model:value="searchElderName" placeholder="客户姓名" clearable style="width: 100px" />
          <NInput v-model:value="searchStaffName" placeholder="服务人员" clearable style="width: 100px" />
          <NSelect
            v-model:value="searchServiceCategory"
            :options="categoryOptions"
            placeholder="服务类别"
            clearable
            style="width: 120px"
          />
          <NDatePicker v-model:value="searchDateRange" type="daterange" clearable style="width: 260px" />
          <NButton type="primary" @click="getData">搜索</NButton>
          <NButton @click="handleResetSearch">重置</NButton>
        </NSpace>
      </div>

      <TableHeaderOperation v-model:columns="columnChecks" :loading="loading" @add="showAddModal" @refresh="getData" />

      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1400"
        :row-key="(row: Api.ServiceLog.ServiceLog) => row.serviceLogId"
        remote
        :pagination="mobilePagination"
      />
    </NCard>

    <!-- Elder Detail Modal -->
    <NModal v-model:show="elderDetailVisible" title="客户档案详情" preset="card" style="width: 600px">
      <NForm v-if="elderDetailData" label-placement="left" label-width="100">
        <NFormItem label="姓名">{{ elderDetailData.name }}</NFormItem>
        <NFormItem label="性别">
          {{ elderDetailData.gender === 'MALE' ? '男' : elderDetailData.gender === 'FEMALE' ? '女' : '未知' }}
        </NFormItem>
        <NFormItem label="年龄">{{ elderDetailData.age }}</NFormItem>
        <NFormItem label="身份证号">{{ elderDetailData.idCard }}</NFormItem>
        <NFormItem label="手机号">{{ elderDetailData.phone }}</NFormItem>
        <NFormItem label="地址">{{ elderDetailData.address }}</NFormItem>
        <NFormItem label="养老类型">
          {{
            elderDetailData.careType === 'HOME'
              ? '居家养老'
              : elderDetailData.careType === 'COMMUNITY'
                ? '社区养老'
                : elderDetailData.careType === 'INSTITUTION'
                  ? '机构养老'
                  : '-'
          }}
        </NFormItem>
        <NFormItem label="护理等级">{{ elderDetailData.careLevel }}</NFormItem>
        <NFormItem label="紧急联系人">{{ elderDetailData.emergencyContact || '-' }}</NFormItem>
        <NFormItem label="紧急联系电话">{{ elderDetailData.emergencyPhone || '-' }}</NFormItem>
      </NForm>
    </NModal>

    <!-- Staff Detail Modal -->
    <NModal v-model:show="staffDetailVisible" title="服务人员详情" preset="card" style="width: 600px">
      <NForm v-if="staffDetailData" label-placement="left" label-width="100">
        <NFormItem label="姓名">{{ staffDetailData.staffName }}</NFormItem>
        <NFormItem label="性别">{{ staffDetailData.gender === 1 ? '男' : '女' }}</NFormItem>
        <NFormItem label="工号">{{ staffDetailData.staffNo || '-' }}</NFormItem>
        <NFormItem label="手机号">{{ staffDetailData.phone || '-' }}</NFormItem>
        <NFormItem label="身份证号">{{ staffDetailData.idCard || '-' }}</NFormItem>
        <NFormItem label="所属服务商">{{ staffDetailData.providerName || '-' }}</NFormItem>
        <NFormItem label="服务类型">{{ staffDetailData.serviceTypes || '-' }}</NFormItem>
        <NFormItem label="紧急联系人">{{ staffDetailData.emergencyContact || '-' }}</NFormItem>
        <NFormItem label="紧急联系电话">{{ staffDetailData.emergencyPhone || '-' }}</NFormItem>
      </NForm>
    </NModal>

    <!-- Add/Edit Drawer -->
    <NDrawer v-model:show="drawerVisible" :width="560" placement="right" closable>
      <NDrawerContent :title="operateType === 'add' ? '添加服务日志' : '编辑服务日志'" closable>
        <div style="padding: 16px 0">
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px">
            <div>
              <div style="margin-bottom: 8px; color: #333; font-weight: 500">关联订单 *</div>
              <NInput v-model:value="formData.orderId" placeholder="请输入订单ID" />
            </div>
            <div>
              <div style="margin-bottom: 8px; color: #333; font-weight: 500">服务时长(分钟)</div>
              <NInputNumber v-model:value="formData.serviceDuration" :min="0" style="width: 100%" />
            </div>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 16px">
            <div>
              <div style="margin-bottom: 8px; color: #333; font-weight: 500">服务开始时间</div>
              <NDatePicker v-model:value="formData.serviceStartTime" type="datetime" style="width: 100%" />
            </div>
            <div>
              <div style="margin-bottom: 8px; color: #333; font-weight: 500">服务结束时间</div>
              <NDatePicker v-model:value="formData.serviceEndTime" type="datetime" style="width: 100%" />
            </div>
          </div>
          <div style="margin-top: 16px">
            <div style="margin-bottom: 8px; color: #333; font-weight: 500">服务内容</div>
            <NInput v-model:value="formData.serviceContent" type="textarea" placeholder="请输入服务内容" :rows="3" />
          </div>
          <div style="margin-top: 16px">
            <div style="margin-bottom: 8px; color: #333; font-weight: 500">健康观察备注</div>
            <NInput
              v-model:value="formData.healthObservations"
              type="textarea"
              placeholder="请输入本次服务的健康观察（可选）"
              :rows="2"
            />
          </div>
          <div style="margin-top: 16px">
            <div style="margin-bottom: 8px; color: #333; font-weight: 500">本次给药记录</div>
            <NInput
              v-model:value="formData.medicationGiven"
              type="textarea"
              placeholder="请输入本次服务给药记录（可选）"
              :rows="2"
            />
          </div>
          <div style="margin-top: 16px">
            <div style="margin-bottom: 8px; color: #333; font-weight: 500">
              服务照片
              <span style="color: #999; font-weight: normal">
                ({{ formData.servicePhotos.length }}/{{ MAX_IMAGE_COUNT }})
              </span>
            </div>
            <div style="display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 12px">
              <div
                v-for="(photo, index) in formData.servicePhotos"
                :key="index"
                style="position: relative; width: 80px; height: 80px"
              >
                <img
                  :src="photo"
                  style="width: 80px; height: 80px; object-fit: cover; border-radius: 8px; border: 2px solid #e6e6e6"
                />
                <div
                  style="
                    position: absolute;
                    top: -8px;
                    right: -8px;
                    width: 22px;
                    height: 22px;
                    background: #ff4d4f;
                    border-radius: 50%;
                    color: white;
                    text-align: center;
                    line-height: 22px;
                    font-size: 14px;
                    cursor: pointer;
                    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
                  "
                  @click="removePhoto(index)"
                >
                  ×
                </div>
              </div>
            </div>
            <NUpload
              :show-file-list="false"
              :max="MAX_IMAGE_COUNT - formData.servicePhotos.length"
              multiple
              accept="image/*"
              :custom-request="handleUploadRequest"
            >
              <NButton :disabled="formData.servicePhotos.length >= MAX_IMAGE_COUNT">
                <template #icon>
                  <span style="margin-right: 4px">+</span>
                </template>
                选择图片
              </NButton>
            </NUpload>
            <div style="font-size: 12px; color: #999; margin-top: 8px">每张图片不超过3M</div>
          </div>
        </div>
        <template #footer>
          <div style="display: flex; justify-content: flex-end; gap: 12px">
            <NButton @click="closeModal">取消</NButton>
            <NButton type="primary" :loading="drawerLoading" @click="handleSubmitForm">
              {{ operateType === 'edit' ? '保存修改' : '确认添加' }}
            </NButton>
          </div>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- Detail Drawer -->
    <NDrawer v-model:show="detailDrawerVisible" :width="600" placement="right" closable>
      <NDrawerContent :title="detailData?.logNo + ' - 服务日志详情'" closable>
        <template #header>
          <div style="display: flex; align-items: center; gap: 12px">
            <div
              style="
                width: 4px;
                height: 24px;
                background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
                border-radius: 2px;
              "
            ></div>
            <span style="font-size: 18px; font-weight: 600">服务日志详情</span>
            <NTag v-if="detailData" :type="getStatusType(detailData.auditStatus)" size="large">
              {{ getStatusLabel(detailData.auditStatus) }}
            </NTag>
          </div>
        </template>
        <div v-if="detailData" style="padding: 8px 0">
          <!-- Info Section -->
          <div style="background: #f8f9fa; border-radius: 12px; padding: 20px; margin-bottom: 16px">
            <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px">
              <div>
                <div style="color: #999; font-size: 12px; margin-bottom: 4px">日志编号</div>
                <div style="font-weight: 500; color: #333">{{ detailData.logNo }}</div>
              </div>
              <div>
                <div style="color: #999; font-size: 12px; margin-bottom: 4px">订单号</div>
                <div style="font-weight: 500; color: #333">{{ detailData.orderNo || '-' }}</div>
              </div>
            </div>
          </div>

          <!-- People Info -->
          <div style="background: #f8f9fa; border-radius: 12px; padding: 20px; margin-bottom: 16px">
            <div style="color: #667eea; font-size: 13px; font-weight: 600; margin-bottom: 12px">服务对象</div>
            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px">
              <div>
                <div style="color: #999; font-size: 12px; margin-bottom: 4px">客户姓名</div>
                <div style="font-weight: 500">{{ detailData.elderName || '-' }}</div>
              </div>
              <div>
                <div style="color: #999; font-size: 12px; margin-bottom: 4px">服务人员</div>
                <div style="font-weight: 500">{{ detailData.staffName || '-' }}</div>
              </div>
              <div>
                <div style="color: #999; font-size: 12px; margin-bottom: 4px">服务商</div>
                <div style="font-weight: 500">{{ detailData.providerName || '-' }}</div>
              </div>
            </div>
          </div>

          <!-- Service Info -->
          <div style="background: #f8f9fa; border-radius: 12px; padding: 20px; margin-bottom: 16px">
            <div style="color: #667eea; font-size: 13px; font-weight: 600; margin-bottom: 12px">服务信息</div>
            <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px">
              <div>
                <div style="color: #999; font-size: 12px; margin-bottom: 4px">服务类别</div>
                <div style="font-weight: 500">{{ getCategoryLabel(detailData.serviceCategory) }}</div>
              </div>
              <div>
                <div style="color: #999; font-size: 12px; margin-bottom: 4px">服务类型</div>
                <div style="font-weight: 500">{{ detailData.serviceType || '-' }}</div>
              </div>
              <div>
                <div style="color: #999; font-size: 12px; margin-bottom: 4px">服务时长</div>
                <div style="font-weight: 500">
                  {{ detailData.serviceDuration ? `${detailData.serviceDuration}分钟` : '-' }}
                </div>
              </div>
              <div>
                <div style="color: #999; font-size: 12px; margin-bottom: 4px">开始时间</div>
                <div style="font-weight: 500; font-size: 12px">{{ detailData.serviceStartTime || '-' }}</div>
              </div>
              <div>
                <div style="color: #999; font-size: 12px; margin-bottom: 4px">结束时间</div>
                <div style="font-weight: 500; font-size: 12px">{{ detailData.serviceEndTime || '-' }}</div>
              </div>
            </div>
          </div>

          <!-- Content -->
          <div
            v-if="detailData.serviceContent"
            style="background: #f8f9fa; border-radius: 12px; padding: 20px; margin-bottom: 16px"
          >
            <div style="color: #667eea; font-size: 13px; font-weight: 600; margin-bottom: 12px">服务内容</div>
            <div style="color: #333; line-height: 1.6">{{ detailData.serviceContent }}</div>
          </div>

          <!-- Health Info -->
          <div
            v-if="detailData.healthObservations || (detailData as any).medicationGiven"
            style="background: #fff7e6; border-radius: 12px; padding: 20px; margin-bottom: 16px; border: 1px solid #ffd591"
          >
            <div style="color: #fa8c16; font-size: 13px; font-weight: 600; margin-bottom: 12px">健康观察</div>
            <div v-if="detailData.healthObservations" style="margin-bottom: 12px">
              <div style="color: #999; font-size: 12px; margin-bottom: 4px">观察备注</div>
              <div style="color: #333; line-height: 1.6">{{ detailData.healthObservations }}</div>
            </div>
            <div v-if="(detailData as any).medicationGiven">
              <div style="color: #999; font-size: 12px; margin-bottom: 4px">给药记录</div>
              <div style="color: #333; line-height: 1.6">{{ (detailData as any).medicationGiven }}</div>
            </div>
          </div>

          <!-- Photos -->
          <div
            v-if="detailData.servicePhotos && detailData.servicePhotos.length > 0"
            style="background: #f8f9fa; border-radius: 12px; padding: 20px; margin-bottom: 16px"
          >
            <div style="color: #667eea; font-size: 13px; font-weight: 600; margin-bottom: 12px">
              服务照片 ({{ detailData.servicePhotos.length }})
            </div>
            <div style="display: flex; flex-wrap: wrap; gap: 12px">
              <img
                v-for="(photo, idx) in detailData.servicePhotos"
                :key="idx"
                :src="photo"
                style="
                  width: 100px;
                  height: 100px;
                  object-fit: cover;
                  border-radius: 8px;
                  cursor: pointer;
                  transition: transform 0.2s;
                "
                @click="openPreview(detailData.servicePhotos || [])"
              />
            </div>
          </div>

          <!-- Review Remarks -->
          <div
            v-if="detailData.reviewRemarks"
            style="
              background: #fff7e6;
              border-radius: 12px;
              padding: 20px;
              margin-bottom: 16px;
              border: 1px solid #ffd591;
            "
          >
            <div style="color: #fa8c16; font-size: 13px; font-weight: 600; margin-bottom: 8px">审核备注</div>
            <div style="color: #333; line-height: 1.6">{{ detailData.reviewRemarks }}</div>
          </div>

          <!-- Time Info -->
          <div style="display: flex; gap: 24px; color: #999; font-size: 13px">
            <div>创建时间: {{ detailData.createTime || '-' }}</div>
            <div>提交时间: {{ detailData.submitTime || '-' }}</div>
          </div>
        </div>
        <template #footer>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <div>
              <NButton
                v-if="!detailData?.auditStatus || detailData?.auditStatus === 'DRAFT'"
                type="default"
                @click="handleUpdateFromDetail"
                style="margin-right: 8px"
              >
                更新
              </NButton>
              <NButton
                v-if="!detailData?.auditStatus || detailData?.auditStatus === 'DRAFT'"
                type="primary"
                @click="handleSubmitReviewFromDetail"
              >
                提交审核
              </NButton>
            </div>
            <NButton @click="detailDrawerVisible = false">关闭</NButton>
          </div>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- Image Preview -->
    <NModal v-model:show="previewVisible" preset="card" style="width: 900px">
      <template #header>
        <div style="display: flex; align-items: center; gap: 12px">
          <div
            style="
              width: 4px;
              height: 24px;
              background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
              border-radius: 2px;
            "
          ></div>
          <span style="font-size: 18px; font-weight: 600">图片预览 ({{ previewImages.length }})</span>
        </div>
      </template>
      <div style="display: flex; flex-wrap: wrap; gap: 16px; justify-content: center; padding: 20px 0">
        <img
          v-for="(photo, idx) in previewImages"
          :key="idx"
          :src="photo"
          style="
            max-width: 400px;
            max-height: 400px;
            object-fit: contain;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          "
        />
      </div>
      <template #footer>
        <div style="display: flex; justify-content: center">
          <NButton @click="previewVisible = false">关闭</NButton>
        </div>
      </template>
    </NModal>

    <!-- Submit for Review Dialog -->
    <NModal v-model:show="reviewDialogVisible" preset="card" style="width: 500px">
      <template #header>
        <div style="display: flex; align-items: center; gap: 12px">
          <div
            style="
              width: 4px;
              height: 24px;
              background: linear-gradient(180deg, #11998e 0%, #38ef7d 100%);
              border-radius: 2px;
            "
          ></div>
          <span style="font-size: 18px; font-weight: 600">提交审核</span>
        </div>
      </template>
      <div style="padding: 16px 0">
        <div
          style="
            background: #e6f7ff;
            border-radius: 8px;
            padding: 12px 16px;
            margin-bottom: 16px;
            color: #1890ff;
            font-size: 13px;
          "
        >
          提交后将进入审核流程，请确认信息无误。
        </div>
        <div style="margin-bottom: 8px; color: #333; font-weight: 500">审核备注（可选）</div>
        <NInput
          v-model:value="reviewRemarks"
          type="textarea"
          placeholder="请输入审核备注，如有问题请说明..."
          :rows="4"
          style="border-radius: 8px"
        />
      </div>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px">
          <NButton @click="closeReviewDialog">取消</NButton>
          <NButton type="primary" @click="submitReview">确认提交</NButton>
        </div>
      </template>
    </NModal>

    <!-- Quality Review Modal (for quality check staff) -->
    <NModal v-model:show="qualityReviewModalVisible" preset="card" style="width: 700px">
      <template #header>
        <div style="display: flex; align-items: center; gap: 12px">
          <div
            style="
              width: 4px;
              height: 24px;
              background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
              border-radius: 2px;
            "
          ></div>
          <span style="font-size: 18px; font-weight: 600">审核服务日志</span>
        </div>
      </template>
      <div v-if="qualityReviewData" style="padding: 16px 0">
        <!-- Service Photos Preview -->
        <div
          v-if="qualityReviewData.servicePhotos && qualityReviewData.servicePhotos.length > 0"
          style="background: #f8f9fa; border-radius: 12px; padding: 20px; margin-bottom: 16px"
        >
          <div style="color: #667eea; font-size: 13px; font-weight: 600; margin-bottom: 12px">
            服务照片 ({{ qualityReviewData.servicePhotos.length }})
          </div>
          <div style="display: flex; flex-wrap: wrap; gap: 12px">
            <img
              v-for="(photo, idx) in qualityReviewData.servicePhotos"
              :key="idx"
              :src="photo"
              style="
                width: 100px;
                height: 100px;
                object-fit: cover;
                border-radius: 8px;
                cursor: pointer;
                transition: transform 0.2s;
              "
              @click="openPreview(qualityReviewData.servicePhotos || [])"
            />
          </div>
        </div>

        <!-- Service Info Summary -->
        <div style="background: #f8f9fa; border-radius: 12px; padding: 20px; margin-bottom: 16px">
          <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px">
            <div>
              <div style="color: #999; font-size: 12px; margin-bottom: 4px">客户姓名</div>
              <div style="font-weight: 500">{{ qualityReviewData.elderName || '-' }}</div>
            </div>
            <div>
              <div style="color: #999; font-size: 12px; margin-bottom: 4px">服务人员</div>
              <div style="font-weight: 500">{{ qualityReviewData.staffName || '-' }}</div>
            </div>
            <div>
              <div style="color: #999; font-size: 12px; margin-bottom: 4px">服务时长</div>
              <div style="font-weight: 500">{{ qualityReviewData.serviceDuration ? `${qualityReviewData.serviceDuration}分钟` : '-' }}</div>
            </div>
          </div>
        </div>

        <!-- Review Result Selection -->
        <div style="margin-bottom: 16px">
          <div style="margin-bottom: 8px; color: #333; font-weight: 500">审核结果 *</div>
          <NSpace>
            <NButton
              :type="qualityReviewResult === 'APPROVED' ? 'success' : 'default'"
              @click="qualityReviewResult = 'APPROVED'"
            >
              通过
            </NButton>
            <NButton
              :type="qualityReviewResult === 'REJECTED' ? 'error' : 'default'"
              @click="qualityReviewResult = 'REJECTED'"
            >
              驳回（需返工）
            </NButton>
          </NSpace>
        </div>

        <!-- Review Comment -->
        <div>
          <div style="margin-bottom: 8px; color: #333; font-weight: 500">
            审核意见 {{ qualityReviewResult === 'REJECTED' ? '*' : '' }}
          </div>
          <NInput
            v-model:value="qualityReviewComment"
            type="textarea"
            :placeholder="qualityReviewResult === 'REJECTED' ? '请输入驳回原因，以便服务人员整改...' : '请输入审核意见（可选）...'"
            :rows="4"
            style="border-radius: 8px"
          />
        </div>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px">
          <NButton @click="qualityReviewModalVisible = false">取消</NButton>
          <NButton
            :type="qualityReviewResult === 'APPROVED' ? 'success' : 'error'"
            @click="handleQualityReview"
          >
            {{ qualityReviewResult === 'APPROVED' ? '确认通过' : '确认驳回' }}
          </NButton>
        </div>
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

.stat-sub {
  font-size: 11px;
  margin-top: 6px;
  opacity: 0.85;
}

.stat-card-mini {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f8f9fa;
  border-radius: 10px;
  padding: 12px 16px;
}

.stat-mini-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-mini-content {
  flex: 1;
  min-width: 0;
}

.stat-mini-value {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  line-height: 1.3;
}

.stat-mini-label {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
</style>
