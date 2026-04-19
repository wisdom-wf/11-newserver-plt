package com.elderlycare.mapper.elder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.elder.HealthMeasurement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康测量记录Mapper
 */
@Mapper
public interface HealthMeasurementMapper extends BaseMapper<HealthMeasurement> {
}
