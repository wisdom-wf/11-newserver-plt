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
import { fetchGetSettlementList, fetchGetSettlementStatistics } from '@/service/api';

defineOptions({
  name: 'BusinessFinancial'
});

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

// Search
const searchOrderNo = ref('');
const searchElderName = ref('');
const searchProviderName = ref('');
const searchStatus = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Table data
const loading = ref(false);
const tableData = ref<any[]>([]);
const pagination = ref({ page: 1, pageSize: 10, total: 0 });

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
    PAID: 'success'
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
  { title: '服务人员', key: 'staffName', width: 100 },
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

async function getTableData() {
  loading.value = true;
  try {
    const params: any = {
      current: pagination.value.page,
      pageSize: pagination.value.pageSize
    };
    if (searchOrderNo.value) params.orderNo = searchOrderNo.value;
    if (searchElderName.value) params.elderName = searchElderName.value;
    if (searchProviderName.value) params.providerName = searchProviderName.value;
    if (searchStatus.value) params.status = searchStatus.value;
    if (searchDateRange.value) {
      params.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      params.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }

    const { data } = await fetchGetSettlementList(params);
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
    <NCard title="财务结算统计" :bordered="false" style="margin-bottom: 16px">
      <NSpace :size="20" :wrap="true">
        <NStatistic label="总金额" :value="`¥${Number(statistics.totalAmount || 0).toFixed(2)}`" />
        <NStatistic label="补贴金额" :value="`¥${Number(statistics.subsidyTotal || 0).toFixed(2)}`" />
        <NStatistic label="自付金额" :value="`¥${Number(statistics.selfPayTotal || 0).toFixed(2)}`" />
        <NStatistic label="待结算金额" :value="`¥${Number(statistics.pending || 0).toFixed(2)}`" />
        <NStatistic label="结算单数" :value="statistics.completed" />
      </NSpace>
    </NCard>

    <!-- Table -->
    <NCard title="财务结算管理" :bordered="false">
      <template #header>
        <NSpace :wrap="true">
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
          <NButton type="primary" @click="getTableData">搜索</NButton>
        </NSpace>
      </template>
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1500"
        :row-key="(row: any) => row.id"
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
