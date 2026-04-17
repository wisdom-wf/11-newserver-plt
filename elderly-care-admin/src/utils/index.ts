import dayjs from 'dayjs';

export const formatDate = (date: string | Date, format: string = 'YYYY-MM-DD HH:mm:ss'): string => {
  if (!date) return '-';
  return dayjs(date).format(format);
};

export const formatDateShort = (date: string | Date): string => {
  return formatDate(date, 'YYYY-MM-DD');
};

export const formatTime = (date: string | Date): string => {
  return formatDate(date, 'HH:mm:ss');
};

export const formatCurrency = (amount: number): string => {
  if (amount === null || amount === undefined) return '-';
  return `¥${amount.toFixed(2)}`;
};

export const formatPhone = (phone: string): string => {
  if (!phone) return '-';
  const reg = /^(\d{3})\d{4}(\d{4})$/;
  return phone.replace(reg, '$1****$2');
};

export const getAge = (idCard: string): number => {
  if (!idCard || idCard.length !== 18) return 0;
  const birthYear = parseInt(idCard.substring(6, 10));
  const currentYear = new Date().getFullYear();
  return currentYear - birthYear;
};

export const getGender = (idCard: string): number => {
  if (!idCard || idCard.length !== 18) return 0;
  return parseInt(idCard.substring(16, 17)) % 2 === 0 ? 2 : 1;
};

export const getGenderText = (gender: number | string): string => {
  const g = typeof gender === 'string' ? parseInt(gender, 10) : gender;
  return g === 1 ? '男' : g === 2 ? '女' : '未知';
};

export const getStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    0: '禁用',
    1: '启用',
    2: '待审核',
    3: '审核拒绝',
  };
  return statusMap[status] || '未知';
};

export const getOrderStatusText = (statusCode: number): string => {
  const statusMap: Record<number, string> = {
    0: '已取消',
    2: '待派单',
    3: '已派单',
    4: '已接单',
    5: '服务中',
    6: '已完成',
    7: '已评价',
    8: '已结算',
    9: '已拒单',
  };
  return statusMap[statusCode] || '未知';
};

export const getSettlementStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    0: '待结算',
    1: '结算中',
    2: '已结算',
    3: '结算失败',
  };
  return statusMap[status] || '未知';
};

export const getCareLevelText = (level: number | string): string => {
  const l = typeof level === 'string' ? parseInt(level, 10) : level;
  const levelMap: Record<number, string> = {
    1: '能力完好',
    2: '轻度失能',
    3: '中度失能',
    4: '重度失能',
  };
  return levelMap[l] || '未知';
};

export const getSubsidyTypeText = (type: number): string => {
  const typeMap: Record<number, string> = {
    0: '无补贴',
    1: '全额补贴',
    2: '部分补贴',
  };
  return typeMap[type] || '未知';
};

export const debounce = <T extends (...args: unknown[]) => unknown>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: ReturnType<typeof setTimeout> | null = null;
  return (...args: Parameters<T>) => {
    if (timeout) clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
};

export const storage = {
  get: (key: string): string | null => {
    return localStorage.getItem(key);
  },
  set: (key: string, value: string): void => {
    localStorage.setItem(key, value);
  },
  remove: (key: string): void => {
    localStorage.removeItem(key);
  },
  clear(): void {
    localStorage.clear();
  },
};
