<script setup lang="ts">
import { ref, h, onMounted, watch } from 'vue';
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
  NSplit,
  NImage,
  NImageGroup,
  useMessage,
  NPopconfirm,
  NDrawer,
  NDrawerContent
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import {
  fetchGetQualityCheckList,
  fetchGetQualityStatistics,
  fetchGetStaff,
  fetchGetQualityCheck,
  fetchSubmitRectify,
  fetchRecheck
} from '@/service/api';
import { useNaivePaginatedTable, defaultTransform } from '@/hooks/common/table';
import { useAuth } from '@/hooks/business/auth';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';

defineOptions({
  name: 'BusinessQuality'
});

const message = useMessage();

// Statistics
const { hasAuth } = useAuth();
const statistics = ref<Api.Quality.Statistics>({
  total: 0,
  qualifiedCount: 0,
  unqualifiedCount: 0,
  needRectifyCount: 0,
  qualifiedRate: 0,
  avgScore: 0
});

// Search
const searchOrderNo = ref('');
const searchProviderName = ref('');
const searchStaffName = ref('');
const searchCheckResult = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Staff detail drawer
const staffDetailDrawerVisible = ref(false);
const staffDetailData = ref<Api.Staff.Staff | null>(null);

async function showStaffDetail(row: Api.Quality.QualityCheck) {
  if (!row.staffId) return;
  try {
    const { data } = await fetchGetStaff(row.staffId);
    if (data) {
      staffDetailData.value = data;
      staffDetailDrawerVisible.value = true;
    }
  } catch (e) {
    console.error('Failed to get staff detail', e);
  }
}

// Quality check detail drawer
const qualityDetailDrawerVisible = ref(false);
const qualityDetailData = ref<Api.Quality.QualityCheck | null>(null);

async function showQualityDetail(row: Api.Quality.QualityCheck) {
  try {
    const { data, error } = await fetchGetQualityCheck(row.qualityCheckId);
    if (error) {
      message.error(error.message || '获取质检详情失败');
      return;
    }
    if (data) {
      qualityDetailData.value = data;
      qualityDetailDrawerVisible.value = true;
    }
  } catch (e) {
    console.error('Failed to get quality check detail', e);
  }
}

// Rectify submit drawer
const rectifyDrawerVisible = ref(false);
const rectifyData = ref({
  photos: [] as string[],
  remark: ''
});
const rectifyLoading = ref(false);

function showRectifyModal(row: Api.Quality.QualityCheck) {
  qualityDetailData.value = row;
  rectifyData.value = { photos: [], remark: '' };
  rectifyDrawerVisible.value = true;
}

async function handleSubmitRectify() {
  if (!qualityDetailData.value) return;
  rectifyLoading.value = true;
  try {
    const { error } = await fetchSubmitRectify(qualityDetailData.value.qualityCheckId, {
      photos: rectifyData.value.photos,
      remark: rectifyData.value.remark
    });
    if (error) {
      message.error(error.message || '整改提交失败');
      return;
    }
    message.success('整改提交成功');
    rectifyDrawerVisible.value = false;
    getData();
    getStatistics();
  } finally {
    rectifyLoading.value = false;
  }
}

// Recheck drawer
const recheckDrawerVisible = ref(false);
const recheckData = ref({
  result: 'PASSED',
  remark: ''
});
const recheckLoading = ref(false);

function showRecheckModal(row: Api.Quality.QualityCheck) {
  qualityDetailData.value = row;
  recheckData.value = { result: 'PASSED', remark: '' };
  recheckDrawerVisible.value = true;
}

async function handleRecheck() {
  if (!qualityDetailData.value) return;
  recheckLoading.value = true;
  try {
    const { error } = await fetchRecheck(qualityDetailData.value.qualityCheckId, {
      result: recheckData.value.result,
      remark: recheckData.value.remark
    });
    if (error) {
      message.error(error.message || '复检失败');
      return;
    }
    message.success('复检成功');
    recheckDrawerVisible.value = false;
    getData();
    getStatistics();
  } finally {
    recheckLoading.value = false;
  }
}

// Check result options
const resultOptions = [
  { label: '合格', value: 'QUALIFIED' },
  { label: '不合格', value: 'UNQUALIFIED' },
  { label: '需整改', value: 'NEED_RECTIFY' }
];

// Check type options
const checkTypeOptions = [
  { label: '随机抽检', value: 'RANDOM' },
  { label: '计划抽检', value: 'SCHEDULED' },
  { label: '投诉抽检', value: 'COMPLAINT' },
  { label: '完工抽检', value: 'COMPLETION' }
];

