<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { NGrid, NGi } from 'naive-ui';
import { useEcharts } from '@/hooks/common/echarts';
import {
  fetchGetPublicCockpitOverview,
  fetchGetPublicOrderTrend,
  fetchGetPublicProviderRanking,
  fetchGetPublicServiceDistribution,
  fetchGetPublicAreaDistribution,
  fetchGetPublicAgeDistribution,
  fetchGetPublicCareLevelDistribution
} from '@/service/api/public';
import type { ECOption } from '@/hooks/common/echarts';

defineOptions({
  name: 'PublicDashboard'
});

// Token from URL
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token') || '';

// 分享的图表列表
const sharedCharts = ref<string[]>([]);

// 图表名称映射：驾驶舱名称 → 公开页名称
const chartNameMap: Record<string, string> = {
  '服务类型分布': '服务类型分布',
  '客户年龄分布': '客户年龄分布',
  '服务需求分布': '服务类型分布',
  '订单来源分布': '服务类型分布',
  '服务类型金额分布': '服务类型分布',
  '服务类型分布(订单)': '服务类型分布',
  '月度收支趋势': '服务类型分布',
  '订单趋势': '订单趋势',
  '区域分布': '区域分布',
  '护理等级分布': '护理等级分布',
  '服务商排名TOP5': '服务商排行TOP10',
  '服务质量雷达': '服务类型分布',
  '订单转化漏斗': '订单趋势',
  '服务人员排名TOP3': '服务商排行TOP10',
  '评分趋势': '订单趋势',
  '评价分布': '服务类型分布',
  '投诉类型分析': '订单趋势'
};

// 获取唯一需要显示的图表
const displayCharts = computed(() => {
  const chartsToShow = new Set<string>();

  if (sharedCharts.value.length === 0) {
    // 如果没有分享，默认显示核心图表
    chartsToShow.add('订单趋势');
    chartsToShow.add('服务类型分布');
    chartsToShow.add('区域分布');
    chartsToShow.add('客户年龄分布');
    chartsToShow.add('护理等级分布');
    chartsToShow.add('服务商排行TOP10');
  } else {
    // 只显示分享的图表，并映射到公开页的图表
    sharedCharts.value.forEach(name => {
      const mapped = chartNameMap[name];
      if (mapped) {
        chartsToShow.add(mapped);
      }
    });
  }

  return chartsToShow;
});

// 判断是否需要显示某个图表
function shouldShow(chartName: string): boolean {
  return displayCharts.value.has(chartName);
}

// 当前时间
const currentTime = ref(new Date().toLocaleString('zh-CN'));
let timeInterval: ReturnType<typeof setInterval>;

// ============ 数据状态 ============
const overview = ref<any>({
  todayOrders: 0,
  monthOrders: 0,
  totalOrders: 0,
  providerCount: 0,
  staffCount: 0,
  elderCount: 0,
  monthRevenue: 0,
  satisfaction: 0
});

const orderTrend = ref<any[]>([]);
const providerRanking = ref<any[]>([]);
const serviceDistribution = ref<any[]>([]);
const areaDistribution = ref<any[]>([]);
const ageDistribution = ref<any[]>([]);
const careLevelDistribution = ref<any[]>([]);

const isLoading = ref(true);
const error = ref<string | null>(null);

// ============ 图表定义 ============
// 订单趋势折线图
const { domRef: orderTrendRef, updateOptions: updateOrderTrendOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '10%', top: '15%', containLabel: true },
  xAxis: { type: 'category', data: [] as string[], boundaryGap: false, axisLabel: { color: '#999' } },
  yAxis: { type: 'value', axisLabel: { color: '#999' } },
  series: [
    {
      name: '订单数',
      type: 'line',
      smooth: true,
      data: [] as number[],
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(93, 168, 255, 0.6)' },
            { offset: 1, color: 'rgba(93, 168, 255, 0.1)' }
          ]
        }
      },
      itemStyle: { color: '#5da8ff' },
      lineStyle: { width: 3 }
    }
  ]
}));

