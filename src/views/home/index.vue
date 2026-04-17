<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { NGrid, NGi, NAlert } from 'naive-ui';
import { useEcharts } from '@/hooks/common/echarts';
import {
  fetchGetCockpitOverview,
  fetchGetElderStatistics,
  fetchGetOrderStatistics,
  fetchGetSettlementStatistics,
  fetchGetQualityStatistics
} from '@/service/api';
import type { ECOption } from '@/hooks/common/echarts';

defineOptions({
  name: 'Home'
});

const router = useRouter();

// ============ 数据状态 ============
const overview = ref<any>({
  todayOrders: 0,
  monthOrders: 0,
  totalOrders: 0,
  providerCount: 0,
  staffCount: 0,
  elderCount: 0,
  monthRevenue: 0,
  satisfaction: 0,
  qualifiedRate: 0
});
const elderStats = ref<any>({ totalElders: 0, monthlyNewElders: 0, activeElders: 0 });
const orderStats = ref<any>({ totalOrders: 0, completedOrders: 0, completionRate: 0, averageRating: 0 });
const financialStats = ref<any>({ totalAmount: 0, totalSubsidyAmount: 0, totalSelfPayAmount: 0, totalPlatformFee: 0 });
const qualityStats = ref<any>({ averageRating: 0, positiveRate: 0, complaintRate: 0, totalEvaluations: 0 });

// ============ 图表定义 ============
// 服务类型分布饼图
const { domRef: pieDomRef, updateOptions: updatePieOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '1%', left: 'center', itemStyle: { borderWidth: 0 } },
  series: [
    {
      color: ['#5da8ff', '#8e9dff', '#fedc69', '#26deca', '#ff7d00', '#00b42a'],
      name: '服务类型',
      type: 'pie',
      radius: ['45%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: 14 } },
      labelLine: { show: false },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

// 老人年龄分布饼图
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

// 服务质量雷达图
const { domRef: radarDomRef, updateOptions: updateRadarOptions } = useEcharts<ECOption>(() => ({
  tooltip: {},
  radar: {
    indicator: [
      { name: '满意度', max: 100 },
      { name: '完成率', max: 100 },
      { name: '好评率', max: 100 },
      { name: '响应速度', max: 100 },
      { name: '服务质量', max: 100 }
    ],
    radius: '65%',
    axisName: { color: '#666' }
  },
  series: [
    {
      type: 'radar',
      data: [
        {
          value: [85, 78, 92, 88, 90],
          name: '服务质量指标',
          itemStyle: { color: '#5da8ff' },
          areaStyle: { color: 'rgba(93, 168, 255, 0.3)' }
        }
      ]
    }
  ]
}));

// 订单转化漏斗图
const { domRef: funnelDomRef, updateOptions: updateFunnelOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'funnel',
      left: '10%',
      top: 20,
      bottom: 20,
      width: '80%',
      min: 0,
      max: 100,
      minSize: '0%',
      maxSize: '100%',
      sort: 'descending',
      gap: 2,
      label: { show: true, position: 'inside', formatter: '{b}: {c}' },
      labelLine: { length: 10, lineStyle: { width: 1, type: 'solid' } },
      itemStyle: { borderColor: '#fff', borderWidth: 1 },
      emphasis: { label: { fontSize: 14 } },
      data: [
        { value: 100, name: '预约' },
        { value: 80, name: '确认' },
        { value: 60, name: '分配' },
        { value: 40, name: '服务中' },
        { value: 20, name: '完成' }
      ]
    }
  ]
}));

