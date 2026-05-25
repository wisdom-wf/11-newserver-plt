<script setup lang="ts">
import { ref, computed } from 'vue';
import {
  NButton,
  NTag,
  NDescriptions,
  NDescriptionsItem,
  NDrawer,
  NDrawerContent,
  NCard,
  NBadge,
  useMessage
} from 'naive-ui';
import type { OrderTimelineItem } from '../order-utils';
import {
  getStatusType,
  getStatusLabel,
  formatServiceTime,
  formatTimelineTime,
  getServiceLogAuditStatusType,
  getServiceLogQualityResultType
} from '../order-utils';

const props = defineProps<{
  visible: boolean;
  orderData: any;
  timelineData: OrderTimelineItem[];
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  'create-evaluation': [];
  'go-to-service-log': [orderNo: string];
  'go-to-appointment': [appointmentId: string];
  'go-to-service-log-detail': [logId: string];
  'go-to-quality-check': [checkId: string];
}>();

const message = useMessage();
const activeDetailTab = ref<'info' | 'timeline' | 'links'>('info');
const expandedNodes = ref<Set<string>>(new Set());

const drawerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
});

function toggleNode(node: OrderTimelineItem) {
  if (expandedNodes.value.has(node.status)) {
    expandedNodes.value.delete(node.status);
  } else {
    expandedNodes.value.add(node.status);
  }
}

function isCurrentNode(node: OrderTimelineItem): boolean {
  if (!props.orderData) return false;
  const timeline = props.timelineData;
  if (!timeline || timeline.length === 0) return false;
  const nodeIndex = timeline.findIndex(n => n.status === node.status);
  const lastIndex = timeline.length - 1;
  return nodeIndex === lastIndex && !['SERVICE_COMPLETED', 'EVALUATED', 'SETTLED', 'CANCELLED'].includes(props.orderData.status);
}

function isCompletedNode(node: OrderTimelineItem): boolean {
  if (!props.orderData) return false;
  const timeline = props.timelineData;
  if (!timeline || timeline.length === 0) return false;
  const nodeIndex = timeline.findIndex(n => n.status === node.status);
  const lastIndex = timeline.length - 1;
  return nodeIndex < lastIndex || ['SERVICE_COMPLETED', 'EVALUATED', 'SETTLED'].includes(props.orderData.status);
}

function getNodeIcon(node: OrderTimelineItem): string {
  if (['SERVICE_COMPLETED', 'EVALUATED', 'SETTLED'].includes(node.status)) return '✓';
  if (node.status === 'CANCELLED') return '✕';
  if (isCurrentNode(node)) return '●';
  return '○';
}

function getNodeColor(node: OrderTimelineItem): string {
  if (['SERVICE_COMPLETED', 'EVALUATED', 'SETTLED'].includes(node.status)) return '#11998e';
  if (node.status === 'CANCELLED') return '#f5576c';
  if (isCurrentNode(node)) return '#4facfe';
  return '#999';
}

function getAuditStatusLabel(status: string): string {
  const map: Record<string, string> = { DRAFT: '草稿', SUBMITTED: '已提交', APPROVED: '已通过', REJECTED: '已驳回' };
  return map[status] || status;
}
function copyToClipboard(text: string, label: string) {
  window.navigator.clipboard.writeText(text);
  message.success(`${label}已复制`);
}

function callPhone(phone: string) {
  window.open(`tel:${phone}`);
}
</script>

