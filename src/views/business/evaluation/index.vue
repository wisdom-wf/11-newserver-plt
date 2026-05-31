<script setup lang="ts">
import { ref, h, onMounted, watch, nextTick, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
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
  NStatistic,
  useMessage,
  NImage,
  NImageGroup,
  NDrawer,
  NDrawerContent,
  NInputNumber,
  NRate,
  NText,
  NSpin
} from 'naive-ui';
import type { DataTableColumns } from 'naive-ui';
import {
  fetchGetEvaluationList,
  fetchGetEvaluationStatistics,
  fetchGetElder,
  fetchGetStaff,
  fetchGetEvaluation,
  fetchReplyEvaluation,
  fetchCreateEvaluation,
  fetchGetOrderList,
  fetchGetQualityCheckByOrderId,
  fetchGenerateEvaluationLink,
  fetchInvalidateInvite,
  fetchBatchDeleteEvaluation
} from '@/service/api';
import { useNaivePaginatedTable, defaultTransform } from '@/hooks/common/table';
import { useAuth } from '@/hooks/business/auth';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';
import FlowIndicator from '@/components/business/FlowIndicator.vue';
import { formatScore } from '@/utils/formatter';

defineOptions({
  name: 'BusinessEvaluation'
});

const message = useMessage();
const router = useRouter();
const route = useRoute();

// Create dialog state
const createDialogVisible = ref(false);
const createLoading = ref(false);
const createOrderNo = ref('');
const createForm = ref<Api.Evaluation.EvaluationForm>({
  orderId: '',
  serviceScore: 5,
  attitudeScore: 5,
  skillScore: 5,
  punctualityScore: 5,
  environmentScore: 5,
  overallScore: 5,
  content: '',
  images: []
});

// Invite dialog state
const inviteDialogVisible = ref(false);
const inviteLoading = ref(false);
const inviteForm = ref({
  orderId: '',
  elderId: '',
  elderName: '',
  expireHours: 72
});
const inviteResult = ref<Api.Evaluation.EvaluationInvite | null>(null);
const generatedLink = ref('');

// Satisfaction level options
const satisfactionOptions = [
  { label: '非常满意', value: 'VERY_SATISFIED' },
  { label: '满意', value: 'SATISFIED' },
  { label: '一般', value: 'NEUTRAL' },
  { label: '不满意', value: 'DISSATISFIED' },
  { label: '非常不满意', value: 'VERY_DISSATISFIED' }
];

// 计算当前流程步骤
const currentFlowStep = computed(() => {
  // 如果有已评价的，进入已完成评价阶段
  if (statistics.value.totalCount > 0) {
    return 'evaluated';
  }
  // 默认是服务完成阶段
  return 'service_completed';
});

// 流程步骤配置
const flowSteps = [
  { key: 'service_started', label: '服务开始' },
  { key: 'log_submitted', label: '日志提交' },
  { key: 'quality_check', label: '质检审核' },
  { key: 'service_completed', label: '服务完成' },
  { key: 'evaluated', label: '已完成评价' }
];

function openCreateDialog() {
  createOrderNo.value = '';
  createForm.value = {
    orderId: '',
    serviceScore: 5,
    attitudeScore: 5,
    skillScore: 5,
    punctualityScore: 5,
    environmentScore: 5,
    overallScore: 5,
    content: '',
    images: []
  };
  createDialogVisible.value = true;
}

async function resolveCreateOrderId() {
  const orderNo = createOrderNo.value.trim();
  if (!orderNo) {
    message.warning('请输入订单编号');
    return false;
  }
  const { data, error } = await fetchGetOrderList({ orderNo, page: 1, pageSize: 10 } as any);
  if (error) {
    message.error(error.message || '定位订单失败');
    return false;
  }
  const order = data?.records?.find(item => item.orderNo === orderNo);
  if (!order) {
    message.warning(`未找到订单：${orderNo}`);
    return false;
  }
  createForm.value.orderId = order.orderId;
  return true;
}

async function handleCreateSubmit() {
  createLoading.value = true;
  try {
    if (!(await resolveCreateOrderId())) return;
    const { error } = await fetchCreateEvaluation(createForm.value);
    if (error) {
      message.error(error.message || '创建评价失败');
      return;
    }
    message.success('创建评价成功');
    createDialogVisible.value = false;
    getData();
    getStatistics();
  } finally {
    createLoading.value = false;
  }
}

