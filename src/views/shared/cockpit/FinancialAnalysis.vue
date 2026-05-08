<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { NGrid, NGi } from 'naive-ui';
import { useEcharts } from '@/hooks/common/echarts';
import { fetchGetSettlementStatistics } from '@/service/api';
import type { ECOption } from '@/hooks/common/echarts';

defineOptions({
  name: 'FinancialAnalysis'
});

const loading = ref(false);
const financialStats = ref<any>({
  totalAmount: 0,
  totalSubsidyAmount: 0,
  totalSelfPayAmount: 0,
  totalPlatformFee: 0,
  monthlyAmount: 0
});

// 收支趋势图
const { domRef: trendRef, updateOptions: updateTrendOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: '1%', left: 'center' },
  grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
  xAxis: { type: 'category', data: [] as string[] },
  yAxis: { type: 'value', name: '金额(元)' },
  series: [
    {
      name: '服务费',
      type: 'bar',
      stack: 'total',
      data: [] as number[],
      itemStyle: { color: '#5da8ff' }
    },
    {
      name: '政府补贴',
      type: 'bar',
      stack: 'total',
      data: [] as number[],
      itemStyle: { color: '#26deca' }
    },
    {
      name: '自付金额',
      type: 'bar',
      stack: 'total',
      data: [] as number[],
      itemStyle: { color: '#fedc69' }
    }
  ]
}));

// 服务类型金额分布
const { domRef: serviceRef, updateOptions: updateServiceOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '1%', left: 'center' },
  series: [
    {
      color: ['#5da8ff', '#8e9dff', '#fedc69', '#26deca', '#ff7d00', '#00b42a'],
      type: 'pie',
      radius: ['40%', '65%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14 } },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

async function getData() {
  loading.value = true;
  try {
    const res = await fetchGetSettlementStatistics();
    if (res.data) {
      financialStats.value = res.data;
    }
  } catch (e) {
    console.error('Failed to get financial statistics', e);
  } finally {
    loading.value = false;
  }
}

function initMockData() {
  updateTrendOptions(opts => {
    (opts.xAxis as any).data = ['1月', '2月', '3月', '4月', '5月', '6月'];
    (opts.series as any)[0].data = [120000, 135000, 150000, 142000, 160000, 175000];
    opts.series[1].data = [80000, 90000, 95000, 92000, 100000, 110000];
    opts.series[2].data = [40000, 45000, 55000, 50000, 60000, 65000];
    return opts;
  });
  updateServiceOptions(opts => {
    (opts.series as any)[0].data = [
      { name: '生活照料', value: 450000 },
      { name: '医疗护理', value: 320000 },
      { name: '助餐服务', value: 280000 },
      { name: '康复护理', value: 180000 },
      { name: '精神慰藉', value: 95000 },
      { name: '紧急救援', value: 45000 }
    ];
    return opts;
  });
}

onMounted(() => {
  initMockData();
  getData();
});

function formatMoney(num: number): string {
  if (num >= 10000) return (num / 10000).toFixed(1) + '万';
  return num?.toString() || '0';
}
</script>

<template>
  <div class="mt-4">
    <!-- 核心指标 -->
    <NGrid :cols="4" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi>
        <div class="stat-card-primary">
          <div class="stat-content">
            <span class="stat-value">¥{{ formatMoney(financialStats.totalAmount) }}</span>
            <span class="stat-label">总收入</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card-success">
          <div class="stat-content">
            <span class="stat-value">¥{{ formatMoney(financialStats.totalSubsidyAmount) }}</span>
            <span class="stat-label">政府补贴</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card-info">
          <div class="stat-content">
            <span class="stat-value">¥{{ formatMoney(financialStats.totalSelfPayAmount) }}</span>
            <span class="stat-label">自付金额</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card-warning">
          <div class="stat-content">
            <span class="stat-value">¥{{ formatMoney(financialStats.totalPlatformFee) }}</span>
            <span class="stat-label">平台费</span>
          </div>
        </div>
      </NGi>
    </NGrid>

    <NGrid :cols="2" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi span="2">
        <div class="chart-card">
          <div class="chart-title">月度收支趋势</div>
          <div ref="trendRef" class="h-300px"></div>
        </div>
      </NGi>
    </NGrid>

    <NGrid :cols="2" :x-gap="16" :y-gap="16" item-responsive>
      <NGi span="2">
        <div class="chart-card">
          <div class="chart-title">服务类型金额分布</div>
          <div ref="serviceRef" class="h-300px"></div>
        </div>
      </NGi>
    </NGrid>
  </div>
</template>

<style scoped>
.stat-card-primary,
.stat-card-success,
.stat-card-info,
.stat-card-warning {
  border-radius: 12px;
  padding: 20px;
}

.stat-card-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.stat-card-success {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}
.stat-card-info {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}
.stat-card-warning {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: white;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 4px;
}

.chart-card {
  background: var(--n-color);
  border-radius: 12px;
  padding: 16px;
}

.chart-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
  color: var(--n-text-color);
}
</style>
