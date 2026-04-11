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
  NDatePicker,
  useMessage,
  NPagination,
  NStatistic
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import {
  fetchGetOrderList,
  fetchAssignOrder,
  fetchAcceptOrder,
  fetchStartOrder,
  fetchCompleteOrder,
  fetchCancelOrder,
  fetchGetOrderStatistics
} from '@/service/api';

defineOptions({
  name: 'BusinessOrder'
});

const message = useMessage();

// Statistics
const statistics = ref<Api.Order.Statistics>({
  total: 0,
  today: 0,
  month: 0,
  pending: 0,
  assigned: 0,
  inService: 0,
  completed: 0,
  cancelled: 0,
  completionRate: 0,
  avgDuration: 0
});

// Search
const searchOrderNo = ref('');
const searchElderName = ref('');
const searchServiceType = ref('');
const searchStatus = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Table data
const loading = ref(false);
const tableData = ref<Api.Order.Order[]>([]);
const pagination = ref({ page: 1, pageSize: 10, total: 0 });

// Status options
const statusOptions = [
  { label: '待分配', value: 'PENDING' },
  { label: '已分配', value: 'ASSIGNED' },
  { label: '已接单', value: 'ACCEPTED' },
  { label: '服务中', value: 'IN_SERVICE' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
];

function getStatusType(status: Api.Order.OrderStatus): 'warning' | 'success' | 'info' | 'error' | 'default' {
  const map: Record<string, 'warning' | 'success' | 'info' | 'error' | 'default'> = {
    PENDING: 'warning',
    ASSIGNED: 'info',
    ACCEPTED: 'info',
    IN_SERVICE: 'info',
    COMPLETED: 'success',
    CANCELLED: 'error'
  };
  return map[status] || 'default';
}

function getStatusLabel(status: string): string {
  const option = statusOptions.find(o => o.value === status);
  return option?.label || status;
}

const columns: DataTableColumns<Api.Order.Order> = [
  { title: '订单号', key: 'orderNo', width: 160 },
  { title: '老人姓名', key: 'elderName', width: 100 },
  { title: '老人手机', key: 'elderPhone', width: 130 },
  { title: '服务类型', key: 'serviceType', width: 120 },
  { title: '预约服务时间', key: 'serviceTime', width: 170 },
  { title: '服务商', key: 'providerName', width: 150 },
  { title: '服务人员', key: 'staffName', width: 100 },
  {
    title: '订单状态',
    key: 'status',
    width: 100,
    render: row => h(NTag, { type: getStatusType(row.status), size: 'small' }, () => getStatusLabel(row.status))
  },
  { title: '服务费', key: 'serviceFee', width: 100 },
  { title: '实付金额', key: 'actualFee', width: 100 },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    fixed: 'right',
    render: row => {
      const buttons: ReturnType<typeof h>[] = [];
      if (row.status === 'PENDING') {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleAssign(row) }, () => '分配'));
      }
      if (row.status === 'ASSIGNED') {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleAccept(row) }, () => '接单'));
      }
      if (row.status === 'ACCEPTED') {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleStart(row) }, () => '开始服务'));
      }
      if (row.status === 'IN_SERVICE') {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleComplete(row) }, () => '完成服务'));
      }
      if (row.status !== 'COMPLETED' && row.status !== 'CANCELLED') {
        buttons.push(h(NButton, { size: 'small', type: 'error', onClick: () => handleCancel(row) }, () => '取消'));
      }
      return h(NSpace, { size: 'small' }, () => buttons);
    }
  }
];

// Assign modal
const assignModalVisible = ref(false);
const assignForm = ref({ staffId: '' });
const currentOrderId = ref('');

// Cancel modal
const cancelModalVisible = ref(false);
const cancelForm = ref({ reason: '' });

// Complete modal
const completeModalVisible = ref(false);
const completeForm = ref({ actualFee: 0, selfPayFee: 0 });

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

async function getTableData() {
  loading.value = true;
  try {
    const params: Api.Order.OrderQuery & Api.Common.PaginatingQueryParams = {
      current: pagination.value.page,
      pageSize: pagination.value.pageSize
    };
    if (searchOrderNo.value) params.orderNo = searchOrderNo.value;
    if (searchElderName.value) params.elderName = searchElderName.value;
    if (searchServiceType.value) params.serviceType = searchServiceType.value;
    if (searchStatus.value) params.status = searchStatus.value;
    if (searchDateRange.value) {
      params.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      params.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }

    const { data } = await fetchGetOrderList(params);
    tableData.value = data?.records || [];
    pagination.value.total = data?.total || 0;
  } catch (e) {
    console.error('Failed to get table data', e);
  } finally {
    loading.value = false;
  }
}

