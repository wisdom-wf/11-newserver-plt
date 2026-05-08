-- 腾讯电子签合同表
-- 创建日期: 2026-05-08

CREATE TABLE IF NOT EXISTS `t_ess_contract` (
  `contract_id` VARCHAR(32) NOT NULL COMMENT '合同ID（UUID）',
  `contract_no` VARCHAR(64) DEFAULT NULL COMMENT '合同编号（业务编号）',
  `order_id` VARCHAR(32) NOT NULL COMMENT '关联订单ID',
  `order_no` VARCHAR(64) DEFAULT NULL COMMENT '订单编号',
  `flow_id` VARCHAR(128) DEFAULT NULL COMMENT '腾讯电子签流程ID',
  `contract_name` VARCHAR(256) NOT NULL COMMENT '合同名称',
  `signers` TEXT COMMENT '签署方信息JSON',
  `status` VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '合同状态：DRAFT/INITIATED/SIGNING/SIGNED/COMPLETED/EXPIRED/REJECTED/CANCELLED',
  `sign_url` VARCHAR(512) DEFAULT NULL COMMENT '签署链接',
  `signed_time` DATETIME DEFAULT NULL COMMENT '签署完成时间',
  `download_url` VARCHAR(512) DEFAULT NULL COMMENT '合同下载链接',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`contract_id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_flow_id` (`flow_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_contract_no` (`contract_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='腾讯电子签合同表';
