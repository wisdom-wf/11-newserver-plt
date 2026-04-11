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

-- 插入预约示例数据（使用真实老人数据）
INSERT INTO `appointment` (`appointment_id`, `appointment_no`, `elder_name`, `elder_id_card`, `elder_phone`, `elder_address`, `elder_area_name`, `service_type`, `service_type_code`, `appointment_time`, `service_duration`, `provider_id`, `provider_name`, `status`, `remark`, `create_time`) VALUES
('APT001', 'APT20260411001', '张风莲', '610602195501011234', '18292141951', '陕西省延安市宝塔区川口乡蟠龙村53号', '宝塔区', '上门服务', '05', '2026-04-15 09:00', 120, '4253cf396b68903505ea8f74b3d37700', '延安市宝塔区丁峰家政服务有限公司', 'PENDING', '需要助餐服务', NOW()),
('APT002', 'APT20260411002', '王秀兰', '610602196002022345', '13892175256', '陕西省延安市宝塔区杨家岭南苑三号楼一单元1103', '宝塔区', '助洁服务', '03', '2026-04-16 14:00', 90, '4253cf396b68903505ea8f74b3d37700', '延安市宝塔区丁峰家政服务有限公司', 'CONFIRMED', '定期保洁服务', NOW()),
('APT003', 'APT20260411003', '吉花凤', '610602195803033456', '15129512451', '陕西省延安市宝塔区马家湾十二区', '宝塔区', '康复护理', '08', '2026-04-17 10:00', 60, 'b147a7db096643eb97385136c555f933', '延安家享悠网络科技有限公司', 'ASSIGNED', '术后康复护理', NOW()),
('APT004', 'APT20260411004', '高美英', '610602196510044567', '18966592068', '陕西省延安市宝塔区桥儿沟镇小王庄村53号', '宝塔区', '日间照料', '02', '2026-04-18 08:00', 480, 'b147a7db096643eb97385136c555f933', '延安家享悠网络科技有限公司', 'PENDING', '日托照料服务', NOW()),
('APT005', 'APT20260411005', '赵德芳', '610602195707055678', '13992160845', '陕西省延安市宝塔区麻洞川乡樊村082号', '宝塔区', '助餐服务', '01', '2026-04-19 11:30', 60, '4253cf396b68903505ea8f74b3d37700', '延安市宝塔区丁峰家政服务有限公司', 'COMPLETED', '午餐配送服务', NOW());

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

-- 插入质检示例数据（使用真实服务人员数据）
INSERT INTO `quality_check` (`quality_check_id`, `check_no`, `order_id`, `order_no`, `provider_id`, `provider_name`, `staff_id`, `staff_name`, `check_type`, `check_method`, `check_score`, `check_result`, `check_remark`, `check_time`, `need_rectify`, `create_time`) VALUES
('QC001', 'QC20260411001', 'ORD001', 'ORD20260408001', '4253cf396b68903505ea8f74b3d37700', '延安市宝塔区丁峰家政服务有限公司', 'S001', '张小梅', 'RANDOM', 'PHOTO_REVIEW', 92.50, 'QUALIFIED', '服务态度好，准时到达', NOW(), 0, NOW()),
('QC002', 'QC20260411002', 'ORD002', 'ORD20260408002', '4253cf396b68903505ea8f74b3d37700', '延安市宝塔区丁峰家政服务有限公司', 'S002', '李建国', 'SCHEDULED', 'ON_SITE', 85.00, 'QUALIFIED', '清洁度良好', NOW(), 0, NOW()),
('QC003', 'QC20260411003', 'ORD003', 'ORD20260408003', 'b147a7db096643eb97385136c555f933', '延安家享悠网络科技有限公司', 'S003', '王秀芳', 'COMPLETION', 'PHONE_REVIEW', 78.00, 'NEED_RECTIFY', '服务时间不足，需要加强', NOW(), 1, NOW()),
('QC004', 'QC20260411004', 'ORD004', 'ORD20260408004', 'b147a7db096643eb97385136c555f933', '延安家享悠网络科技有限公司', 'S004', '陈伟强', 'COMPLAINT', 'ON_SITE', 65.00, 'UNQUALIFIED', '服务态度差，延迟严重', NOW(), 1, NOW()),
('QC005', 'QC20260411005', 'ORD005', 'ORD20260408005', '4253cf396b68903505ea8f74b3d37700', '延安市宝塔区丁峰家政服务有限公司', 'S001', '张小梅', 'RANDOM', 'PHOTO_REVIEW', 95.00, 'QUALIFIED', '非常满意', NOW(), 0, NOW());

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

