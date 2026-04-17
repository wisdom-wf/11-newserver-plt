-- ============================================
-- 老人档案表添加服务商ID字段
-- 用于预约确认时自动关联服务商
-- ============================================

USE elderly_care;

-- 添加 provider_id 字段到 t_elder 表
ALTER TABLE t_elder 
ADD COLUMN provider_id VARCHAR(64) DEFAULT NULL COMMENT '关联服务商ID（预约确认时自动填充）';

-- 添加索引
ALTER TABLE t_elder 
ADD INDEX idx_provider_id (provider_id);
