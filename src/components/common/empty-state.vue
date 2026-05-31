<script setup lang="ts">
/**
 * 空状态组件 - 列表/区域无数据时显示友好提示
 * 支持：图标、标题、描述、操作按钮插槽
 */
interface Props {
  title?: string;
  description?: string;
  icon?: 'default' | 'order' | 'staff' | 'elder' | 'evaluation';
  size?: 'small' | 'medium' | 'large';
}

const props = withDefaults(defineProps<Props>(), {
  title: '暂无数据',
  description: '',
  icon: 'default',
  size: 'medium'
});

const iconMap = {
  default: '📋',
  order: '📑',
  staff: '👤',
  elder: '👴',
  evaluation: '⭐'
};
</script>

<template>
  <div class="empty-state" :class="[`empty-state--${size}`]">
    <div class="empty-icon">{{ iconMap[icon] }}</div>
    <div class="empty-title">{{ title }}</div>
    <div v-if="description" class="empty-desc">{{ description }}</div>
    <slot />
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  text-align: center;
}

.empty-state--small {
  padding: 16px 12px;
}

.empty-state--small .empty-icon {
  font-size: 28px;
}

.empty-state--small .empty-title {
  font-size: 13px;
}

.empty-state--large {
  padding: 48px 24px;
}

.empty-state--large .empty-icon {
  font-size: 56px;
}

.empty-state--large .empty-title {
  font-size: 16px;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
  opacity: 0.7;
}

.empty-title {
  font-size: 14px;
  color: #999;
  font-weight: 500;
  margin-bottom: 4px;
}

.empty-desc {
  font-size: 12px;
  color: #bbb;
  margin-top: 4px;
}
</style>