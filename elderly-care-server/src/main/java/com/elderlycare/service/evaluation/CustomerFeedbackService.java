package com.elderlycare.service.evaluation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.evaluation.*;
import com.elderlycare.entity.evaluation.CustomerFeedback;
import com.elderlycare.vo.evaluation.FeedbackVO;

/**
 * 客户反馈Service接口
 */
public interface CustomerFeedbackService extends IService<CustomerFeedback> {

    /**
     * 创建反馈
     */
    String createFeedback(CreateFeedbackDTO dto);

    /**
     * 分页查询反馈
     */
    PageResult<CustomerFeedback> queryFeedbacks(FeedbackQueryDTO dto);

    /**
     * 获取反馈详情
     */
    FeedbackVO getFeedbackById(String feedbackId);

    /**
     * 处理反馈
     */
    void handleFeedback(String feedbackId, HandleFeedbackDTO dto);
}
