package com.elderlycare.mapper.elder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.elder.HealthMeasurement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 健康测量记录Mapper
 */
@Mapper
public interface HealthMeasurementMapper extends BaseMapper<HealthMeasurement> {

    @Select("SELECT * FROM t_health_measurement WHERE elder_id = #{elderId} AND deleted = 0 ORDER BY measured_at DESC LIMIT #{limit}")
    List<HealthMeasurement> selectByElderIdWithLimit(String elderId, int limit);
}
