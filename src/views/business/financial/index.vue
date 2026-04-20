<script setup lang="ts">
import { ref, h, onMounted, watch } from 'vue';
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
  NTabs,
  NTabPane,
  NPagination,
  useMessage,
  NImage,
  NImageGroup,
  NPopconfirm,
  NInputNumber
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import {
  fetchGetSettlementList,
  fetchGetSettlementStatistics,
  fetchGetElder,
  fetchGetStaff,
  fetchGetServicePriceList,
  fetchCreateServicePrice,
  fetchUpdateServicePrice,
  fetchDeleteServicePrice,
  fetchGetServicePrice,
  fetchGetRefundList,
  fetchGetRefund,
  fetchCreateRefund,
  fetchAuditRefund
} from '@/service/api';
import { useNaivePaginatedTable, defaultTransform } from '@/hooks/common/table';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';

defineOptions({
  name: 'BusinessFinancial'
});

const message = useMessage();

// Tab value
const activeTab = ref('settlement');

// Statistics
const statistics = ref<Api.Financial.Statistics>({
  pending: 0,
  completed: 0,
  monthAmount: 0,
  totalAmount: 0,
  serviceFeeTotal: 0,
  subsidyTotal: 0,
  selfPayTotal: 0
});

// ========== Settlement Tab ==========
// Search
const searchOrderNo = ref('');
const searchElderName = ref('');
const searchProviderName = ref('');
const searchStatus = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Elder detail modal
const elderDetailVisible = ref(false);
const elderDetailData = ref<Api.Elder.Elder | null>(null);

// Staff detail modal
const staffDetailVisible = ref(false);
const staffDetailData = ref<Api.Staff.Staff | null>(null);

