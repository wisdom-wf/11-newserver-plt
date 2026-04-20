<script setup lang="ts">
import { ref, h, onMounted, computed } from 'vue';
import {
  NButton,
  NCard,
  NTag,
  NSpace,
  NInput,
  NSelect,
  NDrawer,
  NDrawerContent,
  useMessage,
  NDataTable,
  NDatePicker,
  NModal,
  NTabs,
  NTabPane,
  NStatistic,
  NGrid,
  NGi,
  NEmpty,
  NSpin,
  NAlert,
  NUpload,
  NImage,
  NProgress,
  NAvatar
} from 'naive-ui';
import PersonCard from '@/components/common/person-card.vue';
import type { DataTableColumns, UploadFile } from 'naive-ui';
import {
  fetchGetElderList,
  fetchGetElder,
  fetchAddHealthMeasurement,
  fetchAddHealthMeasurements,
  fetchGetHealthMeasurementHistory,
  fetchGetMeasurementStatistics,
  fetchGetAllMeasurementStatistics,
  fetchDeleteMeasurement,
  fetchGenerateHealthReport,
  fetchGetHealthReportList,
  fetchDownloadHealthReportPdf,
  fetchDeleteHealthReport,
  fetchGetCareSuggestions,
  fetchGetMedicalSuggestions,
  fetchGetRecentUpdatedElders,
  fetchUpdateElder
} from '@/service/api/elder';
import { useAuth } from '@/hooks/business/auth';

defineOptions({
  name: 'BusinessHealthArchive'
});

const message = useMessage();
const { hasAuth } = useAuth();

// Elder selection
const elderOptions = ref<{ label: string; value: string }[]>([]);
const selectedElderId = ref<string>('');
const selectedElder = ref<Api.Elder.Elder | null>(null);
const elderLoading = ref(false);

// Measurement form
const measurementDrawerVisible = ref(false);
const measurementLoading = ref(false);
const measurementForm = ref({
  measurementType: 'BLOOD_PRESSURE' as Api.Elder.MeasurementType,
  measurementValue: '',
  measuredAt: Date.now(),
  remark: ''
});

// Measurement type options
const measurementTypeOptions = [
  { label: '血压', value: 'BLOOD_PRESSURE' },
  { label: '血糖', value: 'BLOOD_GLUCOSE' },
  { label: '体重', value: 'WEIGHT' },
  { label: '体温', value: 'TEMPERATURE' },
  { label: '脉搏', value: 'PULSE' },
  { label: '血氧', value: 'SPO2' },
  { label: '疼痛指数', value: 'PAIN_SCALE' },
  { label: '其他', value: 'OTHER' }
];

// Report form
const reportDrawerVisible = ref(false);
const reportLoading = ref(false);
const reportForm = ref({
  reportType: 'MONTHLY' as Api.Elder.ReportType,
  startDate: null as number | null,
  endDate: null as number | null
});

// Report type options
const reportTypeOptions = [
  { label: '月度报告', value: 'MONTHLY' },
  { label: '季度报告', value: 'QUARTERLY' },
  { label: '年度报告', value: 'YEARLY' },
  { label: '专项报告', value: 'SPECIAL' }
];

// Measurements data
const measurements = ref<Api.Elder.HealthMeasurement[]>([]);
const measurementsLoading = ref(false);
const measurementStats = ref<Api.Elder.HealthMeasurementStatistics[]>([]);

// Reports data
const reports = ref<Api.Elder.HealthReportVO[]>([]);
const reportsLoading = ref(false);

// AI Suggestions data
const careSuggestions = ref<Api.Elder.CareSuggestionVO | null>(null);
const medicalSuggestions = ref<Api.Elder.MedicalSuggestionVO | null>(null);
const suggestionsLoading = ref(false);

// Active tab
const activeTab = ref('measurements');

// Recent updated elders cards
const recentElders = ref<Api.Elder.ElderHealthCard[]>([]);
const recentEldersLoading = ref(false);

