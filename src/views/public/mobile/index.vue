<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
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
  name: 'PublicMobile'
});

// Token from URL
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token') || '';

// Current time
const currentTime = ref(new Date().toLocaleString('zh-CN'));
let timeInterval: ReturnType<typeof setInterval>;

// ============ 数据状态 ============
const overview = ref<any>({
  todayOrders: 0,
  monthOrders: 0,
  totalOrders: 0,
  providerCount: 0,
  staffCount: 0,
  elderCount: 0
});

const orderTrend = ref<any[]>([]);
const providerRanking = ref<any[]>([]);
const serviceDistribution = ref<any[]>([]);
const ageDistribution = ref<any[]>([]);

const isLoading = ref(true);
const error = ref<string | null>(null);
const activeTab = ref('overview');

// ============ 图表定义 ============
// 订单趋势
const { domRef: trendRef, updateOptions: updateTrendOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '10%', top: '15%', containLabel: true },
  xAxis: { type: 'category', data: [] as string[], boundaryGap: false, axisLabel: { color: '#999', fontSize: 10 } },
  yAxis: { type: 'value', axisLabel: { color: '#999', fontSize: 10 } },
  series: [
    {
      name: '订单数',
      type: 'line',
      smooth: true,
      data: [] as number[],
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(93, 168, 255, 0.6)' },
            { offset: 1, color: 'rgba(93, 168, 255, 0.1)' }
          ]
        }
      },
      itemStyle: { color: '#5da8ff' },
      lineStyle: { width: 2 }
    }
  ]
}));

// 服务类型
const { domRef: serviceRef, updateOptions: updateServiceOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { orient: 'vertical', right: '2%', top: 'center', textStyle: { color: '#666', fontSize: 10 } },
  series: [
    {
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['40%', '50%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 12, fontWeight: 'bold' } },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

// 年龄分布
const { domRef: ageRef, updateOptions: updateAgeOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '10%', top: '15%', containLabel: true },
  xAxis: { type: 'category', data: [] as string[], axisLabel: { color: '#999', fontSize: 10, rotate: 30 } },
  yAxis: { type: 'value', axisLabel: { color: '#999', fontSize: 10 } },
  series: [
    {
      type: 'bar',
      data: [] as number[],
      itemStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
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

// ============ 数据获取 ============
async function getData() {
  try {
    isLoading.value = true;
    error.value = null;

    const params = { token };

    const [overviewRes, trendRes, providerRes, serviceRes, ageRes] = await Promise.all([
      fetchGetPublicCockpitOverview(params),
      fetchGetPublicOrderTrend({ token, type: 'day' }),
      fetchGetPublicProviderRanking({ token, limit: 5 }),
      fetchGetPublicServiceDistribution(params),
      fetchGetPublicAgeDistribution(params)
    ]);

    if (overviewRes.data) {
      overview.value = overviewRes.data;
    }

    if (trendRes.data?.length) {
      updateTrendOptions(opts => {
        opts.xAxis.data = trendRes.data.map((item: any) => item.date || item.label);
        opts.series[0].data = trendRes.data.map((item: any) => item.count || item.value || 0);
        return opts;
      });
    }

    if (providerRes.data) {
      providerRanking.value = providerRes.data;
    }

    if (serviceRes.data?.length) {
      updateServiceOptions(opts => {
        opts.series[0].data = serviceRes.data.map((item: any) => ({
          name: item.category || item.name || '未知',
          value: item.count || item.value || 0
        }));
        return opts;
      });
    }

    if (ageRes.data?.length) {
      updateAgeOptions(opts => {
        opts.xAxis.data = ageRes.data.map((item: any) => item.ageRange || item.label || '未知');
        opts.series[0].data = ageRes.data.map((item: any) => item.count || 0);
        return opts;
      });
    }
  } catch (e: any) {
    console.error('Failed to get data', e);
    error.value = e?.message || '获取数据失败';
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

// ============ 生命周期 ============
onMounted(() => {
  // Set page title
  document.title = '智慧运营数据';

  timeInterval = setInterval(() => {
    currentTime.value = new Date().toLocaleString('zh-CN');
  }, 1000);

  getData();
});

onUnmounted(() => {
  clearInterval(timeInterval);
});
</script>

<template>
  <div class="mobile-container">
    <!-- Header -->
    <header class="mobile-header">
      <h1>智慧养老</h1>
      <span class="update-time">{{ currentTime }}</span>
    </header>

    <!-- Loading -->
    <div v-if="isLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <button @click="getData">重试</button>
    </div>

    <!-- Content -->
    <div v-else class="mobile-content">
      <!-- Tab Navigation -->
      <div class="tab-nav">
        <button
          :class="{ active: activeTab === 'overview' }"
          @click="activeTab = 'overview'"
        >概览</button>
        <button
          :class="{ active: activeTab === 'trend' }"
          @click="activeTab = 'trend'"
        >趋势</button>
        <button
          :class="{ active: activeTab === 'ranking' }"
          @click="activeTab = 'ranking'"
        >排行</button>
      </div>

      <!-- Overview Tab -->
      <div v-if="activeTab === 'overview'" class="tab-content">
        <div class="stats-grid">
          <div class="stat-item">
            <span class="stat-value">{{ formatNumber(overview.todayOrders) }}</span>
            <span class="stat-label">今日订单</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ formatNumber(overview.monthOrders) }}</span>
            <span class="stat-label">本月订单</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ formatNumber(overview.totalOrders) }}</span>
            <span class="stat-label">累计订单</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ formatNumber(overview.elderCount) }}</span>
            <span class="stat-label">服务客户</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ formatNumber(overview.providerCount) }}</span>
            <span class="stat-label">服务商</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ formatNumber(overview.staffCount) }}</span>
            <span class="stat-label">服务人员</span>
          </div>
        </div>

        <div class="chart-section">
          <h3>服务类型分布</h3>
          <div ref="serviceRef" class="chart-container"></div>
        </div>

        <div class="chart-section">
          <h3>年龄分布</h3>
          <div ref="ageRef" class="chart-container"></div>
        </div>
      </div>

      <!-- Trend Tab -->
      <div v-if="activeTab === 'trend'" class="tab-content">
        <div class="chart-section full">
          <h3>订单趋势</h3>
          <div ref="trendRef" class="chart-container tall"></div>
        </div>
      </div>

      <!-- Ranking Tab -->
      <div v-if="activeTab === 'ranking'" class="tab-content">
        <div class="ranking-section">
          <h3>服务商排行TOP5</h3>
          <div class="ranking-list">
            <div
              v-for="(item, index) in providerRanking.slice(0, 5)"
              :key="item.providerId || index"
              class="ranking-item"
            >
              <div class="rank-left">
                <span class="rank-num" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
                <span class="rank-name">{{ item.providerName || '未知' }}</span>
              </div>
              <div class="rank-right">
                <span class="rank-orders">{{ item.orderCount || 0 }}单</span>
                <span class="rank-score">{{ Number(item.rating || 0).toFixed(1) }}分</span>
              </div>
            </div>
            <div v-if="!providerRanking.length" class="empty">暂无数据</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <footer class="mobile-footer">
      <span>智慧居家养老服务管理平台</span>
    </footer>
  </div>
