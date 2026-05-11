<script setup lang="ts">
import { ref, h, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
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
import LazyImage from '@/components/common/lazy-image.vue';
import type { DataTableColumns, UploadFile } from 'naive-ui';
import {
  fetchGetElderList,
  fetchGetElder,
  fetchGetElderHealth,
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
  fetchUpdateElder,
  fetchGetElderDevices,
  fetchBindDevice,
  fetchUnbindDevice,
  fetchGetDeviceBySn
} from '@/service/api';
import { useAuth } from '@/hooks/business/auth';

defineOptions({
  name: 'BusinessHealthArchive'
});

const message = useMessage();
const { hasAuth } = useAuth();
const route = useRoute();

// 语音播报（适老化：AI建议转语音）
let currentAudio: HTMLAudioElement | null = null;
let isPlaying = ref(false);
function playAudio(audioUrl: string) {
  if (currentAudio && isPlaying.value) {
    currentAudio.pause();
    currentAudio = null;
    isPlaying.value = false;
    return;
  }
  if (currentAudio) {
    currentAudio.pause();
    currentAudio = null;
  }
  isPlaying.value = true;
  currentAudio = new Audio(audioUrl);
  currentAudio.play().catch(() => {
    isPlaying.value = false;
    message.warning('您的浏览器不支持自动播放音频');
  });
  currentAudio.onended = () => { isPlaying.value = false; };
}

// Elder selection
const elderOptions = ref<{ label: string; value: string }[]>([]);
const selectedElderId = ref<string>('');
const selectedElder = ref<Api.Elder.Elder | null>(null);
const elderHealth = ref<Api.Elder.ElderHealth | null>(null);
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

// Device management
const elderDevices = ref<Api.Device.Binding[]>([]);
const bindDeviceVisible = ref(false);
const bindDeviceLoading = ref(false);
const bindDeviceForm = ref({
  deviceSn: '',
  measurementType: 'BLOOD_PRESSURE',
  remark: ''
});
const bindDeviceInfo = ref<Api.Device.Device | null>(null);

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

// 横向Tab导航配置
const archiveTabList = [
  { name: 'overview', label: '健康总览' },
  { name: 'measurements', label: '健康测量' },
  { name: 'devices', label: '设备管理' },
  { name: 'reports', label: '健康报告' },
  { name: 'suggestions', label: 'AI健康建议' },
];

// Active tab
const activeTab = ref('overview');

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
    const { data } = await fetchGetElderList({ page: 1, pageSize: 1000 });
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
    elderHealth.value = null;
    return;
  }
  elderLoading.value = true;
  try {
    const [elderRes, healthRes] = await Promise.all([
      fetchGetElder(selectedElderId.value),
      fetchGetElderHealth(selectedElderId.value),
    ]);
    if (elderRes.error) {
      message.error(elderRes.error.message || '获取客户信息失败');
      return;
    }
    selectedElder.value = elderRes.data;
    elderHealth.value = healthRes.data || null;
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
  {
    title: '封面',
    key: 'coverImageUrl',
    width: 80,
    render: row => row.coverImageUrl
      ? h(LazyImage, { src: row.coverImageUrl, width: 48, height: 48, fit: 'cover' })
      : h('span', { style: 'color:#ccc' }, '无')
  },
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

// Get fall risk display name
function getFallRiskName(fallRisk?: number): string {
  switch (fallRisk) {
    case 0:
      return '低风险';
    case 1:
      return '中风险';
    case 2:
      return '高风险';
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
  loadElderDevices();
}

// ========== 设备管理 ==========

async function loadElderDevices() {
  if (!selectedElderId.value) return;
  try {
    const { data } = await fetchGetElderDevices(selectedElderId.value);
    elderDevices.value = data || [];
  } catch {
    elderDevices.value = [];
  }
}

async function handleQueryDevice() {
  if (!bindDeviceForm.value.deviceSn) {
    message.warning('请输入设备序列号');
    return;
  }
  try {
    const { data } = await fetchGetDeviceBySn(bindDeviceForm.value.deviceSn);
    bindDeviceInfo.value = data;
    if (!data) {
      message.warning('未找到该设备，请确认序列号是否正确');
    }
  } catch {
    bindDeviceInfo.value = null;
    message.warning('未找到该设备');
  }
}

async function handleBindDevice() {
  if (!bindDeviceForm.value.deviceSn || !selectedElderId.value) {
    message.warning('请填写完整信息');
    return;
  }
  bindDeviceLoading.value = true;
  try {
    await fetchBindDevice({
      deviceSn: bindDeviceForm.value.deviceSn,
      elderId: selectedElderId.value,
      measurementType: bindDeviceForm.value.measurementType,
      remark: bindDeviceForm.value.remark
    });
    message.success('设备绑定成功');
    bindDeviceVisible.value = false;
    bindDeviceForm.value = { deviceSn: '', measurementType: 'BLOOD_PRESSURE', remark: '' };
    bindDeviceInfo.value = null;
    await loadElderDevices();
  } catch (e: any) {
    message.error(e?.message || '绑定失败');
  } finally {
    bindDeviceLoading.value = false;
  }
}

async function handleUnbindDevice(bindingId: string) {
  try {
    await fetchUnbindDevice(bindingId);
    message.success('已解绑');
    await loadElderDevices();
  } catch (e: any) {
    message.error(e?.message || '解绑失败');
  }
}

onMounted(async () => {
  await loadElders();
  await loadRecentElders();
  // 从URL参数读取elderId
  if (route.query.elderId) {
    const elderId = route.query.elderId as string;
    selectedElderId.value = elderId;
    await handleElderChange(elderId);
  }
});
</script>

<template>
  <div class="health-archive-page">
    <!-- ===== 档案顶栏：老人信息概览 ===== -->
    <div v-if="selectedElder" class="archive-header">
      <div class="archive-header-left">
        <LazyImage
          :src="selectedElder.photoUrl"
          :width="64"
          :height="64"
          fit="cover"
          style="flex-shrink:0; border-radius:50%; border: 3px solid #1E3A5F;"
        />
        <div class="elder-info-stack">
          <div class="elder-name-row">
            <span class="elder-name">{{ selectedElder.name || '未知' }}</span>
            <NTag v-if="selectedElder.careLevelName" size="medium" style="background:#EEF2F7; color:#1E3A5F; font-weight:600; border:none;">
              {{ selectedElder.careLevelName }}
            </NTag>
          </div>
          <div class="elder-meta">
            <span>{{ selectedElder.age || '-' }}岁</span>
            <span class="meta-sep">|</span>
            <span>{{ selectedElder.genderName || '-' }}</span>
            <span class="meta-sep">|</span>
            <span>{{ selectedElder.phone || '-' }}</span>
          </div>
          <!-- 关键风险标签 -->
          <div v-if="measurementStats.length > 0" class="elder-risk-tags">
            <span
              v-for="stat in measurementStats.filter(s => s.alertStatus !== 'NORMAL').slice(0,3)"
              :key="stat.measurementType"
              :class="['risk-tag', stat.alertStatus === 'ALERT' ? 'risk-high' : 'risk-warn']"
            >
              {{ stat.measurementTypeName }} {{ stat.alertStatus === 'ALERT' ? '⚠异常' : '⚡预警' }}
            </span>
          </div>
        </div>
      </div>
      <div class="archive-header-right">
        <NSelect
          v-model:value="selectedElderId"
          :options="elderOptions"
          placeholder="切换客户"
          filterable
          clearable
          size="medium"
          style="width: 240px"
          @update:value="handleElderChange"
        />
        <NButton type="primary" size="large" @click="reportDrawerVisible = true">
          生成报告
        </NButton>
      </div>
    </div>

    <NEmpty v-else-if="!elderLoading" description="请从上方选择一位客户查看健康档案" style="margin: 60px 0" />

    <!-- ===== 横向Tab导航 ===== -->
    <div v-if="selectedElderId" class="archive-tabs-wrapper">
      <div class="archive-tabs">
        <button
          v-for="tab in archiveTabList"
          :key="tab.name"
          :class="['tab-btn', { active: activeTab === tab.name }]"
          @click="activeTab = tab.name"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <!-- ===== Tab内容区 ===== -->
    <div v-if="selectedElderId" class="archive-content">
      <!-- 健康总览 Tab -->
      <div v-show="activeTab === 'overview'">
        <!-- 关键指标4宫格 -->
        <div v-if="measurementStats.length > 0" class="stat-grid">
          <div
            v-for="stat in measurementStats"
            :key="stat.measurementType"
            :class="['stat-card', stat.alertStatus !== 'NORMAL' ? 'stat-alert' : '']"
          >
            <div class="stat-label">{{ stat.measurementTypeName }}</div>
            <div class="stat-value">
              {{ stat.latestValue || '-' }}
              <span class="stat-unit">{{ stat.measurementUnit }}</span>
            </div>
            <div class="stat-range">{{ stat.normalRange || '' }}</div>
            <div v-if="stat.alertStatus !== 'NORMAL'" :class="['stat-badge', stat.alertStatus === 'ALERT' ? 'badge-danger' : 'badge-warn']">
              {{ stat.alertStatus === 'ALERT' ? '异常' : '预警' }}
            </div>
          </div>
        </div>

        <!-- 下方两栏：基础信息 + 风险预警 -->
        <div class="overview-two-col">
          <NCard title="基础信息" size="small" :bordered="true" style="border-left: 4px solid #1E3A5F;">
            <div class="info-grid">
              <div class="info-item">
                <div class="info-label">血型</div>
                <div class="info-value">{{ elderHealth?.bloodTypeName || '-' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">身高 / 体重</div>
                <div class="info-value">{{ elderHealth?.height ? elderHealth.height + 'cm' : '-' }} / {{ elderHealth?.weight ? elderHealth.weight + 'kg' : '-' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">ADL评分</div>
                <div class="info-value">{{ elderHealth?.adlScore ?? '-' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">MMSE评分</div>
                <div class="info-value">{{ elderHealth?.mmseScore ?? '-' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">跌倒风险</div>
                <div :class="['info-value', elderHealth?.fallRisk === 2 ? 'val-danger' : elderHealth?.fallRisk === 1 ? 'val-warn' : '']">
                  {{ getFallRiskName(elderHealth?.fallRisk) }}
                </div>
              </div>
              <div class="info-item">
                <div class="info-label">护理类型</div>
                <div class="info-value">{{ selectedElder?.careTypeName || selectedElder?.careType || '-' }}</div>
              </div>
            </div>
          </NCard>

          <NCard title="风险预警" size="small" :bordered="true" style="border-left: 4px solid #B91C1C;">
            <template #header-extra>
              <span style="color:#B91C1C; font-size:20px">⚠️</span>
            </template>
            <div v-if="careSuggestions && careSuggestions.riskAlerts && careSuggestions.riskAlerts.length > 0">
              <div
                v-for="(alert, idx) in careSuggestions.riskAlerts"
                :key="idx"
                class="risk-alert-item"
              >
                <span style="color:#B91C1C; margin-right:6px">●</span>
                {{ alert }}
              </div>
            </div>
            <NEmpty v-else-if="!careSuggestions" description="暂无建议数据，请确保该客户有完整的健康档案" />
            <div v-else style="color:#2E7D4A; font-size:15px; padding: 8px 0;">
              ✅ 暂无高风险预警
            </div>
          </NCard>
        </div>
      </div>

      <!-- 健康测量 Tab -->
      <div v-show="activeTab === 'measurements'">
        <div class="tab-toolbar">
          <NButton type="primary" size="large" @click="measurementDrawerVisible = true">
            + 新增测量记录
          </NButton>
          <NButton v-if="measurementStats.length > 0" size="large" @click="activeTab = 'overview'">
            ← 查看健康总览
          </NButton>
        </div>

        <div v-if="measurementStats.length > 0" class="stat-grid">
          <div
            v-for="stat in measurementStats"
            :key="stat.measurementType"
            :class="['stat-card', stat.alertStatus !== 'NORMAL' ? 'stat-alert' : '']"
          >
            <div class="stat-label">{{ stat.measurementTypeName }}</div>
            <div class="stat-value">
              {{ stat.latestValue || '-' }}
              <span class="stat-unit">{{ stat.measurementUnit }}</span>
            </div>
            <div class="stat-count">共 {{ stat.count }} 条</div>
          </div>
        </div>

        <NDataTable
          :columns="measurementColumns"
          :data="measurements"
          :loading="measurementsLoading"
          :row-key="(row: Api.Elder.HealthMeasurement) => row.measurementId"
          style="margin-top: 16px"
        />
        <NEmpty v-if="!measurementsLoading && measurements.length === 0" description="暂无测量记录" style="margin-top: 40px" />
      </div>

      <!-- 设备管理 Tab -->
      <div v-show="activeTab === 'devices'">
        <div class="tab-toolbar">
          <NButton type="primary" size="large" @click="bindDeviceVisible = true">+ 绑定设备</NButton>
        </div>

        <div v-if="elderDevices.length === 0" style="text-align:center; padding:60px; color:#94A3B8; font-size:16px">
          暂未绑定任何设备，点击「绑定设备」扫码开始
        </div>

        <div v-else class="device-list">
          <div v-for="device in elderDevices" :key="device.bindingId" class="device-card">
            <div class="device-info">
              <div class="device-name">{{ device.measurementTypeName || device.measurementType }}</div>
              <div class="device-sn">序列号：{{ device.deviceSn || '-' }}</div>
              <div class="device-bind-time">绑定时间：{{ device.bindTime || '-' }}</div>
            </div>
            <div class="device-actions">
              <NTag :type="device.deviceStatus === 'ACTIVE' ? 'success' : 'default'" size="medium">
                {{ device.deviceStatus === 'ACTIVE' ? '在线' : '离线' }}
              </NTag>
              <NButton size="medium" type="error" ghost @click="handleUnbindDevice(device.bindingId)">解绑</NButton>
            </div>
          </div>
        </div>
      </div>

      <!-- 健康报告 Tab -->
      <div v-show="activeTab === 'reports'">
        <div class="tab-toolbar">
          <NButton type="primary" size="large" @click="reportDrawerVisible = true">+ 生成报告</NButton>
        </div>

        <div v-if="reports.length > 0" class="report-grid">
          <div v-for="report in reports" :key="report.reportId" class="report-card">
            <div class="report-cover">
              <LazyImage
                v-if="report.coverImageUrl"
                :src="report.coverImageUrl"
                :width="300"
                :height="120"
                fit="cover"
              />
              <div v-else class="report-cover-placeholder">
                <span style="font-size:32px">📋</span>
                <span style="font-size:13px; color:#94A3B8">{{ report.reportTypeName }}</span>
              </div>
            </div>
            <div class="report-info">
              <div class="report-title">{{ report.title || report.reportTypeName }}</div>
              <div class="report-date">{{ report.reportDate }}</div>
              <div class="report-actions">
                <NButton size="small" type="primary" @click="handleDownloadPdf(report.reportId)">下载PDF</NButton>
                <NButton v-if="hasAuth('elder:list:delete')" size="small" type="error" ghost @click="handleDeleteReport(report.reportId)">删除</NButton>
              </div>
            </div>
          </div>
        </div>
        <NEmpty v-else-if="!reportsLoading" description="暂无报告，点击「生成报告」创建" style="margin-top:40px" />
      </div>

      <!-- AI健康建议 Tab -->
      <div v-show="activeTab === 'suggestions'">
        <NSpin :show="suggestionsLoading">
          <!-- 护理建议 -->
          <NCard v-if="careSuggestions" size="large" :bordered="true" style="border-left: 4px solid #1E3A5F; margin-bottom: 16px">
            <template #header>
              <div style="display:flex; align-items:center; gap:10px">
                <span style="font-size:18px; font-weight:700">护理建议</span>
                <NTag type="info">{{ careSuggestions.careLevelSuggestion }}</NTag>
              </div>
            </template>
            <template #header-extra>
              <NButton
                v-if="careSuggestions.audioUrl"
                type="warning"
                size="medium"
                round
                @click="playAudio(careSuggestions.audioUrl)"
              >
                {{ isPlaying ? '⏸ 暂停播报' : '🔊 语音播报' }}
              </NButton>
            </template>

            <div v-if="careSuggestions.riskAlerts && careSuggestions.riskAlerts.length > 0" style="margin-bottom:16px">
              <div class="alert-box alert-danger">
                <span style="margin-right:6px; font-size:18px">⚠️</span>
                <span style="font-weight:600">风险预警 ({{ careSuggestions.riskAlerts.length }})</span>
                <div v-for="(alert, idx) in careSuggestions.riskAlerts" :key="idx" style="margin-top:6px; font-size:15px">
                  {{ alert }}
                </div>
              </div>
            </div>

            <div v-if="careSuggestions.suggestions && careSuggestions.suggestions.length > 0">
              <div
                v-for="(s, idx) in careSuggestions.suggestions"
                :key="idx"
                class="suggestion-item"
              >
                <div class="suggestion-header">
                  <NTag :type="s.priority <= 1 ? 'error' : s.priority <= 2 ? 'warning' : 'info'" size="medium">
                    {{ s.typeName }}
                  </NTag>
                  <span class="suggestion-basis">依据: {{ s.basis }}</span>
                </div>
                <div class="suggestion-content">{{ s.content }}</div>
              </div>
            </div>
            <NEmpty v-else description="暂无护理建议" />
          </NCard>

          <!-- 就医建议 -->
          <NCard v-if="medicalSuggestions" size="large" :bordered="true" style="border-left: 4px solid #E8833A">
            <template #header>
              <div style="display:flex; align-items:center; gap:10px">
                <span style="font-size:18px; font-weight:700">就医建议</span>
                <NTag
                  :type="medicalSuggestions.urgencyLevel === 'URGENT' ? 'error' : medicalSuggestions.urgencyLevel === 'WARNING' ? 'warning' : 'success'"
                  size="medium"
                >
                  {{ medicalSuggestions.urgencyLevelName }}
                </NTag>
              </div>
            </template>

            <div v-if="medicalSuggestions.symptoms && medicalSuggestions.symptoms.length > 0" style="margin-bottom:12px">
              <div style="font-size:13px; color:#5A6478; margin-bottom:6px">异常症状:</div>
              <NTag v-for="(s, idx) in medicalSuggestions.symptoms" :key="idx" type="warning" size="medium" style="margin-right:6px; margin-bottom:4px">
                {{ s }}
              </NTag>
            </div>

            <div v-if="medicalSuggestions.suggestedDepartment" style="margin-bottom:12px; font-size:15px">
              <span style="color:#5A6478">建议就诊科室: </span>
              <span style="font-weight:600; color:#1E3A5F">{{ medicalSuggestions.suggestedDepartment }}</span>
            </div>

            <div v-if="medicalSuggestions.suggestions && medicalSuggestions.suggestions.length > 0">
              <div
                v-for="(s, idx) in medicalSuggestions.suggestions"
                :key="idx"
                class="suggestion-item"
              >
                <div class="suggestion-header">
                  <NTag :type="s.priority <= 1 ? 'error' : s.priority <= 2 ? 'warning' : 'info'" size="medium">
                    {{ s.typeName }}
                  </NTag>
                  <span class="suggestion-basis">依据: {{ s.basis }}</span>
                </div>
                <div class="suggestion-content">{{ s.content }}</div>
              </div>
            </div>
            <NEmpty v-else description="暂无就医建议" />
          </NCard>

          <NEmpty v-if="!careSuggestions && !medicalSuggestions && !suggestionsLoading" description="暂无AI建议，请确保该客户有完整的健康档案和测量记录" style="margin-top:40px" />
        </NSpin>
      </div>
    </div>

    <!-- ===== 生成报告抽屉 ===== -->

    <!-- Add Measurement Drawer -->
    <NDrawer v-model:show="measurementDrawerVisible" :width="400" placement="right" closable>
      <NDrawerContent title="新增测量记录" closable>
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

    <!-- 绑定设备弹窗 -->
    <NModal v-model:show="bindDeviceVisible" preset="card" title="绑定设备" style="width: 480px">
      <div style="margin-bottom: 16px">
        <div style="color: #999; font-size: 13px; margin-bottom: 8px">设备序列号</div>
        <NSpace>
          <NInput v-model:value="bindDeviceForm.deviceSn" placeholder="输入设备序列号" style="width: 280px" />
          <NButton @click="handleQueryDevice">查询</NButton>
        </NSpace>
      </div>

      <div v-if="bindDeviceInfo" style="background: #f5f5f5; padding: 12px; border-radius: 4px; margin-bottom: 16px">
        <div style="font-size: 13px"><span style="color: #999">设备类型：</span>{{ bindDeviceInfo.deviceType }}</div>
        <div style="font-size: 13px; margin-top: 4px"><span style="color: #999">设备名称：</span>{{ bindDeviceInfo.deviceName || '-' }}</div>
        <div style="font-size: 13px; margin-top: 4px"><span style="color: #999">制造商：</span>{{ bindDeviceInfo.manufacturer || '-' }}</div>
        <div style="font-size: 13px; margin-top: 4px"><span style="color: #999">状态：</span>{{ bindDeviceInfo.statusText }}</div>
      </div>

      <div style="margin-bottom: 16px">
        <div style="color: #999; font-size: 13px; margin-bottom: 8px">测量类型</div>
        <NSelect
          v-model:value="bindDeviceForm.measurementType"
          :options="measurementTypeOptions"
          style="width: 100%"
        />
      </div>

      <div style="margin-bottom: 16px">
        <div style="color: #999; font-size: 13px; margin-bottom: 8px">备注（可选）</div>
        <NInput v-model:value="bindDeviceForm.remark" placeholder="设备用途说明" />
      </div>

      <div style="color: #999; font-size: 12px; margin-bottom: 16px">
        绑定后，设备联网时会自动向系统推送测量数据，数据将自动记录到健康档案中。未绑定设备时，所有指标仍可手动填写。
      </div>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="bindDeviceVisible = false">取消</NButton>
          <NButton type="primary" :loading="bindDeviceLoading" @click="handleBindDevice">确认绑定</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
/* ===== 档案页面整体 ===== */
.health-archive-page {
  padding: 0 0 40px;
}

/* ===== 顶栏：老人信息概览 ===== */
.archive-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(30,58,95,0.08);
}
.archive-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}
.elder-info-stack {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}
.elder-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.elder-name {
  font-size: 22px;
  font-weight: 700;
  color: #1E3A5F;
}
.elder-meta {
  font-size: 14px;
  color: #5A6478;
}
.meta-sep {
  margin: 0 6px;
  color: #CBD5E1;
}
.elder-risk-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 4px;
}
.risk-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 600;
}
.risk-high {
  background: #FEF2F2;
  color: #B91C1C;
  border: 1px solid #FECACA;
}
.risk-warn {
  background: #FFF4EC;
  color: #C0620A;
  border: 1px solid #FDDCB8;
}
.archive-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

/* ===== 横向Tab导航 ===== */
.archive-tabs-wrapper {
  background: #fff;
  border-radius: 12px;
  padding: 8px 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(30,58,95,0.08);
}
.archive-tabs {
  display: flex;
  gap: 4px;
}
.tab-btn {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #5A6478;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}
.tab-btn:hover {
  background: #EEF2F7;
  color: #1E3A5F;
}
.tab-btn.active {
  background: #1E3A5F;
  color: #fff;
  font-weight: 600;
}

/* ===== Tab内容区 ===== */
.archive-content {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 2px 8px rgba(30,58,95,0.08);
  min-height: 400px;
}

/* ===== 指标4宫格 ===== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: #F5F7FA;
  border-radius: 12px;
  padding: 16px 20px;
  position: relative;
  border: 1.5px solid #E2E8F0;
  transition: all 0.2s;
}
.stat-card:hover {
  box-shadow: 0 4px 16px rgba(30,58,95,0.12);
  transform: translateY(-1px);
}
.stat-alert {
  background: #FFF4EC;
  border-color: #FDDCB8;
}
.stat-label {
  font-size: 13px;
  color: #5A6478;
  margin-bottom: 8px;
  font-weight: 500;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1E3A5F;
  line-height: 1;
  margin-bottom: 4px;
}
.stat-unit {
  font-size: 13px;
  font-weight: 400;
  color: #5A6478;
}
.stat-range {
  font-size: 12px;
  color: #94A3B8;
}
.stat-count {
  font-size: 12px;
  color: #94A3B8;
}
.stat-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 11px;
  font-weight: 700;
  margin-top: 6px;
}
.badge-danger { background: #FEE2E2; color: #B91C1C; }
.badge-warn { background: #FEF3C7; color: #C0620A; }

/* ===== 基础信息+风险预警两栏 ===== */
.overview-two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media (max-width: 900px) {
  .overview-two-col { grid-template-columns: 1fr; }
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 16px;
}
.info-item { display: flex; flex-direction: column; gap: 2px; }
.info-label {
  font-size: 12px;
  color: #94A3B8;
  font-weight: 500;
}
.info-value {
  font-size: 15px;
  font-weight: 600;
  color: #1A1A1A;
}
.val-danger { color: #B91C1C !important; }
.val-warn { color: #C0620A !important; }

.risk-alert-item {
  font-size: 15px;
  color: #1A1A1A;
  padding: 6px 0;
  border-bottom: 1px solid #F0F0F0;
}
.risk-alert-item:last-child { border-bottom: none; }

/* ===== Tab工具栏 ===== */
.tab-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

/* ===== 设备列表卡片 ===== */
.device-list { display: flex; flex-direction: column; gap: 12px; }
.device-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #F5F7FA;
  border-radius: 12px;
  border: 1.5px solid #E2E8F0;
}
.device-name { font-size: 16px; font-weight: 600; color: #1E3A5F; margin-bottom: 4px; }
.device-sn { font-size: 13px; color: #5A6478; }
.device-bind-time { font-size: 12px; color: #94A3B8; margin-top: 2px; }
.device-actions { display: flex; align-items: center; gap: 10px; }

/* ===== 报告网格 ===== */
.report-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}
.report-card {
  border-radius: 12px;
  overflow: hidden;
  border: 1.5px solid #E2E8F0;
  background: #fff;
  transition: all 0.2s;
}
.report-card:hover {
  box-shadow: 0 4px 16px rgba(30,58,95,0.14);
  transform: translateY(-2px);
}
.report-cover { height: 120px; overflow: hidden; background: #EEF2F7; }
.report-cover-placeholder {
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: linear-gradient(135deg, #EEF2F7 0%, #E2E8F0 100%);
}
.report-info { padding: 14px 16px; }
.report-title { font-size: 15px; font-weight: 600; color: #1A1A1A; margin-bottom: 4px; }
.report-date { font-size: 12px; color: #94A3B8; margin-bottom: 10px; }
.report-actions { display: flex; gap: 8px; }

/* ===== 建议项 ===== */
.suggestion-item {
  padding: 14px 0;
  border-bottom: 1px solid #F0F0F0;
}
.suggestion-item:last-child { border-bottom: none; }
.suggestion-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.suggestion-basis { font-size: 12px; color: #94A3B8; }
.suggestion-content { font-size: 15px; color: #1A1A1A; line-height: 1.6; }

/* ===== 告警框 ===== */
.alert-box {
  border-radius: 10px;
  padding: 14px 16px;
  font-size: 14px;
}
.alert-danger {
  background: #FEF2F2;
  border: 1.5px solid #FECACA;
  color: #B91C1C;
}
</style>
