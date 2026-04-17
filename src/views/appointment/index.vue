<script setup lang="ts">
import { ref, h, onMounted, computed } from 'vue';
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
  NUpload,
  NAlert,
  NDrawer,
  NDrawerContent,
  NSpin
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import {
  fetchGetAppointmentList,
  fetchCreateAppointment,
  fetchConfirmAppointment,
  fetchCancelAppointment,
  fetchInvalidateAppointment,
  fetchDownloadAppointmentTemplate,
  fetchImportAppointment,
  fetchGetAppointmentStatistics,
  fetchGetProviderOptions,
  fetchGetAppointmentTimeline
} from '@/service/api';
import { useRouterPush } from '@/hooks/common/router';
import { useNaivePaginatedTable, useTableOperate, defaultTransform } from '@/hooks/common/table';
import { useNaiveForm, useFormRules } from '@/hooks/common/form';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';

defineOptions({
  name: 'BusinessAppointment'
});

const message = useMessage();
const { routerPushByKey } = useRouterPush();
const { formRef, validate, restoreValidation } = useNaiveForm();
const { defaultRequiredRule } = useFormRules();

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

// Provider options
const providerOptions = ref<{ label: string; value: string }[]>([]);

async function getProviderOptions() {
  try {
    const data = await fetchGetProviderOptions();
    if (data && data.length > 0) {
      providerOptions.value = data.map((item: { id: string; name: string }) => ({
        label: item.name,
        value: item.id
      }));
    }
  } catch (e) {
    console.error('Failed to get provider options', e);
  }
}

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

