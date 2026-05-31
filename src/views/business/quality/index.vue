<script setup lang="ts">
import { ref, h, onMounted, watch, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
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
  NSplit,
  NImage,
  NImageGroup,
  useMessage,
  NPopconfirm,
  NDrawer,
  NDrawerContent,
  NInputNumber,
  NSwitch,
  NDivider,
  NAlert
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import {
  fetchGetQualityCheckList,
  fetchGetQualityStatistics,
  fetchGetStaff,
  fetchGetQualityCheck,
  fetchSubmitRectify,
  fetchRecheck,
  fetchCreateQualityCheck,
  fetchGetOrderList,
  fetchInspect,
  fetchGetServiceLog,
  fetchBatchDeleteQualityCheck
} from '@/service/api';
import { useNaivePaginatedTable, defaultTransform } from '@/hooks/common/table';
import { useAuth } from '@/hooks/business/auth';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';
import FlowIndicator from '@/components/business/FlowIndicator.vue';
import { formatPercent, formatScore } from '@/utils/formatter';

defineOptions({
  name: 'BusinessQuality'
});

const message = useMessage();
const router = useRouter();
const route = useRoute();

// Create dialog state
const createDialogVisible = ref(false);
const createLoading = ref(false);
const createOrderNo = ref('');
const createForm = ref<Api.Quality.QualityCheckForm>({
  orderId: '',
  checkType: 'RANDOM',
  checkMethod: 'PHOTO_REVIEW',
  checkScore: 100,
  checkResult: 'QUALIFIED',
  checkPhotos: [],
  checkRemark: '',
  needRectify: false,
  rectifyNotice: '',
  rectifyDeadline: ''
});

// Statistics
const { hasAuth } = useAuth();
const statistics = ref<Api.Quality.Statistics>({
  total: 0,
  qualifiedCount: 0,
  unqualifiedCount: 0,
  needRectifyCount: 0,
  qualifiedRate: 0,
  avgScore: 0
});

// 计算当前流程步骤
const currentFlowStep = computed(() => {
  // 如果有待质检的，当前是质检审核阶段
  if (statistics.value.total > 0 && statistics.value.qualifiedCount === 0 && statistics.value.unqualifiedCount === 0) {
    return 'quality_check';
  }
  // 如果有合格的且有待整改的，进入整改阶段
  if (statistics.value.needRectifyCount > 0) {
    return 'service_completed';
  }
  // 如果全部合格，进入服务完成阶段
  if (statistics.value.qualifiedCount > 0 && statistics.value.needRectifyCount === 0) {
    return 'service_completed';
  }
  // 默认是日志提交阶段
  return 'log_submitted';
});

// 流程步骤配置
const flowSteps = [
  { key: 'service_started', label: '服务开始' },
  { key: 'log_submitted', label: '日志提交' },
  { key: 'quality_check', label: '质检审核' },
  { key: 'service_completed', label: '服务完成' },
  { key: 'evaluated', label: '已完成评价' }
];

// Search
const searchOrderNo = ref('');
const searchProviderName = ref('');
const searchStaffName = ref('');
const searchCheckResult = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Staff detail drawer
const staffDetailDrawerVisible = ref(false);
const staffDetailData = ref<Api.Staff.Staff | null>(null);

async function showStaffDetail(row: Api.Quality.QualityCheck) {
  if (!row.staffId) return;
  try {
    const { data } = await fetchGetStaff(row.staffId);
    if (data) {
      staffDetailData.value = data;
      staffDetailDrawerVisible.value = true;
    }
  } catch (e) {
    console.error('Failed to get staff detail', e);
  }
}

// Quality check detail drawer
const qualityDetailDrawerVisible = ref(false);
const qualityDetailData = ref<Api.Quality.QualityCheck | null>(null);
const serviceLogDetail = ref<Api.ServiceLog.ServiceLog | null>(null);

async function showQualityDetail(row: Api.Quality.QualityCheck) {
  try {
    const { data, error } = await fetchGetQualityCheck(row.qualityCheckId);
    if (error) {
      message.error(error.message || '获取质检详情失败');
      return;
    }
    if (data) {
      qualityDetailData.value = data;
      // 获取关联的服务日志详情
      if (data.serviceLogId) {
        const { data: logData } = await fetchGetServiceLog(data.serviceLogId);
        serviceLogDetail.value = logData || null;
      } else {
        serviceLogDetail.value = null;
      }
      qualityDetailDrawerVisible.value = true;
    }
  } catch (e) {
    console.error('Failed to get quality check detail', e);
  }
}

// Create dialog
function openCreateDialog() {
  createOrderNo.value = '';
  createForm.value = {
    orderId: '',
    serviceLogId: '',
    checkType: 'RANDOM',
    checkMethod: 'PHOTO_REVIEW',
    checkScore: 100,
    checkResult: 'QUALIFIED',
    checkPhotos: [],
    checkRemark: '',
    needRectify: false,
    rectifyNotice: '',
    rectifyDeadline: ''
  };
  createDialogVisible.value = true;
}

function goToCompleteService(row: Api.Quality.QualityCheck) {
  router.push({
    path: '/business/order',
    query: {
      orderNo: row.orderNo || '',
      staffId: row.staffId || '',
      staffName: row.staffName || ''
    }
  });
}

async function handleCreateSubmit() {
  const orderNo = createOrderNo.value.trim();
  if (!orderNo) {
    message.warning('请输入订单编号');
    return;
  }
  createLoading.value = true;
  try {
    const { data: orderPage, error: orderError } = await fetchGetOrderList({ orderNo, page: 1, pageSize: 10 } as any);
    if (orderError) {
      message.error(orderError.message || '定位订单失败');
      return;
    }
    const order = orderPage?.records?.find(item => item.orderNo === orderNo);
    if (!order) {
      message.warning(`未找到订单：${orderNo}`);
      return;
    }
    createForm.value.orderId = order.orderId;
    const payload = { ...createForm.value };
    if (payload.rectifyDeadline) {
      payload.rectifyDeadline = new Date(payload.rectifyDeadline).toISOString().split('T')[0];
    }
    const { error } = await fetchCreateQualityCheck(payload);
    if (error) {
      message.error(error.message || '创建质检失败');
      return;
    }
    message.success('创建质检成功');
    createDialogVisible.value = false;
    getData();
    getStatistics();
  } finally {
    createLoading.value = false;
  }
}

// Rectify submit drawer
const rectifyDrawerVisible = ref(false);
const rectifyData = ref({
  photos: [] as string[],
  remark: ''
});
const rectifyLoading = ref(false);

function showRectifyModal(row: Api.Quality.QualityCheck) {
  qualityDetailData.value = row;
  rectifyData.value = { photos: [], remark: '' };
  rectifyDrawerVisible.value = true;
}

async function handleSubmitRectify() {
  if (!qualityDetailData.value) return;
  rectifyLoading.value = true;
  try {
    const { error } = await fetchSubmitRectify(qualityDetailData.value.qualityCheckId, {
      photos: rectifyData.value.photos,
      remark: rectifyData.value.remark
    });
    if (error) {
      message.error(error.message || '整改提交失败');
      return;
    }
    message.success('整改提交成功');
    rectifyDrawerVisible.value = false;
    getData();
    getStatistics();
  } finally {
    rectifyLoading.value = false;
  }
}

// Recheck drawer
const recheckDrawerVisible = ref(false);
const recheckData = ref({
  result: 'PASSED',
  remark: ''
});
const recheckLoading = ref(false);

// Inspect drawer（质检员执行质检，给出结论）
const inspectDrawerVisible = ref(false);
const inspectData = ref({
  checkScore: 100 as number | undefined,
  checkMethod: 'PHOTO_REVIEW',
  checkResult: 'QUALIFIED',
  checkRemark: '',
  checkPhotos: [] as string[],
  rectifyNotice: '',
  rectifyDeadline: null as number | null
});
const inspectLoading = ref(false);

function showRecheckModal(row: Api.Quality.QualityCheck) {
  qualityDetailData.value = row;
  recheckData.value = { result: 'PASSED', remark: '' };
  recheckDrawerVisible.value = true;
}

// 显示执行质检抽屉
function showInspectModal(row: Api.Quality.QualityCheck) {
  qualityDetailData.value = row;
  inspectData.value = {
    checkScore: row.checkScore || 100,
    checkMethod: row.checkMethod || 'PHOTO_REVIEW',
    checkResult: 'QUALIFIED',
    checkRemark: '',
    checkPhotos: [],
    rectifyNotice: '',
    rectifyDeadline: null
  };
  inspectDrawerVisible.value = true;
}

// 执行质检
async function handleInspect() {
  if (!qualityDetailData.value) return;
  inspectLoading.value = true;
  try {
    const payload: any = {
      checkScore: inspectData.value.checkScore,
      checkMethod: inspectData.value.checkMethod,
      checkResult: inspectData.value.checkResult,
      checkRemark: inspectData.value.checkRemark,
      // checkPhotos 后端是 string，前端传逗号分隔
      checkPhotos: inspectData.value.checkPhotos.join(',') || ''
    };
    if (inspectData.value.checkResult === 'NEED_RECTIFY') {
      if (!inspectData.value.rectifyNotice) {
        message.warning('需整改时，整改通知必填');
        return;
      }
      payload.rectifyNotice = inspectData.value.rectifyNotice;
      if (inspectData.value.rectifyDeadline) {
        payload.rectifyDeadline = new Date(inspectData.value.rectifyDeadline).toISOString();
      }
    }
    const { error } = await fetchInspect(qualityDetailData.value.qualityCheckId, payload);
    if (error) {
      message.error(error.message || '执行质检失败');
      return;
    }
    message.success('执行质检成功');
    inspectDrawerVisible.value = false;
    qualityDetailDrawerVisible.value = false;
    getData();
    getStatistics();
  } finally {
    inspectLoading.value = false;
  }
}

async function handleRecheck() {
  if (!qualityDetailData.value) return;
  recheckLoading.value = true;
  try {
    const { error } = await fetchRecheck(qualityDetailData.value.qualityCheckId, {
      result: recheckData.value.result,
      remark: recheckData.value.remark
    });
    if (error) {
      message.error(error.message || '复检失败');
      return;
    }
    message.success('复检成功');
    recheckDrawerVisible.value = false;
    getData();
    getStatistics();
  } finally {
    recheckLoading.value = false;
  }
}

// Check result options
const resultOptions = [
  { label: '待质检', value: 'PENDING' },
  { label: '合格', value: 'QUALIFIED' },
  { label: '不合格', value: 'UNQUALIFIED' },
  { label: '需整改', value: 'NEED_RECTIFY' }
];

// Check type options
const checkTypeOptions = [
  { label: '随机抽检', value: 'RANDOM' },
  { label: '计划抽检', value: 'SCHEDULED' },
  { label: '投诉抽检', value: 'COMPLAINT' },
  { label: '完工抽检', value: 'COMPLETION' }
];

// Check method options
const checkMethodOptions = [
  { label: '照片审核', value: 'PHOTO_REVIEW' },
  { label: '电话回访', value: 'PHONE_REVIEW' },
  { label: '现场检查', value: 'ON_SITE' }
];

function getResultType(result: Api.Quality.CheckResult): 'success' | 'error' | 'warning' | 'default' {
  const map: Record<string, 'success' | 'error' | 'warning' | 'default'> = {
    QUALIFIED: 'success',
    UNQUALIFIED: 'error',
    NEED_RECTIFY: 'warning'
  };
  return map[result] || 'default';
}

function getResultLabel(result: string): string {
  const option = resultOptions.find(o => o.value === result);
  return option?.label || result;
}

function getCheckTypeLabel(checkType: string): string {
  const option = checkTypeOptions.find(o => o.value === checkType);
  return option?.label || checkType;
}

function getCheckMethodLabel(checkMethod: string): string {
  const option = checkMethodOptions.find(o => o.value === checkMethod);
  return option?.label || checkMethod;
}

const tableColumns: DataTableColumns<Api.Quality.QualityCheck> = [
  { type: 'selection' },
  { title: '质检编号', key: 'checkNo', width: 160 },
  { title: '订单号', key: 'orderNo', width: 160 },
  { title: '服务商', key: 'providerName', width: 150 },
  {
    title: '服务人员',
    key: 'staffName',
    width: 100,
    render: row =>
      row.staffName
        ? h('a', { style: { color: '#18a058', cursor: 'pointer' }, onClick: () => showStaffDetail(row) }, row.staffName)
        : '-'
  },
  { title: '质检类型', key: 'checkType', width: 100, render: row => getCheckTypeLabel(row.checkType) },
  { title: '质检方式', key: 'checkMethod', width: 100, render: row => getCheckMethodLabel(row.checkMethod) },
  { title: '综合评分', key: 'checkScore', width: 100 },
  {
    title: '质检结果',
    key: 'checkResult',
    width: 100,
    render: row =>
      h(NTag, { type: getResultType(row.checkResult), size: 'small' }, () => getResultLabel(row.checkResult))
  },
  {
    title: '整改状态',
    key: 'rectifyStatus',
    width: 100,
    render: row => {
      const statusMap: Record<string, string> = {
        PENDING: '待整改',
        IN_PROGRESS: '整改中',
        RECHECK: '待复检',
        COMPLETED: '已完成',
        VERIFIED: '已通过',
        FAILED: '未通过'
      };
      return statusMap[row.rectifyStatus || ''] || '-';
    }
  },
  { title: '质检时间', key: 'checkTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: row => {
      const actions = [];
      actions.push(
        h(
          NButton,
          { size: 'small', type: 'primary', onClick: () => showQualityDetail(row), style: { marginRight: '8px' } },
          () => '查看'
        )
      );
      // 如果需要整改且未提交整改，显示整改按钮（支持 UNQUALIFIED 和 NEED_RECTIFY）
      if ((row.checkResult === 'NEED_RECTIFY' || row.checkResult === 'UNQUALIFIED') && row.rectifyStatus === 'PENDING') {
        actions.push(
          h(
            NButton,
            { size: 'small', type: 'warning', onClick: () => showRectifyModal(row), style: { marginRight: '8px' } },
            () => '提交整改'
          )
        );
      }
      // 复检不通过后可重新整改
      if (row.rectifyStatus === 'FAILED') {
        actions.push(
          h(
            NButton,
            { size: 'small', type: 'warning', onClick: () => showRectifyModal(row), style: { marginRight: '8px' } },
            () => '重新整改'
          )
        );
      }
      // 如果整改状态为待复检，显示复检按钮
      if (row.rectifyStatus === 'RECHECK') {
        actions.push(
          h(
            NButton,
            { size: 'small', type: 'success', onClick: () => showRecheckModal(row), style: { marginRight: '8px' } },
            () => '复检'
          )
        );
      }
      // 质检合格后可完成服务
      if (row.checkResult === 'QUALIFIED') {
        actions.push(
          h(
            NButton,
            { size: 'small', type: 'info', onClick: () => goToCompleteService(row), style: { marginRight: '8px' } },
            () => '去完成服务'
          )
        );
      }
      // 待质检状态：显示执行质检按钮
      if (row.checkResult === 'PENDING') {
        actions.push(
          h(
            NButton,
            { size: 'small', type: 'warning', onClick: () => showInspectModal(row), style: { marginRight: '8px' } },
            () => '执行质检'
          )
        );
      }
      return h(NSpace, null, () => actions);
    }
  }
];

