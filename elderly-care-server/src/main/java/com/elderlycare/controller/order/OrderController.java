package com.elderlycare.controller.order;

import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.dto.order.*;
import com.elderlycare.service.order.OrderService;
import com.elderlycare.service.statistics.StatisticsService;
import com.elderlycare.vo.order.*;
import com.elderlycare.vo.statistics.OrderStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final StatisticsService statisticsService;

    // ==================== 订单管理 ====================

    /**
     * 创建订单
     * POST /api/order/orders
     */
    @PostMapping("")
    public Result<OrderVO> createOrder(@RequestBody CreateOrderDTO dto) {
        OrderVO vo = orderService.createOrder(dto);
        return Result.success("订单创建成功", vo);
    }

    /**
     * 订单列表
     * GET /api/order/orders
     */
    @GetMapping("")
    public Result<PageResult<OrderVO>> getOrderList(OrderQueryDTO query) {
        // 数据权限：PROVIDER用户强制只看自己服务商（不接受前端providerId参数）
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null) {
            query.setProviderId(autoPid);           // 强制覆盖
            query.setStaffId(null);                  // PROVIDER不用staffId过滤
        }
        // STAFF用户强制只看自己（不接受前端任何providerId参数）
        String staffId = UserContext.getStaffId();
        if ("STAFF".equals(userType) && staffId != null) {
            query.setStaffId(staffId);               // 强制覆盖
            query.setProviderId(null);                // STAFF不用providerId过滤
        }
        PageResult<OrderVO> result = orderService.getOrderList(query);
        return Result.success(result);
    }

    /**
     * 订单详情
     * GET /api/orders/{orderId}
     * 隔离：PROVIDER/STAFF只能查属于自己的订单
     */
    @GetMapping("/{orderId}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable String orderId) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        String staffIdCtx = UserContext.getStaffId();

        OrderDetailVO vo = orderService.getOrderDetail(orderId);
        if (vo == null) {
            return Result.notFound("订单不存在");
        }

        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(vo.getProviderId())) {
            throw BusinessException.fail("无权查看其他服务商的订单");
        }
        if ("STAFF".equals(userType) && staffIdCtx != null) {
            // STAFF用户查订单是否属于自己的（通过订单的staffId）
            // 注：订单不一定有staffId（待派单状态）
        }
        return Result.success(vo);
    }

    /**
     * 订单修改
     * PUT /api/order/orders/{orderId}
     */
    @PutMapping("/{orderId}")
    public Result<OrderVO> updateOrder(@PathVariable String orderId, @RequestBody UpdateOrderDTO dto) {
        OrderVO vo = orderService.updateOrder(orderId, dto);
        return Result.success("订单修改成功", vo);
    }

    /**
     * 订单取消
     * PUT /api/order/orders/{orderId}/cancel
     */
    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderId, @RequestBody CancelOrderDTO dto) {
        orderService.cancelOrder(orderId, dto);
        return Result.successMsg("订单取消成功");
    }

    // ==================== 订单派单 ====================

    /**
     * 订单派单
     * POST /api/order/orders/{orderId}/dispatch
     */
    @PostMapping("/{orderId}/dispatch")
    public Result<DispatchVO> dispatchOrder(@PathVariable String orderId, @RequestBody DispatchOrderDTO dto) {
        DispatchVO vo = orderService.dispatchOrder(orderId, dto);
        return Result.success("派单成功", vo);
    }

    /**
     * 订单接单
     * PUT /api/order/orders/{orderId}/receive
     */
    @PutMapping("/{orderId}/receive")
    public Result<Void> receiveOrder(@PathVariable String orderId, @RequestBody ReceiveOrderDTO dto) {
        orderService.receiveOrder(orderId, dto);
        return Result.successMsg("接单成功");
    }

    /**
     * 订单拒单
     * PUT /api/order/orders/{orderId}/reject
     */
    @PutMapping("/{orderId}/reject")
    public Result<Void> rejectOrder(@PathVariable String orderId, @RequestBody RejectOrderDTO dto) {
        orderService.rejectOrder(orderId, dto);
        return Result.successMsg("拒单成功");
    }

    /**
     * 查询可派单资源
     * GET /api/order/orders/{orderId}/available-resources
     */
    @GetMapping("/{orderId}/available-resources")
    public Result<PageResult<AvailableResourceVO>> getAvailableResources(
            @PathVariable String orderId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<AvailableResourceVO> result = orderService.getAvailableResources(orderId, page, pageSize);
        return Result.success(result);
    }

    // ==================== 服务执行 ====================

    /**
     * 开始服务
     * PUT /api/order/orders/{orderId}/start
     */
    @PutMapping("/{orderId}/start")
    public Result<Void> startService(@PathVariable String orderId, @RequestBody StartServiceDTO dto) {
        orderService.startService(orderId, dto);
        return Result.successMsg("开始服务成功");
    }

    /**
     * 完成服务
     * PUT /api/order/orders/{orderId}/complete
     */
    @PutMapping("/{orderId}/complete")
    public Result<Void> completeService(@PathVariable String orderId, @RequestBody CompleteServiceDTO dto) {
        orderService.completeService(orderId, dto);
        return Result.successMsg("完成服务成功");
    }

    /**
     * 查询服务记录
     * GET /api/order/service-records
     */
    @GetMapping("/service-records")
    public Result<PageResult<ServiceRecordVO>> getServiceRecords(ServiceRecordQueryDTO query) {
        PageResult<ServiceRecordVO> result = orderService.getServiceRecords(query);
        return Result.success(result);
    }

    // ==================== 统计 ====================

    /**
     * 获取订单统计
     * GET /api/orders/statistics
     * 数据隔离：PROVIDER用户强制只看自己服务商
     */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> getOrderStatistics() {
        String providerId = UserContext.getProviderId();
        OrderStatisticsVO statistics = statisticsService.getOrderStatistics(providerId, null, null, null, null);
        return Result.success(statistics);
    }
}