// 订单趋势折线图
const { domRef: orderTrendRef, updateOptions: updateOrderTrendOptions } = useEcharts<ECOption>(() => ({
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

// 服务类型分布柱状图
const { domRef: serviceBarRef, updateOptions: updateServiceBarOptions } = useEcharts<ECOption>(() => ({
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

// 收支趋势图
const { domRef: trendRef, updateOptions: updateTrendOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: '1%', left: 'center' },
  grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
  xAxis: { type: 'category', data: [] as string[] },
  yAxis: { type: 'value', name: '金额(元)' },
  series: [
    { name: '服务费', type: 'bar', stack: 'total', data: [] as number[], itemStyle: { color: '#5da8ff' } },
    { name: '政府补贴', type: 'bar', stack: 'total', data: [] as number[], itemStyle: { color: '#26deca' } },
    { name: '自付金额', type: 'bar', stack: 'total', data: [] as number[], itemStyle: { color: '#fedc69' } }
  ]
}));

// 服务类型金额分布
const { domRef: serviceAmountRef, updateOptions: updateServiceAmountOptions } = useEcharts<ECOption>(() => ({
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

// 评分趋势图
const { domRef: ratingTrendRef, updateOptions: updateRatingTrendOptions } = useEcharts<ECOption>(() => ({
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

// ============ 数据获取 ============
async function getData() {
  try {
    const [overviewRes, elderRes, orderRes, financialRes, qualityRes] = await Promise.all([
      fetchGetCockpitOverview(),
      fetchGetElderStatistics(),
      fetchGetOrderStatistics(),
      fetchGetSettlementStatistics(),
      fetchGetQualityStatistics()
    ]);

    if (overviewRes.data) {
      overview.value = overviewRes.data;
      if (overviewRes.data.serviceTypeDistribution?.length) {
        updatePieOptions(opts => {
          const s = opts.series as any[];
          if (s?.[0])
            s[0].data = overviewRes.data.serviceTypeDistribution.map((item: any) => ({
              name: item.category || item.serviceTypeName || '未知',
              value: item.count || 0
            }));
          return opts;
        });
      }
    }

    if (elderRes.data) {
      elderStats.value = elderRes.data;
      if (elderRes.data.ageDistribution?.length) {
        updateAgePieOptions(opts => {
          opts.series[0].data = elderRes.data.ageDistribution.map((item: any) => ({
            name: item.ageRange || item.label || '未知',
            value: item.count || 0
          }));
          return opts;
        });
      }
      if (elderRes.data.serviceDemandDistribution?.length) {
        updateDemandOptions(opts => {
          opts.series[0].data = elderRes.data.serviceDemandDistribution.map((item: any) => ({
            name: item.serviceTypeName || item.serviceType || '未知',
            value: item.count || 0
          }));
          return opts;
        });
      }
    }

    if (orderRes.data) {
      orderStats.value = orderRes.data;
      if (orderRes.data.orderTrend?.length) {
        updateOrderTrendOptions(opts => {
          opts.xAxis.data = orderRes.data.orderTrend.map((item: any) => item.date);
          opts.series[0].data = orderRes.data.orderTrend.map((item: any) => item.orderCount);
          opts.series[1].data = orderRes.data.orderTrend.map((item: any) => item.completedCount);
          return opts;
        });
      }
      if (orderRes.data.serviceTypeDistribution?.length) {
        updateServiceBarOptions(opts => {
          opts.xAxis.data = orderRes.data.serviceTypeDistribution.map((item: any) => item.serviceTypeName);
          opts.series[0].data = orderRes.data.serviceTypeDistribution.map((item: any) => item.orderCount);
          return opts;
        });
      }
      if (orderRes.data.orderSourceDistribution?.length) {
        updateSourceOptions(opts => {
          opts.series[0].data = orderRes.data.orderSourceDistribution.map((item: any) => ({
            name: item.sourceName || item.orderSource || '未知',
            value: item.count || 0
          }));
          return opts;
        });
      }
    }

    if (financialRes.data) {
      financialStats.value = financialRes.data;
    }

    if (qualityRes.data) {
      qualityStats.value = qualityRes.data;
      if (qualityRes.data.ratingTrend?.length) {
        updateRatingTrendOptions(opts => {
          opts.xAxis.data = qualityRes.data.ratingTrend.map((item: any) => item.date);
          opts.series[0].data = qualityRes.data.ratingTrend.map((item: any) => item.averageRating);
          return opts;
        });
      }
      updateEvalOptions(opts => {
        opts.series[0].data = [
          { name: '好评', value: qualityRes.data.positiveCount || 0 },
          { name: '中评', value: qualityRes.data.neutralCount || 0 },
          { name: '差评', value: qualityRes.data.negativeCount || 0 }
        ];
        return opts;
      });
      if (qualityRes.data.complaintTypes?.length) {
        updateComplaintOptions(opts => {
          opts.xAxis.data = qualityRes.data.complaintTypes.map((item: any) => item.typeName || item.complaintType);
          opts.series[0].data = qualityRes.data.complaintTypes.map((item: any) => item.count);
          return opts;
        });
      }
    }
  } catch (e) {
    console.error('Failed to get data', e);
  }
}

// ============ 模拟数据初始化 ============
function initMockData() {
  updatePieOptions(opts => {
    const s = opts.series as any[];
    if (s?.[0])
      s[0].data = [
        { name: '生活照料', value: 320 },
        { name: '医疗护理', value: 240 },
        { name: '助餐服务', value: 180 },
        { name: '康复护理', value: 150 },
        { name: '精神慰藉', value: 90 },
        { name: '紧急救援', value: 60 }
      ];
    return opts;
  });
  updateAgePieOptions(opts => {
    opts.series[0].data = [
      { name: '60-69岁', value: 320 },
      { name: '70-79岁', value: 480 },
      { name: '80-89岁', value: 350 },
      { name: '90岁以上', value: 80 }
    ];
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
  updateOrderTrendOptions(opts => {
    opts.xAxis.data = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
    opts.series[0].data = [120, 150, 180, 170, 190, 210, 195];
    opts.series[1].data = [100, 130, 160, 155, 175, 195, 180];
    return opts;
  });
  updateServiceBarOptions(opts => {
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
  updateTrendOptions(opts => {
    opts.xAxis.data = ['1月', '2月', '3月', '4月', '5月', '6月'];
    opts.series[0].data = [120000, 135000, 150000, 142000, 160000, 175000];
    opts.series[1].data = [80000, 90000, 95000, 92000, 100000, 110000];
    opts.series[2].data = [40000, 45000, 55000, 50000, 60000, 65000];
    return opts;
  });
  updateServiceAmountOptions(opts => {
    opts.series[0].data = [
      { name: '生活照料', value: 450000 },
      { name: '医疗护理', value: 320000 },
      { name: '助餐服务', value: 280000 },
      { name: '康复护理', value: 180000 },
      { name: '精神慰藉', value: 95000 },
      { name: '紧急救援', value: 45000 }
    ];
    return opts;
  });
  updateRatingTrendOptions(opts => {
    opts.xAxis.data = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
    opts.series[0].data = [4.5, 4.6, 4.4, 4.7, 4.8, 4.6, 4.9];
    return opts;
  });
  updateEvalOptions(opts => {
    opts.series[0].data = [
      { name: '好评', value: 850 },
      { name: '中评', value: 120 },
      { name: '差评', value: 30 }
    ];
    return opts;
  });
  updateComplaintOptions(opts => {
    opts.xAxis.data = ['服务态度', '响应速度', '服务质量', '人员素质', '其他'];
    opts.series[0].data = [15, 8, 12, 5, 3];
    return opts;
  });
}

// ============ 工具函数 ============
function formatNumber(num: number | string | undefined | null): string {
  if (num == null) return '0';
  const n = typeof num === 'string' ? parseFloat(num) : num;
  if (isNaN(n)) return '0';
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return n.toString();
}
function formatMoney(num: number): string {
  if (num >= 10000) return (num / 10000).toFixed(1) + '万';
  return num?.toString() || '0';
}

function drillDown(type: string) {
  const routes: Record<string, string> = {
    order: '/business/order',
    elder: '/business/elder',
    provider: '/business/provider',
    staff: '/business/staff',
    financial: '/business/financial',
    evaluation: '/business/evaluation'
  };
  const route = routes[type];
  if (route) router.push(route);
}

// 初始化
initMockData();
getData();
</script>

<template>
  <div class="p-4">
    <NAlert title="运营驾驶舱" type="info" :bordered="false" class="mb-4">
      实时展示平台运营数据，包括订单统计、服务商分布、服务人员业绩等核心指标
    </NAlert>

    <!-- 第一行统计卡片 - 5列 -->
    <NGrid :cols="5" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi>
        <div class="stat-card stat-card-p1 cursor-pointer" @click="drillDown('order')">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">今日订单</span>
            <span class="stat-value">{{ formatNumber(overview.todayOrders) }}</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-p2 cursor-pointer">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">本月订单</span>
            <span class="stat-value">{{ formatNumber(overview.monthOrders) }}</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-p3 cursor-pointer">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">累计订单</span>
            <span class="stat-value">{{ formatNumber(overview.totalOrders) }}</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-p4 cursor-pointer">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">服务老人</span>
            <span class="stat-value">{{ formatNumber(overview.elderCount) }}</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-p5 cursor-pointer">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">服务商</span>
            <span class="stat-value">{{ formatNumber(overview.providerCount) }}</span>
          </div>
        </div>
      </NGi>
    </NGrid>

    <!-- 第二行统计卡片 - 5列 -->
    <NGrid :cols="5" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi>
        <div class="stat-card stat-card-s1">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z" />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">服务人员</span>
            <span class="stat-value">{{ formatNumber(overview.staffCount) }}</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-s2">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">本月营收</span>
            <span class="stat-value">¥{{ formatNumber(overview.monthRevenue) }}</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-s3">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z" />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">满意度</span>
            <span class="stat-value">{{ Number(overview.satisfaction || 0).toFixed(1) }}%</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-s4">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">老人总数</span>
            <span class="stat-value">{{ elderStats.totalElders || 0 }}</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-s5">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">本月新增</span>
            <span class="stat-value">{{ elderStats.monthlyNewElders || 0 }}</span>
          </div>
        </div>
      </NGi>
    </NGrid>

    <!-- 图表区域1 - 每行3列 -->
    <NGrid :cols="3" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi>
        <div class="chart-card">
          <div class="chart-title">服务类型分布</div>
          <div ref="pieDomRef" class="h-280px"></div>
        </div>
      </NGi>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">老人年龄分布</div>
          <div ref="agePieRef" class="h-280px"></div>
        </div>
      </NGi>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">服务需求分布</div>
          <div ref="demandRef" class="h-280px"></div>
        </div>
      </NGi>
    </NGrid>

    <!-- 图表区域2 - 每行3列 -->
    <NGrid :cols="3" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi>
        <div class="chart-card">
          <div class="chart-title">订单来源分布</div>
          <div ref="sourceRef" class="h-280px"></div>
        </div>
      </NGi>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">月度收支趋势</div>
          <div ref="trendRef" class="h-280px"></div>
        </div>
      </NGi>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">服务类型金额分布</div>
          <div ref="serviceAmountRef" class="h-280px"></div>
        </div>
      </NGi>
    </NGrid>

    <!-- 第三行统计卡片 - 4列 -->
    <NGrid :cols="4" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi>
        <div class="stat-card stat-card-o1">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">订单总数</span>
            <span class="stat-value">{{ formatNumber(orderStats.totalOrders) }}</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-o2">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">已完成</span>
            <span class="stat-value">{{ formatNumber(orderStats.completedOrders) }}</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-o3">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">完成率</span>
            <span class="stat-value">{{ (orderStats.completionRate || 0).toFixed(1) }}%</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-f1">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">总收入</span>
            <span class="stat-value">¥{{ formatMoney(financialStats.totalAmount) }}</span>
          </div>
        </div>
      </NGi>
    </NGrid>

    <NGrid :cols="4" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi>
        <div class="stat-card stat-card-f2">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">政府补贴</span>
            <span class="stat-value">¥{{ formatMoney(financialStats.totalSubsidyAmount) }}</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-f3">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"
              />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">自付金额</span>
            <span class="stat-value">¥{{ formatMoney(financialStats.totalSelfPayAmount) }}</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-q1">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z" />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">平均评分</span>
            <span class="stat-value">{{ (qualityStats.averageRating || 0).toFixed(1) }}</span>
          </div>
        </div>
      </NGi>
      <NGi>
        <div class="stat-card stat-card-q2">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z" />
            </svg>
          </div>
          <div class="stat-body">
            <span class="stat-label">好评率</span>
            <span class="stat-value">{{ ((qualityStats.positiveRate || 0) * 100).toFixed(1) }}%</span>
          </div>
        </div>
      </NGi>
    </NGrid>

    <NGrid :cols="3" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi>
        <div class="chart-card">
          <div class="chart-title">服务质量雷达</div>
          <div ref="radarDomRef" class="h-280px"></div>
        </div>
      </NGi>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">订单转化漏斗</div>
          <div ref="funnelDomRef" class="h-280px"></div>
        </div>
      </NGi>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">服务商排名TOP5</div>
          <div v-if="overview.providerRanking?.length" class="ranking-list">
            <div
              v-for="(item, index) in overview.providerRanking.slice(0, 5)"
              :key="item.providerId"
              class="ranking-item"
            >
              <div class="ranking-left">
                <span class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
                <span class="ranking-name">{{ item.providerName }}</span>
              </div>
              <div class="ranking-right">
                <span class="ranking-value">{{ item.orderCount || 0 }}单</span>
                <span
                  class="rank-score"
                  :class="item.rating >= 4.5 ? 'score-high' : item.rating >= 3 ? 'score-mid' : 'score-low'"
                >
                  {{ Number(item.rating || 0).toFixed(1) }}分
                </span>
              </div>
            </div>
          </div>
          <div v-else class="empty-tip">暂无数据</div>
        </div>
      </NGi>
    </NGrid>

    <NGrid :cols="3" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi>
        <div class="chart-card">
          <div class="chart-title">服务人员排名TOP3</div>
          <div v-if="overview.staffRanking?.length" class="ranking-list">
            <div v-for="(item, index) in overview.staffRanking.slice(0, 3)" :key="item.staffId" class="ranking-item">
              <div class="ranking-left">
                <span class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
                <span class="ranking-name">{{ item.staffName }}</span>
                <span class="ranking-provider">{{ item.providerName }}</span>
              </div>
              <div class="ranking-right">
                <span class="ranking-value">{{ item.orderCount || 0 }}单</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-tip">暂无数据</div>
        </div>
      </NGi>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">订单趋势</div>
          <div ref="orderTrendRef" class="h-280px"></div>
        </div>
      </NGi>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">服务类型分布(订单)</div>
          <div ref="serviceBarRef" class="h-280px"></div>
        </div>
      </NGi>
    </NGrid>

    <NGrid :cols="3" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <NGi>
        <div class="chart-card">
          <div class="chart-title">评分趋势</div>
          <div ref="ratingTrendRef" class="h-280px"></div>
        </div>
      </NGi>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">评价分布</div>
          <div ref="evalRef" class="h-280px"></div>
        </div>
      </NGi>
      <NGi>
        <div class="chart-card">
          <div class="chart-title">投诉类型分析</div>
          <div ref="complaintRef" class="h-280px"></div>
        </div>
      </NGi>
    </NGrid>
  </div>
</template>

<style scoped>
/* 统计卡片基础样式 */
.stat-card {
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: none;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}
.cursor-pointer {
  cursor: pointer;
}
.cursor-pointer:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

/* 图标容器 */
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-icon svg {
  width: 26px;
  height: 26px;
  color: white;
}

/* 内容区域 */
.stat-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  white-space: nowrap;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: white;
  line-height: 1.2;
}

/* 第一行卡片配色 - 蓝紫色系 */
.stat-card-p1 {
  background: linear-gradient(135deg, #5da8ff 0%, #8e9dff 100%);
}
.stat-card-p2 {
  background: linear-gradient(135deg, #8e9dff 0%, #a18cd1 100%);
}
.stat-card-p3 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.stat-card-p4 {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}
.stat-card-p5 {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
}

/* 第二行卡片配色 - 绿青/暖色系 */
.stat-card-s1 {
  background: linear-gradient(135deg, #26deca 0%, #00b42a 100%);
}
.stat-card-s2 {
  background: linear-gradient(135deg, #fedc69 0%, #ff7d00 100%);
}
.stat-card-s3 {
  background: linear-gradient(135deg, #ff7d00 0%, #ff4d4f 100%);
}
.stat-card-s4 {
  background: linear-gradient(135deg, #5da8ff 0%, #26deca 100%);
}
.stat-card-s5 {
  background: linear-gradient(135deg, #8e9dff 0%, #fedc69 100%);
}

/* 第三行订单/财务卡片配色 */
.stat-card-o1 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.stat-card-o2 {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}
.stat-card-o3 {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}
.stat-card-f1 {
  background: linear-gradient(135deg, #fedc69 0%, #ff7d00 100%);
}
.stat-card-f2 {
  background: linear-gradient(135deg, #26deca 0%, #00b42a 100%);
}
.stat-card-f3 {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7d00 100%);
}

/* 第四行质量卡片配色 */
.stat-card-q1 {
  background: linear-gradient(135deg, #667eea 0%, #f093fb 100%);
}
.stat-card-q2 {
  background: linear-gradient(135deg, #ff7d00 0%, #fee140 100%);
}

/* 图表卡片 */
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
.h-280px {
  height: 280px;
}

/* 排行榜 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.ranking-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--n-color-embedded);
  border-radius: 8px;
  transition: all 0.2s;
}
.ranking-item:hover {
  background: var(--n-color-hover);
  transform: translateX(4px);
}
.ranking-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.rank-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: white;
}
.rank-1 {
  background: linear-gradient(135deg, #ffd700, #ffb347);
}
.rank-2 {
  background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
}
.rank-3 {
  background: linear-gradient(135deg, #cd7f32, #b8860b);
}
.rank-4,
.rank-5 {
  background: var(--n-primary-color);
}
.ranking-name {
  font-weight: 500;
  color: var(--n-text-color);
}
.ranking-provider {
  font-size: 12px;
  color: var(--n-text-color-3);
}
.ranking-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.ranking-value {
  font-weight: 500;
  color: var(--n-primary-color);
}
.rank-score {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.score-high {
  background: rgba(0, 180, 42, 0.1);
  color: #00b42a;
}
.score-mid {
  background: rgba(255, 125, 0, 0.1);
  color: #ff7d00;
}
.score-low {
  background: rgba(255, 77, 79, 0.1);
  color: #ff4d4f;
}
.empty-tip {
  text-align: center;
  padding: 40px;
  color: var(--n-text-color-3);
}
</style>
