<script setup lang="ts">
import { ref, h, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
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
  NPopconfirm
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import {
  fetchGetOrderList,
  fetchCreateOrder,
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
  fetchGetStaffList
} from '@/service/api';
import { useRouterPush } from '@/hooks/common/router';
import { useNaivePaginatedTable, defaultTransform } from '@/hooks/common/table';
import { useNaiveForm } from '@/hooks/common/form';
import { useAuth } from '@/hooks/business/auth';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';

defineOptions({
  name: 'BusinessOrder'
});

const message = useMessage();
const route = useRoute();
const { routerPushByKey } = useRouterPush();
const { hasAuth } = useAuth();
const { formRef } = useNaiveForm();

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
  { label: '已结算', value: 'SETTLED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '已拒单', value: 'REJECTED' }
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
    REJECTED: 'error'
  };
  return map[status] || 'default';
}

function getStatusLabel(status: string): string {
  const option = statusOptions.find(o => o.value === status);
  return option?.label || status;
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
}

function getNodeIcon(node: OrderTimelineItem): string {
  if (node.status === 'SERVICE_COMPLETED' || node.status === 'EVALUATED' || node.status === 'SETTLED') return '✓';
  if (node.status === 'CANCELLED') return '✕';
  if (isCurrentNode(node)) return '●';
  return '○';
}

function getNodeColor(node: OrderTimelineItem): string {
  if (node.status === 'SERVICE_COMPLETED' || node.status === 'EVALUATED' || node.status === 'SETTLED') return '#11998e';
  if (node.status === 'CANCELLED') return '#f5576c';
  if (isCurrentNode(node)) return '#4facfe';
  return '#999';
}

function isCurrentNode(node: OrderTimelineItem): boolean {
  if (!orderDetailData.value) return false;
  const currentStatus = orderDetailData.value.status;
  const timeline = orderTimelineData.value;
  if (!timeline || timeline.length === 0) return false;
  const nodeIndex = timeline.findIndex(n => n.status === node.status);
  const lastIndex = timeline.length - 1;

  // 当前节点是最后一个且订单未完成/未取消
  return nodeIndex === lastIndex && !['SERVICE_COMPLETED', 'EVALUATED', 'SETTLED', 'CANCELLED'].includes(currentStatus);
}

function isCompletedNode(node: OrderTimelineItem): boolean {
  if (!orderDetailData.value) return false;
  const currentStatus = orderDetailData.value.status;
  const timeline = orderTimelineData.value;
  if (!timeline || timeline.length === 0) return false;
  const nodeIndex = timeline.findIndex(n => n.status === node.status);
  const lastIndex = timeline.length - 1;

  // 已完成节点：不是最后一个，或者订单已完成
  return nodeIndex < lastIndex || ['SERVICE_COMPLETED', 'EVALUATED', 'SETTLED'].includes(currentStatus);
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
const columns: DataTableColumns<Api.Order.Order> = [
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
    width: 260,
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
        buttons.push(h(NButton, { size: 'small', type: 'error', onClick: () => handleCancel(row) }, () => '取消'));
      }
      return h(NSpace, { size: 'small' }, () => buttons);
    }
  }
];

