<script setup lang="ts">
import { ref, h, onMounted, watch, computed } from 'vue';
import { NButton, NCard, NTag, NSpace, NInput, NSelect, NDrawer, NDrawerContent, useMessage, NImage, NImageGroup, NUpload, NGrid, NGi, NPopconfirm, NTabs, NTabPane, NAvatar, NRate, NEmpty, NSpin, NModal, NAlert, NDescriptions, NDescriptionsItem, NForm, NFormItem, NDatePicker, NInputNumber, NCollapse, NCollapseItem, type UploadFile } from 'naive-ui';
import PersonCard from '@/components/common/person-card.vue';
import LazyImage from '@/components/common/lazy-image.vue';
import EmptyState from '@/components/common/empty-state.vue';
import type { DataTableColumns } from 'naive-ui';
import { useNaiveForm, useFormRules } from '@/hooks/common/form';
import { useNaivePaginatedTable, useTableOperate, defaultTransform } from '@/hooks/common/table';
import { useAuth } from '@/hooks/business/auth';
import { useAuthStore } from '@/store/modules/auth';
import { useRouterPush } from '@/hooks/common/router';
import { useRoute, useRouter } from 'vue-router';
import TableHeaderOperation from '@/components/advanced/table-header-operation.vue';
import {
  fetchGetStaffList,
  fetchCreateStaff,
  fetchUpdateStaff,
  fetchDeleteStaff,
  fetchBatchDeleteStaff,
  fetchGetStaffStatistics,
  fetchGetProviderOptions,
  fetchGetStaff,
  fetchGetStaffServiceLogs,
  fetchResetStaffPassword,
  fetchGetStaffQualifications,
  fetchGetStaffQualificationsPreview,
  fetchGetQualificationImages,
  fetchAddStaffQualification,
  fetchUpdateStaffQualification,
  fetchDeleteStaffQualification
} from '@/service/api';

defineOptions({
  name: 'BusinessStaff'
});

const message = useMessage();
const { formRef, validate, restoreValidation } = useNaiveForm();
const { defaultRequiredRule } = useFormRules();
const { hasAuth } = useAuth();
const authStore = useAuthStore();
const { routerPushByKeyWithMetaQuery } = useRouterPush();
const route = useRoute();
const router = useRouter();

// 当前用户是否为服务商管理员
const isProviderAdmin = computed(() => authStore.userInfo.userType === 'PROVIDER');
// 当前服务商管理员的providerId
const currentProviderId = computed(() => authStore.userInfo.providerId || '');
// 当前服务商管理员的服务商名称
const currentProviderName = computed(() => authStore.userInfo.providerName || '');

// Detail drawer state
const detailVisible = ref(false);
const detailData = ref<Api.Staff.Staff | null>(null);
const detailLoading = ref(false);
const MAX_IMAGE_COUNT = 6;

// View type (list or card)
const viewType = ref<'list' | 'card'>('list');

// Service logs for detail drawer
const serviceLogs = ref<Api.Staff.StaffServiceLog[]>([]);
const serviceLogsLoading = ref(false);
const detailActiveTab = ref('basic');

// Qualifications for detail drawer
const qualifications = ref<Api.Staff.Qualification[]>([]);
const qualificationsLoading = ref(false);

// 资质图片懒加载状态
const imageCache = ref<Record<string, string>>({});  // qualificationId -> certificateUrls
const loadingImages = ref<Set<string>>(new Set());   // 正在加载的 qualificationId

// === 新资质管理交互状态（按类型分组，局部刷新） ===
// 资质类型名称映射
const QUALIFICATION_TYPE_NAMES = ['身份证', '健康证', '职业资格证', '培训证书', '无犯罪记录证明', '其他'];

// 折叠面板展开状态（默认全部折叠）
const expandedStates = ref<Set<number | string>>(new Set());

// 各上传按钮的 loading 状态
const uploadingTypes = ref<Set<number | string>>(new Set());

// 上传确认对话框（用于覆盖/追加选择）
const uploadConfirmVisible = ref(false);
const pendingBase64 = ref<string | null>(null);
const uploadMode = ref<'overwrite' | 'add' | null>(null);
const uploadLoading = ref(false);
// 暂存当前操作的资质对象
const pendingQualification = ref<Api.Staff.Qualification | null>(null);

// 添加其他资质弹窗
const showAddOtherDialog = ref(false);
const newOtherName = ref('');

// 根据类型获取资质列表
function getQualificationsByType(type: number): Api.Staff.Qualification[] {
  return qualifications.value.filter(q => q.qualificationType === type);
}

// 获取"其他"类型的不同名称列表
function getOtherTypeNames(): string[] {
  const others = qualifications.value.filter(q => q.qualificationType === 5);
  return [...new Set(others.map(q => q.qualificationName))];
}

// 获取指定名称的"其他"资质
function getQualificationsByOtherName(name: string): Api.Staff.Qualification[] {
  return qualifications.value.filter(q => q.qualificationType === 5 && q.qualificationName === name);
}

// 获取资质的图片URLs（从缓存读取，统一使用string key）
function getQualificationImageUrls(qual: Api.Staff.Qualification | undefined): string | undefined {
  if (!qual) return undefined;
  return imageCache.value[String(qual.qualificationId)];
}

// 检查资质是否正在加载图片
function isQualificationLoading(qual: Api.Staff.Qualification | undefined): boolean {
  if (!qual) return false;
  return loadingImages.value.has(String(qual.qualificationId));
}

// 根据类型和名称查找资质索引
function findQualificationIndex(type: number, name?: string): number {
  return qualifications.value.findIndex(q => {
    if (type !== 5) return q.qualificationType === type;
    return q.qualificationType === 5 && q.qualificationName === name;
  });
}

// 本地添加或更新资质（局部刷新核心）
function upsertQualification(newQual: Api.Staff.Qualification) {
  const index = findQualificationIndex(
    newQual.qualificationType,
    newQual.qualificationType === 5 ? newQual.qualificationName : undefined
  );
  if (index >= 0) {
    qualifications.value[index] = newQual;
  } else {
    qualifications.value.push(newQual);
  }
}

// 获取资质类型名称
function getQualificationTypeName(type: number): string {
  return QUALIFICATION_TYPE_NAMES[type] || '其他';
}

