# 自动生成服务商账号功能设计文档

## 1. 需求概述

**功能目标**：每新增一个服务商，系统自动创建对应的服务商管理员账号。

**用户名生成规则**：
- 固定 5 个字符
- 来源：服务商名称的拼音首字母缩写
- 示例：服务商名"延安宁峰养老服务有限公司" → 拼音首字母"YNJF" + 序号"1" → `YNJF1`（取前5位不够补序号）

**默认密码**：固定初始密码 `Provider@123`（登录后强制修改）

**关联角色**：R002（服务商管理员）

---

## 2. 业务流程

```
前端：服务商新增表单
        ↓
后端：ProviderController.createProvider()
        ↓
Service 层：保存 Provider 实体
        ↓
自动触发：创建对应的 Provider Admin 用户
        ↓
  生成用户名（5位拼音首字母+序号）
  设置默认密码（BCrypt加密）
  关联角色 R_PROVIDER
  关联 providerId
        ↓
返回：Provider 信息 + 自动生成的账号密码
```

---

## 3. 技术方案

### 3.1 用户名生成算法

```java
/**
 * 输入：服务商名称，如"延安宁峰养老服务有限公司"
 * 步骤：
 * 1. 提取所有汉字的首字母，拼接成拼音缩写
 * 2. 转大写
 * 3. 如果不足5位，用序号补足；如果超过5位，截断前5位
 *
 * 示例：
 * "延安宁峰养老服务有限公司" → Y A N F Y L D L F W Y G S → "YANFL" (截断)
 * "阳光养老" → Y G Y L → "YGYL1" (补序号)
 * "阳光养老服务商" → Y G Y L S F W → "YGLSF" (截断)
 *
 * 序号规则：最大序号99，如 YGYL1, YGYL2, ... YGYL99
 */
```

### 3.2 序号分配策略

- 查询当前最大序号：`SELECT MAX(seq) FROM t_user WHERE username LIKE 'PREFIX%'`
- 序号从 1 开始，最大 99
- 如果达到 99 仍然冲突（理论上极小概率），返回错误

### 3.3 密码加密

使用 Spring Security BCrypt：
```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String encodedPassword = encoder.encode("Provider@123");
```

---

## 4. 数据库设计

### 4.1 t_user 表（现有结构，无需修改）

用户类型 user_type = `PROVIDER`

| 字段 | 说明 |
|------|------|
| user_id | 主键 |
| username | 自动生成的5位账号 |
| password | BCrypt加密的初始密码 |
| real_name | 服务商名称（或"服务商管理员"） |
| phone | 服务商联系电话（从 Provider 复制） |
| user_type | PROVIDER |
| provider_id | 关联的服务商 ID |
| status | NORMAL |
| create_time | 创建时间 |

### 4.2 t_user_role 表（现有结构，无需修改）

自动插入：
```sql
INSERT INTO t_user_role (user_role_id, user_id, role_id, create_time)
VALUES ('UR_xxx', '新用户ID', 'R002', NOW());
```

---

## 5. 接口设计

### 5.1 修改 ProviderController.createProvider()

**现有接口**：`POST /api/providers`

**修改点**：在 `providerService.createProvider()` 成功返回后，自动创建用户。

**返回结构扩展**：
```json
{
  "code": 200,
  "data": {
    "provider": { /* 原有服务商信息 */ },
    "autoCreatedAccount": {
      "username": "YNJF1",
      "password": "Provider@123",
      "role": "服务商管理员"
    }
  }
}
```

---

## 6. 实现步骤

### Step 1: 修改后端

#### 6.1 创建 ChineseToPinyin 工具类
**路径**：`common/ChineseToPinyin.java`

使用 pinyin4j 库将汉字转换为拼音首字母。

#### 6.2 创建 ProviderAccountService
**路径**：`service/provider/ProviderAccountService.java`

核心方法：
```java
public User createProviderAccount(Provider provider) {
    // 1. 生成5位用户名
    // 2. 查询序号并分配
    // 3. BCrypt加密默认密码
    // 4. 插入 t_user
    // 5. 插入 t_user_role
    // 6. 返回创建的用户
}
```

#### 6.3 修改 ProviderServiceImpl.createProvider()

在保存 Provider 后调用：
```java
@Autowired
private ProviderAccountService providerAccountService;

public ProviderVO createProvider(ProviderCreateDTO dto) {
    Provider provider = convertToEntity(dto);
    providerMapper.insert(provider);
    // 自动创建服务商管理员账号
    User adminUser = providerAccountService.createProviderAccount(provider);
    return convertToVO(provider);
}
```

### Step 2: 修改 ProviderController.createProvider() 返回结构

将返回值从 `Result<ProviderVO>` 改为包含账号信息的新结构：
```java
public Result<ProviderCreateVO> createProvider(@RequestBody ProviderCreateDTO dto) {
    ProviderVO provider = providerService.createProvider(dto);
    User adminUser = providerAccountService.getByProviderId(provider.getProviderId());
    return Result.success(new ProviderCreateVO(provider, adminUser));
}
```

### Step 3: 添加 pinyin4j 依赖

**pom.xml**：
```xml
<dependency>
    <groupId>com.belerweb</groupId>
    <artifactId>pinyin4j</artifactId>
    <version>2.5.1</version>
</dependency>
```

---

## 7. 异常处理

| 场景 | 处理方式 |
|------|---------|
| 拼音转换失败 | fallback 到服务商的汉语拼音 |
| 序号用尽（99个冲突） | 抛出业务异常"账号生成失败，请联系管理员" |
| 密码加密失败 | 抛出业务异常 |
| 服务商创建失败 | 事务回滚，不创建账号 |

---

## 8. 前端适配

### 8.1 服务商新增表单

在表单提交成功后，弹窗展示自动生成的账号密码：
```
✅ 服务商创建成功！

自动生成的管理员账号：
  用户名：YNJF1
  密码：Provider@123

（请提醒管理员首次登录后修改密码）
```

### 8.2 API 修改

`POST /api/providers` 返回值结构变更：
- 原有字段全部保留
- 新增 `autoCreatedAccount` 对象（包含 username, password）

---

## 9. 涉及文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `ProviderServiceImpl.java` | 修改 | 保存后调用创建账号 |
| `ProviderAccountService.java` | 新建 | 账号生成核心逻辑 |
| `ChineseToPinyin.java` | 新建 | 汉字转拼音首字母工具 |
| `ProviderController.java` | 修改 | 返回值增加账号信息 |
| `ProviderCreateVO.java` | 新建 | 包含账号信息的返回结构 |
| `ProviderVO.java` | 修改 | 可能需要增加 phone 字段复制 |
| `pom.xml` | 修改 | 添加 pinyin4j 依赖 |

---

## 10. 测试用例

| 场景 | 输入 | 预期输出 |
|------|------|---------|
| 正常创建 | 服务商名"阳光养老服务" | 账号 YGLF1，密码 Provider@123 |
| 名称重复前缀 | 两个"阳光养老" | 第一个 YGLF1，第二个 YGLF2 |
| 5位截断 | "北京智慧养老有限公司" | YZHYL（截断） |
| 单字服务商会"养老" | YL | YL001（补序号） |
| 序号达到99 | 已有99个 YGLF1-99 | 返回错误 |
