<script setup lang="ts">
import { ref, watch } from 'vue';
import {
  NButton,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NDatePicker,
  NInputNumber,
  NModal,
  useMessage
} from 'naive-ui';
import { fetchCreateOrder } from '@/service/api';

const props = defineProps<{ visible: boolean }>();
const emit = defineEmits<{
  'update:visible': [value: boolean];
  'success': [];
}>();

const message = useMessage();

const serviceTypeOptions = [
  { label: '生活照料', value: '01', name: '生活照料' },
  { label: '日间照料', value: '02', name: '日间照料' },
  { label: '助餐服务', value: '03', name: '助餐服务' },
  { label: '助洁服务', value: '04', name: '助洁服务' },
  { label: '助浴服务', value: '05', name: '助浴服务' },
  { label: '康复护理', value: '06', name: '康复护理' },
  { label: '精神慰藉', value: '07', name: '精神慰藉' },
  { label: '健康管理', value: '08', name: '健康管理' },
  { label: '信息咨询', value: '09', name: '信息咨询' }
];

const defaultForm = () => ({
  elderName: '',
  elderPhone: '',
  serviceTypeCode: '',
  serviceTypeName: '',
  serviceDate: null as number | null,
  serviceTime: '',
  serviceDuration: 60,
  serviceAddress: '',
  specialRequirements: '',
  estimatedPrice: 0,
  subsidyAmount: 0,
  selfPayAmount: 0
});

const addForm = ref(defaultForm());
const loading = ref(false);

watch(() => props.visible, (val) => {
  if (val) addForm.value = defaultForm();
});

async function handleSubmit() {
  const form = addForm.value;
  if (!form.elderName || !form.elderPhone || !form.serviceTypeCode) {
    message.warning('请填写必填项');
    return;
  }
  loading.value = true;
  try {
    const selected = serviceTypeOptions.find(o => o.value === form.serviceTypeCode);
    const { error } = await fetchCreateOrder({
      ...form,
      serviceTypeName: selected?.name || '',
      serviceDate: form.serviceDate ? new Date(form.serviceDate).toISOString().split('T')[0] : ''
    } as any);
    if (error) {
      message.error(error?.message || '创建失败');
      return;
    }
    message.success('订单创建成功');
    emit('update:visible', false);
    emit('success');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <NModal :show="visible" title="新增订单" preset="card" style="width: 680px"
    @update:show="emit('update:visible', $event)">
    <NForm :model="addForm" label-placement="top" label-width="120">
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0 24px">
        <NFormItem label="客户姓名" required>
          <NInput v-model:value="addForm.elderName" placeholder="请输入客户姓名" size="large" />
        </NFormItem>
        <NFormItem label="手机号" required>
          <NInput v-model:value="addForm.elderPhone" placeholder="请输入手机号" size="large" />
        </NFormItem>
      </div>
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0 24px">
        <NFormItem label="服务类型" required>
          <NSelect v-model:value="addForm.serviceTypeCode" :options="serviceTypeOptions"
            placeholder="请选择服务类型" size="large" />
        </NFormItem>
        <NFormItem label="服务日期" required>
          <NDatePicker v-model:value="addForm.serviceDate" type="date"
            placeholder="请选择服务日期" style="width: 100%" size="large" />
        </NFormItem>
      </div>
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0 24px">
        <NFormItem label="服务时间">
          <NInput v-model:value="addForm.serviceTime" placeholder="如：09:00-11:00" size="large" />
        </NFormItem>
        <NFormItem label="服务时长（分钟）">
          <NInputNumber v-model:value="addForm.serviceDuration" :min="1" placeholder="默认60分钟" size="large" style="width: 100%">
            <template #suffix>分钟</template>
          </NInputNumber>
        </NFormItem>
      </div>
      <NFormItem label="服务地址">
        <NInput v-model:value="addForm.serviceAddress" type="textarea" placeholder="请输入详细服务地址" size="large" />
      </NFormItem>
      <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0 24px">
        <NFormItem label="预估费用">
          <NInputNumber v-model:value="addForm.estimatedPrice" :min="0" placeholder="元" size="large" style="width: 100%">
            <template #suffix>元</template>
          </NInputNumber>
        </NFormItem>
        <NFormItem label="补贴金额">
          <NInputNumber v-model:value="addForm.subsidyAmount" :min="0" placeholder="元" size="large" style="width: 100%">
            <template #suffix>元</template>
          </NInputNumber>
        </NFormItem>
        <NFormItem label="自付金额">
          <NInputNumber v-model:value="addForm.selfPayAmount" :min="0" placeholder="元" size="large" style="width: 100%">
            <template #suffix>元</template>
          </NInputNumber>
        </NFormItem>
      </div>
      <NFormItem label="特殊需求">
        <NInput v-model:value="addForm.specialRequirements" type="textarea" placeholder="请输入特殊需求" size="large" />
      </NFormItem>
    </NForm>
    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 12px">
        <NButton size="large" style="height: 48px; min-width: 80px" @click="emit('update:visible', false)">取消</NButton>
        <NButton type="primary" size="large" style="height: 48px; min-width: 80px" :loading="loading" @click="handleSubmit">确认创建</NButton>
      </div>
    </template>
  </NModal>
</template>

