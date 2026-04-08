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

export const getGenderText = (gender: number): string => {
  return gender === 1 ? '男' : gender === 2 ? '女' : '未知';
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

export const getOrderStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    0: '已取消',
    1: '待支付',
    2: '待派单',
    3: '待接单',
    4: '服务中',
    5: '待评价',
    6: '已完成',
  };
  return statusMap[status] || '未知';
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

export const getCareLevelText = (level: number): string => {
  const levelMap: Record<number, string> = {
    1: '能力完好',
    2: '轻度失能',
    3: '中度失能',
    4: '重度失能',
  };
  return levelMap[level] || '未知';
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
