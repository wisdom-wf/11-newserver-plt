USE elderly_care;
SET NAMES utf8mb4;

-- 用户表新增staff_id字段，关联服务人员
ALTER TABLE t_user ADD COLUMN staff_id VARCHAR(64) DEFAULT NULL COMMENT '关联的服务人员ID'
  AFTER provider_id;

-- 已有STAFF角色用户手动关联（按phone匹配）
-- UPDATE t_user u INNER JOIN t_staff s ON u.phone = s.phone SET u.staff_id = s.staff_id WHERE u.user_type = 'STAFF';
