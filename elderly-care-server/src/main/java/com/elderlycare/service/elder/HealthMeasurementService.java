package com.elderlycare.service.elder;

import com.elderlycare.dto.elder.HealthMeasurementDTO;
import com.elderlycare.entity.elder.HealthMeasurement;
import com.elderlycare.vo.elder.HealthMeasurementStatisticsVO;
import com.elderlycare.vo.elder.HealthMeasurementVO;

import java.util.List;

/**
 * 健康测量记录Service接口
 */
public interface HealthMeasurementService {

    /**
     * 添加健康测量记录
     */
    HealthMeasurement addMeasurement(String elderId, HealthMeasurementDTO dto);

    /**
     * 批量添加健康测量记录
     */
    List<HealthMeasurement> addMeasurements(String elderId, List<HealthMeasurementDTO> dtos);

    /**
     * 获取老人的健康测量历史
     */
    List<HealthMeasurementVO> getMeasurementHistory(String elderId, String measurementType, Integer limit);

    /**
     * 获取老人的最新一次测量记录
     */
    HealthMeasurementVO getLatestMeasurement(String elderId, String measurementType);

    /**
     * 获取老人的最新一次测量记录（所有类型）
     */
    List<HealthMeasurementVO> getLatestMeasurements(String elderId);

    /**
     * 获取老人指定类型测量的统计数据
     */
    HealthMeasurementStatisticsVO getMeasurementStatistics(String elderId, String measurementType);

    /**
     * 获取老人所有类型测量的统计数据
     */
    List<HealthMeasurementStatisticsVO> getAllMeasurementStatistics(String elderId);

    /**
     * 删除测量记录
     */
    void deleteMeasurement(String measurementId);

    /**
     * 计算老人健康指数（0-100）
     * 基于各类型测量的综合评分
     */
    int calculateHealthIndex(String elderId);
}
