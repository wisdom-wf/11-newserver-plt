-- 健康报告表 DDL
USE elderly_care;
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;

-- 创建健康报告表
CREATE TABLE IF NOT EXISTS t_health_report (
  report_id VARCHAR(32) PRIMARY KEY COMMENT '报告ID',
  elder_id VARCHAR(32) NOT NULL COMMENT '老人ID',
  report_no VARCHAR(50) COMMENT '报告编号',
  report_date DATE COMMENT '报告日期',
  report_type VARCHAR(20) COMMENT '报告类型：MONTHLY-月度报告，QUARTERLY-季度报告，YEARLY-年度报告，SPECIAL-专项报告',
  title VARCHAR(200) COMMENT '报告标题',
  content TEXT COMMENT '报告内容（JSON）',
  pdf_url VARCHAR(500) COMMENT 'PDF存储路径',
  staff_id VARCHAR(32) COMMENT '生成员工ID',
  staff_name VARCHAR(50) COMMENT '生成员工姓名',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  INDEX idx_elder_id (elder_id),
  INDEX idx_report_date (report_date),
  INDEX idx_report_type (report_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='健康报告表';
