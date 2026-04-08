package com.elderlycare.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.order.OrderDispatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单派单记录Mapper
 */
@Mapper
public interface OrderDispatchMapper extends BaseMapper<OrderDispatch> {

    /**
     * 根据订单ID查询派单记录
     */
    List<OrderDispatch> selectByOrderId(@Param("orderId") String orderId);

    /**
     * 查询最新的派单记录
     */
    OrderDispatch selectLatestByOrderId(@Param("orderId") String orderId);
}
