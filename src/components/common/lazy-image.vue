<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

interface Props {
  src?: string;
  alt?: string;
  width?: string | number;
  height?: string | number;
  fit?: 'cover' | 'contain' | 'fill';
  placeholder?: string;
}

const props = withDefaults(defineProps<Props>(), {
  src: '',
  alt: '',
  width: '100%',
  height: '100%',
  fit: 'cover',
  placeholder: ''
});

const imageRef = ref<HTMLImageElement | null>(null);
const loaded = ref(false);
const error = ref(false);
const visible = ref(false);

let observer: IntersectionObserver | null = null;

function loadImage() {
  if (!props.src) return;
  const img = new Image();
  img.onload = () => { loaded.value = true; };
  img.onerror = () => { error.value = true; };
  img.src = props.src;
}

onMounted(() => {
  if (!imageRef.value) return;
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) {
        visible.value = true;
        loadImage();
        observer?.disconnect();
      }
    },
    { rootMargin: '100px' }
  );
  observer.observe(imageRef.value);
});

onUnmounted(() => {
  observer?.disconnect();
});
</script>

<template>
  <div
    ref="imageRef"
    :style="{
      width: typeof width === 'number' ? width + 'px' : width,
      height: typeof height === 'number' ? height + 'px' : height,
      background: '#f0f0f0',
      borderRadius: '4px',
      overflow: 'hidden',
      position: 'relative',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }"
  >
    <!-- 占位/加载中：骨架屏脉冲动画 -->
    <div v-if="!loaded && !error" class="lazy-skeleton">
      <div class="lazy-shimmer"></div>
    </div>

    <!-- 加载失败 -->
    <div v-if="error" style="color: #ccc; font-size: 24px">📷</div>

    <!-- 图片 -->
    <img
      v-if="loaded && src"
      :src="src"
      :alt="alt"
      :style="{
        width: '100%',
        height: '100%',
        objectFit: fit,
        position: 'absolute',
        top: 0,
        left: 0
      }"
    />
  </div>
</template>

<style scoped>
.lazy-skeleton {
  position: absolute;
  inset: 0;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}
.lazy-shimmer {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.4) 50%,
    transparent 100%
  );
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}
</style>