// Open invite dialog
function openInviteDialog(row: any) {
  inviteForm.value = {
    orderId: row.orderId,
    elderId: row.elderId,
    elderName: row.elderName,
    expireHours: 72
  };
  inviteResult.value = null;
  generatedLink.value = '';
  inviteDialogVisible.value = true;
}

// Generate evaluation link
async function handleGenerateLink() {
  if (!inviteForm.value.orderId) {
    message.warning('请输入订单ID');
    return;
  }
  if (!inviteForm.value.elderId) {
    message.warning('请输入老人ID');
    return;
  }
  if (!inviteForm.value.elderName) {
    message.warning('请输入老人姓名');
    return;
  }
  inviteLoading.value = true;
  try {
    const { data, error } = await fetchGenerateEvaluationLink({
      orderId: inviteForm.value.orderId,
      elderId: inviteForm.value.elderId,
      elderName: inviteForm.value.elderName,
      expireHours: inviteForm.value.expireHours
    });
    if (error) {
      message.error(error.message || '生成邀请链接失败');
      return;
    }
    if (data) {
      inviteResult.value = data;
      generatedLink.value = data.surveyUrl || window.location.origin + '/public/survey?token=' + data.token;
      message.success('生成成功');
    }
  } finally {
    inviteLoading.value = false;
  }
}

// Copy link to clipboard
function copyLink() {
  if (generatedLink.value) {
    navigator.clipboard.writeText(generatedLink.value);
    message.success('链接已复制到剪贴板');
  }
}

// Statistics
const { hasAuth } = useAuth();
const statistics = ref<Api.Evaluation.Statistics>({
  totalCount: 0,
  averageRating: null,
  fiveStarCount: 0,
  fourStarCount: 0,
  threeStarCount: 0,
  twoStarCount: 0,
  oneStarCount: 0
});

// Search
const searchOrderNo = ref('');
const searchElderName = ref('');
const searchProviderName = ref('');
const searchDateRange = ref<[number, number] | null>(null);

// Elder detail drawer
const elderDetailDrawerVisible = ref(false);
const elderDetailData = ref<Api.Elder.Elder | null>(null);

// Staff detail drawer
const staffDetailDrawerVisible = ref(false);
const staffDetailData = ref<Api.Staff.Staff | null>(null);

async function showElderDetail(row: any) {
  if (!row.elderId) return;
  try {
    const { data } = await fetchGetElder(row.elderId);
    if (data) {
      elderDetailData.value = data;
      elderDetailDrawerVisible.value = true;
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
      staffDetailDrawerVisible.value = true;
    }
  } catch (e) {
    console.error('Failed to get staff detail', e);
  }
}

// Evaluation detail drawer
const evalDetailDrawerVisible = ref(false);
const evalDetailData = ref<Api.Evaluation.Evaluation | null>(null);
const replyContent = ref('');
const replyLoading = ref(false);
const relatedQualityCheck = ref<Api.Quality.QualityCheck | null>(null);
const relatedQualityLoading = ref(false);

async function showEvalDetail(row: any) {
  try {
    const { data, error } = await fetchGetEvaluation(row.evaluationId);
    if (error) {
      message.error(error.message || '获取评价详情失败');
      return;
    }
    if (data) {
      evalDetailData.value = data;
      replyContent.value = '';
      evalDetailDrawerVisible.value = true;
      // 加载关联质检记录
      if (data.orderId) {
        relatedQualityLoading.value = true;
        try {
          const { data: qcData } = await fetchGetQualityCheckByOrderId(data.orderId);
          relatedQualityCheck.value = qcData || null;
        } catch {
          relatedQualityCheck.value = null;
        } finally {
          relatedQualityLoading.value = false;
        }
      }
    }
  } catch (e) {
    console.error('Failed to get evaluation detail', e);
  }
}

async function handleReply() {
  if (!evalDetailData.value || !replyContent.value.trim()) {
    message.warning('请输入回复内容');
    return;
  }
  replyLoading.value = true;
  try {
    const { error } = await fetchReplyEvaluation(evalDetailData.value.evaluationId, { reply: replyContent.value });
    if (error) {
      message.error(error.message || '回复失败');
      return;
    }
    message.success('回复成功');
    evalDetailDrawerVisible.value = false;
    getData();
    getStatistics();
  } finally {
    replyLoading.value = false;
  }
}

