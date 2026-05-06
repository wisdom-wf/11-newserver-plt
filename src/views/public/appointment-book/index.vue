<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { NButton, NInput, NForm, NFormItem, NSpin, NResult, NCard, useMessage } from 'naive-ui';

defineOptions({
  name: 'PublicAppointmentBook'
});

const message = useMessage();

// Token from URL
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token') || '';

// Clear token from URL immediately
if (token) {
  window.history.replaceState({}, document.title, window.location.pathname);
}

// State
const isLoading = ref(true);
const isSubmitting = ref(false);
const isSubmitted = ref(false);
const submittedNo = ref('');
const error = ref<string | null>(null);

// Form data
const formData = ref({
  elderName: '',
  elderPhone: '',
  elderIdCard: '',
  elderAddress: ''
});

// Validation errors
const errors = ref<Record<string, string>>({});

// API base URL - 生产环境需带子路径前缀
const apiBase = import.meta.env.DEV ? 'http://localhost:8080' : '/jxy';

function validateForm(): boolean {
  errors.value = {};

  if (!formData.value.elderName.trim()) {
    errors.value.elderName = '请输入姓名';
  }

  if (!formData.value.elderPhone.trim()) {
    errors.value.elderPhone = '请输入手机号';
  } else if (!/^1[3-9]\d{9}$/.test(formData.value.elderPhone)) {
    errors.value.elderPhone = '手机号格式不正确';
  }

  if (formData.value.elderIdCard && !/^\d{17}[\dXx]$/.test(formData.value.elderIdCard)) {
    errors.value.elderIdCard = '身份证号格式不正确（18位）';
  }

  return Object.keys(errors.value).length === 0;
}

async function handleSubmit() {
  if (!validateForm()) return;

  isSubmitting.value = true;
  try {
    const res = await fetch(`${apiBase}/public/appointment/submit?token=${token}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(formData.value)
    });
    const json = await res.json();

    if (json.code === 200) {
      isSubmitted.value = true;
      submittedNo.value = json.data || '';
    } else {
      message.error(json.msg || '提交失败，请重试');
    }
  } catch (e) {
    message.error('网络错误，请重试');
  } finally {
    isSubmitting.value = false;
  }
}

onMounted(async () => {
  if (!token) {
    error.value = '预约链接无效';
    isLoading.value = false;
    return;
  }

  try {
    const res = await fetch(`${apiBase}/public/appointment?token=${token}`);
    const json = await res.json();

    if (json.code === 200 && json.data?.valid) {
      // Token valid
    } else {
      error.value = json.data?.errorMsg || '预约链接无效或已过期';
    }
  } catch (e) {
    error.value = '网络错误，请稍后重试';
  } finally {
    isLoading.value = false;
  }
});
</script>

<template>
  <div class="appointment-book-page">
    <!-- Loading -->
    <div v-if="isLoading" class="loading-container">
      <NSpin size="large" />
      <p>加载中...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-container">
      <NResult status="error" :title="error" description="请检查链接是否正确，或联系工作人员重新获取" />
    </div>

    <!-- Submitted -->
    <div v-else-if="isSubmitted" class="success-container">
      <NResult status="success" title="预约提交成功">
        <template #footer>
          <p class="success-no">预约单号：{{ submittedNo }}</p>
          <p class="success-hint">工作人员将尽快与您联系确认服务安排</p>
        </template>
      </NResult>
    </div>

    <!-- Form -->
    <div v-else class="form-container">
      <div class="form-header">
        <div class="form-icon">🏠</div>
        <h1>智慧养老预约服务</h1>
        <p class="form-subtitle">填写以下信息完成预约</p>
      </div>

      <NCard class="form-card">
        <NForm :model="formData" label-placement="top" :show-feedback="false">
          <NFormItem label="姓名" :validation-status="errors.elderName ? 'error' : undefined">
            <NInput
              v-model:value="formData.elderName"
              placeholder="请输入您的姓名"
              size="large"
              clearable
            />
            <span v-if="errors.elderName" class="error-text">{{ errors.elderName }}</span>
          </NFormItem>

          <NFormItem label="手机号" :validation-status="errors.elderPhone ? 'error' : undefined">
            <NInput
              v-model:value="formData.elderPhone"
              placeholder="请输入手机号码"
              size="large"
              maxlength="11"
              clearable
            />
            <span v-if="errors.elderPhone" class="error-text">{{ errors.elderPhone }}</span>
          </NFormItem>

          <NFormItem label="身份证号（选填）" :validation-status="errors.elderIdCard ? 'error' : undefined">
            <NInput
              v-model:value="formData.elderIdCard"
              placeholder="请输入18位身份证号"
              size="large"
              maxlength="18"
              clearable
            />
            <span v-if="errors.elderIdCard" class="error-text">{{ errors.elderIdCard }}</span>
          </NFormItem>

          <NFormItem label="家庭住址">
            <div class="address-input">
              <span class="address-prefix">陕西省延安市宝塔区</span>
              <NInput
                v-model:value="formData.elderAddress"
                placeholder="请填写街道/小区/门牌号"
                size="large"
                clearable
              />
            </div>
          </NFormItem>
        </NForm>

        <div class="submit-area">
          <NButton
            type="primary"
            size="large"
            block
            :loading="isSubmitting"
            :disabled="isSubmitting"
            @click="handleSubmit"
          >
            {{ isSubmitting ? '提交中...' : '✅ 提交预约' }}
          </NButton>
        </div>

        <p class="footer-hint">
          提交后工作人员将尽快与您联系确认服务安排
        </p>
      </NCard>
    </div>
  </div>
</template>

<style scoped>
.appointment-book-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e8f4fd 0%, #f5f9ff 50%, #ffffff 100%);
  display: flex;
  justify-content: center;
  padding: 0;
}

.loading-container,
.error-container,
.success-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
}

.loading-container p {
  margin-top: 16px;
  color: #999;
}

.form-container {
  width: 100%;
  max-width: 480px;
  padding: 32px 16px 48px;
}

.form-header {
  text-align: center;
  margin-bottom: 24px;
}

.form-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.form-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.form-subtitle {
  font-size: 14px;
  color: #888;
  margin: 0;
}

.form-card {
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.form-card :deep(.n-card__content) {
  padding: 24px 20px;
}

.form-card :deep(.n-form-item) {
  margin-bottom: 20px;
}

.form-card :deep(.n-form-item-label) {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  padding-bottom: 6px;
}

.form-card :deep(.n-input) {
  font-size: 16px;
}

.address-input {
  width: 100%;
}

.address-prefix {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 6px;
  padding-left: 2px;
}

.error-text {
  display: block;
  font-size: 12px;
  color: #e8804f;
  margin-top: 4px;
  padding-left: 2px;
}

.submit-area {
  margin-top: 28px;
}

.submit-area :deep(.n-button) {
  height: 48px;
  font-size: 17px;
  font-weight: 500;
  border-radius: 12px;
}

.footer-hint {
  text-align: center;
  font-size: 13px;
  color: #999;
  margin-top: 16px;
  margin-bottom: 0;
}

.success-no {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.success-hint {
  font-size: 14px;
  color: #888;
  margin-top: 8px;
}
</style>
