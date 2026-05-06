<script setup lang="ts">
import { ref, h, onMounted } from 'vue';
import { NButton, NCard, NTag, NSpace, NInput, NSelect, useMessage, NPopconfirm } from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import { useFormRules } from '@/hooks/common/form';
import {
  fetchGetProviderList,
  fetchCreateProvider,
  fetchUpdateProvider,
  fetchDeleteProvider,
  fetchGetProviderStatistics,
  fetchBatchDeleteProvider
} from '@/service/api';
import { useNaivePaginatedTable, useTableOperate, defaultTransform } from '@/hooks/common/table';
import { useNaiveForm } from '@/hooks/common/form';
import { useAuth } from '@/hooks/business/auth';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';
import ProviderDetail from './components/ProviderDetail.vue';

defineOptions({
  name: 'BusinessProvider'
});

const message = useMessage();
const { patternRules, createRequiredRule } = useFormRules();
const { formRef, validate, restoreValidation } = useNaiveForm();
const { hasAuth } = useAuth();

// Form validation rules
const rules = {
  providerName: [createRequiredRule('请输入服务商名称')],
  creditCode: [createRequiredRule('请输入统一社会信用代码')],
  providerType: [createRequiredRule('请选择服务商类型')],
  legalPerson: [createRequiredRule('请输入法人姓名')],
  contactPhone: [
    createRequiredRule('请输入联系电话'),
    { pattern: patternRules.phone.pattern, message: patternRules.phone.message, trigger: 'change' }
  ]
};

// Statistics
const statistics = ref<Api.Provider.Statistics>({
  totalProviders: 0,
  enabledProviders: 0,
  disabledProviders: 0
});

// Detail drawer state
const detailVisible = ref(false);
const detailProviderId = ref<string | null>(null);

// Search
const searchName = ref('');
const searchCreditCode = ref('');
const searchServiceCategory = ref('');
const searchStatus = ref('');

// Service category options
const categoryOptions = [
  { label: '养老服务', value: 'ELDER_CARE' },
  { label: '家政服务', value: 'HOME_CARE' }
];

// Status options (与后端数据库一致: ENABLED=启用, DISABLED=禁用)
const statusOptions = [
  { label: '正常', value: 'ENABLED' },
  { label: '禁用', value: 'DISABLED' }
];

// Provider type options
const providerTypeOptions = [
  { label: '家政服务', value: 'HOME_CARE' },
  { label: '养老服务', value: 'ELDER_CARE' },
  { label: '网络科技服务', value: 'TECH_SERVICE' }
];

function getCategoryLabel(category?: string): string {
  const option = categoryOptions.find(o => o.value === category);
  return option?.label || category || '';
}

// Table columns
const columns: DataTableColumns<Api.Provider.Provider> = [
  { type: 'selection' },
  { title: '服务商名称', key: 'providerName', width: 200 },
  { title: '信用代码', key: 'creditCode', width: 180 },
  { title: '服务类别', key: 'serviceCategory', width: 100, render: row => getCategoryLabel(row.serviceCategory) },
  { title: '法人', key: 'legalPerson', width: 100 },
  { title: '联系电话', key: 'contactPhone', width: 130 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: row =>
      h(NTag, { type: row.status === 'ENABLED' ? 'success' : 'error', size: 'small' }, () =>
        row.status === 'ENABLED' ? '正常' : '禁用'
      )
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: row => {
      const buttons = [];
      // 详情按钮
      buttons.push(
        h(NButton, { size: 'small', type: 'info', onClick: () => showDetail(row.providerId), style: { marginRight: '8px' } }, () => '详情')
      );
      if (hasAuth('provider:list:edit')) {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleOpenEdit(row.providerId), style: { marginRight: '8px' } }, () => '编辑'));
      }
      if (hasAuth('provider:list:delete')) {
        buttons.push(
          h(
            NPopconfirm,
            {
              onPositiveClick: () => handleDelete(row.providerId)
            },
            {
              trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
              default: () => '确认删除?'
            }
          )
        );
      }
      return h(NSpace, { size: 'small' }, () => buttons);
    }
  }
];

