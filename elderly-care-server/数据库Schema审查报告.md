# 养老服务系统数据库Schema审查报告

**审查时间**: 2026-04-16
**审查范围**: elderly-care-server/src/main/java/com/elderlycare/entity/*
**审查依据**: MySQL 8.0 + MyBatis-Plus

---

## 一、严重问题（Critical）

### 1.1 表名命名规范严重不统一 ⚠️

| 表名 | 命名风格 | 问题 |
|------|---------|------|
| `t_order` | t_前缀 | ✓ 规范 |
| `t_staff` | t_前缀 | ✓ 规范 |
| `t_elder` | t_前缀 | ✓ 规范 |
| `t_provider` | t_前缀 | ✓ 规范 |
| `t_settlement` | t_前缀 | ✓ 规范 |
| `t_service_evaluation` | t_前缀 | ✓ 规范 |
| `appointment` | 无前缀 | ✗ 不规范 |
| `service_log` | 无前缀 | ✗ 不规范 |
| `quality_check` | 无前缀 | ✗ 不规范 |

**修复建议**: 统一表名为 `t_` 前缀格式
```sql
RENAME TABLE appointment TO t_appointment;
RENAME TABLE service_log TO t_service_log;
RENAME TABLE quality_check TO t_quality_check;
```

### 1.2 Appointment表与Entity字段不一致 ⚠️

**文件位置**: `entity/appointment/Appointment.java`

**Entity中的字段**:
```java
private String orderId;      // 关联的订单ID
private String orderNo;       // 关联的订单号
```

**SQL表定义** (`init_missing_tables.sql`):
```sql
CREATE TABLE `appointment` (
  -- 缺少 order_id 字段
  -- 缺少 order_no 字段
);
```

**问题**: Appointment与Order应该是多对一关系，需要 `order_id` 外键

**修复建议**: 在appointment表中添加
```sql
ALTER TABLE appointment ADD COLUMN order_id VARCHAR(64) DEFAULT NULL COMMENT '关联订单ID';
ALTER TABLE appointment ADD COLUMN order_no VARCHAR(64) DEFAULT NULL COMMENT '关联订单号';
```

### 1.3 ElderMapper.xml SQL语法错误 ⚠️

**文件位置**: `src/main/resources/mapper/elder/ElderMapper.xml` 第63行

**错误代码**:
```xml
SELECT e.elder_id, e.elder_name, e.gender, e.birth_date ...
```
`e.gender` 是非法的SQL语法（点号应该是列名的一部分）

**修复建议**:
```xml
SELECT e.elder_id, e.elder_name, e.gender, e.birth_date ...
-- 改为
SELECT e.elder_id, e.elder_name, e.gender, e.birth_date ...
```
如果 `gender` 是列别名，应使用 `e.gender AS gender` 格式

---

## 二、中等问题（Medium）

### 2.1 ServiceLog Entity缺失关键字段

**文件位置**: `entity/servicelog/ServiceLog.java`

| 字段名 | Entity状态 | 应有状态 | 问题 |
|--------|-----------|---------|------|
| `deleted` | 缺失 | 应该有 | 逻辑删除字段缺失 |
| `auditStatus` | 缺失 | 应该有 | 审核状态字段缺失 |

**修复建议**: 添加缺失字段
```java
@TableLogic
private Integer deleted;

private String auditStatus;
```

### 2.2 QualityCheckMapper.xml为空

**文件位置**: `src/main/resources/mapper/quality/QualityCheckMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.elderlycare.mapper.quality.QualityCheckMapper">
</mapper>
```

**问题**: 空的Mapper，没有定义任何SQL语句，会导致质检功能无法正常工作

**修复建议**: 添加完整的resultMap和SQL语句

### 2.3 Provider Entity字段映射问题

**文件位置**: `entity/provider/Provider.java` + `mapper/provider/ProviderMapper.xml`

**Entity定义**:
```java
private String auditStatus;  // 审核状态
private String status;        // 启用状态
```

**Mapper映射**:
```xml
<result column="status" property="auditStatus"/>
```

**问题**: `status` 字段被映射到 `auditStatus`，而 `status` 属性本身没有映射

**修复建议**: 添加完整的字段映射
```xml
<result column="audit_status" property="auditStatus"/>
<result column="status" property="status"/>
```

### 2.4 金额字段类型混用

**检查结果**:

| 表名 | 字段 | Entity类型 | 建议类型 |
|------|------|-----------|---------|
| t_order | estimatedPrice, subsidyAmount, selfPayAmount | BigDecimal | BigDecimal ✓ |
| t_service_price | prices | BigDecimal | BigDecimal ✓ |
| t_settlement | amounts | BigDecimal | BigDecimal ✓ |
| service_log | serviceScore | BigDecimal | BigDecimal ✓ |
| quality_check | checkScore | BigDecimal | BigDecimal ✓ |

**状态**: 金额字段类型使用正确

---

## 三、轻微问题（Minor）

### 3.1 字段类型命名不一致

| Entity | 字段 | 类型 | 问题 |
|--------|------|------|------|
| Staff | gender | Integer | 1-男, 0-女 |
| Elder | gender | String | MALE/FEMALE |
| ElderFamily | gender | Integer | 1-男, 0-女 |

**问题**: 性别字段类型不统一（Integer vs String）

**建议**: 统一使用String类型并规范枚举值（MALE/FEMALE）

### 3.2 时间字段存储类型不一致

**检查结果**:

| 表名 | 字段 | SQL类型 | Entity类型 | 问题 |
|------|------|---------|-----------|------|
| appointment | appointment_time | VARCHAR(50) | String | ✗ 不规范 |
| service_log | service_date | VARCHAR(20) | String | ✗ 不规范 |
| Elder | birth_date | DATE | LocalDate | ✓ 正确 |

**修复建议**: 将VARCHAR类型改为DATETIME/DATE类型
```sql
ALTER TABLE appointment MODIFY COLUMN appointment_time DATETIME;
ALTER TABLE service_log MODIFY COLUMN service_date DATE;
```

### 3.3 appointment表中状态字段长度不足

**文件位置**: `init_missing_tables.sql` 第31行

```sql
`status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
```

**问题**: VARCHAR(20) 对于某些状态码可能不足

**枚举值分析**:
- CREATED, DISPATCHED, RECEIVED, SERVICE_STARTED, SERVICE_COMPLETED, EVALUATED, SETTLED, CANCELLED, REJECTED (订单)
- PENDING, CONFIRMED, ASSIGNED, IN_SERVICE, COMPLETED, CANCELLED, INVALID (预约)

最长的状态值约18字符，VARCHAR(20)勉强够用

**建议**: 考虑使用VARCHAR(32)以备扩展

---

## 四、字段命名规范检查

### 4.1 主键命名规范

| 表名 | 主键字段 | 命名 | 规范程度 |
|------|---------|------|---------|
| t_order | order_id | 表名_id | ✓ |
| t_staff | staff_id | 表名_id | ✓ |
| t_elder | elder_id | 表名_id | ✓ |
| t_provider | provider_id | 表名_id | ✓ |
| t_settlement | settlement_id | 表名_id | ✓ |
| appointment | appointment_id | 表名_id | ✓ |
| service_log | service_log_id | 表名_id | ✓ |
| quality_check | quality_check_id | 表名_id | ✓ |

**结论**: 主键命名统一 ✓

### 4.2 时间字段命名

| 表名 | 创建时间 | 更新时间 | 规范程度 |
|------|---------|---------|---------|
| t_order | create_time | update_time | ✓ |
| t_staff | create_time | update_time | ✓ |
| t_elder | create_time | update_time | ✓ |
| t_provider | create_time | update_time | ✓ |
| appointment | create_time | update_time | ✓ |
| service_log | create_time | update_time | ✓ |

**结论**: 时间字段命名统一 ✓

### 4.3 外键字段命名

**问题**: 大部分表缺少显式的外键约束定义

**示例**:
- `t_order.elder_id` 应引用 `t_elder.elder_id`
- `t_order.staff_id` 应引用 `t_staff.staff_id`
- `t_order.provider_id` 应引用 `t_provider.provider_id`

**建议**: 添加外键约束以保证数据完整性
```sql
ALTER TABLE t_order
ADD CONSTRAINT fk_order_elder FOREIGN KEY (elder_id) REFERENCES t_elder(elder_id),
ADD CONSTRAINT fk_order_staff FOREIGN KEY (staff_id) REFERENCES t_staff(staff_id),
ADD CONSTRAINT fk_order_provider FOREIGN KEY (provider_id) REFERENCES t_provider(provider_id);
```

---

## 五、完整数据库Schema映射表

### 5.1 t_order (订单表)

| 字段名 | MySQL类型 | Java类型 | TypeScript类型 | 说明 |
|--------|----------|----------|--------------|------|
| order_id | VARCHAR(64) | String | string | 主键 |
| order_no | VARCHAR(64) | String | string | 订单号 |
| elder_id | VARCHAR(64) | String | string | 老人ID |
| elder_name | VARCHAR(100) | String | string | 老人姓名 |
| elder_phone | VARCHAR(20) | String | string | 老人电话 |
| service_type_code | VARCHAR(50) | String | string | 服务类型编码 |
| service_type_name | VARCHAR(100) | String | string | 服务类型名称 |
| service_date | DATE | LocalDate | string | 服务日期 |
| service_time | VARCHAR(20) | String | string | 服务时间 |
| service_duration | INT | Integer | number | 服务时长(分钟) |
| service_address | VARCHAR(255) | String | string | 服务地址 |
| special_requirements | VARCHAR(500) | String | string | 特殊要求 |
| order_type | VARCHAR(20) | String | string | 订单类型 |
| order_source | VARCHAR(20) | String | string | 订单来源 |
| subsidy_type | VARCHAR(20) | String | string | 补贴类型 |
| estimated_price | DECIMAL(10,2) | BigDecimal | number | 预估价格 |
| subsidy_amount | DECIMAL(10,2) | BigDecimal | number | 补贴金额 |
| self_pay_amount | DECIMAL(10,2) | BigDecimal | number | 自付金额 |
| status | VARCHAR(20) | String | number | 订单状态 |
| provider_id | VARCHAR(64) | String | string | 服务商ID |
| staff_id | VARCHAR(64) | String | string | 服务人员ID |
| cancel_reason | VARCHAR(255) | String | string | 取消原因 |
| dispatch_time | DATETIME | LocalDateTime | string | 派单时间 |
| receive_time | DATETIME | LocalDateTime | string | 接单时间 |
| start_time | DATETIME | LocalDateTime | string | 开始时间 |
| complete_time | DATETIME | LocalDateTime | string | 完成时间 |
| create_time | DATETIME | LocalDateTime | string | 创建时间 |
| update_time | DATETIME | LocalDateTime | string | 更新时间 |
| deleted | TINYINT(1) | Integer | number | 逻辑删除 |

### 5.2 t_staff (服务人员表)

| 字段名 | MySQL类型 | Java类型 | TypeScript类型 | 说明 |
|--------|----------|----------|--------------|------|
| staff_id | VARCHAR(64) | String | string | 主键 |
| provider_id | VARCHAR(64) | String | string | 服务商ID |
| staff_no | VARCHAR(50) | String | string | 员工编号 |
| staff_name | VARCHAR(100) | String | string | 姓名 |
| gender | INT | Integer | number | 性别(0-女,1-男) |
| id_card | VARCHAR(18) | String | string | 身份证号 |
| phone | VARCHAR(20) | String | string | 手机号 |
| birth_date | DATE | LocalDate | string | 出生日期 |
| age | INT | Integer | number | 年龄 |
| nation | VARCHAR(50) | String | string | 民族 |
| education | VARCHAR(50) | String | string | 学历 |
| political_status | VARCHAR(50) | String | string | 政治面貌 |
| marital_status | VARCHAR(20) | String | string | 婚姻状况 |
| domicile_address | VARCHAR(255) | String | string | 户籍地址 |
| residence_address | VARCHAR(255) | String | string | 居住地址 |
| emergency_contact | VARCHAR(100) | String | string | 紧急联系人 |
| emergency_phone | VARCHAR(20) | String | string | 紧急联系电话 |
| service_types | VARCHAR(255) | String | string[] | 服务类型 |
| status | VARCHAR(20) | String | number | 员工状态 |
| work_status | VARCHAR(20) | String | string | 工作状态 |
| hire_date | DATE | LocalDate | string | 入职日期 |
| leave_date | DATE | LocalDate | string | 离职日期 |
| leave_reason | VARCHAR(255) | String | string | 离职原因 |
| avatar_url | VARCHAR(255) | String | string | 头像URL |
| email | VARCHAR(100) | String | string | 邮箱 |
| address | VARCHAR(255) | String | string | 地址 |
| remark | VARCHAR(500) | String | string | 备注 |
| deleted | TINYINT(1) | Integer | number | 逻辑删除 |
| create_by | BIGINT | Long | number | 创建者ID |
| create_time | DATETIME | LocalDateTime | string | 创建时间 |
| update_by | BIGINT | Long | number | 更新者ID |
| update_time | DATETIME | LocalDateTime | string | 更新时间 |

### 5.3 t_elder (老人档案表)

| 字段名 | MySQL类型 | Java类型 | TypeScript类型 | 说明 |
|--------|----------|----------|--------------|------|
| elder_id | VARCHAR(64) | String | string | 主键 |
| elder_name | VARCHAR(100) | String | string | 姓名 |
| gender | VARCHAR(10) | String | number | 性别 |
| birth_date | DATE | LocalDate | string | 出生日期 |
| age | INT | Integer | number | 年龄 |
| id_card | VARCHAR(18) | String | string | 身份证号 |
| phone | VARCHAR(20) | String | string | 联系电话 |
| ethnicity | VARCHAR(50) | String | string | 民族 |
| education | VARCHAR(50) | String | string | 教育程度 |
| marital_status | VARCHAR(20) | String | string | 婚姻状况 |
| political_status | VARCHAR(50) | String | string | 政治面貌 |
| photo_url | VARCHAR(255) | String | string | 照片URL |
| status | VARCHAR(20) | String | number | 状态 |
| register_date | DATE | LocalDate | string | 登记日期 |
| address | VARCHAR(255) | String | string | 居住地址 |
| area_id | VARCHAR(64) | String | string | 区域ID |
| care_level | VARCHAR(20) | String | number | 护理等级 |
| living_status | VARCHAR(20) | String | string | 居住状态 |
| health_status | VARCHAR(20) | String | string | 健康状态 |
| care_type | VARCHAR(20) | String | string | 养老类型 |
| emergency_contact | VARCHAR(100) | String | string | 紧急联系人 |
| emergency_phone | VARCHAR(20) | String | string | 紧急联系电话 |
| deleted | TINYINT(1) | Integer | number | 逻辑删除 |
| create_time | DATETIME | LocalDateTime | string | 创建时间 |
| update_time | DATETIME | LocalDateTime | string | 更新时间 |

### 5.4 t_provider (服务商表)

| 字段名 | MySQL类型 | Java类型 | TypeScript类型 | 说明 |
|--------|----------|----------|--------------|------|
| provider_id | VARCHAR(64) | String | string | 主键 |
| provider_name | VARCHAR(200) | String | string | 服务商名称 |
| provider_type | VARCHAR(50) | String | string | 服务商类型 |
| credit_code | VARCHAR(50) | String | string | 统一社会信用代码 |
| legal_person | VARCHAR(100) | String | string | 法定代表人 |
| contact_phone | VARCHAR(20) | String | string | 联系电话 |
| address | VARCHAR(255) | String | string | 所在地址 |
| service_areas | VARCHAR(500) | String | string[] | 服务区域 |
| description | TEXT | String | string | 简介 |
| audit_status | VARCHAR(20) | String | number | 审核状态 |
| audit_comment | VARCHAR(500) | String | string | 审核备注 |
| audit_time | DATETIME | LocalDateTime | string | 审核时间 |
| auditor_id | VARCHAR(64) | String | string | 审核人ID |
| status | VARCHAR(10) | String | number | 启用状态 |
| rating | DOUBLE | Double | number | 平均评分 |
| rating_count | INT | Integer | number | 评分次数 |
| deleted | TINYINT(1) | Integer | number | 逻辑删除 |
| create_time | DATETIME | LocalDateTime | string | 创建时间 |
| update_time | DATETIME | LocalDateTime | string | 更新时间 |

### 5.5 appointment (预约管理表)

| 字段名 | MySQL类型 | Java类型 | TypeScript类型 | 说明 |
|--------|----------|----------|--------------|------|
| appointment_id | VARCHAR(64) | String | string | 主键 |
| appointment_no | VARCHAR(64) | String | string | 预约单号 |
| elder_name | VARCHAR(100) | String | string | 老人姓名 |
| elder_id_card | VARCHAR(18) | String | string | 老人身份证号 |
| elder_phone | VARCHAR(11) | String | string | 老人手机号 |
| elder_address | VARCHAR(255) | String | string | 老人地址 |
| elder_area_id | VARCHAR(64) | String | string | 区域ID |
| elder_area_name | VARCHAR(100) | String | string | 区域名称 |
| service_type | VARCHAR(50) | String | string | 服务类型 |
| service_type_code | VARCHAR(20) | String | string | 服务类型编码 |
| service_content | VARCHAR(255) | String | string | 服务内容 |
| appointment_time | VARCHAR(50) | String | string | 预约时间 |
| service_duration | INT | Integer | number | 预计服务时长 |
| provider_id | VARCHAR(64) | String | string | 服务机构ID |
| provider_name | VARCHAR(200) | String | string | 服务机构名称 |
| provider_address | VARCHAR(255) | String | string | 服务机构地址 |
| visitor_count | INT | Integer | number | 来访人数 |
| remark | VARCHAR(500) | String | string | 备注 |
| status | VARCHAR(20) | String | string | 状态 |
| validity | VARCHAR(20) | String | string | 数据有效性 |
| cancel_reason | VARCHAR(255) | String | string | 取消原因 |
| reply_info | VARCHAR(255) | String | string | 回复信息 |
| assessment_type | VARCHAR(50) | String | string | 评估类型 |
| order_id | VARCHAR(64) | String | string | 关联订单ID ❌缺失 |
| order_no | VARCHAR(64) | String | string | 关联订单号 ❌缺失 |
| create_time | DATETIME | LocalDateTime | string | 创建时间 |
| confirm_time | DATETIME | LocalDateTime | string | 确认时间 |
| update_time | DATETIME | LocalDateTime | string | 更新时间 |
| deleted | TINYINT(1) | Integer | number | 逻辑删除 |

### 5.6 service_log (服务日志表)

| 字段名 | MySQL类型 | Java类型 | TypeScript类型 | 说明 |
|--------|----------|----------|--------------|------|
| service_log_id | VARCHAR(64) | String | string | 主键 |
| log_no | VARCHAR(64) | String | string | 服务日志编号 |
| order_id | VARCHAR(64) | String | string | 订单ID |
| order_no | VARCHAR(64) | String | string | 订单号 |
| elder_id | VARCHAR(64) | String | string | 老人ID |
| elder_name | VARCHAR(100) | String | string | 老人姓名 |
| elder_phone | VARCHAR(11) | String | string | 老人手机号 |
| elder_address | VARCHAR(255) | String | string | 老人地址 |
| staff_id | VARCHAR(64) | String | string | 服务人员ID |
| staff_name | VARCHAR(100) | String | string | 服务人员姓名 |
| staff_phone | VARCHAR(11) | String | string | 服务人员手机号 |
| provider_id | VARCHAR(64) | String | string | 服务商ID |
| provider_name | VARCHAR(200) | String | string | 服务商名称 |
| service_type_code | VARCHAR(20) | String | string | 服务类型编码 |
| service_type_name | VARCHAR(100) | String | string | 服务类型名称 |
| service_date | VARCHAR(20) | String | string | 服务日期 |
| service_start_time | DATETIME | LocalDateTime | string | 服务开始时间 |
| service_end_time | DATETIME | LocalDateTime | string | 服务结束时间 |
| service_duration | INT | Integer | number | 服务时长 |
| service_status | VARCHAR(20) | String | string | 服务状态 |
| actual_duration | INT | Integer | number | 实际服务时长 |
| service_score | DECIMAL(5,2) | BigDecimal | number | 服务评分 |
| service_comment | VARCHAR(500) | String | string | 服务评价 |
| service_photos | MEDIUMTEXT | String | string | 服务照片 |
| elder_signature | VARCHAR(255) | String | string | 老人签名 |
| anomaly_type | VARCHAR(50) | String | string | 异常类型 |
| anomaly_desc | VARCHAR(500) | String | string | 异常描述 |
| anomaly_photos | TEXT | String | string | 异常照片 |
| anomaly_status | VARCHAR(20) | String | string | 异常处理状态 |
| review_remarks | TEXT | String | string | 审核备注 |
| audit_status | VARCHAR(20) | String | string | 审核状态 ❌Entity缺失 |
| deleted | TINYINT(1) | Integer | number | 逻辑删除 ❌Entity缺失 |
| create_time | DATETIME | LocalDateTime | string | 创建时间 |
| update_time | DATETIME | LocalDateTime | string | 更新时间 |

### 5.7 quality_check (服务质检表)

| 字段名 | MySQL类型 | Java类型 | TypeScript类型 | 说明 |
|--------|----------|----------|--------------|------|
| quality_check_id | VARCHAR(64) | String | string | 主键 |
| check_no | VARCHAR(64) | String | string | 质检编号 |
| order_id | VARCHAR(64) | String | string | 订单ID |
| order_no | VARCHAR(64) | String | string | 订单号 |
| service_log_id | VARCHAR(64) | String | string | 服务日志ID |
| service_category | VARCHAR(50) | String | string | 服务类别 |
| provider_id | VARCHAR(64) | String | string | 服务商ID |
| provider_name | VARCHAR(200) | String | string | 服务商名称 |
| staff_id | VARCHAR(64) | String | string | 服务人员ID |
| staff_name | VARCHAR(100) | String | string | 服务人员姓名 |
| check_type | VARCHAR(20) | String | string | 质检类型 |
| check_method | VARCHAR(20) | String | string | 质检方式 |
| check_score | DECIMAL(5,2) | BigDecimal | number | 综合评分 |
| check_result | VARCHAR(20) | String | string | 质检结果 |
| check_photos | TEXT | String | string[] | 质检照片 |
| check_remark | VARCHAR(500) | String | string | 质检备注 |
| check_time | DATETIME | LocalDateTime | string | 质检时间 |
| checker_id | VARCHAR(64) | String | string | 质检员ID |
| checker_name | VARCHAR(100) | String | string | 质检员姓名 |
| need_rectify | TINYINT(1) | Boolean | boolean | 是否需要整改 |
| rectify_notice | VARCHAR(255) | String | string | 整改通知 |
| rectify_deadline | DATETIME | LocalDateTime | string | 整改期限 |
| rectify_status | VARCHAR(20) | String | string | 整改状态 |
| rectify_photos | TEXT | String | string[] | 整改照片 |
| rectify_remark | VARCHAR(500) | String | string | 整改说明 |
| recheck_time | DATETIME | LocalDateTime | string | 复检时间 |
| recheck_result | VARCHAR(20) | String | string | 复检结果 |
| deleted | TINYINT(1) | Integer | number | 逻辑删除 |
| create_time | DATETIME | LocalDateTime | string | 创建时间 |
| update_time | DATETIME | LocalDateTime | string | 更新时间 |

---

## 六、问题汇总与修复优先级

### 问题汇总表

| 序号 | 问题 | 严重程度 | 影响范围 | 修复优先级 |
|------|------|---------|---------|-----------|
| 1 | 表名命名不统一 | Critical | 全系统 | P0 |
| 2 | Appointment表缺少order_id/order_no | Critical | 预约-订单关联 | P0 |
| 3 | ElderMapper.xml SQL语法错误 | Critical | 老人列表查询 | P0 |
| 4 | ServiceLog Entity缺少deleted字段 | High | 逻辑删除 | P1 |
| 5 | ServiceLog Entity缺少auditStatus字段 | High | 审核功能 | P1 |
| 6 | QualityCheckMapper.xml为空 | High | 质检功能 | P1 |
| 7 | Provider Mapper字段映射错误 | Medium | 服务商管理 | P2 |
| 8 | appointment_time使用VARCHAR | Low | 性能/扩展性 | P2 |
| 9 | service_date使用VARCHAR | Low | 性能/扩展性 | P2 |
| 10 | 性别字段类型不统一 | Low | 数据一致性 | P3 |

### 修复建议执行顺序

1. **立即修复 (P0)**:
   - 修复 ElderMapper.xml SQL语法错误
   - 添加 appointment 表的 order_id 和 order_no 字段

2. **尽快修复 (P1)**:
   - 统一表名命名规范（重命名表）
   - 补充 ServiceLog Entity 缺失字段
   - 完善 QualityCheckMapper.xml

3. **计划修复 (P2)**:
   - 修正 ProviderMapper.xml 字段映射
   - 修改 appointment_time 和 service_date 为 DATETIME 类型

4. **优化项 (P3)**:
   - 统一性别字段类型

---

## 七、附录：SQL修复脚本

### 7.1 表名重命名脚本
```sql
-- 表名重命名（统一为 t_ 前缀）
RENAME TABLE appointment TO t_appointment;
RENAME TABLE service_log TO t_service_log;
RENAME TABLE quality_check TO t_quality_check;
```

### 7.2 修复appointment表脚本
```sql
-- 添加缺失的订单关联字段
ALTER TABLE t_appointment
ADD COLUMN order_id VARCHAR(64) DEFAULT NULL COMMENT '关联订单ID' AFTER assessment_type,
ADD COLUMN order_no VARCHAR(64) DEFAULT NULL COMMENT '关联订单号' AFTER order_id;

-- 修改预约时间为DATETIME类型
ALTER TABLE t_appointment
MODIFY COLUMN appointment_time DATETIME DEFAULT NULL COMMENT '预约时间';

-- 添加逻辑删除字段（如果缺失）
ALTER TABLE t_appointment
ADD COLUMN deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除' AFTER update_time;
```

### 7.3 修复service_log表脚本
```sql
-- 修改服务日期为DATE类型
ALTER TABLE t_service_log
MODIFY COLUMN service_date DATE DEFAULT NULL COMMENT '服务日期';

-- 添加审核状态字段
ALTER TABLE t_service_log
ADD COLUMN audit_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审核状态' AFTER review_remarks;
```

### 7.4 添加外键约束脚本
```sql
-- t_order 外键约束
ALTER TABLE t_order
ADD CONSTRAINT fk_order_elder FOREIGN KEY (elder_id) REFERENCES t_elder(elder_id),
ADD CONSTRAINT fk_order_staff FOREIGN KEY (staff_id) REFERENCES t_staff(staff_id),
ADD CONSTRAINT fk_order_provider FOREIGN KEY (provider_id) REFERENCES t_provider(provider_id);

-- service_log 外键约束
ALTER TABLE t_service_log
ADD CONSTRAINT fk_servicelog_order FOREIGN KEY (order_id) REFERENCES t_order(order_id),
ADD CONSTRAINT fk_servicelog_elder FOREIGN KEY (elder_id) REFERENCES t_elder(elder_id),
ADD CONSTRAINT fk_servicelog_staff FOREIGN KEY (staff_id) REFERENCES t_staff(staff_id),
ADD CONSTRAINT fk_servicelog_provider FOREIGN KEY (provider_id) REFERENCES t_provider(provider_id);

-- quality_check 外键约束
ALTER TABLE t_quality_check
ADD CONSTRAINT fk_qualitycheck_order FOREIGN KEY (order_id) REFERENCES t_order(order_id),
ADD CONSTRAINT fk_qualitycheck_service_log FOREIGN KEY (service_log_id) REFERENCES t_service_log(service_log_id);
```

---

**报告生成时间**: 2026-04-16 18:00
**审查人**: database-reviewer
