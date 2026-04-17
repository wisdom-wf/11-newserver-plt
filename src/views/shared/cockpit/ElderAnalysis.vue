<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { NCard, NGrid, NGi, NAlert } from 'naive-ui';
import { useEcharts } from '@/hooks/common/echarts';
import { fetchGetElderStatistics } from '@/service/api';
import type { ECOption } from '@/hooks/common/echarts';

defineOptions({
  name: 'ElderAnalysis'
});

const loading = ref(false);
const elderStats = ref<any>({
  totalElders: 0,
  monthlyNewElders: 0,
  activeElders: 0
});

// 年龄段分布饼图
const { domRef: agePieRef, updateOptions: updateAgePieOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '1%', left: 'center' },
  series: [
    {
      color: ['#5da8ff', '#8e9dff', '#fedc69', '#26deca', '#ff7d00'],
      type: 'pie',
      radius: ['45%', '70%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14 } },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

// 护理等级分布柱状图
const { domRef: careBarRef, updateOptions: updateCareBarOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: '1%', left: 'center' },
  grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
  xAxis: { type: 'category', data: [] as string[] },
  yAxis: { type: 'value' },
  series: [
    {
      name: '老人数量',
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
            { offset: 0, color: '#5da8ff' },
            { offset: 1, color: '#8e9dff' }
          ]
        },
        borderRadius: [4, 4, 0, 0]
      }
    }
  ]
}));

// 服务需求分布
const { domRef: demandRef, updateOptions: updateDemandOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '1%', left: 'center' },
  series: [
    {
      color: ['#00b42a', '#165dff', '#ff7d00', '#fedc69', '#165dff'],
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
    const res = await fetchGetElderStatistics();
    if (res.data) {
      elderStats.value = res.data;
      if (res.data.ageDistribution?.length) {
        updateAgePieOptions(opts => {
          opts.series[0].data = res.data.ageDistribution.map((item: any) => ({
            name: item.ageRange || item.label || '未知',
            value: item.count || 0
          }));
          return opts;
        });
      }
      if (res.data.careLevelDistribution?.length) {
        updateCareBarOptions(opts => {
          opts.xAxis.data = res.data.careLevelDistribution.map(
            (item: any) => item.levelName || `等级${item.careLevel}`
          );
          opts.series[0].data = res.data.careLevelDistribution.map((item: any) => item.count || 0);
          return opts;
        });
      }
      if (res.data.serviceDemandDistribution?.length) {
        updateDemandOptions(opts => {
          opts.series[0].data = res.data.serviceDemandDistribution.map((item: any) => ({
            name: item.serviceTypeName || item.serviceType || '未知',
            value: item.count || 0
          }));
          return opts;
        });
      }
    }
  } catch (e) {
    console.error('Failed to get elder statistics', e);
  } finally {
    loading.value = false;
  }
}

function initMockData() {
  updateAgePieOptions(opts => {
    opts.series[0].data = [
      { name: '60-69岁', value: 320 },
      { name: '70-79岁', value: 480 },
      { name: '80-89岁', value: 350 },
      { name: '90岁以上', value: 80 }
    ];
    return opts;
  });
  updateCareBarOptions(opts => {
    opts.xAxis.data = ['自理', '轻度失能', '中度失能', '重度失能'];
    opts.series[0].data = [520, 380, 240, 90];
    return opts;
  });
  updateDemandOptions(opts => {
    opts.series[0].data = [
      { name: '生活照料', value: 420 },
      { name: '医疗护理', value: 280 },
      { name: '助餐服务', value: 350 },
      { name: '康复护理', value: 180 }
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
    <NGrid :cols="3" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi>
        <div class="stat-card-primary">
          <div class="stat-content">
            <span class="stat-value">{{ formatNumber(elderStats.totalElders) }}</span>
            <span class="stat-label">老人总数</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card-success">
          <div class="stat-content">
            <span class="stat-value">{{ formatNumber(elderStats.monthlyNewElders) }}</span>
            <span class="stat-label">本月新增</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card-warning">
          <div class="stat-content">
            <span class="stat-value">{{ formatNumber(elderStats.activeElders) }}</span>
            <span class="stat-label">活跃老人</span>
          </div>
        </div>
      </NGi>
    </NGrid>

    <!-- 图表 -->
    <NGrid :cols="2" :x-gap="16" :y-gap="16" item-responsive>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">老人年龄分布</div>
          <div ref="agePieRef" class="h-300px"></div>
        </div>
      </NGi>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">护理等级分布</div>
          <div ref="careBarRef" class="h-300px"></div>
        </div>
      </NGi>
      <NGi span="2">
        <div class="chart-card">
          <div class="chart-title">服务需求分布</div>
          <div ref="demandRef" class="h-300px"></div>
        </div>
      </NGi>
    </NGrid>
  </div>
</template>

<style scoped>
.stat-card-primary,
.stat-card-success,
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
  font-size: 32px;
  font-weight: 700;
  color: white;
}

.stat-label {
  font-size: 14px;
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
