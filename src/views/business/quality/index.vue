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
import { fetchGetQualityCheckList, fetchGetQualityStatistics } from '@/service/api';

defineOptions({
  name: 'BusinessQuality'
});

// Statistics
const statistics = ref<Api.Quality.Statistics>({
  total: 0,
  qualifiedCount: 0,
  unqualifiedCount: 0,
  needRectifyCount: 0,
  qualifiedRate: 0,
  avgScore: 0
});

// Search
const searchOrderNo = ref('');
const searchProviderName = ref('');
const searchStaffName = ref('');
const searchCheckResult = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Table data
const loading = ref(false);
const tableData = ref<Api.Quality.QualityCheck[]>([]);
const pagination = ref({ page: 1, pageSize: 10, total: 0 });

// Check result options
const resultOptions = [
  { label: '合格', value: 'QUALIFIED' },
  { label: '不合格', value: 'UNQUALIFIED' },
  { label: '需整改', value: 'NEED_RECTIFY' }
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

const columns: DataTableColumns<Api.Quality.QualityCheck> = [
  { title: '质检编号', key: 'checkNo', width: 160 },
  { title: '订单号', key: 'orderNo', width: 160 },
  { title: '服务商', key: 'providerName', width: 150 },
  { title: '服务人员', key: 'staffName', width: 100 },
  { title: '质检类型', key: 'checkType', width: 100 },
  { title: '质检方式', key: 'checkMethod', width: 100 },
  { title: '综合评分', key: 'checkScore', width: 100 },
  {
    title: '质检结果',
    key: 'checkResult',
    width: 100,
    render: row =>
      h(NTag, { type: getResultType(row.checkResult), size: 'small' }, () => getResultLabel(row.checkResult))
  },
  { title: '质检时间', key: 'checkTime', width: 170 },
  { title: '创建时间', key: 'createTime', width: 170 }
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

async function getTableData() {
  loading.value = true;
  try {
    const params: Api.Quality.QualityCheckQuery & Api.Common.PaginatingQueryParams = {
      current: pagination.value.page,
      pageSize: pagination.value.pageSize
    };
    if (searchOrderNo.value) params.orderNo = searchOrderNo.value;
    if (searchProviderName.value) params.providerName = searchProviderName.value;
    if (searchStaffName.value) params.staffName = searchStaffName.value;
    if (searchCheckResult.value) params.checkResult = searchCheckResult.value;
    if (searchDateRange.value) {
      params.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      params.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }

    const { data } = await fetchGetQualityCheckList(params);
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
    <NCard title="质检统计" :bordered="false" style="margin-bottom: 16px">
      <NSpace :size="20" :wrap="true">
        <NStatistic label="总质检数" :value="statistics.total" />
        <NStatistic label="合格数" :value="statistics.qualifiedCount" />
        <NStatistic label="不合格数" :value="statistics.unqualifiedCount" />
        <NStatistic label="需整改数" :value="statistics.needRectifyCount" />
        <NStatistic label="合格率" :value="`${statistics.qualifiedRate}%`" />
        <NStatistic label="平均评分" :value="`${statistics.avgScore.toFixed(1)}分`" />
      </NSpace>
    </NCard>

    <!-- Table -->
    <NCard title="质检管理" :bordered="false">
      <template #header>
        <NSpace :wrap="true">
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
          <NButton type="primary" @click="getTableData">搜索</NButton>
        </NSpace>
      </template>
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1400"
        :row-key="(row: Api.Quality.QualityCheck) => row.id"
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
