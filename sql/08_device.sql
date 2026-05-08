-- 设备管理模块 DDL
-- 创建日期: 2026-05-08

-- 设备表
CREATE TABLE IF NOT EXISTS `t_device` (
  `device_id` VARCHAR(32) NOT NULL COMMENT '设备ID（UUID）',
  `device_sn` VARCHAR(64) NOT NULL COMMENT '设备序列号（二维码内容）',
  `device_type` VARCHAR(32) NOT NULL COMMENT '设备类型：BP-血压计/BG-血糖仪/WT-体重秤/TP-体温计/PL-脉搏仪/SP-血氧仪',
  `device_name` VARCHAR(128) DEFAULT NULL COMMENT '设备名称',
  `manufacturer` VARCHAR(128) DEFAULT NULL COMMENT '制造商',
  `model` VARCHAR(64) DEFAULT NULL COMMENT '型号',
  `status` VARCHAR(16) DEFAULT 'INACTIVE' COMMENT '状态：INACTIVE-未激活/ACTIVE-已激活/OFFLINE-离线/FAULT-故障',
  `last_push_time` DATETIME DEFAULT NULL COMMENT '最后推送时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`device_id`),
  UNIQUE KEY `uk_device_sn` (`device_sn`),
  INDEX `idx_device_type` (`device_type`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备表';

-- 设备绑定表
CREATE TABLE IF NOT EXISTS `t_device_binding` (
  `binding_id` VARCHAR(32) NOT NULL COMMENT '绑定ID',
  `device_id` VARCHAR(32) NOT NULL COMMENT '设备ID',
  `elder_id` VARCHAR(32) NOT NULL COMMENT '客户ID',
  `measurement_type` VARCHAR(32) NOT NULL COMMENT '测量类型：BLOOD_PRESSURE/BLOOD_GLUCOSE/WEIGHT/TEMPERATURE/PULSE/SPO2',
  `bind_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `unbind_time` DATETIME DEFAULT NULL COMMENT '解绑时间',
  `status` VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-已绑定/UNBOUND-已解绑',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`binding_id`),
  INDEX `idx_device_id` (`device_id`),
  INDEX `idx_elder_id` (`elder_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备绑定表';

-- 设备推送记录表
CREATE TABLE IF NOT EXISTS `t_device_push_log` (
  `push_id` VARCHAR(32) NOT NULL COMMENT '推送ID',
  `device_id` VARCHAR(32) DEFAULT NULL COMMENT '设备ID',
  `binding_id` VARCHAR(32) DEFAULT NULL COMMENT '绑定ID',
  `elder_id` VARCHAR(32) DEFAULT NULL COMMENT '客户ID',
  `raw_data` TEXT COMMENT '原始推送数据（JSON）',
  `measurement_id` VARCHAR(32) DEFAULT NULL COMMENT '关联的测量记录ID',
  `push_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '推送时间',
  `process_status` VARCHAR(16) DEFAULT 'PENDING' COMMENT '处理状态：PENDING/SUCCESS/FAILED',
  `error_msg` VARCHAR(512) DEFAULT NULL COMMENT '错误信息',
  PRIMARY KEY (`push_id`),
  INDEX `idx_device_id` (`device_id`),
  INDEX `idx_push_time` (`push_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备推送记录表';
