package com.elderlycare.controller.order;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.dto.order.*;
import com.elderlycare.service.order.OrderService;
import com.elderlycare.vo.order.*;
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
        PageResult<OrderVO> result = orderService.getOrderList(query);
        return Result.success(result);
    }

    /**
     * 订单详情
     * GET /api/order/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable String orderId) {
        OrderDetailVO vo = orderService.getOrderDetail(orderId);
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
        return Result.success("订单取消成功");
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
        return Result.success("接单成功");
    }

    /**
     * 订单拒单
     * PUT /api/order/orders/{orderId}/reject
     */
    @PutMapping("/{orderId}/reject")
    public Result<Void> rejectOrder(@PathVariable String orderId, @RequestBody RejectOrderDTO dto) {
        orderService.rejectOrder(orderId, dto);
        return Result.success("拒单成功");
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
        return Result.success("开始服务成功");
    }

    /**
     * 完成服务
     * PUT /api/order/orders/{orderId}/complete
     */
    @PutMapping("/{orderId}/complete")
    public Result<Void> completeService(@PathVariable String orderId, @RequestBody CompleteServiceDTO dto) {
        orderService.completeService(orderId, dto);
        return Result.success("完成服务成功");
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
}
