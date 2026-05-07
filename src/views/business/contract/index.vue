<script setup lang="ts">
import { ref, h, onMounted } from 'vue';
import {
  NButton,
  NCard,
  NTag,
  NSpace,
  NInput,
  NSelect,
  NDatePicker,
  useMessage,
  NDescriptions,
  NDescriptionsItem,
  NDrawer,
  NDrawerContent
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import {
  fetchGetContractList,
  fetchGetContractByOrderId,
  fetchGetSignUrl,
  fetchDownloadContract
} from '@/service/api';

defineOptions({
  name: 'BusinessContract'
});

const message = useMessage();

// Search
const searchContractNo = ref('');
const searchStatus = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Status options
const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发起', value: 'INITIATED' },
  { label: '签署中', value: 'SIGNING' },
  { label: '已签署', value: 'SIGNED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已过期', value: 'EXPIRED' },
  { label: '已拒签', value: 'REJECTED' },
  { label: '已撤回', value: 'CANCELLED' }
];

function getStatusType(status: string): 'warning' | 'success' | 'info' | 'error' | 'default' {
  const map: Record<string, 'warning' | 'success' | 'info' | 'error' | 'default'> = {
    DRAFT: 'default',
    INITIATED: 'warning',
    SIGNING: 'info',
    SIGNED: 'success',
    COMPLETED: 'success',
    EXPIRED: 'error',
    REJECTED: 'error',
    CANCELLED: 'error'
  };
  return map[status] || 'default';
}

function getStatusLabel(status: string): string {
  const option = statusOptions.find(o => o.value === status);
  return option?.label || status;
}

// Table data
const tableData = ref<Api.Ess.Contract[]>([]);
const loading = ref(false);
const pagination = ref({ page: 1, pageSize: 10, total: 0 });

// Detail drawer
const detailVisible = ref(false);
const detailData = ref<Api.Ess.Contract | null>(null);

// Table columns
const columns: DataTableColumns<Api.Ess.Contract> = [
  { title: '合同编号', key: 'contractNo', width: 180 },
  { title: '合同名称', key: 'contractName', width: 200 },
  { title: '关联订单', key: 'orderNo', width: 160 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: row => h(NTag, { type: getStatusType(row.status), size: 'small' }, () => getStatusLabel(row.status))
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: row => {
      const buttons = [];
      buttons.push(h(NButton, { size: 'small', onClick: () => showDetail(row) }, () => '详情'));
      if (row.status === 'SIGNED' || row.status === 'COMPLETED') {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleDownload(row) }, () => '下载'));
      }
      return h(NSpace, { size: 'small' }, () => buttons);
    }
  }
];

async function getData() {
  loading.value = true;
  try {
    const params: any = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize
    };
    if (searchContractNo.value) params.contractNo = searchContractNo.value;
    if (searchStatus.value) params.status = searchStatus.value;
    if (searchDateRange.value) {
      params.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      params.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }
    const { data } = await fetchGetContractList(params);
    if (data) {
      // 兼容后端返回的数组格式
      tableData.value = Array.isArray(data) ? data : (data.records || []);
      pagination.value.total = Array.isArray(data) ? data.length : (data.total || 0);
    }
  } catch (e) {
    console.error('Failed to get contract list', e);
  } finally {
    loading.value = false;
  }
}

function showDetail(row: Api.Ess.Contract) {
  detailData.value = row;
  detailVisible.value = true;
}

async function handleDownload(row: Api.Ess.Contract) {
  try {
    const { data } = await fetchDownloadContract(row.contractId);
    if (data?.downloadUrl) {
      window.open(data.downloadUrl, '_blank');
    }
  } catch (e) {
    message.error('下载合同失败');
  }
}

function handleResetSearch() {
  searchContractNo.value = '';
  searchStatus.value = '';
  searchDateRange.value = null;
  pagination.value.page = 1;
  getData();
}

function handlePageChange(page: number) {
  pagination.value.page = page;
  getData();
}

function handlePageSizeChange(pageSize: number) {
  pagination.value.pageSize = pageSize;
  getData();
}

onMounted(() => {
  getData();
});
</script>

<template>
  <div>
    <NCard title="合同管理" :bordered="false">
      <!-- Search -->
      <div style="background: #f5f5f5; padding: 12px; margin-bottom: 12px; border-radius: 4px">
        <NSpace :wrap="true" align="center">
          <NInput v-model:value="searchContractNo" placeholder="合同编号" clearable style="width: 180px" />
          <NSelect
            v-model:value="searchStatus"
            :options="statusOptions"
            placeholder="合同状态"
            clearable
            style="width: 120px"
          />
          <NDatePicker v-model:value="searchDateRange" type="daterange" clearable style="width: 260px" />
          <NButton type="primary" @click="handlePageChange(1)">搜索</NButton>
          <NButton @click="handleResetSearch">重置</NButton>
        </NSpace>
      </div>

      <!-- Table -->
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1200"
        :row-key="(row: Api.Ess.Contract) => row.contractId"
        :pagination="{
          page: pagination.page,
          pageSize: pagination.pageSize,
          total: pagination.total,
          'onUpdate:page': handlePageChange,
          'onUpdate:pageSize': handlePageSizeChange
        }"
      />
    </NCard>

    <!-- Detail Drawer -->
    <NDrawer v-model:show="detailVisible" :width="500" placement="right" closable>
      <NDrawerContent :title="detailData?.contractNo ? `合同详情 - ${detailData.contractNo}` : '合同详情'" closable>
        <NDescriptions v-if="detailData" :column="1" bordered size="small">
          <NDescriptionsItem label="合同编号">{{ detailData.contractNo || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="合同名称">{{ detailData.contractName || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="甲方（服务商）">{{ detailData.providerName || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="乙方（服务人员）">{{ detailData.staffName || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="关联订单">{{ detailData.orderNo || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="合同状态">
            <NTag :type="getStatusType(detailData.status)" size="small">
              {{ getStatusLabel(detailData.status) }}
            </NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="创建时间">{{ detailData.createTime || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="签署时间">{{ detailData.signedTime || '-' }}</NDescriptionsItem>
        </NDescriptions>
        <div v-else style="text-align: center; padding: 40px; color: #999">暂无数据</div>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="detailVisible = false">关闭</NButton>
            <NButton
              v-if="detailData && (detailData.status === 'SIGNED' || detailData.status === 'COMPLETED')"
              type="primary"
              @click="handleDownload(detailData)"
            >
              下载合同
            </NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>
  </div>
</template>