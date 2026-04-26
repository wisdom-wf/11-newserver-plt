<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useMessage, NRate, NInput, NButton, NSpace, NSpin, NResult, NText, NCard } from 'naive-ui';
import { fetchGetSurveyInfo, fetchSubmitSurvey } from '@/service/api/evaluation';

defineOptions({
  name: 'PublicSurvey'
});

const message = useMessage();

// Token from URL
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token') || '';

// State
const isLoading = ref(true);
const isSubmitting = ref(false);
const isSubmitted = ref(false);
const error = ref<string | null>(null);
const surveyInfo = ref<Api.Evaluation.EvaluationInvite | null>(null);

// Form data
const formData = ref({
  serviceScore: 5,
  attitudeScore: 5,
  skillScore: 5,
  punctualityScore: 5,
  environmentScore: 5,
  content: '',
  anonymous: false
});

// Satisfaction options
const satisfactionOptions = [
  { label: '非常满意', value: 'VERY_SATISFIED' },
  { label: '满意', value: 'SATISFIED' },
  { label: '一般', value: 'NEUTRAL' },
  { label: '不满意', value: 'DISSATISFIED' },
  { label: '非常不满意', value: 'VERY_DISSATISFIED' }
];
const selectedSatisfaction = ref('SATISFIED');

// Validation
const isFormValid = computed(() => {
  return (
    formData.value.serviceScore >= 1 &&
    formData.value.attitudeScore >= 1 &&
    formData.value.skillScore >= 1 &&
    formData.value.punctualityScore >= 1
  );
});

// Load survey info
async function loadSurveyInfo() {
  if (!token) {
    error.value = '无效的问卷链接，缺少token参数';
    isLoading.value = false;
    return;
  }

  try {
    isLoading.value = true;
    error.value = null;
    const { data, error: apiError } = await fetchGetSurveyInfo(token);

    if (apiError) {
      error.value = apiError.message || '无效的问卷链接';
      return;
    }

    if (data) {
      surveyInfo.value = data;

      // Check token status
      if (data.tokenStatus === 'COMPLETED') {
        error.value = '该问卷已完成填写，感谢您的反馈';
        return;
      }
      if (data.tokenStatus === 'EXPIRED') {
        error.value = '该问卷链接已过期';
        return;
      }
      if (data.tokenStatus === 'INVALID') {
        error.value = '该问卷链接已失效';
        return;
      }
    }
  } catch (e: any) {
    console.error('Failed to load survey info', e);
    error.value = e?.message || '加载问卷失败';
  } finally {
    isLoading.value = false;
  }
}

// Submit survey
async function handleSubmit() {
  if (!isFormValid.value) {
    message.warning('请完成所有评分');
    return;
  }

  try {
    isSubmitting.value = true;
    const submitData: Api.Evaluation.SurveyForm = {
      serviceScore: formData.value.serviceScore,
      attitudeScore: formData.value.attitudeScore,
      skillScore: formData.value.skillScore,
      punctualityScore: formData.value.punctualityScore,
      environmentScore: formData.value.environmentScore,
      satisfactionLevel: selectedSatisfaction.value,
      content: formData.value.content,
      anonymous: formData.value.anonymous
    };

    const { error: submitError } = await fetchSubmitSurvey(token, submitData);

    if (submitError) {
      message.error(submitError.message || '提交失败');
      return;
    }

    message.success('感谢您的反馈!');
    isSubmitted.value = true;
  } catch (e: any) {
    console.error('Failed to submit survey', e);
    message.error(e?.message || '提交失败');
  } finally {
    isSubmitting.value = false;
  }
}

// Rating labels
const ratingLabels: Record<number, string> = {
  1: '非常不满意',
  2: '不满意',
  3: '一般',
  4: '满意',
  5: '非常满意'
};

onMounted(() => {
  document.title = '满意度评价';
  loadSurveyInfo();
});
</script>

