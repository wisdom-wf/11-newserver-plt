<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { NSpin, NAlert } from 'naive-ui';

export interface MarkerItem {
  /** [longitude, latitude] - BD09 coordinate system for Baidu Map */
  position: [number, number];
  title?: string;
  /** Extra data passed back in click events */
  data?: unknown;
}

export interface HeatPoint {
  /** [longitude, latitude] - BD09 coordinate system */
  lng: number;
  lat: number;
  count: number;
  weight?: number;
  name?: string;
}

interface Props {
  /** Map container height */
  height?: string;
  /** Initial zoom level (3-19) */
  zoom?: number;
  /** Initial center: [lng, lat] - Yan'an city default (BD09) */
  center?: [number, number];
  /** Points to render as BMap.Marker */
  markers?: MarkerItem[];
  /** Heatmap data points for heatmap layer */
  heatPoints?: HeatPoint[];
  /** Whether to show heatmap (default: false) */
  showHeatmap?: boolean;
  /** Heatmap radius (default: 20) */
  heatmapRadius?: number;
}

const props = withDefaults(defineProps<Props>(), {
  height: '400px',
  zoom: 10,
  center: () => [109.4897, 36.5997],
  markers: () => [],
  heatPoints: () => [],
  showHeatmap: false,
  heatmapRadius: 20
});

const emit = defineEmits<{
  (e: 'ready', map: BMap.Map): void;
  (e: 'markerClick', marker: BMap.Marker, item: MarkerItem): void;
}>();

const containerId = `bmap-${Math.random().toString(36).slice(2, 9)}`;
let map: BMap.Map | null = null;
let heatmapOverlay: any = null;
const activeMarkers = ref<BMap.Marker[]>([]);
const isLoading = ref(true);
const loadError = ref<string | null>(null);

function clearMarkers() {
  if (map && activeMarkers.value.length) {
    activeMarkers.value.forEach(marker => map?.removeOverlay(marker));
  }
  activeMarkers.value = [];
}

function clearHeatmap() {
  if (map && heatmapOverlay) {
    map.removeOverlay(heatmapOverlay);
    heatmapOverlay = null;
  }
}

function renderMarkers(items: MarkerItem[]) {
  clearMarkers();
  if (!map) return;

  const newMarkers = items.map(item => {
    const point = new BMap.Point(item.position[0], item.position[1]);
    const marker = new BMap.Marker(point, { title: item.title });
    marker.addEventListener('click', () => emit('markerClick', marker, item));
    map!.addOverlay(marker);
    return marker;
  });
  activeMarkers.value = newMarkers;
}

function renderHeatmap(points: HeatPoint[]) {
  clearHeatmap();
  if (!map || !window.BMapLib || points.length === 0) return;

  // Heatmap configuration - gradient colors like the reference image
  const heatmapCfg = {
    radius: props.heatmapRadius,
    opacity: 0.7,
    gradient: {
      0.0: 'blue',
      0.2: 'cyan',
      0.4: 'lime',
      0.6: 'yellow',
      0.8: 'orange',
      1.0: 'red'
    }
  };

  // Convert to heatmap library format
  const heatData = points.map(p => ({
    lng: p.lng,
    lat: p.lat,
    count: p.count || p.weight || 1
  }));

  try {
    heatmapOverlay = new (window as any).BMapLib.HeatmapOverlay(heatmapCfg);
    map.addOverlay(heatmapOverlay);
    heatmapOverlay.setDataSet({ data: heatData, max: Math.max(...points.map(p => p.count || 1)) });
  } catch (e) {
    console.error('[BMap] Failed to render heatmap', e);
  }
}

function loadBMapScript(): Promise<void> {
  return new Promise((resolve, reject) => {
    if (typeof BMap !== 'undefined') {
      resolve();
      return;
    }
    const ak = import.meta.env.VITE_BMAP_AK || 'ydAUBAAx7jLIGAhNukBM9iCvcJObENVB';

    // Set callback for BMap API
    (window as any).callback = () => resolve();

    const script = document.createElement('script');
    script.src = `https://api.map.baidu.com/api?v=3.0&ak=${ak}&callback=callback`;
    script.onerror = () => reject(new Error('百度地图API加载失败'));
    document.head.appendChild(script);
  });
}

function loadHeatmapLibrary(): Promise<void> {
  return new Promise((resolve, reject) => {
    if (typeof window.BMapLib !== 'undefined') {
      resolve();
      return;
    }
    const script = document.createElement('script');
    script.src = 'https://api.map.baidu.com/library/Heatmap/2.0/src/Heatmap_min.js';
    script.onerror = () => reject(new Error('热力图库加载失败'));
    script.onload = () => resolve();
    document.head.appendChild(script);
  });
}

async function initMap() {
  try {
    await loadBMapScript();

    // Load heatmap library if needed
    if (props.showHeatmap) {
      await loadHeatmapLibrary();
    }

    const container = document.getElementById(containerId);
    if (!container) {
      throw new Error('地图容器未找到');
    }

    map = new BMap.Map(containerId);
    map.enableScrollWheelZoom();

    const centerPoint = new BMap.Point(props.center[0], props.center[1]);
    map.centerAndZoom(centerPoint, props.zoom);

    renderMarkers(props.markers);

    if (props.showHeatmap && props.heatPoints.length > 0) {
      renderHeatmap(props.heatPoints);
    }

    emit('ready', map);
  } catch (err) {
    loadError.value = err instanceof Error ? err.message : '地图初始化失败';
    console.error('[BMap] init error', err);
  } finally {
    isLoading.value = false;
  }
}

watch(
  () => props.markers,
  newMarkers => {
    if (map) renderMarkers(newMarkers);
  },
  { deep: true }
);

watch(
  () => props.heatPoints,
  newPoints => {
    if (map && props.showHeatmap) {
      renderHeatmap(newPoints);
    }
  },
  { deep: true }
);

onMounted(initMap);

onUnmounted(() => {
  clearMarkers();
  clearHeatmap();
  map?.clearOverlays();
  map = null;
});
</script>

<template>
  <div class="relative w-full" :style="{ height }">
    <!-- Loading overlay -->
    <div
      v-if="isLoading"
      class="absolute inset-0 flex items-center justify-center bg-white/80 z-10 dark:bg-[rgb(33,38,45)/0.8]"
    >
      <NSpin size="large" />
    </div>

    <!-- Error state -->
    <NAlert v-if="loadError" type="error" class="absolute inset-x-4 top-4 z-10">
      {{ loadError }}
    </NAlert>

    <!-- Map container -->
    <div :id="containerId" class="w-full h-full" />
  </div>
</template>
