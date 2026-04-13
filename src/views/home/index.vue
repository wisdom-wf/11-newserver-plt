<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { NGrid, NGi, NAlert, NTabs, NTabPane, useMessage } from 'naive-ui';
import { useEcharts } from '@/hooks/common/echarts';
import { fetchGetCockpitOverview } from '@/service/api';
import ElderAnalysis from '@/views/shared/cockpit/ElderAnalysis.vue';
import OrderAnalysis from '@/views/shared/cockpit/OrderAnalysis.vue';
import FinancialAnalysis from '@/views/shared/cockpit/FinancialAnalysis.vue';
import QualityAnalysis from '@/views/shared/cockpit/QualityAnalysis.vue';
import GeoAnalysis from '@/views/shared/cockpit/GeoAnalysis.vue';
import type { ECOption } from '@/hooks/common/echarts';

defineOptions({
  name: 'Home'
});

const router = useRouter();
const message = useMessage();
const loading = ref(false);
const activeTab = ref('overview');

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

// 区域分布柱状图
const { domRef: barDomRef, updateOptions: updateBarOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: '1%', left: 'center' },
  grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
  xAxis: { type: 'category', data: [] as string[], axisLabel: { rotate: 0 } },
  yAxis: { type: 'value' },
  series: [
    {
      name: '订单数',
      type: 'bar',
      data: [] as number[],
      itemStyle: {
        color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
          { offset: 0, color: '#5da8ff' },
          { offset: 1, color: '#8e9dff' }
        ]},
        borderRadius: [4, 4, 0, 0]
      }
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

async function getData() {
  loading.value = true;
  try {
    const res = await fetchGetCockpitOverview();
    if (res.data) {
      overview.value = res.data;
      const serviceDist = res.data.serviceTypeDistribution;
      const areaDist = res.data.areaDistribution;
      if (serviceDist?.length) {
        updatePieOptions(opts => {
          const series = opts.series as any[];
          if (series?.[0]) {
            series[0].data = serviceDist.map((item: any) => ({
              name: item.category || item.serviceTypeName || '未知',
              value: item.count || 0
            }));
          }
          return opts;
        });
      }
      if (areaDist?.length) {
        updateBarOptions(opts => {
          const xAxis = opts.xAxis as any;
          const series = opts.series as any[];
          if (xAxis && xAxis.data !== undefined) {
            xAxis.data = areaDist.map((item: any) => item.areaName || item.areaId || '未知');
          }
          if (series?.[0]) {
            series[0].data = areaDist.map((item: any) => item.orderCount || 0);
          }
          return opts;
        });
      }
    }
  } catch (e) {
    console.error('Failed to get cockpit data', e);
  } finally {
    loading.value = false;
  }
}

function initMockData() {
  updatePieOptions(opts => {
    const series = opts.series as any[];
    if (series?.[0]) {
      series[0].data = [
        { name: '生活照料', value: 320 },
        { name: '医疗护理', value: 240 },
        { name: '助餐服务', value: 180 },
        { name: '康复护理', value: 150 },
        { name: '精神慰藉', value: 90 },
        { name: '紧急救援', value: 60 }
      ];
    }
    return opts;
  });
  updateBarOptions(opts => {
    const xAxis = opts.xAxis as any;
    const series = opts.series as any[];
    if (xAxis && xAxis.data !== undefined) {
      xAxis.data = ['宝塔区', '安塞区', '延长县', '延川县', '子长市'];
    }
    if (series?.[0]) {
      series[0].data = [180, 120, 95, 78, 65];
    }
    return opts;
  });
}

function formatNumber(num: number | string | undefined | null): string {
  if (num == null) return '0';
  const n = typeof num === 'string' ? parseFloat(num) : num;
  if (isNaN(n)) return '0';
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return n.toString();
}