// Check method options
const checkMethodOptions = [
  { label: '照片审核', value: 'PHOTO_REVIEW' },
  { label: '电话回访', value: 'PHONE_REVIEW' },
  { label: '现场检查', value: 'ON_SITE' }
];

function getResultType(result: Api.Quality.CheckResult): 'success' | 'error' | 'warning' | 'default' {
  const map: Record<string, 'success' | 'error' | 'warning' | 'default'> = {
    QUALIFIED: 'success',
    UNQUALIFIED: 'error',
    NEED_RECTIFY: 'warning'
  };
  return map[result] || 'default';
}

function getResultLabel(result: string): string {
  const option = resultOptions.find(o => o.value === result);
  return option?.label || result;
}

function getCheckTypeLabel(checkType: string): string {
  const option = checkTypeOptions.find(o => o.value === checkType);
  return option?.label || checkType;
}

function getCheckMethodLabel(checkMethod: string): string {
  const option = checkMethodOptions.find(o => o.value === checkMethod);
  return option?.label || checkMethod;
}

const columns: DataTableColumns<Api.Quality.QualityCheck> = [
  { title: '质检编号', key: 'checkNo', width: 160 },
  { title: '订单号', key: 'orderNo', width: 160 },
  { title: '服务商', key: 'providerName', width: 150 },
  {
    title: '服务人员',
    key: 'staffName',
    width: 100,
    render: row =>
      row.staffName
        ? h('a', { style: { color: '#18a058', cursor: 'pointer' }, onClick: () => showStaffDetail(row) }, row.staffName)
        : '-'
  },
  { title: '质检类型', key: 'checkType', width: 100, render: row => getCheckTypeLabel(row.checkType) },
  { title: '质检方式', key: 'checkMethod', width: 100, render: row => getCheckMethodLabel(row.checkMethod) },
  { title: '综合评分', key: 'checkScore', width: 100 },
  {
    title: '质检结果',
    key: 'checkResult',
    width: 100,
    render: row =>
      h(NTag, { type: getResultType(row.checkResult), size: 'small' }, () => getResultLabel(row.checkResult))
  },
  {
    title: '整改状态',
    key: 'rectifyStatus',
    width: 100,
    render: row => {
      const statusMap: Record<string, string> = {
        PENDING: '待整改',
        IN_PROGRESS: '整改中',
        RECHECK: '待复检',
        COMPLETED: '已完成',
        VERIFIED: '已通过',
        FAILED: '未通过'
      };
      return statusMap[row.rectifyStatus || ''] || '-';
    }
  },
  { title: '质检时间', key: 'checkTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: row => {
      const actions = [];
      actions.push(
        h(
          NButton,
          { size: 'small', type: 'primary', onClick: () => showQualityDetail(row), style: { marginRight: '8px' } },
          () => '查看'
        )
      );
      // 如果需要整改且未提交整改，显示整改按钮
      if (row.checkResult === 'NEED_RECTIFY' && row.rectifyStatus === 'PENDING') {
        actions.push(
          h(
            NButton,
            { size: 'small', type: 'warning', onClick: () => showRectifyModal(row), style: { marginRight: '8px' } },
            () => '整改'
          )
        );
      }
      // 如果整改状态为待复检，显示复检按钮
      if (row.rectifyStatus === 'RECHECK') {
        actions.push(
          h(
            NButton,
            { size: 'small', type: 'success', onClick: () => showRecheckModal(row), style: { marginRight: '8px' } },
            () => '复检'
          )
        );
      }
      return h(NSpace, null, () => actions);
    }
  }
];

async function getStatistics() {
  try {
    const { data } = await fetchGetQualityStatistics();
    if (data) {
      statistics.value = data;
    }
  } catch (e) {
    console.error('Failed to get statistics', e);
  }
}

// Use framework's table hook
const tableHookResult = useNaivePaginatedTable<Api.Common.PaginatingQueryRecord<Api.Quality.QualityCheck>, Api.Quality.QualityCheck>({
  apiFn: async params => {
    const queryParams: any = {
      current: params.page,
      pageSize: params.pageSize
    };
    if (searchOrderNo.value) queryParams.orderNo = searchOrderNo.value;
    if (searchProviderName.value) queryParams.providerName = searchProviderName.value;
    if (searchStaffName.value) queryParams.staffName = searchStaffName.value;
    if (searchCheckResult.value) queryParams.checkResult = searchCheckResult.value;
    if (searchDateRange.value) {
      queryParams.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      queryParams.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }
    return fetchGetQualityCheckList(queryParams);
  },
  apiParams: {
    page: 1,
    pageSize: 10
  },
  transform: defaultTransform,
  columns: () => columns
});

