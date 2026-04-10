<script setup lang="ts">
import { reactive, watch, ref, onMounted } from 'vue';
import { NForm, NFormItem, NInput, NSelect, NSpace, NButton, NTreeSelect } from 'naive-ui';
import { fetchGetAreaTree } from '@/service/api';

interface Props {
  operateType: 'add' | 'edit';
  editingData: Api.User.Area | null;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'submit'): void;
}>();

const formRef = ref();

const model = reactive({
  id: '',
  areaCode: '',
  areaName: '',
  parentId: undefined,
  level: 'CITY',
  longitude: undefined,
  latitude: undefined,
  sortOrder: 0,
  status: '1'
} as Api.User.Area);

const rules = {
  areaCode: [{ required: true, message: '请输入区域编码', trigger: 'blur' }],
  areaName: [{ required: true, message: '请输入区域名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择区域层级', trigger: 'change' }]
};

const levelOptions = [
  { label: '省/直辖市', value: 'PROVINCE' },
  { label: '市/区', value: 'CITY' },
  { label: '区县', value: 'DISTRICT' },
  { label: '街道/乡镇', value: 'STREET' },
  { label: '社区', value: 'COMMUNITY' }
];

const statusOptions = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '2' }
];

const areaTreeOptions = ref<{ label: string; key: string; children?: any[] }[]>([]);
const expandedKeys = ref<string[]>([]);

async function loadAreaTree() {
  const { data } = await fetchGetAreaTree();
  areaTreeOptions.value = transformToTreeOptions(data || []);
}

function transformToTreeOptions(areas: Api.User.Area[]): any[] {
  return areas.map(area => ({
    label: area.areaName,
    key: area.id,
    children: area.children ? transformToTreeOptions(area.children) : undefined
  }));
}

watch(
  () => props.operateType,
  type => {
    if (type === 'add') {
      Object.assign(model, {
        areaCode: '',
        areaName: '',
        parentId: null,
        level: 'CITY',
        longitude: null,
        latitude: null,
        sortOrder: 0,
        status: '1'
      });
    } else if (type === 'edit' && props.editingData) {
      Object.assign(model, props.editingData);
    }
  },
  { immediate: true }
);

onMounted(() => {
  loadAreaTree();
});

async function handleSubmit() {
  formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      emit('submit');
    }
  });
}

defineExpose({
  model
});
</script>

<template>
  <NForm ref="formRef" :model="model" :rules="rules" label-placement="left" label-width="100">
    <NFormItem label="区域编码" path="areaCode">
      <NInput v-model:value="model.areaCode" placeholder="请输入区域编码" />
    </NFormItem>
    <NFormItem label="区域名称" path="areaName">
      <NInput v-model:value="model.areaName" placeholder="请输入区域名称" />
    </NFormItem>
    <NFormItem label="上级区域" path="parentId">
      <NTreeSelect
        v-model:value="model.parentId"
        :options="areaTreeOptions"
        :expanded-keys="expandedKeys"
        placeholder="请选择上级区域"
        clearable
        virtual-scroll
        @update:expanded-keys="keys => (expandedKeys = keys)"
      />
    </NFormItem>
    <NFormItem label="区域层级" path="level">
      <NSelect v-model:value="model.level" :options="levelOptions" placeholder="请选择区域层级" />
    </NFormItem>
    <NFormItem label="状态" path="status">
      <NSelect v-model:value="model.status" :options="statusOptions" />
    </NFormItem>
    <NFormItem label="排序" path="sortOrder">
      <NInputNumber v-model:value="model.sortOrder" :min="0" />
    </NFormItem>
    <NSpace justify="end">
      <NButton @click="emit('close')">取消</NButton>
      <NButton type="primary" @click="handleSubmit">确认</NButton>
    </NSpace>
  </NForm>
</template>