</template>

<style scoped>
.mobile-container {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* Header */
.mobile-header {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: white;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
}

.mobile-header h1 {
  font-size: 18px;
  margin: 0;
  background: linear-gradient(90deg, #5da8ff, #8e9dff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.update-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
}

/* Loading & Error */
.loading-state,
.error-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 40px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(93, 168, 255, 0.2);
  border-top-color: #5da8ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state button {
  padding: 8px 20px;
  background: #5da8ff;
  border: none;
  border-radius: 6px;
  color: white;
  font-size: 14px;
}

/* Content */
.mobile-content {
  flex: 1;
  padding: 16px;
}

/* Tabs */
.tab-nav {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.tab-nav button {
  flex: 1;
  padding: 10px;
  border: none;
  background: white;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-nav button.active {
  background: #5da8ff;
  color: white;
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-item {
  background: white;
  border-radius: 10px;
  padding: 14px 10px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
}

.stat-label {
  font-size: 11px;
  color: #999;
}

/* Chart Section */
.chart-section {
  background: white;
  border-radius: 10px;
  padding: 14px;
}

.chart-section h3 {
  font-size: 14px;
  margin: 0 0 12px 0;
  color: #333;
}

.chart-section.full {
  margin: 0;
}

.chart-container {
  height: 200px;
}

.chart-container.tall {
  height: 300px;
}

/* Ranking */
.ranking-section {
  background: white;
  border-radius: 10px;
  padding: 14px;
}

.ranking-section h3 {
  font-size: 14px;
  margin: 0 0 12px 0;
  color: #333;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ranking-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f8f8;
  border-radius: 8px;
}

.rank-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rank-num {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  color: white;
}

.rank-1 { background: linear-gradient(135deg, #ffd700, #ffb347); }
.rank-2 { background: linear-gradient(135deg, #c0c0c0, #a0a0a0); }
.rank-3 { background: linear-gradient(135deg, #cd7f32, #b8860b); }
.rank-4, .rank-5 { background: #5da8ff; }

.rank-name {
  font-size: 13px;
  color: #333;
}

.rank-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rank-orders {
  font-size: 12px;
  color: #5da8ff;
  font-weight: 500;
}

.rank-score {
  font-size: 12px;
  color: #26deca;
  font-weight: 500;
}

.empty {
  text-align: center;
  padding: 30px;
  color: #999;
  font-size: 13px;
}

/* Footer */
.mobile-footer {
  padding: 12px;
  text-align: center;
  background: #1a1a2e;
  color: rgba(255, 255, 255, 0.5);
  font-size: 11px;
}
</style>
