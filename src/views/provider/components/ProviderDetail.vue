<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import {
  NDrawer,
  NDrawerContent,
  NGrid,
  NGi,
  NTabs,
  NTab,
  NTabPane,
  NImage,
  NButton,
  NSpace,
  NTag,
  NEmpty,
  useMessage,
  NSpin,
  NUpload,
  NPopconfirm,
  NModal,
  NCard,
  NDescriptions,
  NDescriptionsItem,
  NCollapse,
  NCollapseItem,
  NInput
} from 'naive-ui';
import type { UploadFile } from 'naive-ui';
import {
  fetchGetProvider,
  fetchCreateProviderCertificate,
  fetchDeleteProviderCertificate,
  fetchUpdateProvider,
  fetchGetProviderAdminAccount,
  fetchResetProviderAdminPassword,
  fetchGetProviderQualificationsPreview,
  fetchGetProviderQualificationImages,
  fetchUpdateProviderQualification
} from '@/service/api';
import { useAuthStore } from '@/store/modules/auth';
import { addWatermarkToImage } from '@/utils/watermark';

interface Props {
  providerId: string | null;
  visible: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
}>();

const message = useMessage();

// =============================================
// 资质类型定义
// =============================================
// 前端类型索引 -> API qualificationType 映射
const TYPE_TO_QUALIFICATION_TYPE = ['SPECIAL_OPERATION', 'HEALTH', 'PROFESSIONAL'];
const QUALIFICATION_TYPE_NAMES = ['特种作业证', '健康证', '职业资格证书', '其他'];

// =============================================
// 资质相关状态
// =============================================
const qualifications = ref<Api.Provider.Qualification[]>([]);
const qualificationsLoading = ref(false);

// 资质图片懒加载状态
const imageCache = ref<Record<string, string>>({});  // qualificationId -> attachmentUrl（含 ||| 分隔的多图）
const loadingImages = ref<Set<string>>(new Set());    // 正在加载的 qualificationId

// 折叠面板展开状态
const expandedStates = ref<Set<number | string>>(new Set());

// 上传相关状态
const uploadingTypes = ref<Set<number | string>>(new Set());
const uploadConfirmVisible = ref(false);
const pendingBase64 = ref<string | null>(null);
const uploadLoading = ref(false);
const pendingQualification = ref<Api.Provider.Qualification | null>(null);

// 添加自定义证书弹窗
const showAddOtherDialog = ref(false);
const newOtherName = ref('');

// License upload state
const uploadingLicense = ref(false);

// Admin account state
const adminAccount = ref<{ userId: string; username: string; status: string; createTime: string } | null>(null);
const loadingAccount = ref(false);

// Detail data
const loading = ref(false);
const detailData = ref<Api.Provider.Provider | null>(null);

// Image preview
const previewVisible = ref(false);
const previewUrl = ref('');

// Active tab for lazy loading
const activeTab = ref('basic');

function onTabChange(tab: string) {
  if (tab === 'certificates' && qualifications.value.length === 0) {
    loadQualificationsPreview();
  }
}

// =============================================
// 辅助函数
// =============================================
const drawerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
});

function getQualificationsByType(type: number): Api.Provider.Qualification[] {
  // type 0-2: 映射到 API 字符串类型; type 3 (其他): 使用 CERTIFICATE
  if (type === 3) {
    return qualifications.value.filter(q => q.qualificationType === 'CERTIFICATE');
  }
  const apiType = TYPE_TO_QUALIFICATION_TYPE[type];
  return qualifications.value.filter(q => q.qualificationType === apiType);
}

function getOtherTypeNames(): string[] {
  const others = qualifications.value.filter(q => q.qualificationType === 'CERTIFICATE');
  return [...new Set(others.map(q => q.qualificationName))];
}

function getQualificationsByOtherName(name: string): Api.Provider.Qualification[] {
  return qualifications.value.filter(q => q.qualificationType === 'CERTIFICATE' && q.qualificationName === name);
}

function getQualificationImageUrls(qual: Api.Provider.Qualification | undefined): string | undefined {
  if (!qual) return undefined;
  return imageCache.value[String(qual.qualificationId)];
}

function isQualificationLoading(qual: Api.Provider.Qualification | undefined): boolean {
  if (!qual) return false;
  return loadingImages.value.has(String(qual.qualificationId));
}

