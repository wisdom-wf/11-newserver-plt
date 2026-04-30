<script setup lang="ts">
import { computed, onMounted, nextTick } from 'vue';
import { NConfigProvider, darkTheme } from 'naive-ui';
import type { WatermarkProps } from 'naive-ui';
import { useAppStore } from './store/modules/app';
import { useThemeStore } from './store/modules/theme';
import { naiveDateLocales, naiveLocales } from './locales/naive';

defineOptions({
  name: 'App'
});

const appStore = useAppStore();
const themeStore = useThemeStore();

const naiveDarkTheme = computed(() => (themeStore.darkMode ? darkTheme : undefined));

const naiveLocale = computed(() => {
  return naiveLocales[appStore.locale];
});

const naiveDateLocale = computed(() => {
  return naiveDateLocales[appStore.locale];
});

const watermarkProps = computed<WatermarkProps>(() => {
  return {
    content: themeStore.watermarkContent,
    cross: true,
    fullscreen: true,
    fontSize: 16,
    lineHeight: 16,
    width: 384,
    height: 384,
    xOffset: 12,
    yOffset: 60,
    rotate: -15,
    zIndex: 9999
  };
});

// 强制修复菜单激活项文字色（naiveui 内部优先级问题）
onMounted(async () => {
  await nextTick();
  const fixMenuActive = () => {
    const selected = document.querySelectorAll('.n-menu-item-content--selected');
    selected.forEach(el => {
      // 文字：naiveui 用的是 n-menu-item-content-header
      const textEl = el.querySelector('.n-menu-item-content-header');
      if (textEl && textEl instanceof HTMLElement) {
        textEl.style.color = '#1E3A5F';
        textEl.style.fontWeight = '600';
      }
      // 图标：SVG path 用 currentColor，改 icon div 颜色即可
      const iconEl = el.querySelector('.n-menu-item-content__icon');
      if (iconEl && iconEl instanceof HTMLElement) {
        iconEl.style.color = '#1E3A5F';
      }
    });
  };
  fixMenuActive();
  setTimeout(fixMenuActive, 500);
  setTimeout(fixMenuActive, 1500);
});
</script>

<template>
  <NConfigProvider
    :theme="naiveDarkTheme"
    :theme-overrides="themeStore.naiveTheme"
    :locale="naiveLocale"
    :date-locale="naiveDateLocale"
    class="h-full"
  >
    <AppProvider>
      <RouterView class="bg-layout" />
      <NWatermark v-if="themeStore.watermark.visible" v-bind="watermarkProps" />
    </AppProvider>
  </NConfigProvider>
</template>

<style scoped></style>
