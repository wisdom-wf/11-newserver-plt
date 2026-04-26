package com.elderlycare.mapper.quality;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.quality.QualityAlert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 质量预警Mapper
 */
@Mapper
public interface QualityAlertMapper extends BaseMapper<QualityAlert> {
}
