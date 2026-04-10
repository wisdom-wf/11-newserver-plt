<script setup lang="ts">
import { reactive, watch, ref, onMounted } from 'vue';
import { NForm, NFormItem, NInput, NSelect, NSpace, NButton, NInputNumber, NTreeSelect } from 'naive-ui';
import { fetchGetMenuTree } from '@/service/api';

interface Props {
  operateType: 'add' | 'edit';
  editingData: Api.User.Menu | null;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'submit'): void;
}>();

const formRef = ref();

const model = reactive({
  id: '',
  menuName: '',
  menuCode: '',
  parentId: undefined,
  menuType: 'MENU',
  path: '',
  component: '',
  icon: '',
  sortOrder: 0,
  status: '1'
} as Api.User.Menu);

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuCode: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
};

const menuTypeOptions = [
  { label: '菜单', value: 'MENU' },
  { label: '按钮', value: 'BUTTON' },
  { label: '权限', value: 'PERMISSION' }
];

const statusOptions = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '2' }
];

const menuTreeOptions = ref<{ label: string; key: string; children?: any[] }[]>([]);
const expandedKeys = ref<string[]>([]);

async function loadMenuTree() {
  const { data } = await fetchGetMenuTree();
  menuTreeOptions.value = transformToTreeOptions(data || []);
}

function transformToTreeOptions(menus: Api.User.Menu[]): any[] {
  return menus.map(menu => ({
    label: menu.menuName,
    key: menu.id,
    children: menu.children && menu.children.length > 0 ? transformToTreeOptions(menu.children) : undefined
  }));
}

watch(
  () => props.operateType,
  type => {
    if (type === 'add') {
      Object.assign(model, {
        menuName: '',
        menuCode: '',
        parentId: null,
        menuType: 'MENU',
        path: '',
        component: '',
        icon: '',
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
  loadMenuTree();
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
    <NFormItem label="菜单名称" path="menuName">
      <NInput v-model:value="model.menuName" placeholder="请输入菜单名称" />
    </NFormItem>
    <NFormItem label="菜单编码" path="menuCode">
      <NInput v-model:value="model.menuCode" placeholder="请输入菜单编码" />
    </NFormItem>
    <NFormItem label="上级菜单" path="parentId">
      <NTreeSelect
        v-model:value="model.parentId"
        :options="menuTreeOptions"
        :expanded-keys="expandedKeys"
        placeholder="请选择上级菜单"
        clearable
        virtual-scroll
        @update:expanded-keys="keys => (expandedKeys = keys)"
      />
    </NFormItem>
    <NFormItem label="菜单类型" path="menuType">
      <NSelect v-model:value="model.menuType" :options="menuTypeOptions" placeholder="请选择菜单类型" />
    </NFormItem>
    <NFormItem v-if="model.menuType === 'MENU'" label="路由路径" path="path">
      <NInput v-model:value="model.path" placeholder="路由路径，如：/system/user" />
    </NFormItem>
    <NFormItem v-if="model.menuType === 'MENU'" label="组件路径" path="component">
      <NInput v-model:value="model.component" placeholder="组件路径，如：system/user/index" />
    </NFormItem>
    <NFormItem label="图标" path="icon">
      <NInput v-model:value="model.icon" placeholder="图标名称，如：mdi:user" />
    </NFormItem>
    <NFormItem label="排序" path="sortOrder">
      <NInputNumber v-model:value="model.sortOrder" :min="0" />
    </NFormItem>
    <NFormItem label="状态" path="status">
      <NSelect v-model:value="model.status" :options="statusOptions" />
    </NFormItem>
    <NSpace justify="end">
      <NButton @click="emit('close')">取消</NButton>
      <NButton type="primary" @click="handleSubmit">确认</NButton>
    </NSpace>
  </NForm>
</template>
