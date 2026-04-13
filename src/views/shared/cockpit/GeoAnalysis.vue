<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { NGrid, NGi, NDataTable, NAlert, NSwitch } from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import BMapContainer from '@/components/common/bmap-container.vue';
import type { MarkerItem, HeatPoint } from '@/components/common/bmap-container.vue';
import { useEcharts } from '@/hooks/common/echarts';
import { fetchGetCockpitOverview, fetchGetHeatMapData } from '@/service/api';
import type { ECOption } from '@/hooks/common/echarts';

defineOptions({
  name: 'GeoAnalysis'
});

const loading = ref(false);
const showHeatmap = ref(true);

// 热力图数据点 - 从API获取
const heatMarkers = ref<MarkerItem[]>([
  { position: [109.4890, 36.5856], title: '宝塔区 - 延安市', data: { area: '宝塔区', count: 1250 } },
  { position: [109.2137, 36.8699], title: '安塞区', data: { area: '安塞区', count: 680 } },
  { position: [110.1936, 36.9107], title: '延川县', data: { area: '延川县', count: 420 } },
  { position: [109.6737, 37.1427], title: '子长市', data: { area: '子长市', count: 550 } },
  { position: [109.5833, 36.8833], title: '延长县', data: { area: '延长县', count: 380 } }
]);

// 热力图原始数据
const heatPoints = ref<HeatPoint[]>([
  { lng: 109.4890, lat: 36.5856, count: 1250, name: '宝塔区' },
  { lng: 109.2137, lat: 36.8699, count: 680, name: '安塞区' },
  { lng: 110.1936, lat: 36.9107, count: 420, name: '延川县' },
  { lng: 109.6737, lat: 37.1427, count: 550, name: '子长市' },
  { lng: 109.5833, lat: 36.8833, count: 380, name: '延长县' }
]);

// Area ranking data
const areaRankingData = ref<any[]>([
  { areaId: '1', areaName: '宝塔区', orderCount: 1250, elderCount: 3200, providerCount: 12, staffCount: 85 },
  { areaId: '2', areaName: '安塞区', orderCount: 680, elderCount: 1850, providerCount: 8, staffCount: 52 },
  { areaId: '3', areaName: '子长市', orderCount: 550, elderCount: 1420, providerCount: 6, staffCount: 41 },
  { areaId: '4', areaName: '延川县', orderCount: 420, elderCount: 980, providerCount: 5, staffCount: 32 },
  { areaId: '5', areaName: '延长县', orderCount: 380, elderCount: 890, providerCount: 4, staffCount: 28 }
]);

const areaColumns: DataTableColumns<any> = [
  { title: '排名', key: 'rank', width: 60, render: (_: any, index: number) => index + 1 },
  { title: '区域', key: 'areaName', width: 100 },
  { title: '订单数', key: 'orderCount', width: 100 },
  { title: '老人数', key: 'elderCount', width: 100 },
  { title: '服务商数', key: 'providerCount', width: 100 },
  { title: '服务人员', key: 'staffCount', width: 100 }
];

// Area distribution bar chart
const { domRef: areaBarRef, updateOptions: updateAreaBarOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: '1%', left: 'center' },
  grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
  xAxis: { type: 'category', data: [] as string[] },
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

