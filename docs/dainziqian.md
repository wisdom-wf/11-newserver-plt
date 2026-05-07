# 腾讯电子签集成指南 - 自建应用

> **目标**：将腾讯电子签功能集成到你的Node.js + Vue自建应用中  
> **适用场景**：企业内部系统集成，将电子合同能力融入业务系统  
> **预计完成时间**：2-3小时（含测试）

---

## 📋 目录

1. [前置准备](#1-前置准备)
2. [获取API密钥](#2-获取api密钥)
3. [项目结构规划](#3-项目结构规划)
4. [后端集成（Node.js）](#4-后端集成nodejs)
5. [前端集成（Vue）](#5-前端集成vue)
6. [核心功能实现](#6-核心功能实现)
7. [测试指南](#7-测试指南)
8. [常见问题](#8-常见问题)

---

## 1. 前置准备

### 1.1 确认已完成的事项

在开始编码前，请确认你已完成以下操作：

- ✅ 已在[腾讯电子签官网](https://qian.tencent.com/)注册账号
- ✅ 已完成企业实名认证
- ✅ 已开通电子签服务
- ✅ 已创建自建应用并获取应用ID

### 1.2 需要准备的信息

在开始集成前，你需要从腾讯电子签控制台获取以下信息：

| 参数名称 | 说明 | 获取位置 |
|---------|------|---------|
| SecretId | 腾讯云API密钥ID | [访问管理控制台](https://console.cloud.tencent.com/cam/capi) |
| SecretKey | 腾讯云API密钥Key | [访问管理控制台](https://console.cloud.tencent.com/cam/capi) |
| AgentId | 应用ID（自建应用） | 电子签控制台 → 应用管理 |
| ProxyOrganizationOpenId | 子企业编号（可选） | 电子签控制台 |

### 1.3 了解核心业务流程

腾讯电子签的核心流程如下：

```
1. 上传合同文件或选择模板
   ↓
2. 创建合同流程（指定签署方）
   ↓
3. 发起签署（发送签署链接给签署方）
   ↓
4. 签署方签署合同
   ↓
5. 查询合同状态/下载合同
```

---

## 2. 获取API密钥

### 2.1 获取腾讯云API密钥

**步骤1**：访问[腾讯云访问管理控制台](https://console.cloud.tencent.com/cam/capi)

**步骤2**：点击「新建密钥」或查看已有密钥

**步骤3**：记录`SecretId`和`SecretKey`（注意保密）

### 2.2 获取电子签应用信息

**步骤1**：登录[腾讯电子签控制台](https://qian.tencent.com/)

**步骤2**：进入「应用管理」→「自建应用」

**步骤3**：创建应用或选择已有应用

**步骤4**：记录`AgentId`（应用ID）

**步骤5**：配置应用回调地址（用于接收签署结果通知）

---

## 3. 项目结构规划

### 3.1 推荐的项目结构

```
your-project/
├── server/                 # Node.js 后端
│   ├── src/
│   │   ├── config/
│   │   │   └── ess.config.js      # 电子签配置文件
│   │   ├── controllers/
│   │   │   └── ess.controller.js  # 电子签控制器
│   │   ├── services/
│   │   │   └── ess.service.js     # 电子签业务逻辑
│   │   ├── routes/
│   │   │   └── ess.routes.js      # 电子签路由
│   │   └── utils/
│   │       └── ess.sign.js        # 签名工具（如果需要）
│   ├── package.json
│   └── .env                      # 环境变量（不提交到git）
├── client/                # Vue 前端
│   ├── src/
│   │   ├── api/
│   │   │   └── ess.js            # 电子签API调用
│   │   ├── views/
│   │   │   └── contract/
│   │   │       ├── CreateContract.vue
│   │   │       ├── ContractList.vue
│   │   │       └── SignContract.vue
│   │   └── components/
│   │       └── EssCallback.vue    # 回调处理组件
│   └── package.json
└── docs/
    └── 腾讯电子签集成指南.md
```

---

## 4. 后端集成（Node.js）

### 4.1 安装依赖

**操作步骤**：

```bash
# 进入后端项目目录
cd server

# 安装腾讯云SDK（包含电子签API）
npm install tencentcloud-sdk-nodejs --save

# 安装其他必要依赖
npm install dotenv express axios --save
```

### 4.2 创建配置文件

**文件位置**：`server/src/config/ess.config.js`

**操作说明**：创建电子签配置文件，存储应用凭证

```javascript
// 腾讯电子签配置文件
module.exports = {
  // 腾讯云API密钥
  secretId: process.env.TENCENT_SECRET_ID || '',
  secretKey: process.env.TENCENT_SECRET_KEY || '',
  
  // 电子签应用信息
  agentId: process.env.ESS_AGENT_ID || '',
  
  // API端点
  endpoint: 'ess.tencentcloudapi.com',
  region: 'ap-guangzhou',
  
  // 回调地址（签署结果通知）
  callbackUrl: process.env.ESS_CALLBACK_URL || 'http://your-domain.com/api/ess/callback',
  
  // 默认配置
  defaultConfig: {
    // 合同有效期（天）
    expirePeriod: 7,
    // 是否需要签署前认证
    needSignReview: false,
    // 签署完成后是否自动跳转
    autoJumpAfterSign: true
  }
};
```

### 4.3 创建环境变量文件

**文件位置**：`server/.env`（注意：此文件不应提交到git）

```bash
# 腾讯云API密钥
TENCENT_SECRET_ID=你的SecretId
TENCENT_SECRET_KEY=你的SecretKey

# 电子签应用ID
ESS_AGENT_ID=你的应用AgentId

# 回调地址
ESS_CALLBACK_URL=http://your-domain.com/api/ess/callback

# 服务器端口
PORT=3000
```

### 4.4 创建电子签服务类

**文件位置**：`server/src/services/ess.service.js`

**功能说明**：封装腾讯电子签API调用

```javascript
const tencentcloud = require('tencentcloud-sdk-nodejs');
const EssClient = tencentcloud.ess.v20201111.Client;
const config = require('../config/ess.config');

class EssService {
  constructor() {
    this.client = new EssClient({
      credential: {
        secretId: config.secretId,
        secretKey: config.secretKey,
      },
      region: config.region,
      profile: {
        httpProfile: {
          endpoint: config.endpoint,
        },
      },
    });
  }

  /**
   * 上传合同文件
   * @param {string} filePath - 文件路径
   * @param {string} fileName - 文件名
   * @returns {Promise<string>} 文件ID
   */
  async uploadFile(filePath, fileName) {
    try {
      const fs = require('fs');
      const fileContent = fs.readFileSync(filePath).toString('base64');
      
      const params = {
        Agent: {
          AgentId: config.agentId,
        },
        FileName: fileName,
        FileBody: fileContent,
      };
      
      const response = await this.client.UploadFiles(params);
      return response.FileIds[0];
    } catch (error) {
      console.error('上传文件失败:', error);
      throw error;
    }
  }

  /**
   * 创建合同流程
   * @param {Object} options - 合同参数
   * @returns {Promise<string>} 合同流程ID
   */
  async createFlow(options) {
    try {
      const params = {
        Agent: {
          AgentId: config.agentId,
        },
        FlowName: options.flowName,
        Approvers: options.approvers.map(approver => ({
          ApproverType: approver.type || 0, // 0=个人, 1=企业
          ApproverName: approver.name,
          ApproverMobile: approver.mobile,
          ApproverIdCardNumber: approver.idCard,
          ApproverIdCardType: 'ID_CARD',
        })),
        FileIds: options.fileIds,
        FlowDescription: options.description || '',
        ExpireTime: options.expireTime || this.getExpireTime(7),
      };
      
      const response = await this.client.CreateFlow(params);
      return response.FlowId;
    } catch (error) {
      console.error('创建合同流程失败:', error);
      throw error;
    }
  }

  /**
   * 发起合同签署
   * @param {string} flowId - 合同流程ID
   * @returns {Promise<Object>} 签署链接信息
   */
  async startFlow(flowId) {
    try {
      const params = {
        Agent: {
          AgentId: config.agentId,
        },
        FlowId: flowId,
      };
      
      const response = await this.client.StartFlow(params);
      return response;
    } catch (error) {
      console.error('发起合同签署失败:', error);
      throw error;
    }
  }

  /**
   * 获取签署链接
   * @param {string} flowId - 合同流程ID
   * @param {string} approverId - 签署方ID
   * @returns {Promise<string>} 签署链接
   */
  async getSignUrl(flowId, approverId) {
    try {
      const params = {
        Agent: {
          AgentId: config.agentId,
        },
        FlowId: flowId,
        ApproverInfOS: [{
          ApproverId: approverId,
        }],
      };
      
      const response = await this.client.DescribeSignFaceVideo(params);
      return response.SignUrl;
    } catch (error) {
      console.error('获取签署链接失败:', error);
      throw error;
    }
  }

  /**
   * 查询合同状态
   * @param {string} flowId - 合同流程ID
   * @returns {Promise<Object>} 合同状态信息
   */
  async describeFlow(flowId) {
    try {
      const params = {
        Agent: {
          AgentId: config.agentId,
        },
        FlowId: flowId,
      };
      
      const response = await this.client.DescribeFlow(params);
      return response;
    } catch (error) {
      console.error('查询合同状态失败:', error);
      throw error;
    }
  }

  /**
   * 下载合同文件
   * @param {string} flowId - 合同流程ID
   * @returns {Promise<string>} 合同下载链接
   */
  async downloadFlow(flowId) {
    try {
      const params = {
        Agent: {
          AgentId: config.agentId,
        },
        FlowId: flowId,
      };
      
      const response = await this.client.DownloadFlowFile(params);
      return response.DownloadUrl;
    } catch (error) {
      console.error('下载合同失败:', error);
      throw error;
    }
  }

  /**
   * 计算过期时间
   * @param {number} days - 天数
   * @returns {number} 时间戳（毫秒）
   */
  getExpireTime(days) {
    const now = new Date();
    now.setDate(now.getDate() + days);
    return now.getTime();
  }
}

module.exports = new EssService();
```

### 4.5 创建控制器

**文件位置**：`server/src/controllers/ess.controller.js`

**功能说明**：处理HTTP请求，调用电子签服务

```javascript
const essService = require('../services/ess.service');
const fs = require('fs');
const path = require('path');

class EssController {
  /**
   * 上传合同文件
   */
  async uploadFile(req, res) {
    try {
      const { file } = req;
      if (!file) {
        return res.status(400).json({ error: '请上传文件' });
      }

      const filePath = file.path;
      const fileName = file.originalname;

      const fileId = await essService.uploadFile(filePath, fileName);

      // 删除临时文件
      fs.unlinkSync(filePath);

      res.json({
        success: true,
        fileId,
        message: '文件上传成功',
      });
    } catch (error) {
      res.status(500).json({
        success: false,
        error: error.message,
      });
    }
  }

  /**
   * 创建合同
   */
  async createContract(req, res) {
    try {
      const { flowName, approvers, fileIds, description } = req.body;

      if (!flowName || !approvers || !fileIds) {
        return res.status(400).json({ error: '缺少必要参数' });
      }

      const flowId = await essService.createFlow({
        flowName,
        approvers,
        fileIds: Array.isArray(fileIds) ? fileIds : [fileIds],
        description,
      });

      res.json({
        success: true,
        flowId,
        message: '合同创建成功',
      });
    } catch (error) {
      res.status(500).json({
        success: false,
        error: error.message,
      });
    }
  }

  /**
   * 发起签署
   */
  async startSign(req, res) {
    try {
      const { flowId } = req.body;

      if (!flowId) {
        return res.status(400).json({ error: '缺少flowId' });
      }

      const result = await essService.startFlow(flowId);

      res.json({
        success: true,
        data: result,
        message: '签署已发起',
      });
    } catch (error) {
      res.status(500).json({
        success: false,
        error: error.message,
      });
    }
  }

  /**
   * 获取签署链接
   */
  async getSignUrl(req, res) {
    try {
      const { flowId, approverId } = req.query;

      if (!flowId || !approverId) {
        return res.status(400).json({ error: '缺少必要参数' });
      }

      const signUrl = await essService.getSignUrl(flowId, approverId);

      res.json({
        success: true,
        signUrl,
      });
    } catch (error) {
      res.status(500).json({
        success: false,
        error: error.message,
      });
    }
  }

  /**
   * 查询合同状态
   */
  async getContractStatus(req, res) {
    try {
      const { flowId } = req.params;

      if (!flowId) {
        return res.status(400).json({ error: '缺少flowId' });
      }

      const flowInfo = await essService.describeFlow(flowId);

      res.json({
        success: true,
        data: flowInfo,
      });
    } catch (error) {
      res.status(500).json({
        success: false,
        error: error.message,
      });
    }
  }

  /**
   * 下载合同
   */
  async downloadContract(req, res) {
    try {
      const { flowId } = req.params;

      if (!flowId) {
        return res.status(400).json({ error: '缺少flowId' });
      }

      const downloadUrl = await essService.downloadFlow(flowId);

      res.json({
        success: true,
        downloadUrl,
      });
    } catch (error) {
      res.status(500).json({
        success: false,
        error: error.message,
      });
    }
  }

  /**
   * 处理回调通知
   */
  async handleCallback(req, res) {
    try {
      const callbackData = req.body;
      
      // 验证签名（如果需要）
      // TODO: 实现签名验证逻辑
      
      console.log('收到电子签回调:', callbackData);
      
      // 处理回调逻辑
      // 例如：更新合同状态、发送通知等
      
      res.json({ success: true });
    } catch (error) {
      console.error('处理回调失败:', error);
      res.status(500).json({ success: false });
    }
  }
}

module.exports = new EssController();
```

### 4.6 创建路由

**文件位置**：`server/src/routes/ess.routes.js`

```javascript
const express = require('express');
const router = express.Router();
const essController = require('../controllers/ess.controller');
const multer = require('multer');

// 配置文件上传
const upload = multer({ dest: 'uploads/' });

// 上传合同文件
router.post('/upload', upload.single('file'), essController.uploadFile.bind(essController));

// 创建合同
router.post('/create', essController.createContract.bind(essController));

// 发起签署
router.post('/start', essController.startSign.bind(essController));

// 获取签署链接
router.get('/sign-url', essController.getSignUrl.bind(essController));

// 查询合同状态
router.get('/status/:flowId', essController.getContractStatus.bind(essController));

// 下载合同
router.get('/download/:flowId', essController.downloadContract.bind(essController));

// 回调接口
router.post('/callback', essController.handleCallback.bind(essController));

module.exports = router;
```

### 4.7 注册路由

**文件位置**：`server/src/app.js` 或 `server/src/index.js`

```javascript
const express = require('express');
const cors = require('cors');
const essRoutes = require('./routes/ess.routes');
require('dotenv').config();

const app = express();

// 中间件
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// 路由
app.use('/api/ess', essRoutes);

// 启动服务器
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`服务器运行在 http://localhost:${PORT}`);
});
```

---

## 5. 前端集成（Vue）

### 5.1 创建API调用文件

**文件位置**：`client/src/api/ess.js`

```javascript
import axios from 'axios';

const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:3000/api';

/**
 * 上传合同文件
 */
export const uploadFile = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  
  return axios.post(`${API_BASE_URL}/ess/upload`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

/**
 * 创建合同
 */
export const createContract = (data) => {
  return axios.post(`${API_BASE_URL}/ess/create`, data);
};

/**
 * 发起签署
 */
export const startSign = (flowId) => {
  return axios.post(`${API_BASE_URL}/ess/start`, { flowId });
};

/**
 * 获取签署链接
 */
export const getSignUrl = (flowId, approverId) => {
  return axios.get(`${API_BASE_URL}/ess/sign-url`, {
    params: { flowId, approverId },
  });
};

/**
 * 查询合同状态
 */
export const getContractStatus = (flowId) => {
  return axios.get(`${API_BASE_URL}/ess/status/${flowId}`);
};

/**
 * 下载合同
 */
export const downloadContract = (flowId) => {
  return axios.get(`${API_BASE_URL}/ess/download/${flowId}`);
};
```

### 5.2 创建合同列表页面

**文件位置**：`client/src/views/contract/ContractList.vue`

```vue
<template>
  <div class="contract-list">
    <h2>合同列表</h2>
    
    <div class="actions">
      <el-button type="primary" @click="showCreateDialog = true">
        创建合同
      </el-button>
    </div>
    
    <el-table :data="contracts" style="width: 100%">
      <el-table-column prop="flowName" label="合同名称" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button @click="viewContract(scope.row)">查看</el-button>
          <el-button @click="downloadContract(scope.row.flowId)">下载</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 创建合同对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建合同">
      <el-form :model="contractForm">
        <el-form-item label="合同名称">
          <el-input v-model="contractForm.flowName" />
        </el-form-item>
        
        <el-form-item label="签署方姓名">
          <el-input v-model="contractForm.approverName" />
        </el-form-item>
        
        <el-form-item label="签署方手机号">
          <el-input v-model="contractForm.approverMobile" />
        </el-form-item>
        
        <el-form-item label="合同文件">
          <el-upload
            action=""
            :auto-upload="false"
            :on-change="handleFileChange"
          >
            <el-button>选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createContract">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { createContract, uploadFile } from '@/api/ess';

export default {
  name: 'ContractList',
  setup() {
    const contracts = ref([]);
    const showCreateDialog = ref(false);
    const contractForm = reactive({
      flowName: '',
      approverName: '',
      approverMobile: '',
      file: null,
    });
    
    const handleFileChange = (file) => {
      contractForm.file = file.raw;
    };
    
    const createContractHandler = async () => {
      try {
        // 上传文件
        const uploadRes = await uploadFile(contractForm.file);
        const fileId = uploadRes.data.fileId;
        
        // 创建合同
        const createRes = await createContract({
          flowName: contractForm.flowName,
          approvers: [{
            name: contractForm.approverName,
            mobile: contractForm.approverMobile,
          }],
          fileIds: [fileId],
        });
        
        console.log('合同创建成功:', createRes.data.flowId);
        showCreateDialog.value = false;
        
        // 刷新列表
        loadContracts();
      } catch (error) {
        console.error('创建合同失败:', error);
      }
    };
    
    const loadContracts = () => {
      // TODO: 实现加载合同列表的逻辑
    };
    
    const viewContract = (contract) => {
      // TODO: 实现查看合同详情的逻辑
    };
    
    const downloadContract = (flowId) => {
      // TODO: 实现下载合同的逻辑
    };
    
    onMounted(() => {
      loadContracts();
    });
    
    return {
      contracts,
      showCreateDialog,
      contractForm,
      handleFileChange,
      createContract: createContractHandler,
      viewContract,
      downloadContract,
    };
  },
};
</script>
```

---

## 6. 核心功能实现

### 6.1 完整签署流程示例

以下是一个完整的合同签署流程示例：

```javascript
// 完整签署流程示例
async function completeSignProcess() {
  const essService = require('./services/ess.service');
  
  try {
    // 步骤1: 上传合同文件
    console.log('步骤1: 上传合同文件...');
    const fileId = await essService.uploadFile(
      './contracts/租赁协议.pdf',
      '租赁协议.pdf'
    );
    console.log('文件上传成功, FileId:', fileId);
    
    // 步骤2: 创建合同流程
    console.log('步骤2: 创建合同流程...');
    const flowId = await essService.createFlow({
      flowName: '房屋租赁合同',
      approvers: [
        {
          name: '张三',
          mobile: '13800138000',
          idCard: '110101199001011234',
          type: 0, // 0=个人
        },
        {
          name: '李四',
          mobile: '13800138001',
          idCard: '110101199001015678',
          type: 0,
        },
      ],
      fileIds: [fileId],
      description: '房屋租赁合同 - 深圳市南山区',
    });
    console.log('合同创建成功, FlowId:', flowId);
    
    // 步骤3: 发起签署
    console.log('步骤3: 发起签署...');
    await essService.startFlow(flowId);
    console.log('签署已发起');
    
    // 步骤4: 获取签署链接（发送给签署方）
    console.log('步骤4: 获取签署链接...');
    const signUrl = await essService.getSignUrl(flowId, 'approver-id');
    console.log('签署链接:', signUrl);
    // TODO: 通过短信/邮件等方式发送签署链接给签署方
    
    // 步骤5: 等待签署完成（通过回调或轮询）
    console.log('步骤5: 等待签署完成...');
    // 方式1: 等待回调通知
    // 方式2: 轮询查询状态
    let flowInfo;
    do {
      await new Promise(resolve => setTimeout(resolve, 5000)); // 等待5秒
      flowInfo = await essService.describeFlow(flowId);
      console.log('合同状态:', flowInfo.FlowStatus);
    } while (flowInfo.FlowStatus !== 'Finish');
    
    // 步骤6: 下载已签署的合同
    console.log('步骤6: 下载合同...');
    const downloadUrl = await essService.downloadFlow(flowId);
    console.log('合同下载链接:', downloadUrl);
    
    console.log('签署流程完成！');
  } catch (error) {
    console.error('签署流程失败:', error);
  }
}
```

### 6.2 使用合同模板

如果有预定义的合同模板，可以使用模板发起合同：

```javascript
/**
 * 通过模板创建合同
 */
async function createContractFromTemplate() {
  try {
    const params = {
      Agent: {
        AgentId: config.agentId,
      },
      FlowName: '模板合同',
      TemplateId: 'your-template-id', // 模板ID
      Approvers: [/* 签署方信息 */],
      TemplateComponents: [/* 模板填充参数 */],
    };
    
    const response = await this.client.CreateFlowByTemplates(params);
    return response.FlowId;
  } catch (error) {
    console.error('通过模板创建合同失败:', error);
    throw error;
  }
}
```

---

## 7. 测试指南

### 7.1 测试环境配置

腾讯电子签提供测试环境，建议先在测试环境完成联调：

1. 在电子签控制台切换到测试环境
2. 使用测试环境的API域名：`ess.test.tencentcloudapi.com`
3. 测试环境不扣除合同份额

### 7.2 测试用例

| 测试项 | 测试步骤 | 预期结果 |
|-------|---------|---------|
| 文件上传 | 调用uploadFile接口 | 返回文件ID |
| 合同创建 | 调用createFlow接口 | 返回合同流程ID |
| 发起签署 | 调用startFlow接口 | 签署状态变更为"签署中" |
| 获取签署链接 | 调用getSignUrl接口 | 返回有效的签署URL |
| 签署完成回调 | 签署方完成签署 | 收到回调通知 |
| 合同下载 | 调用downloadFlow接口 | 返回有效的下载URL |

### 7.3 常见问题排查

**问题1：API调用返回"认证失败"**

- 检查SecretId和SecretKey是否正确
- 检查系统时间是否准确（时间偏差过大会导致签名失败）

**问题2：文件上传失败**

- 检查文件格式是否支持（支持PDF、Word、图片等）
- 检查文件大小是否超过限制（单个文件不超过50MB）

**问题3：签署链接无法访问**

- 检查签署方信息是否正确
- 检查合同状态是否为"签署中"

---

## 8. 常见问题

### 8.1 如何获取API文档？

- 官方API文档：https://cloud.tencent.com/document/product/1323/61507
- 开发者中心：https://qian.tencent.com/developers/start/

### 8.2 支持哪些文件格式？

- PDF、Word（.doc/.docx）、图片（.jpg/.png/.bmp）
- 建议使用PDF格式，效果最佳

### 8.3 如何验证签署方的身份？

- 默认通过手机号验证码认证
- 可配置人脸识别认证（需额外开通）

### 8.4 合同签署后如何存证？

- 腾讯电子签提供区块链存证服务
- 可通过API获取存证证书

### 8.5 如何集成到移动端？

- 可使用H5嵌入页面
- 可使用小程序（需额外配置）

---

## 📚 附录

### A. 完整代码仓库结构

```
your-project/
├── server/
│   ├── src/
│   │   ├── config/
│   │   │   └── ess.config.js
│   │   ├── controllers/
│   │   │   └── ess.controller.js
│   │   ├── services/
│   │   │   └── ess.service.js
│   │   ├── routes/
│   │   │   └── ess.routes.js
│   │   └── app.js
│   ├── .env
│   └── package.json
├── client/
│   ├── src/
│   │   ├── api/
│   │   │   └── ess.js
│   │   └── views/
│   │       └── contract/
│   └── package.json
└── README.md
```

### B. 环境变量清单

| 变量名 | 说明 | 必填 | 示例 |
|-------|------|------|------|
| TENCENT_SECRET_ID | 腾讯云API密钥ID | 是 | AKID****** |
| TENCENT_SECRET_KEY | 腾讯云API密钥Key | 是 | ****** |
| ESS_AGENT_ID | 电子签应用ID | 是 | 自建应用AgentId |
| ESS_CALLBACK_URL | 回调地址 | 否 | http://domain.com/callback |

### C. 相关文档链接

- [腾讯电子签官网](https://qian.tencent.com/)
- [API文档](https://cloud.tencent.com/document/product/1323/61507)
- [SDK中心](https://cloud.tencent.com/document/sdk)
- [错误码查询](https://cloud.tencent.com/document/product/1323/61509)

---

## ✅ 检查清单

在完成集成后，请确认以下事项：

- [ ] 已安装腾讯云SDK依赖
- [ ] 已配置环境变量（.env文件）
- [ ] 已创建配置文件（ess.config.js）
- [ ] 已实现文件上传功能
- [ ] 已实现合同创建功能
- [ ] 已实现签署链接获取功能
- [ ] 已实现合同状态查询功能
- [ ] 已实现合同下载功能
- [ ] 已配置回调地址
- [ ] 已在测试环境完成联调
- [ ] 已处理错误和异常情况
- [ ] 已编写单元测试

---

**文档版本**：v1.0  
**最后更新**：2026-05-07  
**适用版本**：腾讯电子签 API v20201111

如有问题，请参考[官方文档](https://qian.tencent.com/developers/start/)或联系腾讯电子签技术支持。