function getSatisfactionLabel(level: string): string {
  const map: Record<string, string> = {
    VERY_SATISFIED: '非常满意',
    SATISFIED: '满意',
    NEUTRAL: '一般',
    DISSATISFIED: '不满意',
    VERY_DISSATISFIED: '非常不满意'
  };
  return map[level] || level;
}

const tableColumns: DataTableColumns<any> = [
  { type: 'selection' },
  { title: '评价编号', key: 'evaluationId', width: 160 },
  { title: '订单号', key: 'orderId', width: 160 },
  {
    title: '客户姓名',
    key: 'elderName',
    width: 100,
    render: row =>
      h('a', { style: { color: '#18a058', cursor: 'pointer' }, onClick: () => showElderDetail(row) }, row.elderName)
  },
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
  { title: '综合评分', key: 'overallScore', width: 80 },
  {
    title: '满意度',
    key: 'satisfactionLevel',
    width: 100,
    render: row => {
      const typeMap: Record<string, 'success' | 'info' | 'warning' | 'error'> = {
        VERY_SATISFIED: 'success',
        SATISFIED: 'success',
        NEUTRAL: 'warning',
        DISSATISFIED: 'error',
        VERY_DISSATISFIED: 'error'
      };
      return h(NTag, { type: typeMap[row.satisfactionLevel] || 'default', size: 'small' }, () => getSatisfactionLabel(row.satisfactionLevel));
    }
  },
  { title: '评价内容', key: 'content', width: 200, ellipsis: { tooltip: true } },
  { title: '评价时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    fixed: 'right',
    render: row =>
      h(NSpace, { size: 'small' }, () => [
        h(NButton, { size: 'small', type: 'primary', onClick: () => showEvalDetail(row) }, () => '查看'),
        h(NButton, { size: 'small', type: 'info', onClick: () => openInviteDialog(row) }, () => '邀请'),
        h(NButton, {
          size: 'small',
          type: 'warning',
          style: 'margin-left: 4px',
          onClick: () => router.push({ path: '/business/order', query: { orderNo: row.orderNo } })
        }, () => '订单'),
        h(NButton, {
          size: 'small',
          type: 'success',
          style: 'margin-left: 4px',
          onClick: () => router.push({ path: '/business/quality', query: { qcId: row.qualityCheckId } })
        }, () => '质检')
      ])
  }
];

// Table hook
const tableHookResult = useNaivePaginatedTable<any, any>({
  apiFn: async params => {
    const queryParams: any = {
      page: params.page,
      pageSize: params.pageSize
    };
    if (searchOrderNo.value) queryParams.orderNo = searchOrderNo.value;
    if (searchElderName.value) queryParams.elderName = searchElderName.value;
    if (searchProviderName.value) queryParams.providerName = searchProviderName.value;
    if (searchDateRange.value) {
      queryParams.startDate = new Date(searchDateRange.value[0]).toISOString().split('T')[0];
      queryParams.endDate = new Date(searchDateRange.value[1]).toISOString().split('T')[0];
    }
    return fetchGetEvaluationList(queryParams);
  },
  apiParams: {
    page: 1,
    pageSize: 10
  },
  transform: defaultTransform,
  columns: () => tableColumns
});

const {
  data: tableData,
  loading,
  pagination,
  mobilePagination,
  getData,
  getDataByPage,
  columns: filteredColumns,
  columnChecks
} = tableHookResult;

const checkedRowKeys = ref<Array<string | number>>([]);

async function getStatistics() {
  try {
    const { data } = await fetchGetEvaluationStatistics();
    if (data) {
      statistics.value = data;
    }
  } catch (e) {
    console.error('Failed to get statistics', e);
  }
}

function handleResetSearch() {
  searchOrderNo.value = '';
  searchElderName.value = '';
  searchProviderName.value = '';
  searchDateRange.value = null;
  getDataByPage(1);
}

async function handleBatchDelete() {
  if (!checkedRowKeys.value.length) return;
  try {
    await fetchBatchDeleteEvaluation(checkedRowKeys.value.map(String));
    message.success('批量删除成功');
    checkedRowKeys.value = [];
    await getData();
    await getStatistics();
  } catch (e: any) {
    console.error('Batch delete error:', e);
    const errMsg = e?.message || e?.response?.data?.message || '批量删除失败';
    message.error(errMsg);
  }
}

