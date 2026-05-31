<script setup lang="ts">
import { ref, h, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  NButton,
  NCard,
  NTag,
  NSpace,
  NInput,
  NSelect,
  NDatePicker,
  NForm,
  NFormItem,
  useMessage,
  NInputNumber,
  NTimeline,
  NTimelineItem,
  NDescriptions,
  NDescriptionsItem,
  NDrawer,
  NDrawerContent,
  NPopconfirm,
  NBadge,
  NPagination
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import {
  fetchGetOrderList,
  fetchGetOrder,
  fetchDeleteOrder,
  fetchDispatchOrder,
  fetchAcceptOrder,
  fetchStartOrder,
  fetchCompleteOrder,
  fetchCancelOrder,
  fetchGetOrderStatistics,
  fetchGetElder,
  fetchGetStaff,
  fetchGetProviderOptions,
  fetchGetStaffList,
  fetchGetServiceLogByOrderId,
  fetchGetEvaluationByOrderId,
  fetchGetQualityCheckByOrderId,
  fetchCreateEvaluation,
  fetchBatchDeleteOrder
} from '@/service/api';
import {
  fetchGetContractByOrderId,
  fetchGetSignUrl
} from '@/service/api';
import { useRouterPush } from '@/hooks/common/router';
import { useNaivePaginatedTable, useTableOperate, defaultTransform } from '@/hooks/common/table';
import { useNaiveForm } from '@/hooks/common/form';
import { useAuth } from '@/hooks/business/auth';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';
import OrderDetailDrawer from './components/OrderDetailDrawer.vue';
import OrderAddModal from './components/OrderAddModal.vue';

defineOptions({
  name: 'BusinessOrder'
});

const message = useMessage();
const route = useRoute();
const router = useRouter();
const { routerPushByKey } = useRouterPush();
const { hasAuth } = useAuth();
const { formRef, validate } = useNaiveForm();

// Statistics
const statistics = ref<Api.Order.Statistics>({
  total: 0,
  today: 0,
  month: 0,
  pendingDispatch: 0,
  dispatched: 0,
  received: 0,
  inService: 0,
  completed: 0,
  cancelled: 0,
  completionRate: 0,
  cancelRate: 0,
  totalEstimatedPrice: 0,
  totalActualPrice: 0,
  totalSubsidy: 0,
  totalSelfPay: 0,
  staffRankings: []
});

// Search
const searchOrderNo = ref('');
const searchElderName = ref('');
const searchProviderId = ref('');
const searchServiceType = ref('');
const searchStatus = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Provider options
const providerOptions = ref<{ label: string; value: string }[]>([]);

// Service type options
const serviceTypeOptions = [
  { label: '生活照料', value: '01' },
  { label: '日间照料', value: '02' },
  { label: '助餐服务', value: '03' },
  { label: '助洁服务', value: '04' },
  { label: '助浴服务', value: '05' },
  { label: '康复护理', value: '06' },
  { label: '精神慰藉', value: '07' },
  { label: '健康管理', value: '08' },
  { label: '信息咨询', value: '09' }
];

// Status options
const statusOptions = [
  { label: '待派单', value: 'CREATED' },
  { label: '已派单', value: 'DISPATCHED' },
  { label: '已接单', value: 'RECEIVED' },
  { label: '服务中', value: 'SERVICE_STARTED' },
  { label: '已完成', value: 'SERVICE_COMPLETED' },
  { label: '已评价', value: 'EVALUATED' },
  { label: '已结算', value: 'SETTLED' }
];

async function getProviderOptions() {
  try {
    const data = await fetchGetProviderOptions();
    if (data) {
      providerOptions.value = data.map((item: { id: string; name: string }) => ({
        label: item.name,
        value: item.id
      }));
    }
  } catch (e) {
    console.error('Failed to get provider options', e);
  }
}

function getStatusType(status: string): 'warning' | 'success' | 'info' | 'error' | 'default' {
  const map: Record<string, 'warning' | 'success' | 'info' | 'error' | 'default'> = {
    CREATED: 'warning',
    DISPATCHED: 'info',
    RECEIVED: 'info',
    SERVICE_STARTED: 'info',
    SERVICE_COMPLETED: 'success',
    EVALUATED: 'success',
    SETTLED: 'success',
    CANCELLED: 'error',
    REJECTED: 'error',
    COMPLETED: 'success'
  };
  return map[status] || 'default';
}

function getStatusLabel(status: string): string {
  const option = statusOptions.find(o => o.value === status);
  return option?.label || status;
}

// 审核状态标签
function getAuditStatusLabel(status: string): string {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    SUBMITTED: '已提交',
    APPROVED: '已通过',
    REJECTED: '已驳回'
  };
  return map[status] || status;
}

// 审核状态颜色
function getAuditStatusType(status: string): 'warning' | 'success' | 'info' | 'error' | 'default' {
  const map: Record<string, 'warning' | 'success' | 'info' | 'error' | 'default'> = {
    DRAFT: 'default',
    SUBMITTED: 'warning',
    APPROVED: 'success',
    REJECTED: 'error'
  };
  return map[status] || 'default';
}

// 质检结果标签
function getQualityResultLabel(result: string): string {
  const map: Record<string, string> = {
    PENDING: '待质检',
    QUALIFIED: '合格',
    UNQUALIFIED: '不合格',
    NEED_RECTIFY: '需整改'
  };
  return map[result] || result;
}

// 质检结果颜色
function getQualityResultType(result: string): 'warning' | 'success' | 'error' | 'info' | 'default' {
  const map: Record<string, 'warning' | 'success' | 'error' | 'info' | 'default'> = {
    PENDING: 'warning',
    QUALIFIED: 'success',
    UNQUALIFIED: 'error',
    NEED_RECTIFY: 'error'
  };
  return map[result] || 'default';
}

// 时间轴相关函数 - 参考预约单设计
interface OrderTimelineItem {
  status: string;
  time: string;
  description: string;
  operator?: string;
  details?: { label: string; value: string }[];
  action?: {
    label: string;
    icon: string;
    onClick: () => void;
  };
  // 关联的服务日志信息
  serviceLog?: {
    auditStatus: string;
    auditStatusLabel: string;
    reviewComment?: string;
    reviewerName?: string;
    reviewTime?: string;
    // 质检信息
    qualityCheck?: {
      checkResult: string;
      checkResultLabel: string;
      checkRemark?: string;
    };
  };
  // 关联的评价信息
  evaluation?: {
    overallScore: number;
    evaluationContent?: string;
    evaluationTime?: string;
  };
}