function handleAssign(row: Api.Order.Order) {
  currentOrderId.value = row.id;
  assignForm.value = { staffId: '' };
  assignModalVisible.value = true;
}

async function handleAssignSubmit() {
  try {
    await fetchAssignOrder(currentOrderId.value, assignForm.value);
    message.success('分配成功');
    assignModalVisible.value = false;
    await getTableData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to assign', e);
  }
}

async function handleAccept(row: Api.Order.Order) {
  try {
    await fetchAcceptOrder(row.id);
    message.success('接单成功');
    await getTableData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to accept', e);
  }
}

async function handleStart(row: Api.Order.Order) {
  try {
    await fetchStartOrder(row.id);
    message.success('开始服务');
    await getTableData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to start', e);
  }
}

function handleComplete(row: Api.Order.Order) {
  currentOrderId.value = row.id;
  completeForm.value = { actualFee: row.actualFee || 0, selfPayFee: row.selfPayFee || 0 };
  completeModalVisible.value = true;
}

async function handleCompleteSubmit() {
  try {
    await fetchCompleteOrder(currentOrderId.value, completeForm.value);
    message.success('完成服务');
    completeModalVisible.value = false;
    await getTableData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to complete', e);
  }
}

function handleCancel(row: Api.Order.Order) {
  currentOrderId.value = row.id;
  cancelForm.value = { reason: '' };
  cancelModalVisible.value = true;
}

async function handleCancelSubmit() {
  try {
    await fetchCancelOrder(currentOrderId.value, cancelForm.value);
    message.success('取消成功');
    cancelModalVisible.value = false;
    await getTableData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to cancel', e);
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
    <NCard title="订单统计" :bordered="false" style="margin-bottom: 16px">
      <div class="statistics-grid">
        <div class="stat-card stat-primary">
          <div class="stat-label">总订单数</div>
          <div class="stat-value">{{ statistics.total }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">今日订单</div>
          <div class="stat-value">{{ statistics.today }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">本月订单</div>
          <div class="stat-value">{{ statistics.month }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">完成率</div>
          <div class="stat-value">{{ statistics.completionRate }}%</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-label">待分配</div>
          <div class="stat-value">{{ statistics.pending }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">已分配</div>
          <div class="stat-value">{{ statistics.assigned }}</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-label">服务中</div>
          <div class="stat-value">{{ statistics.inService }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">已完成</div>
          <div class="stat-value">{{ statistics.completed }}</div>
        </div>
        <div class="stat-card stat-error">
          <div class="stat-label">已取消</div>
          <div class="stat-value">{{ statistics.cancelled }}</div>
        </div>
      </div>
    </NCard>

    <!-- Table -->
    <NCard title="订单管理" :bordered="false">
      <template #header>
        <NSpace :wrap="true">
          <NInput v-model:value="searchOrderNo" placeholder="订单号" clearable style="width: 150px" />
          <NInput v-model:value="searchElderName" placeholder="老人姓名" clearable style="width: 120px" />
          <NSelect
            v-model:value="searchStatus"
            :options="statusOptions"
            placeholder="状态"
            clearable
            style="width: 120px"
          />
          <NDatePicker v-model:value="searchDateRange" type="daterange" clearable style="width: 260px" />
          <NButton type="primary" @click="getTableData">搜索</NButton>
        </NSpace>
      </template>
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1600"
        :row-key="(row: Api.Order.Order) => row.id"
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

    <!-- Assign Modal -->
    <NModal v-model:show="assignModalVisible" title="分配订单" preset="card" style="width: 500px">
      <NForm :model="assignForm" label-placement="left" label-width="100">
        <NFormItem label="服务人员ID">
          <NInput v-model:value="assignForm.staffId" placeholder="请输入服务人员ID" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="assignModalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleAssignSubmit">确认</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Complete Modal -->
    <NModal v-model:show="completeModalVisible" title="完成服务" preset="card" style="width: 500px">
      <NForm :model="completeForm" label-placement="left" label-width="100">
        <NFormItem label="实际收费">
          <NInputNumber v-model:value="completeForm.actualFee" :min="0" />
        </NFormItem>
        <NFormItem label="自付金额">
          <NInputNumber v-model:value="completeForm.selfPayFee" :min="0" />
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