// Use framework's table hook
const tableHookResult = useNaivePaginatedTable<Api.Common.PaginatingQueryRecord<Api.Order.Order>, Api.Order.Order>({
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

// Table checked row keys
const checkedRowKeys = ref<string[]>([]);

// Add modal
const addModalVisible = ref(false);
const addForm = ref({
  elderName: '',
  elderPhone: '',
  serviceTypeCode: '',
  serviceTypeName: '',
  serviceDate: null as number | null,
  serviceTime: '',
  serviceDuration: 60,
  serviceAddress: '',
  specialRequirements: '',
  estimatedPrice: 0,
  subsidyAmount: 0,
  selfPayAmount: 0
});

// Service type options for add form
const addServiceTypeOptions = [
  { label: '生活照料', value: '01', name: '生活照料' },
  { label: '日间照料', value: '02', name: '日间照料' },
  { label: '助餐服务', value: '03', name: '助餐服务' },
  { label: '助洁服务', value: '04', name: '助洁服务' },
  { label: '助浴服务', value: '05', name: '助浴服务' },
  { label: '康复护理', value: '06', name: '康复护理' },
  { label: '精神慰藉', value: '07', name: '精神慰藉' },
  { label: '健康管理', value: '08', name: '健康管理' },
  { label: '信息咨询', value: '09', name: '信息咨询' }
];

function handleAdd() {
  addForm.value = {
    elderName: '',
    elderPhone: '',
    serviceTypeCode: '',
    serviceTypeName: '',
    serviceDate: null,
    serviceTime: '',
    serviceDuration: 60,
    serviceAddress: '',
    specialRequirements: '',
    estimatedPrice: 0,
    subsidyAmount: 0,
    selfPayAmount: 0
  };
  addModalVisible.value = true;
}

async function handleAddSubmit() {
  if (!addForm.value.elderName) {
    message.warning('请输入客户姓名');
    return;
  }
  if (!addForm.value.elderPhone) {
    message.warning('请输入客户手机号');
    return;
  }
  if (!addForm.value.serviceTypeCode) {
    message.warning('请选择服务类型');
    return;
  }
  if (!addForm.value.serviceDate) {
    message.warning('请选择服务日期');
    return;
  }

  try {
    const selectedService = addServiceTypeOptions.find(s => s.value === addForm.value.serviceTypeCode);
    const data = {
      elderName: addForm.value.elderName,
      elderPhone: addForm.value.elderPhone,
      serviceTypeCode: addForm.value.serviceTypeCode,
      serviceTypeName: selectedService?.name || '',
      serviceDate: new Date(addForm.value.serviceDate).toISOString().split('T')[0],
      serviceTime: addForm.value.serviceTime,
      serviceDuration: addForm.value.serviceDuration,
      serviceAddress: addForm.value.serviceAddress,
      specialRequirements: addForm.value.specialRequirements,
      estimatedPrice: addForm.value.estimatedPrice,
      subsidyAmount: addForm.value.subsidyAmount,
      selfPayAmount: addForm.value.selfPayAmount,
      orderType: 'NORMAL',
      orderSource: 'MANUAL'
    };
    await fetchCreateOrder(data);
    message.success('添加订单成功');
    addModalVisible.value = false;
    await getData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to add order', e);
    message.error(e?.message || '添加失败');
  }
}

// Assign modal
const assignModalVisible = ref(false);
const assignForm = ref({ staffId: '', providerId: '' });
const currentOrderId = ref('');
const currentOrderProviderId = ref('');
const currentOrderProviderName = ref('');
const staffOptions = ref<{ label: string; value: string; phone?: string; serviceTypes?: string }[]>([]);
const selectedStaffDetail = ref<Api.Staff.Staff | null>(null);
const loadingStaff = ref(false);

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
const activeDetailTab = ref<'info' | 'timeline'>('info');
const expandedNodes = ref<Set<string>>(new Set());

function toggleNode(node: OrderTimelineItem) {
  if (expandedNodes.value.has(node.status)) {
    expandedNodes.value.delete(node.status);
  } else {
    expandedNodes.value.add(node.status);
  }
}

async function showElderDetail(row: Api.Order.Order) {
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

async function showStaffDetail(row: Api.Order.Order) {
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
        value: staff.staffId || staff.id,
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
  if (!assignForm.value.staffId) {
    message.warning('请选择服务人员');
    return;
  }
  const { error } = await fetchDispatchOrder(currentOrderId.value, assignForm.value);
  if (error) {
    console.error('派单失败:', error);
    const errMsg = error?.message || error?.response?.data?.message || '派单失败';
    message.error(errMsg);
    return;
  }
  message.success('派单成功');
  assignModalVisible.value = false;
  await getData();
  await getStatistics();
}

async function handleAccept(row: Api.Order.Order) {
  try {
    await fetchAcceptOrder(row.orderId, {
      staffId: row.staffId
    });
    message.success('接单成功');
    await getData();
    await getStatistics();
  } catch (e: any) {
    console.error('Accept error:', e);
    const errMsg = e?.message || e?.response?.data?.message || '接单失败';
    message.error(errMsg);
  }
}

async function handleStart(row: Api.Order.Order) {
  try {
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

function handleCancel(row: Api.Order.Order) {
  currentOrderId.value = row.orderId;
  cancelForm.value = { reason: '' };
  cancelModalVisible.value = true;
}

async function handleCancelSubmit() {
  try {
    await fetchCancelOrder(currentOrderId.value, cancelForm.value);
    message.success('取消成功');
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
  searchProviderId.value = '';
  searchServiceType.value = '';
  searchStatus.value = '';
  searchDateRange.value = null;
  getDataByPage(1);
}

// 生成订单时间轴数据 - 参考预约单设计，添加详情信息
function generateOrderTimeline(order: Api.Order.Order): OrderTimelineItem[] {
  const timeline: OrderTimelineItem[] = [];

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
    timeline.push({
      status: 'SERVICE_STARTED',
      time: order.startTime,
      description: '服务人员开始服务',
      operator: order.staffName
    });
  }

  if (order.completeTime) {
    const details: { label: string; value: string }[] = [
      { label: '实际费用', value: `¥${order.actualPrice || 0}` },
      { label: '自付金额', value: `¥${order.selfPayAmount || 0}` },
      { label: '补贴金额', value: `¥${order.subsidyAmount || 0}` }
    ];
    timeline.push({
      status: 'SERVICE_COMPLETED',
      time: order.completeTime,
      description: '服务已完成',
      operator: order.staffName,
      details
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
    const { data } = await fetchGetOrder(row.orderId);
    if (data) {
      orderDetailData.value = data;
      orderTimelineData.value = generateOrderTimeline(data);
      orderDetailVisible.value = true;
    }
  } catch (e) {
    console.error('Failed to get order detail', e);
    message.error('获取订单详情失败');
  }
}

function goToServiceLog(orderNo: string | undefined) {
  if (!orderNo) return;
  routerPushByKey('business_service-log', { query: { orderNo } });
}

onMounted(() => {
  if (route.query.elderName) {
    searchElderName.value = route.query.elderName as string;
  }
  getStatistics();
  getData();
  getProviderOptions();
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
          <div class="stat-mini-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
            <span style="font-size: 20px">📋</span>
          </div>
          <div class="stat-mini-content">
            <div class="stat-mini-value">{{ statistics.pendingDispatch }}</div>
            <div class="stat-mini-label">待派单</div>
          </div>
        </div>
        <div class="stat-card-mini">
          <div class="stat-mini-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
            <span style="font-size: 20px">🚀</span>
          </div>
          <div class="stat-mini-content">
            <div class="stat-mini-value">{{ statistics.dispatched }}</div>
            <div class="stat-mini-label">已派单</div>
          </div>
        </div>
        <div class="stat-card-mini">
          <div class="stat-mini-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%)">
            <span style="font-size: 20px">✅</span>
          </div>
          <div class="stat-mini-content">
            <div class="stat-mini-value">{{ statistics.received }}</div>
            <div class="stat-mini-label">已接单</div>
          </div>
        </div>
        <div class="stat-card-mini">
          <div class="stat-mini-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
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
      <div style="background: #f5f5f5; padding: 12px; margin-bottom: 12px; border-radius: 4px">
        <NSpace :wrap="true" align="center">
          <NInput v-model:value="searchOrderNo" placeholder="订单号" clearable style="width: 150px" />
          <NInput v-model:value="searchElderName" placeholder="客户姓名" clearable style="width: 100px" />
          <NSelect
            v-model:value="searchProviderId"
            :options="providerOptions"
            placeholder="服务商"
            clearable
            filterable
            style="width: 180px"
          />
          <NSelect
            v-model:value="searchServiceType"
            :options="serviceTypeOptions"
            placeholder="服务类型"
            clearable
            style="width: 120px"
          />
          <NSelect
            v-model:value="searchStatus"
            :options="statusOptions"
            placeholder="状态"
            clearable
            style="width: 120px"
          />
          <NDatePicker v-model:value="searchDateRange" type="daterange" clearable style="width: 260px" />
          <NButton type="primary" @click="getData">搜索</NButton>
          <NButton @click="handleResetSearch">重置</NButton>
        </NSpace>
      </div>

      <!-- Use framework's TableHeaderOperation component -->
      <TableHeaderOperation
        v-model:columns="columnChecks"
        :disabled-delete="checkedRowKeys.length === 0"
        :loading="loading"
        @add="handleAdd"
        @refresh="getData"
      />

      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1600"
        :row-key="(row: Api.Order.Order) => row.orderId"
        v-model:checked-row-keys="checkedRowKeys"
        :row-class-name="() => 'clickable-row'"
        remote
        :pagination="mobilePagination"
      />
    </NCard>

    <!-- Add Modal -->
    <NModal v-model:show="addModalVisible" title="添加订单" preset="card" style="width: 600px">
      <NForm :model="addForm" label-placement="left" label-width="100">
        <NFormItem label="客户姓名" required>
          <NInput v-model:value="addForm.elderName" placeholder="请输入客户姓名" />
        </NFormItem>
        <NFormItem label="手机号" required>
          <NInput v-model:value="addForm.elderPhone" placeholder="请输入手机号" />
        </NFormItem>
        <NFormItem label="服务类型" required>
          <NSelect
            v-model:value="addForm.serviceTypeCode"
            :options="addServiceTypeOptions"
            placeholder="请选择服务类型"
          />
        </NFormItem>
        <NFormItem label="服务日期" required>
          <NDatePicker
            v-model:value="addForm.serviceDate"
            type="date"
            placeholder="请选择服务日期"
            style="width: 100%"
          />
        </NFormItem>
        <NFormItem label="服务时间">
          <NInput v-model:value="addForm.serviceTime" placeholder="如：09:00-11:00" />
        </NFormItem>
        <NFormItem label="服务时长">
          <NInputNumber v-model:value="addForm.serviceDuration" :min="1" placeholder="分钟">
            <template #suffix>分钟</template>
          </NInputNumber>
        </NFormItem>
        <NFormItem label="服务地址">
          <NInput v-model:value="addForm.serviceAddress" type="textarea" placeholder="请输入服务地址" />
        </NFormItem>
        <NFormItem label="预估费用">
          <NInputNumber v-model:value="addForm.estimatedPrice" :min="0" placeholder="元">
            <template #suffix>元</template>
          </NInputNumber>
        </NFormItem>
        <NFormItem label="补贴金额">
          <NInputNumber v-model:value="addForm.subsidyAmount" :min="0" placeholder="元">
            <template #suffix>元</template>
          </NInputNumber>
        </NFormItem>
        <NFormItem label="自付金额">
          <NInputNumber v-model:value="addForm.selfPayAmount" :min="0" placeholder="元">
            <template #suffix>元</template>
          </NInputNumber>
        </NFormItem>
        <NFormItem label="特殊要求">
          <NInput v-model:value="addForm.specialRequirements" type="textarea" placeholder="请输入特殊要求" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="addModalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleAddSubmit">确认</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Assign Modal -->
    <NModal v-model:show="assignModalVisible" title="派单" preset="card" style="width: 600px">
      <div style="margin-bottom: 16px">
        <div style="color: #666; font-size: 13px; margin-bottom: 4px">服务商</div>
        <div style="font-size: 15px; font-weight: 500">{{ currentOrderProviderName || '未指定' }}</div>
      </div>
      <NForm :model="assignForm" label-placement="left" label-width="80">
        <NFormItem label="选择人员">
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
            <div>{{ selectedStaffDetail.gender === 1 ? '男' : '女' }}</div>
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
          <NButton type="primary" @click="handleAssignSubmit">确认派单</NButton>
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

    <!-- Cancel Modal -->
    <NModal v-model:show="cancelModalVisible" title="取消订单" preset="card" style="width: 500px">
      <NForm :model="cancelForm" label-placement="left" label-width="100">
        <NFormItem label="取消原因">
          <NInput v-model:value="cancelForm.reason" type="textarea" placeholder="请输入取消原因" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="cancelModalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleCancelSubmit">确认</NButton>
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
        <NFormItem label="性别">{{ staffDetailData.gender === 1 ? '男' : '女' }}</NFormItem>
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
    <NDrawer v-model:show="orderDetailVisible" :width="560" placement="right" closable>
      <NDrawerContent :title="orderDetailData?.orderNo ? `订单详情 - ${orderDetailData.orderNo}` : '订单详情'" closable>
        <!-- Tab Switch -->
        <div style="display: flex; gap: 8px; margin-bottom: 16px; border-bottom: 1px solid #eee; padding-bottom: 12px">
          <NButton
            :type="activeDetailTab === 'info' ? 'primary' : 'default'"
            size="small"
            @click="activeDetailTab = 'info'"
          >
            基本信息
          </NButton>
          <NButton
            :type="activeDetailTab === 'timeline' ? 'primary' : 'default'"
            size="small"
            @click="activeDetailTab = 'timeline'"
          >
            时间轴
          </NButton>
        </div>

        <!-- Basic Info Tab -->
        <div v-if="activeDetailTab === 'info' && orderDetailData">
          <NDescriptions :column="1" bordered size="small">
            <NDescriptionsItem label="订单号">{{ orderDetailData.orderNo }}</NDescriptionsItem>
            <NDescriptionsItem label="订单状态">
              <NTag :type="getStatusType(orderDetailData.status)" size="small">
                {{ getStatusLabel(orderDetailData.status) }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="客户姓名">{{ orderDetailData.elderName }}</NDescriptionsItem>
            <NDescriptionsItem label="客户手机">{{ orderDetailData.elderPhone }}</NDescriptionsItem>
            <NDescriptionsItem label="服务类型">{{ orderDetailData.serviceTypeName }}</NDescriptionsItem>
            <NDescriptionsItem label="预约时间">
              {{ formatServiceTime(orderDetailData.serviceDate, orderDetailData.serviceTime) }}
            </NDescriptionsItem>
            <NDescriptionsItem label="服务地址">{{ orderDetailData.serviceAddress || '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="服务商">{{ orderDetailData.providerName || '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="服务人员">{{ orderDetailData.staffName || '-' }}</NDescriptionsItem>
            <NDescriptionsItem label="预估费用">¥{{ orderDetailData.estimatedPrice || 0 }}</NDescriptionsItem>
            <NDescriptionsItem label="实际费用">¥{{ orderDetailData.actualPrice || 0 }}</NDescriptionsItem>
            <NDescriptionsItem label="自付金额">¥{{ orderDetailData.selfPayAmount || 0 }}</NDescriptionsItem>
            <NDescriptionsItem label="补贴金额">¥{{ orderDetailData.subsidyAmount || 0 }}</NDescriptionsItem>
            <NDescriptionsItem label="创建时间">{{ orderDetailData.createTime }}</NDescriptionsItem>
            <NDescriptionsItem label="备注">{{ orderDetailData.remark || '-' }}</NDescriptionsItem>
          </NDescriptions>
        </div>

        <!-- Timeline Tab -->
        <div v-if="activeDetailTab === 'timeline'">
          <div v-if="orderTimelineData.length > 0" class="timeline-container">
            <div v-for="(node, index) in orderTimelineData" :key="node.status" class="timeline-node">
              <div
                class="timeline-node-header"
                :class="{
                  'timeline-node-active': isCurrentNode(node),
                  'timeline-node-completed': isCompletedNode(node)
                }"
                @click="toggleNode(node)"
              >
                <div class="timeline-connector">
                  <div class="timeline-dot" :style="{ background: getNodeColor(node) }">
                    <span v-if="isCompletedNode(node) && !isCurrentNode(node)" class="timeline-check">✓</span>
                    <span v-else-if="isCurrentNode(node)" class="timeline-pulse"></span>
                    <span v-else class="timeline-icon">{{ getNodeIcon(node) }}</span>
                  </div>
                  <div
                    v-if="index < orderTimelineData.length - 1"
                    class="timeline-line"
                    :class="{ 'timeline-line-completed': isCompletedNode(node) }"
                  ></div>
                </div>
                <div class="timeline-node-content">
                  <div class="timeline-node-title">
                    <span class="timeline-node-status-icon" :style="{ color: getNodeColor(node) }">
                      {{ getNodeIcon(node) }}
                    </span>
                    <span class="timeline-node-title-text">{{ getStatusLabel(node.status) }}</span>
                    <span class="timeline-node-time">{{ formatTimelineTime(node.time) }}</span>
                  </div>
                  <div class="timeline-node-description">{{ node.description }}</div>
                  <div v-if="node.operator" class="timeline-node-operator">
                    <span class="operator-label">操作人:</span>
                    <span class="operator-name">{{ node.operator }}</span>
                  </div>
                  <!-- 服务中节点：快速查看服务日志 -->
                  <div v-if="node.status === 'SERVICE_STARTED'" class="timeline-node-actions">
                    <NButton text type="primary" size="small" @click.stop="goToServiceLog(orderDetailData.orderNo)">
                      <template #icon>
                        <icon:material-symbols:description-outline />
                      </template>
                      查看服务日志
                    </NButton>
                  </div>
                  <div
                    v-if="node.details && node.details.length > 0"
                    class="timeline-node-details"
                    :class="{ 'timeline-node-details-expanded': expandedNodes.has(node.status) }"
                  >
                    <div class="timeline-details-list">
                      <div v-for="detail in node.details" :key="detail.label" class="timeline-detail-item">
                        <span class="timeline-detail-label">{{ detail.label }}:</span>
                        <span class="timeline-detail-value">{{ detail.value }}</span>
                      </div>
                    </div>
                  </div>
                  <div v-if="node.details && node.details.length > 0" class="timeline-expand-hint">
                    <span>{{ expandedNodes.has(node.status) ? '点击收起' : '点击展开详情' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="timeline-empty">
            <div class="timeline-empty-icon">📋</div>
            <div class="timeline-empty-text">暂无时间轴数据</div>
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
  font-weight: 600;
  color: #333;
  line-height: 1.3;
}

.stat-mini-label {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
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
  background: #11998e;
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
  color: #4facfe;
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
