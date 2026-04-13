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
  NPagination,
  NUpload,
  NAlert
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import {
  fetchGetAppointmentList,
  fetchConfirmAppointment,
  fetchAssignAppointment,
  fetchCancelAppointment,
  fetchInvalidateAppointment,
  fetchDownloadAppointmentTemplate,
  fetchImportAppointment,
  fetchGetAppointmentStatistics,
  fetchGetProviderOptions
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

// Provider options
const providerOptions = ref<{ label: string; value: string }[]>([]);

async function getProviderOptions() {
  try {
    const { data } = await fetchGetProviderOptions();
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

// Import modal
const importModalVisible = ref(false);
const importFile = ref<File | null>(null);
const importResult = ref<{ successCount: number; failCount: number; errors: string[] } | null>(null);
const importing = ref(false);

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
    await fetchConfirmAppointment(currentRow.value.appointmentId, confirmForm.value);
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
    await fetchCancelAppointment(currentRow.value.appointmentId, cancelForm.value);
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
    await fetchAssignAppointment(currentRow.value.appointmentId, assignForm.value);
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
    await fetchInvalidateAppointment(currentRow.value.appointmentId, invalidateForm.value);
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

function handleOpenImport() {
  importFile.value = null;
  importResult.value = null;
  importModalVisible.value = true;
}

function handleImportFile(options: { file: File }) {
  importFile.value = options.file;
  return false; // Prevent auto upload
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
      await getTableData();
      await getStatistics();
    }
  } catch (e) {
    console.error('Failed to import', e);
    message.error('导入失败');
  } finally {
    importing.value = false;
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
          <NButton type="primary" @click="getTableData">搜索</NButton>
          <NButton @click="() => { searchAppointmentNo = ''; searchElderName = ''; searchElderPhone = ''; searchServiceType = ''; searchStatus = ''; searchDateRange = null; pagination.page = 1; getTableData(); }">重置</NButton>
          <NButton @click="handleOpenImport">导入</NButton>
          <NButton @click="handleDownloadTemplate">下载模板</NButton>
        </NSpace>
      </div>
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1400"
        :row-key="(row: Api.Appointment.Appointment) => row.appointmentId"
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

    <!-- Assign Modal -->
    <NModal v-model:show="assignModalVisible" title="分配预约" preset="card" style="width: 500px">
      <NForm :model="assignForm" label-placement="left" label-width="100">
        <NFormItem label="服务商">
          <NSelect
            v-model:value="assignForm.providerId"
            :options="providerOptions"
            placeholder="请选择服务商"
            filterable
          />
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
