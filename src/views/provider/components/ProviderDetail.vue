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
  NDescriptionsItem
} from 'naive-ui';
import type { UploadFile } from 'naive-ui';
import {
  fetchGetProvider,
  fetchCreateProviderCertificate,
  fetchDeleteProviderCertificate,
  fetchUpdateProvider,
  fetchGetProviderAdminAccount,
  fetchResetProviderAdminPassword
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
const loading = ref(false);
const detailData = ref<Api.Provider.Provider | null>(null);

// Certificate upload state
const uploadingCerts = ref<Set<string>>(new Set());

// License upload state
const uploadingLicense = ref(false);

// Admin account state
const adminAccount = ref<{ userId: string; username: string; status: string; createTime: string } | null>(null);
const loadingAccount = ref(false);

// Get current user info for watermark
const authStore = useAuthStore();
const watermarkText = computed(() => {
  const user = authStore.userInfo;
  const account = user?.userName || user?.realName || '未知用户';
  return `社区智慧运营管理平台\n${account}`;
});

// Image preview
const previewVisible = ref(false);
const previewUrl = ref('');
const previewImages = ref<string[]>([]);

const drawerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
});

// Reset data when drawer closes
watch(drawerVisible, (val) => {
  if (!val) {
    detailData.value = null;
    adminAccount.value = null;
  }
});

async function loadProviderDetail() {
  if (!props.providerId) return;

  loading.value = true;
  try {
    const { data, error } = await fetchGetProvider(props.providerId);
    if (error) {
      message.error(error.message || '获取服务商详情失败');
      return;
    }
    if (data) {
      detailData.value = data;
    }
  } finally {
    loading.value = false;
  }
}