// Use framework's table hook
const {
  data: tableData,
  loading,
  pagination,
  mobilePagination,
  getData,
  getDataByPage,
  columnChecks
} = useNaivePaginatedTable<Api.Common.PaginatingQueryRecord<Api.Provider.Provider>, Api.Provider.Provider>({
  apiFn: async params => {
    const queryParams: any = {
      page: params.page,
      pageSize: params.pageSize
    };
    if (searchName.value) queryParams.providerName = searchName.value;
    if (searchCreditCode.value) queryParams.creditCode = searchCreditCode.value;
    if (searchServiceCategory.value) queryParams.serviceCategory = searchServiceCategory.value;
    if (searchStatus.value) queryParams.status = searchStatus.value;
    return fetchGetProviderList(queryParams as Api.Provider.ProviderQuery);
  },
  apiParams: {
    page: 1,
    pageSize: 10
  },
  transform: defaultTransform,
  columns: () => columns
});

// Use framework's table operate hook
const {
  drawerVisible,
  operateType,
  handleAdd,
  editingData,
  handleEdit,
  checkedRowKeys,
  onBatchDeleted,
  onDeleted,
  closeDrawer
} = useTableOperate(tableData, 'providerId', getData);

// Form data
const form = ref({
  providerName: '',
  creditCode: '',
  providerType: '',
  serviceCategory: 'HOME_CARE',
  legalPerson: '',
  contactPhone: '',
  address: '',
  serviceAreas: '',
  description: '',
  status: 'ENABLED'
});

async function getStatistics() {
  try {
    const { data } = await fetchGetProviderStatistics();
    if (data) {
      statistics.value = data;
    }
  } catch (e) {
    console.error('Failed to get statistics', e);
  }
}

function resetForm() {
  form.value = {
    providerName: '',
    creditCode: '',
    providerType: '',
    serviceCategory: 'HOME_CARE',
    legalPerson: '',
    contactPhone: '',
    address: '',
    serviceAreas: '',
    description: '',
    status: 'ENABLED'
  };
}

function showDetail(providerId: string) {
  detailProviderId.value = providerId;
  detailVisible.value = true;
}

function handleOpenAdd() {
  resetForm();
  handleAdd();
}

async function handleOpenEdit(id: string) {
  const row = tableData.value.find(item => String(item.providerId) === String(id));
  if (row) {
    form.value = {
      providerName: row.providerName,
      creditCode: row.creditCode || '',
      providerType: row.providerType || '',
      serviceCategory: row.serviceCategory || 'HOME_CARE',
      legalPerson: row.legalPerson || '',
      contactPhone: row.contactPhone || '',
      address: row.address || '',
      serviceAreas: row.serviceAreas || '',
      description: row.description || '',
      status: row.status
    };
    handleEdit(id);
  }
}

async function handleDelete(providerId: string) {
  try {
    await fetchDeleteProvider(providerId);
    message.success('删除成功');
    await onDeleted();
    await getStatistics();
  } catch (e) {
    console.error('Failed to delete', e);
  }
}

async function handleBatchDelete() {
  if (!checkedRowKeys.value.length) return;
  try {
    await fetchBatchDeleteProvider(checkedRowKeys.value);
    message.success('批量删除成功');
    await onBatchDeleted();
    await getStatistics();
  } catch (e) {
    console.error('Failed to batch delete', e);
  }
}

async function handleSubmit() {
  try {
    await validate();
  } catch {
    return;
  }
  try {
    if (operateType.value === 'add') {
      await fetchCreateProvider(form.value);
      message.success('添加成功');
    } else if (editingData.value) {
      await fetchUpdateProvider(editingData.value.providerId, form.value);
      message.success('修改成功');
    }
    closeDrawer();
    await getData();
    await getStatistics();
    restoreValidation();
  } catch (e: any) {
    message.error(e?.message || '操作失败，请检查必填项');
  }
}

function handleResetSearch() {
  searchName.value = '';
  searchCreditCode.value = '';
  searchServiceCategory.value = '';
  searchStatus.value = '';
  getDataByPage(1);
}

onMounted(() => {
  getStatistics();
  getData();
});
</script>