// 处理图片上传（支持局部刷新）
async function handleImageUpload(opt: { file: UploadFile }, targetType: number, targetName?: string) {
  const file = opt.file;
  if (!file || !file.file) return false;
  const staffId = detailData.value?.staffId;
  if (!staffId) return false;

  // 设置当前上传状态
  const uploadKey = targetType === 5 ? `other-${targetName}` : targetType;
  uploadingTypes.value.add(uploadKey);

  // 是否显示了覆盖/追加对话框
  let dialogShown = false;

  try {
    // Validate file size (max 2MB) - 提前验证，避免等半天
    const MAX_SIZE = 2 * 1024 * 1024;
    if (file.file.size > MAX_SIZE) {
      message.error('图片大小不能超过2MB');
      return false;
    }
    // Validate MIME type
    const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
    if (!ALLOWED_TYPES.includes(file.file.type)) {
      message.error('只能上传 JPG/PNG/WEBP 格式的图片');
      return false;
    }

    // 转为Base64
    message.loading('正在处理图片...', { duration: 0 });
    const base64 = await new Promise<string>((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e) => resolve(e.target?.result as string);
      reader.onerror = reject;
      reader.readAsDataURL(file.file as Blob);
    });
    message.destroyAll();

    // 查找是否已有该类型的资质
    const existingIndex = findQualificationIndex(targetType, targetName);
    const existing = existingIndex >= 0 ? qualifications.value[existingIndex] : null;

    if (existing) {
      // 已有资质，暂存并弹出覆盖/追加确认
      pendingBase64.value = base64;
      pendingQualification.value = existing;
      uploadConfirmVisible.value = true;
      dialogShown = true;
      return false;
    }

    // 新增资质（局部刷新）
    const qualName = targetType === 5 ? targetName! : getQualificationTypeName(targetType);
    const { data, error } = await fetchAddStaffQualification(staffId, {
      qualificationType: targetType,
      qualificationName: qualName,
      certificateUrls: base64
    } as Api.Staff.QualificationForm);
    if (error) {
      message.error('上传失败');
      return false;
    }
    if (data) {
      // 预览模式：certificateUrls 显示为 HAS_IMAGES，但缓存真实图片
      // 注意：qualificationId 可能是 string 或 number，统一转为 string 确保缓存 key 一致
      const cacheKey = String(data.qualificationId);
      data.certificateUrls = 'HAS_IMAGES';
      qualifications.value.push(data);
      imageCache.value[cacheKey] = base64;
    }
    message.success('✓ 已保存');
    return false;
  } finally {
    // 无论成功/失败/对话框，都要在最后清除 loading 状态
    // 对话框的确认/取消回调也会清除，这里做双重保障
    uploadingTypes.value.delete(uploadKey);
  }
}

// 删除资质图片（单张）- 局部刷新
async function deleteQualificationImage(qual: Api.Staff.Qualification, url: string) {
  const cacheKey = String(qual.qualificationId);
  const urls = qual.certificateUrls.split('|||').filter(u => u !== url);
  // 本地立即更新
  if (urls.length === 0) {
    qual.certificateUrls = undefined;
    // 清除缓存
    delete imageCache.value[cacheKey];
  } else {
    qual.certificateUrls = urls.join('|||');
    // 更新缓存
    imageCache.value[cacheKey] = urls.join('|||');
  }
  const { error } = await fetchUpdateStaffQualification(qual.qualificationId, {
    ...qual,
    certificateUrls: qual.certificateUrls
  } as Api.Staff.QualificationForm);
  if (error) {
    message.error('删除失败');
    return;
  }
  message.success('已删除');
}

// 删除整个资质 - 局部刷新
async function deleteQualification(qualificationId: string) {
  const cacheKey = String(qualificationId);
  // 本地立即移除
  const index = qualifications.value.findIndex(q => String(q.qualificationId) === cacheKey);
  if (index >= 0) {
    qualifications.value.splice(index, 1);
  }
  // 清除缓存
  delete imageCache.value[cacheKey];
  const { error } = await fetchDeleteStaffQualification(qualificationId);
  if (error) {
    message.error(error.message || '删除失败');
    return;
  }
  message.success('删除成功');
}

// 覆盖确认回调 - 局部刷新
async function handleOverwriteConfirm() {
  uploadConfirmVisible.value = false;
  if (!pendingBase64.value || !pendingQualification.value) return;
  const existing = pendingQualification.value;
  const uploadKey = existing.qualificationType === 5
    ? `other-${existing.qualificationName}`
    : existing.qualificationType;
  // 统一使用 string 作为 cache key
  const cacheKey = String(existing.qualificationId);
  // 本地立即更新
  existing.certificateUrls = pendingBase64.value;
  // 更新缓存
  imageCache.value[cacheKey] = pendingBase64.value;
  // 清除对话框的 loading 状态（双重保障，handleImageUpload 的 finally 也会清除）
  uploadingTypes.value.delete(uploadKey);
  try {
    const { error } = await fetchUpdateStaffQualification(existing.qualificationId, {
      ...existing,
      certificateUrls: pendingBase64.value
    } as Api.Staff.QualificationForm);
    if (error) {
      message.error('覆盖失败');
      return;
    }
    message.success('✓ 已覆盖');
  } finally {
    pendingBase64.value = null;
    pendingQualification.value = null;
  }
}

// 追加确认回调 - 局部刷新
async function handleAddConfirm() {
  uploadConfirmVisible.value = false;
  if (!pendingBase64.value || !pendingQualification.value) return;
  const existing = pendingQualification.value;
  const uploadKey = existing.qualificationType === 5
    ? `other-${existing.qualificationName}`
    : existing.qualificationType;
  // 统一使用 string 作为 cache key
  const cacheKey = String(existing.qualificationId);
  // 从 imageCache 获取真实 URL 进行追加，而不是从 preview 模式的 certificateUrls
  // 因为 preview 模式下 certificateUrls 是 "HAS_IMAGES" 字符串，不是真实 URL
  const existingUrls = imageCache.value[cacheKey]
    ? imageCache.value[cacheKey].split('|||')
    : [];
  const newUrls = existingUrls.concat(pendingBase64.value);
  // 本地立即更新
  existing.certificateUrls = newUrls.join('|||');
  // 更新缓存
  imageCache.value[cacheKey] = newUrls.join('|||');
  // 清除对话框的 loading 状态（双重保障）
  uploadingTypes.value.delete(uploadKey);
  try {
    const { error } = await fetchUpdateStaffQualification(existing.qualificationId, {
      ...existing,
      certificateUrls: newUrls.join('|||')
    } as Api.Staff.QualificationForm);
    if (error) {
      message.error('新增失败');
      return;
    }
    message.success('✓ 已新增');
  } finally {
    pendingBase64.value = null;
    pendingQualification.value = null;
  }
}

// 添加其他资质
async function addOtherQualification() {
  if (!newOtherName.value.trim()) {
    message.warning('请输入资质名称');
    return;
  }
  if (!detailData.value?.staffId) return;

  const name = newOtherName.value.trim();
  // 创建空资质条目
  const { data, error } = await fetchAddStaffQualification(detailData.value.staffId, {
    qualificationType: 5,
    qualificationName: name,
    certificateUrls: ''
  } as Api.Staff.QualificationForm);

  if (error) {
    message.error('新增失败');
    return;
  }

  if (data) {
    qualifications.value.push(data);
    // 自动展开新的面板
    expandedStates.value.add('other-' + name);
  }
  showAddOtherDialog.value = false;
  newOtherName.value = '';
  message.success('已新增，请在下方上传证书');
}
async function addQualificationForNewType() {
  // 空实现，新增类型由 addOtherQualification 处理
}

async function showDetail(row: Api.Staff.Staff) {
  detailLoading.value = true;
  try {
    const { data, error } = await fetchGetStaff(row.staffId);
    if (error) {
      message.error(error.message || '获取详情失败');
      return;
    }
    if (data) {
      detailData.value = data;
      detailVisible.value = true;
      detailActiveTab.value = 'basic';
      // Load service logs and qualifications
      loadServiceLogs(row.staffId);
      loadQualifications(row.staffId);
    }
  } finally {
    detailLoading.value = false;
  }
}

