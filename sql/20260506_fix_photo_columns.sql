-- 修复文件存储字段过小问题
-- 执行时间: 2026-05-06
-- 问题: base64编码的图片数据超出text类型(64KB)限制

USE elderly_care;
SET NAMES utf8mb4;

-- service_log 表
ALTER TABLE service_log MODIFY COLUMN service_photos mediumtext COMMENT '服务照片';
ALTER TABLE service_log MODIFY COLUMN anomaly_photos mediumtext COMMENT '异常照片';
ALTER TABLE service_log MODIFY COLUMN sign_in_photos mediumtext COMMENT '签到照片';
ALTER TABLE service_log MODIFY COLUMN sign_out_photos mediumtext COMMENT '签退照片';

-- quality_check 表
ALTER TABLE quality_check MODIFY COLUMN check_photos mediumtext COMMENT '检查照片';
ALTER TABLE quality_check MODIFY COLUMN rectify_photos mediumtext COMMENT '整改照片';

-- t_service_record 表
ALTER TABLE t_service_record MODIFY COLUMN service_photos mediumtext COMMENT '服务照片';

-- t_staff_work_record 表
ALTER TABLE t_staff_work_record MODIFY COLUMN service_photos mediumtext COMMENT '服务照片';

-- t_customer_feedback 表
ALTER TABLE t_customer_feedback MODIFY COLUMN feedback_attachments mediumtext COMMENT '反馈附件';