async function loadAdminAccount() {
  if (!props.providerId) return;

  loadingAccount.value = true;
  try {
    const { data, error } = await fetchGetProviderAdminAccount(props.providerId);
    if (error) {
      adminAccount.value = null;
      return;
    }
    if (data) {
      adminAccount.value = data;
    } else {
      adminAccount.value = null;
    }
  } finally {
    loadingAccount.value = false;
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

// Handle image preview - open modal with single image
function previewImage(url: string) {
  previewUrl.value = url;
  previewVisible.value = true;
}

// Handle image preview - open modal with multiple images
function previewImagesHandler(images: string[], index: number) {
  previewImages.value = images;
  previewUrl.value = images[index];
  previewVisible.value = true;
}

// Handle business license upload
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

// Handle image download with watermark
async function handleDownload(url: string, filename: string) {
  if (!url) return;
  try {
    const watermarkedBase64 = await addWatermarkToImage(url, watermarkText.value);
    const link = document.createElement('a');
    link.href = watermarkedBase64;
    link.download = filename;
    link.click();
    message.success('下载成功');
  } catch (err) {
    console.error('Download failed', err);
    message.error('下载失败');
  }
}

// Handle certificate upload request
function handleCertUploadRequest({ file }: { file: UploadFile }) {
  if (!file.file || !props.providerId) return false;

  const reader = new FileReader();
  reader.onload = (e) => {
    if (e.target?.result) {
      const base64Data = e.target.result as string;
      uploadCertificate(base64Data, file.name, file.file.size);
    }
  };
  reader.readAsDataURL(file.file);
  return false;
}

async function uploadCertificate(base64Data: string, fileName: string, fileSize: number) {
  if (!props.providerId) return;

  // Check if this exact file already exists in the list (by name + size)
  const fileKey = `${fileName}-${fileSize}`;
  const alreadyExists = detailData.value?.qualifications?.some(
    cert => cert.qualificationName === fileName
  );
  if (alreadyExists) {
    message.warning('该证书已存在，请勿重复上传');
    return;
  }

  // Prevent duplicate uploads in progress: use filename+size as key
  if (uploadingCerts.value.has(fileKey)) {
    message.warning('文件正在上传中，请勿重复上传');
    return;
  }

  uploadingCerts.value.add(fileKey);
  try {
    // Extract cert type from filename or use default
    const qualificationType = fileName.split('.').pop() || 'CERT';
    await fetchCreateProviderCertificate(props.providerId, {
      qualificationName: fileName,
      qualificationType,
      attachmentUrl: base64Data
    });
    message.success('上传成功');
    await loadProviderDetail();
  } catch (e) {
    console.error('Failed to upload certificate', e);
    message.error('上传失败');
  } finally {
    uploadingCerts.value.delete(fileKey);
  }
}

// Handle certificate delete
async function handleDeleteCert(certId: string) {
  try {
    await fetchDeleteProviderCertificate(certId);
    message.success('删除成功');
    await loadProviderDetail();
  } catch (e) {
    console.error('Failed to delete certificate', e);
    message.error('删除失败');
  }
}

// Handle reset password
async function handleResetPassword() {
  if (!props.providerId) return;

  try {
    const { data, error } = await fetchResetProviderAdminPassword(props.providerId);
    if (error) {
      message.error(error.message || '操作失败');
      return;
    }
    message.success(data);
    await loadAdminAccount();
  } catch (e) {
    console.error('Failed to reset password', e);
    message.error('操作失败');
  }
}

// Status options
const statusOptions: Record<string, string> = {
  ENABLED: '正常',
  DISABLED: '禁用',
  NORMAL: '正常'
};

function getStatusType(status: string): 'success' | 'error' | 'warning' {
  if (status === 'ENABLED' || status === 'NORMAL') return 'success';
  if (status === 'DISABLED') return 'error';
  return 'warning';
}

function getStatusLabel(status: string): string {
  return statusOptions[status] || status || '-';
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-';
  return dateStr.split('T')[0];
}

// Get all certificates (business license + qualifications) for merged tab
const allCertificates = computed(() => {
  const certs: { id: string; url: string; name: string; type: 'license' | 'cert' }[] = [];

  // Add business license
  if (detailData.value?.businessLicense) {
    certs.push({
      id: 'business-license',
      url: detailData.value.businessLicense,
      name: '营业执照',
      type: 'license'
    });
  }

  // Add qualifications
  if (detailData.value?.qualifications) {
    detailData.value.qualifications.forEach(cert => {
      if (cert.attachmentUrl) {
        certs.push({
          id: cert.qualificationId,
          url: cert.attachmentUrl,
          name: cert.qualificationName || '证书',
          type: 'cert'
        });
      }
    });
  }

  return certs;
});
</script>

<template>
  <NDrawer v-model:show="drawerVisible" :width="600" placement="right" closable>
    <NDrawerContent title="服务商详情" closable>
      <template #header>
        <div style="display: flex; flex-direction: column; gap: 4px">
          <div style="font-size: 16px; font-weight: 600">服务商详情</div>
          <div v-if="detailData" style="font-size: 13px; color: #666">
            {{ detailData.providerName }}
          </div>
        </div>
      </template>

      <NSpin :show="loading">
        <NTabs type="line" animated>
          <!-- 基本信息 -->
          <NTabPane name="basic" tab="基本信息">
            <div v-if="detailData" style="padding: 8px 0">
              <NGrid :cols="2" :x-gap="16" :y-gap="12">
                <NGi>
                  <div style="color: #999; font-size: 13px">服务商名称</div>
                  <div style="margin-top: 4px; font-weight: 500">{{ detailData.providerName }}</div>
                </NGi>
                <NGi>
                  <div style="color: #999; font-size: 13px">统一社会信用代码</div>
                  <div style="margin-top: 4px">{{ detailData.creditCode || '-' }}</div>
                </NGi>
                <NGi>
                  <div style="color: #999; font-size: 13px">服务商类型</div>
                  <div style="margin-top: 4px">{{ detailData.providerType === 'TECH_SERVICE' ? '网络科技服务' : detailData.providerType === 'HOME_CARE' ? '家政服务' : '养老服务' }}</div>
                </NGi>
                <NGi>
                  <div style="color: #999; font-size: 13px">服务类别</div>
                  <div style="margin-top: 4px">{{ detailData.serviceCategory === 'ELDER_CARE' ? '养老服务' : '家政服务' }}</div>
                </NGi>
                <NGi>
                  <div style="color: #999; font-size: 13px">法人姓名</div>
                  <div style="margin-top: 4px">{{ detailData.legalPerson || '-' }}</div>
                </NGi>
                <NGi>
                  <div style="color: #999; font-size: 13px">联系电话</div>
                  <div style="margin-top: 4px">{{ detailData.contactPhone || '-' }}</div>
                </NGi>
                <NGi :span="2">
                  <div style="color: #999; font-size: 13px">联系地址</div>
                  <div style="margin-top: 4px">{{ detailData.address || '-' }}</div>
                </NGi>
                <NGi :span="2">
                  <div style="color: #999; font-size: 13px">服务区域</div>
                  <div style="margin-top: 4px">{{ detailData.serviceAreas || '-' }}</div>
                </NGi>
                <NGi :span="2">
                  <div style="color: #999; font-size: 13px">简介</div>
                  <div style="margin-top: 4px">{{ detailData.description || '-' }}</div>
                </NGi>
                <NGi>
                  <div style="color: #999; font-size: 13px">状态</div>
                  <div style="margin-top: 4px">
                    <NTag :type="getStatusType(detailData.status)" size="small">
                      {{ getStatusLabel(detailData.status) }}
                    </NTag>
                  </div>
                </NGi>
                <NGi>
                  <div style="color: #999; font-size: 13px">评分</div>
                  <div style="margin-top: 4px">
                    {{ detailData.rating ? `${detailData.rating} (${detailData.ratingCount}次)` : '-' }}
                  </div>
                </NGi>
                <NGi :span="2">
                  <div style="color: #999; font-size: 13px">创建时间</div>
                  <div style="margin-top: 4px">{{ formatDate(detailData.createTime) }}</div>
                </NGi>
              </NGrid>
            </div>
          </NTabPane>

          <!-- 证照管理（营业执照第一行 + 资质证书从第二行开始） -->
          <NTabPane name="certificates" tab="证照管理">
            <div style="padding: 8px 0">
              <!-- 营业执照预览 + 上传按钮 -->
              <div style="display: flex; align-items: flex-start; gap: 12px; margin-bottom: 16px">
                <div style="color: #666; font-size: 13px; line-height: 32px">营业执照</div>
                <NUpload
                  :show-file-list="false"
                  accept="image/*"
                  :custom-request="handleLicenseUploadRequest"
                >
                  <NButton size="small" type="primary" :loading="uploadingLicense">
                    {{ detailData?.businessLicense ? '重新上传' : '上传执照' }}
                  </NButton>
                </NUpload>
              </div>

              <!-- 营业执照图片 -->
              <div v-if="detailData?.businessLicense" style="margin-bottom: 16px; margin-left: 44px">
                <img
                  :src="detailData.businessLicense"
                  style="width: 120px; height: 120px; object-fit: cover; border-radius: 8px; cursor: pointer; border: 2px solid #e6e6e6"
                  @click="previewImage(detailData.businessLicense!)"
                />
              </div>

              <!-- 第二行开始：资质证书 -->
              <div style="border-top: 1px dashed #eee; padding-top: 16px">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px">
                  <div style="color: #666; font-size: 13px">资质证书</div>
                  <NUpload
                    :show-file-list="false"
                    accept="image/*"
                    :custom-request="handleCertUploadRequest"
                  >
                    <NButton size="small" type="primary" :loading="uploadingCerts.size > 0">
                      上传证书
                    </NButton>
                  </NUpload>
                </div>

                <!-- 资质证书网格 -->
                <div v-if="detailData?.qualifications?.length" style="display: flex; flex-wrap: wrap; gap: 12px">
                  <div
                    v-for="(cert, index) in detailData.qualifications"
                    :key="cert.qualificationId"
                    style="position: relative; width: 100px; height: 100px"
                  >
                    <img
                      :src="cert.attachmentUrl"
                      style="width: 100px; height: 100px; object-fit: cover; border-radius: 8px; cursor: pointer; border: 2px solid #e6e6e6"
                      @click="previewImage(cert.attachmentUrl)"
                    />
                    <div
                      style="position: absolute; bottom: 4px; left: 4px; right: 4px; background: rgba(0,0,0,0.6); color: white; font-size: 10px; padding: 2px 4px; border-radius: 4px; text-align: center; overflow: hidden; text-overflow: ellipsis; white-space: nowrap"
                    >
                      {{ cert.qualificationName }}
                    </div>
                    <div
                      style="position: absolute; top: -6px; right: -6px; width: 18px; height: 18px; background: #ff4d4f; border-radius: 50%; color: white; text-align: center; line-height: 18px; font-size: 10px; cursor: pointer"
                      @click.stop="handleDeleteCert(cert.qualificationId)"
                    >
                      ×
                    </div>
                  </div>
                </div>
                <NEmpty v-else-if="!detailData?.qualifications?.length" description="暂无资质证书" />
              </div>
            </div>
          </NTabPane>

          <!-- 账户管理 -->
          <NTabPane name="account" tab="账户管理">
            <div style="padding: 8px 0">
              <NSpin :show="loadingAccount">
                <NEmpty v-if="!adminAccount" description="暂无管理员账户">
                  <template #extra>
                    <NButton type="primary" @click="handleResetPassword">
                      创建账户
                    </NButton>
                  </template>
                </NEmpty>
                <NCard v-else title="服务商管理员账户" size="small">
                  <NDescriptions size="small" :column="1">
                    <NDescriptionsItem label="用户名">
                      <b>{{ adminAccount.username }}</b>
                    </NDescriptionsItem>
                    <NDescriptionsItem label="账户状态">
                      <NTag :type="getStatusType(adminAccount.status)" size="small">
                        {{ getStatusLabel(adminAccount.status) }}
                      </NTag>
                    </NDescriptionsItem>
                    <NDescriptionsItem label="创建时间">
                      {{ formatDate(adminAccount.createTime) }}
                    </NDescriptionsItem>
                  </NDescriptions>
                  <template #footer>
                    <NSpace justify="end">
                      <NButton type="warning" size="small" @click="handleResetPassword">
                        重置密码
                      </NButton>
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
                <div
                  v-for="(service, index) in detailData.serviceTypes"
                  :key="index"
                  style="padding: 12px; border: 1px solid #eee; border-radius: 8px; margin-bottom: 12px"
                >
                  <div style="font-weight: 500; margin-bottom: 8px">{{ service.serviceTypeName || service.serviceTypeCode || `服务${index + 1}` }}</div>
                  <NGrid :cols="2" :x-gap="12">
                    <NGi>
                      <div style="color: #999; font-size: 12px">服务类型编码</div>
                      <div style="margin-top: 2px">{{ service.serviceTypeCode || '-' }}</div>
                    </NGi>
                    <NGi>
                      <div style="color: #999; font-size: 12px">政府补贴价格</div>
                      <div style="margin-top: 2px">{{ service.subsidyPrice ? `¥${service.subsidyPrice}` : '-' }}</div>
                    </NGi>
                    <NGi>
                      <div style="color: #999; font-size: 12px">市场价格</div>
                      <div style="margin-top: 2px">{{ service.servicePrice ? `¥${service.servicePrice}` : '-' }}</div>
                    </NGi>
                    <NGi>
                      <div style="color: #999; font-size: 12px">服务区域</div>
                      <div style="margin-top: 2px">{{ service.serviceArea || '-' }}</div>
                    </NGi>
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
</template>
