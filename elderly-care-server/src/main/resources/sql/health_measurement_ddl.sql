-- 健康测量记录表 DDL
USE elderly_care;
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;

-- 创建健康测量记录表
CREATE TABLE IF NOT EXISTS t_health_measurement (
  measurement_id VARCHAR(32) PRIMARY KEY COMMENT '测量记录ID',
  elder_id VARCHAR(32) NOT NULL COMMENT '老人ID',
  service_log_id VARCHAR(32) COMMENT '服务日志ID（可选，关联服务）',
  measurement_type VARCHAR(32) NOT NULL COMMENT '测量类型：BLOOD_PRESSURE-血压, BLOOD_GLUCOSE-血糖, WEIGHT-体重, TEMPERATURE-体温, PULSE-脉搏, SPO2-血氧, PAIN_SCALE-疼痛指数, OTHER-其他',
  measurement_value VARCHAR(64) NOT NULL COMMENT '测量值',
  measurement_unit VARCHAR(32) COMMENT '单位',
  measured_at DATETIME NOT NULL COMMENT '测量时间',
  staff_id VARCHAR(32) COMMENT '测量人员ID',
  staff_name VARCHAR(50) COMMENT '测量人员姓名',
  remark VARCHAR(512) COMMENT '备注',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  INDEX idx_elder_id (elder_id),
  INDEX idx_service_log_id (service_log_id),
  INDEX idx_measurement_type (measurement_type),
  INDEX idx_measured_at (measured_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='健康测量记录表';
