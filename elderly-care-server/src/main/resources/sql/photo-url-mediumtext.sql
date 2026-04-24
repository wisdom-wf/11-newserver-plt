-- DDL修复: t_staff.photo_url 字段类型
-- 问题: varchar(500) 无法存储 base64 DataURL（照片上传后 base64 字符串超长）
-- 修复: 改为 MEDIUMTEXT，最大支持约16MB文本
-- 执行时间: 2026-04-21（容器内直接执行，未持久化）
-- 持久化时间: 2026-04-24

-- 验证当前类型
-- SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS
--   WHERE TABLE_SCHEMA = 'elderly_care'
--     AND TABLE_NAME = 't_staff'
--     AND COLUMN_NAME = 'photo_url';

ALTER TABLE t_staff
  MODIFY COLUMN photo_url MEDIUMTEXT COMMENT '照片URL(base64或文件路径)';

-- 验证修改结果
-- SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
--   FROM INFORMATION_SCHEMA.COLUMNS
--   WHERE TABLE_SCHEMA = 'elderly_care'
--     AND TABLE_NAME = 't_staff'
--     AND COLUMN_NAME = 'photo_url';
