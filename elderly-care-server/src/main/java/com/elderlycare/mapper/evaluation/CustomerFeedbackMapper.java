package com.elderlycare.mapper.evaluation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.evaluation.FeedbackQueryDTO;
import com.elderlycare.entity.evaluation.CustomerFeedback;
import org.apache.ibatis.annotations.Param;

/**
 * 客户反馈Mapper接口
 */
public interface CustomerFeedbackMapper extends BaseMapper<CustomerFeedback> {

    /**
     * 分页查询反馈
     */
    IPage<CustomerFeedback> selectFeedbackPage(Page<?> page, @Param("query") FeedbackQueryDTO query);
}
