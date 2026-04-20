/**
 * 格式化工具函数 - 统一管理数据格式化
 * 解决空值/小数/大数字的显示问题
 */

/**
 * 格式化数字 - 智能单位切换
 * @param num 数值
 * @param decimals 小数位数，默认自动
 * @returns 格式化后的字符串
 */
export function formatNumber(
  num: number | undefined | null,
  decimals?: number
): string {
  if (num == null || isNaN(num)) return '--';

  // 自动判断小数位数
  const d = decimals ?? (num >= 1e8 ? 2 : num >= 1e4 ? 1 : 0);

  if (num >= 1e8) {
    return (num / 1e8).toFixed(d) + '亿';
  }
  if (num >= 1e4) {
    return (num / 1e4).toFixed(d) + '万';
  }
  return num.toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: d,
  });
}

/**
 * 格式化金额 - 带¥符号
 * @param num 数值
 * @returns 格式化后的金额字符串
 */
export function formatMoney(num: number | undefined | null): string {
  if (num == null || isNaN(num) || num === 0) return '--';
  return '¥' + formatNumber(num);
}

/**
/**
 * 百分比格式化
 * - null/undefined/NaN/0 → '--'
 * - 其他返回 'XX%'
 */
export function formatPercent(
  num: number | undefined | null,
  asPercent = true
): string {
  if (num == null || isNaN(num)) return '--';
  // 0值视为无数据
  if (num === 0) return '--';
  // 如果数值大于1，认为已经是百分数形式
  const value = asPercent && num <= 1 ? num * 100 : num;
  return value.toFixed(1) + '%';
}

/**
 * 格式化评分
 * @param num 数值
 * @returns 格式化后的评分字符串
 */
export function formatScore(num: number | undefined | null): string {
  if (num == null || isNaN(num) || num === 0) return '--';
  return num.toFixed(1);
}

/**
 * 格式化数量
 * @param num 数值
 * @returns 格式化后的数量字符串
 */
export function formatCount(num: number | undefined | null): string {
  if (num == null || isNaN(num)) return '--';
  if (num >= 1e4) {
    return (num / 1e4).toFixed(1) + '万';
  }
  return num.toLocaleString('zh-CN');
}

/**
 * 格式化时长
 * @param minutes 分钟数
 * @returns 格式化后的时长字符串 (如: 2小时30分)
 */
export function formatDuration(minutes: number | undefined | null): string {
  if (minutes == null || isNaN(minutes)) return '--';
  if (minutes < 60) return minutes + '分钟';
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return mins > 0 ? `${hours}小时${mins}分` : `${hours}小时`;
}

/**
 * 空值处理
 * @param value 任意值
 * @param fallback 替代文本，默认 '--'
 * @returns 处理后的字符串
 */
export function emptyValue<T>(value: T, fallback = '--'): string {
  if (value == null || value === '') return fallback;
  return String(value);
}
