package com.elderlycare.mapper.quality;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.quality.QualityCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 质检Mapper
 */
@Mapper
public interface QualityCheckMapper extends BaseMapper<QualityCheck> {

    @Select("<script>SELECT AVG(check_score) FROM quality_check WHERE deleted = 0 AND check_score IS NOT NULL"
            + "<if test='providerId != null and providerId != &quot;&quot;'> AND provider_id = #{providerId}</if>"
            + "</script>")
    BigDecimal avgCheckScore(@Param("providerId") String providerId);
}