function formatTimelineTime(time: string): string {
  if (!time) return '-';
  const date = new Date(time);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

function getRelativeTime(time: string): string {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60));
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60));
      return minutes <= 1 ? '刚刚' : `${minutes}分钟前`;
    }
    return `${hours}小时前`;
  } else if (days === 1) {
    return '昨天';
  } else if (days < 7) {
    return `${days}天前`;
  } else if (days < 30) {
    return `${Math.floor(days / 7)}周前`;
  }
  return `${Math.floor(days / 30)}月前`;
}

// 格式化预约服务时间显示
function formatServiceTime(serviceDate: string | undefined, serviceTime: string | undefined): string {
  if (!serviceDate) return serviceTime || '-';
  try {
    const date = new Date(serviceDate);
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    let timePeriod = '';
    if (serviceTime) {
      const hourStr = serviceTime.split(':')[0];
      const hour = parseInt(hourStr, 10);
      if (hour < 12) {
        timePeriod = '上午';
      } else {
        timePeriod = '下午';
      }
    }
    return `${year}年${month}月${day}日 ${timePeriod}`;
  } catch (e) {
    return serviceTime || '-';
  }
}

// Table columns
const tableColumns: DataTableColumns<Api.Order.Order> = [
  { type: 'selection' },
  { title: '订单号', key: 'orderNo', width: 160 },
  {
    title: '客户姓名',
    key: 'elderName',
    width: 100,
    render: row =>
      h('a', { style: { color: '#18a058', cursor: 'pointer' }, onClick: () => showElderDetail(row) }, row.elderName)
  },
  { title: '客户手机', key: 'elderPhone', width: 130 },
  { title: '服务类型', key: 'serviceTypeName', width: 120 },
  {
    title: '预约服务时间',
    key: 'serviceTime',
    width: 170,
    render: row => formatServiceTime(row.serviceDate, row.serviceTime)
  },
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
  {
    title: '订单状态',
    key: 'status',
    width: 100,
    render: row => h(NTag, { type: getStatusType(row.status), size: 'small' }, () => getStatusLabel(row.status))
  },
  { title: '补贴金额', key: 'subsidyAmount', width: 100, render: row => `¥${row.subsidyAmount || 0}` },
  { title: '自付金额', key: 'selfPayAmount', width: 100, render: row => `¥${row.selfPayAmount || 0}` },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 300,
    fixed: 'right',
    render: row => {
      const buttons: ReturnType<typeof h>[] = [];
      // 详情按钮 - 始终显示
      buttons.push(h(NButton, { size: 'small', onClick: () => handleDetail(row) }, () => '详情'));
      if ((row.status === 'CREATED' || row.status === 'PENDING') && hasAuth('order:list:dispatch')) {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleAssign(row) }, () => '分配'));
      }
      if (row.status === 'DISPATCHED') {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleAccept(row) }, () => '接单'));
      }
      if (row.status === 'RECEIVED') {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleStart(row) }, () => '开始服务'));
      }
      if (row.status === 'SERVICE_STARTED') {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleComplete(row) }, () => '完成服务'));
      }
      // 已完成订单支持删除
      if (
        (row.status === 'SERVICE_COMPLETED' || row.status === 'EVALUATED' || row.status === 'SETTLED') &&
        hasAuth('order:list:delete')
      ) {
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
          )
        );
      }
      if (
        row.status !== 'SERVICE_COMPLETED' &&
        row.status !== 'EVALUATED' &&
        row.status !== 'SETTLED' &&
        row.status !== 'CANCELLED' &&
        row.status !== 'REJECTED' &&
        hasAuth('order:list:cancel')
      ) {
        buttons.push(h(NButton, { size: 'small', type: 'error', onClick: () => handleCancel(row) }, () => '退回'));
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
  getData,
  getDataByPage
} = useNaivePaginatedTable<Api.Common.PaginatingQueryRecord<Api.Order.Order>, Api.Order.Order>({
  apiFn: async params => {
    const queryParams: any = {
      page: params.page,
      pageSize: params.pageSize
    };
    if (searchOrderNo.value) queryParams.orderNo = searchOrderNo.value;
    if (searchElderName.value) queryParams.elderName = searchElderName.value;
    if (searchProviderId.value) queryParams.providerId = searchProviderId.value;
    if (searchServiceType.value) queryParams.serviceTypeCode = searchServiceType.value;
    if (searchStatus.value) queryParams.status = searchStatus.value;
    if (searchDateRange.value) {
      queryParams.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      queryParams.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }
    return fetchGetOrderList(queryParams);
  },
  apiParams: {
    page: 1,
    pageSize: 10
  },
  transform: defaultTransform,
  columns: () => tableColumns
});

const { checkedRowKeys } = useTableOperate(tableData, 'orderId', getData);

// Add modal
const addModalVisible = ref(false);

function handleAdd() {
  addModalVisible.value = true;
}

// Assign modal
const assignModalVisible = ref(false);
const assignLoading = ref(false);
const assignForm = ref({ staffId: '', providerId: '' });
const currentOrderId = ref('');
const currentOrderProviderId = ref('');
const currentOrderProviderName = ref('');
const staffOptions = ref<{ label: string; value: string; phone?: string; serviceTypes?: string }[]>([]);
const selectedStaffDetail = ref<Api.Staff.Staff | null>(null);
const loadingStaff = ref(false);
const assignFormRules = {
  staffId: [{ required: true, message: '请选择服务人员', trigger: 'change' }]
};

// Cancel modal
const cancelModalVisible = ref(false);
const cancelForm = ref({ reason: '' });

// Complete modal
const completeModalVisible = ref(false);
const completeForm = ref({ actualFee: 0, subsidyAmount: 0, selfPayAmount: 0 });

// 实际收费变化时自动计算自付金额
watch(
  () => completeForm.value.actualFee,
  val => {
    if (completeModalVisible.value) {
      completeForm.value.selfPayAmount = Math.max(0, val - completeForm.value.subsidyAmount);
    }
  }
);

// Elder detail modal
const elderDetailVisible = ref(false);
const elderDetailData = ref<Api.Elder.Elder | null>(null);

// Staff detail modal
const staffDetailVisible = ref(false);
const staffDetailData = ref<Api.Staff.Staff | null>(null);

// Order detail drawer
const orderDetailVisible = ref(false);
const orderDetailData = ref<Api.Order.Order | null>(null);
const orderTimelineData = ref<OrderTimelineItem[]>([]);