async function getStatistics() {
  try {
    const { data } = await fetchGetQualityStatistics();
    if (data) {
      statistics.value = data;
    }
  } catch (e) {
    console.error('Failed to get statistics', e);
  }
}

// Use framework's table hook
const tableHookResult = useNaivePaginatedTable<Api.Common.PaginatingQueryRecord<Api.Quality.QualityCheck>, Api.Quality.QualityCheck>({
  apiFn: async params => {
    const queryParams: any = {
      current: params.page,
      pageSize: params.pageSize
    };
    if (searchOrderNo.value) queryParams.orderNo = searchOrderNo.value;
    if (searchProviderName.value) queryParams.providerName = searchProviderName.value;
    if (searchStaffName.value) queryParams.staffName = searchStaffName.value;
    if (searchCheckResult.value) queryParams.checkResult = searchCheckResult.value;
    if (searchDateRange.value) {
      queryParams.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      queryParams.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }
    return fetchGetQualityCheckList(queryParams);
  },
  apiParams: {
    page: 1,
    pageSize: 10
  },
  transform: defaultTransform,
  columns: () => tableColumns
});

const {
  data: tableData,
  loading,
  pagination,
  mobilePagination,
  getData,
  getDataByPage,
  columns: filteredColumns,
  columnChecks
} = tableHookResult;