async function loadServiceLogs(staffId: string) {
  serviceLogsLoading.value = true;
  try {
    const { data, error } = await fetchGetStaffServiceLogs(staffId, 20);
    if (error) {
      console.error('Failed to load service logs', error);
      return;
    }
    serviceLogs.value = data || [];
  } catch (e) {
    console.error('Failed to load service logs', e);
  } finally {
    serviceLogsLoading.value = false;
  }
}

async function loadQualifications(staffId: string) {
  qualificationsLoading.value = true;
  try {
    // 使用预览API（不含图片URL），图片按需加载
    const { data, error } = await fetchGetStaffQualificationsPreview(staffId);
    if (error) {
      console.error('Failed to load qualifications', error);
      return;
    }
    qualifications.value = data || [];
  } catch (e) {
    console.error('Failed to load qualifications', e);
  } finally {
    qualificationsLoading.value = false;
  }
}

// 懒加载资质图片（展开面板时调用）
async function loadQualificationImages(qualificationId: string) {
  // 统一使用 string 作为 cache key，避免类型不一致
  const cacheKey = String(qualificationId);
  // 已有缓存或正在加载，直接返回
  if (imageCache.value[cacheKey] || loadingImages.value.has(cacheKey)) {
    return;
  }
  loadingImages.value.add(cacheKey);
  try {
    const { data, error } = await fetchGetQualificationImages(qualificationId);
    if (error || !data) {
      return;
    }
    imageCache.value[cacheKey] = data;
  } finally {
    loadingImages.value.delete(cacheKey);
  }
}

// 处理面板展开事件（按需加载图片）
function handlePanelExpand(name: number | string) {
  const nameStr = String(name);
  // 固定类型 0-4
  if (/^[0-4]$/.test(nameStr)) {
    const type = parseInt(nameStr);
    const quals = getQualificationsByType(type);
    for (const q of quals) {
      if (q.certificateUrls === 'HAS_IMAGES') {
        loadQualificationImages(String(q.qualificationId));
      }
    }
  }
  // 其他类型 "other-xxx"
  else if (nameStr.startsWith('other-')) {
    const otherName = nameStr.slice(6);
    const quals = getQualificationsByOtherName(otherName);
    for (const q of quals) {
      if (q.certificateUrls === 'HAS_IMAGES') {
        loadQualificationImages(String(q.qualificationId));
      }
    }
  }
}

function onTabChange(tab: string) {
  if (tab === 'qualifications' && detailData.value && qualifications.value.length === 0) {
    loadQualifications(detailData.value.staffId);
  }
}

// 资质类型名称映射（已迁移到 QUALIFICATION_TYPE_NAMES 常量）

const rules = {
  staffName: [defaultRequiredRule],
  phone: [defaultRequiredRule],
  providerId: [defaultRequiredRule]
};

// Statistics
const statistics = ref<Api.Staff.Statistics>({
  total: 0,
  active: 0,
  pending: 0,
  inactive: 0,
  avgRating: 0
});

// Search
const searchName = ref('');
const searchPhone = ref('');
const searchServiceCategory = ref('');
const searchStatus = ref('');
const searchInsuranceStatus = ref<number | null>(null);

// Gender options
const genderOptions = [
  { label: '女', value: 0 },
  { label: '男', value: 1 }
];

// Status options
const statusOptions = [
  { label: '待上岗', value: 'PENDING' },
  { label: '在职', value: 'ON_JOB' },
  { label: '离职', value: 'OFF_JOB' }
];

// Insurance status options (平台统一集采保险)
const insuranceStatusOptions = [
  { label: '未参保', value: 0, color: '#999' },
  { label: '正在参保', value: 1, color: '#52c41a' },
  { label: '已参保', value: 2, color: '#1890ff' },
  { label: '已过期', value: 3, color: '#ff4d4f' }
];

// Provider options for dropdown
const providerOptions = ref<{ label: string; value: string }[]>([]);
const providerOptionsLoading = ref(false);

// Service category options
const categoryOptions = [
  { label: '养老服务', value: 'ELDER_CARE' },
  { label: '家政服务', value: 'HOME_CARE' }
];

function getGenderLabel(gender?: number | string): string {
  const g = Number(gender);
  if (g === 0) return '女';
  if (g === 1) return '男';
  return '未知';
}

// 重置密码
async function resetPassword() {
  if (!detailData.value?.staffId) return;
  try {
    await fetchResetStaffPassword(detailData.value.staffId);
    message.success('密码已重置为 mima123');
  } catch (e) {
    console.error('Failed to reset password', e);
  }
}

function getStatusType(status?: string): 'success' | 'warning' | 'error' | 'default' {
  if (status === 'PENDING') return 'warning';
  if (status === 'ON_JOB') return 'success';
  if (status === 'OFF_JOB') return 'error';
  return 'default';
}

function getStatusLabel(status?: string): string {
  if (status === 'PENDING') return '待上岗';
  if (status === 'ON_JOB') return '在职';
  if (status === 'OFF_JOB') return '离职';
  return status || '';
}

function getInsuranceStatusLabel(val?: number): string {
  if (val === 0) return '未参保';
  if (val === 1) return '正在参保';
  if (val === 2) return '已参保';
  if (val === 3) return '已过期';
  return val === undefined || val === null ? '' : '未知';
}

function getInsuranceStatusColor(val?: number): string {
  if (val === 0) return '#999';
  if (val === 1) return '#52c41a';
  if (val === 2) return '#1890ff';
  if (val === 3) return '#ff4d4f';
  return '#999';
}

const tableColumns: DataTableColumns<Api.Staff.Staff> = [
  { type: 'selection' },
  { title: '工号', key: 'staffNo', width: 110 },
  { title: '姓名', key: 'staffName', width: 90 },
  { title: '性别', key: 'gender', width: 60, render: row => getGenderLabel(row.gender) },
  { title: '年龄', key: 'age', width: 60 },
  { title: '手机号', key: 'phone', width: 120 },
  { title: '所属服务商', key: 'providerName', width: 180 },
  { title: '身份证号', key: 'idCard', width: 170 },
  { title: '紧急联系人', key: 'emergencyContact', width: 100 },
  { title: '紧急联系电话', key: 'emergencyPhone', width: 130 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: row => h(NTag, { type: getStatusType(row.status), size: 'small' }, () => getStatusLabel(row.status))
  },
  {
    title: '参保状态',
    key: 'insuranceStatus',
    width: 90,
    render: row => {
      const ins = row.insuranceStatus;
      const type = ins === 2 ? 'success' : ins === 1 ? 'warning' : ins === 3 ? 'error' : 'default';
      const label = ['', '未参保', '正在参保', '已参保', '已过期'][ins ?? 0] || '未参保';
      return h(NTag, { type, size: 'small' }, () => label);
    }
  },
  { title: '入职日期', key: 'hireDate', width: 110 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: row => {
      const buttons: any[] = [];
      buttons.push(h(NButton, { size: 'small', type: 'info', onClick: () => showDetail(row), style: { marginRight: '8px' } }, () => '详情'));
      if (hasAuth('staff:list:edit')) {
        buttons.push(h(NButton, { size: 'small', onClick: () => handleEdit(row.staffId), style: { marginRight: '8px' } }, () => '编辑'));
      }
      if (hasAuth('staff:list:delete')) {
        buttons.push(
          h(
            NPopconfirm,
            {
              onPositiveClick: () => handleDelete(row.staffId)
            },
            {
              trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
              default: () => '确认删除?'
            }
          )
        );
      }
      return h(NSpace, { size: 'small' }, () => buttons);
    }
  }
];

