package com.elderlycare.mapper.elder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.elder.ElderDemand;
import org.apache.ibatis.annotations.Mapper;

/**
 * 老人服务需求Mapper
 */
@Mapper
public interface ElderDemandMapper extends BaseMapper<ElderDemand> {
}
