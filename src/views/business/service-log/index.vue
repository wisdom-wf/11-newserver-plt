<script setup lang="ts">
import { ref, h, onMounted } from 'vue';
import {
  NButton,
  NCard,
  NDataTable,
  NTag,
  NSpace,
  NInput,
  NSelect,
  NDatePicker,
  NPagination,
  NStatistic
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import { fetchGetServiceLogList, fetchGetServiceLogStatistics } from '@/service/api';

defineOptions({
  name: 'BusinessServiceLog'
});

// Statistics
const statistics = ref<Api.ServiceLog.Statistics>({
  total: 0,
  today: 0,
  month: 0,
  avgDuration: 0,
  anomalyCount: 0
});

// Search
const searchOrderNo = ref('');
const searchElderName = ref('');
const searchStaffName = ref('');
const searchServiceCategory = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Table data
const loading = ref(false);
const tableData = ref<Api.ServiceLog.ServiceLog[]>([]);
const pagination = ref({ page: 1, pageSize: 10, total: 0 });

// Service category options
const categoryOptions = [
  { label: '养老服务', value: 'ELDER_CARE' },
  { label: '家政服务', value: 'HOME_CARE' }
];

// Log status options
const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已提交', value: 'SUBMITTED' },
  { label: '已审核', value: 'VERIFIED' }
];

function getCategoryLabel(category?: string): string {
  const option = categoryOptions.find(o => o.value === category);
  return option?.label || category || '';
}

function getStatusType(status: Api.ServiceLog.LogStatus): 'warning' | 'success' | 'info' | 'default' {
  const map: Record<string, 'warning' | 'success' | 'info' | 'default'> = {
    DRAFT: 'warning',
    SUBMITTED: 'info',
    VERIFIED: 'success'
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
  { title: '老人姓名', key: 'elderName', width: 100 },
  { title: '服务人员', key: 'staffName', width: 100 },
  { title: '服务类别', key: 'serviceCategory', width: 100, render: row => getCategoryLabel(row.serviceCategory) },
  { title: '服务类型', key: 'serviceType', width: 120 },
  {
    title: '服务时长',
    key: 'serviceDuration',
    width: 100,
    render: row => (row.serviceDuration ? `${row.serviceDuration}分钟` : '-')
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
    key: 'status',
    width: 100,
    render: row => h(NTag, { type: getStatusType(row.status), size: 'small' }, () => getStatusLabel(row.status))
  },
  { title: '提交时间', key: 'submitTime', width: 170 },
  { title: '创建时间', key: 'createTime', width: 170 }
];

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

async function getTableData() {
  loading.value = true;
  try {
    const params: Api.ServiceLog.ServiceLogQuery & Api.Common.PaginatingQueryParams = {
      current: pagination.value.page,
      pageSize: pagination.value.pageSize
    };
    if (searchOrderNo.value) params.orderNo = searchOrderNo.value;
    if (searchElderName.value) params.elderName = searchElderName.value;
    if (searchStaffName.value) params.staffName = searchStaffName.value;
    if (searchServiceCategory.value) params.serviceCategory = searchServiceCategory.value;
    if (searchDateRange.value) {
      params.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      params.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }

    const { data } = await fetchGetServiceLogList(params);
    tableData.value = data?.records || [];
    pagination.value.total = data?.total || 0;
  } catch (e) {
    console.error('Failed to get table data', e);
  } finally {
    loading.value = false;
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
    <NCard title="服务日志统计" :bordered="false" style="margin-bottom: 16px">
      <NSpace :size="20" :wrap="true">
        <NStatistic label="总日志数" :value="statistics.total" />
        <NStatistic label="今日服务" :value="statistics.today" />
        <NStatistic label="本月服务" :value="statistics.month" />
        <NStatistic label="平均服务时长" :value="`${statistics.avgDuration.toFixed(0)}分钟`" />
        <NStatistic label="异常数" :value="statistics.anomalyCount" />
      </NSpace>
    </NCard>

    <!-- Table -->
    <NCard title="服务日志管理" :bordered="false">
      <template #header>
        <NSpace :wrap="true">
          <NInput v-model:value="searchOrderNo" placeholder="订单号" clearable style="width: 150px" />
          <NInput v-model:value="searchElderName" placeholder="老人姓名" clearable style="width: 100px" />
          <NInput v-model:value="searchStaffName" placeholder="服务人员" clearable style="width: 100px" />
          <NSelect
            v-model:value="searchServiceCategory"
            :options="categoryOptions"
            placeholder="服务类别"
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
        :scroll-x="1400"
        :row-key="(row: Api.ServiceLog.ServiceLog) => row.id"
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
  </div>
</template>