// Load recent elders cards
async function loadRecentElders() {
  recentEldersLoading.value = true;
  try {
    const { data, error } = await fetchGetRecentUpdatedElders(10);
    if (error) {
      console.error('Failed to load recent elders', error);
      return;
    }
    recentElders.value = data || [];
    // 自动选中第一位客户并加载信息
    if (recentElders.value.length > 0 && !selectedElderId.value) {
      const firstElder = recentElders.value[0];
      selectedElderId.value = firstElder.elderId;
      handleElderChange(firstElder.elderId);
    }
  } catch (e) {
    console.error('Failed to load recent elders', e);
  } finally {
    recentEldersLoading.value = false;
  }
}

// Get health index color
function getHealthIndexColor(index?: number): string {
  if (!index) return '#999';
  if (index >= 80) return '#52c41a'; // 绿色
  if (index >= 60) return '#fa8c16'; // 橙色
  return '#f5222d'; // 红色
}

// Get health status text
function getHealthStatusText(index?: number): string {
  if (!index) return '暂无数据';
  if (index >= 80) return '正常';
  if (index >= 60) return '预警';
  return '告警';
}

// Handle elder card click
function handleElderCardClick(elderId: string) {
  selectedElderId.value = elderId;
  handleElderChange(elderId);
}

// Handle photo upload for elder card
async function handlePhotoUpload(elderId: string, file: File) {
  const reader = new FileReader();
  reader.onload = async (e) => {
    if (e.target?.result) {
      const base64Data = e.target.result as string;
      try {
        const { error } = await fetchUpdateElder(elderId, { photoUrl: base64Data } as any);
        if (error) {
          message.error(error.message || '上传失败');
          return;
        }
        message.success('上传成功');
        // Update local data
        const card = recentElders.value.find(c => c.elderId === elderId);
        if (card) {
          card.photoUrl = base64Data;
        }
        // Also refresh elder detail if this is the selected one
        if (selectedElderId.value === elderId) {
          loadElderDetail();
        }
      } catch (err) {
        message.error('上传失败');
      }
    }
  };
  reader.readAsDataURL(file);
  return false;
}

// Load elders for selection
async function loadElders() {
  try {
    const { data } = await fetchGetElderList({ page: 1, pageSize: 100 });
    if (data?.records) {
      elderOptions.value = data.records.map(e => ({
        label: `${e.name} (${e.idCard})`,
        value: e.elderId
      }));
    }
  } catch (e) {
    console.error('Failed to load elders', e);
  }
}

// Load elder detail
async function loadElderDetail() {
  if (!selectedElderId.value) {
    selectedElder.value = null;
    return;
  }
  elderLoading.value = true;
  try {
    const { data, error } = await fetchGetElder(selectedElderId.value);
    if (error) {
      message.error(error.message || '获取客户信息失败');
      return;
    }
    selectedElder.value = data;
  } finally {
    elderLoading.value = false;
  }
}

// Load measurements
async function loadMeasurements() {
  if (!selectedElderId.value) return;
  measurementsLoading.value = true;
  try {
    const { data, error } = await fetchGetHealthMeasurementHistory(selectedElderId.value, { limit: 50 });
    if (error) {
      message.error(error.message || '获取测量记录失败');
      return;
    }
    measurements.value = data || [];
  } finally {
    measurementsLoading.value = false;
  }
}

// Load measurement statistics
async function loadMeasurementStats() {
  if (!selectedElderId.value) return;
  try {
    const { data, error } = await fetchGetAllMeasurementStatistics(selectedElderId.value);
    if (error) {
      message.error(error.message || '获取统计失败');
      return;
    }
    measurementStats.value = data || [];
  } catch (e) {
    console.error('Failed to load statistics', e);
  }
}

// Load reports
async function loadReports() {
  if (!selectedElderId.value) return;
  reportsLoading.value = true;
  try {
    const { data, error } = await fetchGetHealthReportList(selectedElderId.value);
    if (error) {
      message.error(error.message || '获取报告失败');
      return;
    }
    reports.value = data || [];
  } finally {
    reportsLoading.value = false;
  }
}

// Load AI suggestions
async function loadSuggestions() {
  if (!selectedElderId.value) return;
  suggestionsLoading.value = true;
  try {
    const [careRes, medicalRes] = await Promise.all([
      fetchGetCareSuggestions(selectedElderId.value),
      fetchGetMedicalSuggestions(selectedElderId.value)
    ]);
    if (careRes.error) {
      console.error('Failed to load care suggestions', careRes.error);
    } else {
      careSuggestions.value = careRes.data;
    }
    if (medicalRes.error) {
      console.error('Failed to load medical suggestions', medicalRes.error);
    } else {
      medicalSuggestions.value = medicalRes.data;
    }
  } catch (e) {
    console.error('Failed to load suggestions', e);
  } finally {
    suggestionsLoading.value = false;
  }
}

