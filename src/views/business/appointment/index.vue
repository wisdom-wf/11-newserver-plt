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
  NBadge,
  NStatistic,
  NPagination
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import {
  fetchGetAppointmentList,
  fetchConfirmAppointment,
  fetchAssignAppointment,
  fetchCancelAppointment,
  fetchInvalidateAppointment,
  fetchDownloadAppointmentTemplate,
  fetchGetAppointmentStatistics
} from '@/service/api';

defineOptions({
  name: 'BusinessAppointment'
});

const message = useMessage();

// Statistics
const statistics = ref({
  total: 0,
  pending: 0,
  confirmed: 0,
  assigned: 0,
  completed: 0,
  cancelled: 0,
  invalid: 0
});

// Search
const searchAppointmentNo = ref('');
const searchElderName = ref('');
const searchElderPhone = ref('');
const searchServiceType = ref('');
const searchStatus = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Table data
const loading = ref(false);
const tableData = ref<Api.Appointment.Appointment[]>([]);
const pagination = ref({ page: 1, pageSize: 10, total: 0 });

// Status options
const statusOptions = [
  { label: '待确认', value: 'PENDING' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已分配', value: 'ASSIGNED' },
  { label: '服务中', value: 'IN_SERVICE' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '已作废', value: 'INVALID' }
];

// Status tag type
function getStatusType(
  status: Api.Appointment.AppointmentStatus
): 'warning' | 'success' | 'info' | 'error' | 'default' {
  const map: Record<string, 'warning' | 'success' | 'info' | 'error' | 'default'> = {
    PENDING: 'warning',
    CONFIRMED: 'success',
    ASSIGNED: 'info',
    IN_SERVICE: 'info',
    COMPLETED: 'success',
    CANCELLED: 'error',
    INVALID: 'error'
  };
  return map[status] || 'default';
}

function getStatusLabel(status: string): string {
  const option = statusOptions.find(o => o.value === status);
  return option?.label || status;
}

const columns: DataTableColumns<Api.Appointment.Appointment> = [
  { title: '预约单号', key: 'appointmentNo', width: 160 },
  { title: '老人姓名', key: 'elderName', width: 100 },
  { title: '老人手机号', key: 'elderPhone', width: 130 },
  { title: '服务类型', key: 'serviceType', width: 120 },
  { title: '预约时间', key: 'appointmentTime', width: 170 },
  { title: '服务商', key: 'providerName', width: 150 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: row => h(NTag, { type: getStatusType(row.status), size: 'small' }, () => getStatusLabel(row.status))
  },
  { title: '备注', key: 'remark', width: 150, ellipsis: { tooltip: true } },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: row => {
      const buttons: ReturnType<typeof h>[] = [];
      if (row.status === 'PENDING') {
        buttons.push(
          h(NButton, { size: 'small', onClick: () => handleConfirm(row) }, () => '确认'),
          h(NButton, { size: 'small', onClick: () => handleCancel(row) }, () => '取消')
        );
      }
      if (row.status === 'CONFIRMED') {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleAssign(row) }, () => '分配'));
      }
      if (row.status !== 'COMPLETED' && row.status !== 'CANCELLED' && row.status !== 'INVALID') {
        buttons.push(h(NButton, { size: 'small', type: 'error', onClick: () => handleInvalidate(row) }, () => '作废'));
      }
      return h(NSpace, { size: 'small' }, () => buttons);
    }
  }
];

// Confirm modal
const confirmModalVisible = ref(false);
const confirmForm = ref({ providerId: '', appointmentTime: '' });

// Cancel modal
const cancelModalVisible = ref(false);
const cancelForm = ref({ reason: '' });

// Assign modal
const assignModalVisible = ref(false);
const assignForm = ref({ providerId: '' });

// Invalidate modal
const invalidateModalVisible = ref(false);
const invalidateForm = ref({ reason: '' });

// Current row for modals
const currentRow = ref<Api.Appointment.Appointment | null>(null);

async function getStatistics() {
  try {
    const { data } = await fetchGetAppointmentStatistics();
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
    const params: Api.Appointment.AppointmentQuery & Api.Common.PaginatingQueryParams = {
      current: pagination.value.page,
      pageSize: pagination.value.pageSize
    };
    if (searchAppointmentNo.value) params.appointmentNo = searchAppointmentNo.value;
    if (searchElderName.value) params.elderName = searchElderName.value;
    if (searchElderPhone.value) params.elderPhone = searchElderPhone.value;
    if (searchServiceType.value) params.serviceTypeCode = searchServiceType.value;
    if (searchStatus.value) params.status = searchStatus.value;
    if (searchDateRange.value) {
      params.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      params.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }

    const { data } = await fetchGetAppointmentList(params);
    tableData.value = data?.records || [];
    pagination.value.total = data?.total || 0;
  } catch (e) {
    console.error('Failed to get table data', e);
  } finally {
    loading.value = false;
  }
}

function handleConfirm(row: Api.Appointment.Appointment) {
  currentRow.value = row;
  confirmForm.value = { providerId: '', appointmentTime: row.appointmentTime };
  confirmModalVisible.value = true;
}

