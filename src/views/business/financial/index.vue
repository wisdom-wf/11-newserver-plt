<script setup lang="ts">
import { ref, h, onMounted } from 'vue';
import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NModal,
  NTag,
  NSpace,
  NInput,
  NSelect,
  NDatePicker,
  NPagination,
  NStatistic
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import { fetchGetSettlementList, fetchGetSettlementStatistics, fetchGetElder, fetchGetStaff } from '@/service/api';

defineOptions({
  name: 'BusinessFinancial'
});

// Statistics
const statistics = ref<Api.Financial.Statistics>({
  pending: 0,
  completed: 0,
  monthAmount: 0,
  totalAmount: 0,
  serviceFeeTotal: 0,
  subsidyTotal: 0,
  selfPayTotal: 0
});

// Search
const searchOrderNo = ref('');
const searchElderName = ref('');
const searchProviderName = ref('');
const searchStatus = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Table data
const loading = ref(false);
const tableData = ref<any[]>([]);
const pagination = ref({ page: 1, pageSize: 10, total: 0 });

// Elder detail modal
const elderDetailVisible = ref(false);
const elderDetailData = ref<Api.Elder.Elder | null>(null);

// Staff detail modal
const staffDetailVisible = ref(false);
const staffDetailData = ref<Api.Staff.Staff | null>(null);

async function showElderDetail(row: any) {
  if (!row.elderId) return;
  try {
    const { data } = await fetchGetElder(row.elderId);
    if (data) {
      elderDetailData.value = data;
      elderDetailVisible.value = true;
    }
  } catch (e) {
    console.error('Failed to get elder detail', e);
  }
}

async function showStaffDetail(row: any) {
  if (!row.staffId) return;
  try {
    const { data } = await fetchGetStaff(row.staffId);
    if (data) {
      staffDetailData.value = data;
      staffDetailVisible.value = true;
    }
  } catch (e) {
    console.error('Failed to get staff detail', e);
  }
}

// Status options
const statusOptions = [
  { label: '待结算', value: 'PENDING' },
  { label: '已结算', value: 'SETTLED' },
  { label: '已支付', value: 'PAID' }
];

function getStatusType(status: string): 'warning' | 'success' | 'info' | 'default' {
  const map: Record<string, 'warning' | 'success' | 'info' | 'default'> = {
    PENDING: 'warning',
    SETTLED: 'info',
    PAID: 'success'
  };
  return map[status] || 'default';
}

function getStatusLabel(status: string): string {
  const option = statusOptions.find(o => o.value === status);
  return option?.label || status;
}

const columns: DataTableColumns<any> = [
  { title: '结算单号', key: 'settlementNo', width: 160 },
  { title: '服务商', key: 'providerName', width: 150 },
  {
    title: '老人姓名',
    key: 'elderName',
    width: 100,
    render: row => row.elderName ? h('a', { style: { color: '#18a058', cursor: 'pointer' }, onClick: () => showElderDetail(row) }, row.elderName) : '-'
  },
  {
    title: '服务人员',
    key: 'staffName',
    width: 100,
    render: row => row.staffName ? h('a', { style: { color: '#18a058', cursor: 'pointer' }, onClick: () => showStaffDetail(row) }, row.staffName) : '-'
  },
  { title: '总服务费', key: 'totalServiceAmount', width: 100 },
  { title: '补贴金额', key: 'totalSubsidyAmount', width: 100 },
  { title: '自付金额', key: 'totalSelfPayAmount', width: 100 },
  { title: '结算金额', key: 'settlementAmount', width: 100 },
  {
    title: '结算状态',
    key: 'status',
    width: 100,
    render: row => h(NTag, { type: getStatusType(row.status), size: 'small' }, () => getStatusLabel(row.status))
  },
  { title: '确认时间', key: 'confirmTime', width: 170 },
  { title: '创建时间', key: 'createTime', width: 170 }
];

async function getStatistics() {
  try {
    const { data } = await fetchGetSettlementStatistics();
    if (data) {
      statistics.value = data;
    }
  } catch (e) {
    console.error('Failed to get statistics', e);
  }
}

