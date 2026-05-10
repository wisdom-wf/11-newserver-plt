-- ============================================================
-- 健康AI建议表：存储阿里云百炼生成的护理建议和就医建议
-- 每日凌晨由定时任务批量生成，写入数据库后前端直接读缓存
-- ============================================================

CREATE TABLE IF NOT EXISTS t_health_ai_suggestion (
    id              BIGINT          AUTO_INCREMENT  PRIMARY KEY COMMENT '主键',
    elder_id        VARCHAR(32)     NOT NULL        COMMENT '老人ID',
    elder_name      VARCHAR(64)     NOT NULL        COMMENT '老人姓名（冗存，查询方便）',

    -- 护理建议（护理建议VO字段）
    care_level_suggestion   VARCHAR(255)                    COMMENT '护理等级建议',
    risk_alerts              TEXT                            COMMENT '风险预警JSON数组',
    care_suggestions         TEXT                            COMMENT '护理建议JSON数组[{type,typeName,content,priority,basis}]',
    care_evaluate_time       DATETIME                        COMMENT '护理建议生成时间',

    -- 就医建议（就医建议VO字段）
    urgency_level            VARCHAR(32)                     COMMENT '就医紧急程度：URGENT/WARNING/ATTENTION/NORMAL',
    urgency_level_name       VARCHAR(64)                     COMMENT '紧急程度名称',
    suggested_department     VARCHAR(64)                     COMMENT '建议就诊科室',
    medical_suggestions      TEXT                            COMMENT '就医建议JSON数组[{type,typeName,content,priority,basis}]',
    symptoms                 TEXT                            COMMENT '异常症状JSON数组',
    medical_evaluate_time    DATETIME                        COMMENT '就医建议生成时间',

    -- AI模型信息
    ai_model                 VARCHAR(64)     DEFAULT 'qwen-plus'   COMMENT '调用的AI模型',
    ai_tokens_used           INT            DEFAULT 0             COMMENT 'Token消耗量',

    -- 元数据
    generated_at             DATETIME       NOT NULL               COMMENT 'AI生成时间',
    expires_at               DATETIME       NOT NULL               COMMENT '过期时间（生成时间+7天）',
    status                   TINYINT        DEFAULT 0              COMMENT '状态：0-有效，1-已过期，2-生成失败',

    -- 软删除
    deleted                  TINYINT        DEFAULT 0              COMMENT '是否删除',
    create_time              DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time              DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引
    INDEX idx_elder_id (elder_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康AI建议缓存表';