# 腾讯电子签模块 (Contract Module)

## 概述

合同模块用于实现智慧居家养老服务管理平台的电子签功能，与腾讯电子签平台集成，实现合同的创建、签署、管理全流程。

## 业务流程

```
1. 管理员点击"分配"按钮
   ↓
2. 选择服务人员，确认派单
   ↓
3. 后端自动创建腾讯电子签合同（甲方=服务商，乙方=服务人员）
   ↓
4. 前端显示"派单成功，合同已创建：XXX"
   ↓
5. 服务人员点击"接单"按钮
   ↓
6. 弹出合同详情对话框，显示甲方/乙方信息
   ↓
7. 服务人员点击"签署合同"按钮
   ↓
8. 跳转腾讯电子签签署页面完成签署
   ↓
9. 签署完成后可点击"开始服务"
```

## 数据库设计

### 表结构: `t_ess_contract`

```sql
CREATE TABLE `t_ess_contract` (
  `contract_id` VARCHAR(32) PRIMARY KEY COMMENT '合同ID',
  `contract_no` VARCHAR(64) COMMENT '合同编号',
  `order_id` VARCHAR(32) NOT NULL COMMENT '关联订单ID',
  `order_no` VARCHAR(64) COMMENT '订单编号',
  `flow_id` VARCHAR(128) COMMENT '腾讯电子签流程ID',
  `contract_name` VARCHAR(256) NOT NULL COMMENT '合同名称',
  `signers` TEXT COMMENT '签署方信息JSON',
  `status` VARCHAR(32) NOT NULL COMMENT '合同状态',
  `sign_url` VARCHAR(512) COMMENT '签署链接',
  `signed_time` DATETIME COMMENT '签署完成时间',
  `download_url` VARCHAR(512) COMMENT '合同下载链接',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_flow_id` (`flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### signers 字段格式

```json
[
  {"name": "李白", "mobile": "13658351276", "type": 0},
  {"name": "陕西红泥数智科技有限公司", "type": 1}
]
```

- `type=0`: 个人签署方（乙方 - 服务人员）
- `type=1`: 企业签署方（甲方 - 服务商）

### 合同状态

| 状态值 | 中文描述 | 说明 |
|--------|----------|------|
| DRAFT | 草稿 | 合同未发起 |
| INITIATED | 已发起 | 合同已创建，等待签署 |
| SIGNING | 签署中 | 正在签署中 |
| SIGNED | 已签署 | 双方签署完成 |
| COMPLETED | 已完成 | 合同已归档 |
| EXPIRED | 已过期 | 签署链接过期 |
| REJECTED | 已拒签 | 签署被拒绝 |
| CANCELLED | 已撤回 | 合同已撤回 |

## 后端设计

### 文件结构

```
elderly-care-server/src/main/java/com/elderlycare/
├── controller/ess/
│   └── ContractController.java       # 合同管理控制器
├── service/ess/
│   ├── ContractService.java         # 合同服务接口
│   └── impl/
│       └── ContractServiceImpl.java # 合同服务实现
├── entity/ess/
│   └── EssContract.java             # 合同实体
├── vo/ess/
│   ├── ContractVO.java             # 合同视图对象
│   └── SignUrlVO.java              # 签署链接视图对象
├── mapper/ess/
│   └── EssContractMapper.java      # 合同Mapper
├── dto/ess/
│   └── ContractQueryDTO.java       # 查询DTO
└── config/
    └── TencentEssConfig.java       # 腾讯电子签配置
```

### 核心服务接口

```java
public interface ContractService {
    // 创建服务合同（派单时自动调用）
    ContractVO createServiceContract(String orderId, String staffId);

    // 获取订单关联的合同
    ContractVO getContractByOrderId(String orderId);

    // 获取签署链接
    SignUrlVO getSignUrl(String contractId, Integer approverType);

    // 检查合同是否已签署
    boolean isContractSigned(String orderId);

    // 查询合同状态
    String getContractStatus(String contractId);

    // 处理腾讯电子签回调
    void handleCallback(Object callbackData);

    // 下载合同
    String downloadContract(String contractId);

    // 获取合同列表
    List<ContractVO> getContractList(ContractQueryDTO queryDTO);
}
```

### API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/contracts | 获取合同列表 |
| GET | /api/contracts/{id} | 获取合同详情 |
| GET | /api/contracts/order/{orderId} | 获取订单关联合同 |
| GET | /api/contracts/{id}/sign-url | 获取签署链接 |
| GET | /api/contracts/{id}/status | 检查合同状态 |
| GET | /api/contracts/{id}/download | 下载合同 |
| POST | /api/contracts/callback | 腾讯电子签回调 |

### 配置 (application.yml)

```yaml
tencent:
  ess:
    secret-id: ${TENCENT_ESS_SECRET_ID}   # 从环境变量读取
    secret-key: ${TENCENT_ESS_SECRET_KEY} # 从环境变量读取
    agent-id: 1252521778
    region: ap-guangzhou
    callback-url: ${TENCENT_ESS_CALLBACK_URL}
    default-expire-days: 7
```

### 关键实现细节

#### createServiceContract 方法

1. 获取订单信息和派单的服务人员信息
2. 生成合同编号：`CONTRACT + yyyyMMddHHmmss + 4位随机数`
3. 构建签署方信息（甲方=服务商，乙方=服务人员）
4. 调用腾讯电子签API创建Flow（模拟实现，返回 `FLOW_ + 时间戳`）
5. 保存合同记录到数据库，状态为 `INITIATED`

#### getContractByOrderId / getContractList 方法

这两个方法会解析 `signers` JSON 字段，提取：
- `providerName`: 甲方（服务商）名称（type=1）
- `staffName`: 乙方（服务人员）名称（type=0）

## 前端设计

### 文件结构

```
dingfeng-work/src/
├── service/api/
│   └── contract.ts              # 合同API定义
├── views/business/
│   ├── order/
│   │   └── index.vue           # 订单页面（接单/签署合同）
│   └── contract/
│       └── index.vue          # 合同管理页面
└── locales/
    └── zh-cn.ts                # 国际化（合同管理菜单）
```

### API 函数

```typescript
// 获取订单关联的合同
fetchGetContractByOrderId(orderId: string): Promise<Api.Ess.Contract>

// 获取签署链接
fetchGetSignUrl(contractId: string, approverType: number = 0): Promise<Api.Ess.SignUrl>

// 检查合同状态
fetchGetContractStatus(contractId: string): Promise<{ status: string }>

// 下载合同
fetchDownloadContract(contractId: string): Promise<{ downloadUrl: string }>

// 获取合同列表
fetchGetContractList(params?: Api.Ess.ContractQuery): Promise<PaginatingQueryResult<Contract>>
```

### 订单页面合同相关逻辑

#### handleAssignSubmit (分配提交)

```typescript
async function handleAssignSubmit() {
  // 派单
  const { error } = await fetchDispatchOrder(currentOrderId.value, assignForm.value);
  if (error) { /* 处理错误 */ }

  // 派单成功后查询合同信息并展示
  try {
    const { data: contractData } = await fetchGetContractByOrderId(currentOrderId.value);
    if (contractData) {
      message.success(`派单成功，合同已创建：${contractData.contractNo}`);
    } else {
      message.success('派单成功');
    }
  } catch {
    message.success('派单成功');
  }
}
```

#### handleAccept (接单)

```typescript
async function handleAccept(row: Api.Order.Order) {
  // 检查合同状态
  const { data: contractData } = await fetchGetContractByOrderId(row.orderId);

  if (contractData && contractData.status !== 'SIGNED' && contractData.status !== 'COMPLETED') {
    // 合同未签署，弹出合同详情让服务人员查看/签署
    contractDetailData.value = contractData;
    contractDetailVisible.value = true;
    return;
  }
  // 合同已签署或无合同，直接接单
  await fetchAcceptOrder(row.orderId, { staffId: row.staffId });
  message.success('接单成功');
}
```

#### handleStart (开始服务)

```typescript
async function handleStart(row: Api.Order.Order) {
  // 检查合同状态
  const { data: contractData } = await fetchGetContractByOrderId(row.orderId);

  if (contractData && ['DRAFT', 'INITIATED', 'SIGNING'].includes(contractData.status)) {
    message.warning('请先完成合同签署后再开始服务');
    contractDetailData.value = contractData;
    contractDetailVisible.value = true;
    return;
  }
  // 执行开始服务
  await fetchStartOrder(row.orderId, {});
  message.success('开始服务');
}
```

### 合同详情弹窗

显示字段：
- 合同编号
- 合同名称
- 甲方（服务商）
- 乙方（服务人员）
- 合同状态（标签显示）
- 创建时间

签署按钮：仅在合同状态不是 `SIGNED` 或 `COMPLETED` 时显示

### 合同管理页面

功能：
- 合同列表展示（编号、名称、甲方、乙方、状态、创建时间）
- 搜索筛选（合同编号、状态、时间范围）
- 合同详情查看抽屉
- 合同下载

## 与其他模块的集成

### 订单模块 (OrderServiceImpl)

#### dispatchOrder - 派单时创建合同

```java
// 派单成功后自动创建电子签合同
try {
    contractService.createServiceContract(orderId, dto.getStaffId());
    log.info("电子签合同创建成功，订单号: {}", order.getOrderNo());
} catch (Exception e) {
    log.error("电子签合同创建失败，订单号: {}", order.getOrderNo(), e);
    // 派单成功不因合同创建失败而回滚
}
```

#### startService - 开始服务前检查合同

```java
// 检查合同是否已签署
if (!contractService.isContractSigned(orderId)) {
    throw new BusinessException(400, "请先完成合同签署后再开始服务");
}
```

## 腾讯电子签集成说明

当前实现为**模拟实现**，实际腾讯电子签SDK调用已配置但未真正连通。

### 模拟实现的方法

- `createTencentFlow`: 创建腾讯电子签流程（返回 `FLOW_ + 时间戳`）
- `getTencentSignUrl`: 获取签署链接（返回 `https://ess.gz.gov.cn/mock-sign?flowId=xxx`）
- `getTencentDownloadUrl`: 获取下载链接（返回 `https://ess.gz.gov.cn/mock-download?flowId=xxx`）

### 正式接入步骤

1. 确保服务器能访问 `ess.tencentcloudapi.com`
2. 配置代理或内网访问
3. 替换模拟方法为真实SDK调用
4. 配置回调地址并测试

## 待完善功能

1. **腾讯电子签真实SDK接入** - 当前为模拟实现
2. **签署回调处理** - `handleCallback` 方法需要解析真实回调数据并更新合同状态
3. **签署进度通知** - 签署状态变更时发送通知
4. **合同模板管理** - 支持自定义合同模板
5. **签署方认证** - 实名认证、人脸识别等

## 测试验证

### API 测试

```bash
# 登录获取token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | jq -r .data.accessToken)

# 派单创建合同
curl -X POST http://localhost:8080/api/orders/{orderId}/dispatch \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"providerId":"xxx","staffId":"xxx"}'

# 获取合同列表
curl http://localhost:8080/api/contracts \
  -H "Authorization: Bearer $TOKEN"

# 获取订单关联合同
curl http://localhost:8080/api/contracts/order/{orderId} \
  -H "Authorization: Bearer $TOKEN"
```

### 前端测试流程

1. 以管理员身份创建或查看一个待派单订单（CREATED状态）
2. 点击"分配"按钮，选择服务人员，确认
3. 检查是否显示"派单成功，合同已创建：XXX"
4. 以服务人员身份查看该订单（DISPATCHED状态）
5. 点击"接单"，检查是否弹出合同详情对话框
6. 点击"签署合同"，检查是否跳转到签署页面
7. 签署完成后，订单状态变为 RECEIVED
8. 点击"开始服务"，检查合同未签署时是否被拦截