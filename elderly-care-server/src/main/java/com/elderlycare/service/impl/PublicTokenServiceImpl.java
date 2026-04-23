package com.elderlycare.service.impl;

import com.elderlycare.common.IDGenerator;
import com.elderlycare.entity.PublicToken;
import com.elderlycare.mapper.PublicTokenMapper;
import com.elderlycare.service.PublicTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 公开Token服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublicTokenServiceImpl implements PublicTokenService {

    private static final long DEFAULT_EXPIRES_IN_SECONDS = 86400; // 24小时

    private final PublicTokenMapper publicTokenMapper;

    @Override
    public String generateToken(String type, long expiresInSeconds) {
        String token = UUID.randomUUID().toString().replace("-", "");

        PublicToken publicToken = new PublicToken();
        publicToken.setTokenId(IDGenerator.generateId());
        publicToken.setToken(token);
        publicToken.setType(type);
        publicToken.setExpiresAt(LocalDateTime.now().plusSeconds(
                expiresInSeconds > 0 ? expiresInSeconds : DEFAULT_EXPIRES_IN_SECONDS));
        publicToken.setCreateTime(LocalDateTime.now());
        publicToken.setStatus("ACTIVE");

        publicTokenMapper.insert(publicToken);
        log.info("生成公开Token: type={}, expiresAt={}", type, publicToken.getExpiresAt());

        return token;
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        PublicToken publicToken = publicTokenMapper.selectByToken(token);
        return publicToken != null && "ACTIVE".equals(publicToken.getStatus());
    }

    @Override
    public void revokeToken(String token) {
        publicTokenMapper.revokeToken(token);
        log.info("撤销公开Token: {}", token);
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点清理
    public void cleanupExpiredTokens() {
        publicTokenMapper.deleteExpiredTokens(LocalDateTime.now());
        log.info("清理过期公开Token完成");
    }
}