// Use framework's table hook
const {
  data: tableData,
  loading,
  pagination,
  mobilePagination,
  getData,
  getDataByPage,
  columns: filteredColumns,
  columnChecks
} = useNaivePaginatedTable<Api.Common.PaginatingQueryRecord<Api.Staff.Staff>, Api.Staff.Staff>({
  apiFn: async params => {
    const queryParams: any = {
      page: params.page,
      pageSize: params.pageSize
    };
    if (searchName.value) queryParams.staffName = searchName.value;
    if (searchPhone.value) queryParams.phone = searchPhone.value;
    if (searchServiceCategory.value) queryParams.serviceType = searchServiceCategory.value;
    if (searchStatus.value) queryParams.status = searchStatus.value;
    if (searchInsuranceStatus.value !== null) queryParams.insuranceStatus = searchInsuranceStatus.value;
    return fetchGetStaffList(queryParams);
  },
  apiParams: {
    page: 1,
    pageSize: 10
  },
  transform: defaultTransform,
  columns: () => tableColumns
});

// Use framework's table operate hook
const {
  drawerVisible,
  operateType,
  handleAdd,
  editingData,
  handleEdit,
  checkedRowKeys,
  onBatchDeleted,
  onDeleted,
  closeDrawer
} = useTableOperate(tableData, 'staffId', getData);

const form = ref({
  staffName: '',
  gender: 0 as 0 | 1,
  idCard: '',
  phone: '',
  providerId: '',
  emergencyContact: '',
  emergencyPhone: '',
  remark: '',
  status: 'PENDING' as 'PENDING' | 'ON_JOB' | 'OFF_JOB',
  insuranceStatus: 0 as 0 | 1 | 2 | 3
});

// Reset form to empty state
function resetForm() {
  form.value = {
    staffName: '',
    gender: 0,
    idCard: '',
    phone: '',
    providerId: isProviderAdmin.value ? currentProviderId.value : '',
    emergencyContact: '',
    emergencyPhone: '',
    remark: '',
    status: 'PENDING',
    insuranceStatus: 0
  };
}

// Watch editingData to fill form when editing
watch(
  () => editingData.value,
  data => {
    if (data) {
      form.value = {
        staffName: data.staffName || '',
        gender: (data.gender ?? 0) as 0 | 1,
        idCard: data.idCard || '',
        phone: data.phone || '',
        providerId: data.providerId || '',
        emergencyContact: data.emergencyContact || '',
        emergencyPhone: data.emergencyPhone || '',
        remark: data.remark || '',
        status: (data.status as 'PENDING' | 'ON_JOB' | 'OFF_JOB') || 'PENDING',
        insuranceStatus: data.insuranceStatus ?? 0
      };
    } else {
      resetForm();
    }
  },
  { immediate: true }
);

// Calculate age from ID card number (18-digit Chinese ID card)
function calculateAge(idCard: string): number {
  if (!idCard || idCard.length !== 18) return 0;
  const birthYear = parseInt(idCard.substring(6, 10));
  const birthMonth = parseInt(idCard.substring(10, 12));
  const birthDay = parseInt(idCard.substring(12, 14));
  const now = new Date();
  const birthDate = new Date(birthYear, birthMonth - 1, birthDay);
  let age = now.getFullYear() - birthYear;
  const monthDiff = now.getMonth() - birthDate.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && now.getDate() < birthDate.getDate())) {
    age--;
  }
  return age;
}

// Display age computed from ID card
const displayAge = computed(() => {
  if (!form.value.idCard || form.value.idCard.length !== 18) return '';
  return calculateAge(form.value.idCard);
});

// Watch idCard changes to auto-fill gender
watch(
  () => form.value.idCard,
  idCard => {
    if (idCard && idCard.length === 18) {
      // Extract gender from digit 17 (index 16): odd=male(1), even=female(0)
      const genderDigit = parseInt(idCard.charAt(16));
      form.value.gender = genderDigit % 2 === 0 ? 0 : 1;
    }
  }
);

// 监听对话框关闭（用户点X或遮罩关闭，而非确认操作）
watch(
  () => uploadConfirmVisible.value,
  (visible, prevVisible) => {
    if (prevVisible && !visible && pendingQualification.value) {
      // 对话框被关闭但没有确认，清除 loading 状态
      const q = pendingQualification.value;
      const uploadKey = q.qualificationType === 5
        ? `other-${q.qualificationName}`
        : q.qualificationType;
      uploadingTypes.value.delete(uploadKey);
      pendingBase64.value = null;
      pendingQualification.value = null;
    }
  }
);

async function getStatistics() {
  try {
    const { data } = await fetchGetStaffStatistics();
    if (data) {
      statistics.value = data;
    }
  } catch (e) {
    console.error('Failed to get statistics', e);
  }
}

async function handleDelete(staffId: string) {
  try {
    await fetchDeleteStaff(staffId);
    message.success('删除成功');
    await getData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to delete', e);
  }
}

async function handleBatchDelete() {
  if (!checkedRowKeys.value.length) return;
  try {
    await fetchBatchDeleteStaff(checkedRowKeys.value);
    message.success('批量删除成功');
    await getData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to batch delete', e);
  }
}

// 照片上传处理
async function handlePhotoUpload(staffId: string, file: File) {
  const reader = new FileReader();
  reader.onload = async (e) => {
    if (e.target?.result) {
      const base64Data = e.target.result as string;
      try {
        const { error } = await fetchUpdateStaff(staffId, { avatarUrl: base64Data } as any);
        if (error) {
          message.error(error.message || '上传失败');
          return;
        }
        message.success('上传成功');
        await getData();
        await getStatistics();
      } catch (err) {
        message.error('上传失败');
      }
    }
  };
  reader.readAsDataURL(file);
}

// 跳转到服务日志页面
function handleViewServiceLogs(staff: Api.Staff.Staff) {
  routerPushByKeyWithMetaQuery('business_service-log', { staffId: staff.staffId });
}

// 账户信息弹窗
const accountModalVisible = ref(false);
const accountInfo = ref<{ username: string; password: string; staffName: string } | null>(null);

async function handleSubmit() {
  try {
    await validate();
  } catch {
    return;
  }
  try {
    if (operateType.value === 'add') {
      const result = await fetchCreateStaff(form.value);
      message.success('新增成功');
      // 如果创建了账户，显示账户信息
      const data = (result as any)?.data || result;
      if (data?.accountCreated && data.username && data.password) {
        accountInfo.value = {
          username: data.username,
          password: data.password,
          staffName: form.value.staffName || ''
        };
        accountModalVisible.value = true;
      }
    } else if (editingData.value) {
      await fetchUpdateStaff(editingData.value.staffId, form.value);
      message.success('修改成功');
    }
    closeDrawer();
    await getData();
    await getStatistics();
  } catch (e) {
    console.error('Failed to submit', e);
  }
}

