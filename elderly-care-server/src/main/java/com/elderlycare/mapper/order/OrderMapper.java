package com.elderlycare.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.order.OrderQueryDTO;
import com.elderlycare.entity.order.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 分页查询订单
     */
    IPage<Order> selectOrderPage(Page<?> page, @Param("query") OrderQueryDTO query);

    /**
     * 根据订单号查询
     */
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据ID查询订单（带服务商和服务人员名称）
     */
    Order selectByIdWithNames(@Param("orderId") String orderId);
}
