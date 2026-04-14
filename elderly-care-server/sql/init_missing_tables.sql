-- ============================================
-- 缺失表创建及示例数据初始化
-- 数据库: elderly_care
-- ============================================

USE elderly_care;

-- ============================================
-- 1. 预约表 (appointment)
-- ============================================
DROP TABLE IF EXISTS `appointment`;
CREATE TABLE `appointment` (
  `appointment_id` VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '预约ID',
  `appointment_no` VARCHAR(64) DEFAULT NULL COMMENT '预约单号',
  `elder_name` VARCHAR(100) DEFAULT NULL COMMENT '老人姓名',
  `elder_id_card` VARCHAR(18) DEFAULT NULL COMMENT '老人身份证号',
  `elder_phone` VARCHAR(11) DEFAULT NULL COMMENT '老人手机号',
  `elder_address` VARCHAR(255) DEFAULT NULL COMMENT '老人地址',
  `elder_area_id` VARCHAR(64) DEFAULT NULL COMMENT '区域ID',
  `elder_area_name` VARCHAR(100) DEFAULT NULL COMMENT '区域名称',
  `service_type` VARCHAR(50) DEFAULT NULL COMMENT '服务类型',
  `service_type_code` VARCHAR(20) DEFAULT NULL COMMENT '服务类型编码',
  `service_content` VARCHAR(255) DEFAULT NULL COMMENT '服务内容',
  `appointment_time` VARCHAR(50) DEFAULT NULL COMMENT '预约时间',
  `service_duration` INT DEFAULT NULL COMMENT '预计服务时长(分钟)',
  `provider_id` VARCHAR(64) DEFAULT NULL COMMENT '服务机构ID',
  `provider_name` VARCHAR(200) DEFAULT NULL COMMENT '服务机构名称',
  `provider_address` VARCHAR(255) DEFAULT NULL COMMENT '服务机构地址',
  `visitor_count` INT DEFAULT NULL COMMENT '来访人数',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
  `validity` VARCHAR(20) DEFAULT 'VALID' COMMENT '数据有效性',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `reply_info` VARCHAR(255) DEFAULT NULL COMMENT '回复信息',
  `assessment_type` VARCHAR(50) DEFAULT NULL COMMENT '评估类型',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `confirm_time` DATETIME DEFAULT NULL COMMENT '确认时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
  UNIQUE KEY `uk_appointment_no` (`appointment_no`),
  KEY `idx_elder_phone` (`elder_phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约管理表';

-- 插入预约示例数据（使用真实老人数据，与t_elder表关联，延安市）
INSERT INTO `appointment` (`appointment_id`, `appointment_no`, `elder_name`, `elder_id_card`, `elder_phone`, `elder_address`, `elder_area_name`, `service_type`, `service_type_code`, `appointment_time`, `service_duration`, `provider_id`, `provider_name`, `status`, `remark`, `create_time`) VALUES
('APT001', 'APT20260411001', '张桂兰', '610602194506200012', '13700137001', '延安市宝塔区宝塔山街道102号', '宝塔区', '生活照料', 'HOME_CARE', '2026-04-15 09:00', 120, 'P001', '延安市宝塔区福寿康养老服务中心', 'PENDING', '日常照料服务预约', NOW()),
('APT002', 'APT20260411002', '王德胜', '610602194211150013', '13700137002', '延安市宝塔区南市街道88号', '宝塔区', '助洁服务', 'CLEANING', '2026-04-16 14:00', 90, 'P001', '延安市宝塔区福寿康养老服务中心', 'CONFIRMED', '定期保洁服务', NOW()),
('APT003', 'APT20260411003', '李秀英', '610603194803080014', '13700137003', '延安市安塞区真武洞街道200号', '安塞区', '助餐服务', 'MEAL_DELIVERY', '2026-04-17 10:00', 60, 'P002', '延安市安塞区康泰养老服务中心', 'ASSIGNED', '午餐配送服务', NOW()),
('APT004', 'APT20260411004', '陈明辉', '610621195009250015', '13700137004', '延安市延川县社管中心50号', '延川县', '日间照料', 'DAY_CARE', '2026-04-18 08:00', 480, 'P003', '延安市延川县夕阳红养老服务中心', 'PENDING', '日托照料服务', NOW()),
('APT005', 'APT20260411005', '刘玉珍', '610623194607120016', '13700137005', '延安市子长市瓦窑堡街道88号', '子长市', '康复护理', 'REHABILITATION', '2026-04-19 11:30', 120, 'P004', '延安市子长市福乐养老服务中心', 'COMPLETED', '术后康复护理', NOW());

-- ============================================
-- 2. 质检表 (quality_check)
-- ============================================
DROP TABLE IF EXISTS `quality_check`;
CREATE TABLE `quality_check` (
  `quality_check_id` VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '质检ID',
  `check_no` VARCHAR(64) DEFAULT NULL COMMENT '质检编号',
  `order_id` VARCHAR(64) DEFAULT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) DEFAULT NULL COMMENT '订单号',
  `service_log_id` VARCHAR(64) DEFAULT NULL COMMENT '服务日志ID',
  `service_category` VARCHAR(50) DEFAULT NULL COMMENT '服务类别',
  `provider_id` VARCHAR(64) DEFAULT NULL COMMENT '服务商ID',
  `provider_name` VARCHAR(200) DEFAULT NULL COMMENT '服务商名称',
  `staff_id` VARCHAR(64) DEFAULT NULL COMMENT '服务人员ID',
  `staff_name` VARCHAR(100) DEFAULT NULL COMMENT '服务人员姓名',
  `check_type` VARCHAR(20) DEFAULT NULL COMMENT '质检类型',
  `check_method` VARCHAR(20) DEFAULT NULL COMMENT '质检方式',
  `check_score` DECIMAL(5,2) DEFAULT NULL COMMENT '综合评分',
  `check_result` VARCHAR(20) DEFAULT NULL COMMENT '质检结果',
  `check_photos` TEXT COMMENT '质检照片',
  `check_remark` VARCHAR(500) DEFAULT NULL COMMENT '质检备注',
  `check_time` DATETIME DEFAULT NULL COMMENT '质检时间',
  `checker_id` VARCHAR(64) DEFAULT NULL COMMENT '质检员ID',
  `checker_name` VARCHAR(100) DEFAULT NULL COMMENT '质检员姓名',
  `need_rectify` TINYINT(1) DEFAULT 0 COMMENT '是否需要整改',
  `rectify_notice` VARCHAR(255) DEFAULT NULL COMMENT '整改通知',
  `rectify_deadline` DATETIME DEFAULT NULL COMMENT '整改期限',
  `rectify_status` VARCHAR(20) DEFAULT NULL COMMENT '整改状态',
  `rectify_photos` TEXT COMMENT '整改照片',
  `rectify_remark` VARCHAR(500) DEFAULT NULL COMMENT '整改说明',
  `recheck_time` DATETIME DEFAULT NULL COMMENT '复检时间',
  `recheck_result` VARCHAR(20) DEFAULT NULL COMMENT '复检结果',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
  KEY `idx_order_id` (`order_id`),
  KEY `idx_provider_id` (`provider_id`),
  KEY `idx_check_result` (`check_result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务质检表';

-- 插入质检示例数据（使用真实服务人员数据，关联t_order表，延安市）
INSERT INTO `quality_check` (`quality_check_id`, `check_no`, `order_id`, `order_no`, `service_log_id`, `provider_id`, `provider_name`, `staff_id`, `staff_name`, `check_type`, `check_method`, `check_score`, `check_result`, `check_remark`, `check_time`, `need_rectify`, `create_time`) VALUES
('QC001', 'QC20260411001', 'O001', 'O20240315001', 'SL001', 'P001', '延安市宝塔区福寿康养老服务中心', 'S001', '张小梅', 'RANDOM', 'PHOTO_REVIEW', 92.50, 'QUALIFIED', '服务态度好，准时到达', '2024-03-15 14:00:00', 0, NOW()),
('QC002', 'QC20260411002', 'O002', 'O20240316001', 'SL002', 'P001', '延安市宝塔区福寿康养老服务中心', 'S001', '张小梅', 'SCHEDULED', 'ON_SITE', 85.00, 'QUALIFIED', '清洁度良好，老人反馈满意', '2024-03-16 15:00:00', 0, NOW()),
('QC003', 'QC20260411003', 'O003', 'O20240317001', 'SL003', 'P002', '延安市安塞区康泰养老服务中心', 'S003', '王秀芳', 'COMPLETION', 'PHONE_REVIEW', 78.00, 'NEED_RECTIFY', '服务时间略短，需加强', '2024-03-17 15:00:00', 1, NOW()),
('QC004', 'QC20260411004', 'O004', 'O20240318001', 'SL004', 'P002', '延安市延川县夕阳红养老服务中心', 'S004', '陈伟强', 'COMPLAINT', 'ON_SITE', 65.00, 'UNQUALIFIED', '服务延迟严重，态度需改进', '2024-03-18 18:00:00', 1, NOW()),
('QC005', 'QC20260411005', 'O005', 'O20240319001', 'SL005', 'P005', '延安市子长市福乐养老服务中心', 'S005', '林美红', 'RANDOM', 'PHOTO_REVIEW', 95.00, 'QUALIFIED', '非常满意，专业度高', '2024-03-19 12:00:00', 0, NOW());

-- ============================================
-- 3. 服务日志表 (service_log)
-- ============================================
DROP TABLE IF EXISTS `service_log`;
CREATE TABLE `service_log` (
  `service_log_id` VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '服务日志ID',
  `log_no` VARCHAR(64) DEFAULT NULL COMMENT '服务日志编号',
  `order_id` VARCHAR(64) DEFAULT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) DEFAULT NULL COMMENT '订单号',
  `elder_id` VARCHAR(64) DEFAULT NULL COMMENT '老人ID',
  `elder_name` VARCHAR(100) DEFAULT NULL COMMENT '老人姓名',
  `elder_phone` VARCHAR(11) DEFAULT NULL COMMENT '老人手机号',
  `elder_address` VARCHAR(255) DEFAULT NULL COMMENT '老人地址',
  `staff_id` VARCHAR(64) DEFAULT NULL COMMENT '服务人员ID',
  `staff_name` VARCHAR(100) DEFAULT NULL COMMENT '服务人员姓名',
  `staff_phone` VARCHAR(11) DEFAULT NULL COMMENT '服务人员手机号',
  `provider_id` VARCHAR(64) DEFAULT NULL COMMENT '服务商ID',
  `provider_name` VARCHAR(200) DEFAULT NULL COMMENT '服务商名称',
  `service_type_code` VARCHAR(20) DEFAULT NULL COMMENT '服务类型编码',
  `service_type_name` VARCHAR(100) DEFAULT NULL COMMENT '服务类型名称',
  `service_date` VARCHAR(20) DEFAULT NULL COMMENT '服务日期',
  `service_start_time` DATETIME DEFAULT NULL COMMENT '服务开始时间',
  `service_end_time` DATETIME DEFAULT NULL COMMENT '服务结束时间',
  `service_duration` INT DEFAULT NULL COMMENT '服务时长(分钟)',
  `service_status` VARCHAR(20) DEFAULT NULL COMMENT '服务状态',
  `actual_duration` INT DEFAULT NULL COMMENT '实际服务时长',
  `service_score` DECIMAL(5,2) DEFAULT NULL COMMENT '服务评分',
  `service_comment` VARCHAR(500) DEFAULT NULL COMMENT '服务评价',
  `service_photos` TEXT COMMENT '服务照片',
  `elder_signature` VARCHAR(255) DEFAULT NULL COMMENT '老人签名',
  `anomaly_type` VARCHAR(50) DEFAULT NULL COMMENT '异常类型',
  `anomaly_desc` VARCHAR(500) DEFAULT NULL COMMENT '异常描述',
  `anomaly_photos` TEXT COMMENT '异常照片',
  `anomaly_status` VARCHAR(20) DEFAULT NULL COMMENT '异常处理状态',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
  KEY `idx_order_id` (`order_id`),
  KEY `idx_staff_id` (`staff_id`),
  KEY `idx_provider_id` (`provider_id`),
  KEY `idx_service_date` (`service_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务日志表';

-- 插入服务日志示例数据（关联t_order/t_elder/t_staff表，延安市）
INSERT INTO `service_log` (`service_log_id`, `log_no`, `order_id`, `order_no`, `elder_id`, `elder_name`, `elder_phone`, `elder_address`, `staff_id`, `staff_name`, `staff_phone`, `provider_id`, `provider_name`, `service_type_code`, `service_type_name`, `service_date`, `service_start_time`, `service_end_time`, `service_duration`, `service_status`, `actual_duration`, `service_score`, `service_comment`, `create_time`) VALUES
('SL001', 'SL20260411001', 'O001', 'O20240315001', 'E001', '张桂兰', '13700137001', '延安市宝塔区宝塔山街道102号', 'S001', '张小梅', '13900139001', 'P001', '延安市宝塔区福寿康养老服务中心', 'HOME_CARE', '生活照料', '2024-03-15', '2024-03-15 09:00:00', '2024-03-15 11:00:00', 120, 'COMPLETED', 120, 95.00, '服务态度很好，非常满意', NOW()),
('SL002', 'SL20260411002', 'O002', 'O20240316001', 'E002', '王德胜', '13700137002', '延安市宝塔区南市街道88号', 'S001', '张小梅', '13900139001', 'P001', '延安市宝塔区福寿康养老服务中心', 'HOME_CARE', '生活照料', '2024-03-16', '2024-03-16 10:00:00', '2024-03-16 13:00:00', 180, 'COMPLETED', 175, 88.00, '照顾老人细心，专业度高', NOW()),
('SL003', 'SL20260411003', 'O003', 'O20240317001', 'E003', '李秀英', '13700137003', '延安市安塞区真武洞街道200号', 'S003', '王秀芳', '13900139003', 'P002', '延安市安塞区康泰养老服务中心', 'MEAL_DELIVERY', '助餐服务', '2024-03-17', '2024-03-17 11:30:00', '2024-03-17 12:30:00', 60, 'COMPLETED', 60, 92.00, '送餐准时，饭菜可口', NOW()),
('SL004', 'SL20260411004', 'O004', 'O20240318001', 'E004', '陈明辉', '13700137004', '延安市延川县社管中心50号', 'S004', '陈伟强', '13900139004', 'P003', '延安市延川县夕阳红养老服务中心', 'CLEANING', '助洁服务', '2024-03-18', '2024-03-18 14:00:00', '2024-03-18 16:00:00', 120, 'COMPLETED', 115, 78.00, '清洁较彻底，稍有延迟', NOW()),
('SL005', 'SL20260411005', 'O005', 'O20240319001', 'E005', '刘玉珍', '13700137005', '延安市子长市瓦窑堡街道88号', 'S005', '林美红', '13900139005', 'P004', '延安市子长市福乐养老服务中心', 'HEALTH_MONITORING', '健康监测', '2024-03-19', '2024-03-19 08:30:00', '2024-03-19 09:30:00', 60, 'COMPLETED', 60, 96.00, '健康监测专业，建议很实用', NOW());

-- ============================================
-- 执行完成
-- ============================================
SELECT '建表和示例数据插入完成!' AS Result;