// Table checked row keys
const checkedRowKeys = ref<string[]>([]);

function handleResetSearch() {
  searchOrderNo.value = '';
  searchProviderName.value = '';
  searchStaffName.value = '';
  searchCheckResult.value = '';
  searchDateRange.value = null;
  getDataByPage(1);
}

async function handleBatchDelete() {
  if (!checkedRowKeys.value.length) return;
  try {
    await fetchBatchDeleteQualityCheck(checkedRowKeys.value);
    message.success('批量删除成功');
    checkedRowKeys.value = [];
    await getData();
    await getStatistics();
  } catch (e: any) {
    console.error('Batch delete error:', e);
    const errMsg = e?.message || e?.response?.data?.message || '批量删除失败';
    message.error(errMsg);
  }
}

onMounted(async () => {
  // 接收服务日志跳转来的订单号参数
  if (route.query.orderNo) {
    searchOrderNo.value = String(route.query.orderNo);
    createOrderNo.value = String(route.query.orderNo);
    message.info(`已定位到订单：${createOrderNo.value}`);
  }
  // 接收服务日志跳转来的serviceLogId（预填创建表单并自动打开创建对话框）
  if (route.query.serviceLogId) {
    createForm.value.serviceLogId = String(route.query.serviceLogId);
    createDialogVisible.value = true;
  }
  // 接收质检详情跳转参数（订单详情→质检详情），直接打开该质检详情抽屉
  if (route.query.qcId) {
    const qcId = String(route.query.qcId);
    try {
      const { data } = await fetchGetQualityCheck(qcId);
      if (data) {
        qualityDetailData.value = data;
        qualityDetailDrawerVisible.value = true;
      }
    } catch (e) {
      console.error('Failed to load quality check detail from route', e);
    }
  }
  getStatistics();
  getData();
});
</script>