async function showElderDetail(row: any) {
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

async function showStaffDetail(row: any) {
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

// Status options
const statusOptions = [
  { label: '待结算', value: 'PENDING' },
  { label: '已结算', value: 'SETTLED' },
  { label: '已支付', value: 'PAID' }
];

function getStatusType(status: string): 'warning' | 'success' | 'info' | 'default' {
  const map: Record<string, 'warning' | 'success' | 'info' | 'default'> = {
    PENDING: 'warning',
    SETTLED: 'info',
    PAID: 'success',
    COMPLETED: 'success',
    PROCESSING: 'info',
    FAILED: 'error'
  };
  return map[status] || 'default';
}

function getStatusLabel(status: string): string {
  const option = statusOptions.find(o => o.value === status);
  return option?.label || status;
}

const columns: DataTableColumns<any> = [
  { title: '结算单号', key: 'settlementNo', width: 160 },
  { title: '服务商', key: 'providerName', width: 150 },
  { title: '结算类型', key: 'settlementTypeName', width: 120 },
  {
    title: '服务人员',
    key: 'staffName',
    width: 100,
    render: row =>
      row.staffName
        ? h('a', { style: { color: '#18a058', cursor: 'pointer' }, onClick: () => showStaffDetail(row) }, row.staffName)
        : '-'
  },
  { title: '订单数', key: 'totalOrderCount', width: 80 },
  { title: '总服务费', key: 'totalServiceAmount', width: 100 },
  { title: '补贴金额', key: 'totalSubsidyAmount', width: 100 },
  { title: '自付金额', key: 'totalSelfPayAmount', width: 100 },
  { title: '结算金额', key: 'settlementAmount', width: 100 },
  {
    title: '结算状态',
    key: 'status',
    width: 100,
    render: row => h(NTag, { type: getStatusType(row.status), size: 'small' }, () => getStatusLabel(row.status))
  },
  { title: '确认时间', key: 'confirmTime', width: 170 },
  { title: '创建时间', key: 'createTime', width: 170 }
];

async function getStatistics() {
  try {
    const { data } = await fetchGetSettlementStatistics();
    if (data) {
      statistics.value = data;
    }
  } catch (e) {
    console.error('Failed to get statistics', e);
  }
}

// Use framework's table hook
const tableHookResult = useNaivePaginatedTable<any, any>({
  apiFn: async params => {
    const queryParams: any = {
      page: params.page,
      pageSize: params.pageSize
    };
    if (searchOrderNo.value) queryParams.orderNo = searchOrderNo.value;
    if (searchElderName.value) queryParams.elderName = searchElderName.value;
    if (searchProviderName.value) queryParams.providerName = searchProviderName.value;
    if (searchStatus.value) queryParams.status = searchStatus.value;
    if (searchDateRange.value) {
      queryParams.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      queryParams.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }
    return fetchGetSettlementList(queryParams);
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

function handleResetSearch() {
  searchOrderNo.value = '';
  searchElderName.value = '';
  searchProviderName.value = '';
  searchStatus.value = '';
  searchDateRange.value = null;
  getData();
}

// ========== Service Price Tab ==========
// Service price list
const priceLoading = ref(false);
const priceData = ref<Api.Financial.ServicePrice[]>([]);
const pricePagination = ref({ page: 1, pageSize: 10, itemCount: 0 });

// Service price modal
const priceModalVisible = ref(false);
const priceModalLoading = ref(false);
const priceFormData = ref<Api.Financial.ServicePriceForm>({
  serviceTypeCode: '',
  serviceTypeName: '',
  providerId: '',
  providerName: '',
  governmentSubsidy: 0,
  selfPayStandard: 0,
  minimumFee: 0,
  maximumFee: 0,
  timeUnit: 60,
  status: 'ACTIVE',
  remark: ''
});
const isPriceEdit = ref(false);
const priceEditId = ref('');

async function loadPriceData() {
  priceLoading.value = true;
  try {
    const { data } = await fetchGetServicePriceList({
      page: pricePagination.value.page,
      pageSize: pricePagination.value.pageSize
    });
    if (data) {
      priceData.value = data.records;
      pricePagination.value.itemCount = data.total;
    }
  } finally {
    priceLoading.value = false;
  }
}

function showAddPriceModal() {
  priceFormData.value = {
    serviceTypeCode: '',
    serviceTypeName: '',
    providerId: '',
    providerName: '',
    governmentSubsidy: 0,
    selfPayStandard: 0,
    minimumFee: 0,
    maximumFee: 0,
    timeUnit: 60,
    status: 'ACTIVE',
    remark: ''
  };
  isPriceEdit.value = false;
  priceEditId.value = '';
  priceModalVisible.value = true;
}

async function showEditPriceModal(row: Api.Financial.ServicePrice) {
  const { data } = await fetchGetServicePrice(row.id);
  if (data) {
    priceFormData.value = {
      serviceTypeCode: data.serviceTypeCode,
      serviceTypeName: data.serviceTypeName,
      providerId: data.providerId,
      providerName: data.providerName || '',
      governmentSubsidy: data.governmentSubsidy,
      selfPayStandard: data.selfPayStandard,
      minimumFee: data.minimumFee || 0,
      maximumFee: data.maximumFee || 0,
      timeUnit: data.timeUnit || 60,
      status: data.status,
      remark: data.remark || ''
    };
    isPriceEdit.value = true;
    priceEditId.value = row.id;
    priceModalVisible.value = true;
  }
}

async function handleSavePrice() {
  priceModalLoading.value = true;
  try {
    if (isPriceEdit.value) {
      const { error } = await fetchUpdateServicePrice(priceEditId.value, priceFormData.value);
      if (error) {
        message.error(error.message || '更新失败');
        return;
      }
      message.success('更新成功');
    } else {
      const { error } = await fetchCreateServicePrice(priceFormData.value);
      if (error) {
        message.error(error.message || '创建失败');
        return;
      }
      message.success('创建成功');
    }
    priceModalVisible.value = false;
    loadPriceData();
  } finally {
    priceModalLoading.value = false;
  }
}

async function handleDeletePrice(row: Api.Financial.ServicePrice) {
  const { error } = await fetchDeleteServicePrice(row.id);
  if (error) {
    message.error(error.message || '删除失败');
    return;
  }
  message.success('删除成功');
  loadPriceData();
}

// ========== Refund Tab ==========
// Refund list
const refundLoading = ref(false);
const refundData = ref<Api.Financial.Refund[]>([]);
const refundPagination = ref({ page: 1, pageSize: 10, itemCount: 0 });

// Refund detail modal
const refundDetailVisible = ref(false);
const refundDetailData = ref<Api.Financial.Refund | null>(null);

// Refund audit modal
const refundAuditVisible = ref(false);
const refundAuditData = ref({ result: 'APPROVED', remark: '' });
const refundAuditLoading = ref(false);
const currentRefundId = ref('');

async function loadRefundData() {
  refundLoading.value = true;
  try {
    const { data } = await fetchGetRefundList({
      page: refundPagination.value.page,
      pageSize: refundPagination.value.pageSize
    });
    if (data) {
      refundData.value = data.records;
      refundPagination.value.itemCount = data.total;
    }
  } finally {
    refundLoading.value = false;
  }
}

async function showRefundDetail(row: Api.Financial.Refund) {
  const { data } = await fetchGetRefund(row.id);
  if (data) {
    refundDetailData.value = data;
    refundDetailVisible.value = true;
  }
}

function showRefundAuditModal(row: Api.Financial.Refund) {
  refundDetailData.value = row;
  refundAuditData.value = { result: 'APPROVED', remark: '' };
  currentRefundId.value = row.id;
  refundAuditVisible.value = true;
}

async function handleRefundAudit() {
  refundAuditLoading.value = true;
  try {
    const { error } = await fetchAuditRefund(currentRefundId.value, refundAuditData.value);
    if (error) {
      message.error(error.message || '审核失败');
      return;
    }
    message.success('审核成功');
    refundAuditVisible.value = false;
    loadRefundData();
  } finally {
    refundAuditLoading.value = false;
  }
}

function getRefundStatusType(status: string): 'warning' | 'success' | 'error' | 'info' | 'default' {
  const map: Record<string, 'warning' | 'success' | 'error' | 'info' | 'default'> = {
    PENDING: 'warning',
    APPROVED: 'info',
    REJECTED: 'error',
    COMPLETED: 'success',
    FAILED: 'error'
  };
  return map[status] || 'default';
}

function getRefundStatusLabel(status: string): string {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    COMPLETED: '已完成',
    FAILED: '失败'
  };
  return map[status] || status;
}

const refundColumns: DataTableColumns<Api.Financial.Refund> = [
  { title: '退款单号', key: 'refundNo', width: 160 },
  { title: '订单号', key: 'orderNo', width: 160 },
  { title: '服务商', key: 'providerName', width: 150 },
  { title: '服务人员', key: 'staffName', width: 100 },
  { title: '老人姓名', key: 'elderName', width: 100 },
  { title: '退款金额', key: 'amount', width: 100 },
  { title: '退款原因', key: 'reason', width: 150, ellipsis: { tooltip: true } },
  {
    title: '退款状态',
    key: 'status',
    width: 100,
    render: row => h(NTag, { type: getRefundStatusType(row.status), size: 'small' }, () => getRefundStatusLabel(row.status))
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: row => {
      const actions = [];
      actions.push(
        h(NButton, { size: 'small', type: 'primary', onClick: () => showRefundDetail(row), style: { marginRight: '8px' } }, () => '查看')
      );
      if (row.status === 'PENDING') {
        actions.push(
          h(NButton, { size: 'small', type: 'warning', onClick: () => showRefundAuditModal(row), style: { marginRight: '8px' } }, () => '审核')
        );
      }
      return h(NSpace, null, () => actions);
    }
  }
];

const priceColumns: DataTableColumns<Api.Financial.ServicePrice> = [
  { title: '服务类型编码', key: 'serviceTypeCode', width: 120 },
  { title: '服务类型名称', key: 'serviceTypeName', width: 150 },
  { title: '服务商', key: 'providerName', width: 150 },
  { title: '政府补贴', key: 'governmentSubsidy', width: 100 },
  { title: '自付标准', key: 'selfPayStandard', width: 100 },
  { title: '最低收费', key: 'minimumFee', width: 100 },
  { title: '最高收费', key: 'maximumFee', width: 100 },
  { title: '计时单位', key: 'timeUnit', width: 100 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: row => h(NTag, { type: row.status === 'ACTIVE' ? 'success' : 'default', size: 'small' }, () => row.status === 'ACTIVE' ? '启用' : '禁用')
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: row => {
      const actions = [];
      actions.push(
        h(NButton, { size: 'small', type: 'primary', onClick: () => showEditPriceModal(row), style: { marginRight: '8px' } }, () => '编辑')
      );
      actions.push(
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDeletePrice(row) },
          {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'),
            default: () => '确认删除？'
          }
        )
      );
      return h(NSpace, null, () => actions);
    }
  }
];

onMounted(() => {
  getStatistics();
  getData();
  loadPriceData();
  loadRefundData();
});
</script>

<template>
  <div>
    <!-- Statistics Cards -->
    <NCard title="财务统计" :bordered="false" style="margin-bottom: 16px">
      <div class="statistics-grid">
        <div class="stat-card stat-primary">
          <div class="stat-label">总金额</div>
          <div class="stat-value">¥{{ Number(statistics.totalAmount || 0).toFixed(0) }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">补贴金额</div>
          <div class="stat-value">¥{{ Number(statistics.subsidyTotal || 0).toFixed(0) }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">自付金额</div>
          <div class="stat-value">¥{{ Number(statistics.selfPayTotal || 0).toFixed(0) }}</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-label">待结算金额</div>
          <div class="stat-value">¥{{ Number(statistics.pending || 0).toFixed(0) }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">结算单数</div>
          <div class="stat-value">{{ statistics.completed }}</div>
        </div>
      </div>
    </NCard>

    <!-- Tabs -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <NTabs v-model:value="activeTab" type="line">
        <!-- Settlement Tab -->
        <NTabPane name="settlement" tab="结算管理">
          <div style="background: #f5f5f5; padding: 12px; margin-bottom: 12px; border-radius: 4px">
            <NSpace :wrap="true" align="center">
              <NInput v-model:value="searchOrderNo" placeholder="订单号" clearable style="width: 150px" />
              <NInput v-model:value="searchElderName" placeholder="老人姓名" clearable style="width: 100px" />
              <NInput v-model:value="searchProviderName" placeholder="服务商" clearable style="width: 150px" />
              <NSelect
                v-model:value="searchStatus"
                :options="statusOptions"
                placeholder="结算状态"
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
            :loading="loading"
            @refresh="getData"
          />

          <NDataTable
            :columns="columns"
            :data="tableData"
            :loading="loading"
            :scroll-x="1500"
            :row-key="(row: any) => row.settlementId || row.id"
            remote
            :pagination="mobilePagination"
          />
        </NTabPane>

        <!-- Service Price Tab -->
        <NTabPane name="price" tab="服务定价">
          <div style="margin-bottom: 12px">
            <NButton type="primary" @click="showAddPriceModal">新增定价</NButton>
          </div>

          <NDataTable
            :columns="priceColumns"
            :data="priceData"
            :loading="priceLoading"
            :scroll-x="1200"
            :row-key="(row: Api.Financial.ServicePrice) => row.id"
            :pagination="false"
          />
          <div style="margin-top: 12px; display: flex; justify-content: flex-end">
            <NPagination
              v-model:page="pricePagination.page"
              :page-size="pricePagination.pageSize"
              :item-count="pricePagination.itemCount"
              @update:page="loadPriceData"
            />
          </div>
        </NTabPane>

        <!-- Refund Tab -->
        <NTabPane name="refund" tab="退款管理">
          <NDataTable
            :columns="refundColumns"
            :data="refundData"
            :loading="refundLoading"
            :scroll-x="1400"
            :row-key="(row: Api.Financial.Refund) => row.id"
            :pagination="false"
          />
          <div style="margin-top: 12px; display: flex; justify-content: flex-end">
            <NPagination
              v-model:page="refundPagination.page"
              :page-size="refundPagination.pageSize"
              :item-count="refundPagination.itemCount"
              @update:page="loadRefundData"
            />
          </div>
        </NTabPane>
      </NTabs>
    </NCard>

    <!-- Elder Detail Modal -->
    <NModal v-model:show="elderDetailVisible" title="老人档案详情" preset="card" style="width: 600px">
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

    <!-- Service Price Modal -->
    <NModal v-model:show="priceModalVisible" :title="isPriceEdit ? '编辑定价' : '新增定价'" preset="card" style="width: 500px">
      <NForm label-placement="left" label-width="100">
        <NFormItem label="服务类型编码">
          <NInput v-model:value="priceFormData.serviceTypeCode" placeholder="请输入服务类型编码" />
        </NFormItem>
        <NFormItem label="服务类型名称">
          <NInput v-model:value="priceFormData.serviceTypeName" placeholder="请输入服务类型名称" />
        </NFormItem>
        <NFormItem label="政府补贴">
          <NInput-number v-model:value="priceFormData.governmentSubsidy" :min="0" style="width: 100%" />
        </NFormItem>
        <NFormItem label="自付标准">
          <NInput-number v-model:value="priceFormData.selfPayStandard" :min="0" style="width: 100%" />
        </NFormItem>
        <NFormItem label="最低收费">
          <NInput-number v-model:value="priceFormData.minimumFee" :min="0" style="width: 100%" />
        </NFormItem>
        <NFormItem label="最高收费">
          <NInput-number v-model:value="priceFormData.maximumFee" :min="0" style="width: 100%" />
        </NFormItem>
        <NFormItem label="计时单位(分钟)">
          <NInput-number v-model:value="priceFormData.timeUnit" :min="1" style="width: 100%" />
        </NFormItem>
        <NFormItem label="状态">
          <NSelect
            v-model:value="priceFormData.status"
            :options="[{ label: '启用', value: 'ACTIVE' }, { label: '禁用', value: 'INACTIVE' }]"
            style="width: 100%"
          />
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="priceFormData.remark" type="textarea" placeholder="请输入备注" :rows="2" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="priceModalVisible = false">取消</NButton>
          <NButton type="primary" :loading="priceModalLoading" @click="handleSavePrice">保存</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Refund Detail Modal -->
    <NModal v-model:show="refundDetailVisible" title="退款详情" preset="card" style="width: 600px">
      <NForm v-if="refundDetailData" label-placement="left" label-width="100">
        <NFormItem label="退款单号">{{ refundDetailData.refundNo }}</NFormItem>
        <NFormItem label="订单号">{{ refundDetailData.orderNo || '-' }}</NFormItem>
        <NFormItem label="服务商">{{ refundDetailData.providerName || '-' }}</NFormItem>
        <NFormItem label="服务人员">{{ refundDetailData.staffName || '-' }}</NFormItem>
        <NFormItem label="老人姓名">{{ refundDetailData.elderName || '-' }}</NFormItem>
        <NFormItem label="退款金额">¥{{ refundDetailData.amount }}</NFormItem>
        <NFormItem label="退款类型">{{ refundDetailData.refundType || '-' }}</NFormItem>
        <NFormItem label="退款原因">{{ refundDetailData.reason }}</NFormItem>
        <NFormItem label="退款状态">
          <NTag :type="getRefundStatusType(refundDetailData.status)" size="small">
            {{ getRefundStatusLabel(refundDetailData.status) }}
          </NTag>
        </NFormItem>
        <NFormItem label="审核备注" v-if="refundDetailData.auditRemark">{{ refundDetailData.auditRemark }}</NFormItem>
        <NFormItem label="审核时间" v-if="refundDetailData.auditTime">{{ refundDetailData.auditTime }}</NFormItem>
        <NFormItem label="审核人" v-if="refundDetailData.auditorName">{{ refundDetailData.auditorName }}</NFormItem>
        <NFormItem label="创建时间">{{ refundDetailData.createTime }}</NFormItem>
      </NForm>
    </NModal>

    <!-- Refund Audit Modal -->
    <NModal v-model:show="refundAuditVisible" title="退款审核" preset="card" style="width: 500px">
      <NForm label-placement="left" label-width="80">
        <NFormItem label="审核结果">
          <NSelect
            v-model:value="refundAuditData.result"
            :options="[
              { label: '通过', value: 'APPROVED' },
              { label: '驳回', value: 'REJECTED' }
            ]"
            style="width: 200px"
          />
        </NFormItem>
        <NFormItem label="审核备注">
          <NInput
            v-model:value="refundAuditData.remark"
            type="textarea"
            placeholder="请输入审核备注"
            :rows="3"
          />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="refundAuditVisible = false">取消</NButton>
          <NButton type="primary" :loading="refundAuditLoading" @click="handleRefundAudit">提交</NButton>
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
