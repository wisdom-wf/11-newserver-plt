-- =============================================
-- 服务日志、质检、满意度评价 模块增强 SQL脚本
-- 执行日期: 2026-04-25
-- 状态: 已执行完成
-- =============================================

-- 注意: 以下SQL已执行完成，此文件仅作记录
-- 如需重新执行，请先删除已创建的表和字段

-- USE elderly_care;
-- SET NAMES utf8mb4;
-- SET character_set_client = utf8mb4;
-- SET character_set_connection = utf8mb4;

-- =============================================
-- 1. service_log 表新增签到签退字段 (已执行)
-- =============================================
-- ALTER TABLE service_log
--     ADD COLUMN departure_time DATETIME COMMENT '出发时间' AFTER medication_given,
--     ADD COLUMN sign_in_time DATETIME COMMENT '签到时间' AFTER departure_time,
--     ADD COLUMN sign_in_location VARCHAR(200) COMMENT '签到位置(经纬度)' AFTER sign_in_time,
--     ADD COLUMN sign_in_photos VARCHAR(1000) COMMENT '签到照片' AFTER sign_in_location,
--     ADD COLUMN sign_out_time DATETIME COMMENT '签退时间' AFTER sign_in_photos,
--     ADD COLUMN sign_out_location VARCHAR(200) COMMENT '签退位置(经纬度)' AFTER sign_out_time,
--     ADD COLUMN sign_out_photos VARCHAR(1000) COMMENT '签退照片' AFTER sign_out_location;

-- =============================================
-- 2. t_service_evaluation 表新增回复字段和环境评分字段 (已执行)
-- =============================================
-- ALTER TABLE t_service_evaluation
--     ADD COLUMN environment_score INT COMMENT '环境评分(1-5)' AFTER efficiency_score,
--     ADD COLUMN reply_content TEXT COMMENT '回复内容' AFTER evaluation_time,
--     ADD COLUMN reply_time DATETIME COMMENT '回复时间' AFTER reply_content,
--     ADD COLUMN replyer_id VARCHAR(32) COMMENT '回复人ID' AFTER reply_time,
--     ADD COLUMN replyer_name VARCHAR(100) COMMENT '回复人姓名' AFTER replyer_id;

