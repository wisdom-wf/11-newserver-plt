package com.elderlycare.service.order.impl;

import java.math.BigDecimal;
import java.util.Optional;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.order.*;
import com.elderlycare.entity.order.Order;
import com.elderlycare.entity.order.OrderDispatch;
import com.elderlycare.entity.order.OrderStatus;
import com.elderlycare.entity.order.ServiceRecord;
import com.elderlycare.mapper.order.OrderDispatchMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.mapper.order.ServiceRecordMapper;
import com.elderlycare.mapper.provider.ProviderMapper;
import com.elderlycare.mapper.servicelog.ServiceLogMapper;
import com.elderlycare.mapper.staff.StaffMapper;
import com.elderlycare.mapper.config.ConfigServiceTypeMapper;
import com.elderlycare.entity.provider.Provider;
import com.elderlycare.entity.servicelog.ServiceLog;
import com.elderlycare.entity.staff.Staff;
import com.elderlycare.entity.quality.QualityCheck;
import com.elderlycare.entity.config.ConfigServiceType;
import com.elderlycare.mapper.quality.QualityCheckMapper;
import com.elderlycare.service.order.OrderService;
import com.elderlycare.vo.order.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderDispatchMapper orderDispatchMapper;
    private final ServiceRecordMapper serviceRecordMapper;
    private final ServiceLogMapper serviceLogMapper;
    private final ProviderMapper providerMapper;
    private final StaffMapper staffMapper;
    private final QualityCheckMapper qualityCheckMapper;
    private final ConfigServiceTypeMapper configServiceTypeMapper;

    // ==================== 订单管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderDTO dto) {
        // 生成订单编号
        String orderNo = generateOrderNo();

        // 创建订单实体
        Order order = new Order();
        order.setOrderId(IDGenerator.generateId());
        order.setOrderNo(orderNo);
        order.setElderId(dto.getElderId());
        order.setElderName(dto.getElderName());
        order.setElderPhone(dto.getElderPhone());
        order.setServiceTypeCode(dto.getServiceTypeCode());
        order.setServiceTypeName(dto.getServiceTypeName());
        order.setServiceDate(dto.getServiceDate());
        order.setServiceTime(dto.getServiceTime());
        order.setServiceDuration(dto.getServiceDuration());
        order.setServiceAddress(dto.getServiceAddress());
        order.setSpecialRequirements(dto.getSpecialRequirements());
        order.setOrderType(dto.getOrderType() != null ? dto.getOrderType() : "NORMAL");
        order.setOrderSource(dto.getOrderSource() != null ? dto.getOrderSource() : "COMMUNITY");
        order.setSubsidyType(dto.getSubsidyType() != null ? dto.getSubsidyType() : "SELF_PAY");
        order.setEstimatedPrice(dto.getEstimatedPrice());
        order.setSubsidyAmount(dto.getSubsidyAmount());
        order.setSelfPayAmount(dto.getSelfPayAmount());
        order.setStatus(OrderStatus.CREATED.getCode());
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);

        return convertToOrderVO(order);
    }

    @Override
    public PageResult<OrderVO> getOrderList(OrderQueryDTO query) {
        Page<Order> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<Order> orderPage = orderMapper.selectOrderPage(page, query);

        List<OrderVO> voList = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            voList.add(convertToOrderVO(order));
        }

        return PageResult.of(orderPage.getTotal(), query.getPage(), query.getPageSize(), voList);
    }

    @Override
    public OrderDetailVO getOrderDetail(String orderId) {
        Order order = orderMapper.selectByIdWithNames(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrderId(order.getOrderId());
        vo.setOrderNo(order.getOrderNo());
        vo.setElderId(order.getElderId());
        vo.setElderName(order.getElderName());
        vo.setElderPhone(order.getElderPhone());
        vo.setServiceTypeCode(order.getServiceTypeCode());
        vo.setServiceTypeName(order.getServiceTypeName());
        vo.setServiceDate(order.getServiceDate());
        vo.setServiceTime(order.getServiceTime());
        vo.setServiceDuration(order.getServiceDuration());
        vo.setServiceAddress(order.getServiceAddress());
        vo.setSpecialRequirements(order.getSpecialRequirements());
        vo.setOrderType(order.getOrderType());
        vo.setOrderSource(order.getOrderSource());
        vo.setSubsidyType(order.getSubsidyType());
        vo.setEstimatedPrice(order.getEstimatedPrice());
        // 实际费用 = 自付金额 + 补贴金额
        BigDecimal actualPrice = BigDecimal.ZERO;
        if (order.getSelfPayAmount() != null) {
            actualPrice = actualPrice.add(order.getSelfPayAmount());
        }
        if (order.getSubsidyAmount() != null) {
            actualPrice = actualPrice.add(order.getSubsidyAmount());
        }
        vo.setActualPrice(actualPrice);
        vo.setSubsidyAmount(order.getSubsidyAmount());
        vo.setSelfPayAmount(order.getSelfPayAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusCode(getStatusNumericCode(order.getStatus()));
        vo.setStatusName(getStatusName(order.getStatus()));
        vo.setProviderId(order.getProviderId());
        vo.setProviderName(order.getProviderName());
        vo.setStaffId(order.getStaffId());
        vo.setStaffName(order.getStaffName());
        vo.setStaffPhone(order.getStaffPhone());
        vo.setCancelReason(order.getCancelReason());
        vo.setDispatchTime(order.getDispatchTime());
        vo.setReceiveTime(order.getReceiveTime());
        vo.setStartTime(order.getStartTime());
        vo.setCompleteTime(order.getCompleteTime());
        vo.setCreateTime(order.getCreateTime());

        // 构建时间线
        vo.setTimeline(buildOrderTimeline(order));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO updateOrder(String orderId, UpdateOrderDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 只有待派单状态才能修改
        if (!OrderStatus.CREATED.getCode().equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许修改订单");
        }

        // 更新字段
        if (StringUtils.isNotBlank(dto.getElderName())) {
            order.setElderName(dto.getElderName());
        }
        if (StringUtils.isNotBlank(dto.getElderPhone())) {
            order.setElderPhone(dto.getElderPhone());
        }
        if (StringUtils.isNotBlank(dto.getServiceTypeCode())) {
            order.setServiceTypeCode(dto.getServiceTypeCode());
        }
        if (StringUtils.isNotBlank(dto.getServiceTypeName())) {
            order.setServiceTypeName(dto.getServiceTypeName());
        }
        if (dto.getServiceDate() != null) {
            order.setServiceDate(dto.getServiceDate());
        }
        if (StringUtils.isNotBlank(dto.getServiceTime())) {
            order.setServiceTime(dto.getServiceTime());
        }
        if (dto.getServiceDuration() != null) {
            order.setServiceDuration(dto.getServiceDuration());
        }
        if (StringUtils.isNotBlank(dto.getServiceAddress())) {
            order.setServiceAddress(dto.getServiceAddress());
        }
        if (StringUtils.isNotBlank(dto.getSpecialRequirements())) {
            order.setSpecialRequirements(dto.getSpecialRequirements());
        }
        if (StringUtils.isNotBlank(dto.getOrderType())) {
            order.setOrderType(dto.getOrderType());
        }
        if (StringUtils.isNotBlank(dto.getSubsidyType())) {
            order.setSubsidyType(dto.getSubsidyType());
        }
        if (dto.getEstimatedPrice() != null) {
            order.setEstimatedPrice(dto.getEstimatedPrice());
        }
        if (dto.getSubsidyAmount() != null) {
            order.setSubsidyAmount(dto.getSubsidyAmount());
        }
        if (dto.getSelfPayAmount() != null) {
            order.setSelfPayAmount(dto.getSelfPayAmount());
        }

        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        return convertToOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderId, CancelOrderDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 检查状态是否允许取消
        String status = order.getStatus();
        if (OrderStatus.CANCELLED.getCode().equals(status) ||
            OrderStatus.SERVICE_COMPLETED.getCode().equals(status) ||
            OrderStatus.EVALUATED.getCode().equals(status) ||
            OrderStatus.SETTLED.getCode().equals(status)) {
            throw new BusinessException(400, "当前状态不允许取消订单");
        }

        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelReason(dto.getCancelReason());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    // ==================== 订单派单 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DispatchVO dispatchOrder(String orderId, DispatchOrderDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 只有待派单状态才能派单
        if (!OrderStatus.CREATED.getCode().equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许派单");
        }

        // 校验服务商状态
        if (dto.getProviderId() != null && !dto.getProviderId().isEmpty()) {
            Provider provider = providerMapper.selectById(dto.getProviderId());
            if (provider == null) {
                throw new BusinessException(400, "所选服务商不存在");
            }
            if (!"ENABLED".equals(provider.getStatus())) {
                throw new BusinessException(400, "所选服务商已被禁用，无法派单");
            }
        }

        // 校验服务人员状态（只能派给在职人员）
        if (dto.getStaffId() != null && !dto.getStaffId().isEmpty()) {
            Staff staff = staffMapper.selectStaffById(dto.getStaffId());
            if (staff == null) {
                throw new BusinessException(400, "所选服务人员不存在");
            }
            if (!"ON_JOB".equals(staff.getStatus())) {
                throw new BusinessException(400, "只能派单给在职人员，当前人员状态不允许接单");
            }
        }

        // 创建派单记录
        OrderDispatch dispatch = new OrderDispatch();
        dispatch.setDispatchId(IDGenerator.generateId());
        dispatch.setOrderId(orderId);
        dispatch.setProviderId(dto.getProviderId());
        dispatch.setStaffId(dto.getStaffId());
        dispatch.setDispatchTime(LocalDateTime.now());
        dispatch.setDispatchType(dto.getDispatchType() != null ? dto.getDispatchType() : "MANUAL");
        dispatch.setDispatchStatus("PENDING");
        dispatch.setCreateTime(LocalDateTime.now());
        orderDispatchMapper.insert(dispatch);

        // 更新订单状态
        order.setStatus(OrderStatus.DISPATCHED.getCode());
        order.setProviderId(dto.getProviderId());
        order.setStaffId(dto.getStaffId());
        order.setDispatchTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        return convertToDispatchVO(dispatch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveOrder(String orderId, ReceiveOrderDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 只有已派单状态才能接单
        if (!OrderStatus.DISPATCHED.getCode().equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许接单");
        }

        // 更新派单记录
        OrderDispatch dispatch = orderDispatchMapper.selectLatestByOrderId(orderId);
        if (dispatch != null) {
            dispatch.setDispatchStatus("RECEIVED");
            dispatch.setReceiveTime(LocalDateTime.now());
            dispatch.setReceiveComment(dto.getReceiveComment());
            orderDispatchMapper.updateById(dispatch);
        }

        // 更新订单状态
        order.setStatus(OrderStatus.RECEIVED.getCode());
        order.setReceiveTime(LocalDateTime.now());
        if (StringUtils.isNotBlank(dto.getStaffId())) {
            order.setStaffId(dto.getStaffId());
        }
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectOrder(String orderId, RejectOrderDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 只有已派单状态才能拒单
        if (!OrderStatus.DISPATCHED.getCode().equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许拒单");
        }

        // 更新派单记录
        OrderDispatch dispatch = orderDispatchMapper.selectLatestByOrderId(orderId);
        if (dispatch != null) {
            dispatch.setDispatchStatus("REJECTED");
            dispatch.setRejectReason(dto.getRejectReason());
            orderDispatchMapper.updateById(dispatch);
        }

        // 更新订单状态
        order.setStatus(OrderStatus.REJECTED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public PageResult<AvailableResourceVO> getAvailableResources(String orderId, Integer page, Integer pageSize) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // Query enabled providers
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Provider> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Provider::getStatus, "ENABLED");
        wrapper.eq(Provider::getDeleted, 0);
        List<Provider> providers = providerMapper.selectList(wrapper);

        List<AvailableResourceVO> resources = new ArrayList<>();
        for (Provider p : providers) {
            AvailableResourceVO vo = new AvailableResourceVO();
            vo.setProviderId(p.getProviderId());
            vo.setProviderName(p.getProviderName());
            vo.setProviderAddress(p.getAddress());
            vo.setServiceTypeName(order.getServiceTypeName());
            vo.setRating(p.getRating() != null ? p.getRating().doubleValue() : null);
            resources.add(vo);
        }

        int total = resources.size();
        int fromIndex = Math.min((page - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        return PageResult.of((long) total, page, pageSize, resources.subList(fromIndex, toIndex));
    }

    // ==================== 服务执行 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startService(String orderId, StartServiceDTO dto) {
        Order order = orderMapper.selectByIdWithNames(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 只有已接单状态才能开始服务
        if (!OrderStatus.RECEIVED.getCode().equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许开始服务");
        }

        // 创建服务记录（签到）
        ServiceRecord record = new ServiceRecord();
        record.setServiceRecordId(IDGenerator.generateId());
        record.setOrderId(orderId);
        // 优先使用DTO中的staffId，否则使用订单中已分配的服务人员
        String staffId = StringUtils.isNotBlank(dto.getStaffId()) ? dto.getStaffId() : order.getStaffId();
        record.setStaffId(staffId);
        record.setCheckInTime(LocalDateTime.now());
        record.setCheckInLocation(dto.getLongitude() + "," + dto.getLatitude());
        if (StringUtils.isNotBlank(dto.getCheckInPhoto())) {
            record.setServicePhotos(dto.getCheckInPhoto());
        }
        record.setServiceStatus("CHECKED_IN");
        record.setCreateTime(LocalDateTime.now());
        serviceRecordMapper.insert(record);

        // 自动生成服务日志（草稿状态）
        ServiceLog serviceLog = new ServiceLog();
        serviceLog.setServiceLogId(IDGenerator.generateId());
        serviceLog.setLogNo("SL" + System.currentTimeMillis());
        serviceLog.setOrderId(orderId);
        serviceLog.setOrderNo(order.getOrderNo());
        serviceLog.setElderId(order.getElderId());
        serviceLog.setElderName(order.getElderName());
        serviceLog.setElderPhone(order.getElderPhone());
        serviceLog.setStaffId(staffId);
        serviceLog.setStaffName(order.getStaffName());
        serviceLog.setStaffPhone(order.getStaffPhone());
        serviceLog.setProviderId(order.getProviderId());
        serviceLog.setProviderName(order.getProviderName());
        serviceLog.setServiceTypeCode(order.getServiceTypeCode());
        // 从服务类型字典表查中文名称
        String serviceTypeName = order.getServiceTypeName();
        if (StringUtils.isBlank(serviceTypeName) && StringUtils.isNotBlank(order.getServiceTypeCode())) {
            ConfigServiceType st = configServiceTypeMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ConfigServiceType>()
                    .eq(ConfigServiceType::getServiceTypeCode, order.getServiceTypeCode())
            );
            if (st != null) serviceTypeName = st.getServiceTypeName();
        }
        serviceLog.setServiceTypeName(serviceTypeName);
        serviceLog.setServiceDate(order.getServiceDate() != null ? order.getServiceDate().toString() : null);
        serviceLog.setServiceStartTime(LocalDateTime.now());
        serviceLog.setServiceStatus("IN_PROGRESS"); // 服务中
        serviceLog.setAuditStatus("DRAFT"); // 草稿状态
        serviceLog.setCreateTime(LocalDateTime.now());
        serviceLogMapper.insert(serviceLog);

        // 自动生成质检单（待质检状态）
        QualityCheck qualityCheck = new QualityCheck();
        qualityCheck.setQualityCheckId(IDGenerator.generateId());
        qualityCheck.setCheckNo("QC" + System.currentTimeMillis());
        qualityCheck.setOrderId(orderId);
        qualityCheck.setOrderNo(order.getOrderNo());
        qualityCheck.setServiceLogId(serviceLog.getServiceLogId());
        qualityCheck.setServiceCategory(order.getServiceTypeCode());
        qualityCheck.setProviderId(order.getProviderId());
        qualityCheck.setStaffId(staffId);
        qualityCheck.setCheckType("COMPLETION"); // 完工质检
        qualityCheck.setCheckMethod("PHOTO_REVIEW"); // 默认照片审核
        qualityCheck.setCheckResult("PENDING"); // 待质检
        qualityCheck.setRectifyStatus("PENDING");
        qualityCheck.setCreateTime(LocalDateTime.now());
        qualityCheckMapper.insert(qualityCheck);

        // 更新订单状态
        order.setStatus(OrderStatus.SERVICE_STARTED.getCode());
        order.setStartTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeService(String orderId, CompleteServiceDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 只有服务中状态才能完成服务
        if (!OrderStatus.SERVICE_STARTED.getCode().equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不允许完成服务");
        }

        // 更新服务记录（签退）
        ServiceRecord record = serviceRecordMapper.selectByOrderId(orderId);
        if (record != null) {
            record.setCheckOutTime(LocalDateTime.now());
            record.setCheckOutLocation(dto.getLongitude() + "," + dto.getLatitude());
            record.setServiceLog(dto.getServiceSummary());
            if (dto.getServicePhotos() != null && !dto.getServicePhotos().isEmpty()) {
                record.setServicePhotos(String.join(",", dto.getServicePhotos()));
            }
            // 计算服务时长
            if (record.getCheckInTime() != null) {
                long minutes = java.time.Duration.between(record.getCheckInTime(), record.getCheckOutTime()).toMinutes();
                record.setServiceDuration((int) minutes);
            }
            record.setServiceStatus("COMPLETED");
            serviceRecordMapper.updateById(record);
        }

        // 更新订单状态
        order.setStatus(OrderStatus.SERVICE_COMPLETED.getCode());
        order.setCompleteTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        // 保存实际费用信息
        // 实际费用 = 自付金额 + 补贴金额
        BigDecimal actualFee = dto.getActualServiceFee() != null ? dto.getActualServiceFee() : dto.getActualFee();
        BigDecimal selfPay = dto.getSelfPayAmount();
        BigDecimal subsidy = dto.getSubsidyAmount();

        if (actualFee != null) {
            order.setEstimatedPrice(actualFee);
            // 如果只传了总费用和自付金额，则补贴金额 = 总费用 - 自付金额
            if (subsidy == null && selfPay != null) {
                subsidy = actualFee.subtract(selfPay);
            }
        }
        if (subsidy != null) {
            order.setSubsidyAmount(subsidy);
        }
        if (selfPay != null) {
            order.setSelfPayAmount(selfPay);
        }
        orderMapper.updateById(order);
    }

    @Override
    public PageResult<ServiceRecordVO> getServiceRecords(ServiceRecordQueryDTO query) {
        Page<ServiceRecord> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<ServiceRecord> recordPage = serviceRecordMapper.selectServiceRecordPage(page, query);

        List<ServiceRecordVO> voList = new ArrayList<>();
        for (ServiceRecord record : recordPage.getRecords()) {
            voList.add(convertToServiceRecordVO(record));
        }

        return PageResult.of(recordPage.getTotal(), query.getPage(), query.getPageSize(), voList);
    }

    // ==================== 私有方法 ====================

    private String generateOrderNo() {
        String prefix = "ORD";
        String date = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + date + random;
    }

    private OrderVO convertToOrderVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setOrderId(order.getOrderId());
        vo.setOrderNo(order.getOrderNo());
        vo.setElderId(order.getElderId());
        vo.setElderName(order.getElderName());
        vo.setElderPhone(order.getElderPhone());
        vo.setServiceTypeCode(order.getServiceTypeCode());
        vo.setServiceTypeName(order.getServiceTypeName());
        vo.setServiceDate(order.getServiceDate());
        vo.setServiceTime(order.getServiceTime());
        vo.setServiceDuration(order.getServiceDuration());
        vo.setServiceAddress(order.getServiceAddress());
        vo.setSpecialRequirements(order.getSpecialRequirements());
        vo.setOrderType(order.getOrderType());
        vo.setOrderSource(order.getOrderSource());
        vo.setSubsidyType(order.getSubsidyType());
        vo.setEstimatedPrice(order.getEstimatedPrice());
        vo.setActualPrice(Optional.ofNullable(order.getSubsidyAmount()).orElse(BigDecimal.ZERO)
                .add(Optional.ofNullable(order.getSelfPayAmount()).orElse(BigDecimal.ZERO)));
        vo.setSubsidyAmount(order.getSubsidyAmount());
        vo.setSelfPayAmount(order.getSelfPayAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusCode(getStatusNumericCode(order.getStatus()));
        vo.setStatusName(getStatusName(order.getStatus()));
        vo.setProviderId(order.getProviderId());
        vo.setProviderName(order.getProviderName());
        vo.setStaffId(order.getStaffId());
        vo.setStaffName(order.getStaffName());
        vo.setCancelReason(order.getCancelReason());
        vo.setDispatchTime(order.getDispatchTime());
        vo.setReceiveTime(order.getReceiveTime());
        vo.setStartTime(order.getStartTime());
        vo.setCompleteTime(order.getCompleteTime());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    private DispatchVO convertToDispatchVO(OrderDispatch dispatch) {
        DispatchVO vo = new DispatchVO();
        vo.setDispatchId(dispatch.getDispatchId());
        vo.setOrderId(dispatch.getOrderId());
        vo.setProviderId(dispatch.getProviderId());
        vo.setStaffId(dispatch.getStaffId());
        vo.setDispatchTime(dispatch.getDispatchTime());
        vo.setDispatchType(dispatch.getDispatchType());
        vo.setDispatchStatus(dispatch.getDispatchStatus());
        vo.setReceiveTime(dispatch.getReceiveTime());
        vo.setReceiveComment(dispatch.getReceiveComment());
        vo.setRejectReason(dispatch.getRejectReason());
        vo.setCreateTime(dispatch.getCreateTime());
        return vo;
    }

    private ServiceRecordVO convertToServiceRecordVO(ServiceRecord record) {
        ServiceRecordVO vo = new ServiceRecordVO();
        vo.setServiceRecordId(record.getServiceRecordId());
        vo.setOrderId(record.getOrderId());
        vo.setStaffId(record.getStaffId());
        vo.setCheckInTime(record.getCheckInTime());
        vo.setCheckOutTime(record.getCheckOutTime());
        vo.setServiceDuration(record.getServiceDuration());
        vo.setCheckInLocation(record.getCheckInLocation());
        vo.setCheckOutLocation(record.getCheckOutLocation());
        if (StringUtils.isNotBlank(record.getServicePhotos())) {
            vo.setServicePhotos(List.of(record.getServicePhotos().split(",")));
        }
        vo.setServiceVideo(record.getServiceVideo());
        vo.setServiceLog(record.getServiceLog());
        vo.setAbnormalSituation(record.getAbnormalSituation());
        vo.setElderSignature(record.getElderSignature());
        vo.setServiceStatus(record.getServiceStatus());
        vo.setCreateTime(record.getCreateTime());

        // 补充订单相关信息
        Order order = orderMapper.selectById(record.getOrderId());
        if (order != null) {
            vo.setOrderNo(order.getOrderNo());
            vo.setElderName(order.getElderName());
            vo.setServiceTypeName(order.getServiceTypeName());
        }
        return vo;
    }

    private List<OrderTimelineVO> buildOrderTimeline(Order order) {
        List<OrderTimelineVO> timeline = new ArrayList<>();

        // 创建订单
        OrderTimelineVO created = new OrderTimelineVO();
        created.setStatus(OrderStatus.CREATED.getCode());
        created.setStatusName(OrderStatus.CREATED.getDescription());
        created.setDescription("订单已创建");
        created.setTime(order.getCreateTime());
        created.setOperator("系统");
        timeline.add(created);

        // 派单
        if (order.getDispatchTime() != null) {
            OrderTimelineVO dispatched = new OrderTimelineVO();
            dispatched.setStatus(OrderStatus.DISPATCHED.getCode());
            dispatched.setStatusName(OrderStatus.DISPATCHED.getDescription());
            dispatched.setDescription("订单已派单");
            dispatched.setTime(order.getDispatchTime());
            dispatched.setOperator("管理员");
            timeline.add(dispatched);
        }

        // 接单
        if (order.getReceiveTime() != null) {
            OrderTimelineVO received = new OrderTimelineVO();
            received.setStatus(OrderStatus.RECEIVED.getCode());
            received.setStatusName(OrderStatus.RECEIVED.getDescription());
            received.setDescription("服务人员已接单");
            received.setTime(order.getReceiveTime());
            received.setOperator("服务人员");
            timeline.add(received);
        }

        // 开始服务
        if (order.getStartTime() != null) {
            OrderTimelineVO started = new OrderTimelineVO();
            started.setStatus(OrderStatus.SERVICE_STARTED.getCode());
            started.setStatusName(OrderStatus.SERVICE_STARTED.getDescription());
            started.setDescription("服务已开始");
            started.setTime(order.getStartTime());
            started.setOperator("服务人员");
            timeline.add(started);
        }

        // 完成服务
        if (order.getCompleteTime() != null) {
            OrderTimelineVO completed = new OrderTimelineVO();
            completed.setStatus(OrderStatus.SERVICE_COMPLETED.getCode());
            completed.setStatusName(OrderStatus.SERVICE_COMPLETED.getDescription());
            completed.setDescription("服务已完成");
            completed.setTime(order.getCompleteTime());
            completed.setOperator("服务人员");
            timeline.add(completed);
        }

        // 取消
        if (OrderStatus.CANCELLED.getCode().equals(order.getStatus())) {
            OrderTimelineVO cancelled = new OrderTimelineVO();
            cancelled.setStatus(OrderStatus.CANCELLED.getCode());
            cancelled.setStatusName(OrderStatus.CANCELLED.getDescription());
            cancelled.setDescription("订单已取消: " + order.getCancelReason());
            cancelled.setTime(order.getUpdateTime());
            cancelled.setOperator("用户/管理员");
            timeline.add(cancelled);
        }

        // 拒单
        if (OrderStatus.REJECTED.getCode().equals(order.getStatus())) {
            OrderTimelineVO rejected = new OrderTimelineVO();
            rejected.setStatus(OrderStatus.REJECTED.getCode());
            rejected.setStatusName(OrderStatus.REJECTED.getDescription());
            rejected.setDescription("订单已被拒单");
            rejected.setTime(order.getUpdateTime());
            rejected.setOperator("服务人员");
            timeline.add(rejected);
        }

        return timeline;
    }

    private String getStatusName(String status) {
        if (status == null) return "";
        OrderStatus orderStatus = OrderStatus.fromCode(status);
        return orderStatus != null ? orderStatus.getDescription() : status;
    }

    private Integer getStatusNumericCode(String status) {
        if (status == null) return null;
        OrderStatus orderStatus = OrderStatus.fromCode(status);
        return orderStatus != null ? orderStatus.getNumericCode() : null;
    }
}
