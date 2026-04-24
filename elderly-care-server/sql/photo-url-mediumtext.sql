-- photo-url-mediumtext.sql
-- 修复 t_staff.photo_url 和 t_elder.photo_url 字段长度不足问题
-- Base64编码的图片DataURL可能超过varchar(500)限制
-- 执行时间: 2026-04-24
-- 执行人: DevOps

USE elderly_care;
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;

-- 确认当前字段类型
-- SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH
-- FROM INFORMATION_SCHEMA.COLUMNS
-- WHERE TABLE_SCHEMA = 'elderly_care'
--   AND TABLE_NAME IN ('t_staff', 't_elder')
--   AND COLUMN_NAME = 'photo_url';

-- 修改 t_staff 表的 photo_url 字段
ALTER TABLE t_staff
MODIFY COLUMN photo_url MEDIUMTEXT COMMENT '照片URL/Base64';

-- 修改 t_elder 表的 photo_url 字段
ALTER TABLE t_elder
MODIFY COLUMN photo_url MEDIUMTEXT COMMENT '照片URL/Base64';

-- 验证修改结果
-- SHOW FULL COLUMNS FROM t_staff WHERE Field = 'photo_url';
-- SHOW FULL COLUMNS FROM t_elder WHERE Field = 'photo_url';