function getQualificationTypeName(type: number): string {
  return QUALIFICATION_TYPE_NAMES[type] || '其他';
}

// =============================================
// 数据加载
// =============================================
async function loadProviderDetail() {
  if (!props.providerId) return;
  loading.value = true;
  try {
    const { data, error } = await fetchGetProvider(props.providerId);
    if (error) { message.error(error.message || '获取服务商详情失败'); return; }
    if (data) { detailData.value = data; }
  } finally { loading.value = false; }
}

async function loadAdminAccount() {
  if (!props.providerId) return;
  loadingAccount.value = true;
  try {
    const { data, error } = await fetchGetProviderAdminAccount(props.providerId);
    if (error) { adminAccount.value = null; return; }
    adminAccount.value = data || null;
  } finally { loadingAccount.value = false; }
}

async function loadQualificationsPreview() {
  if (!props.providerId) return;
  qualificationsLoading.value = true;
  try {
    const { data, error } = await fetchGetProviderQualificationsPreview(props.providerId);
    if (error) { message.error(error.message || '获取资质列表失败'); return; }
    if (data) { qualifications.value = data; }
  } finally { qualificationsLoading.value = false; }
}

async function loadQualificationImages(qualificationId: string) {
  const cacheKey = String(qualificationId);
  if (imageCache.value[cacheKey] || loadingImages.value.has(cacheKey)) return;
  loadingImages.value.add(cacheKey);
  try {
    const { data, error } = await fetchGetProviderQualificationImages(qualificationId);
    if (error || !data) return;
    imageCache.value[cacheKey] = data;
  } finally {
    loadingImages.value.delete(cacheKey);
  }
}

function handlePanelExpand(name: number | string) {
  const nameStr = String(name);
  if (/^[0-2]$/.test(nameStr)) {
    const type = parseInt(nameStr);
    const quals = getQualificationsByType(type);
    for (const q of quals) {
      if (q.attachmentUrl === 'HAS_IMAGES') {
        loadQualificationImages(String(q.qualificationId));
      }
    }
  } else if (nameStr.startsWith('other-')) {
    const otherName = nameStr.slice(6);
    const quals = getQualificationsByOtherName(otherName);
    for (const q of quals) {
      if (q.attachmentUrl === 'HAS_IMAGES') {
        loadQualificationImages(String(q.qualificationId));
      }
    }
  }
}

watch(
  () => props.visible,
  (val) => {
    if (val && props.providerId) {
      loadProviderDetail();
      loadAdminAccount();
    }
  }
);

watch(drawerVisible, (val) => {
  if (!val) {
    detailData.value = null;
    adminAccount.value = null;
    qualifications.value = [];
    imageCache.value = {};
    activeTab.value = 'basic';
  }
});

// =============================================
// 营业执照上传
// =============================================
function handleLicenseUploadRequest({ file }: { file: UploadFile }) {
  if (!file.file || !props.providerId) return false;
  uploadingLicense.value = true;
  const reader = new FileReader();
  reader.onload = async (e) => {
    if (e.target?.result) {
      const base64Data = e.target.result as string;
      try {
        await fetchUpdateProvider(props.providerId, { businessLicense: base64Data });
        message.success('营业执照上传成功');
        await loadProviderDetail();
      } catch (err) {
        console.error('Failed to upload license', err);
        message.error('上传失败');
      } finally {
        uploadingLicense.value = false;
      }
    }
  };
  reader.readAsDataURL(file.file);
  return false;
}

// =============================================
// 资质上传
// =============================================
// 本地添加或更新资质（局部刷新核心）
function upsertQualification(newQual: Api.Provider.Qualification) {
  const index = qualifications.value.findIndex(q => q.qualificationName === newQual.qualificationName);
  if (index >= 0) {
    qualifications.value[index] = newQual;
  } else {
    qualifications.value.push(newQual);
  }
  // 同时更新缓存（新建时 attachmentUrl 是真实 base64）
  if (newQual.attachmentUrl && newQual.attachmentUrl !== 'HAS_IMAGES') {
    imageCache.value[String(newQual.qualificationId)] = newQual.attachmentUrl;
  }
}

