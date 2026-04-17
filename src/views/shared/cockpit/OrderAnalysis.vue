<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { NGrid, NGi } from 'naive-ui';
import { useEcharts } from '@/hooks/common/echarts';
import { fetchGetOrderStatistics } from '@/service/api';
import type { ECOption } from '@/hooks/common/echarts';

defineOptions({
  name: 'OrderAnalysis'
});

const loading = ref(false);
const orderStats = ref<any>({
  totalOrders: 0,
  completedOrders: 0,
  completionRate: 0,
  averageRating: 0
});

// 订单趋势折线图
const { domRef: trendRef, updateOptions: updateTrendOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: '1%', left: 'center' },
  grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
  xAxis: { type: 'category', data: [] as string[], boundaryGap: false },
  yAxis: { type: 'value' },
  series: [
    {
      name: '订单数',
      type: 'line',
      smooth: true,
      data: [] as number[],
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(93, 168, 255, 0.5)' },
            { offset: 1, color: 'rgba(93, 168, 255, 0.1)' }
          ]
        }
      },
      itemStyle: { color: '#5da8ff' }
    },
    {
      name: '完成数',
      type: 'line',
      smooth: true,
      data: [] as number[],
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(38, 222, 202, 0.5)' },
            { offset: 1, color: 'rgba(38, 222, 202, 0.1)' }
          ]
        }
      },
      itemStyle: { color: '#26deca' }
    }
  ]
}));

// 服务类型分布
const { domRef: serviceRef, updateOptions: updateServiceOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: '1%', left: 'center' },
  grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
  xAxis: { type: 'category', data: [] as string[], axisLabel: { rotate: 30 } },
  yAxis: { type: 'value' },
  series: [
    {
      name: '订单数',
      type: 'bar',
      data: [] as number[],
      itemStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#fedc69' },
            { offset: 1, color: '#ff7d00' }
          ]
        },
        borderRadius: [4, 4, 0, 0]
      }
    }
  ]
}));

// 订单来源分布饼图
const { domRef: sourceRef, updateOptions: updateSourceOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '1%', left: 'center' },
  series: [
    {
      color: ['#5da8ff', '#8e9dff', '#fedc69', '#26deca'],
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
    const res = await fetchGetOrderStatistics();
    if (res.data) {
      orderStats.value = res.data;
      if (res.data.orderTrend?.length) {
        updateTrendOptions(opts => {
          opts.xAxis.data = res.data.orderTrend.map((item: any) => item.date);
          opts.series[0].data = res.data.orderTrend.map((item: any) => item.orderCount);
          opts.series[1].data = res.data.orderTrend.map((item: any) => item.completedCount);
          return opts;
        });
      }
      if (res.data.serviceTypeDistribution?.length) {
        updateServiceOptions(opts => {
          opts.xAxis.data = res.data.serviceTypeDistribution.map((item: any) => item.serviceTypeName);
          opts.series[0].data = res.data.serviceTypeDistribution.map((item: any) => item.orderCount);
          return opts;
        });
      }
      if (res.data.orderSourceDistribution?.length) {
        updateSourceOptions(opts => {
          opts.series[0].data = res.data.orderSourceDistribution.map((item: any) => ({
            name: item.sourceName || item.orderSource || '未知',
            value: item.count || 0
          }));
          return opts;
        });
      }
    }
  } catch (e) {
    console.error('Failed to get order statistics', e);
  } finally {
    loading.value = false;
  }
}

function initMockData() {
  updateTrendOptions(opts => {
    opts.xAxis.data = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
    opts.series[0].data = [120, 150, 180, 170, 190, 210, 195];
    opts.series[1].data = [100, 130, 160, 155, 175, 195, 180];
    return opts;
  });
  updateServiceOptions(opts => {
    opts.xAxis.data = ['生活照料', '医疗护理', '助餐服务', '康复护理', '精神慰藉', '紧急救援'];
    opts.series[0].data = [320, 240, 180, 150, 90, 60];
    return opts;
  });
  updateSourceOptions(opts => {
    opts.series[0].data = [
      { name: 'APP下单', value: 450 },
      { name: '电话预约', value: 280 },
      { name: '社区代订', value: 150 },
      { name: '其他', value: 60 }
    ];
    return opts;
  });
}

onMounted(() => {
  initMockData();
  getData();
});

function formatNumber(num: number): string {
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
            <span class="stat-value">{{ formatNumber(orderStats.totalOrders) }}</span>
            <span class="stat-label">订单总数</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card-success">
          <div class="stat-content">
            <span class="stat-value">{{ formatNumber(orderStats.completedOrders) }}</span>
            <span class="stat-label">已完成</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card-info">
          <div class="stat-content">
            <span class="stat-value">{{ (orderStats.completionRate || 0).toFixed(1) }}%</span>
            <span class="stat-label">完成率</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card-warning">
          <div class="stat-content">
            <span class="stat-value">{{ (orderStats.averageRating || 0).toFixed(1) }}</span>
            <span class="stat-label">平均评分</span>
          </div>
        </div>
      </NGi>
    </NGrid>

    <NGrid :cols="2" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi span="2">
        <div class="chart-card">
          <div class="chart-title">订单趋势</div>
          <div ref="trendRef" class="h-300px"></div>
        </div>
      </NGi>
    </NGrid>

    <NGrid :cols="2" :x-gap="16" :y-gap="16" item-responsive>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">服务类型分布</div>
          <div ref="serviceRef" class="h-300px"></div>
        </div>
      </NGi>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">订单来源分布</div>
          <div ref="sourceRef" class="h-300px"></div>
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
  font-size: 28px;
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