// 复制账户信息
async function copyAccountInfo() {
  if (accountInfo.value) {
    const text = `用户名: ${accountInfo.value.username}\n密码: ${accountInfo.value.password}`;
    await navigator.clipboard.writeText(text);
    message.success('已复制到剪贴板');
  }
}

function handleResetSearch() {
  searchName.value = '';
  searchPhone.value = '';
  searchServiceCategory.value = '';
  searchStatus.value = null;
  searchInsuranceStatus.value = null;
  getDataByPage(1);
}

// Quick update insurance status inline
async function quickUpdateInsuranceStatus(staffId: string, newStatus: number) {
  try {
    await fetchUpdateStaffInsuranceStatus(staffId, newStatus);
    message.success('参保状态已更新');
    getData();
  } catch (e) {
    console.error('Failed to update insurance status', e);
    message.error('更新失败');
  }
}

function handleStatusPillClick(statusValue: string | null) {
  searchStatus.value = statusValue;
  pagination.page = 1;
  getData();
}

function handleInsuranceStatusPillClick(val: number | null) {
  searchInsuranceStatus.value = val;
  pagination.page = 1;
  getData();
}

async function getProviderOptions() {
  providerOptionsLoading.value = true;
  try {
    const data = await fetchGetProviderOptions();
    if (data) {
      providerOptions.value = data.map((p: { id: string; name: string }) => ({
        label: p.name,
        value: p.id
      }));
    }
  } catch (e) {
    console.error('Failed to get provider options', e);
  } finally {
    providerOptionsLoading.value = false;
  }
}

onMounted(async () => {
  // 接收服务人员详情跳转参数（订单详情→服务人员详情），直接打开该服务人员详情抽屉
  if (route.query.staffId) {
    const staffId = String(route.query.staffId);
    try {
      const { data } = await fetchGetStaff(staffId);
      if (data) {
        detailData.value = data;
        detailVisible.value = true;
        detailActiveTab.value = 'basic';
        loadServiceLogs(staffId);
      }
    } catch (e) {
      console.error('Failed to load staff detail from route', e);
    }
  }
  getStatistics();
  getData();
  getProviderOptions();
});
</script>

