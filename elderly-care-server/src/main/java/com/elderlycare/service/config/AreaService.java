package com.elderlycare.service.config;

import com.elderlycare.dto.config.AreaDTO;
import com.elderlycare.entity.config.Area;
import com.elderlycare.vo.config.AreaTreeVO;
import com.elderlycare.vo.config.AreaVO;
import java.util.List;

/**
 * 区域Service接口
 */
public interface AreaService {

    /**
     * 区域树查询
     */
    List<AreaTreeVO> getAreaTree();

    /**
     * 区域列表
     */
    List<Area> listAreas();

    /**
     * 区域详情
     */
    AreaVO getAreaById(String areaId);

    /**
     * 区域新增
     */
    String createArea(AreaDTO dto);

    /**
     * 区域修改
     */
    void updateArea(String areaId, AreaDTO dto);

    /**
     * 区域删除
     */
    void deleteArea(String areaId);
}
