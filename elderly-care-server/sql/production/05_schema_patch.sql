-- 05_schema_patch.sql
-- 补齐 t_service_evaluation 缺失列 + service_log 性能优化
-- 执行前请确认: SET NAMES utf8mb4;

USE elderly_care;
SET NAMES utf8mb4;

-- ============================================================
-- 1. t_service_evaluation: 补齐 environment_score, evaluation_images
-- ============================================================

ALTER TABLE t_service_evaluation
  ADD COLUMN `environment_score` int DEFAULT NULL COMMENT '环境评分(1-5)' AFTER `efficiency_score`,
  ADD COLUMN `evaluation_images` varchar(2000) DEFAULT NULL COMMENT '评价图片(多个逗号分隔)' AFTER `evaluation_tags`;

-- ============================================================
-- 2. service_log: 添加 elder_id 索引 + service_status 索引
--    解决列表查询超时风险
-- ============================================================

ALTER TABLE service_log
  ADD INDEX `idx_elder_id` (`elder_id`),
  ADD INDEX `idx_service_status` (`service_status`);
