package com.elderlycare.service.provider;

import com.elderlycare.common.PageResult;
import com.elderlycare.entity.provider.ProviderScore;

import java.util.List;

/**
 * 服务商评分服务接口
 */
public interface ProviderScoreService {

    /**
     * 创建或更新服务商评分
     */
    void saveOrUpdateScore(String providerId, String periodType);

    /**
     * 获取服务商评分列表
     */
    PageResult<ProviderScore> getScoreList(String providerId, String periodType, int page, int pageSize);

    /**
     * 获取服务商最新评分
     */
    ProviderScore getLatestScore(String providerId);

    /**
     * 获取服务商评分趋势
     */
    List<ProviderScore> getScoreTrend(String providerId, String periodType, int months);
}