-- =============================================
-- 3. 服务商评分表 t_provider_score
-- =============================================
CREATE TABLE IF NOT EXISTS t_provider_score (
    score_id VARCHAR(32) PRIMARY KEY COMMENT '评分ID',
    provider_id VARCHAR(32) NOT NULL COMMENT '服务商ID',
    provider_name VARCHAR(200) COMMENT '服务商名称',
    period_type VARCHAR(20) NOT NULL COMMENT '评分周期类型：DAY/WEEK/MONTH/QUARTER/YEAR',
    period_start DATE COMMENT '周期开始日期',
    period_end DATE COMMENT '周期结束日期',
    attitude_score DECIMAL(5,2) DEFAULT 0 COMMENT '态度评分',
    quality_score DECIMAL(5,2) DEFAULT 0 COMMENT '质量评分',
    efficiency_score DECIMAL(5,2) DEFAULT 0 COMMENT '效率评分',
    environment_score DECIMAL(5,2) DEFAULT 0 COMMENT '环境评分',
    overall_score DECIMAL(5,2) DEFAULT 0 COMMENT '总体评分',
    evaluation_count INT DEFAULT 0 COMMENT '评价数量',
    complaint_count INT DEFAULT 0 COMMENT '投诉数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT(1) DEFAULT 0,
    INDEX idx_provider (provider_id),
    INDEX idx_period (period_type, period_start, period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务商评分表';

-- =============================================
-- 4. 服务人员评分表 t_staff_score
-- =============================================
CREATE TABLE IF NOT EXISTS t_staff_score (
    score_id VARCHAR(32) PRIMARY KEY COMMENT '评分ID',
    staff_id VARCHAR(32) NOT NULL COMMENT '服务人员ID',
    staff_name VARCHAR(100) COMMENT '服务人员姓名',
    provider_id VARCHAR(32) COMMENT '服务商ID',
    period_type VARCHAR(20) NOT NULL COMMENT '评分周期类型：DAY/WEEK/MONTH/QUARTER/YEAR',
    period_start DATE COMMENT '周期开始日期',
    period_end DATE COMMENT '周期结束日期',
    attitude_score DECIMAL(5,2) DEFAULT 0 COMMENT '态度评分',
    quality_score DECIMAL(5,2) DEFAULT 0 COMMENT '质量评分',
    efficiency_score DECIMAL(5,2) DEFAULT 0 COMMENT '效率评分',
    environment_score DECIMAL(5,2) DEFAULT 0 COMMENT '环境评分',
    overall_score DECIMAL(5,2) DEFAULT 0 COMMENT '总体评分',
    evaluation_count INT DEFAULT 0 COMMENT '评价数量',
    complaint_count INT DEFAULT 0 COMMENT '投诉数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT(1) DEFAULT 0,
    INDEX idx_staff (staff_id),
    INDEX idx_provider (provider_id),
    INDEX idx_period (period_type, period_start, period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务人员评分表';

-- =============================================
-- 5. 质量预警表 t_quality_alert
-- =============================================
CREATE TABLE IF NOT EXISTS t_quality_alert (
    alert_id VARCHAR(32) PRIMARY KEY COMMENT '预警ID',
    alert_type VARCHAR(20) NOT NULL COMMENT '预警类型：LOW_SCORE-评分过低/DECLINE-评分下降/COMPLAINT_BURST-投诉增多/BAD_REVIEW_BURST-差评集中',
    provider_id VARCHAR(32) COMMENT '服务商ID',
    staff_id VARCHAR(32) COMMENT '服务人员ID',
    elder_id VARCHAR(32) COMMENT '老人ID',
    order_id VARCHAR(32) COMMENT '关联订单ID',
    evaluation_id VARCHAR(32) COMMENT '关联评价ID',
    severity VARCHAR(10) NOT NULL COMMENT '严重程度：LOW/MEDIUM/HIGH/CRITICAL',
    alert_content TEXT COMMENT '预警内容',
    alert_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '预警状态：PENDING-待处理/HANDLING-处理中/HANDLED-已处理/IGNORED-已忽略',
    handler_id VARCHAR(32) COMMENT '处理人ID',
    handler_name VARCHAR(100) COMMENT '处理人姓名',
    handle_result TEXT COMMENT '处理结果',
    handle_time DATETIME COMMENT '处理时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT(1) DEFAULT 0,
    INDEX idx_provider (provider_id),
    INDEX idx_staff (staff_id),
    INDEX idx_status (alert_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质量预警表';

-- =============================================
-- 6. 质量改进表 t_quality_improvement
-- =============================================
CREATE TABLE IF NOT EXISTS t_quality_improvement (
    improvement_id VARCHAR(32) PRIMARY KEY COMMENT '改进ID',
    title VARCHAR(200) NOT NULL COMMENT '改进标题',
    problem_description TEXT COMMENT '问题描述',
    root_cause TEXT COMMENT '根本原因',
    improvement_plan TEXT COMMENT '改进计划',
    measures TEXT COMMENT '改进措施',
    responsible_person VARCHAR(100) COMMENT '责任人',
    responsible_phone VARCHAR(20) COMMENT '责任人电话',
    provider_id VARCHAR(32) COMMENT '关联服务商ID',
    staff_id VARCHAR(32) COMMENT '关联服务人员ID',
    alert_id VARCHAR(32) COMMENT '关联预警ID',
    start_date DATE COMMENT '开始日期',
    target_date DATE COMMENT '目标完成日期',
    completion_date DATE COMMENT '实际完成日期',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理/IN_PROGRESS-进行中/COMPLETED-已完成/EVALUATED-已评估',
    effect_evaluation TEXT COMMENT '效果评估',
    evaluator_id VARCHAR(32) COMMENT '评估人ID',
    evaluator_name VARCHAR(100) COMMENT '评估人姓名',
    evaluate_time DATETIME COMMENT '评估时间',
    creator_id VARCHAR(32) COMMENT '创建人ID',
    creator_name VARCHAR(100) COMMENT '创建人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) DEFAULT 0,
    INDEX idx_provider (provider_id),
    INDEX idx_staff (staff_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质量改进表';

-- =============================================
-- 7. 服务争议表 t_service_dispute
-- =============================================
CREATE TABLE IF NOT EXISTS t_service_dispute (
    dispute_id VARCHAR(32) PRIMARY KEY COMMENT '争议ID',
    dispute_no VARCHAR(64) NOT NULL COMMENT '争议编号',
    order_id VARCHAR(32) COMMENT '关联订单ID',
    elder_id VARCHAR(32) COMMENT '老人ID',
    staff_id VARCHAR(32) COMMENT '服务人员ID',
    provider_id VARCHAR(32) COMMENT '服务商ID',
    dispute_type VARCHAR(50) NOT NULL COMMENT '争议类型：SERVICE_QUALITY-服务质量/DAMAGE-财产损失/REFUND-退款申请/OTHER-其他',
    dispute_status VARCHAR(20) DEFAULT 'APPLIED' COMMENT '争议状态：APPLIED-已申请/INVESTIGATING-调查中/MEDIATING-调解中/AGREED-已协议/CLOSED-已关闭',
    applicant_name VARCHAR(100) COMMENT '申请人姓名',
    applicant_phone VARCHAR(20) COMMENT '申请人电话',
    application_content TEXT COMMENT '申请内容',
    applied_time DATETIME COMMENT '申请时间',
    investigation_content TEXT COMMENT '调查结果',
    investigator_id VARCHAR(32) COMMENT '调查人ID',
    investigator_name VARCHAR(100) COMMENT '调查人姓名',
    investigated_time DATETIME COMMENT '调查时间',
    mediation_content TEXT COMMENT '调解内容',
    mediator_id VARCHAR(32) COMMENT '调解人ID',
    mediator_name VARCHAR(100) COMMENT '调解人姓名',
    mediated_time DATETIME COMMENT '调解时间',
    agreement_content TEXT COMMENT '协议内容',
    agreed_time DATETIME COMMENT '协议时间',
    close_reason VARCHAR(500) COMMENT '关闭原因',
    closed_time DATETIME COMMENT '关闭时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) DEFAULT 0,
    INDEX idx_order (order_id),
    INDEX idx_provider (provider_id),
    INDEX idx_status (dispute_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务争议表';

-- =============================================
-- 验证脚本
-- =============================================
-- SELECT 'service_log签到签退字段' AS check_item,
--        GROUP_CONCAT(COLUMN_NAME) AS columns
-- FROM INFORMATION_SCHEMA.COLUMNS
-- WHERE TABLE_SCHEMA = 'elderly_care' AND TABLE_NAME = 'service_log'
--       AND COLUMN_NAME IN ('departure_time','sign_in_time','sign_in_location','sign_in_photos','sign_out_time','sign_out_location','sign_out_photos');

-- SELECT 't_service_evaluation回复和环境字段' AS check_item,
--        GROUP_CONCAT(COLUMN_NAME) AS columns
-- FROM INFORMATION_SCHEMA.COLUMNS
-- WHERE TABLE_SCHEMA = 'elderly_care' AND TABLE_NAME = 't_service_evaluation'
--       AND COLUMN_NAME IN ('environment_score','reply_content','reply_time','replyer_id','replyer_name');

-- SELECT '新增表检查' AS check_item, COUNT(*) AS table_count
-- FROM INFORMATION_SCHEMA.TABLES
-- WHERE TABLE_SCHEMA = 'elderly_care' AND TABLE_NAME IN ('t_provider_score','t_staff_score','t_quality_alert','t_quality_improvement','t_service_dispute');
