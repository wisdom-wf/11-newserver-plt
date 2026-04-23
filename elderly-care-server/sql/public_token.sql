-- 公开访问Token表
CREATE TABLE IF NOT EXISTS t_public_token (
    token_id VARCHAR(32) PRIMARY KEY COMMENT 'Token ID',
    token VARCHAR(64) NOT NULL UNIQUE COMMENT 'Token值',
    type VARCHAR(32) NOT NULL DEFAULT 'cockpit' COMMENT 'Token类型：cockpit-驾驶舱',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by VARCHAR(64) COMMENT '创建人',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-有效，REVOKED-已撤销',
    remark VARCHAR(255) COMMENT '备注',
    INDEX idx_token (token),
    INDEX idx_expires_at (expires_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公开访问Token表';
