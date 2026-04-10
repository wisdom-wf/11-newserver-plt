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
import { fetchGetEvaluationList, fetchGetEvaluationStatistics } from '@/service/api';

defineOptions({
  name: 'BusinessEvaluation'
});

// Statistics
const statistics = ref<Api.Evaluation.Statistics>({
  total: 0,
  avgOverallScore: 0,
  avgServiceScore: 0,
  avgAttitudeScore: 0,
  avgSkillScore: 0,
  avgPunctualityScore: 0,
  verySatisfiedCount: 0,
  satisfiedCount: 0,
  neutralCount: 0,
  dissatisfiedCount: 0,
  veryDissatisfiedCount: 0,
  satisfactionRate: 0
});

// Search
const searchOrderNo = ref('');
const searchElderName = ref('');
const searchProviderName = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Table data
const loading = ref(false);
const tableData = ref<any[]>([]);
const pagination = ref({ page: 1, pageSize: 10, total: 0 });

const columns: DataTableColumns<any> = [
  { title: '评价编号', key: 'evaluationNo', width: 160 },
  { title: '订单号', key: 'orderNo', width: 160 },
  { title: '老人姓名', key: 'elderName', width: 100 },
  { title: '服务商', key: 'providerName', width: 150 },
  { title: '服务人员', key: 'staffName', width: 100 },
  { title: '评分', key: 'score', width: 80 },
  {
    title: '满意度',
    key: 'satisfaction',
    width: 100,
    render: row => {
      const type = row.satisfaction === 'SATISFIED' ? 'success' : row.satisfaction === 'NEUTRAL' ? 'warning' : 'error';
      const label = row.satisfaction === 'SATISFIED' ? '满意' : row.satisfaction === 'NEUTRAL' ? '一般' : '不满意';
      return h(NTag, { type, size: 'small' }, () => label);
    }
  },
  { title: '评价内容', key: 'content', width: 200, ellipsis: { tooltip: true } },
  { title: '评价时间', key: 'createTime', width: 170 }
];

async function getStatistics() {
  try {
    const { data } = await fetchGetEvaluationStatistics();
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
    if (searchDateRange.value) {
      params.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      params.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }

    const { data } = await fetchGetEvaluationList(params);
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
    <NCard title="满意度评价统计" :bordered="false" style="margin-bottom: 16px">
      <NSpace :size="20" :wrap="true">
        <NStatistic label="总评价数" :value="statistics.total" />
        <NStatistic label="平均评分" :value="`${statistics.avgOverallScore.toFixed(1)}分`" />
        <NStatistic label="满意率" :value="`${statistics.satisfactionRate}%`" />
        <NStatistic label="投诉数" :value="statistics.dissatisfiedCount + statistics.veryDissatisfiedCount" />
      </NSpace>
    </NCard>

    <!-- Table -->
    <NCard title="满意度评价管理" :bordered="false">
      <template #header>
        <NSpace :wrap="true">
          <NInput v-model:value="searchOrderNo" placeholder="订单号" clearable style="width: 150px" />
          <NInput v-model:value="searchElderName" placeholder="老人姓名" clearable style="width: 100px" />
          <NInput v-model:value="searchProviderName" placeholder="服务商" clearable style="width: 150px" />
          <NDatePicker v-model:value="searchDateRange" type="daterange" clearable style="width: 260px" />
          <NButton type="primary" @click="getTableData">搜索</NButton>
        </NSpace>
      </template>
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1200"
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