// Table columns
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
    width: 320,
    fixed: 'right',
    render: row => {
      const buttons: ReturnType<typeof h>[] = [];
      // Always show timeline button
      buttons.push(h(NButton, { size: 'small', quaternary: true, type: 'info', onClick: () => showTimeline(row) }, () => '详情'));
      if (row.status === 'PENDING') {
        buttons.push(
          h(NButton, { size: 'small', onClick: () => handleConfirm(row) }, () => '确认'),
          h(NButton, { size: 'small', onClick: () => handleCancel(row) }, () => '取消')
        );
      }
      if (row.status === 'PENDING') {
        buttons.push(h(NButton, { size: 'small', type: 'error', onClick: () => handleInvalidate(row) }, () => '作废'));
      }
      if (row.status === 'CONFIRMED') {
        buttons.push(h(NButton, { size: 'small', type: 'info', onClick: () => handleViewOrder(row) }, () => '查看订单'));
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
} = useNaivePaginatedTable<Api.Common.PaginatingQueryRecord<Api.Appointment.Appointment>, Api.Appointment.Appointment>({
  apiFn: async params => {
    const queryParams: Api.Appointment.AppointmentQuery & Api.Common.PaginatingQueryParams = {
      current: params.page,
      pageSize: params.pageSize
    };
    if (searchAppointmentNo.value) queryParams.appointmentNo = searchAppointmentNo.value;
    if (searchElderName.value) queryParams.elderName = searchElderName.value;
    if (searchElderPhone.value) queryParams.elderPhone = searchElderPhone.value;
    if (searchServiceType.value) queryParams.serviceTypeCode = searchServiceType.value;
    if (searchStatus.value) queryParams.status = searchStatus.value;
    if (searchDateRange.value) {
      queryParams.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      queryParams.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }
    return fetchGetAppointmentList(queryParams);
  },
  apiParams: {
    current: 1,
    pageSize: 10
  },
  transform: defaultTransform,
  columns: () => columns
});

// Table checked row keys
const checkedRowKeys = ref<string[]>([]);

// Add modal
const addModalVisible = ref(false);
const addForm = ref({
  elderName: '',
  elderPhone: '',
  elderIdCard: '',
  elderAddress: '',
  serviceType: '',
  serviceTypeCode: '',
  appointmentTime: '',
  remark: ''
});

// Service type options
const serviceTypeOptions = [
  { label: '上门服务', value: '上门服务', code: 'DOOR_TO_DOOR' },
  { label: '日间照料', value: '日间照料', code: 'DAY_CARE' },
  { label: '助餐服务', value: '助餐服务', code: 'MEAL' },
  { label: '助洁服务', value: '助洁服务', code: 'CLEANING' },
  { label: '助浴服务', value: '助浴服务', code: 'BATHING' },
  { label: '健康监测', value: '健康监测', code: 'HEALTH' },
  { label: '康复护理', value: '康复护理', code: 'REHAB' },
  { label: '精神慰藉', value: '精神慰藉', code: 'COMFORT' },
  { label: '信息咨询', value: '信息咨询', code: 'INFO' },
  { label: '紧急救援', value: '紧急救援', code: 'EMERGENCY' }
];

function handleAdd() {
  addForm.value = {
    elderName: '',
    elderPhone: '',
    elderIdCard: '',
    elderAddress: '',
    serviceType: '',
    serviceTypeCode: '',
    appointmentTime: '',
    remark: ''
  };
  addModalVisible.value = true;
}

async function handleAddSubmit() {
  try {
    const selectedService = serviceTypeOptions.find(s => s.value === addForm.value.serviceType);
    const data = {
      ...addForm.value,
      serviceTypeCode: selectedService?.code || ''
    };
    await fetchCreateAppointment(data);
    message.success('添加预约成功');
    addModalVisible.value = false;
    await getData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to add appointment', e);
    message.error(e?.message || '添加失败');
  }
}

// Confirm modal
const confirmModalVisible = ref(false);
const confirmForm = ref({ providerId: '', appointmentTime: '' });

// Cancel modal
const cancelModalVisible = ref(false);
const cancelForm = ref({ reason: '' });

// Invalidate modal
const invalidateModalVisible = ref(false);
const invalidateForm = ref({ reason: '' });

// Import modal
const importModalVisible = ref(false);
const importFile = ref<File | null>(null);
const importResult = ref<{ successCount: number; failCount: number; errors: string[] } | null>(null);
const importing = ref(false);

// Current row for modals
const currentRow = ref<Api.Appointment.Appointment | null>(null);

// Timeline drawer
const timelineDrawerVisible = ref(false);
const timelineData = ref<Api.Appointment.AppointmentTimeline | null>(null);
const timelineLoading = ref(false);
const expandedNodes = ref<Set<string>>(new Set());

async function showTimeline(row: Api.Appointment.Appointment) {
  timelineDrawerVisible.value = true;
  timelineLoading.value = true;
  expandedNodes.value = new Set();
  try {
    const { data } = await fetchGetAppointmentTimeline(row.appointmentId);
    if (data) {
      timelineData.value = data;
      data.nodes?.forEach((n: Api.Appointment.AppointmentTimelineNode) => {
        expandedNodes.value.add(n.status);
      });
    }
  } catch (e) {
    console.error('Failed to get timeline', e);
    message.error('获取时间轴失败');
  } finally {
    timelineLoading.value = false;
  }
}

function toggleNode(status: string) {
  if (expandedNodes.value.has(status)) {
    expandedNodes.value.delete(status);
  } else {
    expandedNodes.value.add(status);
  }
}

function goToOrder(orderId: string) {
  timelineDrawerVisible.value = false;
  routerPushByKey('business_order_detail', { query: { orderId } });
}

function getNodeIcon(node: Api.Appointment.AppointmentTimelineNode) {
  if (node.completed) return '✓';
  if (node.active) return '●';
  return '○';
}

function getNodeColor(node: Api.Appointment.AppointmentTimelineNode) {
  if (node.completed) return '#11998e';
  if (node.active) return '#4facfe';
  if (node.status === 'CANCELLED' || node.status === 'INVALID') return '#f5576c';
  return '#999';
}

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

async function handleConfirm(row: Api.Appointment.Appointment) {
  currentRow.value = row;
  confirmForm.value = { providerId: '', appointmentTime: row.appointmentTime };
  await getProviderOptions();
  confirmModalVisible.value = true;
}

async function handleConfirmSubmit() {
  if (!currentRow.value) return;
  try {
    await fetchConfirmAppointment(currentRow.value.appointmentId, confirmForm.value);
    message.success('确认成功');
    confirmModalVisible.value = false;
    await getData();
    try {
      await getStatistics();
    } catch (statsError) {
      console.warn('获取统计信息失败:', statsError);
    }
  } catch (e) {
    console.error('Failed to confirm', e);
    message.error(e?.message || '确认失败，请重试');
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
    await fetchCancelAppointment(currentRow.value.appointmentId, cancelForm.value);
    message.success('取消成功');
    cancelModalVisible.value = false;
    await getData();
    try {
      await getStatistics();
    } catch (statsError) {
      console.warn('获取统计信息失败:', statsError);
    }
  } catch (e) {
    console.error('Failed to cancel', e);
    message.error(e?.message || '取消失败，请重试');
  }
}

function handleInvalidate(row: Api.Appointment.Appointment) {
  currentRow.value = row;
  invalidateForm.value = { reason: '' };
  invalidateModalVisible.value = true;
}

async function handleViewOrder(row: Api.Appointment.Appointment) {
  await routerPushByKey('business_order', {
    query: {
      elderName: row.elderName,
      appointmentTime: row.appointmentTime
    }
  });
}

async function handleInvalidateSubmit() {
  if (!currentRow.value) return;
  try {
    await fetchInvalidateAppointment(currentRow.value.appointmentId, invalidateForm.value);
    message.success('作废成功');
    invalidateModalVisible.value = false;
    await getData();
    try {
      await getStatistics();
    } catch (statsError) {
      console.warn('获取统计信息失败:', statsError);
    }
  } catch (e) {
    console.error('Failed to invalidate', e);
    message.error(e?.message || '作废失败，请重试');
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

function handleOpenImport() {
  importFile.value = null;
  importResult.value = null;
  importModalVisible.value = true;
}

function handleImportFile(options: { file: File }) {
  importFile.value = options.file;
  return false;
}

async function handleImportSubmit() {
  if (!importFile.value) {
    message.warning('请选择要导入的文件');
    return;
  }
  importing.value = true;
  try {
    const res = await fetchImportAppointment(importFile.value);
    importResult.value = res.data;
    if (res.data && res.data.failCount === 0) {
      message.success(`导入成功！共导入 ${res.data.successCount} 条记录`);
      importModalVisible.value = false;
      await getData();
      await getStatistics();
    }
  } catch (e) {
    console.error('Failed to import', e);
    message.error('导入失败');
  } finally {
    importing.value = false;
  }
}

function handleResetSearch() {
  searchAppointmentNo.value = '';
  searchElderName.value = '';
  searchElderPhone.value = '';
  searchServiceType.value = '';
  searchStatus.value = '';
  searchDateRange.value = null;
  getDataByPage(1);
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
    <NCard title="预约统计" :bordered="false" style="margin-bottom: 16px">
      <div class="statistics-grid">
        <div class="stat-card stat-primary">
          <div class="stat-label">总预约数</div>
          <div class="stat-value">{{ statistics.total }}</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-label">待确认</div>
          <div class="stat-value">{{ statistics.pending }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">已确认</div>
          <div class="stat-value">{{ statistics.confirmed }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">已分配</div>
          <div class="stat-value">{{ statistics.assigned }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">已完成</div>
          <div class="stat-value">{{ statistics.completed }}</div>
        </div>
        <div class="stat-card stat-error">
          <div class="stat-label">已取消</div>
          <div class="stat-value">{{ statistics.cancelled }}</div>
        </div>
        <div class="stat-card stat-error">
          <div class="stat-label">已作废</div>
          <div class="stat-value">{{ statistics.invalid }}</div>
        </div>
      </div>
    </NCard>

    <!-- Table -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>预约信息管理</span>
        </div>
      </template>
      <div style="background: #f5f5f5; padding: 12px; margin-bottom: 12px; border-radius: 4px;">
        <NSpace :wrap="true" align="center">
          <span style="font-size: 13px; color: #666;">筛选条件:</span>
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
          <NButton type="primary" @click="getData">搜索</NButton>
          <NButton @click="handleResetSearch">重置</NButton>
          <NButton @click="handleOpenImport">导入</NButton>
          <NButton @click="handleDownloadTemplate">下载模板</NButton>
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
        :scroll-x="1400"
        :row-key="(row: Api.Appointment.Appointment) => row.appointmentId"
        remote
        :pagination="mobilePagination"
      />
    </NCard>

    <!-- Confirm Modal -->
    <NModal v-model:show="confirmModalVisible" title="确认预约" preset="card" style="width: 500px">
      <NForm :model="confirmForm" label-placement="left" label-width="100">
        <NFormItem label="服务商">
          <NSelect
            v-model:value="confirmForm.providerId"
            :options="providerOptions"
            placeholder="请选择服务商"
            filterable
          />
        </NFormItem>
        <NFormItem label="预约时间">
          <NInput v-model:value="confirmForm.appointmentTime" placeholder="请输入预约时间" />
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

    <!-- Add Modal -->
    <NModal v-model:show="addModalVisible" title="添加预约" preset="card" style="width: 600px">
      <NForm :model="addForm" label-placement="left" label-width="100">
        <NFormItem label="老人姓名" required>
          <NInput v-model:value="addForm.elderName" placeholder="请输入老人姓名" />
        </NFormItem>
        <NFormItem label="手机号" required>
          <NInput v-model:value="addForm.elderPhone" placeholder="请输入手机号" />
        </NFormItem>
        <NFormItem label="身份证号">
          <NInput v-model:value="addForm.elderIdCard" placeholder="请输入身份证号" />
        </NFormItem>
        <NFormItem label="地址">
          <NInput v-model:value="addForm.elderAddress" placeholder="请输入地址" />
        </NFormItem>
        <NFormItem label="服务类型" required>
          <NSelect
            v-model:value="addForm.serviceType"
            :options="serviceTypeOptions"
            placeholder="请选择服务类型"
          />
        </NFormItem>
        <NFormItem label="预约时间" required>
          <NInput v-model:value="addForm.appointmentTime" placeholder="请输入预约时间，如：2024-04-20 09:00" />
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="addForm.remark" type="textarea" placeholder="请输入备注" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="addModalVisible = false">取消</NButton>
          <NButton type="primary" @click="handleAddSubmit">确认</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Import Modal -->
    <NModal v-model:show="importModalVisible" title="导入预约" preset="card" style="width: 500px">
      <NForm label-placement="left" label-width="100">
        <NFormItem label="选择文件">
          <NUpload
            :max-size="5 * 1024 * 1024"
            :file-list="importFile ? [importFile] : []"
            @update:file-list="(list: any) => { importFile = list[0]?.file || null; }"
            @change="handleImportFile"
            accept=".xlsx,.xls"
          >
            <NButton>选择Excel文件</NButton>
          </NUpload>
        </NFormItem>
        <NFormItem label="提示">
          <span style="color: #666; font-size: 13px;">请先下载模板，按模板格式填写数据后导入</span>
        </NFormItem>
        <NAlert v-if="importResult" type="info" style="margin-top: 12px;">
          <template #header>导入结果</template>
          <div>成功: {{ importResult.successCount }} 条</div>
          <div v-if="importResult.failCount > 0">失败: {{ importResult.failCount }} 条</div>
          <div v-if="importResult.errors && importResult.errors.length > 0" style="margin-top: 8px;">
            <div v-for="(err, idx) in importResult.errors.slice(0, 5)" :key="idx" style="color: red; font-size: 12px;">{{ err }}</div>
            <div v-if="importResult.errors.length > 5" style="color: #999; font-size: 12px;">...还有 {{ importResult.errors.length - 5 }} 条错误</div>
          </div>
        </NAlert>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="importModalVisible = false">取消</NButton>
          <NButton type="primary" :loading="importing" @click="handleImportSubmit">导入</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Timeline Drawer -->
    <NDrawer v-model:show="timelineDrawerVisible" :width="480" placement="right" closable>
      <NDrawerContent :title="timelineData?.appointmentNo ? `预约时间轴 - ${timelineData.appointmentNo}` : '预约时间轴'" closable>
        <template #header>
          <div style="display: flex; flex-direction: column; gap: 4px;">
            <div style="font-size: 16px; font-weight: 600;">预约时间轴</div>
            <div v-if="timelineData" style="font-size: 13px; color: #666;">
              当前状态：
              <NTag :type="timelineData.currentStatus === 'COMPLETED' || timelineData.currentStatus === 'CONFIRMED' ? 'success' : timelineData.currentStatus === 'CANCELLED' || timelineData.currentStatus === 'INVALID' ? 'error' : 'warning'" size="small">
                {{ timelineData.currentStatusName }}
              </NTag>
              <template v-if="timelineData.orderId">
                <span style="margin-left: 12px;">|</span>
                <span style="margin-left: 12px;">关联订单：</span>
                <NTag type="info" size="small" style="cursor: pointer;" @click="goToOrder(timelineData.orderId)">
                  {{ timelineData.orderNo }} ({{ timelineData.orderStatusName }})
                </NTag>
              </template>
            </div>
          </div>
        </template>
        <div v-if="timelineLoading" style="display: flex; justify-content: center; padding: 40px;">
          <NSpin size="large" />
        </div>
        <div v-else-if="timelineData?.nodes?.length" class="timeline-container">
          <div v-for="(node, index) in timelineData.nodes" :key="node.status" class="timeline-node">
            <div
              class="timeline-node-header"
              :class="{ 'timeline-node-active': node.active, 'timeline-node-completed': node.completed }"
              @click="toggleNode(node.status)"
            >
              <div class="timeline-connector">
                <div class="timeline-dot" :style="{ background: getNodeColor(node) }">
                  <span v-if="node.completed" class="timeline-check">✓</span>
                  <span v-else-if="node.active" class="timeline-pulse"></span>
                </div>
                <div v-if="index < timelineData.nodes.length - 1" class="timeline-line" :class="{ 'timeline-line-completed': node.completed }"></div>
              </div>
              <div class="timeline-node-content">
                <div class="timeline-node-title">
                  <span class="timeline-node-status-icon" :style="{ color: getNodeColor(node) }">{{ getNodeIcon(node) }}</span>
                  <span class="timeline-node-title-text">{{ node.title }}</span>
                  <span v-if="node.time" class="timeline-node-time">{{ node.time }}</span>
                </div>
                <div v-if="node.details && node.details.length > 0" class="timeline-node-details" :class="{ 'timeline-node-details-expanded': expandedNodes.has(node.status) }">
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
        <div v-else style="text-align: center; padding: 40px; color: #999;">
          暂无时间轴数据
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

/* Timeline Styles */
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

.timeline-node-details {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s ease, opacity 0.3s ease;
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
</style>
