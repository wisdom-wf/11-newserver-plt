package com.elderlycare.service.evaluation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.evaluation.*;
import com.elderlycare.entity.evaluation.CustomerFeedback;
import com.elderlycare.entity.elder.Elder;
import com.elderlycare.entity.order.Order;
import com.elderlycare.mapper.evaluation.CustomerFeedbackMapper;
import com.elderlycare.mapper.elder.ElderMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.service.evaluation.CustomerFeedbackService;
import com.elderlycare.vo.evaluation.FeedbackVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户反馈Service实现
 */
@Service
@RequiredArgsConstructor
public class CustomerFeedbackServiceImpl extends ServiceImpl<CustomerFeedbackMapper, CustomerFeedback>
        implements CustomerFeedbackService {

    private final CustomerFeedbackMapper feedbackMapper;
    private final ElderMapper elderMapper;
    private final OrderMapper orderMapper;

    private static final Map<String, String> FEEDBACK_TYPE_MAP = new HashMap<String, String>() {{
        put("COMPLAINT", "投诉");
        put("SUGGESTION", "建议");
        put("PRAISE", "表扬");
        put("OTHER", "其他");
    }};

    private static final Map<String, String> HANDLE_STATUS_MAP = new HashMap<String, String>() {{
        put("PENDING", "待处理");
        put("PROCESSING", "处理中");
        put("RESOLVED", "已解决");
        put("REJECTED", "已驳回");
    }};

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createFeedback(CreateFeedbackDTO dto) {
        // 查询老人信息
        Elder elder = elderMapper.selectById(dto.getElderId());
        if (elder == null) {
            throw BusinessException.notFound("老人不存在");
        }

        CustomerFeedback feedback = new CustomerFeedback();
        BeanUtils.copyProperties(dto, feedback);

        // 设置老人信息
        feedback.setElderName(elder.getName());
        feedback.setElderPhone(elder.getPhone());
        feedback.setHandleStatus("PENDING"); // 默认待处理
        feedback.setStatus(1); // 默认显示

        // 如果有订单ID，查询订单获取服务商和服务人员信息
        if (StringUtils.isNotBlank(dto.getOrderId())) {
            Order order = orderMapper.selectById(dto.getOrderId());
            if (order != null) {
                feedback.setProviderId(order.getProviderId());
                // providerName需要通过providerId查询获取，此处简化处理
                feedback.setStaffId(order.getStaffId());
            }
        }

        // 如果有服务人员ID，查询服务人员姓名
        if (StringUtils.isNotBlank(dto.getStaffId())) {
            // 这里可以通过StaffMapper查询，此处简化处理
            feedback.setStaffName("");
        }

        feedbackMapper.insert(feedback);
        return feedback.getFeedbackId();
    }

    @Override
    public PageResult<CustomerFeedback> queryFeedbacks(FeedbackQueryDTO dto) {
        Page<CustomerFeedback> page = new Page<>(dto.getPage(), dto.getPageSize());
        IPage<CustomerFeedback> result = feedbackMapper.selectFeedbackPage(page, dto);

        return PageResult.of(result.getTotal(), dto.getPage(), dto.getPageSize(), result.getRecords());
    }

    @Override
    public FeedbackVO getFeedbackById(String feedbackId) {
        CustomerFeedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw BusinessException.notFound("反馈不存在");
        }

        FeedbackVO vo = new FeedbackVO();
        BeanUtils.copyProperties(feedback, vo);

        // 设置类型名称
        vo.setFeedbackTypeName(FEEDBACK_TYPE_MAP.getOrDefault(feedback.getFeedbackType(), feedback.getFeedbackType()));
        vo.setHandleStatusName(HANDLE_STATUS_MAP.getOrDefault(feedback.getHandleStatus(), feedback.getHandleStatus()));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleFeedback(String feedbackId, HandleFeedbackDTO dto) {
        CustomerFeedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw BusinessException.notFound("反馈不存在");
        }

        if (!"PENDING".equals(feedback.getHandleStatus()) && !"PROCESSING".equals(feedback.getHandleStatus())) {
            throw BusinessException.fail("该反馈已处理，无法重复处理");
        }

        feedback.setHandlerId(dto.getHandlerId());
        feedback.setHandlerName(dto.getHandlerName());
        feedback.setHandlingResult(dto.getHandlingResult());
        feedback.setHandleTime(LocalDateTime.now());
        feedback.setHandleStatus("RESOLVED"); // 已解决

        feedbackMapper.updateById(feedback);
    }
}
