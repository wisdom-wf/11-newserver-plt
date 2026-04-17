/**
 * Baidu Map (BMap) Type Declarations
 */

declare namespace BMap {
  class Map {
    constructor(container: string | HTMLElement);
    enableScrollWheelZoom(): void;
    centerAndZoom(point: Point, zoom: number): void;
    addOverlay(overlay: Overlay): void;
    removeOverlay(overlay: Overlay): void;
    clearOverlays(): void;
    getZoom(): number;
    getCenter(): Point;
  }

  class Point {
    constructor(lng: number, lat: number);
    lng: number;
    lat: number;
  }

  class Marker {
    constructor(point: Point, options?: MarkerOptions);
    setTitle(title: string): void;
    addEventListener(event: string, handler: (e: Event) => void): void;
  }

  interface MarkerOptions {
    title?: string;
    enableDragging?: boolean;
  }

  interface Overlay {}

  class Overlay {}

  interface Event {}
}

declare namespace BMapLib {
  class HeatmapOverlay {
    constructor(options: HeatmapOptions);
    setDataSet(data: { data: HeatPoint[]; max: number }): void;
  }

  interface HeatmapOptions {
    radius?: number;
    opacity?: number;
    gradient?: Record<number, string>;
  }

  interface HeatPoint {
    lng: number;
    lat: number;
    count?: number;
  }
}

interface Window {
  BMap: typeof BMap;
  BMapLib: typeof BMapLib;
}