async function handleImageUpload(opt: { file: UploadFile }, targetType: number, targetName?: string) {
  const file = opt.file;
  if (!file || !file.file || !props.providerId) return false;

  // 文件校验
  const MAX_SIZE = 2 * 1024 * 1024;
  if (file.file.size > MAX_SIZE) { message.error('图片大小不能超过2MB'); return false; }
  const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
  if (!ALLOWED_TYPES.includes(file.file.type)) { message.error('只能上传 JPG/PNG/WEBP 格式'); return false; }

  const uploadKey = targetType === 3 ? `other-${targetName}` : targetType;
  uploadingTypes.value.add(uploadKey);
  message.loading('正在处理图片...', { duration: 0 });

  try {
    const base64 = await new Promise<string>((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e) => resolve(e.target?.result as string);
      reader.onerror = reject;
      reader.readAsDataURL(file.file as Blob);
    });
    message.destroyAll();

    // 查找是否已有该类型资质（按名称查找，因为后端按 providerId + qualificationName 去重）
    const qualName = targetType === 3 ? targetName! : getQualificationTypeName(targetType);
    const existingIndex = qualifications.value.findIndex(q => q.qualificationName === qualName);
    const existing = existingIndex >= 0 ? qualifications.value[existingIndex] : null;

    if (existing) {
      // 已有资质 → 弹出覆盖/追加确认
      pendingBase64.value = base64;
      pendingQualification.value = existing;
      uploadConfirmVisible.value = true;
      return false;
    }

    // 新建资质
    const newQual: Api.Provider.Qualification = {
      qualificationId: '', // 先用空，后续会从列表中获取
      qualificationName: qualName,
      qualificationType: targetType === 3 ? 'CERTIFICATE' : ['SPECIAL_OPERATION', 'HEALTH', 'PROFESSIONAL'][targetType],
      attachmentUrl: base64,
      status: 'VALID',
      auditStatus: 'APPROVED'
    };
    const { data, error } = await fetchCreateProviderCertificate(props.providerId, {
      qualificationName: qualName,
      qualificationType: targetType === 3 ? 'CERTIFICATE' : ['SPECIAL_OPERATION', 'HEALTH', 'PROFESSIONAL'][targetType],
      attachmentUrl: base64
    });
    if (error) { message.error('上传失败'); return false; }
    message.success('✓ 已保存');
    // 局部刷新：用 upsertQualification 代替重新加载全量列表
    newQual.qualificationId = data || '';
    upsertQualification(newQual);
    return false;
  } finally {
    uploadingTypes.value.delete(uploadKey);
  }
}

// 覆盖确认
async function handleOverwriteConfirm() {
  uploadConfirmVisible.value = false;
  if (!pendingBase64.value || !pendingQualification.value || !props.providerId) return;
  const existing = pendingQualification.value;
  const cacheKey = String(existing.qualificationId);

  existing.attachmentUrl = pendingBase64.value;
  imageCache.value[cacheKey] = pendingBase64.value;
  uploadLoading.value = true;

  try {
    const { error } = await fetchUpdateProviderQualification(existing.qualificationId, {
      ...existing,
      attachmentUrl: pendingBase64.value
    });
    if (error) { message.error('覆盖失败'); return; }
    message.success('✓ 已覆盖');
  } finally {
    uploadLoading.value = false;
    pendingBase64.value = null;
    pendingQualification.value = null;
  }
}

// 追加确认
async function handleAddConfirm() {
  uploadConfirmVisible.value = false;
  if (!pendingBase64.value || !pendingQualification.value || !props.providerId) return;
  const existing = pendingQualification.value;
  const cacheKey = String(existing.qualificationId);

  // 关键：从 imageCache 获取真实 URL 追加，不用 existing.attachmentUrl（可能是 'HAS_IMAGES'）
  const existingUrls = imageCache.value[cacheKey]
    ? imageCache.value[cacheKey].split('|||')
    : [];
  const newUrls = existingUrls.concat(pendingBase64.value);

  existing.attachmentUrl = newUrls.join('|||');
  imageCache.value[cacheKey] = newUrls.join('|||');
  uploadLoading.value = true;

  try {
    const { error } = await fetchUpdateProviderQualification(existing.qualificationId, {
      ...existing,
      attachmentUrl: newUrls.join('|||')
    });
    if (error) { message.error('添加失败'); return; }
    message.success('✓ 已添加');
  } finally {
    uploadLoading.value = false;
    pendingBase64.value = null;
    pendingQualification.value = null;
  }
}

