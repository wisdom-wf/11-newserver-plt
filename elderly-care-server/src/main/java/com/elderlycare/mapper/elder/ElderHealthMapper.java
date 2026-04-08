package com.elderlycare.mapper.elder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.elder.ElderHealth;
import org.apache.ibatis.annotations.Mapper;

/**
 * 老人健康信息Mapper
 */
@Mapper
public interface ElderHealthMapper extends BaseMapper<ElderHealth> {
}
