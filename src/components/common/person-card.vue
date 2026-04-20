<script setup lang="ts">
/**
 * 照片卡片组件 - 带照片的人员信息卡片
 * 用于服务人员、老人等需要照片展示的场景
 * 支持：照片上传、健康/状态指数、选中状态、跳转操作
 */
import { computed } from 'vue';
import { NImage, NAvatar, NUpload, NButton, NTag, NProgress } from 'naive-ui';
import type { UploadFile } from 'naive-ui';

interface Props {
  /** 照片URL */
  photoUrl?: string;
  /** 姓名 */
  name: string;
  /** 副标题信息（如：年龄 | 护理级别） */
  subtitle?: string;
  /** 额外信息列表 */
  extraInfo?: Array<{ label: string; value: string }>;
  /** 指数值（健康指数/评分等） */
  indexValue?: number;
  /** 指数标签 */
  indexLabel?: string;
  /** 选中状态 */
  selected?: boolean;
  /** 是否显示上传按钮 */
  showUploadBtn?: boolean;
  /** 上传按钮文字 */
  uploadText?: string;
  /** 跳转按钮文字 */
  clickText?: string;
  /** 照片宽度 */
  photoWidth?: number;
  /** 卡片最小宽度 */
  cardMinWidth?: number;
}

const props = withDefaults(defineProps<Props>(), {
  photoUrl: '',
  name: '',
  subtitle: '',
  extraInfo: undefined,
  indexValue: undefined,
  indexLabel: '指数',
  selected: false,
  showUploadBtn: true,
  uploadText: '上传照片',
  clickText: '',
  photoWidth: 90,
  cardMinWidth: 320
});

const emit = defineEmits<{
  (e: 'click'): void;
  (e: 'upload-click'): void;
  (e: 'photo-upload', file: File): void;
}>();

// 计算照片高度（保持9:16比例）
const computedPhotoHeight = computed(() => Math.round(props.photoWidth * (16 / 9)));

function getIndexColor(index?: number): string {
  if (index === undefined) return '#999';
  if (index >= 80) return '#52c41a';
  if (index >= 60) return '#fa8c16';
  return '#f5222d';
}

function getIndexText(index?: number): string {
  if (index === undefined) return '暂无';
  if (index >= 80) return '正常';
  if (index >= 60) return '预警';
  return '告警';
}

function handlePhotoUpload(file: UploadFile) {
  if (file.file) {
    emit('photo-upload', file.file);
  }
  return false;
}

function handleCardClick() {
  emit('click');
}

function handleUploadClick(e: Event) {
  e.stopPropagation();
  emit('upload-click');
}

function handleActionClick(e: Event) {
  e.stopPropagation();
  emit('click');
}
</script>

<template>
  <div
    :style="{
      display: 'flex',
      flexDirection: 'column',
      border: selected ? '2px solid #18a058' : '1px solid #f0f0f0',
      borderRadius: '8px',
      padding: '12px',
      cursor: 'pointer',
      transition: 'all 0.2s',
      background: selected ? '#f6ffed' : '#fff',
      width: cardMinWidth + 'px',
      minWidth: cardMinWidth + 'px',
      flexShrink: 0,
      boxSizing: 'border-box'
    }"
    @click="handleCardClick"
  >
    <!-- 顶部：照片 + 信息 -->
    <div style="display: flex; gap: 12px; margin-bottom: 8px">
      <!-- 照片区域 9:16比例 -->
      <div
        :style="{
          position: 'relative',
          width: photoWidth + 'px',
          height: computedPhotoHeight + 'px',
          flexShrink: 0,
          background: '#f5f5f5',
          borderRadius: '6px',
          overflow: 'hidden'
        }"
      >
        <NImage
          v-if="photoUrl"
          :src="photoUrl"
          :style="{ width: photoWidth + 'px', height: computedPhotoHeight + 'px', objectFit: 'cover' }"
        />
        <NAvatar
          v-else
          :size="Math.min(photoWidth, computedPhotoHeight) - 12"
          round
          :style="{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)' }"
        >
          {{ name?.charAt(0) || '?' }}
        </NAvatar>
        <!-- 上传按钮 -->
        <NUpload
          v-if="showUploadBtn"
          :show-file-list="false"
          accept="image/*"
          :custom-request="(options) => handlePhotoUpload(options.file)"
        >
          <NButton
            size="tiny"
            :style="{
              position: 'absolute',
              bottom: '4px',
              right: '4px',
              opacity: 0.9,
              width: '24px',
              height: '24px',
              minWidth: '24px',
              padding: 0
            }"
            circle
            type="primary"
            @click.stop
          >
            +
          </NButton>
        </NUpload>
      </div>

      <!-- 信息区域 -->
      <div style="flex: 1; display: flex; flex-direction: column; justify-content: space-between; overflow: hidden">
        <div>
          <div
            style="
              font-weight: 600;
              font-size: 15px;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              margin-bottom: 4px;
            "
          >
            {{ name || '未知' }}
          </div>
          <div v-if="subtitle" style="font-size: 12px; color: #666; margin-bottom: 4px">
            {{ subtitle }}
          </div>
          <div v-if="extraInfo && extraInfo.length > 0" style="display: flex; flex-wrap: wrap; gap: 4px 8px; font-size: 11px; color: #888">
            <span v-for="(item, index) in extraInfo" :key="index">{{ item.label }}: {{ item.value }}</span>
          </div>
        </div>
        <!-- 指数区域 -->
        <div v-if="indexValue !== undefined">
          <div style="display: flex; align-items: center; justify-content: space-between; font-size: 12px">
            <span style="color: #666">{{ indexLabel }}</span>
            <NTag
              size="tiny"
              :color="{
                color: getIndexColor(indexValue) + '22',
                textColor: getIndexColor(indexValue),
                borderColor: 'transparent'
              }"
            >
              {{ indexValue ?? '-' }}
            </NTag>
          </div>
          <NProgress
            type="line"
            :percentage="indexValue || 0"
            :color="getIndexColor(indexValue)"
            :show-indicator="false"
            :height="4"
            style="width: 100%; margin-top: 4px"
          />
        </div>
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <div v-if="clickText" style="display: flex; gap: 8px; margin-top: 8px">
      <NButton size="small" style="flex: 1" @click="handleActionClick">
        {{ clickText }}
      </NButton>
    </div>
  </div>
</template>