<template>
  <NDrawer v-model:show="drawerVisible" :width="560" placement="right" closable>
    <NDrawerContent :title="orderData?.orderNo ? `订单详情 - ${orderData.orderNo}` : '订单详情'" closable>
      <!-- Tab Switch -->
      <div style="display: flex; gap: 8px; margin-bottom: 16px; border-bottom: 1px solid #eee; padding-bottom: 12px">
        <NButton :type="activeDetailTab === 'info' ? 'primary' : 'default'" size="small" @click="activeDetailTab = 'info'">
          基本信息
        </NButton>
        <NButton :type="activeDetailTab === 'timeline' ? 'primary' : 'default'" size="small" @click="activeDetailTab = 'timeline'">
          时间轴
        </NButton>
        <NButton :type="activeDetailTab === 'links' ? 'primary' : 'default'" size="small" @click="activeDetailTab = 'links'">
          关联信息
          <template v-if="(orderData as any)?.serviceLogs?.length || (orderData as any)?.qualityChecks?.length">
            <NBadge :value="((orderData as any)?.serviceLogs?.length || 0) + ((orderData as any)?.qualityChecks?.length || 0)" :max="99" />
          </template>
        </NButton>
      </div>

      <!-- 快捷操作栏 -->
      <div style="display: flex; gap: 8px; margin-bottom: 16px; padding: 12px; background: #f8f9fa; border-radius: 8px; overflow-x: auto">
        <NButton size="small" style="height: 36px; font-size: 13px; font-weight: 600; padding: 0 14px; white-space: nowrap; flex-shrink: 0"
          @click="copyToClipboard(orderData?.orderNo || '', '订单号')">
          复制订单号
        </NButton>
        <NButton size="small" style="height: 36px; font-size: 13px; font-weight: 600; padding: 0 14px; white-space: nowrap; flex-shrink: 0"
          @click="copyToClipboard(orderData?.elderPhone || '', '电话')">
          复制电话
        </NButton>
        <NButton v-if="orderData?.elderPhone" size="small" type="primary"
          style="height: 36px; font-size: 13px; font-weight: 600; padding: 0 14px; white-space: nowrap; flex-shrink: 0"
          @click="callPhone(orderData.elderPhone)">
          拨打电话
        </NButton>
      </div>

      <!-- Basic Info Tab -->
      <div v-if="activeDetailTab === 'info' && orderData">
        <NDescriptions :column="1" bordered size="small">
          <NDescriptionsItem label="订单号">{{ orderData.orderNo }}</NDescriptionsItem>
          <NDescriptionsItem label="订单状态">
            <NTag :type="getStatusType(orderData.status)" size="small">{{ getStatusLabel(orderData.status) }}</NTag>
          </NDescriptionsItem>
          <NDescriptionsItem label="客户姓名">{{ orderData.elderName }}</NDescriptionsItem>
          <NDescriptionsItem label="客户手机">{{ orderData.elderPhone }}</NDescriptionsItem>
          <NDescriptionsItem label="服务类型">{{ orderData.serviceTypeName }}</NDescriptionsItem>
          <NDescriptionsItem label="预约时间">{{ formatServiceTime(orderData.serviceDate, orderData.serviceTime) }}</NDescriptionsItem>
          <NDescriptionsItem label="服务地址">{{ orderData.serviceAddress || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="服务商">{{ orderData.providerName || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="服务人员">{{ orderData.staffName || '-' }}</NDescriptionsItem>
          <NDescriptionsItem label="预估费用">¥{{ orderData.estimatedPrice || 0 }}</NDescriptionsItem>
          <NDescriptionsItem label="实际费用">¥{{ orderData.actualPrice || 0 }}</NDescriptionsItem>
          <NDescriptionsItem label="自付金额">¥{{ orderData.selfPayAmount || 0 }}</NDescriptionsItem>
          <NDescriptionsItem label="补贴金额">¥{{ orderData.subsidyAmount || 0 }}</NDescriptionsItem>
          <NDescriptionsItem label="创建时间">{{ orderData.createTime }}</NDescriptionsItem>
          <NDescriptionsItem label="备注">{{ orderData.remark || '-' }}</NDescriptionsItem>
        </NDescriptions>
      </div>

      <!-- Timeline Tab -->
      <div v-if="activeDetailTab === 'timeline'">
        <div v-if="timelineData.length > 0" class="timeline-container">
          <div v-for="(node, index) in timelineData" :key="node.status" class="timeline-node">
            <div class="timeline-node-header"
              :class="{ 'timeline-node-active': isCurrentNode(node), 'timeline-node-completed': isCompletedNode(node) }"
              @click="toggleNode(node)">
              <div class="timeline-connector">
                <div class="timeline-dot" :style="{ background: getNodeColor(node) }">
                  <span v-if="isCompletedNode(node) && !isCurrentNode(node)" class="timeline-check">✓</span>
                  <span v-else-if="isCurrentNode(node)" class="timeline-pulse"></span>
                  <span v-else class="timeline-icon">{{ getNodeIcon(node) }}</span>
                </div>
                <div v-if="index < timelineData.length - 1" class="timeline-line"
                  :class="{ 'timeline-line-completed': isCompletedNode(node) }"></div>
              </div>
              <div class="timeline-node-content">
                <div class="timeline-node-title">
                  <span class="timeline-node-status-icon" :style="{ color: getNodeColor(node) }">{{ getNodeIcon(node) }}</span>
                  <span class="timeline-node-title-text">{{ getStatusLabel(node.status) }}</span>
                  <span class="timeline-node-time">{{ formatTimelineTime(node.time) }}</span>
                </div>
                <div class="timeline-node-description">{{ node.description }}</div>
                <div v-if="node.operator" class="timeline-node-operator">
                  <span class="operator-label">操作人:</span>
                  <span class="operator-name">{{ node.operator }}</span>
                </div>

                <!-- 服务中节点：服务日志审核记录 -->
                <div v-if="node.status === 'SERVICE_STARTED' && node.serviceLog" class="timeline-service-log">
                  <div class="timeline-section-title">
                    <icon:material-symbols:description-outline /> 服务日志审核
                  </div>
                  <div class="timeline-log-info">
                    <NTag :type="getServiceLogAuditStatusType(node.serviceLog.auditStatus)" size="small">
                      {{ node.serviceLog.auditStatusLabel }}
                    </NTag>
                    <span v-if="node.serviceLog.reviewerName" class="timeline-log-reviewer">
                      {{ node.serviceLog.reviewerName }}
                      <span v-if="node.serviceLog.reviewTime">{{ formatTimelineTime(node.serviceLog.reviewTime) }}</span>
                    </span>
                  </div>
                  <div v-if="node.serviceLog.reviewComment" class="timeline-log-comment">{{ node.serviceLog.reviewComment }}</div>
                  <div v-if="node.serviceLog.qualityCheck" class="timeline-quality-info">
                    <NTag :type="getServiceLogQualityResultType(node.serviceLog.qualityCheck.checkResult)" size="small">
                      质检{{ node.serviceLog.qualityCheck.checkResultLabel }}
                    </NTag>
                    <span v-if="node.serviceLog.qualityCheck.checkRemark" class="timeline-quality-remark">
                      {{ node.serviceLog.qualityCheck.checkRemark }}
                    </span>
                  </div>
                </div>

                <!-- 服务完成节点：满意度评价 -->
                <div v-if="node.status === 'SERVICE_COMPLETED'" class="timeline-evaluation">
                  <div class="timeline-section-title"><icon:material-symbols:star-outline /> 满意度评价</div>
                  <template v-if="node.evaluation">
                    <div class="timeline-eval-info">
                      <NTag type="success" size="small">评分: {{ node.evaluation.overallScore }}分</NTag>
                    </div>
                    <div v-if="node.evaluation.evaluationContent" class="timeline-eval-content">{{ node.evaluation.evaluationContent }}</div>
                  </template>
                  <template v-else>
                    <div class="timeline-no-evaluation">
                      <span class="timeline-no-eval-text">暂无评价记录</span>
                      <NButton type="warning" size="small" @click.stop="emit('create-evaluation')">
                        <template #icon><icon:material-symbols:edit-note /></template>
                        满意度调查
                      </NButton>
                    </div>
                  </template>
                </div>

                <!-- 服务中节点：快速查看服务日志按钮 -->
                <div v-if="node.status === 'SERVICE_STARTED'" class="timeline-node-actions">
                  <NButton text type="primary" size="small" @click.stop="emit('go-to-service-log', orderData!.orderNo)">
                    <template #icon><icon:material-symbols:description-outline /></template>
                    查看服务日志
                  </NButton>
                </div>

                <div v-if="node.details && node.details.length > 0" class="timeline-node-details"
                  :class="{ 'timeline-node-details-expanded': expandedNodes.has(node.status) }">
                  <div class="timeline-details-list">
                    <div v-for="detail in node.details" :key="detail.label" class="timeline-detail-item">
                      <span class="timeline-detail-label">{{ detail.label }}:</span>
                      <span class="timeline-detail-value">{{ detail.value }}</span>
                    </div>
                  </div>
                </div>
                <div v-if="node.details && node.details.length > 0" class="timeline-expand-hint">
                  <span>{{ expandedNodes.has(node.status) ? '点击收起' : '点击展开详情' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="timeline-empty">
          <div class="timeline-empty-icon">📋</div>
          <div class="timeline-empty-text">暂无时间轴数据</div>
        </div>
      </div>

      <!-- Linked Info Tab -->
      <div v-if="activeDetailTab === 'links' && orderData">
        <!-- 来源预约 -->
        <div v-if="orderData.appointmentNo" style="margin-bottom: 16px">
          <div style="font-weight: 600; font-size: 14px; margin-bottom: 8px">来源预约</div>
          <NCard size="small" :bordered="true" hoverable style="cursor: pointer" @click="emit('go-to-appointment', orderData.appointmentId)">
            <div style="display: flex; justify-content: space-between; align-items: center">
              <div>
                <div style="font-weight: 500; font-size: 13px">{{ orderData.appointmentNo }}</div>
                <div style="color: #666; font-size: 12px; margin-top: 2px">预约时间: {{ orderData.appointmentTime || '-' }}</div>
              </div>
              <NButton size="tiny" type="primary" quaternary>
                <template #icon><icon:material-symbols:open-in-new /></template>
                查看预约
              </NButton>
            </div>
          </NCard>
        </div>

        <!-- 服务日志列表 -->
        <div style="margin-bottom: 20px">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px">
            <div style="font-weight: 600; font-size: 14px">
              服务日志 <NTag size="small" type="info" style="margin-left: 6px">{{ orderData.serviceLogs?.length || 0 }} 条</NTag>
            </div>
            <NButton :type="orderData.serviceLogs?.length > 0 ? 'primary' : 'default'"
              :disabled="!orderData.serviceLogs?.length" size="small"
              @click="orderData.serviceLogs?.length > 0 && emit('go-to-service-log-detail', orderData.serviceLogs[0].serviceLogId)">
              关联日志
            </NButton>
          </div>
          <div v-if="orderData.serviceLogs && orderData.serviceLogs.length > 0">
            <NCard v-for="log in orderData.serviceLogs" :key="log.serviceLogId" size="small"
              style="margin-bottom: 8px; cursor: pointer" :bordered="true" hoverable
              @click="emit('go-to-service-log-detail', log.serviceLogId)">
              <div style="display: flex; justify-content: space-between; align-items: center">
                <div>
                  <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px">
                    <span style="font-weight: 500; font-size: 13px">{{ log.logNo || log.serviceLogId }}</span>
                    <NTag :type="getServiceLogAuditStatusType(log.auditStatus)" size="small">{{ getAuditStatusLabel(log.auditStatus) }}</NTag>
                  </div>
                  <div style="font-size: 12px; color: #666">
                    <span v-if="log.serviceDate">{{ log.serviceDate }}</span>
                    <span v-if="log.serviceDuration"> · {{ log.serviceDuration }}分钟</span>
                    <span v-if="log.staffName"> · {{ log.staffName }}</span>
                  </div>
                  <div v-if="log.reviewComment" style="font-size: 12px; color: #888; margin-top: 2px">审核意见：{{ log.reviewComment }}</div>
                </div>
                <icon:material-symbols:chevron-right style="color: #ccc; font-size: 18px" />
              </div>
            </NCard>
          </div>
          <div v-else style="text-align: center; padding: 24px 0; color: #999; font-size: 13px">暂无服务日志</div>
        </div>

        <!-- 质检单列表 -->
        <div>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px">
            <div style="font-weight: 600; font-size: 14px">
              质检单 <NTag size="small" type="warning" style="margin-left: 6px">{{ orderData.qualityChecks?.length || 0 }} 条</NTag>
            </div>
          </div>
          <div v-if="orderData.qualityChecks && orderData.qualityChecks.length > 0">
            <NCard v-for="check in orderData.qualityChecks" :key="check.qualityCheckId" size="small"
              style="margin-bottom: 8px; cursor: pointer" :bordered="true" hoverable
              @click="emit('go-to-quality-check', check.qualityCheckId)">
              <div style="display: flex; justify-content: space-between; align-items: center">
                <div>
                  <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px">
                    <span style="font-weight: 500; font-size: 13px">{{ check.checkNo || check.qualityCheckId }}</span>
                    <NTag :type="getServiceLogQualityResultType(check.checkResult)" size="small">
                      {{ check.checkResult === 'QUALIFIED' ? '合格' : check.checkResult === 'UNQUALIFIED' ? '不合格' : '待质检' }}
                    </NTag>
                  </div>
                  <div style="font-size: 12px; color: #666">
                    <span v-if="check.checkTime">{{ check.checkTime }}</span>
                    <span v-if="check.checkerName"> · {{ check.checkerName }}</span>
                  </div>
                  <div v-if="check.checkRemark" style="font-size: 12px; color: #888; margin-top: 2px">{{ check.checkRemark }}</div>
                </div>
                <icon:material-symbols:chevron-right style="color: #ccc; font-size: 18px" />
              </div>
            </NCard>
          </div>
          <div v-else style="text-align: center; padding: 24px 0; color: #999; font-size: 13px">暂无质检单</div>
        </div>
      </div>
    </NDrawerContent>
  </NDrawer>
</template>