// 下钻功能
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
  if (route) {
    router.push(route);
  } else {
    message.info('该模块下钻功能开发中');
  }
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

    <NTabs v-model:value="activeTab" type="line" animated>
      <NTabPane name="overview" tab="综合概览">
        <!-- 统计卡片 -->
        <NGrid :cols="4" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
          <NGi>
            <div class="stat-card cursor-pointer" @click="drillDown('order')">
              <div class="stat-content">
                <div class="stat-icon order-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
                  </svg>
                </div>
                <div class="stat-info">
                  <span class="stat-label">今日订单</span>
                  <span class="stat-value">{{ formatNumber(overview.todayOrders) }}</span>
                </div>
              </div>
            </div>
          </NGi>
          <NGi>
            <div class="stat-card cursor-pointer" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <div class="stat-content">
                <div class="stat-icon month-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z"/>
                  </svg>
                </div>
                <div class="stat-info">
                  <span class="stat-label">本月订单</span>
                  <span class="stat-value">{{ formatNumber(overview.monthOrders) }}</span>
                </div>
              </div>
            </div>
          </NGi>
          <NGi>
            <div class="stat-card cursor-pointer" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <div class="stat-content">
                <div class="stat-icon total-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                  </svg>
                </div>
                <div class="stat-info">
                  <span class="stat-label">累计订单</span>
                  <span class="stat-value">{{ formatNumber(overview.totalOrders) }}</span>
                </div>
              </div>
            </div>
          </NGi>
          <NGi>
            <div class="stat-card cursor-pointer" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <div class="stat-content">
                <div class="stat-icon elder-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>
                  </svg>
                </div>
                <div class="stat-info">
                  <span class="stat-label">服务老人</span>
                  <span class="stat-value">{{ formatNumber(overview.elderCount) }}</span>
                </div>
              </div>
            </div>
          </NGi>
        </NGrid>

        <NGrid :cols="4" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
          <NGi>
            <div class="stat-card cursor-pointer" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
              <div class="stat-content">
                <div class="stat-icon provider-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z"/>
                  </svg>
                </div>
                <div class="stat-info">
                  <span class="stat-label">服务商</span>
                  <span class="stat-value">{{ formatNumber(overview.providerCount) }}</span>
                </div>
              </div>
            </div>
          </NGi>
          <NGi>
            <div class="stat-card cursor-pointer" style="background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)">
              <div class="stat-content">
                <div class="stat-icon staff-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z"/>
                  </svg>
                </div>
                <div class="stat-info">
                  <span class="stat-label">服务人员</span>
                  <span class="stat-value">{{ formatNumber(overview.staffCount) }}</span>
                </div>
              </div>
            </div>
          </NGi>
          <NGi>
            <div class="stat-card" style="background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)">
              <div class="stat-content">
                <div class="stat-icon revenue-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/>
                  </svg>
                </div>
                <div class="stat-info">
                  <span class="stat-label">本月营收</span>
                  <span class="stat-value">¥{{ formatNumber(overview.monthRevenue) }}</span>
                </div>
              </div>
            </div>
          </NGi>
          <NGi>
            <div class="stat-card" style="background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)">
              <div class="stat-content">
                <div class="stat-icon satisfaction-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                  </svg>
                </div>
                <div class="stat-info">
                  <span class="stat-label">满意度</span>
                  <span class="stat-value">{{ Number(overview.satisfaction || 0).toFixed(1) }}%</span>
                </div>
              </div>
            </div>
          </NGi>
        </NGrid>

        <NGrid :cols="3" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
          <NGi>
            <div class="chart-card">
              <div class="chart-title">服务类型分布</div>
              <div ref="pieDomRef" class="h-250px"></div>
            </div>
          </NGi>
          <NGi>
            <div class="chart-card">
              <div class="chart-title">服务质量雷达</div>
              <div ref="radarDomRef" class="h-250px"></div>
            </div>
          </NGi>
          <NGi>
            <div class="chart-card">
              <div class="chart-title">订单转化漏斗</div>
              <div ref="funnelDomRef" class="h-250px"></div>
            </div>
          </NGi>
        </NGrid>

        <NGrid :cols="2" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
          <NGi>
            <div class="chart-card">
              <div class="chart-title">客户年龄分布</div>
              <div ref="barDomRef" class="h-250px"></div>
            </div>
          </NGi>
          <NGi>
            <div class="chart-card">
              <div class="chart-title">服务商排名TOP5</div>
              <div v-if="overview.providerRanking?.length" class="ranking-list">
                <div v-for="(item, index) in overview.providerRanking.slice(0, 5)" :key="item.providerId" class="ranking-item">
                  <div class="ranking-left">
                    <span class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
                    <span class="ranking-name">{{ item.providerName }}</span>
                  </div>
                  <div class="ranking-right">
                    <span class="ranking-value">{{ item.orderCount || 0 }}单</span>
                    <span class="rank-score" :class="item.rating >= 4.5 ? 'score-high' : item.rating >= 3 ? 'score-mid' : 'score-low'">
                      {{ Number(item.rating || 0).toFixed(1) }}分
                    </span>
                  </div>
                </div>
              </div>
              <div v-else class="empty-tip">暂无数据</div>
            </div>
          </NGi>
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
        </NGrid>
      </NTabPane>

      <NTabPane name="elder" tab="老人服务专题">
        <ElderAnalysis />
      </NTabPane>

      <NTabPane name="order" tab="订单分析专题">
        <OrderAnalysis />
      </NTabPane>

      <NTabPane name="financial" tab="财务分析专题">
        <FinancialAnalysis />
      </NTabPane>

      <NTabPane name="quality" tab="服务质量专题">
        <QualityAnalysis />
      </NTabPane>
      <NTabPane name="geo" tab="地理专题">
        <GeoAnalysis />
      </NTabPane>
    </NTabs>
  </div>
</template>

<style scoped>
.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  border: none;
}

.cursor-pointer {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.cursor-pointer:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
  color: white;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: white;
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

.rank-1 { background: linear-gradient(135deg, #ffd700, #ffb347); }
.rank-2 { background: linear-gradient(135deg, #c0c0c0, #a0a0a0); }
.rank-3 { background: linear-gradient(135deg, #cd7f32, #b8860b); }
.rank-4, .rank-5 { background: var(--n-primary-color); }

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

.score-high { background: rgba(0, 180, 42, 0.1); color: #00b42a; }
.score-mid { background: rgba(255, 125, 0, 0.1); color: #ff7d00; }
.score-low { background: rgba(255, 77, 79, 0.1); color: #ff4d4f; }

.empty-tip {
  text-align: center;
  padding: 40px;
  color: var(--n-text-color-3);
}
</style>