onMounted(() => {
  // 接收质检详情跳转来的订单编号，自动搜索并打开新增评价对话框
  if (route.query.orderNo) {
    searchOrderNo.value = String(route.query.orderNo);
    createOrderNo.value = String(route.query.orderNo);
    message.info(`已定位到订单：${createOrderNo.value}`);
    nextTick(() => {
      getData();
      createDialogVisible.value = true;
    });
  }
  // 接收质检跳转时带入的质检单ID和服务日志ID
  if (route.query.qualityCheckId) {
    createForm.value.qualityCheckId = String(route.query.qualityCheckId);
  }
  if (route.query.serviceLogId) {
    createForm.value.serviceLogId = String(route.query.serviceLogId);
  }
  getStatistics();
  getData();
});
</script>

<template>
  <div>
    <!-- Statistics Cards -->
    <NCard title="满意度评价统计" :bordered="false" style="margin-bottom: 16px">
      <div class="statistics-grid">
        <div class="stat-card stat-primary">
          <div class="stat-label">总评价数</div>
          <div class="stat-value">{{ statistics.totalCount }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">平均综合评分</div>
          <div class="stat-value">{{ formatScore(statistics.averageRating) }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">五星</div>
          <div class="stat-value">{{ statistics.fiveStarCount }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">四星</div>
          <div class="stat-value">{{ statistics.fourStarCount }}</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-label">三星</div>
          <div class="stat-value">{{ statistics.threeStarCount }}</div>
        </div>
        <div class="stat-card stat-error">
          <div class="stat-label">二星</div>
          <div class="stat-value">{{ statistics.twoStarCount }}</div>
        </div>
        <div class="stat-card stat-error">
          <div class="stat-label">一星</div>
          <div class="stat-value">{{ statistics.oneStarCount }}</div>
        </div>
      </div>
    </NCard>

    <!-- 流程指示器 -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <FlowIndicator
        :current-step="currentFlowStep"
        :steps="flowSteps"
        style="border-radius: 12px"
      />
    </NCard>

    <!-- Table -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>满意度评价管理</span>
        </div>
      </template>
      <div style="background: #f5f5f5; padding: 12px; margin-bottom: 12px; border-radius: 4px">
        <NSpace :wrap="true" align="center">
          <NInput v-model:value="searchOrderNo" placeholder="订单号" clearable style="width: 150px" />
          <NInput v-model:value="searchElderName" placeholder="客户姓名" clearable style="width: 100px" />
          <NInput v-model:value="searchProviderName" placeholder="服务商" clearable style="width: 150px" />
          <NDatePicker v-model:value="searchDateRange" type="daterange" clearable style="width: 260px" />
          <NButton type="primary" @click="getData">搜索</NButton>
          <NButton @click="handleResetSearch">重置</NButton>
        </NSpace>
      </div>
      <TableHeaderOperation
        v-model:columns="columnChecks"
        :disabled-delete="checkedRowKeys.length === 0"
        :loading="loading"
        @add="openCreateDialog"
        @refresh="getData"
        @delete="handleBatchDelete"
      />
      <NDataTable
        :columns="filteredColumns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1200"
        :row-key="(row: any) => row.evaluationId"
        v-model:checked-row-keys="checkedRowKeys"
        remote
        :pagination="mobilePagination"
      />
    </NCard>

    <!-- Elder Detail Drawer -->
    <NDrawer v-model:show="elderDetailDrawerVisible" :width="500" placement="right" closable>
      <NDrawerContent title="客户档案详情" closable>
        <NForm v-if="elderDetailData" label-placement="left" label-width="100">
          <NFormItem label="姓名">{{ elderDetailData.name }}</NFormItem>
          <NFormItem label="性别">
            {{ elderDetailData.gender === 'MALE' ? '男' : elderDetailData.gender === 'FEMALE' ? '女' : '未知' }}
          </NFormItem>
          <NFormItem label="年龄">{{ elderDetailData.age }}</NFormItem>
          <NFormItem label="身份证号">{{ elderDetailData.idCard }}</NFormItem>
          <NFormItem label="手机号">{{ elderDetailData.phone }}</NFormItem>
          <NFormItem label="地址">{{ elderDetailData.address }}</NFormItem>
          <NFormItem label="养老类型">
            {{
              elderDetailData.careType === 'HOME'
                ? '居家养老'
                : elderDetailData.careType === 'COMMUNITY'
                  ? '社区养老'
                  : elderDetailData.careType === 'INSTITUTION'
                    ? '机构养老'
                    : '-'
            }}
          </NFormItem>
          <NFormItem label="护理等级">{{ elderDetailData.careLevel }}</NFormItem>
          <NFormItem label="紧急联系人">{{ elderDetailData.emergencyContact || '-' }}</NFormItem>
          <NFormItem label="紧急联系电话">{{ elderDetailData.emergencyPhone || '-' }}</NFormItem>
        </NForm>
      </NDrawerContent>
    </NDrawer>

    <!-- Staff Detail Drawer -->
    <NDrawer v-model:show="staffDetailDrawerVisible" :width="500" placement="right" closable>
      <NDrawerContent title="服务人员详情" closable>
        <NForm v-if="staffDetailData" label-placement="left" label-width="100">
          <NFormItem label="姓名">{{ staffDetailData.staffName }}</NFormItem>
          <NFormItem label="性别">{{ Number(staffDetailData.gender) === 1 ? "男" : "女" }}</NFormItem>
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

    <!-- Evaluation Detail Drawer -->
    <NDrawer v-model:show="evalDetailDrawerVisible" :width="560" placement="right" closable>
      <NDrawerContent title="评价详情" closable>
        <NForm v-if="evalDetailData" label-placement="left" label-width="100">
          <NFormItem label="评价编号">{{ evalDetailData.evaluationId }}</NFormItem>
          <NFormItem label="订单号">{{ evalDetailData.orderId || '-' }}</NFormItem>
          <NFormItem label="服务商">{{ evalDetailData.providerName || '-' }}</NFormItem>
          <NFormItem label="服务人员">{{ evalDetailData.staffName || '-' }}</NFormItem>
          <NFormItem label="评价人">{{ evalDetailData.elderName }}</NFormItem>
          <NFormItem label="服务评分">{{ evalDetailData.overallScore }}分</NFormItem>
          <NFormItem label="态度评分">{{ evalDetailData.attitudeScore }}分</NFormItem>
          <NFormItem label="技能评分">{{ evalDetailData.qualityScore }}分</NFormItem>
          <NFormItem label="准时评分">{{ evalDetailData.efficiencyScore }}分</NFormItem>
          <NFormItem label="综合评分">
            <NTag type="success" size="small">{{ evalDetailData.overallScore }}分</NTag>
          </NFormItem>
          <NFormItem label="满意度">
            <NTag :type="evalDetailData.overallScore >= 4 ? 'success' : 'warning'" size="small">
              {{ evalDetailData.overallScore >= 5 ? '非常满意' : evalDetailData.overallScore >= 4 ? '满意' : evalDetailData.overallScore >= 3 ? '一般' : '不满意' }}
            </NTag>
          </NFormItem>
          <NFormItem label="评价内容">{{ evalDetailData.content || '-' }}</NFormItem>
          <NFormItem label="评价图片" v-if="evalDetailData.images && evalDetailData.images.length">
            <NImageGroup>
              <NSpace>
                <template v-for="img in evalDetailData.images" :key="img">
                  <NImage :src="img" width="80" height="80" object-fit="cover" />
                </template>
              </NSpace>
            </NImageGroup>
          </NFormItem>
          <NFormItem label="机构回复" v-if="evalDetailData.replyContent">
            {{ evalDetailData.replyContent }}
          </NFormItem>
          <NFormItem label="回复时间" v-if="evalDetailData.replyTime">
            {{ evalDetailData.replyTime }}
          </NFormItem>
          <NFormItem label="评价时间">{{ evalDetailData.createTime }}</NFormItem>
          <!-- 关联质检记录 -->
          <NFormItem label="关联质检">
            <NSpin :show="relatedQualityLoading">
              <template v-if="relatedQualityCheck">
                <NTag type="info" size="small">质检编号：{{ relatedQualityCheck.checkNo }}</NTag>
                <NTag type="warning" size="small" style="margin-left: 8px">
                  {{ relatedQualityCheck.checkType === 'ROUTINE' ? '日常巡检' : relatedQualityCheck.checkType === 'SPOT' ? '突击检查' : '专项检查' }}
                </NTag>
                <NTag :type="relatedQualityCheck.checkResult === 'PASS' ? 'success' : 'error'" size="small" style="margin-left: 8px">
                  {{ relatedQualityCheck.checkResult === 'PASS' ? '合格' : '不合格' }}
                </NTag>
              </template>
              <NText v-else depth="3" italic>暂无关联质检记录</NText>
            </NSpin>
          </NFormItem>
        </NForm>
        <template #footer>
          <NSpace vertical align="stretch" style="width: 100%">
            <NInput
              v-model:value="replyContent"
              type="textarea"
              placeholder="请输入回复内容"
              :rows="2"
            />
            <NSpace justify="end">
              <NButton @click="evalDetailDrawerVisible = false">关闭</NButton>
              <NButton type="primary" :loading="replyLoading" @click="handleReply">
                回复
              </NButton>
            </NSpace>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- Create Evaluation Dialog -->
    <NModal
      v-model:show="createDialogVisible"
      preset="card"
      title="新增满意度评价"
      style="width: 560px"
      :segmented="{ content: true, footer: true }"
    >
      <NForm label-placement="left" label-width="100">
        <NFormItem label="订单编号" required>
          <NInput v-model:value="createOrderNo" placeholder="请输入订单编号" />
        </NFormItem>
        <NFormItem label="服务评分">
          <NInputNumber v-model:value="createForm.serviceScore" :min="1" :max="5" style="width: 100px" />
        </NFormItem>
        <NFormItem label="态度评分">
          <NInputNumber v-model:value="createForm.attitudeScore" :min="1" :max="5" style="width: 100px" />
        </NFormItem>
        <NFormItem label="技能评分">
          <NInputNumber v-model:value="createForm.skillScore" :min="1" :max="5" style="width: 100px" />
        </NFormItem>
        <NFormItem label="准时评分">
          <NInputNumber v-model:value="createForm.punctualityScore" :min="1" :max="5" style="width: 100px" />
        </NFormItem>
        <NFormItem label="环境评分">
          <NInputNumber v-model:value="createForm.environmentScore" :min="1" :max="5" style="width: 100px" />
        </NFormItem>
        <NFormItem label="综合评分">
          <NInputNumber v-model:value="createForm.overallScore" :min="1" :max="5" style="width: 100px" />
        </NFormItem>
        <NFormItem label="评价内容">
          <NInput
            v-model:value="createForm.content"
            type="textarea"
            placeholder="请输入评价内容"
            :rows="3"
          />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="createDialogVisible = false">取消</NButton>
          <NButton type="primary" :loading="createLoading" @click="handleCreateSubmit">
            提交
          </NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Invite Drawer -->
    <NDrawer v-model:show="inviteDialogVisible" :width="420" placement="right" closable>
      <NDrawerContent title="生成评价邀请链接" closable>
        <NForm label-placement="left" label-width="90">
          <NFormItem label="订单ID">
            <NText>{{ inviteForm.orderId || '-' }}</NText>
          </NFormItem>
          <NFormItem label="老人ID">
            <NText>{{ inviteForm.elderId || '-' }}</NText>
          </NFormItem>
          <NFormItem label="老人姓名">
            <NText>{{ inviteForm.elderName || '-' }}</NText>
          </NFormItem>
          <NFormItem label="有效期">
            <NInputNumber v-model:value="inviteForm.expireHours" :min="1" :max="720" style="width: 120px" />
            <NText depth="3" style="margin-left: 8px">小时</NText>
          </NFormItem>
        </NForm>

        <NSpace vertical style="width: 100%; margin-top: 16px" v-if="!generatedLink">
          <NButton type="primary" :loading="inviteLoading" style="width: 100%" @click="handleGenerateLink">
            生成邀请链接
          </NButton>
        </NSpace>

        <div v-if="generatedLink" style="margin-top: 16px">
          <NText depth="3" style="margin-bottom: 8px; display: block">邀请链接：</NText>
          <NSpace vertical style="width: 100%">
            <NInput :value="generatedLink" readonly />
            <NButton type="primary" @click="copyLink" style="width: 100%">
              复制链接
            </NButton>
          </NSpace>
        </div>
        <template #footer>
          <NButton @click="inviteDialogVisible = false">关闭</NButton>
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
  background: linear-gradient(135deg, #5E8B7E 0%, #7BA89F 100%);
  color: white;
}

.stat-primary .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-info {
  background: linear-gradient(135deg, #3A506B 0%, #5A7A8B 100%);
  color: white;
}

.stat-info .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-success {
  background: linear-gradient(135deg, #5E8B7E 0%, #81B29A 100%);
  color: white;
}

.stat-success .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-warning {
  background: linear-gradient(135deg, #F9B572 0%, #F7C59F 100%);
  color: white;
}

.stat-warning .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-error {
  background: linear-gradient(135deg, #D64045 0%, #E07070 100%);
  color: white;
}

.stat-error .stat-label {
  color: rgba(255, 255, 255, 0.85);
}
</style>
