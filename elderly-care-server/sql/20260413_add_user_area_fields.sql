-- 迁移脚本: 为t_user表添加区域和服务商字段
-- 日期: 2026-04-13
-- 说明: 前端dingfeng-work需要userInfo包含areaId/providerId等字段

-- 添加区域和服务商字段到t_user表
ALTER TABLE t_user
    ADD COLUMN IF NOT EXISTS area_id VARCHAR(32) COMMENT '区域ID' AFTER status,
    ADD COLUMN IF NOT EXISTS area_code VARCHAR(50) COMMENT '区域编码' AFTER area_id,
    ADD COLUMN IF NOT EXISTS area_name VARCHAR(100) COMMENT '区域名称' AFTER area_code,
    ADD COLUMN IF NOT EXISTS provider_id VARCHAR(32) COMMENT '服务商ID' AFTER area_name,
    ADD COLUMN IF NOT EXISTS provider_name VARCHAR(200) COMMENT '服务商名称' AFTER provider_id;

-- 为现有管理员用户设置默认区域(可选)
-- UPDATE t_user SET area_id = 'default', area_name = '系统管理员' WHERE user_type = 'SUPER_ADMIN';

SELECT 'Migration completed: Added area_id, area_code, area_name, provider_id, provider_name to t_user' AS result;
