<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { NCard, NSpace, NStatistic, NGrid, NGi, NProgress, NTag } from 'naive-ui';
import {
  fetchGetCockpitOverview,
  fetchGetServiceDistribution,
  fetchGetAreaDistribution,
  fetchGetProviderRanking,
  fetchGetStaffRanking
} from '@/service/api';

defineOptions({
  name: 'BusinessCockpit'
});

const loading = ref(false);
const data = ref<any>({
  overview: {
    totalOrders: 0,
    totalElders: 0,
    totalProviders: 0,
    totalStaff: 0,
    todayOrders: 0,
    monthOrders: 0,
    completionRate: 0
  },
  orderTrend: [],
  serviceTypeDistribution: [],
  areaDistribution: [],
  providerRanking: [],
  staffRanking: []
});

async function getData() {
  loading.value = true;
  try {
    const [overviewRes, serviceDistRes, areaDistRes, providerRankingRes, staffRankingRes] = await Promise.all([
      fetchGetCockpitOverview(),
      fetchGetServiceDistribution(),
      fetchGetAreaDistribution(),
      fetchGetProviderRanking(),
      fetchGetStaffRanking()
    ]);

    data.value = {
      overview: overviewRes.data || data.value.overview,
      orderTrend: [],
      serviceTypeDistribution: serviceDistRes.data || [],
      areaDistribution: areaDistRes.data || [],
      providerRanking: providerRankingRes.data || [],
      staffRanking: staffRankingRes.data || []
    };
  } catch (e) {
    console.error('Failed to get cockpit data', e);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  getData();
});
</script>

<template>
  <div>
    <NCard title="运营驾驶舱" :bordered="false">
      <!-- Overview Stats -->
      <NGrid :cols="4" :x-gap="20" :y-gap="20" item-responsive>
        <NGi>
          <NCard size="small">
            <NStatistic label="总订单数" :value="data.overview.totalOrders" />
          </NCard>
        </NGi>
        <NGi>
          <NCard size="small">
            <NStatistic label="服务老人数" :value="data.overview.totalElders" />
          </NCard>
        </NGi>
        <NGi>
          <NCard size="small">
            <NStatistic label="服务商数" :value="data.overview.totalProviders" />
          </NCard>
        </NGi>
        <NGi>
          <NCard size="small">
            <NStatistic label="服务人员数" :value="data.overview.totalStaff" />
          </NCard>
        </NGi>
        <NGi>
          <NCard size="small">
            <NStatistic label="今日订单" :value="data.overview.todayOrders" />
          </NCard>
        </NGi>
        <NGi>
          <NCard size="small">
            <NStatistic label="本月订单" :value="data.overview.monthOrders" />
          </NCard>
        </NGi>
        <NGi>
          <NCard size="small">
            <NStatistic label="完成率">
              <template #suffix>%</template>
              {{ data.overview.completionRate }}
            </NStatistic>
          </NCard>
        </NGi>
      </NGrid>

      <!-- Service Type Distribution -->
      <NGrid :cols="2" :x-gap="20" style="margin-top: 20px">
        <NGi>
          <NCard title="服务类型分布" size="small">
            <div v-for="item in data.serviceTypeDistribution" :key="item.type" style="margin-bottom: 8px">
              <NSpace justify="space-between">
                <span>{{ item.type }}</span>
                <span>{{ item.count }} ({{ item.percentage }}%)</span>
              </NSpace>
              <NProgress
                :percentage="item.percentage"
                :show-indicator="false"
                status="success"
                style="margin-top: 4px"
              />
            </div>
          </NCard>
        </NGi>
        <NGi>
          <NCard title="区域分布" size="small">
            <div v-for="item in data.areaDistribution" :key="item.area" style="margin-bottom: 8px">
              <NSpace justify="space-between">
                <span>{{ item.area }}</span>
                <span>{{ item.count }}</span>
              </NSpace>
              <NProgress :percentage="item.percentage" :show-indicator="false" status="info" style="margin-top: 4px" />
            </div>
          </NCard>
        </NGi>
      </NGrid>

      <!-- Provider Ranking -->
      <NGrid :cols="2" :x-gap="20" style="margin-top: 20px">
        <NGi>
          <NCard title="服务商排名" size="small">
            <div v-for="(item, index) in data.providerRanking" :key="item.providerId" style="margin-bottom: 8px">
              <NSpace>
                <NTag size="small" :type="index === 0 ? 'success' : index === 1 ? 'warning' : 'default'">
                  {{ Number(index) + 1 }}
                </NTag>
                <span>{{ item.providerName }}</span>
                <span style="margin-left: auto">{{ item.orderCount }}单</span>
              </NSpace>
            </div>
          </NCard>
        </NGi>
        <NGi>
          <NCard title="服务人员排名" size="small">
            <div v-for="(item, index) in data.staffRanking" :key="item.staffId" style="margin-bottom: 8px">
              <NSpace>
                <NTag size="small" :type="index === 0 ? 'success' : index === 1 ? 'warning' : 'default'">
                  {{ Number(index) + 1 }}
                </NTag>
                <span>{{ item.staffName }}</span>
                <span style="margin-left: auto">{{ item.orderCount }}单</span>
              </NSpace>
            </div>
          </NCard>
        </NGi>
      </NGrid>
    </NCard>
  </div>
</template>
