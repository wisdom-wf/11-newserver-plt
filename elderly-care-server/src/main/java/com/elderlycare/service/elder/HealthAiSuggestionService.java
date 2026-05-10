package com.elderlycare.service.elder;

import com.elderlycare.entity.elder.HealthAiSuggestion;
import com.elderlycare.vo.elder.CareSuggestionVO;
import com.elderlycare.vo.elder.MedicalSuggestionVO;

/**
 * 健康AI建议服务接口
 */
public interface HealthAiSuggestionService {

    /**
     * 获取老人最新的AI健康建议（从数据库缓存读取）
     * 如果缓存不存在或已过期，返回null
     */
    HealthAiSuggestion getLatestSuggestion(String elderId);

    /**
     * 获取护理建议VO（从缓存转换）
     */
    CareSuggestionVO getCareSuggestionVo(String elderId);

    /**
     * 获取就医建议VO（从缓存转换）
     */
    MedicalSuggestionVO getMedicalSuggestionVo(String elderId);

    /**
     * 手动触发某个老人的AI建议生成（管理员操作）
     */
    void generateForElder(String elderId);

    /**
     * 批量生成所有老人的AI建议（定时任务调用）
     * @return 成功生成的数量
     */
    int generateAll();
}