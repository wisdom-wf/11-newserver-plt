-- 健康报告表新增封面图片字段
-- 支持AI文生图(wan2.2-t2i-plus)生成的报告封面

ALTER TABLE t_health_report
  ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(500) DEFAULT NULL
  COMMENT '封面图片URL（AI文生图生成）' AFTER pdf_url;

-- 健康AI建议表新增语音URL字段
ALTER TABLE t_health_ai_suggestion
  ADD COLUMN IF NOT EXISTS audio_url VARCHAR(500) DEFAULT NULL
  COMMENT 'AI建议语音播报URL（qwen-tts合成）' AFTER care_suggestions;

-- 新增图片识别记录表（用于OCR/图片理解）
CREATE TABLE IF NOT EXISTS t_image_recognition_log (
  log_id VARCHAR(32) PRIMARY KEY COMMENT '日志ID',
  elder_id VARCHAR(32) COMMENT '关联老人ID',
  image_type VARCHAR(32) COMMENT '图片类型：PRESCRIPTION-药品照片/LAB_REPORT-化验单/MEASUREMENT-测量读数/OTHER-其他',
  original_filename VARCHAR(200) COMMENT '原始文件名',
  image_url VARCHAR(500) COMMENT '图片存储URL',
  recognized_text TEXT COMMENT 'AI识别的原始文本',
  structured_result TEXT COMMENT 'AI解析后的结构化结果(JSON)',
  model VARCHAR(50) COMMENT '调用的AI模型',
  staff_id VARCHAR(32) COMMENT '操作员工ID',
  staff_name VARCHAR(50) COMMENT '操作员工姓名',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  deleted TINYINT DEFAULT 0 COMMENT '是否删除',
  INDEX idx_elder_id (elder_id),
  INDEX idx_image_type (image_type),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图片AI识别记录表';
