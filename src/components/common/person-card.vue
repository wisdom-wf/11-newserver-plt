<script setup lang="ts">
/**
 * 照片卡片组件 - 带照片的人员信息卡片
 * 支持：照片上传、评分、状态标签、保险状态、月度/季度之星
 * 布局：左侧大照片(9:16) + 右侧信息区（姓名/电话/状态标签/额外信息）
 */
import { computed, ref } from 'vue';
import { NImage, NAvatar, NButton, NTag, NProgress } from 'naive-ui';

const imageError = ref(false);
function handleImageError() { imageError.value = true; }
function handleImageLoad() { imageError.value = false; }

interface ExtraInfo {
  label: string;
  value: string;
  color?: string;
}

interface Props {
  photoUrl?: string;
  name: string;
  subtitle?: string;
  extraInfo?: ExtraInfo[];
  indexValue?: number;
  indexLabel?: string;
  selected?: boolean;
  showUploadBtn?: boolean;
  clickText?: string;
  photoWidth?: number;
  scale?: number;
  cardMinWidth?: number;
  // 保险状态：0=未参保 1=正在参保 2=已参保 3=已过期
  insuranceStatus?: number;
  // 月度之星
  monthlyStar?: boolean;
  // 季度之星
  quarterlyStar?: boolean;
  // 工号
  staffNo?: string;
}

const props = withDefaults(defineProps<Props>(), {
  photoUrl: '',
  name: '',
  subtitle: '',
  extraInfo: undefined,
  indexValue: undefined,
  indexLabel: '评分',
  selected: false,
  showUploadBtn: true,
  clickText: '',
  photoWidth: 85,
  scale: 1,
  cardMinWidth: 300,
  insuranceStatus: undefined,
  monthlyStar: false,
  quarterlyStar: false,
  staffNo: ''
});

const emit = defineEmits<{
  (e: 'click'): void;
  (e: 'upload-click'): void;
  (e: 'photo-upload', file: File): void;
}>();

const computedPhotoHeight = computed(() => Math.round(props.photoWidth * (16 / 9) * props.scale));

function getIndexColor(index?: number): string {
  if (index === undefined) return '#999';
  if (index >= 80) return '#52c41a';
  if (index >= 60) return '#fa8c16';
  return '#f5222d';
}

function getInsuranceStatusLabel(val?: number): string {
  if (val === 0) return '未参保';
  if (val === 1) return '参保中';
  if (val === 2) return '已参保';
  if (val === 3) return '已过期';
  return '';
}

function getInsuranceStatusColor(val?: number): string {
  if (val === 0) return '#999';
  if (val === 1) return '#52c41a';
  if (val === 2) return '#1890ff';
  if (val === 3) return '#ff4d4f';
  return '#999';
}

const fileInputRef = ref<HTMLInputElement | null>(null);

function triggerUpload() { fileInputRef.value?.click(); }
function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement;
  const file = target.files?.[0];
  if (file) { emit('photo-upload', file); }
  target.value = '';
}
function handleCardClick() { emit('click'); }
function handleUploadClick(e: Event) { e.stopPropagation(); emit('upload-click'); }
function handleActionClick(e: Event) { e.stopPropagation(); emit('click'); }
</script>