async function getTableData() {
  loading.value = true;
  try {
    const params: any = {
      current: pagination.value.page,
      pageSize: pagination.value.pageSize
    };
    if (searchOrderNo.value) params.orderNo = searchOrderNo.value;
    if (searchElderName.value) params.elderName = searchElderName.value;
    if (searchProviderName.value) params.providerName = searchProviderName.value;
    if (searchStatus.value) params.status = searchStatus.value;
    if (searchDateRange.value) {
      params.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      params.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }

    const { data } = await fetchGetSettlementList(params);
    tableData.value = data?.records || [];
    pagination.value.total = data?.total || 0;
  } catch (e) {
    console.error('Failed to get table data', e);
  } finally {
    loading.value = false;
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page;
  getTableData();
}

function handlePageSizeChange(pageSize: number) {
  pagination.value.pageSize = pageSize;
  pagination.value.page = 1;
  getTableData();
}

onMounted(() => {
  getStatistics();
  getTableData();
});
</script>

<template>
  <div>
    <!-- Statistics Cards -->
    <NCard title="财务结算统计" :bordered="false" style="margin-bottom: 16px">
      <div class="statistics-grid">
        <div class="stat-card stat-primary">
          <div class="stat-label">总金额</div>
          <div class="stat-value">¥{{ Number(statistics.totalAmount || 0).toFixed(0) }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">补贴金额</div>
          <div class="stat-value">¥{{ Number(statistics.subsidyTotal || 0).toFixed(0) }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">自付金额</div>
          <div class="stat-value">¥{{ Number(statistics.selfPayTotal || 0).toFixed(0) }}</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-label">待结算金额</div>
          <div class="stat-value">¥{{ Number(statistics.pending || 0).toFixed(0) }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">结算单数</div>
          <div class="stat-value">{{ statistics.completed }}</div>
        </div>
      </div>
    </NCard>

    <!-- Table -->
    <NCard title="财务结算管理" :bordered="false">
      <template #header>
        <NSpace :wrap="true">
          <NInput v-model:value="searchOrderNo" placeholder="订单号" clearable style="width: 150px" />
          <NInput v-model:value="searchElderName" placeholder="老人姓名" clearable style="width: 100px" />
          <NInput v-model:value="searchProviderName" placeholder="服务商" clearable style="width: 150px" />
          <NSelect
            v-model:value="searchStatus"
            :options="statusOptions"
            placeholder="结算状态"
            clearable
            style="width: 120px"
          />
          <NDatePicker v-model:value="searchDateRange" type="daterange" clearable style="width: 260px" />
          <NButton type="primary" @click="getTableData">搜索</NButton>
        </NSpace>
      </template>
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1500"
        :row-key="(row: any) => row.id"
      />
      <div style="padding: 12px 0">
        <NSpace justify="end">
          <NSelect
            v-model:value="pagination.pageSize"
            :options="[10, 20, 50].map(s => ({ label: `${s}条/页`, value: s }))"
            style="width: 120px"
            @update:value="handlePageSizeChange"
          />
          <NPagination
            v-model:page="pagination.page"
            :page-count="Math.ceil(pagination.total / pagination.pageSize)"
            @update:page="handlePageChange"
          />
        </NSpace>
      </div>
    </NCard>

    <!-- Elder Detail Modal -->
    <NModal v-model:show="elderDetailVisible" title="老人档案详情" preset="card" style="width: 600px">
      <NForm v-if="elderDetailData" label-placement="left" label-width="100">
        <NFormItem label="姓名">{{ elderDetailData.name }}</NFormItem>
        <NFormItem label="性别">{{ elderDetailData.gender === 'MALE' ? '男' : elderDetailData.gender === 'FEMALE' ? '女' : '未知' }}</NFormItem>
        <NFormItem label="年龄">{{ elderDetailData.age }}</NFormItem>
        <NFormItem label="身份证号">{{ elderDetailData.idCard }}</NFormItem>
        <NFormItem label="手机号">{{ elderDetailData.phone }}</NFormItem>
        <NFormItem label="地址">{{ elderDetailData.address }}</NFormItem>
        <NFormItem label="养老类型">{{ elderDetailData.careType === 'HOME' ? '居家养老' : elderDetailData.careType === 'COMMUNITY' ? '社区养老' : elderDetailData.careType === 'INSTITUTION' ? '机构养老' : '-' }}</NFormItem>
        <NFormItem label="护理等级">{{ elderDetailData.careLevel }}</NFormItem>
        <NFormItem label="紧急联系人">{{ elderDetailData.emergencyContact || '-' }}</NFormItem>
        <NFormItem label="紧急联系电话">{{ elderDetailData.emergencyPhone || '-' }}</NFormItem>
      </NForm>
    </NModal>

    <!-- Staff Detail Modal -->
    <NModal v-model:show="staffDetailVisible" title="服务人员详情" preset="card" style="width: 600px">
      <NForm v-if="staffDetailData" label-placement="left" label-width="100">
        <NFormItem label="姓名">{{ staffDetailData.staffName }}</NFormItem>
        <NFormItem label="性别">{{ staffDetailData.gender === 1 ? '男' : '女' }}</NFormItem>
        <NFormItem label="工号">{{ staffDetailData.staffNo || '-' }}</NFormItem>
        <NFormItem label="手机号">{{ staffDetailData.phone || '-' }}</NFormItem>
        <NFormItem label="身份证号">{{ staffDetailData.idCard || '-' }}</NFormItem>
        <NFormItem label="所属服务商">{{ staffDetailData.providerName || '-' }}</NFormItem>
        <NFormItem label="服务类型">{{ staffDetailData.serviceTypes || '-' }}</NFormItem>
        <NFormItem label="紧急联系人">{{ staffDetailData.emergencyContact || '-' }}</NFormItem>
        <NFormItem label="紧急联系电话">{{ staffDetailData.emergencyPhone || '-' }}</NFormItem>
      </NForm>
    </NModal>
  </div>
</template>

<style scoped>
.statistics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 16px;
  border-radius: 8px;
  text-align: center;
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  line-height: 1.2;
}

.stat-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-primary .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-info {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-info .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-success {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.stat-success .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-warning {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-warning .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-error {
  background: linear-gradient(135deg, #ff0844 0%, #ffb199 100%);
  color: white;
}

.stat-error .stat-label {
  color: rgba(255, 255, 255, 0.85);
}
</style>
