package com.elderlycare.service.cockpit;

import com.elderlycare.vo.cockpit.CockpitOverviewVO;

/**
 * 驾驶舱Service接口
 */
public interface CockpitService {

    /**
     * 获取驾驶舱概览数据
     */
    CockpitOverviewVO getOverview();
}
