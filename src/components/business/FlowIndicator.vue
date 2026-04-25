<script setup lang="ts">
import { computed } from 'vue';

interface Step {
  key: string;
  label: string;
}

interface Props {
  currentStep: string;
  steps?: Step[];
  direction?: 'horizontal' | 'vertical';
}

const props = withDefaults(defineProps<Props>(), {
  steps: () => [
    { key: 'created', label: '订单创建' },
    { key: 'service_started', label: '服务开始' },
    { key: 'log_submitted', label: '日志提交' },
    { key: 'log_approved', label: '日志审核' },
    { key: 'service_completed', label: '服务完成' },
    { key: 'evaluated', label: '已完成评价' }
  ],
  direction: 'horizontal'
});

const emit = defineEmits<{
  (e: 'step-click', step: Step): void;
}>();

function getStepStatus(stepKey: string): 'completed' | 'active' | 'pending' {
  const stepOrder = props.steps.map(s => s.key);
  const currentIndex = stepOrder.indexOf(props.currentStep);
  const stepIndex = stepOrder.indexOf(stepKey);

  if (stepIndex < currentIndex) {
    return 'completed';
  } else if (stepIndex === currentIndex) {
    return 'active';
  } else {
    return 'pending';
  }
}

function isCompleted(stepKey: string): boolean {
  return getStepStatus(stepKey) === 'completed';
}

function isActive(stepKey: string): boolean {
  return getStepStatus(stepKey) === 'active';
}

function isPending(stepKey: string): boolean {
  return getStepStatus(stepKey) === 'pending';
}

function handleStepClick(step: Step) {
  if (isCompleted(step.key) || isActive(step.key)) {
    emit('step-click', step);
  }
}

const currentStepLabel = computed(() => {
  const current = props.steps.find(s => s.key === props.currentStep);
  return current?.label || '';
});
</script>

<template>
  <div class="flow-indicator" :class="direction">
    <div class="flow-steps">
      <template v-for="(step, index) in steps" :key="step.key">
        <div
          class="step-item"
          :class="getStepStatus(step.key)"
          @click="handleStepClick(step)"
        >
          <div class="step-icon">
            <span v-if="isCompleted(step.key)" class="icon-check">✓</span>
            <span v-else-if="isActive(step.key)" class="icon-number">{{ index + 1 }}</span>
            <span v-else class="icon-circle">○</span>
          </div>
          <div class="step-label">{{ step.label }}</div>
        </div>
        <div v-if="index < steps.length - 1" class="step-connector">
          <div class="connector-line" :class="{ completed: isCompleted(step.key) }"></div>
        </div>
      </template>
    </div>
    <div class="flow-hint" v-if="currentStepLabel">
      <span class="hint-label">当前步骤:</span>
      <span class="hint-value">{{ currentStepLabel }}</span>
    </div>
  </div>
</template>

<style scoped>
.flow-indicator {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px 24px;
  color: white;
}

.flow-indicator.vertical {
  padding: 16px;
}

.flow-steps {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flow-indicator.vertical .flow-steps {
  flex-direction: column;
  align-items: flex-start;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.step-item:hover {
  transform: scale(1.05);
}

.step-item.pending {
  opacity: 0.6;
  cursor: default;
}

.step-item.pending:hover {
  transform: none;
}

.step-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.4);
}

.step-item.completed .step-icon {
  background: #38ef7d;
  border-color: #38ef7d;
  color: white;
}

.step-item.active .step-icon {
  background: white;
  color: #667eea;
  border-color: #38ef7d;
  box-shadow: 0 0 12px rgba(56, 239, 125, 0.5);
}

.step-item.pending .step-icon {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.5);
}

.icon-check {
  font-size: 16px;
}

.icon-number {
  font-size: 14px;
}

.icon-circle {
  font-size: 16px;
}

.step-label {
  font-size: 12px;
  text-align: center;
  max-width: 80px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.step-item.completed .step-label {
  color: rgba(255, 255, 255, 0.8);
}

.step-item.active .step-label {
  color: white;
  font-weight: 600;
}

.step-item.pending .step-label {
  color: rgba(255, 255, 255, 0.5);
}

.step-connector {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
  min-width: 40px;
}

.connector-line {
  width: 100%;
  height: 2px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 1px;
}

.connector-line.completed {
  background: #38ef7d;
}

.flow-hint {
  margin-top: 16px;
  text-align: center;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.hint-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-right: 8px;
}

.hint-value {
  font-size: 14px;
  font-weight: 600;
  color: white;
}
</style>
