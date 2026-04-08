-- ============================================
-- 智慧居家养老服务管理平台 - 数据库初始化脚本
-- 数据库名: elderly_care
-- 版本: V1.0
-- ============================================

CREATE DATABASE IF NOT EXISTS elderly_care DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE elderly_care;

-- ============================================
-- 1. 用户服务相关表
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    user_id VARCHAR(32) PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    gender VARCHAR(10) COMMENT '性别',
    avatar VARCHAR(500) COMMENT '头像URL',
    user_type VARCHAR(20) COMMENT '用户类型 SYSTEM/ADMIN/PROVIDER/STAFF',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态 NORMAL/DISABLED/LOCKED',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    login_fail_count INT DEFAULT 0 COMMENT '登录失败次数',
    lock_time DATETIME COMMENT '锁定时间',
    tenant_id VARCHAR(32) COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS t_role (
    role_id VARCHAR(32) PRIMARY KEY COMMENT '角色ID',
    role_code VARCHAR(50) UNIQUE NOT NULL COMMENT '角色编码',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_desc VARCHAR(500) COMMENT '角色描述',
    role_type VARCHAR(20) COMMENT '角色类型',
    data_scope VARCHAR(20) DEFAULT 'ALL' COMMENT '数据范围 ALL/DEPT/DEPT_CHILDREN/SELF',
    sort_order INT COMMENT '排序',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态',
    tenant_id VARCHAR(32) COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS t_permission (
    permission_id VARCHAR(32) PRIMARY KEY COMMENT '权限ID',
    permission_code VARCHAR(100) UNIQUE NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_type VARCHAR(20) COMMENT '权限类型 CATALOG/MENU/BUTTON',
    parent_id VARCHAR(32) DEFAULT '0' COMMENT '父权限ID',
    permission_url VARCHAR(500) COMMENT '权限URL',
    permission_method VARCHAR(10) COMMENT '请求方法',
    sort_order INT COMMENT '排序',
    icon VARCHAR(200) COMMENT '图标',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS t_user_role (
    user_role_id VARCHAR(32) PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    role_id VARCHAR(32) NOT NULL COMMENT '角色ID',
    area_id VARCHAR(32) COMMENT '区域ID',
    tenant_id VARCHAR(32) COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS t_role_permission (
    role_permission_id VARCHAR(32) PRIMARY KEY,
    role_id VARCHAR(32) NOT NULL COMMENT '角色ID',
    permission_id VARCHAR(32) NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role (role_id),
    INDEX idx_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS t_login_log (
    login_log_id VARCHAR(32) PRIMARY KEY,
    user_id VARCHAR(32) COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    login_time DATETIME COMMENT '登录时间',
    login_ip VARCHAR(50) COMMENT '登录IP',
    login_location VARCHAR(200) COMMENT '登录地点',
    login_device VARCHAR(200) COMMENT '登录设备',
    login_status VARCHAR(20) COMMENT '登录状态 SUCCESS/FAIL',
    fail_reason VARCHAR(500) COMMENT '失败原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ============================================
-- 2. 服务商服务相关表
-- ============================================

-- 服务商信息表
CREATE TABLE IF NOT EXISTS t_provider (
    provider_id VARCHAR(32) PRIMARY KEY COMMENT '服务商ID',
    provider_no VARCHAR(50) UNIQUE COMMENT '服务商编号',
    provider_name VARCHAR(200) NOT NULL COMMENT '企业名称',
    provider_type VARCHAR(20) COMMENT '服务商类型 GOVERNMENT/PRIVATE/NON_PROFIT',
    credit_code VARCHAR(18) COMMENT '统一社会信用代码',
    legal_person VARCHAR(50) COMMENT '法定代表人',
    legal_person_phone VARCHAR(20) COMMENT '法定代表人电话',
    register_address VARCHAR(500) COMMENT '注册地址',
    business_address VARCHAR(500) COMMENT '经营地址',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    service_scope TEXT COMMENT '服务范围',
    staff_size INT COMMENT '人员规模',
    credit_level VARCHAR(10) COMMENT '信用等级 AAA/AA/A/B/C',
    credit_score DECIMAL(5,2) DEFAULT 100.00 COMMENT '信用得分',
    business_license VARCHAR(500) COMMENT '营业执照',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态 PENDING/APPROVED/REJECTED/SUSPENDED',
    audit_comment VARCHAR(500) COMMENT '审核备注',
    audit_time DATETIME COMMENT '审核时间',
    auditor_id VARCHAR(32) COMMENT '审核人ID',
    tenant_id VARCHAR(32) COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_provider_no (provider_no),
    INDEX idx_credit_code (credit_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务商信息表';

-- 服务商资质表
CREATE TABLE IF NOT EXISTS t_provider_qualification (
    qualification_id VARCHAR(32) PRIMARY KEY,
    provider_id VARCHAR(32) NOT NULL COMMENT '服务商ID',
    qualification_type VARCHAR(50) COMMENT '资质类型',
    qualification_name VARCHAR(200) COMMENT '资质名称',
    qualification_number VARCHAR(100) COMMENT '资质编号',
    issue_date DATE COMMENT '发证日期',
    expiry_date DATE COMMENT '有效期至',
    issue_organization VARCHAR(200) COMMENT '发证机构',
    attachment_url VARCHAR(500) COMMENT '附件URL',
    status VARCHAR(20) DEFAULT 'VALID' COMMENT '状态 VALID/EXPIRED',
    audit_status VARCHAR(20) DEFAULT 'APPROVED' COMMENT '审核状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_provider (provider_id),
    INDEX idx_expiry (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务商资质表';

-- 服务商服务类型表
CREATE TABLE IF NOT EXISTS t_provider_service_type (
    provider_service_id VARCHAR(32) PRIMARY KEY,
    provider_id VARCHAR(32) NOT NULL,
    service_type_code VARCHAR(20) NOT NULL,
    service_type_name VARCHAR(100) NOT NULL,
    service_price DECIMAL(10,2) COMMENT '服务价格',
    subsidy_price DECIMAL(10,2) COMMENT '补贴价格',
    service_area VARCHAR(500) COMMENT '服务区域',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_provider (provider_id),
    INDEX idx_service_type (service_type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务商服务类型表';

-- ============================================
-- 3. 服务人员服务相关表
-- ============================================

-- 服务人员信息表
CREATE TABLE IF NOT EXISTS t_staff (
    staff_id VARCHAR(32) PRIMARY KEY COMMENT '人员ID',
    staff_no VARCHAR(50) UNIQUE COMMENT '人员编号',
    provider_id VARCHAR(32) NOT NULL COMMENT '所属服务商ID',
    staff_name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender VARCHAR(10) COMMENT '性别 MALE/FEMALE',
    birth_date DATE COMMENT '出生日期',
    age INT COMMENT '年龄',
    id_card VARCHAR(18) COMMENT '身份证号',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    address VARCHAR(500) COMMENT '家庭住址',
    emergency_contact VARCHAR(50) COMMENT '紧急联系人',
    emergency_phone VARCHAR(20) COMMENT '紧急联系人电话',
    education VARCHAR(20) COMMENT '学历 PRIMARY/JUNIOR/SENIOR/COLLEGE/BACHELOR/MASTER',
    photo_url VARCHAR(500) COMMENT '个人照片',
    status VARCHAR(20) DEFAULT 'ON_JOB' COMMENT '状态 ON_JOB/LEAVE/QUIT',
    work_status VARCHAR(20) DEFAULT 'IDLE' COMMENT '工作状态 IDLE/BUSY/LEAVE',
    hire_date DATE COMMENT '入职日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_staff_no (staff_no),
    INDEX idx_phone (phone),
    INDEX idx_id_card (id_card),
    INDEX idx_provider (provider_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务人员信息表';

-- 服务人员资质表
CREATE TABLE IF NOT EXISTS t_staff_qualification (
    qualification_id VARCHAR(32) PRIMARY KEY,
    staff_id VARCHAR(32) NOT NULL COMMENT '人员ID',
    qualification_type VARCHAR(50) COMMENT '资质类型',
    qualification_name VARCHAR(200) COMMENT '证书名称',
    qualification_number VARCHAR(100) COMMENT '证书编号',
    issue_date DATE COMMENT '发证日期',
    expiry_date DATE COMMENT '有效期至',
    issue_organization VARCHAR(200) COMMENT '发证机构',
    attachment_url VARCHAR(500) COMMENT '证书扫描件',
    status VARCHAR(20) DEFAULT 'VALID' COMMENT '状态',
    audit_status VARCHAR(20) DEFAULT 'APPROVED' COMMENT '审核状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_staff (staff_id),
    INDEX idx_expiry (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务人员资质表';

-- 服务人员排班表
CREATE TABLE IF NOT EXISTS t_staff_schedule (
    schedule_id VARCHAR(32) PRIMARY KEY,
    staff_id VARCHAR(32) NOT NULL,
    schedule_date DATE NOT NULL COMMENT '排班日期',
    shift_type VARCHAR(20) COMMENT '班次类型 MORNING/AFTERNOON/EVENING/FULL_DAY',
    start_time VARCHAR(10) COMMENT '开始时间',
    end_time VARCHAR(10) COMMENT '结束时间',
    work_area VARCHAR(200) COMMENT '服务区域',
    service_types TEXT COMMENT '服务类型',
    schedule_status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '排班状态 NORMAL/LEAVE/ADJUST',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_staff (staff_id),
    INDEX idx_date (schedule_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务人员排班表';

-- 服务人员工作记录表
CREATE TABLE IF NOT EXISTS t_staff_work_record (
    work_record_id VARCHAR(32) PRIMARY KEY,
    staff_id VARCHAR(32) NOT NULL,
    order_id VARCHAR(32) COMMENT '订单ID',
    elder_id VARCHAR(32) COMMENT '老人ID',
    service_date DATE COMMENT '服务日期',
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签退时间',
    service_duration INT COMMENT '实际服务时长(分钟)',
    check_in_location VARCHAR(500) COMMENT '签到位置',
    check_out_location VARCHAR(500) COMMENT '签退位置',
    service_photos TEXT COMMENT '服务照片',
    service_log TEXT COMMENT '服务日志',
    abnormal_situation TEXT COMMENT '异常情况',
    elder_signature VARCHAR(500) COMMENT '老人签名',
    service_status VARCHAR(20) COMMENT '服务状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_staff (staff_id),
    INDEX idx_order (order_id),
    INDEX idx_date (service_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务人员工作记录表';

-- ============================================
-- 4. 老人服务相关表
-- ============================================

-- 老人基本信息表
CREATE TABLE IF NOT EXISTS t_elder (
    elder_id VARCHAR(32) PRIMARY KEY COMMENT '老人ID',
    elder_no VARCHAR(50) UNIQUE COMMENT '老人编号',
    elder_name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender VARCHAR(10) COMMENT '性别 MALE/FEMALE',
    birth_date DATE COMMENT '出生日期',
    age INT COMMENT '年龄',
    id_card VARCHAR(18) COMMENT '身份证号',
    phone VARCHAR(20) COMMENT '联系电话',
    ethnicity VARCHAR(20) DEFAULT '汉族' COMMENT '民族',
    education VARCHAR(20) COMMENT '学历',
    marital_status VARCHAR(20) COMMENT '婚姻状况',
    political_status VARCHAR(20) COMMENT '政治面貌',
    photo_url VARCHAR(500) COMMENT '个人照片',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/TRANSFERRED/DECEASED',
    register_date DATE COMMENT '建档日期',
    address VARCHAR(500) COMMENT '居住地址',
    area_id VARCHAR(32) COMMENT '所属区域ID',
    care_level VARCHAR(20) DEFAULT 'NORMAL' COMMENT '护理级别 NORMAL/MILD/MODERATE/SEVERE',
    living_status VARCHAR(20) COMMENT '居住状况 ALONE/WITH_FAMILY/NURSING_HOME',
    health_status VARCHAR(20) DEFAULT 'FAIR' COMMENT '健康状况 GOOD/FAIR/POOR',
    tenant_id VARCHAR(32) COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_elder_no (elder_no),
    INDEX idx_name (elder_name),
    INDEX idx_id_card (id_card),
    INDEX idx_phone (phone),
    INDEX idx_area (area_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人基本信息表';

-- 老人家庭信息表
CREATE TABLE IF NOT EXISTS t_elder_family (
    family_id VARCHAR(32) PRIMARY KEY,
    elder_id VARCHAR(32) NOT NULL COMMENT '老人ID',
    family_type VARCHAR(20) COMMENT '家庭关系类型',
    family_name VARCHAR(50) COMMENT '家属姓名',
    family_phone VARCHAR(20) COMMENT '家属电话',
    family_address VARCHAR(500) COMMENT '家属地址',
    is_caretaker TINYINT DEFAULT 0 COMMENT '是否主要照顾人',
    is_emergency_contact TINYINT DEFAULT 0 COMMENT '是否紧急联系人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_elder (elder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人家庭信息表';

-- 老人健康信息表
CREATE TABLE IF NOT EXISTS t_elder_health (
    health_id VARCHAR(32) PRIMARY KEY,
    elder_id VARCHAR(32) NOT NULL COMMENT '老人ID',
    blood_type VARCHAR(10) COMMENT '血型',
    height DECIMAL(5,2) COMMENT '身高',
    weight DECIMAL(5,2) COMMENT '体重',
    medical_history TEXT COMMENT '病史记录',
    medication_list TEXT COMMENT '用药清单',
    allergy_history TEXT COMMENT '过敏史',
    chronic_diseases TEXT COMMENT '慢性病',
    disabilities TEXT COMMENT '残疾情况',
    self_care_ability VARCHAR(20) COMMENT '自理能力',
    care_level VARCHAR(20) COMMENT '照护等级',
    last_checkup_date DATE COMMENT '上次体检日期',
    checkup_report_url VARCHAR(500) COMMENT '体检报告',
    health_status VARCHAR(50) COMMENT '健康状况',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_elder (elder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人健康信息表';

-- 老人服务需求表
CREATE TABLE IF NOT EXISTS t_elder_demand (
    demand_id VARCHAR(32) PRIMARY KEY,
    elder_id VARCHAR(32) NOT NULL COMMENT '老人ID',
    service_type_code VARCHAR(20) COMMENT '服务类型编码',
    service_type_name VARCHAR(100) COMMENT '服务类型名称',
    demand_priority VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '优先级 HIGH/MEDIUM/LOW',
    service_frequency VARCHAR(20) COMMENT '服务频次 ONCE/WEEKLY/MONTHLY',
    preferred_time VARCHAR(50) COMMENT '服务时间偏好',
    estimated_duration INT COMMENT '预计服务时长',
    special_requirements TEXT COMMENT '特殊要求',
    demand_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '需求状态 ACTIVE/INACTIVE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_elder (elder_id),
    INDEX idx_service_type (service_type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人服务需求表';

-- 老人补贴表
CREATE TABLE IF NOT EXISTS t_elder_subsidy (
    subsidy_id VARCHAR(32) PRIMARY KEY,
    elder_id VARCHAR(32) NOT NULL COMMENT '老人ID',
    subsidy_type VARCHAR(50) COMMENT '补贴类型 GOVERNMENT/SELF_PAY/MIXED',
    subsidy_name VARCHAR(100) COMMENT '补贴名称',
    subsidy_standard DECIMAL(10,2) COMMENT '补贴标准',
    subsidy_quota DECIMAL(10,2) COMMENT '补贴额度',
    subsidy_balance DECIMAL(10,2) COMMENT '补贴余额',
    subsidy_start_date DATE COMMENT '补贴开始日期',
    subsidy_end_date DATE COMMENT '补贴结束日期',
    subsidy_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '补贴状态 ACTIVE/SUSPENDED/EXPIRED',
    approval_time DATETIME COMMENT '审批通过时间',
    approver_id VARCHAR(32) COMMENT '审批人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_elder (elder_id),
    INDEX idx_subsidy_type (subsidy_type),
    INDEX idx_status (subsidy_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人补贴表';

-- ============================================
-- 5. 订单服务相关表
-- ============================================

-- 订单表（按月分表，使用分区）
CREATE TABLE IF NOT EXISTS t_order (
    order_id VARCHAR(32) PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(50) UNIQUE NOT NULL COMMENT '订单编号',
    elder_id VARCHAR(32) NOT NULL COMMENT '老人ID',
    elder_name VARCHAR(50) COMMENT '老人姓名',
    elder_phone VARCHAR(20) COMMENT '老人电话',
    elder_address VARCHAR(500) COMMENT '老人地址',
    service_type_code VARCHAR(20) NOT NULL COMMENT '服务类型编码',
    service_type_name VARCHAR(100) COMMENT '服务类型名称',
    service_date DATE COMMENT '服务日期',
    service_time VARCHAR(10) COMMENT '服务时间',
    service_duration INT COMMENT '预计服务时长(分钟)',
    service_address VARCHAR(500) COMMENT '服务地址',
    longitude VARCHAR(20) COMMENT '经度',
    latitude VARCHAR(20) COMMENT '纬度',
    special_requirements TEXT COMMENT '特殊要求',
    order_type VARCHAR(20) DEFAULT 'NORMAL' COMMENT '订单类型 NORMAL/URGENT',
    order_source VARCHAR(20) COMMENT '订单来源 COMMUNITY/ELDER/FAMILY',
    subsidy_type VARCHAR(20) COMMENT '补贴类型',
    estimated_price DECIMAL(10,2) COMMENT '预计价格',
    subsidy_amount DECIMAL(10,2) COMMENT '补贴金额',
    self_pay_amount DECIMAL(10,2) COMMENT '自付金额',
    actual_price DECIMAL(10,2) COMMENT '实际价格',
    status VARCHAR(20) DEFAULT 'CREATED' COMMENT '订单状态',
    provider_id VARCHAR(32) COMMENT '服务商ID',
    staff_id VARCHAR(32) COMMENT '服务人员ID',
    community_id VARCHAR(32) COMMENT '社区ID',
    tenant_id VARCHAR(32) COMMENT '租户ID',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    cancel_time DATETIME COMMENT '取消时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_order_no (order_no),
    INDEX idx_elder (elder_id),
    INDEX idx_status (status),
    INDEX idx_service_date (service_date),
    INDEX idx_provider (provider_id),
    INDEX idx_staff (staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单派单记录表
CREATE TABLE IF NOT EXISTS t_order_dispatch (
    dispatch_id VARCHAR(32) PRIMARY KEY,
    order_id VARCHAR(32) NOT NULL COMMENT '订单ID',
    provider_id VARCHAR(32) NOT NULL COMMENT '服务商ID',
    staff_id VARCHAR(32) COMMENT '服务人员ID',
    dispatch_time DATETIME COMMENT '派单时间',
    dispatcher_id VARCHAR(32) COMMENT '派单人ID',
    dispatch_type VARCHAR(20) COMMENT '派单类型 MANUAL/AUTO/GRAB',
    dispatch_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '派单状态 PENDING/RECEIVED/REJECTED',
    receive_time DATETIME COMMENT '接单时间',
    reject_reason VARCHAR(500) COMMENT '拒绝理由',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order (order_id),
    INDEX idx_provider (provider_id),
    INDEX idx_staff (staff_id),
    INDEX idx_status (dispatch_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单派单记录表';

-- 服务记录表
CREATE TABLE IF NOT EXISTS t_service_record (
    service_record_id VARCHAR(32) PRIMARY KEY,
    order_id VARCHAR(32) NOT NULL,
    staff_id VARCHAR(32) NOT NULL,
    elder_id VARCHAR(32) COMMENT '老人ID',
    service_date DATE COMMENT '服务日期',
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签退时间',
    service_duration INT COMMENT '实际服务时长',
    check_in_location VARCHAR(500) COMMENT '签到位置',
    check_out_location VARCHAR(500) COMMENT '签退位置',
    service_photos TEXT COMMENT '服务照片',
    service_log TEXT COMMENT '服务日志',
    elder_feedback TEXT COMMENT '老人反馈',
    service_status VARCHAR(20) COMMENT '服务状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_order (order_id),
    INDEX idx_staff (staff_id),
    INDEX idx_date (service_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务记录表';

-- ============================================
-- 6. 财务服务相关表
-- ============================================

-- 服务定价表
CREATE TABLE IF NOT EXISTS t_service_price (
    price_id VARCHAR(32) PRIMARY KEY COMMENT '定价ID',
    service_type_code VARCHAR(20) NOT NULL COMMENT '服务类型编码',
    service_type_name VARCHAR(100) COMMENT '服务类型名称',
    price_unit VARCHAR(20) COMMENT '计价单位',
    standard_price DECIMAL(10,2) COMMENT '标准价格',
    government_price DECIMAL(10,2) COMMENT '政府指导价',
    subsidy_price DECIMAL(10,2) COMMENT '补贴价格',
    area_id VARCHAR(32) COMMENT '适用区域',
    effective_date DATE COMMENT '生效日期',
    expiry_date DATE COMMENT '失效日期',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_service_type (service_type_code),
    INDEX idx_area (area_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务定价表';

-- 结算单表
CREATE TABLE IF NOT EXISTS t_settlement (
    settlement_id VARCHAR(32) PRIMARY KEY COMMENT '结算ID',
    settlement_no VARCHAR(50) UNIQUE COMMENT '结算编号',
    order_id VARCHAR(32) NOT NULL COMMENT '订单ID',
    provider_id VARCHAR(32) COMMENT '服务商ID',
    staff_id VARCHAR(32) COMMENT '服务人员ID',
    elder_id VARCHAR(32) COMMENT '老人ID',
    service_date DATE COMMENT '服务日期',
    service_duration INT COMMENT '实际服务时长',
    unit_price DECIMAL(10,2) COMMENT '服务单价',
    total_amount DECIMAL(10,2) COMMENT '总金额',
    subsidy_amount DECIMAL(10,2) COMMENT '补贴金额',
    self_pay_amount DECIMAL(10,2) COMMENT '自付金额',
    payment_method VARCHAR(20) COMMENT '支付方式 WECHAT/ALIPAY/BANK_CARD/CASH',
    payment_status VARCHAR(20) DEFAULT 'UNPAID' COMMENT '支付状态 UNPAID/PAID/REFUNDED',
    payment_time DATETIME COMMENT '支付时间',
    transaction_id VARCHAR(100) COMMENT '交易流水号',
    settlement_time DATETIME COMMENT '结算时间',
    invoice_status VARCHAR(20) DEFAULT 'NOT_ISSUED' COMMENT '发票状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order (order_id),
    INDEX idx_provider (provider_id),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算单表';

-- 退款记录表
CREATE TABLE IF NOT EXISTS t_refund (
    refund_id VARCHAR(32) PRIMARY KEY COMMENT '退款ID',
    refund_no VARCHAR(50) UNIQUE COMMENT '退款编号',
    order_id VARCHAR(32) NOT NULL COMMENT '订单ID',
    settlement_id VARCHAR(32) COMMENT '结算ID',
    refund_type VARCHAR(20) COMMENT '退款类型 FULL/PARTIAL',
    refund_amount DECIMAL(10,2) COMMENT '退款金额',
    refund_reason VARCHAR(500) COMMENT '退款原因',
    refund_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '退款状态 PENDING/PROCESSING/COMPLETED/REJECTED',
    applicant_id VARCHAR(32) COMMENT '申请人ID',
    approver_id VARCHAR(32) COMMENT '审批人ID',
    approval_time DATETIME COMMENT '审批时间',
    refund_time DATETIME COMMENT '退款时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order (order_id),
    INDEX idx_status (refund_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- ============================================
-- 7. 评价服务相关表
-- ============================================

-- 服务评价表
CREATE TABLE IF NOT EXISTS t_service_evaluation (
    evaluation_id VARCHAR(32) PRIMARY KEY COMMENT '评价ID',
    order_id VARCHAR(32) NOT NULL COMMENT '订单ID',
    staff_id VARCHAR(32) NOT NULL COMMENT '服务人员ID',
    provider_id VARCHAR(32) NOT NULL COMMENT '服务商ID',
    elder_id VARCHAR(32) COMMENT '老人ID',
    attitude_score INT COMMENT '态度得分(1-5星)',
    quality_score INT COMMENT '质量得分(1-5星)',
    efficiency_score INT COMMENT '效率得分(1-5星)',
    overall_score INT COMMENT '总体得分(1-5星)',
    average_score DECIMAL(5,2) COMMENT '平均得分',
    evaluation_content TEXT COMMENT '评价内容',
    evaluation_tags VARCHAR(500) COMMENT '评价标签',
    is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名',
    evaluation_time DATETIME COMMENT '评价时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_order (order_id),
    INDEX idx_staff (staff_id),
    INDEX idx_provider (provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务评价表';

-- 客户反馈表
CREATE TABLE IF NOT EXISTS t_customer_feedback (
    feedback_id VARCHAR(32) PRIMARY KEY COMMENT '反馈ID',
    elder_id VARCHAR(32) COMMENT '老人ID',
    feedback_type VARCHAR(20) COMMENT '反馈类型 COMPLAINT/SUGGESTION/PRAISE',
    feedback_content TEXT COMMENT '反馈内容',
    feedback_date DATE COMMENT '反馈日期',
    feedback_channel VARCHAR(20) COMMENT '反馈渠道 ONLINE/PHONE/ON_SITE',
    feedback_attachments TEXT COMMENT '反馈附件',
    handler_id VARCHAR(32) COMMENT '处理人ID',
    handling_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '处理状态 PENDING/HANDLING/HANDLED',
    handling_result TEXT COMMENT '处理结果',
    handling_time DATETIME COMMENT '处理时间',
    feedback_response TEXT COMMENT '回复内容',
    satisfaction INT COMMENT '满意度(1-5星)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_elder (elder_id),
    INDEX idx_feedback_type (feedback_type),
    INDEX idx_handling_status (handling_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户反馈表';

-- ============================================
-- 8. 配置服务相关表
-- ============================================

-- 数据字典类型表
CREATE TABLE IF NOT EXISTS t_dict_type (
    dict_type_id VARCHAR(32) PRIMARY KEY COMMENT '字典类型ID',
    dict_type_code VARCHAR(50) UNIQUE NOT NULL COMMENT '字典类型编码',
    dict_type_name VARCHAR(100) COMMENT '字典类型名称',
    dict_type_desc VARCHAR(500) COMMENT '字典类型描述',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统字典',
    sort_order INT COMMENT '排序',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_dict_type_code (dict_type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典类型表';

-- 数据字典项表
CREATE TABLE IF NOT EXISTS t_dict_item (
    dict_item_id VARCHAR(32) PRIMARY KEY,
    dict_type_id VARCHAR(32) NOT NULL COMMENT '字典类型ID',
    dict_item_code VARCHAR(50) COMMENT '字典项编码',
    dict_item_name VARCHAR(100) COMMENT '字典项名称',
    dict_item_value VARCHAR(100) COMMENT '字典项值',
    sort_order INT COMMENT '排序',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_dict_type (dict_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典项表';

-- 服务类型表
CREATE TABLE IF NOT EXISTS t_service_type (
    service_type_id VARCHAR(32) PRIMARY KEY COMMENT '服务类型ID',
    service_type_code VARCHAR(50) UNIQUE NOT NULL COMMENT '服务类型编码',
    service_type_name VARCHAR(100) COMMENT '服务类型名称',
    parent_id VARCHAR(32) COMMENT '父类型ID',
    service_level VARCHAR(20) COMMENT '服务层级',
    service_desc TEXT COMMENT '服务描述',
    service_standard TEXT COMMENT '服务标准',
    service_unit VARCHAR(20) COMMENT '计价单位',
    estimated_duration INT COMMENT '预计服务时长(分钟)',
    icon VARCHAR(200) COMMENT '图标',
    sort_order INT COMMENT '排序',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务类型表';

-- 区域表
CREATE TABLE IF NOT EXISTS t_area (
    area_id VARCHAR(32) PRIMARY KEY COMMENT '区域ID',
    area_code VARCHAR(50) UNIQUE NOT NULL COMMENT '区域编码',
    area_name VARCHAR(100) COMMENT '区域名称',
    parent_id VARCHAR(32) COMMENT '父区域ID',
    area_level VARCHAR(20) COMMENT '区域层级 PROVINCE/CITY/DISTRICT/STREET/COMMUNITY',
    longitude DECIMAL(10,6) COMMENT '经度',
    latitude DECIMAL(10,6) COMMENT '纬度',
    sort_order INT COMMENT '排序',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_parent (parent_id),
    INDEX idx_area_code (area_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域表';

-- ============================================
-- 初始化基础数据
-- ============================================

-- 插入超级管理员用户 (密码: admin123)
INSERT INTO t_user (user_id, username, password, real_name, phone, user_type, status) VALUES
('U001', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', '13800138000', 'SYSTEM', 'NORMAL');

-- 插入角色
INSERT INTO t_role (role_id, role_code, role_name, role_desc, role_type, data_scope, sort_order, status) VALUES
('R001', 'SUPER_ADMIN', '超级管理员', '系统超级管理员', 'SYSTEM', 'ALL', 1, 'NORMAL'),
('R002', 'CITY_ADMIN', '市级管理员', '市级系统管理员', 'ADMIN', 'CITY', 2, 'NORMAL'),
('R003', 'DISTRICT_ADMIN', '区县管理员', '区县系统管理员', 'ADMIN', 'DISTRICT', 3, 'NORMAL'),
('R004', 'PROVIDER_ADMIN', '服务商管理员', '服务商管理员', 'PROVIDER', 'SELF', 4, 'NORMAL');

-- 插入服务类型
INSERT INTO t_service_type (service_type_id, service_type_code, service_type_name, service_desc, service_unit, estimated_duration, sort_order, status) VALUES
('ST001', 'MEAL', '助餐服务', '为老年人提供餐饮配送服务', '次', 30, 1, 'ACTIVE'),
('ST002', 'CLEANING', '助洁服务', '为老年人提供家庭清洁服务', '小时', 60, 2, 'ACTIVE'),
('ST003', 'BATHING', '助浴服务', '为老年人提供洗浴协助服务', '次', 30, 3, 'ACTIVE'),
('ST004', 'LAUNDRY', '助洗服务', '为老年人提供衣物清洗服务', '次', 30, 4, 'ACTIVE'),
('ST005', 'HAIRDRESSING', '助行服务', '为老年人提供出行协助服务', '次', 30, 5, 'ACTIVE'),
('ST006', 'HEALTH', '健康监测', '为老年人提供健康指标监测服务', '次', 20, 6, 'ACTIVE'),
('ST007', 'REHABILITATION', '康复护理', '为老年人提供康复训练和护理服务', '小时', 60, 7, 'ACTIVE'),
('ST008', 'MEDICATION', '用药提醒', '为老年人提供用药提醒服务', '次', 5, 8, 'ACTIVE'),
('ST009', 'COMPANION', '精神慰藉', '为老年人提供陪伴和心理支持服务', '小时', 60, 9, 'ACTIVE'),
('ST010', 'EMERGENCY', '紧急救援', '为老年人提供紧急情况救援服务', '次', 0, 10, 'ACTIVE'),
('ST011', 'CONSULTATION', '信息咨询', '为老年人提供政策和服务信息咨询服务', '次', 20, 11, 'ACTIVE'),
('ST012', 'SHOPPING', '助购服务', '为老年人提供生活物资代购服务', '次', 30, 12, 'ACTIVE');

-- 插入省级区域数据
INSERT INTO t_area (area_id, area_code, area_name, parent_id, area_level, sort_order, status) VALUES
('A001', '110000', '北京市', '0', 'PROVINCE', 1, 'ACTIVE'),
('A002', '310000', '上海市', '0', 'PROVINCE', 2, 'ACTIVE'),
('A003', '440000', '广东省', '0', 'PROVINCE', 3, 'ACTIVE');

-- 插入市级区域数据
INSERT INTO t_area (area_id, area_code, area_name, parent_id, area_level, sort_order, status) VALUES
('A101', '110100', '市辖区', 'A001', 'CITY', 1, 'ACTIVE'),
('A201', '310100', '市辖区', 'A002', 'CITY', 1, 'ACTIVE'),
('A301', '440100', '广州市', 'A003', 'CITY', 1, 'ACTIVE');

-- 插入区县区域数据
INSERT INTO t_area (area_id, area_code, area_name, parent_id, area_level, sort_order, status) VALUES
('A111', '110101', '东城区', 'A101', 'DISTRICT', 1, 'ACTIVE'),
('A112', '110102', '西城区', 'A101', 'DISTRICT', 2, 'ACTIVE'),
('A113', '110105', '朝阳区', 'A101', 'DISTRICT', 3, 'ACTIVE'),
('A211', '310101', '黄浦区', 'A201', 'DISTRICT', 1, 'ACTIVE'),
('A311', '440103', '荔湾区', 'A301', 'DISTRICT', 1, 'ACTIVE');

-- 插入字典类型
INSERT INTO t_dict_type (dict_type_id, dict_type_code, dict_type_name, dict_type_desc, sort_order, status) VALUES
('DT001', 'gender', '性别', '性别字典', 1, 'NORMAL'),
('DT002', 'education', '学历', '学历字典', 2, 'NORMAL'),
('DT003', 'marital_status', '婚姻状况', '婚姻状况字典', 3, 'NORMAL'),
('DT004', 'care_level', '护理级别', '老年人护理级别字典', 4, 'NORMAL'),
('DT005', 'health_status', '健康状况', '健康状况字典', 5, 'NORMAL'),
('DT006', 'living_status', '居住状况', '居住状况字典', 6, 'NORMAL'),
('DT007', 'provider_type', '服务商类型', '服务商类型字典', 7, 'NORMAL'),
('DT008', 'order_status', '订单状态', '订单状态字典', 8, 'NORMAL'),
('DT009', 'shift_type', '班次类型', '排班班次类型字典', 9, 'NORMAL');

-- 插入字典项
INSERT INTO t_dict_item (dict_item_id, dict_type_id, dict_item_code, dict_item_name, dict_item_value, sort_order, status) VALUES
-- 性别
('DI001', 'DT001', 'MALE', '男', 'MALE', 1, 'NORMAL'),
('DI002', 'DT001', 'FEMALE', '女', 'FEMALE', 2, 'NORMAL'),
-- 学历
('DI003', 'DT002', 'PRIMARY', '小学', 'PRIMARY', 1, 'NORMAL'),
('DI004', 'DT002', 'JUNIOR', '初中', 'JUNIOR', 2, 'NORMAL'),
('DI005', 'DT002', 'SENIOR', '高中/中专', 'SENIOR', 3, 'NORMAL'),
('DI006', 'DT002', 'COLLEGE', '大专', 'COLLEGE', 4, 'NORMAL'),
('DI007', 'DT002', 'BACHELOR', '本科', 'BACHELOR', 5, 'NORMAL'),
('DI008', 'DT002', 'MASTER', '硕士及以上', 'MASTER', 6, 'NORMAL'),
-- 护理级别
('DI009', 'DT004', 'NORMAL', '正常', 'NORMAL', 1, 'NORMAL'),
('DI010', 'DT004', 'MILD', '轻度', 'MILD', 2, 'NORMAL'),
('DI011', 'DT004', 'MODERATE', '中度', 'MODERATE', 3, 'NORMAL'),
('DI012', 'DT004', 'SEVERE', '重度', 'SEVERE', 4, 'NORMAL'),
-- 健康状况
('DI013', 'DT005', 'GOOD', '良好', 'GOOD', 1, 'NORMAL'),
('DI014', 'DT005', 'FAIR', '一般', 'FAIR', 2, 'NORMAL'),
('DI015', 'DT005', 'POOR', '较差', 'POOR', 3, 'NORMAL'),
-- 服务商类型
('DI016', 'DT007', 'GOVERNMENT', '公办', 'GOVERNMENT', 1, 'NORMAL'),
('DI017', 'DT007', 'PRIVATE', '民营', 'PRIVATE', 2, 'NORMAL'),
('DI018', 'DT007', 'NON_PROFIT', '非营利', 'NON_PROFIT', 3, 'NORMAL'),
-- 订单状态
('DI019', 'DT008', 'CREATED', '已创建', 'CREATED', 1, 'NORMAL'),
('DI020', 'DT008', 'PAID', '已支付', 'PAID', 2, 'NORMAL'),
('DI021', 'DT008', 'DISPATCHED', '已派单', 'DISPATCHED', 3, 'NORMAL'),
('DI022', 'DT008', 'RECEIVED', '已接单', 'RECEIVED', 4, 'NORMAL'),
('DI023', 'DT008', 'SERVICE_STARTED', '服务开始', 'SERVICE_STARTED', 5, 'NORMAL'),
('DI024', 'DT008', 'SERVICE_COMPLETED', '服务完成', 'SERVICE_COMPLETED', 6, 'NORMAL'),
('DI025', 'DT008', 'EVALUATED', '已评价', 'EVALUATED', 7, 'NORMAL'),
('DI026', 'DT008', 'CANCELLED', '已取消', 'CANCELLED', 8, 'NORMAL');

-- ============================================
-- 创建默认配置参数
-- ============================================

CREATE TABLE IF NOT EXISTS t_system_param (
    param_id VARCHAR(32) PRIMARY KEY COMMENT '参数ID',
    param_category VARCHAR(50) COMMENT '参数分类',
    param_code VARCHAR(100) UNIQUE NOT NULL COMMENT '参数编码',
    param_name VARCHAR(100) COMMENT '参数名称',
    param_value VARCHAR(500) COMMENT '参数值',
    param_type VARCHAR(20) DEFAULT 'STRING' COMMENT '参数类型 STRING/NUMBER/BOOLEAN',
    default_value VARCHAR(500) COMMENT '默认值',
    param_desc VARCHAR(500) COMMENT '参数说明',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统参数',
    sort_order INT COMMENT '排序',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_param_code (param_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数表';

INSERT INTO t_system_param (param_id, param_category, param_code, param_name, param_value, param_type, default_value, param_desc, is_system) VALUES
('P001', 'SYSTEM', 'PLATFORM_NAME', '平台名称', '智慧居家养老服务管理平台', 'STRING', '智慧居家养老服务管理平台', '系统平台名称', 1),
('P002', 'SYSTEM', 'PLATFORM_VERSION', '平台版本', 'V1.0.0', 'STRING', 'V1.0.0', '系统平台版本', 1),
('P003', 'ORDER', 'ORDER_EXPIRE_MINUTES', '订单超时分钟数', '30', 'NUMBER', '30', '订单未支付超时时间(分钟)', 1),
('P004', 'ORDER', 'MAX_DISPATCH_ATTEMPTS', '最大派单次数', '3', 'NUMBER', '3', '自动派单最大尝试次数', 1);

-- ============================================
-- 创建操作日志表
-- ============================================

CREATE TABLE IF NOT EXISTS t_operation_log (
    operation_log_id VARCHAR(32) PRIMARY KEY,
    user_id VARCHAR(32) COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation_module VARCHAR(50) COMMENT '操作模块',
    operation_type VARCHAR(50) COMMENT '操作类型',
    operation_content TEXT COMMENT '操作内容',
    operation_time DATETIME COMMENT '操作时间',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- 完成
-- ============================================