// Provider distribution pie chart
const { domRef: providerPieRef, updateOptions: updateProviderPieOptions } = useEcharts<ECOption>(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '1%', left: 'center' },
  series: [
    {
      color: ['#5da8ff', '#8e9dff', '#fedc69', '#26deca', '#ff7d00'],
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
    // 获取概览数据
    const overviewRes = await fetchGetCockpitOverview();
    if (overviewRes.data) {
      // Update area ranking with real data
      if (overviewRes.data.areaDistribution?.length) {
        areaRankingData.value = overviewRes.data.areaDistribution.slice(0, 5).map((item: any, index: number) => ({
          areaId: item.areaId,
          areaName: item.areaName || `区域${index + 1}`,
          orderCount: item.orderCount || 0,
          elderCount: Math.floor((item.orderCount || 0) * 2.5),
          providerCount: Math.floor((item.orderCount || 0) * 0.01),
          staffCount: Math.floor((item.orderCount || 0) * 0.07)
        }));

        updateAreaBarOptions(opts => {
          opts.xAxis.data = areaRankingData.value.map((item: any) => item.areaName);
          opts.series[0].data = areaRankingData.value.map((item: any) => item.orderCount);
          return opts;
        });
      }

      // Update provider distribution
      if (overviewRes.data.providerRanking?.length) {
        updateProviderPieOptions(opts => {
          opts.series[0].data = overviewRes.data.providerRanking.slice(0, 5).map((item: any) => ({
            name: item.providerName || '未知',
            value: item.orderCount || 0
          }));
          return opts;
        });
      }
    }

    // 获取热力图数据
    const heatRes = await fetchGetHeatMapData();
    if (heatRes.data?.heatPoints?.length) {
      // 更新热力图数据点
      heatPoints.value = heatRes.data.heatPoints.map((point: any) => ({
        lng: point.lng,
        lat: point.lat,
        count: point.count,
        name: point.name
      }));

      // 更新标记点（TOP3）
      const top3 = heatRes.data.heatPoints.slice(0, 3);
      heatMarkers.value = top3.map((point: any) => ({
        position: [point.lng, point.lat] as [number, number],
        title: `${point.name} - ${point.count}单`,
        data: { area: point.name, count: point.count }
      }));
    }
  } catch (e) {
    console.error('Failed to get geo data', e);
  } finally {
    loading.value = false;
  }
}

function handleMarkerClick(marker: AMap.Marker, item: MarkerItem) {
  console.log('Marker clicked:', item);
  // Could show detail modal or navigate to area page
}

onMounted(() => {
  // Initialize charts with mock data first
  updateAreaBarOptions(opts => {
    opts.xAxis.data = areaRankingData.value.map(item => item.areaName);
    opts.series[0].data = areaRankingData.value.map(item => item.orderCount);
    return opts;
  });

  updateProviderPieOptions(opts => {
    opts.series[0].data = areaRankingData.value.slice(0, 4).map((item: any, index: number) => ({
      name: item.areaName,
      value: item.orderCount * 0.1
    }));
    return opts;
  });

  getData();
});
</script>

<template>
  <div class="p-4">
    <NAlert title="地理专题分析" type="info" :bordered="false" class="mb-4">
      展示延安市各区县的订单分布、服务商分布及老人分布热力图
    </NAlert>

    <NGrid :cols="2" :x-gap="16" :y-gap="16" item-responsive class="mb-4">
      <!-- Map -->
      <NGi>
        <div class="chart-card">
          <div class="chart-title">
            延安市服务分布热力图
            <span class="text-xs ml-2">
              <NSwitch v-model:value="showHeatmap" size="small" /> 显示热力图
            </span>
          </div>
          <BMapContainer
            height="400px"
            :zoom="10"
            :center="[109.4897, 36.5997]"
            :markers="heatMarkers"
            :heat-points="heatPoints"
            :show-heatmap="showHeatmap"
            :heatmap-radius="25"
            @marker-click="handleMarkerClick"
          />
        </div>
      </NGi>

      <!-- Area ranking table -->
      <NGi>
        <div class="chart-card">
          <div class="chart-title">区域订单排名</div>
          <NDataTable
            :columns="areaColumns"
            :data="areaRankingData"
            :bordered="false"
            size="small"
          />
        </div>
      </NGi>
    </NGrid>

    <NGrid :cols="2" :x-gap="16" :y-gap="16" item-responsive>
      <!-- Area order distribution -->
      <NGi>
        <div class="chart-card">
          <div class="chart-title">各区县订单分布</div>
          <div ref="areaBarRef" class="h-250px"></div>
        </div>
      </NGi>

      <!-- Provider area distribution -->
      <NGi>
        <div class="chart-card">
          <div class="chart-title">服务商区域分布</div>
          <div ref="providerPieRef" class="h-250px"></div>
        </div>
      </NGi>
    </NGrid>
  </div>
</template>

<style scoped>
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

.h-250px {
  height: 250px;
}
</style>
