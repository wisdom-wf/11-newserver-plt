package com.elderlycare.mapper.quality;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.quality.QualityCheck;
import org.apache.ibatis.annotations.Mapper;

/**
 * 质检Mapper
 */
@Mapper
public interface QualityCheckMapper extends BaseMapper<QualityCheck> {
}
