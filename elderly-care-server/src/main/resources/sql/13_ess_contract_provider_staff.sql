-- 合同表增加服务商和服务人员归属字段，用于数据隔离
-- 执行前请备份数据

SET NAMES utf8mb4;
SET character_set_client = utf8mb4;

ALTER TABLE t_ess_contract
  ADD COLUMN provider_id VARCHAR(32) DEFAULT NULL COMMENT '服务商ID' AFTER order_no,
  ADD COLUMN staff_id VARCHAR(32) DEFAULT NULL COMMENT '服务人员ID' AFTER provider_id,
  ADD INDEX idx_provider_id (provider_id),
  ADD INDEX idx_staff_id (staff_id);

-- 同步历史数据：根据订单信息回填 provider_id 和 staff_id
UPDATE t_ess_contract c
JOIN t_order o ON c.order_id = o.order_id
SET c.provider_id = o.provider_id,
    c.staff_id = o.staff_id
WHERE c.provider_id IS NULL OR c.staff_id IS NULL;
