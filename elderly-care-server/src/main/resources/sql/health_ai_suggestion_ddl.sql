-- t_health_ai_suggestion 表（健康档案AI建议）
-- 用于存储阿里云百炼生成的护理建议和就医建议，每日定时生成，前端直接读缓存
CREATE TABLE IF NOT EXISTS `t_health_ai_suggestion` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `elder_id` VARCHAR(64) NOT NULL COMMENT '老人ID',
  `elder_name` VARCHAR(100) COMMENT '老人姓名（冗存）',
  -- 护理建议字段
  `care_level_suggestion` VARCHAR(500) COMMENT '护理等级建议',
  `risk_alerts` JSON COMMENT '风险预警JSON数组',
  `care_suggestions` JSON COMMENT '护理建议JSON数组',
  `care_evaluate_time` DATETIME COMMENT '护理建议生成时间',
  -- 就医建议字段
  `urgency_level` VARCHAR(20) COMMENT 'URGENT/WARNING/ATTENTION/NORMAL',
  `urgency_level_name` VARCHAR(50) COMMENT '紧急程度名称',
  `suggested_department` VARCHAR(200) COMMENT '建议就诊科室',
  `medical_suggestions` JSON COMMENT '就医建议JSON数组',
  `symptoms` JSON COMMENT '异常症状JSON数组',
  `medical_evaluate_time` DATETIME COMMENT '就医建议生成时间',
  -- AI模型信息
  `ai_model` VARCHAR(100) COMMENT '调用的AI模型',
  `ai_tokens_used` INT COMMENT 'Token消耗量',
  -- 语音播报
  `audio_url` MEDIUMTEXT COMMENT 'AI建议语音URL（qwen-tts合成，base64 data URI）',
  -- 元数据
  `generated_at` DATETIME COMMENT 'AI生成时间',
  `expires_at` DATETIME COMMENT '过期时间（生成时间+7天）',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-有效，1-已过期，2-生成失败',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_elder_id` (`elder_id`),
  KEY `idx_status_deleted` (`status`, `deleted`),
  KEY `idx_elder_status` (`elder_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康AI建议表';
