package com.elderlycare.service.staff;

import com.elderlycare.common.PageResult;
import com.elderlycare.entity.staff.StaffScore;

import java.util.List;

/**
 * 服务人员评分服务接口
 */
public interface StaffScoreService {

    /**
     * 创建或更新服务人员评分
     */
    void saveOrUpdateScore(String staffId, String periodType);

    /**
     * 获取服务人员评分列表
     */
    PageResult<StaffScore> getScoreList(String staffId, String providerId, String periodType, int page, int pageSize);

    /**
     * 获取服务人员最新评分
     */
    StaffScore getLatestScore(String staffId);

    /**
     * 获取服务人员评分趋势
     */
    List<StaffScore> getScoreTrend(String staffId, String periodType, int months);
}
