package com.elderlycare.mapper.quality;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.quality.QualityCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 质检Mapper
 */
@Mapper
public interface QualityCheckMapper extends BaseMapper<QualityCheck> {

    @Select("SELECT AVG(check_score) FROM quality_check WHERE deleted = 0 AND check_score IS NOT NULL")
    BigDecimal avgCheckScore();
}
