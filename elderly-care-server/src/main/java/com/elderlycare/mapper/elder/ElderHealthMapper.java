package com.elderlycare.mapper.elder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.elder.ElderHealth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 老人健康信息Mapper
 */
@Mapper
public interface ElderHealthMapper extends BaseMapper<ElderHealth> {

    @Select("SELECT * FROM t_elder_health WHERE elder_id = #{elderId} AND deleted = 0 LIMIT 1")
    ElderHealth selectByElderId(String elderId);
}