async function handleConfirmSubmit() {
  if (!currentRow.value) return;
  try {
    await fetchConfirmAppointment(currentRow.value.id, confirmForm.value);
    message.success('确认成功');
    confirmModalVisible.value = false;
    await getTableData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to confirm', e);
  }
}

function handleCancel(row: Api.Appointment.Appointment) {
  currentRow.value = row;
  cancelForm.value = { reason: '' };
  cancelModalVisible.value = true;
}

async function handleCancelSubmit() {
  if (!currentRow.value) return;
  try {
    await fetchCancelAppointment(currentRow.value.id, cancelForm.value);
    message.success('取消成功');
    cancelModalVisible.value = false;
    await getTableData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to cancel', e);
  }
}

function handleAssign(row: Api.Appointment.Appointment) {
  currentRow.value = row;
  assignForm.value = { providerId: '' };
  assignModalVisible.value = true;
}

async function handleAssignSubmit() {
  if (!currentRow.value) return;
  try {
    await fetchAssignAppointment(currentRow.value.id, assignForm.value);
    message.success('分配成功');
    assignModalVisible.value = false;
    await getTableData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to assign', e);
  }
}

function handleInvalidate(row: Api.Appointment.Appointment) {
  currentRow.value = row;
  invalidateForm.value = { reason: '' };
  invalidateModalVisible.value = true;
}

async function handleInvalidateSubmit() {
  if (!currentRow.value) return;
  try {
    await fetchInvalidateAppointment(currentRow.value.id, invalidateForm.value);
    message.success('作废成功');
    invalidateModalVisible.value = false;
    await getTableData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to invalidate', e);
  }
}

async function handleDownloadTemplate() {
  try {
    await fetchDownloadAppointmentTemplate();
    message.success('模板下载成功');
  } catch (e) {
    console.error('Failed to download template', e);
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
    <NCard title="预约统计" :bordered="false" style="margin-bottom: 16px">
      <NSpace :size="20" :wrap="true">
        <NStatistic label="总预约数" :value="statistics.total" />
        <NStatistic label="待确认" :value="statistics.pending">
          <template #suffix>
            <NBadge value="PENDING" type="warning" />
          </template>
        </NStatistic>
        <NStatistic label="已确认" :value="statistics.confirmed" />
        <NStatistic label="已分配" :value="statistics.assigned" />
        <NStatistic label="已完成" :value="statistics.completed" />
        <NStatistic label="已取消" :value="statistics.cancelled" />
        <NStatistic label="已作废" :value="statistics.invalid" />
      </NSpace>
    </NCard>

    <!-- Table -->
    <NCard title="预约信息管理" :bordered="false">
      <template #header>
        <NSpace :wrap="true">
          <NInput v-model:value="searchAppointmentNo" placeholder="预约单号" clearable style="width: 150px" />
          <NInput v-model:value="searchElderName" placeholder="老人姓名" clearable style="width: 120px" />
          <NInput v-model:value="searchElderPhone" placeholder="老人手机号" clearable style="width: 130px" />
          <NSelect
            v-model:value="searchStatus"
            :options="statusOptions"
            placeholder="状态"
            clearable
            style="width: 120px"
          />
          <NDatePicker v-model:value="searchDateRange" type="daterange" clearable style="width: 260px" />
          <NButton type="primary" @click="getTableData">搜索</NButton>
          <NButton @click="handleDownloadTemplate">下载模板</NButton>
        </NSpace>
      </template>
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1400"
        :row-key="(row: Api.Appointment.Appointment) => row.id"
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

    <!-- Confirm Modal -->
    <NModal v-model:show="confirmModalVisible" title="确认预约" preset="card" style="width: 500px">
      <NForm :model="confirmForm" label-placement="left" label-width="100">
        <NFormItem label="预约时间">
          <NInput v-model:value="confirmForm.appointmentTime" placeholder="请输入预约时间" />
        </NFormItem>
        <NFormItem label="服务商ID">
          <NInput v-model:value="confirmForm.providerId" placeholder="请输入服务商ID" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="confirmModalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleConfirmSubmit">确认</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Cancel Modal -->
    <NModal v-model:show="cancelModalVisible" title="取消预约" preset="card" style="width: 500px">
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

    <!-- Assign Modal -->
    <NModal v-model:show="assignModalVisible" title="分配预约" preset="card" style="width: 500px">
      <NForm :model="assignForm" label-placement="left" label-width="100">
        <NFormItem label="服务商ID">
          <NInput v-model:value="assignForm.providerId" placeholder="请输入服务商ID" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="assignModalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleAssignSubmit">确认</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Invalidate Modal -->
    <NModal v-model:show="invalidateModalVisible" title="作废预约" preset="card" style="width: 500px">
      <NForm :model="invalidateForm" label-placement="left" label-width="100">
        <NFormItem label="作废原因">
          <NInput v-model:value="invalidateForm.reason" type="textarea" placeholder="请输入作废原因" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="invalidateModalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleInvalidateSubmit">确认</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