// 删除单张图片
async function deleteQualificationImage(qual: Api.Provider.Qualification, url: string) {
  const cacheKey = String(qual.qualificationId);
  const urls = qual.attachmentUrl.split('|||').filter(u => u !== url);

  if (urls.length === 0) {
    qual.attachmentUrl = undefined as any;
    delete imageCache.value[cacheKey];
  } else {
    qual.attachmentUrl = urls.join('|||');
    imageCache.value[cacheKey] = urls.join('|||');
  }

  const { error } = await fetchUpdateProviderQualification(qual.qualificationId, {
    ...qual,
    attachmentUrl: qual.attachmentUrl
  });
  if (error) { message.error('删除失败'); return; }
  message.success('已删除');
}

// 删除整资质
async function deleteQualification(qualificationId: string) {
  const cacheKey = String(qualificationId);
  const index = qualifications.value.findIndex(q => String(q.qualificationId) === cacheKey);
  if (index >= 0) qualifications.value.splice(index, 1);
  delete imageCache.value[cacheKey];

  const { error } = await fetchDeleteProviderCertificate(qualificationId);
  if (error) { message.error('删除失败'); return; }
  message.success('删除成功');
}

// 添加其他资质
async function handleAddOther() {
  if (!newOtherName.value.trim()) { message.error('请输入证书名称'); return; }
  if (!props.providerId) return;

  showAddOtherDialog.value = false;
  const name = newOtherName.value.trim();

  const { error } = await fetchCreateProviderCertificate(props.providerId, {
    qualificationName: name,
    qualificationType: 'CERTIFICATE',
    attachmentUrl: ''
  });
  if (error) { message.error('添加失败'); return; }
  message.success('✓ 已添加');
  newOtherName.value = '';
  await loadQualificationsPreview();
}

// =============================================
// 其他
// =============================================
async function handleResetPassword() {
  if (!props.providerId) return;
  try {
    const { data, error } = await fetchResetProviderAdminPassword(props.providerId);
    if (error) { message.error(error.message || '操作失败'); return; }
    message.success(data);
    await loadAdminAccount();
  } catch (e) {
    message.error('操作失败');
  }
}

function previewImage(url: string) {
  previewUrl.value = url;
  previewVisible.value = true;
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-';
  return dateStr.split('T')[0];
}

function getStatusType(status: string): 'success' | 'error' | 'warning' {
  if (status === 'ENABLED' || status === 'NORMAL') return 'success';
  if (status === 'DISABLED') return 'error';
  return 'warning';
}

function getStatusLabel(status: string): string {
  const options: Record<string, string> = { ENABLED: '正常', DISABLED: '禁用', NORMAL: '正常' };
  return options[status] || status || '-';
}
</script>

