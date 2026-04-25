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
  fetchGetQualityCheckByOrderId
} from '@/service/api';
import { useNaivePaginatedTable, defaultTransform } from '@/hooks/common/table';
import { useAuth } from '@/hooks/business/auth';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';
import FlowIndicator from '@/components/business/FlowIndicator.vue';

defineOptions({
  name: 'BusinessEvaluation'
});

const message = useMessage();
const router = useRouter();
const route = useRoute();

// Create dialog state
const createDialogVisible = ref(false);
const createLoading = ref(false);
const createForm = ref<Api.Evaluation.EvaluationForm>({
  orderId: '',
  serviceScore: 5,
  attitudeScore: 5,
  skillScore: 5,
  punctualityScore: 5,
  overallScore: 5,
  content: '',
  images: []
});

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
  if (statistics.value.total > 0) {
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
  createForm.value = {
    orderId: '',
    serviceScore: 5,
    attitudeScore: 5,
    skillScore: 5,
    punctualityScore: 5,
    overallScore: 5,
    content: '',
    images: []
  };
  createDialogVisible.value = true;
}

async function handleCreateSubmit() {
  if (!createForm.value.orderId) {
    message.warning('请输入订单ID');
    return;
  }
  createLoading.value = true;
  try {
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

// Statistics
const { hasAuth } = useAuth();
const statistics = ref<Api.Evaluation.Statistics>({
  total: 0,
  avgOverallScore: 0,
  avgServiceScore: 0,
  avgAttitudeScore: 0,
  avgSkillScore: 0,
  avgPunctualityScore: 0,
  verySatisfiedCount: 0,
  satisfiedCount: 0,
  neutralCount: 0,
  dissatisfiedCount: 0,
  veryDissatisfiedCount: 0,
  satisfactionRate: 0
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
    const { error } = await fetchReplyEvaluation(evalDetailData.value.id, { reply: replyContent.value });
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

const columns: DataTableColumns<any> = [
  { title: '评价编号', key: 'evalNo', width: 160 },
  { title: '订单号', key: 'orderNo', width: 160 },
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
    width: 100,
    fixed: 'right',
    render: row =>
      h(
        NButton,
        { size: 'small', type: 'primary', onClick: () => showEvalDetail(row) },
        () => '查看'
      )
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

const checkedRowKeys = ref<Array<string | number>>([]);

const columnChecks = ref<Array<{ prop: string; label: string; checked: boolean }>>(
  rawColumnChecks.value ? [...rawColumnChecks.value] : []
);
watch(
  () => rawColumnChecks.value,
  val => {
    if (val && val.length > 0) {
      columnChecks.value = [...val];
    }
  },
  { immediate: true, deep: true }
);

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

onMounted(() => {
  // 接收质检详情跳转来的订单编号，自动搜索并打开新建评价对话框
  if (route.query.orderNo) {
    searchOrderNo.value = String(route.query.orderNo);
    createForm.value.orderId = String(route.query.orderNo); // orderId与orderNo相同，方便快速填写
    nextTick(() => {
      getData();
      createDialogVisible.value = true;
    });
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
          <div class="stat-value">{{ statistics.total }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">平均综合评分</div>
          <div class="stat-value">{{ Number(statistics.avgOverallScore || 0).toFixed(1) }}</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-label">非常满意</div>
          <div class="stat-value">{{ statistics.verySatisfiedCount }}</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-label">满意</div>
          <div class="stat-value">{{ statistics.satisfiedCount }}</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-label">一般</div>
          <div class="stat-value">{{ statistics.neutralCount }}</div>
        </div>
        <div class="stat-card stat-error">
          <div class="stat-label">不满意</div>
          <div class="stat-value">{{ statistics.dissatisfiedCount }}</div>
        </div>
        <div class="stat-card stat-error">
          <div class="stat-label">非常不满意</div>
          <div class="stat-value">{{ statistics.veryDissatisfiedCount }}</div>
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
        :loading="loading"
        @add="openCreateDialog"
        @refresh="getData"
      />
      <NDataTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1200"
        :row-key="(row: any) => row.evaluationId"
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

    <!-- Evaluation Detail Drawer -->
    <NDrawer v-model:show="evalDetailDrawerVisible" :width="560" placement="right" closable>
      <NDrawerContent title="评价详情" closable>
        <NForm v-if="evalDetailData" label-placement="left" label-width="100">
          <NFormItem label="评价编号">{{ evalDetailData.evalNo }}</NFormItem>
          <NFormItem label="订单号">{{ evalDetailData.orderNo || '-' }}</NFormItem>
          <NFormItem label="服务商">{{ evalDetailData.providerName || '-' }}</NFormItem>
          <NFormItem label="服务人员">{{ evalDetailData.staffName || '-' }}</NFormItem>
          <NFormItem label="评价人">{{ evalDetailData.elderName }}</NFormItem>
          <NFormItem label="服务评分">{{ evalDetailData.serviceScore }}分</NFormItem>
          <NFormItem label="态度评分">{{ evalDetailData.attitudeScore }}分</NFormItem>
          <NFormItem label="技能评分">{{ evalDetailData.skillScore }}分</NFormItem>
          <NFormItem label="准时评分">{{ evalDetailData.punctualityScore }}分</NFormItem>
          <NFormItem label="综合评分">
            <NTag type="success" size="small">{{ evalDetailData.overallScore }}分</NTag>
          </NFormItem>
          <NFormItem label="满意度">
            <NTag :type="evalDetailData.satisfactionLevel === 'VERY_SATISFIED' || evalDetailData.satisfactionLevel === 'SATISFIED' ? 'success' : 'warning'" size="small">
              {{ getSatisfactionLabel(evalDetailData.satisfactionLevel) }}
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
          <NFormItem label="机构回复" v-if="evalDetailData.reply">
            {{ evalDetailData.reply }}
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
      title="新建满意度评价"
      style="width: 560px"
      :segmented="{ content: true, footer: true }"
    >
      <NForm label-placement="left" label-width="100">
        <NFormItem label="订单ID" required>
          <NInput v-model:value="createForm.orderId" placeholder="请输入订单ID" />
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
