package com.elderlycare.task;

import com.elderlycare.service.elder.HealthAiSuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 健康AI建议定时生成任务
 * 每天凌晨2点批量为所有有效老人生成AI健康建议
 * 使用 qwen-plus 模型，新人100万Token免费额度
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthAiCronJob {

    private final HealthAiSuggestionService healthAiSuggestionService;

    /**
     * 每天凌晨2点执行
     * 考虑到API限流，每次生成间隔500ms，100个老人约需50秒
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailySuggestions() {
        log.info("========== 开始每日健康AI建议生成任务 ==========");
        long start = System.currentTimeMillis();
        try {
            int count = healthAiSuggestionService.generateAll();
            long elapsed = System.currentTimeMillis() - start;
            log.info("========== 每日AI建议生成完成，成功 {} 条，耗时 {} ms ==========", count, elapsed);
        } catch (Exception e) {
            log.error("每日AI建议生成任务异常: {}", e.getMessage(), e);
        }
    }
}