<template>
  <NDrawer v-model:show="drawerVisible" :width="600" placement="right" closable>
    <NDrawerContent title="服务商详情" closable>
      <template #header>
        <div style="display: flex; flex-direction: column; gap: 4px">
          <div style="font-size: 16px; font-weight: 600">服务商详情</div>
          <div v-if="detailData" style="font-size: 13px; color: #666">{{ detailData.providerName }}</div>
        </div>
      </template>

      <NSpin :show="loading">
        <NTabs v-model:active="activeTab" type="line" animated @update:active="onTabChange">
          <!-- 基本信息 -->
          <NTabPane name="basic" tab="基本信息">
            <div v-if="detailData" style="padding: 8px 0">
              <NGrid :cols="2" :x-gap="16" :y-gap="12">
                <NGi><div style="color: #999; font-size: 13px">服务商名称</div><div style="margin-top: 4px; font-weight: 500">{{ detailData.providerName }}</div></NGi>
                <NGi><div style="color: #999; font-size: 13px">统一社会信用代码</div><div style="margin-top: 4px">{{ detailData.creditCode || '-' }}</div></NGi>
                <NGi><div style="color: #999; font-size: 13px">服务商类型</div><div style="margin-top: 4px">{{ detailData.providerType === 'TECH_SERVICE' ? '网络科技服务' : detailData.providerType === 'HOME_CARE' ? '家政服务' : '养老服务' }}</div></NGi>
                <NGi><div style="color: #999; font-size: 13px">服务类别</div><div style="margin-top: 4px">{{ detailData.serviceCategory === 'ELDER_CARE' ? '养老服务' : '家政服务' }}</div></NGi>
                <NGi><div style="color: #999; font-size: 13px">法人姓名</div><div style="margin-top: 4px">{{ detailData.legalPerson || '-' }}</div></NGi>
                <NGi><div style="color: #999; font-size: 13px">联系电话</div><div style="margin-top: 4px">{{ detailData.contactPhone || '-' }}</div></NGi>
                <NGi :span="2"><div style="color: #999; font-size: 13px">联系地址</div><div style="margin-top: 4px">{{ detailData.address || '-' }}</div></NGi>
                <NGi :span="2"><div style="color: #999; font-size: 13px">服务区域</div><div style="margin-top: 4px">{{ detailData.serviceAreas || '-' }}</div></NGi>
                <NGi :span="2"><div style="color: #999; font-size: 13px">简介</div><div style="margin-top: 4px">{{ detailData.description || '-' }}</div></NGi>
                <NGi><div style="color: #999; font-size: 13px">状态</div><div style="margin-top: 4px"><NTag :type="getStatusType(detailData.status)" size="small">{{ getStatusLabel(detailData.status) }}</NTag></div></NGi>
                <NGi><div style="color: #999; font-size: 13px">评分</div><div style="margin-top: 4px">{{ detailData.rating ? `${detailData.rating} (${detailData.ratingCount}次)` : '-' }}</div></NGi>
                <NGi :span="2"><div style="color: #999; font-size: 13px">创建时间</div><div style="margin-top: 4px">{{ formatDate(detailData.createTime) }}</div></NGi>
              </NGrid>
            </div>
          </NTabPane>

          <!-- 证照管理 -->
          <NTabPane name="certificates" tab="证照管理">
            <div style="padding: 8px 0">
              <!-- 营业执照（单独处理） -->
              <div style="margin-bottom: 20px">
                <div style="font-weight: 600; margin-bottom: 8px; color: #333">营业执照</div>
                <div style="display: flex; align-items: flex-start; gap: 12px">
                  <NUpload :show-file-list="false" accept="image/*" :custom-request="handleLicenseUploadRequest">
                    <NButton type="primary" size="small" :loading="uploadingLicense">
                      {{ detailData?.businessLicense ? '重新上传' : '上传执照' }}
                    </NButton>
                  </NUpload>
                  <div v-if="detailData?.businessLicense" style="margin-left: 20px">
                    <NImage :src="detailData.businessLicense" width="120" height="120" :preview="true" style="border-radius: 8px; border: 2px solid #e6e6e6" />
                  </div>
                </div>
              </div>

              <!-- 加载状态 -->
              <div v-if="qualificationsLoading" style="text-align: center; padding: 40px 0">
                <NSpin size="medium" />
              </div>

              <!-- 资质折叠面板 -->
              <NCollapse
                v-else
                :expanded-names="Array.from(expandedStates)"
                @update:expanded-names="(val: any) => { expandedStates = new Set(val); val.forEach((v: any) => handlePanelExpand(v)); }"
              >
                <!-- 固定类型 0-2：特种作业证、健康证、职业资格证书 -->
                <NCollapseItem v-for="type in [0, 1, 2]" :key="type" :name="type">
                  <template #header>
                    <div style="display: flex; align-items: center; gap: 12px">
                      <span style="font-weight: 600">{{ getQualificationTypeName(type) }}</span>
                      <NTag v-if="getQualificationsByType(type).length && getQualificationsByType(type)[0].attachmentUrl" type="success" size="small">✓ 已上传</NTag>
                      <NTag v-else type="default" size="small">未上传</NTag>
                    </div>
                  </template>
                  <div style="padding: 8px 0">
                    <!-- 加载中 -->
                    <div v-if="isQualificationLoading(getQualificationsByType(type)[0])" style="text-align: center; padding: 20px 0">
                      <NSpin size="small" />
                      <div style="font-size: 12px; color: #999; margin-top: 8px">加载图片中...</div>
                    </div>
                    <!-- 已有图片 -->
                    <div v-else-if="getQualificationImageUrls(getQualificationsByType(type)[0])" style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 12px">
                      <div v-for="(url, idx) in getQualificationImageUrls(getQualificationsByType(type)[0])!.split('|||')" :key="idx" style="position: relative; display: inline-block">
                        <NImage :src="url" width="70" height="70" :preview="true" style="object-fit: cover; border-radius: 6px; border: 1px solid #eee; cursor: pointer" />
                        <NButton size="tiny" type="error" style="position: absolute; top: -8px; right: -8px; min-width: 22px; height: 22px; padding: 0; border-radius: 50%" @click.stop="deleteQualificationImage(getQualificationsByType(type)[0], url)"> × </NButton>
                      </div>
                    </div>
                    <!-- 上传/删除按钮 -->
                    <div style="display: flex; justify-content: space-between; align-items: center">
                      <NUpload :multiple="false" :max="1" accept="image/*" :show-file-list="false" :custom-request="(opt: any) => handleImageUpload(opt, type)">
                        <NButton type="primary" size="small" :loading="uploadingTypes.has(type)">
                          {{ uploadingTypes.has(type) ? '处理中...' : '+ 上传' }}
                        </NButton>
                      </NUpload>
                      <NPopconfirm v-if="getQualificationsByType(type).length" @positive-click="deleteQualification(getQualificationsByType(type)[0].qualificationId)">
                        <template #trigger><NButton size="small" type="error">删除</NButton></template>
                        确定要删除该资质吗？
                      </NPopconfirm>
                    </div>
                  </div>
                </NCollapseItem>

                <!-- 自定义证书（类型3）：每个不同名称一个折叠面板 -->
                <NCollapseItem v-for="name in getOtherTypeNames()" :key="'other-' + name" :name="'other-' + name">
                  <template #header>
                    <div style="display: flex; align-items: center; gap: 12px">
                      <span style="font-weight: 600">{{ name }}</span>
                      <NTag v-if="getQualificationsByOtherName(name)[0]?.attachmentUrl" type="success" size="small">✓ 已上传</NTag>
                      <NTag v-else type="default" size="small">未上传</NTag>
                    </div>
                  </template>
                  <div style="padding: 8px 0">
                    <!-- 加载中 -->
                    <div v-if="isQualificationLoading(getQualificationsByOtherName(name)[0])" style="text-align: center; padding: 20px 0">
                      <NSpin size="small" />
                      <div style="font-size: 12px; color: #999; margin-top: 8px">加载图片中...</div>
                    </div>
                    <!-- 已有图片 -->
                    <div v-else-if="getQualificationImageUrls(getQualificationsByOtherName(name)[0])" style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 12px">
                      <div v-for="(url, idx) in getQualificationImageUrls(getQualificationsByOtherName(name)[0])!.split('|||')" :key="idx" style="position: relative; display: inline-block">
                        <NImage :src="url" width="70" height="70" :preview="true" style="object-fit: cover; border-radius: 6px; border: 1px solid #eee; cursor: pointer" />
                        <NButton size="tiny" type="error" style="position: absolute; top: -8px; right: -8px; min-width: 22px; height: 22px; padding: 0; border-radius: 50%" @click.stop="deleteQualificationImage(getQualificationsByOtherName(name)[0], url)"> × </NButton>
                      </div>
                    </div>
                    <!-- 上传/删除按钮 -->
                    <div style="display: flex; justify-content: space-between; align-items: center">
                      <NUpload :multiple="false" :max="1" accept="image/*" :show-file-list="false" :custom-request="(opt: any) => handleImageUpload(opt, 3, name)">
                        <NButton type="primary" size="small" :loading="uploadingTypes.has('other-' + name)">
                          {{ uploadingTypes.has('other-' + name) ? '处理中...' : '+ 上传' }}
                        </NButton>
                      </NUpload>
                      <NPopconfirm v-if="getQualificationsByOtherName(name).length" @positive-click="deleteQualification(getQualificationsByOtherName(name)[0].qualificationId)">
                        <template #trigger><NButton size="small" type="error">删除</NButton></template>
                        确定要删除该资质吗？
                      </NPopconfirm>
                    </div>
                  </div>
                </NCollapseItem>

                <!-- 添加自定义证书 -->
                <div style="padding: 12px 0; text-align: center">
                  <NButton size="small" @click="showAddOtherDialog = true">+ 添加自定义证书</NButton>
                </div>
              </NCollapse>
            </div>
          </NTabPane>

          <!-- 账户管理 -->
          <NTabPane name="account" tab="账户管理">
            <div style="padding: 8px 0">
              <NSpin :show="loadingAccount">
                <NEmpty v-if="!adminAccount" description="暂无管理员账户">
                  <template #extra>
                    <NButton type="primary" @click="handleResetPassword">创建账户</NButton>
                  </template>
                </NEmpty>
                <NCard v-else title="服务商管理员账户" size="small">
                  <NDescriptions size="small" :column="1">
                    <NDescriptionsItem label="用户名"><b>{{ adminAccount.username }}</b></NDescriptionsItem>
                    <NDescriptionsItem label="账户状态"><NTag :type="getStatusType(adminAccount.status)" size="small">{{ getStatusLabel(adminAccount.status) }}</NTag></NDescriptionsItem>
                    <NDescriptionsItem label="创建时间">{{ formatDate(adminAccount.createTime) }}</NDescriptionsItem>
                  </NDescriptions>
                  <template #footer>
                    <NSpace justify="end">
                      <NButton type="warning" size="small" @click="handleResetPassword">重置密码</NButton>
                    </NSpace>
                  </template>
                </NCard>
              </NSpin>
            </div>
          </NTabPane>

          <!-- 服务类型 -->
          <NTabPane name="serviceTypes" tab="服务类型">
            <div style="padding: 8px 0">
              <div v-if="detailData?.serviceTypes?.length">
                <div v-for="(service, index) in detailData.serviceTypes" :key="index" style="padding: 12px; border: 1px solid #eee; border-radius: 8px; margin-bottom: 12px">
                  <div style="font-weight: 500; margin-bottom: 8px">{{ service.serviceTypeName || service.serviceTypeCode || `服务${index + 1}` }}</div>
                  <NGrid :cols="2" :x-gap="12">
                    <NGi><div style="color: #999; font-size: 12px">服务类型编码</div><div style="margin-top: 2px">{{ service.serviceTypeCode || '-' }}</div></NGi>
                    <NGi><div style="color: #999; font-size: 12px">政府补贴价格</div><div style="margin-top: 2px">{{ service.subsidyPrice ? `¥${service.subsidyPrice}` : '-' }}</div></NGi>
                    <NGi><div style="color: #999; font-size: 12px">市场价格</div><div style="margin-top: 2px">{{ service.servicePrice ? `¥${service.servicePrice}` : '-' }}</div></NGi>
                    <NGi><div style="color: #999; font-size: 12px">服务区域</div><div style="margin-top: 2px">{{ service.serviceArea || '-' }}</div></NGi>
                  </NGrid>
                </div>
              </div>
              <NEmpty v-else description="暂无服务类型" />
            </div>
          </NTabPane>
        </NTabs>
      </NSpin>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="drawerVisible = false">关闭</NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>

  <!-- Image Preview Modal -->
  <NModal v-model:show="previewVisible" preset="card" title="图片预览" style="width: 700px">
    <div style="display: flex; justify-content: center; align-items: center; padding: 20px">
      <NImage :src="previewUrl" object-fit="contain" style="max-height: 500px" />
    </div>
  </NModal>

  <!-- 上传确认对话框 -->
  <NModal
    v-model:show="uploadConfirmVisible"
    preset="dialog"
    title="该类型已有证书"
    content="要覆盖现有证书还是添加新图片？"
    positive-text="覆盖"
    negative-text="添加"
    :loading="uploadLoading"
    @positive-click="handleOverwriteConfirm"
    @negative-click="handleAddConfirm"
  />

  <!-- 添加自定义证书弹窗 -->
  <NModal v-model:show="showAddOtherDialog" preset="dialog" title="添加自定义证书" style="width: 400px"
    positive-text="添加" negative-text="取消" @positive-click="handleAddOther" @negative-click="showAddOtherDialog = false">
    <div style="padding: 12px 0">
      <NInput v-model:value="newOtherName" placeholder="请输入证书名称（如：消防许可证）" />
    </div>
  </NModal>
</template>