<template>
  <div class="survey-container">
    <!-- Header -->
    <header class="survey-header">
      <h1>满意度评价</h1>
      <p class="subtitle">您的反馈是我们改进的动力</p>
    </header>

    <!-- Loading -->
    <div v-if="isLoading" class="loading-state">
      <NSpin size="large" />
      <p>加载中...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="result-state">
      <NResult status="error" title="问卷不可用" :description="error" />
    </div>

    <!-- Submitted -->
    <div v-else-if="isSubmitted" class="result-state">
      <NResult status="success" title="提交成功" description="感谢您的反馈，祝您生活愉快!" />
    </div>

    <!-- Survey Form -->
    <div v-else-if="surveyInfo" class="survey-content">
      <!-- Service Info Card -->
      <NCard title="服务信息" size="small" class="info-card">
        <NText depth="3">服务商：{{ surveyInfo.providerName || '-' }}</NText>
        <br />
        <NText depth="3">服务人员：{{ surveyInfo.staffName || '-' }}</NText>
        <br />
        <NText depth="3">服务类型：{{ surveyInfo.serviceType || '-' }}</NText>
      </NCard>

      <!-- Rating Section -->
      <NCard title="服务评分" size="small" class="rating-card">
        <div class="rating-item">
          <div class="rating-label">
            <span class="label-text">服务评分</span>
            <span class="label-desc">总体服务质量</span>
          </div>
          <NRate v-model:value="formData.serviceScore" :max="5" />
          <span class="rating-text">{{ ratingLabels[formData.serviceScore] }}</span>
        </div>

        <div class="rating-item">
          <div class="rating-label">
            <span class="label-text">态度评分</span>
            <span class="label-desc">服务人员态度</span>
          </div>
          <NRate v-model:value="formData.attitudeScore" :max="5" />
          <span class="rating-text">{{ ratingLabels[formData.attitudeScore] }}</span>
        </div>

        <div class="rating-item">
          <div class="rating-label">
            <span class="label-text">技能评分</span>
            <span class="label-desc">专业技能水平</span>
          </div>
          <NRate v-model:value="formData.skillScore" :max="5" />
          <span class="rating-text">{{ ratingLabels[formData.skillScore] }}</span>
        </div>

        <div class="rating-item">
          <div class="rating-label">
            <span class="label-text">准时评分</span>
            <span class="label-desc">按时到达情况</span>
          </div>
          <NRate v-model:value="formData.punctualityScore" :max="5" />
          <span class="rating-text">{{ ratingLabels[formData.punctualityScore] }}</span>
        </div>

        <div class="rating-item">
          <div class="rating-label">
            <span class="label-text">环境评分</span>
            <span class="label-desc">服务环境整洁度</span>
          </div>
          <NRate v-model:value="formData.environmentScore" :max="5" />
          <span class="rating-text">{{ ratingLabels[formData.environmentScore] }}</span>
        </div>
      </NCard>

      <!-- Satisfaction Level -->
      <NCard title="满意度等级" size="small" class="satisfaction-card">
        <NSpace vertical>
          <NSpace>
            <NButton
              v-for="option in satisfactionOptions"
              :key="option.value"
              :type="selectedSatisfaction === option.value ? 'primary' : 'default'"
              :tertiary="selectedSatisfaction === option.value"
              size="small"
              @click="selectedSatisfaction = option.value"
            >
              {{ option.label }}
            </NButton>
          </NSpace>
        </NSpace>
      </NCard>

      <!-- Feedback Content -->
      <NCard title="意见反馈" size="small" class="content-card">
        <NInput
          v-model:value="formData.content"
          type="textarea"
          placeholder="请输入您的意见或建议（选填）"
          :rows="4"
          :maxlength="500"
          show-count
        />
      </NCard>

      <!-- Anonymous Option -->
      <div class="anonymous-option">
        <label>
          <input type="checkbox" v-model="formData.anonymous" />
          匿名提交
        </label>
      </div>

      <!-- Submit Button -->
      <div class="submit-section">
        <NButton
          type="primary"
          size="large"
          :loading="isSubmitting"
          :disabled="!isFormValid"
          block
          @click="handleSubmit"
        >
          提交评价
        </NButton>
      </div>
    </div>

    <!-- Footer -->
    <footer class="survey-footer">
      <span>智慧居家养老服务管理平台</span>
    </footer>
  </div>
</template>

<style scoped>
.survey-container {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* Header */
.survey-header {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: white;
  padding: 24px 16px;
  text-align: center;
}

.survey-header h1 {
  font-size: 22px;
  margin: 0 0 8px 0;
  background: linear-gradient(90deg, #5da8ff, #8e9dff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.survey-header .subtitle {
  font-size: 13px;
  margin: 0;
  color: rgba(255, 255, 255, 0.6);
}

/* Loading & Error States */
.loading-state,
.result-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 40px;
}

/* Content */
.survey-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-card {
  background: white;
}

.rating-card {
  background: white;
}

.rating-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.rating-item:last-child {
  border-bottom: none;
}

.rating-label {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.label-text {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.label-desc {
  font-size: 12px;
  color: #999;
}

.rating-text {
  font-size: 12px;
  color: #5da8ff;
  min-width: 60px;
  text-align: right;
}

.satisfaction-card {
  background: white;
}

.content-card {
  background: white;
}

.anonymous-option {
  padding: 8px 16px;
  background: white;
  border-radius: 8px;
}

.anonymous-option label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
}

.anonymous-option input[type="checkbox"] {
  width: 16px;
  height: 16px;
}

.submit-section {
  padding: 8px 0 16px;
}

/* Footer */
.survey-footer {
  padding: 12px;
  text-align: center;
  background: #1a1a2e;
  color: rgba(255, 255, 255, 0.5);
  font-size: 11px;
}
</style>
