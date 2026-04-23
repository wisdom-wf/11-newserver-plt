package com.elderlycare.service;

import java.time.LocalDateTime;

/**
 * 公开Token服务接口
 */
public interface PublicTokenService {

    /**
     * 生成公开访问Token
     * @param type Token类型：cockpit-驾驶舱
     * @param expiresInSeconds 过期时间（秒），默认86400（24小时）
     * @return 生成的Token值
     */
    String generateToken(String type, long expiresInSeconds);

    /**
     * 验证Token是否有效
     * @param token Token值
     * @return true-有效，false-无效
     */
    boolean validateToken(String token);

    /**
     * 撤销Token
     * @param token Token值
     */
    void revokeToken(String token);

    /**
     * 清理过期Token
     */
    void cleanupExpiredTokens();
}