// Add measurement
async function handleAddMeasurement() {
  if (!selectedElderId.value) {
    message.warning('请先选择客户');
    return;
  }
  if (!measurementForm.value.measurementValue) {
    message.warning('请输入测量值');
    return;
  }
  measurementLoading.value = true;
  try {
    const { error } = await fetchAddHealthMeasurement(selectedElderId.value, {
      measurementType: measurementForm.value.measurementType,
      measurementValue: measurementForm.value.measurementValue,
      measuredAt: measurementForm.value.measuredAt ? new Date(measurementForm.value.measuredAt).toISOString() : undefined,
      remark: measurementForm.value.remark || undefined
    });
    if (error) {
      message.error(error.message || '添加失败');
      return;
    }
    message.success('添加成功');
    measurementDrawerVisible.value = false;
    resetMeasurementForm();
    await loadMeasurements();
    await loadMeasurementStats();
  } finally {
    measurementLoading.value = false;
  }
}

// Reset measurement form
function resetMeasurementForm() {
  measurementForm.value = {
    measurementType: 'BLOOD_PRESSURE',
    measurementValue: '',
    measuredAt: Date.now(),
    remark: ''
  };
}

// Delete measurement
async function handleDeleteMeasurement(id: string) {
  try {
    const { error } = await fetchDeleteMeasurement(id);
    if (error) {
      message.error(error.message || '删除失败');
      return;
    }
    message.success('删除成功');
    await loadMeasurements();
    await loadMeasurementStats();
  } catch (e) {
    console.error('Failed to delete', e);
  }
}

// Generate report
async function handleGenerateReport() {
  if (!selectedElderId.value) {
    message.warning('请先选择客户');
    return;
  }
  reportLoading.value = true;
  try {
    const { error } = await fetchGenerateHealthReport(selectedElderId.value, {
      reportType: reportForm.value.reportType,
      startDate: reportForm.value.startDate ? new Date(reportForm.value.startDate).toISOString().split('T')[0] : undefined,
      endDate: reportForm.value.endDate ? new Date(reportForm.value.endDate).toISOString().split('T')[0] : undefined
    });
    if (error) {
      message.error(error.message || '生成失败');
      return;
    }
    message.success('生成成功');
    reportDrawerVisible.value = false;
    resetReportForm();
    await loadReports();
  } finally {
    reportLoading.value = false;
  }
}

// Reset report form
function resetReportForm() {
  reportForm.value = {
    reportType: 'MONTHLY',
    startDate: null,
    endDate: null
  };
}

