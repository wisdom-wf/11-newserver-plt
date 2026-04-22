USE elderly_care;
SET NAMES utf8mb4;

-- 将t_staff表的photo_url字段改为MEDIUMTEXT以支持更长的base64头像URL
ALTER TABLE t_staff MODIFY COLUMN photo_url MEDIUMTEXT COMMENT '头像URL(Base64)';