-- 插入服务日志示例数据（使用真实老人和服务人员数据）
INSERT INTO `service_log` (`service_log_id`, `log_no`, `order_id`, `order_no`, `elder_id`, `elder_name`, `elder_phone`, `elder_address`, `staff_id`, `staff_name`, `staff_phone`, `provider_id`, `provider_name`, `service_type_code`, `service_type_name`, `service_date`, `service_start_time`, `service_end_time`, `service_duration`, `service_status`, `actual_duration`, `service_score`, `service_comment`, `create_time`) VALUES
('SL001', 'SL20260411001', 'ORD001', 'ORD20260408001', '0053b9060f7b3b7e', '张风莲', '18292141951', '陕西省延安市宝塔区川口乡蟠龙村53号', 'S001', '张小梅', '13900139001', '4253cf396b68903505ea8f74b3d37700', '延安市宝塔区丁峰家政服务有限公司', '05', '上门服务', '2026-04-11', '2026-04-11 09:00:00', '2026-04-11 11:00:00', 120, 'COMPLETED', 120, 95.00, '服务态度很好，非常满意', NOW()),
('SL002', 'SL20260411002', 'ORD002', 'ORD20260408002', '02589690e02abe19', '王秀兰', '13892175256', '陕西省延安市宝塔区杨家岭南苑三号楼一单元1103', 'S002', '李建国', '13900139002', '4253cf396b68903505ea8f74b3d37700', '延安市宝塔区丁峰家政服务有限公司', '03', '助洁服务', '2026-04-11', '2026-04-11 14:00:00', '2026-04-11 15:30:00', 90, 'COMPLETED', 90, 90.00, '清洁彻底，很满意', NOW()),
('SL003', 'SL20260411003', 'ORD003', 'ORD20260408003', '03293e1bf5c9f3f1', '吉花凤', '15129512451', '陕西省延安市宝塔区马家湾十二区', 'S003', '王秀芳', '13900139003', 'b147a7db096643eb97385136c555f933', '延安家享悠网络科技有限公司', '08', '康复护理', '2026-04-11', '2026-04-11 10:00:00', '2026-04-11 11:00:00', 60, 'COMPLETED', 60, 88.00, '手法专业，有所改善', NOW()),
('SL004', 'SL20260411004', 'ORD004', 'ORD20260408004', '03685af04fb16948', '高美英', '18966592068', '陕西省延安市宝塔区桥儿沟镇小王庄村53号', 'S004', '陈伟强', '13900139004', 'b147a7db096643eb97385136c555f933', '延安家享悠网络科技有限公司', '02', '日间照料', '2026-04-11', '2026-04-11 08:00:00', '2026-04-11 18:00:00', 480, 'IN_SERVICE', NULL, NULL, '服务中', NOW()),
('SL005', 'SL20260411005', 'ORD005', 'ORD20260408005', '0402274b0415db09', '赵德芳', '13992160845', '陕西省延安市宝塔区麻洞川乡樊村082号', 'S001', '张小梅', '13900139001', '4253cf396b68903505ea8f74b3d37700', '延安市宝塔区丁峰家政服务有限公司', '01', '助餐服务', '2026-04-11', '2026-04-11 11:30:00', '2026-04-11 12:30:00', 60, 'COMPLETED', 60, 92.00, '饭菜可口，准时送达', NOW());

-- ============================================
-- 执行完成
-- ============================================
SELECT '建表和示例数据插入完成!' AS Result;