// 合同详情弹窗
const contractDetailVisible = ref(false);
const contractDetailData = ref<any>(null);
const contractSignLoading = ref(false);
const contractRefreshLoading = ref(false);

async function showElderDetail(row: Api.Order.Order) {
  if (!row.elderId) return;
  router.push({ path: '/business/elder', query: { elderId: row.elderId } });
}

async function showStaffDetail(row: Api.Order.Order) {
  if (!row.staffId) return;
  router.push({ path: '/business/staff', query: { staffId: row.staffId } });
}

async function getStatistics() {
  try {
    const { data } = await fetchGetOrderStatistics();
    if (data) {
      statistics.value = data;
    }
  } catch (e) {
    console.error('Failed to get statistics', e);
  }
}

async function handleAssign(row: Api.Order.Order) {
  currentOrderId.value = row.orderId;
  currentOrderProviderId.value = row.providerId || '';
  currentOrderProviderName.value = row.providerName || '';
  assignForm.value = { staffId: '', providerId: row.providerId || '' };
  selectedStaffDetail.value = null;
  assignModalVisible.value = true;

  loadingStaff.value = true;
  try {
    const { data } = await fetchGetStaffList({
      providerId: currentOrderProviderId.value,
      page: 1,
      pageSize: 100
    } as any);
    if (data?.records) {
      staffOptions.value = data.records.map((staff: any) => ({
        label: `${staff.staffName || staff.name} - ${staff.phone || '无电话'}`,
        value: staff.staffId,
        phone: staff.phone,
        serviceTypes: staff.serviceTypes || staff.serviceTypeNames || '-'
      }));
    }
  } catch (e) {
    console.error('Failed to get staff list', e);
  } finally {
    loadingStaff.value = false;
  }
}

async function handleStaffSelect(staffId: string) {
  assignForm.value.staffId = staffId;
  try {
    const { data } = await fetchGetStaff(staffId);
    if (data) {
      selectedStaffDetail.value = data;
      assignForm.value.providerId = data.providerId || currentOrderProviderId.value;
    }
  } catch (e) {
    console.error('Failed to get staff detail', e);
  }
}

async function handleAssignSubmit() {
  if (!currentOrderId.value) return;
  try {
    await validate();
    assignLoading.value = true;
    const { error } = await fetchDispatchOrder(currentOrderId.value, assignForm.value);
    if (error) {
      console.error('派单失败:', error);
      const errMsg = error?.message || error?.response?.data?.message || '派单失败';
      message.error(errMsg);
      return;
    }
    // 派单成功后查询合同信息并展示
    try {
      const { data: contractData } = await fetchGetContractByOrderId(currentOrderId.value);
      if (contractData) {
        message.success(`派单成功，合同已创建：${contractData.contractNo}`);
      } else {
        message.success('派单成功');
      }
    } catch {
      message.success('派单成功');
    }
    assignModalVisible.value = false;
    await getData();
    await getStatistics();
  } finally {
    assignLoading.value = false;
  }
}

async function handleAccept(row: Api.Order.Order) {
  try {
    // 先检查合同状态
    const { data: contractData } = await fetchGetContractByOrderId(row.orderId);
    if (contractData && contractData.status !== 'SIGNED' && contractData.status !== 'COMPLETED') {
      // 合同未签署，弹出合同详情让服务人员查看/签署
      contractDetailData.value = contractData;
      contractDetailVisible.value = true;
      return;
    }
    // 合同已签署或无合同，直接接单
    await fetchAcceptOrder(row.orderId, { staffId: row.staffId });
    message.success('接单成功');
    await getData();
    await getStatistics();
  } catch (e: any) {
    console.error('Accept error:', e);
    // 无合同记录，直接接单
    try {
      await fetchAcceptOrder(row.orderId, { staffId: row.staffId });
      message.success('接单成功');
      await getData();
      await getStatistics();
    } catch (e2: any) {
      const errMsg = e2?.message || e2?.response?.data?.message || '接单失败';
      message.error(errMsg);
    }
  }
}

// 跳转到签署页面
async function handleSignContract() {
  if (!contractDetailData.value?.contractId) return;
  contractSignLoading.value = true;
  try {
    const { data: signUrlData } = await fetchGetSignUrl(contractDetailData.value.contractId);
    if (signUrlData?.signUrl) {
      window.open(signUrlData.signUrl, '_blank');
      contractDetailVisible.value = false;
      message.success('请在签署完成后刷新订单状态');
    }
  } catch (e) {
    message.error('获取签署链接失败');
  } finally {
    contractSignLoading.value = false;
  }
}

// 刷新合同状态
async function handleRefreshContractStatus() {
  if (!contractDetailData.value?.contractId) return;
  contractRefreshLoading.value = true;
  try {
    const { data: contractData } = await fetchGetContractByOrderId(contractDetailData.value.orderId);
    if (contractData) {
      contractDetailData.value = contractData;
      if (contractData.status === 'SIGNED' || contractData.status === 'COMPLETED') {
        message.success('合同已签署完成，可以接单了');
      } else {
        message.info('合同状态已刷新');
      }
    }
  } catch (e) {
    message.error('刷新合同状态失败');
  } finally {
    contractRefreshLoading.value = false;
  }
}

// 签署完成后接单
async function handleAcceptAfterSign() {
  if (!contractDetailData.value) return;
  const orderId = contractDetailData.value.orderId;
  contractDetailVisible.value = false;
  try {
    await fetchAcceptOrder(orderId, { staffId: '' });
    message.success('接单成功');
    await getData();
    await getStatistics();
  } catch (e: any) {
    const errMsg = e?.message || e?.response?.data?.message || '接单失败';
    message.error(errMsg);
  }
}

async function handleStart(row: Api.Order.Order) {
  try {
    // 先检查合同状态
    const { data: contractData } = await fetchGetContractByOrderId(row.orderId);
    if (contractData && ['DRAFT', 'INITIATED', 'SIGNING'].includes(contractData.status)) {
      // 合同未签署，弹出合同详情
      message.warning('请先完成合同签署后再开始服务');
      contractDetailData.value = contractData;
      contractDetailVisible.value = true;
      return;
    }
    // 合同已签署或无合同，执行开始服务
    await fetchStartOrder(row.orderId, {});
    message.success('开始服务');
    await getData();
    await getStatistics();
  } catch (e: any) {
    console.error('Start error:', e);
    const errMsg = e?.message || e?.response?.data?.message || '开始服务失败';
    message.error(errMsg);
  }
}

