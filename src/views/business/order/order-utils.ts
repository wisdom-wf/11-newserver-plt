export interface OrderTimelineItem {
  status: string;
  time: string;
  description: string;
  operator?: string;
  details?: { label: string; value: string }[];
  action?: {
    label: string;
    icon: string;
    onClick: () => void;
  };
  serviceLog?: {
    auditStatus: string;
    auditStatusLabel: string;
    reviewComment?: string;
    reviewerName?: string;
    reviewTime?: string;
    qualityCheck?: {
      checkResult: string;
      checkResultLabel: string;
      checkRemark?: string;
    };
  };
  evaluation?: {
    overallScore: number;
    evaluationContent?: string;
    evaluationTime?: string;
  };
}

export const statusOptions = [
  { label: '待派单', value: 'CREATED' },
  { label: '已派单', value: 'DISPATCHED' },
  { label: '已接单', value: 'RECEIVED' },
  { label: '服务中', value: 'SERVICE_STARTED' },
  { label: '已完成', value: 'SERVICE_COMPLETED' },
  { label: '已评价', value: 'EVALUATED' },
  { label: '已结算', value: 'SETTLED' }
];

export const serviceTypeOptions = [
  { label: '生活照料', value: '01' },
  { label: '日间照料', value: '02' },
  { label: '助餐服务', value: '03' },
  { label: '助洁服务', value: '04' },
  { label: '助浴服务', value: '05' },
  { label: '康复护理', value: '06' },
  { label: '精神慰藉', value: '07' },
  { label: '健康管理', value: '08' },
  { label: '信息咨询', value: '09' }
];

type TagType = 'warning' | 'success' | 'info' | 'error' | 'default';

export function getStatusType(status: string): TagType {
  const map: Record<string, TagType> = {
    CREATED: 'warning',
    DISPATCHED: 'info',
    RECEIVED: 'info',
    SERVICE_STARTED: 'info',
    SERVICE_COMPLETED: 'success',
    EVALUATED: 'success',
    SETTLED: 'success',
    CANCELLED: 'error',
    REJECTED: 'error',
    COMPLETED: 'success'
  };
  return map[status] || 'default';
}

export function getStatusLabel(status: string): string {
  const option = statusOptions.find(o => o.value === status);
  return option?.label || status;
}

export function getServiceLogAuditStatusType(status: string): TagType {
  const map: Record<string, TagType> = {
    DRAFT: 'warning',
    SUBMITTED: 'info',
    APPROVED: 'success',
    REJECTED: 'error',
    COMPLETED: 'success'
  };
  return map[status] || 'default';
}

export function getServiceLogQualityResultType(result: string): TagType {
  const map: Record<string, TagType> = {
    PENDING: 'warning',
    QUALIFIED: 'success',
    UNQUALIFIED: 'error',
    NEED_RECTIFY: 'warning'
  };
  return map[result] || 'default';
}

export function formatTimelineTime(time: string): string {
  if (!time) return '-';
  const date = new Date(time);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

export function formatServiceTime(
  serviceDate: string | undefined,
  serviceTime: string | undefined
): string {
  if (!serviceDate) return serviceTime || '-';
  try {
    const date = new Date(serviceDate);
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    let timePeriod = '';
    if (serviceTime) {
      const hourStr = serviceTime.split(':')[0];
      const hour = parseInt(hourStr, 10);
      timePeriod = hour < 12 ? '上午' : '下午';
    }
    return `${year}年${month}月${day}日 ${timePeriod}`;
  } catch {
    return serviceTime || '-';
  }
}