<template>
  <div
    :style="{
      display: 'flex',
      flexDirection: 'column',
      border: selected ? '2px solid #18a058' : '1px solid #e8e8e8',
      borderRadius: '10px',
      padding: '14px',
      cursor: 'pointer',
      transition: 'all 0.2s',
      background: selected ? '#f6ffed' : '#fff',
      width: cardMinWidth + 'px',
      minWidth: cardMinWidth + 'px',
      flexShrink: 0,
      boxSizing: 'border-box',
      boxShadow: '0 1px 4px rgba(0,0,0,0.06)'
    }"
    @click="handleCardClick"
  >
    <!-- 顶部：照片 + 右侧信息 -->
    <div style="display: flex; gap: 14px; margin-bottom: 10px; align-items: flex-start">
      <!-- 照片区域 -->
      <div
        :style="{
          position: 'relative',
          width: photoWidth + 'px',
          height: computedPhotoHeight + 'px',
          flexShrink: 0,
          background: '#f0f5ff',
          borderRadius: '8px',
          overflow: 'hidden'
        }"
      >
        <NImage
          v-if="photoUrl && !imageError"
          :src="photoUrl"
          :style="{ width: photoWidth + 'px', height: computedPhotoHeight + 'px', objectFit: 'cover' }"
          :preview-src="photoUrl"
          :show-toolbar="false"
          @error="handleImageError"
          @load="handleImageLoad"
        />
        <NAvatar
          v-if="!photoUrl || imageError"
          :size="Math.min(photoWidth, computedPhotoHeight) - 10"
          round
          :style="{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', background: '#5394ec', fontSize: '22px' }"
        >
          {{ name?.charAt(0) || '?' }}
        </NAvatar>
        <!-- 上传按钮 -->
        <NButton
          v-if="showUploadBtn"
          size="tiny"
          :style="{
            position: 'absolute', bottom: '4px', right: '4px', opacity: 0.85,
            width: '26px', height: '26px', minWidth: '26px', padding: 0
          }"
          circle type="primary"
          @click.stop="triggerUpload"
        >+</NButton>
        <input ref="fileInputRef" type="file" accept="image/*" style="display: none" @change="handleFileChange" />
        <!-- 月度/季度之星标签 -->
        <div v-if="monthlyStar || quarterlyStar" :style="{ position: 'absolute', top: '4px', left: '4px' }">
          <div v-if="monthlyStar" style="background: #fa8c16; color: #fff; font-size: 9px; font-weight: 700; padding: 1px 5px; border-radius: 3px">月度之星</div>
          <div v-if="quarterlyStar" style="background: #722ed1; color: #fff; font-size: 9px; font-weight: 700; padding: 1px 5px; border-radius: 3px; margin-top: 2px">季度之星</div>
        </div>
      </div>

      <!-- 右侧信息 -->
      <div style="flex: 1; display: flex; flex-direction: column; justify-content: space-between; overflow: hidden; min-width: 0">
        <!-- 姓名 + 工号 -->
        <div>
          <div style="font-size: 16px; font-weight: 700; color: #1a1a1a; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 2px">
            {{ name || '未知' }}
          </div>
          <div v-if="staffNo" style="font-size: 11px; color: #999; margin-bottom: 4px">工号 {{ staffNo }}</div>
          <div v-if="subtitle" style="font-size: 13px; color: #666; margin-bottom: 6px">{{ subtitle }}</div>
        </div>

        <!-- 状态标签行 -->
        <div style="display: flex; flex-wrap: wrap; gap: 5px; align-items: center">
          <!-- 保险状态 -->
          <NTag
            v-if="insuranceStatus !== undefined && insuranceStatus !== null"
            size="tiny" round
            :color="{ color: getInsuranceStatusColor(insuranceStatus) + '18', textColor: getInsuranceStatusColor(insuranceStatus), borderColor: getInsuranceStatusColor(insuranceStatus) }"
            style="font-size: 11px"
          >
            {{ getInsuranceStatusLabel(insuranceStatus) }}
          </NTag>
          <!-- 额外信息标签 -->
          <template v-if="extraInfo && extraInfo.length > 0">
            <NTag
              v-for="(item, i) in extraInfo"
              :key="i"
              size="tiny" round
              :style="{ color: item.color || '#666', borderColor: item.color ? item.color + '40' : '#e5e5e5', background: item.color ? item.color + '10' : '#f5f5f5' }"
              style="font-size: 11px"
            >
              {{ item.value }}
            </NTag>
          </template>
        </div>
      </div>
    </div>

    <!-- 底部：评分进度条 -->
    <div v-if="indexValue !== undefined" style="margin-top: 4px">
      <div style="display: flex; justify-content: space-between; font-size: 12px; color: #888; margin-bottom: 5px">
        <span>{{ indexLabel }}</span>
        <span :style="{ color: getIndexColor(indexValue), fontWeight: 600 }">{{ indexValue ?? '-' }}</span>
      </div>
      <NProgress
        type="line"
        :percentage="indexValue || 0"
        :color="getIndexColor(indexValue)"
        :show-indicator="false"
        :height="5"
        style="width: 100%"
      />
    </div>
  </div>
</template>
