package com.elderlycare.mapper.evaluation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.evaluation.EvaluationQueryDTO;
import com.elderlycare.entity.evaluation.ServiceEvaluation;
import org.apache.ibatis.annotations.Param;

/**
 * 服务评价Mapper接口
 */
public interface ServiceEvaluationMapper extends BaseMapper<ServiceEvaluation> {

    /**
     * 分页查询评价
     */
    IPage<ServiceEvaluation> selectEvaluationPage(Page<?> page, @Param("query") EvaluationQueryDTO query);
}