function handleComplete(row: Api.Order.Order) {
  currentOrderId.value = row.orderId;
  // 补贴金额固定为800，实际收费减去补贴金额自动计算自付金额
  completeForm.value = { actualFee: row.estimatedPrice || 0, subsidyAmount: 800, selfPayAmount: Math.max(0, (row.estimatedPrice || 0) - 800) };
  completeModalVisible.value = true;
}

async function handleCompleteSubmit() {
  // 校验：实际收费 = 补贴金额 + 自付金额
  const { actualFee, subsidyAmount, selfPayAmount } = completeForm.value;
  if (actualFee !== subsidyAmount + selfPayAmount) {
    message.error('实际收费金额必须等于补贴金额加自付金额');
    return;
  }
  try {
    await fetchCompleteOrder(currentOrderId.value, completeForm.value);
    message.success('完成服务');
    completeModalVisible.value = false;
    await getData();
    await getStatistics();
  } catch (e: any) {
    console.error('Complete error:', e);
    const errMsg = e?.message || e?.response?.data?.message || '完成服务失败';
    message.error(errMsg);
  }
}

async function handleDelete(row: Api.Order.Order) {
  try {
    await fetchDeleteOrder(row.orderId);
    message.success('删除成功');
    await getData();
    await getStatistics();
  } catch (e: any) {
    console.error('Delete error:', e);
    const errMsg = e?.message || e?.response?.data?.message || '删除失败';
    message.error(errMsg);
  }
}