// Download report PDF
async function handleDownloadPdf(reportId: string) {
  try {
    const { data, error } = await fetchDownloadHealthReportPdf(reportId);
    if (error) {
      message.error(error.message || '下载失败');
      return;
    }
    // Create blob and download
    const blob = new Blob([data as BlobPart], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `health-report-${reportId}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    message.success('下载成功');
  } catch (e) {
    console.error('Failed to download', e);
    message.error('下载失败');
  }
}

// Delete report
async function handleDeleteReport(reportId: string) {
  try {
    const { error } = await fetchDeleteHealthReport(reportId);
    if (error) {
      message.error(error.message || '删除失败');
      return;
    }
    message.success('删除成功');
    await loadReports();
  } catch (e) {
    console.error('Failed to delete', e);
  }
}

// Measurement table columns
const measurementColumns: DataTableColumns<Api.Elder.HealthMeasurement> = [
  { title: '类型', key: 'measurementTypeName', width: 100 },
  { title: '测量值', key: 'measurementValue', width: 120 },
  { title: '单位', key: 'measurementUnit', width: 80 },
  { title: '测量时间', key: 'measuredAt', width: 170 },
  { title: '备注', key: 'remark', ellipsis: true },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: row =>
      h(
        NSpace,
        { size: 'small' },
        () => [
          hasAuth('elder:list:delete') &&
            h(
              NButton,
              { size: 'small', type: 'error', onClick: () => handleDeleteMeasurement(row.measurementId) },
              () => '删除'
            )
        ]
      )
  }
];

// Report table columns
const reportColumns: DataTableColumns<Api.Elder.HealthReportVO> = [
  { title: '报告编号', key: 'reportNo', width: 180 },
  { title: '标题', key: 'title', ellipsis: true },
  { title: '类型', key: 'reportTypeName', width: 100 },
  { title: '日期', key: 'reportDate', width: 120 },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: row =>
      h(
        NSpace,
        { size: 'small' },
        () => [
          h(
            NButton,
            { size: 'small', type: 'info', onClick: () => handleDownloadPdf(row.reportId) },
            () => '下载PDF'
          ),
          hasAuth('elder:list:delete') &&
            h(
              NButton,
              { size: 'small', type: 'error', onClick: () => handleDeleteReport(row.reportId) },
              () => '删除'
            )
        ]
      )
  }
];

// Get alert status color
function getAlertStatusColor(status?: string): string {
  switch (status) {
    case 'NORMAL':
      return 'success';
    case 'WARNING':
      return 'warning';
    case 'ALERT':
      return 'error';
    default:
      return 'default';
  }
}

// Get alert status text
function getAlertStatusText(status?: string): string {
  switch (status) {
    case 'NORMAL':
      return '正常';
    case 'WARNING':
      return '预警';
    case 'ALERT':
      return '告警';
    default:
      return '未知';
  }
}

// Watch for elder selection change
function handleElderChange(value: string) {
  selectedElderId.value = value;
  loadElderDetail();
  loadMeasurements();
  loadMeasurementStats();
  loadReports();
  loadSuggestions();
}

onMounted(() => {
  loadElders();
  loadRecentElders();
});
</script>

<template>
  <div>
    <!-- 最近更新客户卡片区域 -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between; width: 100%">
          <span>最近更新客户</span>
          <NSelect
            v-model:value="selectedElderId"
            :options="elderOptions"
            placeholder="快速选择客户"
            filterable
            clearable
            style="width: 200px"
            @update:value="handleElderChange"
          />
        </div>
      </template>
      <NSpin :show="recentEldersLoading">
        <div v-if="recentElders.length > 0" style="display: flex; gap: 12px; overflow-x: auto; padding-bottom: 8px; flex-wrap: nowrap; align-items: stretch">
          <PersonCard
            v-for="elder in recentElders"
            :key="elder.elderId"
            :photo-url="elder.photoUrl"
            :name="elder.name || '未知'"
            :subtitle="`${elder.age || '-'}岁 | ${elder.careLevelName || elder.careLevel || '-'}`"
            :extra-info="elder.genderName ? [{ label: '性别', value: elder.genderName }] : []"
            :index-value="elder.healthIndex"
            index-label="健康指数"
            :selected="selectedElderId === elder.elderId"
            photo-width="76"
            scale="0.85"
            @click="handleElderCardClick(elder.elderId)"
            @photo-upload="(file) => handlePhotoUpload(elder.elderId, file)"
          />
        </div>
        <NEmpty v-else-if="!recentEldersLoading" description="暂无最近更新的客户" />
      </NSpin>
    </NCard>

    <NCard v-if="selectedElderId" :bordered="false">
      <NTabs v-model:value="activeTab" type="line">
        <NTabPane name="measurements" tab="健康测量">
          <div style="margin-bottom: 16px">
            <NSpace>
              <NButton type="primary" @click="measurementDrawerVisible = true">添加测量记录</NButton>
            </NSpace>
          </div>

          <!-- Statistics Cards -->
          <div v-if="measurementStats.length > 0" style="margin-bottom: 16px; display: flex; gap: 16px; flex-wrap: wrap">
            <NCard
              v-for="stat in measurementStats"
              :key="stat.measurementType"
              size="small"
              style="width: 200px"
            >
              <div style="font-size: 13px; color: #666">{{ stat.measurementTypeName }}</div>
              <div style="font-size: 24px; font-weight: 600; margin: 8px 0">
                {{ stat.latestValue || '-' }}
                <span style="font-size: 12px; color: #999">{{ stat.measurementUnit }}</span>
              </div>
              <div style="font-size: 12px; color: #999">
                共 {{ stat.count }} 条记录
              </div>
              <NTag
                v-if="stat.alertStatus !== 'NORMAL'"
                :type="getAlertStatusColor(stat.alertStatus) as any"
                size="small"
                style="margin-top: 4px"
              >
                {{ getAlertStatusText(stat.alertStatus) }}
              </NTag>
            </NCard>
          </div>

          <NDataTable
            :columns="measurementColumns"
            :data="measurements"
            :loading="measurementsLoading"
            :row-key="(row: Api.Elder.HealthMeasurement) => row.measurementId"
          />

          <NEmpty v-if="!measurementsLoading && measurements.length === 0" description="暂无测量记录" style="margin-top: 40px" />
        </NTabPane>

        <NTabPane name="reports" tab="健康报告">
          <div style="margin-bottom: 16px">
            <NSpace>
              <NButton type="primary" @click="reportDrawerVisible = true">生成报告</NButton>
            </NSpace>
          </div>

          <NDataTable
            :columns="reportColumns"
            :data="reports"
            :loading="reportsLoading"
            :row-key="(row: Api.Elder.HealthReportVO) => row.reportId"
          />

          <NEmpty v-if="!reportsLoading && reports.length === 0" description="暂无报告" style="margin-top: 40px" />
        </NTabPane>

        <NTabPane name="suggestions" tab="AI健康建议">
          <NSpin :show="suggestionsLoading">
            <!-- 护理建议 -->
            <NCard v-if="careSuggestions" title="护理建议" size="small" style="margin-bottom: 16px">
              <template #header-extra>
                <NTag type="info">{{ careSuggestions.careLevelSuggestion }}</NTag>
              </template>
              <div v-if="careSuggestions.riskAlerts && careSuggestions.riskAlerts.length > 0" style="margin-bottom: 12px">
                <NAlert type="warning" :title="'风险预警 (' + careSuggestions.riskAlerts.length + ')'">
                  <div v-for="(alert, idx) in careSuggestions.riskAlerts" :key="idx" style="margin-bottom: 4px">
                    {{ alert }}
                  </div>
                </NAlert>
              </div>
              <div v-if="careSuggestions.suggestions && careSuggestions.suggestions.length > 0">
                <div
                  v-for="(suggestion, idx) in careSuggestions.suggestions"
                  :key="idx"
                  style="padding: 12px; border-bottom: 1px solid #f0f0f0"
                >
                  <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px">
                    <NTag :type="suggestion.priority <= 1 ? 'error' : suggestion.priority <= 2 ? 'warning' : 'info'" size="small">
                      {{ suggestion.typeName }}
                    </NTag>
                    <span style="font-size: 12px; color: #999">依据: {{ suggestion.basis }}</span>
                  </div>
                  <div>{{ suggestion.content }}</div>
                </div>
              </div>
              <NEmpty v-else description="暂无护理建议" />
            </NCard>

            <!-- 就医建议 -->
            <NCard v-if="medicalSuggestions" title="就医建议" size="small">
              <template #header-extra>
                <NTag :type="medicalSuggestions.urgencyLevel === 'URGENT' ? 'error' : medicalSuggestions.urgencyLevel === 'WARNING' ? 'warning' : 'success'" size="small">
                  {{ medicalSuggestions.urgencyLevelName }}
                </NTag>
              </template>
              <div v-if="medicalSuggestions.symptoms && medicalSuggestions.symptoms.length > 0" style="margin-bottom: 12px">
                <div style="color: #999; font-size: 13px; margin-bottom: 4px">异常症状:</div>
                <NTag v-for="(symptom, idx) in medicalSuggestions.symptoms" :key="idx" type="warning" size="small" style="margin-right: 4px">
                  {{ symptom }}
                </NTag>
              </div>
              <div v-if="medicalSuggestions.suggestedDepartment" style="margin-bottom: 12px">
                <span style="color: #999; font-size: 13px">建议就诊科室: </span>
                <span style="font-weight: 500">{{ medicalSuggestions.suggestedDepartment }}</span>
              </div>
              <div v-if="medicalSuggestions.suggestions && medicalSuggestions.suggestions.length > 0">
                <div
                  v-for="(suggestion, idx) in medicalSuggestions.suggestions"
                  :key="idx"
                  style="padding: 12px; border-bottom: 1px solid #f0f0f0"
                >
                  <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px">
                    <NTag :type="suggestion.priority <= 1 ? 'error' : suggestion.priority <= 2 ? 'warning' : 'info'" size="small">
                      {{ suggestion.typeName }}
                    </NTag>
                    <span style="font-size: 12px; color: #999">依据: {{ suggestion.basis }}</span>
                  </div>
                  <div>{{ suggestion.content }}</div>
                </div>
              </div>
              <NEmpty v-else description="暂无就医建议" />
            </NCard>

            <NEmpty v-if="!careSuggestions && !medicalSuggestions" description="暂无AI建议，请确保该客户有完整的健康档案和测量记录" style="margin-top: 40px" />
          </NSpin>
        </NTabPane>
      </NTabs>
    </NCard>

    <NEmpty v-if="!selectedElderId" description="请先选择客户" style="margin-top: 100px" />

    <!-- Add Measurement Drawer -->
    <NDrawer v-model:show="measurementDrawerVisible" :width="400" placement="right" closable>
      <NDrawerContent title="添加测量记录" closable>
        <div style="padding: 0 8px">
          <div style="margin-bottom: 16px">
            <div style="color: #999; font-size: 13px; margin-bottom: 8px">测量类型</div>
            <NSelect
              v-model:value="measurementForm.measurementType"
              :options="measurementTypeOptions"
              style="width: 100%"
            />
          </div>

          <div style="margin-bottom: 16px">
            <div style="color: #999; font-size: 13px; margin-bottom: 8px">
              测量值
              <span v-if="measurementForm.measurementType === 'BLOOD_PRESSURE'" style="color: #999">
                (格式: 收缩压/舒张压，如 120/80)
              </span>
              <span v-if="measurementForm.measurementType === 'BLOOD_GLUCOSE'" style="color: #999">
                (单位: mmol/L)
              </span>
            </div>
            <NInput
              v-model:value="measurementForm.measurementValue"
              placeholder="请输入测量值"
            />
          </div>

          <div style="margin-bottom: 16px">
            <div style="color: #999; font-size: 13px; margin-bottom: 8px">测量时间</div>
            <NDatePicker
              v-model:value="measurementForm.measuredAt"
              type="datetime"
              style="width: 100%"
            />
          </div>

          <div style="margin-bottom: 16px">
            <div style="color: #999; font-size: 13px; margin-bottom: 8px">备注</div>
            <NInput
              v-model:value="measurementForm.remark"
              type="textarea"
              placeholder="请输入备注"
              :rows="3"
            />
          </div>
        </div>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="measurementDrawerVisible = false">取消</NButton>
            <NButton type="primary" :loading="measurementLoading" @click="handleAddMeasurement">确认</NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- Generate Report Drawer -->
    <NDrawer v-model:show="reportDrawerVisible" :width="400" placement="right" closable>
      <NDrawerContent title="生成健康报告" closable>
        <div style="padding: 0 8px">
          <div style="margin-bottom: 16px">
            <div style="color: #999; font-size: 13px; margin-bottom: 8px">报告类型</div>
            <NSelect
              v-model:value="reportForm.reportType"
              :options="reportTypeOptions"
              style="width: 100%"
            />
          </div>

          <div style="margin-bottom: 16px">
            <div style="color: #999; font-size: 13px; margin-bottom: 8px">开始日期</div>
            <NDatePicker
              v-model:value="reportForm.startDate"
              type="date"
              style="width: 100%"
            />
          </div>

          <div style="margin-bottom: 16px">
            <div style="color: #999; font-size: 13px; margin-bottom: 8px">结束日期</div>
            <NDatePicker
              v-model:value="reportForm.endDate"
              type="date"
              style="width: 100%"
            />
          </div>

          <div style="color: #999; font-size: 12px; margin-top: 16px">
            报告将包含：客户基本信息、健康档案摘要、服务统计、健康测量统计等内容。
          </div>
        </div>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="reportDrawerVisible = false">取消</NButton>
            <NButton type="primary" :loading="reportLoading" @click="handleGenerateReport">生成</NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>
  </div>
</template>