<template>
  <div>
    <!-- Statistics Cards -->
    <NCard title="质检统计" :bordered="false" style="margin-bottom: 16px">
      <div class="statistics-grid">
        <div class="stat-card stat-primary">
          <div class="stat-label">总质检数</div>
          <div class="stat-value">{{ statistics.total }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">合格数</div>
          <div class="stat-value">{{ statistics.qualifiedCount }}</div>
        </div>
        <div class="stat-card stat-error">
          <div class="stat-label">不合格数</div>
          <div class="stat-value">{{ statistics.unqualifiedCount }}</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-label">需整改数</div>
          <div class="stat-value">{{ statistics.needRectifyCount }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">合格率</div>
          <div class="stat-value">{{ formatPercent(statistics.qualifiedRate, false) }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">平均评分</div>
          <div class="stat-value">{{ formatScore(statistics.avgScore) }}</div>
        </div>
      </div>
    </NCard>

    <!-- 流程指示器 -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <FlowIndicator
        :current-step="currentFlowStep"
        :steps="flowSteps"
        style="border-radius: 12px"
      />
    </NCard>

    <!-- Table -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>质检管理</span>
        </div>
      </template>
      <div style="background: #f5f5f5; padding: 12px; margin-bottom: 12px; border-radius: 4px">
        <NSpace :wrap="true" align="center">
          <NInput v-model:value="searchOrderNo" placeholder="订单号" clearable style="width: 150px" />
          <NInput v-model:value="searchProviderName" placeholder="服务商名称" clearable style="width: 150px" />
          <NInput v-model:value="searchStaffName" placeholder="服务人员" clearable style="width: 100px" />
          <NSelect
            v-model:value="searchCheckResult"
            :options="resultOptions"
            placeholder="质检结果"
            clearable
            style="width: 120px"
          />
          <NDatePicker v-model:value="searchDateRange" type="daterange" clearable style="width: 260px" />
          <NButton type="primary" @click="getData">搜索</NButton>
          <NButton @click="handleResetSearch">重置</NButton>
        </NSpace>
      </div>

      <TableHeaderOperation
        v-model:columns="columnChecks"
        :disabled-delete="checkedRowKeys.length === 0"
        :loading="loading"
        @add="openCreateDialog"
        @refresh="getData"
        @delete="handleBatchDelete"
      />

      <NDataTable
        :columns="filteredColumns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1400"
        :row-key="(row: Api.Quality.QualityCheck) => row.qualityCheckId"
        v-model:checked-row-keys="checkedRowKeys"
        remote
        :pagination="mobilePagination"
      />
    </NCard>

    <!-- Staff Detail Drawer -->
    <NDrawer v-model:show="staffDetailDrawerVisible" :width="500" placement="right" closable>
      <NDrawerContent title="服务人员详情" closable>
        <NForm v-if="staffDetailData" label-placement="left" label-width="100">
          <NFormItem label="姓名">{{ staffDetailData.staffName }}</NFormItem>
          <NFormItem label="性别">{{ Number(staffDetailData.gender) === 1 ? "男" : "女" }}</NFormItem>
          <NFormItem label="工号">{{ staffDetailData.staffNo || '-' }}</NFormItem>
          <NFormItem label="手机号">{{ staffDetailData.phone || '-' }}</NFormItem>
          <NFormItem label="身份证号">{{ staffDetailData.idCard || '-' }}</NFormItem>
          <NFormItem label="所属服务商">{{ staffDetailData.providerName || '-' }}</NFormItem>
          <NFormItem label="服务类型">{{ staffDetailData.serviceTypes || '-' }}</NFormItem>
          <NFormItem label="紧急联系人">{{ staffDetailData.emergencyContact || '-' }}</NFormItem>
          <NFormItem label="紧急联系电话">{{ staffDetailData.emergencyPhone || '-' }}</NFormItem>
        </NForm>
      </NDrawerContent>
    </NDrawer>

    <!-- Quality Check Detail Drawer -->
    <NDrawer v-model:show="qualityDetailDrawerVisible" :width="560" placement="right" closable>
      <NDrawerContent title="质检详情" closable>
        <NForm v-if="qualityDetailData" label-placement="left" label-width="100">
          <NFormItem label="质检编号">{{ qualityDetailData.checkNo }}</NFormItem>
          <NFormItem label="订单号">{{ qualityDetailData.orderNo || '-' }}</NFormItem>
          <NFormItem label="服务商">{{ qualityDetailData.providerName || '-' }}</NFormItem>
          <NFormItem label="服务人员">{{ qualityDetailData.staffName || '-' }}</NFormItem>
          <NFormItem label="服务类别">{{ qualityDetailData.serviceCategory || '-' }}</NFormItem>

          <!-- 服务日志信息 -->
          <NDivider>服务日志信息</NDivider>
          <NFormItem label="服务日期">{{ serviceLogDetail?.serviceDate || '-' }}</NFormItem>
          <NFormItem label="服务开始时间">{{ serviceLogDetail?.serviceStartTime || '-' }}</NFormItem>
          <NFormItem label="服务结束时间">{{ serviceLogDetail?.serviceEndTime || '-' }}</NFormItem>
          <NFormItem label="服务时长">{{ serviceLogDetail?.serviceDuration ? serviceLogDetail.serviceDuration + '分钟' : '-' }}</NFormItem>
          <NFormItem label="服务内容">{{ serviceLogDetail?.serviceContent || '-' }}</NFormItem>
          <NFormItem label="健康观察">{{ serviceLogDetail?.healthObservations || '-' }}</NFormItem>
          <NFormItem label="给药记录">{{ serviceLogDetail?.medicationGiven || '-' }}</NFormItem>
          <NFormItem label="服务照片" v-if="serviceLogDetail?.servicePhotoList && serviceLogDetail.servicePhotoList.length">
            <NImageGroup>
              <NSpace>
                <template v-for="photo in serviceLogDetail.servicePhotoList" :key="photo">
                  <NImage :src="photo" width="80" height="80" object-fit="cover" style="border-radius: 4px; border: 1px solid #e6e6e6" />
                </template>
              </NSpace>
            </NImageGroup>
          </NFormItem>
          <NFormItem label="服务照片" v-else-if="serviceLogDetail?.servicePhotos">
            <NImageGroup>
              <NSpace>
                <template v-for="photo in (Array.isArray(serviceLogDetail.servicePhotos) ? serviceLogDetail.servicePhotos : String(serviceLogDetail.servicePhotos).split(','))" :key="photo">
                  <NImage :src="photo" width="80" height="80" object-fit="cover" style="border-radius: 4px; border: 1px solid #e6e6e6" />
                </template>
              </NSpace>
            </NImageGroup>
          </NFormItem>
          <NFormItem label="服务照片" v-else>-</NFormItem>

          <NFormItem label="质检类型">{{ getCheckTypeLabel(qualityDetailData.checkType) }}</NFormItem>
          <NFormItem label="质检方式">{{ getCheckMethodLabel(qualityDetailData.checkMethod) }}</NFormItem>
          <NFormItem label="综合评分">{{ qualityDetailData.checkScore }}分</NFormItem>
          <NFormItem label="质检结果">
            <NTag :type="getResultType(qualityDetailData.checkResult)" size="small">
              {{ getResultLabel(qualityDetailData.checkResult) }}
            </NTag>
          </NFormItem>
          <NFormItem label="质检时间">{{ qualityDetailData.checkTime || '-' }}</NFormItem>
          <NFormItem label="质检备注">{{ qualityDetailData.checkRemark || '-' }}</NFormItem>
          <NFormItem label="质检照片" v-if="qualityDetailData.checkPhotos && qualityDetailData.checkPhotos.length">
            <NImageGroup>
              <NSpace>
                <template v-for="photo in qualityDetailData.checkPhotos" :key="photo">
                  <NImage :src="photo" width="80" height="80" object-fit="cover" />
                </template>
              </NSpace>
            </NImageGroup>
          </NFormItem>
          <NFormItem label="整改通知" v-if="qualityDetailData.rectifyNotice">
            {{ qualityDetailData.rectifyNotice }}
          </NFormItem>
          <NFormItem label="整改期限" v-if="qualityDetailData.rectifyDeadline">
            {{ qualityDetailData.rectifyDeadline }}
          </NFormItem>
          <NFormItem label="整改状态">
            {{ { PENDING: '待整改', IN_PROGRESS: '整改中', RECHECK: '待复检', COMPLETED: '已完成', VERIFIED: '已通过', FAILED: '未通过' }[qualityDetailData.rectifyStatus || ''] || '-' }}
          </NFormItem>
          <NFormItem label="整改照片" v-if="qualityDetailData.rectifyPhotos">
            <NImageGroup>
              <NSpace>
                <template v-for="photo in (Array.isArray(qualityDetailData.rectifyPhotos) ? qualityDetailData.rectifyPhotos : (qualityDetailData.rectifyPhotos as string).split(','))" :key="photo">
                  <NImage :src="photo" width="80" height="80" object-fit="cover" />
                </template>
              </NSpace>
            </NImageGroup>
          </NFormItem>
          <NFormItem label="整改说明" v-if="qualityDetailData.rectifyRemark">
            {{ qualityDetailData.rectifyRemark }}
          </NFormItem>
          <NFormItem label="复检结果" v-if="qualityDetailData.recheckResult">
            {{ qualityDetailData.recheckResult === 'PASSED' ? '通过' : '不通过' }}
          </NFormItem>
          <NFormItem label="复检时间" v-if="qualityDetailData.recheckTime">
            {{ qualityDetailData.recheckTime }}
          </NFormItem>
          <NFormItem label="创建时间">{{ qualityDetailData.createTime }}</NFormItem>
        </NForm>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="qualityDetailDrawerVisible = false">关闭</NButton>
            <!-- 待质检状态：显示执行质检按钮 -->
            <NButton
              v-if="qualityDetailData?.checkResult === 'PENDING'"
              type="warning"
              @click="showInspectModal(qualityDetailData)"
            >
              执行质检
            </NButton>
            <NButton
              type="success"
              @click="router.push({ path: '/business/evaluation', query: { orderNo: qualityDetailData?.orderNo || '', qualityCheckId: qualityDetailData?.qualityCheckId || '', serviceLogId: qualityDetailData?.serviceLogId || '' } })"
            >
              发起满意度评价
            </NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- Rectify Submit Drawer -->
    <NDrawer v-model:show="rectifyDrawerVisible" :width="480" placement="right" closable>
      <NDrawerContent title="提交整改" closable>
        <NForm label-placement="left" label-width="80">
          <NFormItem label="整改说明">
            <NInput
              v-model:value="rectifyData.remark"
              type="textarea"
              placeholder="请输入整改说明"
              :rows="3"
            />
          </NFormItem>
          <NFormItem label="整改照片">
            <div style="color: #999; font-size: 12px">照片上传功能开发中</div>
          </NFormItem>
        </NForm>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="rectifyDrawerVisible = false">取消</NButton>
            <NButton type="primary" :loading="rectifyLoading" @click="handleSubmitRectify">
              提交
            </NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- Recheck Drawer -->
    <NDrawer v-model:show="recheckDrawerVisible" :width="480" placement="right" closable>
      <NDrawerContent title="复检" closable>
        <NForm label-placement="left" label-width="80">
          <NFormItem label="复检结果">
            <NSelect
              v-model:value="recheckData.result"
              :options="[
                { label: '通过', value: 'PASSED' },
                { label: '不通过', value: 'FAILED' }
              ]"
              style="width: 200px"
            />
          </NFormItem>
          <NFormItem label="复检备注">
            <NInput
              v-model:value="recheckData.remark"
              type="textarea"
              placeholder="请输入复检备注"
              :rows="3"
            />
          </NFormItem>
        </NForm>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="recheckDrawerVisible = false">取消</NButton>
            <NButton type="primary" :loading="recheckLoading" @click="handleRecheck">
              提交
            </NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- Inspect Drawer：质检员执行质检，给出合格/不合格/需整改结论 -->
    <NDrawer v-model:show="inspectDrawerVisible" :width="520" placement="right" closable>
      <NDrawerContent title="执行质检" closable>
        <NForm label-placement="left" label-width="90">
          <!-- 质检单基本信息（只读） -->
          <NFormItem label="质检编号">{{ qualityDetailData?.checkNo }}</NFormItem>
          <NFormItem label="订单号">{{ qualityDetailData?.orderNo }}</NFormItem>
          <NFormItem label="服务类别">{{ qualityDetailData?.serviceCategory }}</NFormItem>

          <NDivider title-placement="left">质检结论</NDivider>

          <NFormItem label="质检方式" required>
            <NSelect
              v-model:value="inspectData.checkMethod"
              :options="checkMethodOptions"
              style="width: 200px"
            />
          </NFormItem>

          <NFormItem label="综合评分" required>
            <NInputNumber
              v-model:value="inspectData.checkScore"
              :min="0"
              :max="100"
              style="width: 120px"
            />
            <span style="margin-left: 8px; color: #888">分（0-100）</span>
          </NFormItem>

          <NFormItem label="质检结论" required>
            <NSelect
              v-model:value="inspectData.checkResult"
              :options="resultOptions"
              style="width: 200px"
            />
          </NFormItem>

          <NFormItem label="质检备注">
            <NInput
              v-model:value="inspectData.checkRemark"
              type="textarea"
              placeholder="请输入质检备注，说明检查依据"
              :rows="3"
            />
          </NFormItem>

          <!-- 需整改时显示整改信息 -->
          <template v-if="inspectData.checkResult === 'NEED_RECTIFY'">
            <NDivider title-placement="left">整改通知</NDivider>
            <NFormItem label="整改通知" required>
              <NInput
                v-model:value="inspectData.rectifyNotice"
                type="textarea"
                placeholder="请输入整改要求，告知服务商需要整改的内容"
                :rows="3"
              />
            </NFormItem>
            <NFormItem label="整改期限">
              <NDatePicker
                v-model:value="inspectData.rectifyDeadline"
                type="date"
                style="width: 200px"
                clearable
              />
            </NFormItem>
          </template>

          <NAlert type="info" style="margin-top: 8px">
            <template v-if="inspectData.checkResult === 'QUALIFIED'">
              合格：服务日志→已通过，订单→已完成
            </template>
            <template v-else-if="inspectData.checkResult === 'UNQUALIFIED'">
              不合格：服务终止，进入整改流程
            </template>
            <template v-else>
              需整改：开启整改流程，服务商提交整改后需复检
            </template>
          </NAlert>
        </NForm>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="inspectDrawerVisible = false">取消</NButton>
            <NButton type="primary" :loading="inspectLoading" @click="handleInspect">
              确认提交
            </NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- Create Quality Check Dialog -->
    <NModal
      v-model:show="createDialogVisible"
      preset="card"
      title="新增质检"
      style="width: 560px"
      :segmented="{ content: true, footer: true }"
    >
      <NForm label-placement="left" label-width="100">
        <NFormItem label="订单编号" required>
          <NInput v-model:value="createOrderNo" placeholder="请输入订单编号" />
        </NFormItem>
        <NFormItem label="质检类型">
          <NSelect
            v-model:value="createForm.checkType"
            :options="checkTypeOptions"
            style="width: 200px"
          />
        </NFormItem>
        <NFormItem label="质检方式">
          <NSelect
            v-model:value="createForm.checkMethod"
            :options="checkMethodOptions"
            style="width: 200px"
          />
        </NFormItem>
        <NFormItem label="综合评分">
          <NInputNumber v-model:value="createForm.checkScore" :min="0" :max="100" style="width: 120px" />
        </NFormItem>
        <NFormItem label="质检结果">
          <NSelect
            v-model:value="createForm.checkResult"
            :options="resultOptions"
            style="width: 200px"
          />
        </NFormItem>
        <NFormItem label="质检备注">
          <NInput
            v-model:value="createForm.checkRemark"
            type="textarea"
            placeholder="请输入质检备注"
            :rows="3"
          />
        </NFormItem>
        <NFormItem label="需要整改">
          <NSwitch v-model:value="createForm.needRectify" />
        </NFormItem>
        <template v-if="createForm.needRectify">
          <NFormItem label="整改通知">
            <NInput v-model:value="createForm.rectifyNotice" placeholder="请输入整改通知" />
          </NFormItem>
          <NFormItem label="整改期限">
            <NDatePicker v-model:value="createForm.rectifyDeadline" type="date" style="width: 200px" />
          </NFormItem>
        </template>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="createDialogVisible = false">取消</NButton>
          <NButton type="primary" :loading="createLoading" @click="handleCreateSubmit">
            提交
          </NButton>
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
  background: linear-gradient(135deg, #5E8B7E 0%, #7BA89F 100%);
  color: white;
}

.stat-primary .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-info {
  background: linear-gradient(135deg, #3A506B 0%, #5A7A8B 100%);
  color: white;
}

.stat-info .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-success {
  background: linear-gradient(135deg, #5E8B7E 0%, #81B29A 100%);
  color: white;
}

.stat-success .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-warning {
  background: linear-gradient(135deg, #F9B572 0%, #F7C59F 100%);
  color: white;
}

.stat-warning .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-error {
  background: linear-gradient(135deg, #D64045 0%, #E07070 100%);
  color: white;
}

.stat-error .stat-label {
  color: rgba(255, 255, 255, 0.85);
}
</style>