// 服务类型分布饼图
const { domRef: servicePieRef, updateOptions: updateServicePieOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { color: '#666' } },
  series: [
    {
      name: '服务类型',
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['35%', '50%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

// 区域分布饼图
const { domRef: areaPieRef, updateOptions: updateAreaPieOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { color: '#666' } },
  series: [
    {
      name: '区域分布',
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['35%', '50%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

// 年龄分布柱状图
const { domRef: ageBarRef, updateOptions: updateAgeBarOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '10%', top: '15%', containLabel: true },
  xAxis: { type: 'category', data: [] as string[], axisLabel: { color: '#999', rotate: 30 } },
  yAxis: { type: 'value', axisLabel: { color: '#999' } },
  series: [
    {
      name: '客户数量',
      type: 'bar',
      data: [] as number[],
      itemStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
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

// 护理等级分布
const { domRef: careLevelRef, updateOptions: updateCareLevelOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { color: '#666' } },
  series: [
    {
      name: '护理等级',
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['35%', '50%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

// ============ 数据获取 ============
async function getData() {
  try {
    isLoading.value = true;
    error.value = null;

    const params = { token };

    const [overviewRes, trendRes, providerRes, serviceRes, areaRes, ageRes, careLevelRes] = await Promise.all([
      fetchGetPublicCockpitOverview(params),
      fetchGetPublicOrderTrend({ token, type: 'day' }),
      fetchGetPublicProviderRanking({ token, limit: 10 }),
      fetchGetPublicServiceDistribution(params),
      fetchGetPublicAreaDistribution(params),
      fetchGetPublicAgeDistribution(params),
      fetchGetPublicCareLevelDistribution(params)
    ]);

    if (overviewRes.data) {
      overview.value = overviewRes.data;
    }

    if (trendRes.data?.length) {
      updateOrderTrendOptions(opts => {
        (opts.xAxis as any).data = trendRes.data.map((item: any) => item.date || item.label);
        (opts.series as any)[0].data = trendRes.data.map((item: any) => item.count || item.value || 0);
        return opts;
      });
    }

    if (providerRes.data) {
      providerRanking.value = providerRes.data;
    }

    if (serviceRes.data?.length) {
      updateServicePieOptions(opts => {
        (opts.series as any)[0].data = serviceRes.data.map((item: any) => ({
          name: item.category || item.name || '未知',
          value: item.count || item.value || 0
        }));
        return opts;
      });
    }

    if (areaRes.data?.length) {
      updateAreaPieOptions(opts => {
        (opts.series as any)[0].data = areaRes.data.map((item: any) => ({
          name: item.area || item.name || '未知',
          value: item.count || item.value || 0
        }));
        return opts;
      });
    }

    if (ageRes.data?.length) {
      updateAgeBarOptions(opts => {
        (opts.xAxis as any).data = ageRes.data.map((item: any) => item.ageRange || item.label || '未知');
        (opts.series as any)[0].data = ageRes.data.map((item: any) => item.count || 0);
        return opts;
      });
    }

    if (careLevelRes.data?.length) {
      updateCareLevelOptions(opts => {
        (opts.series as any)[0].data = careLevelRes.data.map((item: any) => ({
          name: item.level || item.name || '未知',
          value: item.count || 0
        }));
        return opts;
      });
    }
  } catch (e: any) {
    console.error('Failed to get public data', e);
    error.value = e?.message || '获取数据失败，请检查Token是否有效';
  } finally {
    isLoading.value = false;
  }
}

// ============ 工具函数 ============
function formatNumber(num: number | string | undefined | null): string {
  if (num == null) return '0';
  const n = typeof num === 'string' ? parseFloat(num) : num;
  if (isNaN(n)) return '0';
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return n.toString();
}

function formatMoney(num: number | string | undefined | null): string {
  if (num == null) return '0';
  const n = typeof num === 'string' ? parseFloat(num) : num;
  if (isNaN(n)) return '0';
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return n.toString();
}

// ============ 生命周期 ============
onMounted(() => {
  // Set page title
  document.title = '智慧运营数据';

  // Load shared charts from localStorage
  const saved = localStorage.getItem('sharedCharts');
  if (saved) {
    try {
      sharedCharts.value = JSON.parse(saved);
    } catch (e) {
      console.error('Failed to load shared charts', e);
    }
  }

  // Update time every second
  timeInterval = setInterval(() => {
    currentTime.value = new Date().toLocaleString('zh-CN');
  }, 1000);

  // Get data
  getData();

  // Auto refresh every 60 seconds
  const refreshInterval = setInterval(getData, 60000);

  onUnmounted(() => {
    clearInterval(timeInterval);
    clearInterval(refreshInterval);
  });
});
</script>

<template>
  <div class="dashboard-container">
    <!-- Header -->
    <header class="dashboard-header">
      <div class="header-title">
        <h1>智慧居家养老服务管理平台</h1>
        <span class="header-subtitle">数据驾驶舱</span>
      </div>
      <div class="header-time">
        <span class="time-label">数据更新时间</span>
        <span class="time-value">{{ currentTime }}</span>
      </div>
    </header>

    <!-- Loading State -->
    <div v-if="isLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载数据...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-state">
      <p class="error-message">{{ error }}</p>
      <button class="retry-btn" @click="getData">重新加载</button>
    </div>

    <!-- Dashboard Content -->
    <div v-else class="dashboard-content">
      <!-- 核心指标区 -->
      <section class="section stats-section">
        <div class="stats-grid">
          <div class="stat-card stat-card-1">
            <div class="stat-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-label">今日订单</span>
              <span class="stat-value">{{ formatNumber(overview.todayOrders) }}</span>
            </div>
          </div>

          <div class="stat-card stat-card-2">
            <div class="stat-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-label">本月订单</span>
              <span class="stat-value">{{ formatNumber(overview.monthOrders) }}</span>
            </div>
          </div>

          <div class="stat-card stat-card-3">
            <div class="stat-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-label">累计订单</span>
              <span class="stat-value">{{ formatNumber(overview.totalOrders) }}</span>
            </div>
          </div>

          <div class="stat-card stat-card-4">
            <div class="stat-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5z"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-label">服务客户</span>
              <span class="stat-value">{{ formatNumber(overview.elderCount) }}</span>
            </div>
          </div>

          <div class="stat-card stat-card-5">
            <div class="stat-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-label">服务商</span>
              <span class="stat-value">{{ formatNumber(overview.providerCount) }}</span>
            </div>
          </div>

          <div class="stat-card stat-card-6">
            <div class="stat-icon">
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
      </section>

      <!-- 图表区域 - 第一行 -->
      <section class="section charts-section">
        <NGrid :cols="3" :x-gap="20" :y-gap="20">
          <!-- 订单趋势 -->
          <NGi v-if="shouldShow('订单趋势')">
            <div class="chart-card">
              <div class="chart-title">订单趋势</div>
              <div ref="orderTrendRef" class="chart-content"></div>
            </div>
          </NGi>

          <!-- 服务类型分布 -->
          <NGi v-if="shouldShow('服务类型分布')">
            <div class="chart-card">
              <div class="chart-title">服务类型分布</div>
              <div ref="servicePieRef" class="chart-content"></div>
            </div>
          </NGi>

          <!-- 区域分布 -->
          <NGi v-if="shouldShow('区域分布')">
            <div class="chart-card">
              <div class="chart-title">区域分布</div>
              <div ref="areaPieRef" class="chart-content"></div>
            </div>
          </NGi>
        </NGrid>
      </section>

      <!-- 图表区域 - 第二行 -->
      <section class="section charts-section">
        <NGrid :cols="3" :x-gap="20" :y-gap="20">
          <!-- 年龄分布 -->
          <NGi v-if="shouldShow('客户年龄分布')">
            <div class="chart-card">
              <div class="chart-title">客户年龄分布</div>
              <div ref="ageBarRef" class="chart-content"></div>
            </div>
          </NGi>

          <!-- 护理等级分布 -->
          <NGi v-if="shouldShow('护理等级分布')">
            <div class="chart-card">
              <div class="chart-title">护理等级分布</div>
              <div ref="careLevelRef" class="chart-content"></div>
            </div>
          </NGi>

          <!-- 服务商排行 -->
          <NGi v-if="shouldShow('服务商排行TOP10')">
            <div class="chart-card">
              <div class="chart-title">服务商排行TOP10</div>
              <div class="ranking-content">
                <div
                  v-for="(item, index) in providerRanking.slice(0, 10)"
                  :key="item.providerId || index"
                  class="ranking-item"
                >
                  <div class="ranking-left">
                    <span class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
                    <span class="ranking-name">{{ item.providerName || item.name || '未知' }}</span>
                  </div>
                  <div class="ranking-right">
                    <span class="ranking-value">{{ item.orderCount || item.count || 0 }}单</span>
                    <span class="ranking-score">{{ Number(item.rating || 0).toFixed(1) }}分</span>
                  </div>
                </div>
                <div v-if="!providerRanking.length" class="empty-tip">暂无数据</div>
              </div>
            </div>
          </NGi>
        </NGrid>
      </section>

      <!-- 无分享内容提示 -->
      <div v-if="sharedCharts.length > 0 && displayCharts.size === 0" class="empty-share">
        <p>分享的图表暂不支持在公开页显示</p>
      </div>
    </div>

    <!-- Footer -->
    <footer class="dashboard-footer">
      <span>智慧居家养老服务管理平台</span>
      <span class="footer-divider">|</span>
      <span>数据来源：系统实时统计</span>
    </footer>
  </div>
</template>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  color: white;
  display: flex;
  flex-direction: column;
}

/* Header */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background: rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-title {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.header-title h1 {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  background: linear-gradient(90deg, #5da8ff, #8e9dff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
}

.header-time {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.time-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.time-value {
  font-size: 16px;
  color: #5da8ff;
  font-family: monospace;
}

/* Loading & Error */
.loading-state,
.error-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-top-color: #5da8ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-message {
  color: #ff6b6b;
  font-size: 18px;
}

.retry-btn {
  padding: 10px 24px;
  background: #5da8ff;
  border: none;
  border-radius: 6px;
  color: white;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.retry-btn:hover {
  background: #4a96e8;
}

/* Dashboard Content */
.dashboard-content {
  flex: 1;
  padding: 20px 40px;
  overflow-y: auto;
}

.section {
  margin-bottom: 24px;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon svg {
  width: 28px;
  height: 28px;
  color: white;
}

.stat-card-1 .stat-icon { background: linear-gradient(135deg, #5da8ff, #8e9dff); }
.stat-card-2 .stat-icon { background: linear-gradient(135deg, #8e9dff, #a18cd1); }
.stat-card-3 .stat-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-card-4 .stat-icon { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-card-5 .stat-icon { background: linear-gradient(135deg, #26deca, #00b42a); }
.stat-card-6 .stat-icon { background: linear-gradient(135deg, #fedc69, #ff7d00); }

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: white;
  line-height: 1.2;
}

/* Chart Cards */
.chart-card {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 20px;
  height: 100%;
  min-height: 320px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.chart-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
  color: rgba(255, 255, 255, 0.9);
}

.chart-content {
  height: 260px;
}

/* Ranking */
.ranking-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 260px;
  overflow-y: auto;
}

.ranking-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  transition: background 0.2s;
}

.ranking-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.ranking-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.rank-badge {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.rank-1 { background: linear-gradient(135deg, #ffd700, #ffb347); }
.rank-2 { background: linear-gradient(135deg, #c0c0c0, #a0a0a0); }
.rank-3 { background: linear-gradient(135deg, #cd7f32, #b8860b); }
.rank-4, .rank-5, .rank-6, .rank-7, .rank-8, .rank-9, .rank-10 {
  background: #5da8ff;
}

.ranking-name {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ranking-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.ranking-value {
  font-size: 12px;
  color: #5da8ff;
  font-weight: 500;
}

.ranking-score {
  font-size: 12px;
  color: #26deca;
  font-weight: 500;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: rgba(255, 255, 255, 0.4);
}

.empty-share {
  text-align: center;
  padding: 40px;
  color: rgba(255, 255, 255, 0.5);
}

/* Footer */
.dashboard-footer {
  padding: 16px 40px;
  background: rgba(0, 0, 0, 0.3);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.footer-divider {
  opacity: 0.3;
}

/* Responsive */
@media (max-width: 1400px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .dashboard-header {
    padding: 16px 20px;
  }

  .header-title h1 {
    font-size: 22px;
  }

  .dashboard-content {
    padding: 16px 20px;
  }
}
</style>