<template>
  <div>
    <!-- Statistics Strip - compact single row -->
    <div style="display: flex; align-items: center; gap: 0; margin-bottom: 12px; font-size: 13px; color: #666">
      <span style="font-weight: 600; margin-right: 16px; font-size: 14px">服务人员统计</span>
      <span style="padding: 4px 14px; background: #1890ff18; color: #1890ff; border-radius: 4px; font-weight: 600">总人数 {{ statistics.total }}</span>
      <span style="padding: 4px 14px; background: #52c41a18; color: #52c41a; border-radius: 4px; font-weight: 600; margin-left: 8px">在职 {{ statistics.active }}</span>
      <span style="padding: 4px 14px; background: #fa8c1618; color: #fa8c16; border-radius: 4px; font-weight: 600; margin-left: 8px">待上岗 {{ statistics.pending }}</span>
      <span style="padding: 4px 14px; background: #ff4d4f18; color: #ff4d4f; border-radius: 4px; font-weight: 600; margin-left: 8px">已离职 {{ statistics.inactive }}</span>
      <span style="padding: 4px 14px; background: #722ed118; color: #722ed1; border-radius: 4px; font-weight: 600; margin-left: 8px">平均评分 {{ Number(statistics.avgRating || 0).toFixed(1) }}</span>
    </div>

    <!-- Table -->
    <NCard :bordered="false" style="margin-bottom: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%">
          <span>服务人员管理</span>
          <NSpace>
            <NButton
              :type="viewType === 'list' ? 'primary' : 'default'"
              size="small"
              quaternary
              @click="viewType = 'list'"
            >
              列表
            </NButton>
            <NButton
              :type="viewType === 'card' ? 'primary' : 'default'"
              size="small"
              quaternary
              @click="viewType = 'card'"
            >
              卡片
            </NButton>
          </NSpace>
        </div>
      </template>
      <div style="background: #f5f5f5; padding: 12px; margin-bottom: 12px; border-radius: 8px">
        <!-- 适老化：状态快捷筛选 Pill - 合并一行 -->
        <div style="display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin-bottom: 10px">
          <span style="font-size: 13px; color: #999; font-weight: 500; margin-right: 4px">在职状态</span>
          <button
            :class="!searchStatus ? 'status-pill active' : 'status-pill'"
            :style="!searchStatus ? '' : 'background:#fff;border-color:#d1d5db'"
            @click="handleStatusPillClick('')"
          >全部</button>
          <button
            v-for="opt in statusOptions"
            :key="opt.value"
            :class="searchStatus === opt.value ? 'status-pill active' : 'status-pill'"
            :style="searchStatus === opt.value ? '' : 'background:#fff;border-color:#d1d5db'"
            @click="handleStatusPillClick(opt.value)"
          >
            {{ opt.label }}
          </button>
          <span style="width: 1px; height: 20px; background: #e5e5e5; margin: 0 6px"></span>
          <span style="font-size: 13px; color: #999; font-weight: 500; margin-right: 4px">参保状态</span>
          <button
            :class="searchInsuranceStatus === null ? 'status-pill active' : 'status-pill'"
            :style="searchInsuranceStatus === null ? '' : 'background:#fff;border-color:#d1d5db'"
            @click="handleInsuranceStatusPillClick(null)"
          >全部</button>
          <button
            v-for="opt in insuranceStatusOptions"
            :key="opt.value"
            :class="searchInsuranceStatus === opt.value ? 'status-pill active' : 'status-pill'"
            :style="searchInsuranceStatus === opt.value ? `background:${opt.color}20;border-color:${opt.color};color:${opt.color}` : 'background:#fff;border-color:#d1d5db'"
            @click="handleInsuranceStatusPillClick(opt.value)"
          >
            {{ opt.label }}
          </button>
        </div>
        <!-- 搜索条件行 -->
        <div style="display: flex; flex-wrap: wrap; gap: 10px; align-items: center">
          <NInput v-model:value="searchName" placeholder="姓名搜索" clearable style="width: 160px" size="medium" />
          <NInput v-model:value="searchPhone" placeholder="手机号" clearable style="width: 140px" size="medium" />
          <NSelect
            v-model:value="searchServiceCategory"
            :options="categoryOptions"
            placeholder="服务类别"
            clearable
            style="width: 160px"
          />
          <NButton type="primary" @click="() => { getData(); pagination.page = 1; }" style="height: 40px; font-size: 15px; font-weight: 600">搜索</NButton>
          <NButton @click="handleResetSearch" style="height: 40px; font-size: 15px">重置</NButton>
        </div>
      </div>

      <!-- Use framework's TableHeaderOperation component -->
      <TableHeaderOperation
        v-model:columns="columnChecks"
        :disabled-delete="checkedRowKeys.length === 0"
        :loading="loading"
        @add="handleAdd"
        @refresh="getData"
        @delete="handleBatchDelete"
      >
      </TableHeaderOperation>

      <!-- List View -->
      <NDataTable
        v-if="viewType === 'list'"
        :columns="filteredColumns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1400"
        :row-key="(row: Api.Staff.Staff) => row.staffId"
        v-model:checked-row-keys="checkedRowKeys"
        remote
        :pagination="mobilePagination"
      />

      <!-- Card View - 使用PersonCard组件 -->
      <div v-else style="display: flex; flex-wrap: wrap; gap: 12px">
        <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 5px; width: 100%">
          <PersonCard
            v-for="staff in tableData"
            :key="staff.staffId"
            :photo-url="staff.avatarUrl"
            :name="staff.staffName || '未知'"
            :subtitle="staff.phone || '-'"
            :staff-no="staff.staffNo || ''"
            :insurance-status="staff.insuranceStatus"
            :monthly-star="staff.monthlyStar === 1"
            :quarterly-star="staff.quarterlyStar === 1"
            :extra-info="[
              { label: '在职', value: getStatusLabel(staff.status), color: staff.status === 'ON_JOB' ? '#52c41a' : staff.status === 'PENDING' ? '#fa8c16' : '#ff4d4f' },
              ...(staff.serviceTypes ? [{ label: '服务类型', value: staff.serviceTypes.split(',')[0] }] : [])
            ]"
            :index-value="staff.rating ? Number(staff.rating) : undefined"
            index-label="评分"
            photo-width="85"
            scale="1"
            :show-upload-btn="true"
            @click="showDetail(staff)"
            @photo-upload="(file) => handlePhotoUpload(staff.staffId, file)"
          />
        </div>
        <EmptyState v-if="tableData.length === 0" icon="staff" title="暂无服务人员" description="点击上方新增按钮添加第一位员工" />
      </div>
    </NCard>

    <!-- Staff Drawer -->
    <NDrawer v-model:show="drawerVisible" :width="500" placement="right" closable>
      <NDrawerContent :title="operateType === 'add' ? '新增服务人员' : '编辑服务人员'" closable>
        <NForm ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100">
          <NFormItem label="姓名" path="staffName">
            <NInput v-model:value="form.staffName" placeholder="请输入姓名" />
          </NFormItem>
          <NFormItem label="身份证号">
            <NInput v-model:value="form.idCard" placeholder="请输入身份证号，填写后自动计算年龄和性别" />
          </NFormItem>
          <NFormItem label="年龄">
            <NInput :value="displayAge ? String(displayAge) : ''" disabled placeholder="自动计算" />
          </NFormItem>
          <NFormItem label="性别">
            <NSelect v-model:value="form.gender" :options="genderOptions" style="width: 120px" />
          </NFormItem>
          <NFormItem label="手机号" path="phone">
            <NInput v-model:value="form.phone" placeholder="请输入手机号" />
          </NFormItem>
          <!-- 服务商：服务商管理员自动归属，非服务商管理员需选择 -->
          <NFormItem v-if="isProviderAdmin" label="服务商">
            <NInput :value="currentProviderName" disabled placeholder="自动归属当前服务商" />
          </NFormItem>
          <NFormItem v-else label="服务商" path="providerId" required>
            <NSelect
              v-model:value="form.providerId"
              :options="providerOptions"
              :loading="providerOptionsLoading"
              placeholder="请选择服务商"
              style="width: 100%"
            />
          </NFormItem>
          <NFormItem label="紧急联系人">
            <NInput v-model:value="form.emergencyContact" placeholder="请输入紧急联系人" />
          </NFormItem>
          <NFormItem label="紧急联系电话">
            <NInput v-model:value="form.emergencyPhone" placeholder="请输入紧急联系电话" />
          </NFormItem>
          <NFormItem label="状态">
            <NSelect v-model:value="form.status" :options="statusOptions" style="width: 150px" />
          </NFormItem>
          <NFormItem label="参保状态">
            <NSelect v-model:value="form.insuranceStatus" :options="insuranceStatusOptions" style="width: 150px" />
          </NFormItem>
          <NFormItem label="备注">
            <NInput v-model:value="form.remark" type="textarea" placeholder="请输入备注" />
          </NFormItem>
        </NForm>
        <template #footer>
          <NSpace justify="end">
            <NButton @click="closeDrawer">取消</NButton>
            <NButton type="primary" @click="handleSubmit">确认</NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- Staff Detail Drawer -->
    <NDrawer v-model:show="detailVisible" :width="550" placement="right" closable>
      <NDrawerContent title="服务人员详情" closable>
        <NTabs v-model:value="detailActiveTab" type="line" style="margin-top: 8px" @update:value="onTabChange">
          <NTabPane name="basic" tab="基本信息">
            <div style="max-height: calc(100vh - 220px); overflow-y: auto">
              <!-- Avatar and Basic Info -->
              <div style="display: flex; gap: 16px; margin-bottom: 24px; align-items: flex-start">
                <div style="width: 80px; height: 80px; border-radius: 8px; overflow: hidden; flex-shrink: 0">
                  <LazyImage v-if="detailData.avatarUrl" :src="detailData.avatarUrl" :width="80" :height="80" fit="cover" />
                  <NAvatar v-else :size="80" round style="background: #5394ec; color: #fff; font-size: 24px">{{ detailData.staffName?.charAt(0) || '?' }}</NAvatar>
                </div>
                <div style="flex: 1">
                  <div style="font-size: 18px; font-weight: 600; margin-bottom: 4px">{{ detailData.staffName }}</div>
                  <div style="color: #666; font-size: 13px; margin-bottom: 8px">{{ detailData.phone || '-' }}</div>
                  <NTag :type="getStatusType(detailData.status)" size="small">{{ getStatusLabel(detailData.status) }}</NTag>
                </div>
              </div>

              <!-- Basic Info Grid -->
              <div style="margin-bottom: 24px">
                <NGrid :cols="2" :x-gap="16" :y-gap="8">
                  <NGi><div style="color: #999; font-size: 13px">工号</div><div style="margin-top: 4px">{{ detailData.staffNo || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">年龄</div><div style="margin-top: 4px">{{ detailData.age || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">性别</div><div style="margin-top: 4px">{{ getGenderLabel(detailData.gender) }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">身份证号</div><div style="margin-top: 4px">{{ detailData.idCard || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">服务类型</div><div style="margin-top: 4px">{{ detailData.serviceTypes || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">评分</div><div style="margin-top: 4px"><NRate v-if="detailData.rating" :value="detailData.rating" readonly size="small" /><span v-else>-</span></div></NGi>
                </NGrid>
              </div>

              <!-- Employment Info -->
              <div style="margin-bottom: 24px">
                <div style="font-size: 14px; font-weight: 600; margin-bottom: 12px; color: #333">任职信息</div>
                <NGrid :cols="2" :x-gap="16">
                  <NGi><div style="color: #999; font-size: 13px">所属服务商</div><div style="margin-top: 4px">{{ detailData.providerName || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">入职日期</div><div style="margin-top: 4px">{{ detailData.hireDate || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">接单数</div><div style="margin-top: 4px">{{ detailData.orderCount || 0 }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">状态</div><div style="margin-top: 4px">
                    <NTag :type="getStatusType(detailData.status)" size="small">{{ getStatusLabel(detailData.status) }}</NTag>
                  </div></NGi>
                </NGrid>
              </div>

              <!-- Account Info -->
              <div style="margin-bottom: 24px">
                <div style="font-size: 14px; font-weight: 600; margin-bottom: 12px; color: #333">系统账户</div>
                <NGrid :cols="2" :x-gap="16">
                  <NGi><div style="color: #999; font-size: 13px">用户名</div><div style="margin-top: 4px">
                    <NTag v-if="detailData.username" type="info">{{ detailData.username }}</NTag>
                    <span v-else>-</span>
                  </div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">账户状态</div><div style="margin-top: 4px">
                    <NTag v-if="detailData.hasAccount" type="success">已开通</NTag>
                    <NTag v-else type="default">未开通</NTag>
                  </div></NGi>
                </NGrid>
                <div v-if="detailData.username" style="margin-top: 12px">
                  <NButton size="small" type="warning" @click="resetPassword">重置密码</NButton>
                </div>
              </div>

              <!-- Insurance & Honors -->
              <div style="margin-bottom: 24px">
                <div style="font-size: 14px; font-weight: 600; margin-bottom: 12px; color: #333">保险与荣誉</div>
                <NGrid :cols="2" :x-gap="16">
                  <NGi><div style="color: #999; font-size: 13px">参保状态</div>
                    <div style="margin-top: 4px">
                      <NTag :type="detailData.insuranceStatus === 2 ? 'success' : detailData.insuranceStatus === 1 ? 'warning' : 'default'" size="small">
                        {{ ['', '未参保', '正在参保', '已参保', '已过期'][detailData.insuranceStatus ?? 0] || '未参保' }}
                      </NTag>
                    </div>
                  </NGi>
                  <NGi><div style="color: #999; font-size: 13px">月度之星</div>
                    <div style="margin-top: 4px">
                      <NTag v-if="detailData.monthlyStar === 1" type="warning">🏅 月度之星</NTag>
                      <span v-else style="color: #ccc">—</span>
                    </div>
                  </NGi>
                  <NGi><div style="color: #999; font-size: 13px">季度之星</div>
                    <div style="margin-top: 4px">
                      <NTag v-if="detailData.quarterlyStar === 1" type="info">🏆 季度之星</NTag>
                      <span v-else style="color: #ccc">—</span>
                    </div>
                  </NGi>
                </NGrid>
              </div>

              <!-- Emergency Contact -->
              <div style="margin-bottom: 24px">
                <div style="font-size: 14px; font-weight: 600; margin-bottom: 12px; color: #333">紧急联系人</div>
                <NGrid :cols="2" :x-gap="16">
                  <NGi><div style="color: #999; font-size: 13px">联系人</div><div style="margin-top: 4px">{{ detailData.emergencyContact || '-' }}</div></NGi>
                  <NGi><div style="color: #999; font-size: 13px">联系电话</div><div style="margin-top: 4px">{{ detailData.emergencyPhone || '-' }}</div></NGi>
                </NGrid>
              </div>

              <!-- Other Info -->
              <div style="margin-bottom: 24px">
                <div style="font-size: 14px; font-weight: 600; margin-bottom: 12px; color: #333">其他信息</div>
                <div style="color: #999; font-size: 13px">备注</div>
                <div style="margin-top: 4px">{{ detailData.remark || '-' }}</div>
                <div style="color: #999; font-size: 13px; margin-top: 8px">创建时间</div>
                <div style="margin-top: 4px">{{ detailData.createTime || '-' }}</div>
              </div>
            </div>
          </NTabPane>
          <NTabPane name="qualifications" tab="资质">
            <!-- 加载状态 -->
            <div v-if="qualificationsLoading" style="text-align: center; padding: 40px 0">
              <NSpin size="medium" />
            </div>

            <!-- 资质折叠面板列表 -->
            <NCollapse v-else :expanded-names="Array.from(expandedStates)" @update:expanded-names="(val: any) => { expandedStates = new Set(val); val.forEach((v: any) => handlePanelExpand(v)); }">
              <!-- 固定类型 0-4：每个类型一个折叠面板 -->
              <NCollapseItem
                v-for="type in [0, 1, 2, 3, 4]"
                :key="type"
                :name="type"
              >
                <template #header>
                  <div style="display: flex; align-items: center; gap: 12px">
                    <span style="font-weight: 600">{{ getQualificationTypeName(type) }}</span>
                    <NTag v-if="getQualificationsByType(type).length && getQualificationsByType(type)[0].certificateUrls" type="success" size="small">
                      ✓ 已上传
                    </NTag>
                    <NTag v-else type="default" size="small">未上传</NTag>
                  </div>
                </template>

                <div style="padding: 8px 0">
                  <!-- 加载图片中状态 -->
                  <div v-if="isQualificationLoading(getQualificationsByType(type)[0])" style="text-align: center; padding: 20px 0">
                    <NSpin size="small" />
                    <div style="font-size: 12px; color: #999; margin-top: 8px">加载图片中...</div>
                  </div>

                  <!-- 已有图片（从缓存读取） -->
                  <div v-else-if="getQualificationImageUrls(getQualificationsByType(type)[0])" style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 12px">
                    <div
                      v-for="(url, idx) in getQualificationImageUrls(getQualificationsByType(type)[0])!.split('|||')"
                      :key="idx"
                      style="position: relative; display: inline-block"
                    >
                      <NImage
                        :src="url"
                        width="70"
                        height="70"
                        :preview="true"
                        style="object-fit: cover; border-radius: 6px; border: 1px solid #eee; cursor: pointer"
                      />
                      <NButton
                        size="tiny"
                        type="error"
                        style="position: absolute; top: -8px; right: -8px; min-width: 22px; height: 22px; padding: 0; border-radius: 50%"
                        @click.stop="deleteQualificationImage(getQualificationsByType(type)[0], url)"
                      >
                        ×
                      </NButton>
                    </div>
                  </div>

                  <!-- 上传和删除按钮 -->
                  <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px">
                    <NUpload
                      :multiple="false"
                      :max="1"
                      accept="image/*"
                      :show-file-list="false"
                      :custom-request="(opt: any) => handleImageUpload(opt, type)"
                    >
                      <NButton type="primary" size="small" :loading="uploadingTypes.has(type)">
                        {{ uploadingTypes.has(type) ? '处理中...' : '+ 上传' }}
                      </NButton>
                    </NUpload>

                    <!-- 删除整个资质 -->
                    <NPopconfirm v-if="getQualificationsByType(type).length" @positive-click="deleteQualification(getQualificationsByType(type)[0].qualificationId)">
                      <template #trigger>
                        <NButton size="small" type="error">删除</NButton>
                      </template>
                      确定要删除该资质吗？
                    </NPopconfirm>
                  </div>
                </div>
              </NCollapseItem>

              <!-- 其他类型（5）：每个不同名称一个折叠面板 -->
              <NCollapseItem
                v-for="name in getOtherTypeNames()"
                :key="'other-' + name"
                :name="'other-' + name"
              >
                <template #header>
                  <div style="display: flex; align-items: center; gap: 12px">
                    <span style="font-weight: 600">{{ name }}</span>
                    <NTag v-if="getQualificationsByOtherName(name)[0]?.certificateUrls" type="success" size="small">
                      ✓ 已上传
                    </NTag>
                    <NTag v-else type="default" size="small">未上传</NTag>
                  </div>
                </template>

                <div style="padding: 8px 0">
                  <!-- 加载图片中状态 -->
                  <div v-if="isQualificationLoading(getQualificationsByOtherName(name)[0])" style="text-align: center; padding: 20px 0">
                    <NSpin size="small" />
                    <div style="font-size: 12px; color: #999; margin-top: 8px">加载图片中...</div>
                  </div>

                  <!-- 已有图片（从缓存读取） -->
                  <div v-else-if="getQualificationImageUrls(getQualificationsByOtherName(name)[0])" style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 12px">
                    <div
                      v-for="(url, idx) in getQualificationImageUrls(getQualificationsByOtherName(name)[0])!.split('|||')"
                      :key="idx"
                      style="position: relative; display: inline-block"
                    >
                      <NImage
                        :src="url"
                        width="70"
                        height="70"
                        :preview="true"
                        style="object-fit: cover; border-radius: 6px; border: 1px solid #eee; cursor: pointer"
                      />
                      <NButton
                        size="tiny"
                        type="error"
                        style="position: absolute; top: -8px; right: -8px; min-width: 22px; height: 22px; padding: 0; border-radius: 50%"
                        @click.stop="deleteQualificationImage(getQualificationsByOtherName(name)[0], url)"
                      >
                        ×
                      </NButton>
                    </div>
                  </div>

                  <!-- 上传和删除按钮 -->
                  <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px">
                    <NUpload
                      :multiple="false"
                      :max="1"
                      accept="image/*"
                      :show-file-list="false"
                      :custom-request="(opt: any) => handleImageUpload(opt, 5, name)"
                    >
                      <NButton type="primary" size="small" :loading="uploadingTypes.has('other-' + name)">
                        {{ uploadingTypes.has('other-' + name) ? '处理中...' : '+ 上传' }}
                      </NButton>
                    </NUpload>

                    <!-- 删除整个资质 -->
                    <NPopconfirm v-if="getQualificationsByOtherName(name).length" @positive-click="deleteQualification(getQualificationsByOtherName(name)[0].qualificationId)">
                      <template #trigger>
                        <NButton size="small" type="error">删除</NButton>
                      </template>
                      确定要删除该资质吗？
                    </NPopconfirm>
                  </div>
                </div>
              </NCollapseItem>

              <!-- 添加其他资质按钮 -->
              <div style="padding: 12px 0; text-align: center">
                <NButton size="small" @click="showAddOtherDialog = true">+ 新增其他资质</NButton>
              </div>
            </NCollapse>

            <!-- 上传确认对话框 -->
            <NModal
              v-model:show="uploadConfirmVisible"
              preset="dialog"
              title="该类型已有证书"
              content="要覆盖现有证书还是新增证书图片？"
              positive-text="覆盖"
              negative-text="新增"
              @positive-click="handleOverwriteConfirm"
              @negative-click="handleAddConfirm"
            />

            <!-- 添加其他资质弹窗 -->
            <NModal
              v-model:show="showAddOtherDialog"
              preset="dialog"
              title="新增其他资质"
              style="width: 360px"
            >
              <NInput v-model:value="newOtherName" placeholder="输入资质名称" style="width: 100%" />
              <template #action>
                <NButton @click="showAddOtherDialog = false">取消</NButton>
                <NButton type="primary" @click="addOtherQualification">确定</NButton>
              </template>
            </NModal>
          </NTabPane>
          <NTabPane name="logs" tab="服务记录">
            <div style="max-height: calc(100vh - 220px); overflow-y: auto">
              <div v-if="serviceLogsLoading" style="text-align: center; padding: 40px 0">
                <NSpin size="medium" />
              </div>
              <div v-else-if="serviceLogs.length === 0" style="text-align: center; padding: 40px 0">
                <NEmpty description="暂无服务记录" />
              </div>
              <div v-else style="display: flex; flex-direction: column; gap: 12px">
                <div v-for="log in serviceLogs" :key="log.serviceLogId" style="background: #f8f8f8; border-radius: 8px; padding: 12px">
                  <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px">
                    <div style="font-weight: 600">{{ log.serviceCategory || log.serviceType || '-' }}</div>
                    <NTag :type="log.hasAnomaly ? 'error' : 'success'" size="small">{{ log.hasAnomaly ? '有异常' : '正常' }}</NTag>
                  </div>
                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 4px; font-size: 13px; color: #666">
                    <div>客户：{{ log.elderName || '-' }}</div>
                    <div>日期：{{ log.serviceDate || '-' }}</div>
                    <div>时长：{{ log.actualDuration ? log.actualDuration + '分钟' : '-' }}</div>
                    <div>评分：{{ log.serviceComment ? '⭐'.repeat(Math.min(5, parseInt(log.serviceComment))) : '-' }}</div>
                  </div>
                  <div v-if="log.serviceContent" style="margin-top: 8px; font-size: 13px; color: #666">{{ log.serviceContent }}</div>
                </div>
              </div>
            </div>
          </NTabPane>
        </NTabs>
      </NDrawerContent>
    </NDrawer>

    <!-- 账户信息弹窗 -->
    <NModal
      v-model:show="accountModalVisible"
      preset="card"
      title="系统账户信息"
      :style="{ width: '400px' }"
    >
      <NAlert type="warning" :bordered="false" style="margin-bottom: 16px">
        请将以下信息告知服务人员，建议首次登录后修改密码
      </NAlert>
      <NDescriptions label-placement="top">
        <NDescriptionsItem label="服务人员">
          {{ accountInfo?.staffName }}
        </NDescriptionsItem>
        <NDescriptionsItem label="用户名">
          <NTag type="info">{{ accountInfo?.username }}</NTag>
        </NDescriptionsItem>
        <NDescriptionsItem label="密码">
          <NTag type="warning">{{ accountInfo?.password }}</NTag>
        </NDescriptionsItem>
      </NDescriptions>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="accountModalVisible = false">关闭</NButton>
          <NButton type="primary" @click="copyAccountInfo">复制信息</NButton>
        </NSpace>
      </template>
    </NModal>

      </div>
</template>

<style scoped>
.statistics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 16px;
  border-radius: 8px;
  text-align: center;
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  line-height: 1.2;
}

.stat-primary {
  background: linear-gradient(135deg, #5E8B7E 0%, #7BA89F 100%);
  color: white;
}

.stat-primary .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-info {
  background: linear-gradient(135deg, #3A506B 0%, #5A7A8B 100%);
  color: white;
}

.stat-info .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-success {
  background: linear-gradient(135deg, #5E8B7E 0%, #81B29A 100%);
  color: white;
}

.stat-success .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-warning {
  background: linear-gradient(135deg, #F9B572 0%, #F7C59F 100%);
  color: white;
}

.stat-warning .stat-label {
  color: rgba(255, 255, 255, 0.85);
}

.stat-error {
  background: linear-gradient(135deg, #D64045 0%, #E07070 100%);
  color: white;
}

.stat-error .stat-label {
  color: rgba(255, 255, 255, 0.85);
}
</style>
