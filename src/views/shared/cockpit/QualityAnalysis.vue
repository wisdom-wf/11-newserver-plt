<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { NGrid, NGi } from 'naive-ui';
import { useEcharts } from '@/hooks/common/echarts';
import { fetchGetQualityStatistics } from '@/service/api';
import type { ECOption } from '@/hooks/common/echarts';

defineOptions({
  name: 'QualityAnalysis'
});

const loading = ref(false);
const qualityStats = ref<any>({
  averageRating: 0,
  positiveRate: 0,
  complaintRate: 0,
  totalEvaluations: 0
});

// 评分趋势图
const { domRef: trendRef, updateOptions: updateTrendOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: '1%', left: 'center' },
  grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
  xAxis: { type: 'category', data: [] as string[], boundaryGap: false },
  yAxis: { type: 'value', min: 0, max: 5 },
  series: [
    {
      name: '平均评分',
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
    }
  ]
}));

// 评价分布饼图
const { domRef: evalRef, updateOptions: updateEvalOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '1%', left: 'center' },
  series: [
    {
      color: ['#00b42a', '#fedc69', '#ff7d00', '#ff4d4f'],
      type: 'pie',
      radius: ['40%', '65%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14 } },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

// 投诉类型分布
const { domRef: complaintRef, updateOptions: updateComplaintOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: '1%', left: 'center' },
  grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
  xAxis: { type: 'category', data: [] as string[], axisLabel: { rotate: 30 } },
  yAxis: { type: 'value' },
  series: [
    {
      name: '投诉数',
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
            { offset: 0, color: '#ff4d4f' },
            { offset: 1, color: '#ff7d00' }
          ]
        },
        borderRadius: [4, 4, 0, 0]
      }
    }
  ]
}));

async function getData() {
  loading.value = true;
  try {
    const res = await fetchGetQualityStatistics();
    if (res.data) {
      qualityStats.value = res.data;
      if (res.data.ratingTrend?.length) {
        updateTrendOptions(opts => {
          (opts.xAxis as any).data = res.data.ratingTrend.map((item: any) => item.date);
          (opts.series as any)[0].data = res.data.ratingTrend.map((item: any) => item.averageRating);
          return opts;
        });
      }
      updateEvalOptions(opts => {
        (opts.series as any)[0].data = [
          { name: '好评', value: res.data.positiveCount || 0 },
          { name: '中评', value: res.data.neutralCount || 0 },
          { name: '差评', value: res.data.negativeCount || 0 }
        ];
        return opts;
      });
      if (res.data.complaintTypes?.length) {
        updateComplaintOptions(opts => {
          (opts.xAxis as any).data = res.data.complaintTypes.map((item: any) => item.typeName || item.complaintType);
          (opts.series as any)[0].data = res.data.complaintTypes.map((item: any) => item.count);
          return opts;
        });
      }
    }
  } catch (e) {
    console.error('Failed to get quality statistics', e);
  } finally {
    loading.value = false;
  }
}

function initMockData() {
  updateTrendOptions(opts => {
    (opts.xAxis as any).data = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
    (opts.series as any)[0].data = [4.5, 4.6, 4.4, 4.7, 4.8, 4.6, 4.9];
    return opts;
  });
  updateEvalOptions(opts => {
    (opts.series as any)[0].data = [
      { name: '好评', value: 850 },
      { name: '中评', value: 120 },
      { name: '差评', value: 30 }
    ];
    return opts;
  });
  updateComplaintOptions(opts => {
    (opts.xAxis as any).data = ['服务态度', '响应速度', '服务质量', '人员素质', '其他'];
    (opts.series as any)[0].data = [15, 8, 12, 5, 3];
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
            <span class="stat-value">{{ (qualityStats.averageRating || 0).toFixed(1) }}</span>
            <span class="stat-label">平均评分</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card-success">
          <div class="stat-content">
            <span class="stat-value">{{ ((qualityStats.positiveRate || 0) * 100).toFixed(1) }}%</span>
            <span class="stat-label">好评率</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card-danger">
          <div class="stat-content">
            <span class="stat-value">{{ ((qualityStats.complaintRate || 0) * 100).toFixed(1) }}%</span>
            <span class="stat-label">投诉率</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card-info">
          <div class="stat-content">
            <span class="stat-value">{{ formatNumber(qualityStats.totalEvaluations) }}</span>
            <span class="stat-label">评价总数</span>
          </div>
        </div>
      </NGi>
    </NGrid>

    <NGrid :cols="2" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi>
        <div class="chart-card">
          <div class="chart-title">评分趋势</div>
          <div ref="trendRef" class="h-300px"></div>
        </div>
      </NGi>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">评价分布</div>
          <div ref="evalRef" class="h-300px"></div>
        </div>
      </NGi>
    </NGrid>

    <NGrid :cols="2" :x-gap="16" :y-gap="16" item-responsive>
      <NGi span="2">
        <div class="chart-card">
          <div class="chart-title">投诉类型分析</div>
          <div ref="complaintRef" class="h-300px"></div>
        </div>
      </NGi>
    </NGrid>
  </div>
</template>

<style scoped>
.stat-card-primary,
.stat-card-success,
.stat-card-info,
.stat-card-danger {
  border-radius: 12px;
  padding: 20px;
}

.stat-card-primary {
  background: linear-gradient(135deg, #5E8B7E 0%, #7BA89F 100%);
}
.stat-card-success {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}
.stat-card-info {
  background: linear-gradient(135deg, #3A506B 0%, #5A7A8B 100%);
}
.stat-card-danger {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7d00 100%);
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
