package com.elderlycare.service.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.order.*;
import com.elderlycare.vo.order.*;

/**
 * 订单服务接口
 */
public interface OrderService {

    // ==================== 订单管理 ====================

    /**
     * 创建订单
     */
    OrderVO createOrder(CreateOrderDTO dto);

    /**
     * 分页查询订单列表
     */
    PageResult<OrderVO> getOrderList(OrderQueryDTO query);

    /**
     * 获取订单详情
     */
    OrderDetailVO getOrderDetail(String orderId);

    /**
     * 更新订单
     */
    OrderVO updateOrder(String orderId, UpdateOrderDTO dto);

    /**
     * 取消订单
     */
    void cancelOrder(String orderId, CancelOrderDTO dto);

    // ==================== 订单派单 ====================

    /**
     * 订单派单
     */
    DispatchVO dispatchOrder(String orderId, DispatchOrderDTO dto);

    /**
     * 订单接单
     */
    void receiveOrder(String orderId, ReceiveOrderDTO dto);

    /**
     * 订单拒单
     */
    void rejectOrder(String orderId, RejectOrderDTO dto);

    /**
     * 查询可派单资源
     */
    PageResult<AvailableResourceVO> getAvailableResources(String orderId, Integer page, Integer pageSize);

    // ==================== 服务执行 ====================

    /**
     * 开始服务
     */
    void startService(String orderId, StartServiceDTO dto);

    /**
     * 完成服务
     */
    void completeService(String orderId, CompleteServiceDTO dto);

    /**
     * 查询服务记录
     */
    PageResult<ServiceRecordVO> getServiceRecords(ServiceRecordQueryDTO query);

    // ==================== 订单统计 ====================

    /**
     * 获取订单统计
     */
    OrderStatisticsVO getStatistics();
}
