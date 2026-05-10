-- 2026-05-10: 补充 ElderHealth 表缺失字段
-- Entity字段：currentMedication, adlScore, mmseScore, gdsScore, fallRisk, pressureSoreRisk,
--             nutritionStatus, visionStatus, hearingStatus, oralHealth, mobilityStatus, healthRemark

ALTER TABLE t_elder_health
  ADD COLUMN IF NOT EXISTS current_medication TEXT COMMENT '当前用药' AFTER medical_history,
  ADD COLUMN IF NOT EXISTS adl_score INT COMMENT 'ADL日常生活活动能力评分(0-100)' AFTER current_medication,
  ADD COLUMN IF NOT EXISTS mmse_score INT COMMENT 'MMSE简易精神状态量表评分(0-30)' AFTER adl_score,
  ADD COLUMN IF NOT EXISTS gds_score INT COMMENT 'GDS老年抑郁量表评分(0-15)' AFTER mmse_score,
  ADD COLUMN IF NOT EXISTS fall_risk INT COMMENT '跌倒风险: 0低/1中/2高' AFTER gds_score,
  ADD COLUMN IF NOT EXISTS pressure_sore_risk INT COMMENT '压疮风险: 0低/1中/2高' AFTER fall_risk,
  ADD COLUMN IF NOT EXISTS nutrition_status INT COMMENT '营养状态: 0正常/1轻度/2中度/3重度' AFTER pressure_sore_risk,
  ADD COLUMN IF NOT EXISTS vision_status INT COMMENT '视力状态: 0正常/1轻度/2中度/3重度' AFTER nutrition_status,
  ADD COLUMN IF NOT EXISTS hearing_status INT COMMENT '听力状态: 0正常/1轻度/2中度/3重度' AFTER vision_status,
  ADD COLUMN IF NOT EXISTS oral_health INT COMMENT '口腔健康: 0良好/1一般/2较差' AFTER hearing_status,
  ADD COLUMN IF NOT EXISTS mobility_status INT COMMENT '行动能力: 0正常/1轻度/2中度/3重度受限' AFTER oral_health,
  ADD COLUMN IF NOT EXISTS health_remark VARCHAR(1000) COMMENT '健康备注' AFTER mobility_status;