-- 服务日志健康字段扩展
-- 为 service_log 表添加健康观察和给药记录字段

USE elderly_care;
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;

ALTER TABLE service_log
ADD COLUMN IF NOT EXISTS health_observations VARCHAR(1024) DEFAULT NULL COMMENT '健康观察备注' AFTER review_time,
ADD COLUMN IF NOT EXISTS medication_given VARCHAR(1024) DEFAULT NULL COMMENT '本次给药记录' AFTER health_observations;
