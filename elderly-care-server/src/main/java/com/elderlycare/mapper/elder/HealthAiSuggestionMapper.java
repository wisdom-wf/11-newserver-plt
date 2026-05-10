package com.elderlycare.mapper.elder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.elder.HealthAiSuggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 健康AI建议Mapper
 */
@Mapper
public interface HealthAiSuggestionMapper extends BaseMapper<HealthAiSuggestion> {

    /**
     * 查询老人最新一条有效的AI建议
     */
    @Select("SELECT * FROM t_health_ai_suggestion " +
           "WHERE elder_id = #{elderId} AND status = 0 AND deleted = 0 " +
           "ORDER BY generated_at DESC LIMIT 1")
    HealthAiSuggestion selectLatestByElderId(String elderId);
}