const {
  data: tableData,
  loading,
  pagination,
  mobilePagination,
  getData,
  getDataByPage,
  columnChecks: rawColumnChecks
} = tableHookResult;

// Ensure columnChecks is always an array (writable ref for v-model)
const columnChecks = ref<Array<{ prop: string; label: string; checked: boolean }>>([]);

// Watch rawColumnChecks and sync to columnChecks
watch(
  () => rawColumnChecks.value,
  val => {
    if (val && val.length > 0) {
      columnChecks.value = val;
    }
  },
  { immediate: true, deep: true }
);

function handleResetSearch() {
  searchOrderNo.value = '';
  searchProviderName.value = '';
  searchStaffName.value = '';
  searchCheckResult.value = '';
  searchDateRange.value = null;
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
    <NCard title="质检统计" :bordered="false" style="margin-bottom: 16px">
      <div class="statistics-grid">
        <div class="stat-card stat-primary">
          <div class="stat-label">总质检数</div>
          <div class="stat-value">{{ statistics.total }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">合格数</div>
          <div class="stat-value">{{ statistics.qualifiedCount }}</div>
        </div>
        <div class="stat-card stat-error">
          <div class="stat-label">不合格数</div>
          <div class="stat-value">{{ statistics.unqualifiedCount }}</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-label">需整改数</div>
          <div class="stat-value">{{ statistics.needRectifyCount }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">合格率</div>
          <div class="stat-value">{{ statistics.qualifiedRate }}%</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">平均评分</div>
          <div class="stat-value">{{ Number(statistics.avgScore || 0).toFixed(1) }}</div>
        </div>
      </div>
    </NCard>

    <!-- Table -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>质检管理</span>
        </div>
      </template>
      <div style="background: #f5f5f5; padding: 12px; margin-bottom: 12px; border-radius: 4px">
        <NSpace :wrap="true" align="center">
          <NInput v-model:value="searchOrderNo" placeholder="订单号" clearable style="width: 150px" />
          <NInput v-model:value="searchProviderName" placeholder="服务商名称" clearable style="width: 150px" />
          <NInput v-model:value="searchStaffName" placeholder="服务人员" clearable style="width: 100px" />
          <NSelect
            v-model:value="searchCheckResult"
            :options="resultOptions"
            placeholder="质检结果"
            clearable
            style="width: 120px"
          />
          <NDatePicker v-model:value="searchDateRange" type="daterange" clearable style="width: 260px" />
          <NButton type="primary" @click="getData">搜索</NButton>
          <NButton @click="handleResetSearch">重置</NButton>
        </NSpace>
      </div>

      <TableHeaderOperation
        v-model:columns="columnChecks"
        :loading="loading"
        @refresh="getData"
      />

      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1400"
        :row-key="(row: Api.Quality.QualityCheck) => row.qualityCheckId"
        remote
        :pagination="mobilePagination"
      />
    </NCard>

    <!-- Staff Detail Drawer -->
    <NDrawer v-model:show="staffDetailDrawerVisible" :width="500" placement="right" closable>
      <NDrawerContent title="服务人员详情" closable>
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
      </NDrawerContent>
    </NDrawer>

    <!-- Quality Check Detail Drawer -->
    <NDrawer v-model:show="qualityDetailDrawerVisible" :width="560" placement="right" closable>
      <NDrawerContent title="质检详情" closable>
        <NForm v-if="qualityDetailData" label-placement="left" label-width="100">
          <NFormItem label="质检编号">{{ qualityDetailData.checkNo }}</NFormItem>
          <NFormItem label="订单号">{{ qualityDetailData.orderNo || '-' }}</NFormItem>
          <NFormItem label="服务商">{{ qualityDetailData.providerName || '-' }}</NFormItem>
          <NFormItem label="服务人员">{{ qualityDetailData.staffName || '-' }}</NFormItem>
          <NFormItem label="服务类别">{{ qualityDetailData.serviceCategory || '-' }}</NFormItem>
          <NFormItem label="质检类型">{{ getCheckTypeLabel(qualityDetailData.checkType) }}</NFormItem>
          <NFormItem label="质检方式">{{ getCheckMethodLabel(qualityDetailData.checkMethod) }}</NFormItem>
          <NFormItem label="综合评分">{{ qualityDetailData.checkScore }}分</NFormItem>
          <NFormItem label="质检结果">
            <NTag :type="getResultType(qualityDetailData.checkResult)" size="small">
              {{ getResultLabel(qualityDetailData.checkResult) }}
            </NTag>
          </NFormItem>
          <NFormItem label="质检时间">{{ qualityDetailData.checkTime || '-' }}</NFormItem>
          <NFormItem label="质检备注">{{ qualityDetailData.checkRemark || '-' }}</NFormItem>
          <NFormItem label="质检照片" v-if="qualityDetailData.checkPhotos && qualityDetailData.checkPhotos.length">
            <NImageGroup>
              <NSpace>
                <template v-for="photo in qualityDetailData.checkPhotos" :key="photo">
                  <NImage :src="photo" width="80" height="80" object-fit="cover" />
                </template>
              </NSpace>
            </NImageGroup>
          </NFormItem>
          <NFormItem label="整改通知" v-if="qualityDetailData.rectifyNotice">
            {{ qualityDetailData.rectifyNotice }}
          </NFormItem>
          <NFormItem label="整改期限" v-if="qualityDetailData.rectifyDeadline">
            {{ qualityDetailData.rectifyDeadline }}
          </NFormItem>
          <NFormItem label="整改状态">
            {{ { PENDING: '待整改', IN_PROGRESS: '整改中', RECHECK: '待复检', COMPLETED: '已完成', VERIFIED: '已通过', FAILED: '未通过' }[qualityDetailData.rectifyStatus || ''] || '-' }}
          </NFormItem>
          <NFormItem label="整改照片" v-if="qualityDetailData.rectifyPhotos">
            <NImageGroup>
              <NSpace>
                <template v-for="photo in (Array.isArray(qualityDetailData.rectifyPhotos) ? qualityDetailData.rectifyPhotos : (qualityDetailData.rectifyPhotos as string).split(','))" :key="photo">
                  <NImage :src="photo" width="80" height="80" object-fit="cover" />
                </template>
              </NSpace>
            </NImageGroup>
          </NFormItem>
          <NFormItem label="整改说明" v-if="qualityDetailData.rectifyRemark">
            {{ qualityDetailData.rectifyRemark }}
          </NFormItem>
          <NFormItem label="复检结果" v-if="qualityDetailData.recheckResult">
            {{ qualityDetailData.recheckResult === 'PASSED' ? '通过' : '不通过' }}
          </NFormItem>
          <NFormItem label="复检时间" v-if="qualityDetailData.recheckTime">
            {{ qualityDetailData.recheckTime }}
          </NFormItem>
          <NFormItem label="创建时间">{{ qualityDetailData.createTime }}</NFormItem>
        </NForm>
      </NDrawerContent>
    </NDrawer>

    <!-- Rectify Submit Drawer -->
    <NDrawer v-model:show="rectifyDrawerVisible" :width="480" placement="right" closable>
      <NDrawerContent title="提交整改" closable>
        <NForm label-placement="left" label-width="80">
          <NFormItem label="整改说明">
            <NInput
              v-model:value="rectifyData.remark"
              type="textarea"
              placeholder="请输入整改说明"
              :rows="3"
            />
          </NFormItem>
          <NFormItem label="整改照片">
            <div style="color: #999; font-size: 12px">照片上传功能开发中</div>
          </NFormItem>
        </NForm>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="rectifyDrawerVisible = false">取消</NButton>
            <NButton type="primary" :loading="rectifyLoading" @click="handleSubmitRectify">
              提交
            </NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- Recheck Drawer -->
    <NDrawer v-model:show="recheckDrawerVisible" :width="480" placement="right" closable>
      <NDrawerContent title="复检" closable>
        <NForm label-placement="left" label-width="80">
          <NFormItem label="复检结果">
            <NSelect
              v-model:value="recheckData.result"
              :options="[
                { label: '通过', value: 'PASSED' },
                { label: '不通过', value: 'FAILED' }
              ]"
              style="width: 200px"
            />
          </NFormItem>
          <NFormItem label="复检备注">
            <NInput
              v-model:value="recheckData.remark"
              type="textarea"
              placeholder="请输入复检备注"
              :rows="3"
            />
          </NFormItem>
        </NForm>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="recheckDrawerVisible = false">取消</NButton>
            <NButton type="primary" :loading="recheckLoading" @click="handleRecheck">
              提交
            </NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>
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