<template>
  <div>
    <!-- Statistics Cards -->
    <NCard title="服务商统计" :bordered="false" style="margin-bottom: 16px">
      <div class="statistics-grid">
        <div class="stat-card stat-primary">
          <div class="stat-label">总数</div>
          <div class="stat-value">{{ statistics.totalProviders }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">启用</div>
          <div class="stat-value">{{ statistics.enabledProviders }}</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-label">禁用</div>
          <div class="stat-value">{{ statistics.disabledProviders }}</div>
        </div>
      </div>
    </NCard>

    <!-- Table -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>服务商管理</span>
        </div>
      </template>
      <div style="background: #f5f5f5; padding: 12px; margin-bottom: 12px; border-radius: 4px">
        <NSpace :wrap="true" align="center">
          <NInput v-model:value="searchName" placeholder="服务商名称" clearable style="width: 150px" />
          <NInput v-model:value="searchCreditCode" placeholder="信用代码" clearable style="width: 180px" />
          <NSelect
            v-model:value="searchServiceCategory"
            :options="categoryOptions"
            placeholder="服务类别"
            clearable
            style="width: 120px"
          />
          <NSelect
            v-model:value="searchStatus"
            :options="statusOptions"
            placeholder="状态"
            clearable
            style="width: 100px"
          />
          <NButton type="primary" @click="getData">搜索</NButton>
          <NButton @click="handleResetSearch">重置</NButton>
        </NSpace>
      </div>

      <!-- Use framework's TableHeaderOperation component -->
      <TableHeaderOperation
        v-model:columns="columnChecks"
        :disabled-delete="checkedRowKeys.length === 0"
        :loading="loading"
        @refresh="getData"
        @delete="handleBatchDelete"
      >
        <template #default>
          <NButton v-if="hasAuth('provider:list:add')" size="small" ghost type="primary" @click="handleOpenAdd">
            <template #icon>
              <icon-ic-round-plus class="text-icon" />
            </template>
            {{ $t('common.add') }}
          </NButton>
        </template>
      </TableHeaderOperation>

      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1400"
        :row-key="(row: Api.Provider.Provider) => row.providerId"
        v-model:checked-row-keys="checkedRowKeys"
        remote
        :pagination="mobilePagination"
      />
    </NCard>

    <!-- Provider Drawer -->
    <NDrawer v-model:show="drawerVisible" :width="500" placement="right" closable>
      <NDrawerContent :title="operateType === 'add' ? '新增服务商' : '编辑服务商'" closable>
        <NForm ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100">
          <NFormItem label="服务商名称" path="providerName">
            <NInput v-model:value="form.providerName" placeholder="请输入服务商名称" />
          </NFormItem>
          <NFormItem label="信用代码" path="creditCode">
            <NInput v-model:value="form.creditCode" placeholder="请输入统一社会信用代码" />
          </NFormItem>
          <NFormItem label="服务类别">
            <NSelect
              v-model:value="form.serviceCategory"
              :options="categoryOptions"
              placeholder="请选择服务类别"
              style="width: 200px"
            />
          </NFormItem>
          <NFormItem label="服务商类型" path="providerType">
            <NSelect
              v-model:value="form.providerType"
              :options="providerTypeOptions"
              placeholder="请选择服务商类型"
              style="width: 200px"
            />
          </NFormItem>
          <NFormItem label="法人" path="legalPerson">
            <NInput v-model:value="form.legalPerson" placeholder="请输入法人姓名" />
          </NFormItem>
          <NFormItem label="联系电话" path="contactPhone">
            <NInput v-model:value="form.contactPhone" placeholder="请输入联系电话" />
          </NFormItem>
          <NFormItem label="地址">
            <NInput v-model:value="form.address" placeholder="请输入地址" />
          </NFormItem>
          <NFormItem label="服务区域">
            <NInput v-model:value="form.serviceAreas" placeholder="请输入服务区域" />
          </NFormItem>
          <NFormItem label="状态">
            <NSelect v-model:value="form.status" :options="statusOptions" style="width: 120px" />
          </NFormItem>
          <NFormItem label="简介">
            <NInput v-model:value="form.description" type="textarea" placeholder="请输入简介" />
          </NFormItem>
        </NForm>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="closeDrawer">取消</NButton>
            <NButton type="primary" @click="handleSubmit">确认</NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- Provider Detail Drawer -->
    <ProviderDetail
      v-model:visible="detailVisible"
      :provider-id="detailProviderId"
    />
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