async function handleBatchDelete() {
  if (!checkedRowKeys.value.length) return;
  try {
    await fetchBatchDeleteOrder(checkedRowKeys.value);
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

function handleCancel(row: Api.Order.Order) {
  currentOrderId.value = row.orderId;
  cancelForm.value = { reason: '' };
  cancelModalVisible.value = true;
}

async function handleCancelSubmit() {
  try {
    await fetchCancelOrder(currentOrderId.value, cancelForm.value);
    message.success('退回成功，预约已返回待分配状态');
    cancelModalVisible.value = false;
    await getData();
    await getStatistics();
  } catch (e: any) {
    console.error('Cancel error:', e);
    const errMsg = e?.message || e?.response?.data?.message || '取消订单失败';
    message.error(errMsg);
  }
}

function handleResetSearch() {
  searchOrderNo.value = '';
  searchElderName.value = '';
  searchProviderId.value = null;
  searchServiceType.value = null;
  searchStatus.value = null;
  searchDateRange.value = null;
  pagination.page = 1;
  getData();
}

function handleStatusPillClick(statusValue: string | null) {
  searchStatus.value = statusValue;
  pagination.page = 1;
  getData();
}

// 生成订单时间轴数据 - 参考预约单设计，添加详情信息
interface TimelineRelations {
  serviceLog?: Api.ServiceLog.ServiceLog | null;
  evaluation?: Api.Evaluation.Evaluation | null;
  qualityCheck?: Api.Quality.QualityCheck | null;
}

function generateOrderTimeline(order: Api.Order.Order, relations?: TimelineRelations): OrderTimelineItem[] {
  const timeline: OrderTimelineItem[] = [];
  const { serviceLog, evaluation, qualityCheck } = relations || {};

  // 审核状态映射
  const auditStatusMap: Record<string, string> = {
    DRAFT: '草稿',
    SUBMITTED: '已提交',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    COMPLETED: '已完成'
  };

  // 质检结果映射
  const checkResultMap: Record<string, string> = {
    PENDING: '待质检',
    QUALIFIED: '合格',
    UNQUALIFIED: '不合格',
    NEED_RECTIFY: '需整改'
  };

  if (order.createTime) {
    const details: { label: string; value: string }[] = [
      { label: '客户姓名', value: order.elderName || '-' },
      { label: '服务类型', value: order.serviceTypeName || '-' },
      { label: '预估费用', value: `¥${order.estimatedPrice || 0}` }
    ];
    if (order.remark) {
      details.push({ label: '备注', value: order.remark });
    }
    timeline.push({
      status: 'CREATED',
      time: order.createTime,
      description: '订单创建成功',
      details
    });
  }

  if (order.dispatchTime) {
    timeline.push({
      status: 'DISPATCHED',
      time: order.dispatchTime,
      description: `派单给服务商：${order.providerName || '-'}`,
      operator: order.dispatcherName || '系统',
      details: [
        { label: '服务商', value: order.providerName || '-' },
        { label: '服务人员', value: order.staffName || '-' },
        { label: '派单人', value: order.dispatcherName || '系统' }
      ]
    });
  }

  if (order.receiveTime) {
    timeline.push({
      status: 'RECEIVED',
      time: order.receiveTime,
      description: '服务人员已接单',
      operator: order.staffName,
      details: [
        { label: '服务人员', value: order.staffName || '-' },
        { label: '联系电话', value: order.staffPhone || '-' }
      ]
    });
  }

  if (order.startTime) {
    // 服务日志详情
    const serviceLogInfo = serviceLog ? {
      auditStatus: serviceLog.auditStatus || '',
      auditStatusLabel: auditStatusMap[serviceLog.auditStatus] || serviceLog.auditStatus || '-',
      reviewComment: serviceLog.reviewComment,
      reviewerName: serviceLog.reviewerName,
      reviewTime: serviceLog.reviewTime,
      // 质检信息
      qualityCheck: qualityCheck ? {
        checkResult: qualityCheck.checkResult || '',
        checkResultLabel: checkResultMap[qualityCheck.checkResult] || qualityCheck.checkResult || '-',
        checkRemark: qualityCheck.checkRemark
      } : undefined
    } : undefined;

    timeline.push({
      status: 'SERVICE_STARTED',
      time: order.startTime,
      description: '服务人员开始服务',
      operator: order.staffName,
      serviceLog: serviceLogInfo
    });
  }

  if (order.completeTime) {
    const details: { label: string; value: string }[] = [
      { label: '实际费用', value: `¥${order.actualPrice || 0}` },
      { label: '自付金额', value: `¥${order.selfPayAmount || 0}` },
      { label: '补贴金额', value: `¥${order.subsidyAmount || 0}` }
    ];

    // 评价详情
    const evaluationInfo = evaluation ? {
      overallScore: evaluation.overallScore || 0,
      evaluationContent: evaluation.content || evaluation.evaluationContent,
      evaluationTime: evaluation.createTime || evaluation.evaluationTime
    } : undefined;

    timeline.push({
      status: 'SERVICE_COMPLETED',
      time: order.completeTime,
      description: '服务已完成',
      operator: order.staffName,
      details,
      evaluation: evaluationInfo
    });
  }

  if (order.cancelTime) {
    timeline.push({
      status: 'CANCELLED',
      time: order.cancelTime,
      description: `订单已取消${order.cancelReason ? '：' + order.cancelReason : ''}`,
      operator: order.cancellerName || '系统'
    });
  }

  return timeline.sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime());
}

async function handleDetail(row: Api.Order.Order) {
  try {
    // fetchGetOrder 返回 OrderDetailVO，已包含 serviceLogs + qualityChecks 数组
    const { data: orderData } = await fetchGetOrder(row.orderId);
    if (!orderData) return;

    // 并行获取评价、服务日志、质检记录
    const [evaluationRes, serviceLogRes, qualityCheckRes] = await Promise.all([
      fetchGetEvaluationByOrderId(row.orderId).catch(() => null),
      fetchGetServiceLogByOrderId(row.orderId).catch(() => null),
      fetchGetQualityCheckByOrderId(row.orderId).catch(() => null)
    ]);

    const evaluation = evaluationRes?.data;
    const serviceLog = serviceLogRes?.data;
    const qualityCheck = qualityCheckRes?.data;

    orderDetailData.value = orderData;
    orderTimelineData.value = generateOrderTimeline(orderData, { evaluation, serviceLog, qualityCheck });
    orderDetailVisible.value = true;
  } catch (e) {
    console.error('Failed to get order detail', e);
    message.error('获取订单详情失败');
  }
}

function goToServiceLog(orderNo: string | undefined) {
  if (!orderNo) return;
  routerPushByKey('business_service-log', { query: { orderNo } });
}

function goToAppointment(appointmentId: string | undefined) {
  if (!appointmentId) return;
  routerPushByKey('appointment', { query: { id: appointmentId } });
}

// 满意度调查 - 创建评价记录
async function handleCreateEvaluation() {
  const order = orderDetailData.value;
  if (!order || !order.orderId) {
    message.warning('订单信息不完整');
    return;
  }

  // 获取服务人员ID（从订单或服务日志中获取）
  const staffId = order.staffId;
  if (!staffId) {
    message.warning('无法获取服务人员信息');
    return;
  }

  try {
    const evaluationData: Api.Evaluation.EvaluationForm = {
      orderId: order.orderId,
      staffId: staffId,
      serviceScore: 5,
      attitudeScore: 5,
      skillScore: 5,
      punctualityScore: 5,
      environmentScore: 5,
      overallScore: 5,
      content: '系统自动创建的满意度评价记录'
    };

    await fetchCreateEvaluation(evaluationData);
    message.success('满意度评价记录已创建');

    // 刷新详情数据
    await handleDetail(order);
  } catch (e: any) {
    console.error('Failed to create evaluation', e);
    message.error(e.message || '创建满意度评价失败');
  }
}

onMounted(async () => {
  // 接收从其他页面跳转过来的过滤参数
  if (route.query.orderNo) {
    searchOrderNo.value = route.query.orderNo as string;
  }
  if (route.query.elderName) {
    searchElderName.value = route.query.elderName as string;
  }
  await getStatistics();
  await getData();
  getProviderOptions();

  // 如果带orderNo参数，自动打开第一条订单的详情
  if (route.query.orderNo) {
    await new Promise(resolve => setTimeout(resolve, 500));
    const matchOrder = tableData.value.find((o: any) => o.orderNo === route.query.orderNo);
    if (matchOrder) {
      handleDetail(matchOrder);
    }
  }
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
                          background: linear-gradient(180deg, #5E8B7E 0%, #7BA89F 100%);
                          border-radius: 2px;
                        ">
          ></div>
          <span style="font-size: 16px; font-weight: 600">订单概览</span>
        </div>
      </template>
      <!-- 第一行：核心数量 -->
      <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px">
        <div class="stat-card stat-primary">
          <div class="stat-label">总订单数</div>
          <div class="stat-value">{{ statistics.total }}</div>
          <div class="stat-sub">本月新增 {{ statistics.month }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">今日新增</div>
          <div class="stat-value">{{ statistics.today }}</div>
          <div class="stat-sub">服务中 {{ statistics.inService }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">已完成</div>
          <div class="stat-value">{{ statistics.completed }}</div>
          <div class="stat-sub">完成率 {{ Number(statistics.completionRate || 0).toFixed(1) }}%</div>
        </div>
        <div class="stat-card stat-error">
          <div class="stat-label">已取消</div>
          <div class="stat-value">{{ statistics.cancelled }}</div>
          <div class="stat-sub">取消率 {{ Number(statistics.cancelRate || 0).toFixed(1) }}%</div>
        </div>
      </div>
      <!-- 第二行：状态分布 -->
      <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px">
        <div class="stat-card-mini">
          <div class="stat-mini-icon" style="background: linear-gradient(135deg, #F9B572 0%, #F7C59F 100%)">
            <span style="font-size: 20px">📋</span>
          </div>
          <div class="stat-mini-content">
            <div class="stat-mini-value">{{ statistics.pendingDispatch }}</div>
            <div class="stat-mini-label">待派单</div>
          </div>
        </div>
        <div class="stat-card-mini">
          <div class="stat-mini-icon" style="background: linear-gradient(135deg, #3A506B 0%, #5A7A8B 100%)">
            <span style="font-size: 20px">🚀</span>
          </div>
          <div class="stat-mini-content">
            <div class="stat-mini-value">{{ statistics.dispatched }}</div>
            <div class="stat-mini-label">已派单</div>
          </div>
        </div>
        <div class="stat-card-mini">
          <div class="stat-mini-icon" style="background: linear-gradient(135deg, #5E8B7E 0%, #81B29A 100%)">
            <span style="font-size: 20px">✅</span>
          </div>
          <div class="stat-mini-content">
            <div class="stat-mini-value">{{ statistics.received }}</div>
            <div class="stat-mini-label">已接单</div>
          </div>
        </div>
        <div class="stat-card-mini">
          <div class="stat-mini-icon" style="background: linear-gradient(135deg, #D64045 0%, #E07070 100%)">
            <span style="font-size: 20px">💰</span>
          </div>
          <div class="stat-mini-content">
            <div class="stat-mini-value">{{ Number(statistics.totalSelfPay || 0).toFixed(0) }}</div>
            <div class="stat-mini-label">总自付金额</div>
          </div>
        </div>
      </div>
      <!-- 第三行：服务人员排名 -->
      <div
        v-if="statistics.staffRankings && statistics.staffRankings.length > 0"
        style="background: #f8f9fa; border-radius: 12px; padding: 16px"
      >
        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 12px">
          <span style="font-size: 14px; font-weight: 600; color: #5E8B7E">🏆 服务标兵排行榜</span>
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
                (index === 0 ? '#ffd700' : index === 1 ? '#c0c0c0' : index === 2 ? '#cd7f32' : '#5E8B7E') +
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
              <span style="color: #18a058">完成 {{ rank.completedCount }}</span>
            </div>
          </div>
        </div>
      </div>
    </NCard>

    <!-- Table -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>订单管理</span>
        </div>
      </template>
      <!-- 紧凑搜索区：去灰色盒，Pill内联单行 -->
      <div style="display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin-bottom: 12px">
        <button
          v-for="opt in statusOptions"
          :key="opt.value"
          :class="searchStatus === opt.value ? 'status-pill active' : 'status-pill'"
          :style="searchStatus === opt.value ? '' : 'background:#fff;border-color:#d1d5db'"
          @click="handleStatusPillClick(opt.value)"
          style="height: 32px; padding: 0 12px; border-radius: 16px; font-size: 13px; font-weight: 600"
        >
          {{ opt.label }}
        </button>
        <div style="width: 1px; height: 22px; background: #e0e0e0; margin: 0 4px" />
        <NInput v-model:value="searchOrderNo" placeholder="订单号" clearable style="width: 150px" size="small" />
        <NInput v-model:value="searchElderName" placeholder="客户" clearable style="width: 110px" size="small" />
        <NSelect v-model:value="searchProviderId" :options="providerOptions" placeholder="服务商" clearable filterable style="width: 150px" size="small" :consistent-menu-width="false" />
        <NSelect v-model:value="searchServiceType" :options="serviceTypeOptions" placeholder="服务类型" clearable style="width: 130px" size="small" />
        <NDatePicker v-model:value="searchDateRange" type="daterange" clearable style="width: 220px" size="small" />
        <NButton type="primary" size="small" @click="() => { getData(); pagination.page = 1; }">搜索</NButton>
        <NButton size="small" @click="handleResetSearch">重置</NButton>
        <div style="flex: 1" />
        <NButton size="small" type="primary" @click="handleAdd">+ 新增</NButton>
      </div>

      <!-- 卡片网格：3列（适老化大字版） -->
      <div v-if="!loading && tableData.length > 0" style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 16px">
        <div
          v-for="row in tableData"
          :key="row.orderId"
          class="order-card"
          @click="handleDetail(row)"
          style="border: 1px solid #e8e8e8; border-radius: 10px; padding: 14px; cursor: pointer; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06)"
        >
          <!-- 第1行：客户名 + 状态 + 金额 -->
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px">
            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap">
              <span style="font-size: 17px; font-weight: 700; color: #222">{{ row.elderName }}</span>
              <span style="font-size: 13px; color: #999">{{ row.elderPhone || '' }}</span>
              <NTag :type="getStatusType(row.status)" size="small" style="font-size: 13px; font-weight: 600">{{ getStatusLabel(row.status) }}</NTag>
            </div>
            <span style="font-size: 18px; font-weight: 800; color: #ee4a07; font-family: 'DIN Alternate', 'Helvetica Neue', Arial, sans-serif">¥{{ row.actualPrice || row.estimatedPrice || 0 }}</span>
          </div>
          <!-- 第2行：服务+时间 -->
          <div style="font-size: 14px; color: #555; margin-bottom: 4px">
            <span style="font-weight: 600">{{ row.serviceTypeName || '-' }}</span>
            <span style="margin: 0 6px; color: #ccc">|</span>
            <span>{{ formatServiceTime(row.serviceDate, row.serviceTime) }}</span>
          </div>
          <!-- 第3行：服务商+护工 -->
          <div style="font-size: 13px; color: #888; margin-bottom: 10px">
            {{ row.providerName || '-' }}<span v-if="row.staffName" style="margin: 0 6px; color: #ccc">|</span><span v-if="row.staffName">{{ row.staffName }}</span>
          </div>
          <!-- 第4行：订单号 + 操作 -->
          <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #f0f0f0; padding-top: 10px; gap: 8px">
            <span style="font-size: 12px; color: #bbb">{{ row.orderNo }}</span>
            <div style="display: flex; gap: 6px; flex-shrink: 0; flex-wrap: nowrap" @click.stop>
              <NButton v-if="(row.status === 'CREATED' || row.status === 'PENDING') && hasAuth('order:list:dispatch')" size="small" type="primary" style="height: 34px; font-size: 13px; font-weight: 600; white-space: nowrap" @click="handleAssign(row)">派单</NButton>
              <NButton v-if="row.status === 'DISPATCHED'" size="small" type="primary" style="height: 34px; font-size: 13px; font-weight: 600; white-space: nowrap" @click="handleAccept(row)">接单</NButton>
              <NButton v-if="row.status === 'RECEIVED'" size="small" type="primary" style="height: 34px; font-size: 13px; font-weight: 600; white-space: nowrap" @click="handleStart(row)">开始</NButton>
              <NButton v-if="row.status === 'SERVICE_STARTED'" size="small" type="primary" style="height: 34px; font-size: 13px; font-weight: 600; white-space: nowrap" @click="handleComplete(row)">完成</NButton>
              <NButton size="small" style="height: 34px; font-size: 13px; font-weight: 600; white-space: nowrap" @click="handleDetail(row)">详情</NButton>
              <NPopconfirm v-if="(row.status === 'SERVICE_COMPLETED' || row.status === 'EVALUATED' || row.status === 'SETTLED') && hasAuth('order:list:delete')" @positive-click="handleDelete(row)">
                <template #trigger><NButton size="small" type="error" style="height: 34px; font-size: 13px; font-weight: 600; white-space: nowrap">删除</NButton></template>
                确认删除？
              </NPopconfirm>
              <NButton v-if="row.status !== 'SERVICE_COMPLETED' && row.status !== 'EVALUATED' && row.status !== 'SETTLED' && row.status !== 'CANCELLED' && row.status !== 'REJECTED' && hasAuth('order:list:cancel')" size="small" type="error" ghost style="height: 34px; font-size: 13px; font-weight: 600; white-space: nowrap" @click="handleCancel(row)">退回</NButton>
            </div>
          </div>
        </div>
      </div>

      <!-- 无数据状态 -->
      <div v-if="!loading && tableData.length === 0" style="text-align: center; padding: 60px 0; color: #999; font-size: 15px">
        暂无订单数据
      </div>

      <!-- Loading -->
      <div v-if="loading" style="text-align: center; padding: 40px 0">
        <NSpin size="large" />
      </div>

      <!-- 分页 -->
      <div v-if="!loading && tableData.length > 0" style="display: flex; justify-content: center; margin-top: 16px">
        <NPagination
          v-model:page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[12, 24, 48]"
          :page-count="pagination.pageCount"
          :show-quick-jumper="true"
          @update:page="(p) => { pagination.page = p; getData(); }"
          @update:page-size="(s) => { pagination.pageSize = s; pagination.page = 1; getData(); }"
        />
      </div>
    </NCard>

    <!-- Add Modal -->
    <OrderAddModal v-model:visible="addModalVisible" @success="getData" />

    <!-- Assign Modal -->
    <NModal v-model:show="assignModalVisible" title="派单" preset="card" style="width: 600px">
      <div style="margin-bottom: 16px">
        <div style="color: #666; font-size: 13px; margin-bottom: 4px">服务商</div>
        <div style="font-size: 15px; font-weight: 500">{{ currentOrderProviderName || '未指定' }}</div>
      </div>
      <NForm ref="formRef" :model="assignForm" :rules="assignFormRules" label-placement="left" label-width="80">
        <NFormItem label="选择人员" path="staffId">
          <NSelect
            v-model:value="assignForm.staffId"
            :options="staffOptions"
            placeholder="请选择服务人员"
            filterable
            :loading="loadingStaff"
            @update:value="handleStaffSelect"
          />
        </NFormItem>
      </NForm>
      <!-- Selected staff detail card -->
      <div v-if="selectedStaffDetail" style="background: #f5f5f5; border-radius: 8px; padding: 16px; margin-top: 12px">
        <div style="font-weight: 600; margin-bottom: 12px; color: #333">服务人员信息</div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px">
          <div>
            <div style="color: #999; font-size: 12px">姓名</div>
            <div>{{ selectedStaffDetail.staffName }}</div>
          </div>
          <div>
            <div style="color: #999; font-size: 12px">性别</div>
            <div>{{ Number(selectedStaffDetail.gender) === 1 ? "男" : "女" }}</div>
          </div>
          <div>
            <div style="color: #999; font-size: 12px">联系电话</div>
            <div>{{ selectedStaffDetail.phone || '-' }}</div>
          </div>
          <div>
            <div style="color: #999; font-size: 12px">服务类型</div>
            <div>{{ selectedStaffDetail.serviceTypes || selectedStaffDetail.serviceTypesText || '-' }}</div>
          </div>
          <div style="grid-column: span 2">
            <div style="color: #999; font-size: 12px">简介</div>
            <div>{{ selectedStaffDetail.remark || '暂无简介' }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="assignModalVisible = false">取消</NButton>
          <NButton type="primary" :loading="assignLoading" @click="handleAssignSubmit">
            {{ assignLoading ? '派单中...' : '确认派单' }}
          </NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Complete Modal -->
    <NModal v-model:show="completeModalVisible" title="完成服务" preset="card" style="width: 500px">
      <NForm :model="completeForm" label-placement="left" label-width="100">
        <NFormItem label="实际收费">
          <NInputNumber v-model:value="completeForm.actualFee" :min="0" />
        </NFormItem>
        <NFormItem label="补贴金额">
          <NInputNumber v-model:value="completeForm.subsidyAmount" :min="0" disabled />
          <template #feedback><span style="color: #999; font-size: 12px;">固定金额800元</span></template>
        </NFormItem>
        <NFormItem label="自付金额">
          <NInputNumber v-model:value="completeForm.selfPayAmount" :min="0" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="completeModalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleCompleteSubmit">确认</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Cancel/Return Modal -->
    <NModal v-model:show="cancelModalVisible" title="退回订单" preset="card" style="width: 500px">
      <p style="color: #666; margin-bottom: 16px;">退回后预约将返回待分配状态，管理员可重新指定服务商。</p>
      <NForm :model="cancelForm" label-placement="left" label-width="100">
        <NFormItem label="退回原因">
          <NInput v-model:value="cancelForm.reason" type="textarea" placeholder="请输入退回原因（如：服务商无法处理）" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="cancelModalVisible = false">取消</NButton>
          <NButton type="error" @click="handleCancelSubmit">确认退回</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Contract Detail Modal -->
    <NModal v-model:show="contractDetailVisible" title="合同详情" preset="card" style="width: 520px">
      <NForm v-if="contractDetailData" label-placement="left" label-width="100">
        <NFormItem label="合同编号">{{ contractDetailData.contractNo || '-' }}</NFormItem>
        <NFormItem label="合同名称">{{ contractDetailData.contractName || '智慧居家养老服务合同' }}</NFormItem>
        <NFormItem label="甲方（服务商）">{{ contractDetailData.providerName || '-' }}</NFormItem>
        <NFormItem label="乙方（服务人员）">{{ contractDetailData.staffName || '-' }}</NFormItem>
        <NFormItem label="合同状态">
          <NTag :type="contractDetailData.status === 'SIGNED' || contractDetailData.status === 'COMPLETED' ? 'success' : 'warning'" size="small">
            {{ { DRAFT: '草稿', INITIATED: '已发起', SIGNING: '签署中', SIGNED: '已签署', COMPLETED: '已完成', EXPIRED: '已过期', REJECTED: '已拒签', CANCELLED: '已撤回' }[contractDetailData.status] || contractDetailData.status }}
          </NTag>
        </NFormItem>
        <NFormItem label="创建时间">{{ contractDetailData.createTime || '-' }}</NFormItem>
      </NForm>
      <div v-else style="text-align: center; padding: 20px; color: #999">暂无合同信息</div>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="contractDetailVisible = false">关闭</NButton>
          <NButton
            v-if="contractDetailData && contractDetailData.status !== 'SIGNED' && contractDetailData.status !== 'COMPLETED'"
            @click="handleRefreshContractStatus"
            :loading="contractRefreshLoading"
          >
            刷新状态
          </NButton>
          <NButton
            v-if="contractDetailData && contractDetailData.status !== 'SIGNED' && contractDetailData.status !== 'COMPLETED'"
            type="primary"
            :loading="contractSignLoading"
            @click="handleSignContract"
          >
            签署合同
          </NButton>
          <NButton
            v-if="contractDetailData && (contractDetailData.status === 'SIGNED' || contractDetailData.status === 'COMPLETED')"
            type="primary"
            @click="handleAcceptAfterSign"
          >
            确认接单
          </NButton>
        </NSpace>
      </template>
    </NModal>

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
        <NFormItem label="补贴类型">
          {{
            elderDetailData.subsidyType === 'FULL_SUBSIDY'
              ? '全额补贴'
              : elderDetailData.subsidyType === 'PARTIAL_SUBSIDY'
                ? '部分补贴'
                : elderDetailData.subsidyType === 'SELF_PAY'
                  ? '自费'
                  : '-'
          }}
        </NFormItem>
        <NFormItem label="紧急联系人">{{ elderDetailData.emergencyContact || '-' }}</NFormItem>
        <NFormItem label="紧急联系电话">{{ elderDetailData.emergencyPhone || '-' }}</NFormItem>
      </NForm>
    </NModal>

    <!-- Staff Detail Modal -->
    <NModal v-model:show="staffDetailVisible" title="服务人员详情" preset="card" style="width: 600px">
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
        <NFormItem label="状态">
          {{ staffDetailData.status === 'ON_JOB' ? '在职' : staffDetailData.status === 'OFF_JOB' ? '离职' : '-' }}
        </NFormItem>
      </NForm>
    </NModal>

    <!-- Order Detail Drawer -->
    <OrderDetailDrawer
      v-model:visible="orderDetailVisible"
      :order-data="orderDetailData"
      :timeline-data="orderTimelineData"
      @create-evaluation="handleCreateEvaluation"
      @go-to-service-log="goToServiceLog"
      @go-to-appointment="goToAppointment"
      @go-to-service-log-detail="(logId) => routerPushByKey('business_service-log', { query: { logId } })"
      @go-to-quality-check="(checkId) => routerPushByKey('business_quality', { query: { id: checkId } })"
    />
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
  font-size: 15px;
  color: #666;
  margin-bottom: 8px;
  font-weight: 600;
}

.stat-value {
  font-size: var(--font-size-stat);
  font-weight: 800;
  line-height: 1.1;
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
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  line-height: 1.3;
}

.stat-mini-label {
  font-size: 14px;
  color: #666;
  margin-top: 2px;
  font-weight: 500;
}

.stat-sub {
  font-size: 13px;
  margin-top: 6px;
  opacity: 0.85;
}

::deep(.n-data-table-tr--checked) {
  background-color: rgba(24, 160, 88, 0.12) !important;
}

::deep(.n-data-table-tr--checked:hover) {
  background-color: rgba(24, 160, 88, 0.18) !important;
}

/* ========== 时间轴样式 - 参考预约单设计 ========== */
.timeline-container {
  padding: 16px 0;
}

.timeline-node {
  position: relative;
}

.timeline-node-header {
  display: flex;
  cursor: pointer;
  padding: 12px 16px;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.timeline-node-header:hover {
  background-color: #f5f5f5;
}

.timeline-node-active {
  background-color: rgba(79, 172, 254, 0.08);
}

.timeline-node-completed {
  opacity: 0.85;
}

.timeline-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 32px;
  flex-shrink: 0;
}

.timeline-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 600;
  z-index: 1;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.timeline-check {
  font-size: 14px;
}

.timeline-pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: white;
  animation: timeline-pulse 1.5s infinite;
}

@keyframes timeline-pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(79, 172, 254, 0.6);
  }
  70% {
    box-shadow: 0 0 0 8px rgba(79, 172, 254, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(79, 172, 254, 0);
  }
}

.timeline-line {
  width: 2px;
  flex: 1;
  min-height: 24px;
  background: #e0e0e0;
  margin: 4px 0;
}

.timeline-line-completed {
  background: #5E8B7E;
}

.timeline-node-content {
  flex: 1;
  padding-left: 16px;
  padding-right: 8px;
  min-width: 0;
}

.timeline-node-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.timeline-node-status-icon {
  font-size: 14px;
  width: 18px;
  text-align: center;
}

.timeline-node-title-text {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.timeline-node-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.timeline-node-description {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
  line-height: 1.5;
}

.timeline-node-operator {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.timeline-node-operator .operator-label {
  color: #999;
}

.timeline-node-operator .operator-name {
  color: #3A506B;
  font-weight: 500;
}

.timeline-node-details {
  max-height: 0;
  overflow: hidden;
  transition:
    max-height 0.3s ease,
    opacity 0.3s ease;
  opacity: 0;
}

.timeline-node-details-expanded {
  max-height: 500px;
  opacity: 1;
  margin-top: 12px;
}

.timeline-details-list {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px 16px;
}

.timeline-detail-item {
  display: flex;
  padding: 6px 0;
  font-size: 13px;
  border-bottom: 1px solid #eee;
}

.timeline-detail-item:last-child {
  border-bottom: none;
}

.timeline-detail-label {
  color: #666;
  min-width: 80px;
  flex-shrink: 0;
}

.timeline-detail-value {
  color: #333;
  word-break: break-all;
}

.timeline-expand-hint {
  font-size: 11px;
  color: #999;
  margin-top: 8px;
  text-align: right;
}

.timeline-node-actions {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #eee;
}

/* 服务日志审核记录样式 */
.timeline-service-log {
  margin-top: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  border-left: 3px solid #5E8B7E;
}

.timeline-section-title {
  font-size: 12px;
  font-weight: 600;
  color: #5E8B7E;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.timeline-log-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.timeline-log-reviewer {
  font-size: 12px;
  color: #666;
}

.timeline-log-reviewer span {
  color: #999;
  margin-left: 4px;
}

.timeline-log-comment {
  font-size: 12px;
  color: #666;
  margin-top: 6px;
  padding: 6px 8px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 4px;
  font-style: italic;
}

.timeline-quality-info {
  margin-top: 8px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}

.timeline-quality-remark {
  font-size: 12px;
  color: #666;
  background: rgba(255, 255, 255, 0.6);
  padding: 4px 8px;
  border-radius: 4px;
}

/* 满意度评价样式 */
.timeline-evaluation {
  margin-top: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%);
  border-radius: 8px;
  border-left: 3px solid #ffc107;
}

.timeline-eval-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.timeline-eval-score {
  font-size: 12px;
  color: #856404;
  font-weight: 500;
}

.timeline-eval-content {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 4px;
  line-height: 1.5;
}

.timeline-no-evaluation {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.timeline-no-eval-text {
  font-size: 12px;
  color: #999;
}

.timeline-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.timeline-empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.timeline-empty-text {
  font-size: 14px;
}
</style>
