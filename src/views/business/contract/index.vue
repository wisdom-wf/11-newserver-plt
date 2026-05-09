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
  fetchDownloadContract,
  fetchDeleteContract
} from '@/service/api';
import { useRouterPush } from '@/hooks/common/router';
import { useAuthStore } from '@/store/modules/auth';

const authStore = useAuthStore();
const isAdmin = authStore.userInfo.userType === 'ADMIN';

defineOptions({
  name: 'BusinessContract'
});

const message = useMessage();
const { routerPushByKeyWithMetaQuery } = useRouterPush();

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
const checkedRowKeys = ref<string[]>([]);
const pagination = ref({ page: 1, pageSize: 10, total: 0 });

// Detail drawer
const detailVisible = ref(false);
const detailData = ref<Api.Ess.Contract | null>(null);

// Table columns
const columns: DataTableColumns<Api.Ess.Contract> = [
  { type: 'selection' as const, width: 50 },
  { title: '合同编号', key: 'contractNo', width: 180 },
  {
    title: '合同名称',
    key: 'contractName',
    width: 280,
    render: row => h('div', { style: 'display: flex; align-items: center; gap: 8px' }, [
      h('span', {}, row.contractName || '-'),
      h(NButton, { size: 'tiny', type: 'primary', onClick: () => handlePreview(row) }, () => '查看原文')
    ])
  },
  { title: '关联订单', key: 'orderNo', width: 160, render: row => row.orderNo ? h(NButton, { size: 'tiny', text: true, type: 'primary', onClick: () => routerPushByKeyWithMetaQuery('business_order', { orderNo: row.orderNo }) }, () => row.orderNo) : '-' },
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
    width: 220,
    fixed: 'right',
    render: row => {
      const buttons: any[] = [];
      buttons.push(h(NButton, { size: 'small', onClick: () => showDetail(row) }, () => '详情'));
      buttons.push(h(NButton, { size: 'small', onClick: () => handlePreview(row) }, () => '查看'));
      if (row.status === 'SIGNED' || row.status === 'COMPLETED') {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleDownload(row) }, () => '下载'));
        buttons.push(h(NButton, { size: 'small', onClick: () => handlePrint(row) }, () => '打印'));
      }
      if (isAdmin) {
        buttons.push(h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, () => '删除'));
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
    const url = typeof data === 'string' ? data : (data as any)?.downloadUrl;
    if (url) {
      window.open(url, '_blank');
    }
  } catch (e) {
    message.error('下载合同失败');
  }
}

// 预览合同（新窗口打开PDF）
async function handlePreview(row: Api.Ess.Contract) {
  try {
    const { data } = await fetchDownloadContract(row.contractId);
    // API直接返回URL字符串，非对象
    const url = typeof data === 'string' ? data : (data as any)?.downloadUrl;
    if (url) {
      window.open(url, '_blank');
    } else {
      message.warning('合同文件暂不可用');
    }
  } catch {
    message.error('获取合同预览链接失败');
  }
}

// 打印合同
async function handlePrint(row: Api.Ess.Contract) {
  try {
    const { data } = await fetchDownloadContract(row.contractId);
    const url = typeof data === 'string' ? data : (data as any)?.downloadUrl;
    if (url) {
      const printWindow = window.open(url, '_blank');
      if (printWindow) {
        printWindow.onload = () => {
          printWindow.print();
        };
      }
    }
  } catch {
    message.error('打印失败');
  }
}

// 批量下载
const batchDownloading = ref(false);
// 删除合同（仅管理员）
async function handleDelete(row: Api.Ess.Contract) {
  try {
    await fetchDeleteContract(row.contractId);
    message.success('合同已删除');
    await getData();
  } catch (e: any) {
    message.error(e?.message || '删除失败');
  }
}

async function handleBatchDownload() {
  if (checkedRowKeys.value.length === 0) {
    message.warning('请先选择要下载的合同');
    return;
  }
  batchDownloading.value = true;
  let successCount = 0;
  for (const contractId of checkedRowKeys.value) {
    try {
      const { data } = await fetchDownloadContract(contractId);
      if (data?.downloadUrl) {
        window.open(data.downloadUrl, '_blank');
        successCount++;
      }
    } catch {
      // skip failed
    }
  }
  batchDownloading.value = false;
  message.success(`已打开 ${successCount}/${checkedRowKeys.value.length} 个合同下载链接`);
  checkedRowKeys.value = [];
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

      <!-- Batch operations -->
      <div v-if="checkedRowKeys.length > 0" style="margin-bottom: 12px">
        <NSpace>
          <span style="color: #666">已选择 {{ checkedRowKeys.length }} 个合同</span>
          <NButton type="primary" size="small" :loading="batchDownloading" @click="handleBatchDownload">批量下载</NButton>
        </NSpace>
      </div>

      <!-- Table -->
      <NDataTable
        v-model:checked-row-keys="checkedRowKeys"
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1400"
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