-- 预约二维码Token字段
-- 2026-05-05
ALTER TABLE appointment
  ADD COLUMN appointment_token VARCHAR(64) DEFAULT NULL COMMENT '预约二维码Token',
  ADD COLUMN token_status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'Token状态: ACTIVE/EXPIRED',
  ADD COLUMN token_expire_time DATETIME DEFAULT NULL COMMENT 'Token过期时间(默认30天)',
  ADD UNIQUE INDEX idx_appointment_token (appointment_token);
