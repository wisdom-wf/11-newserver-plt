package com.elderlycare.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.order.OrderQueryDTO;
import com.elderlycare.entity.order.Order;
import com.elderlycare.vo.order.OrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

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

    // ==================== 统计方法 ====================

    @Select("SELECT COUNT(*) FROM t_order WHERE DATE(create_time) = CURDATE()")
    Integer countToday();

    @Select("SELECT COUNT(*) FROM t_order WHERE YEAR(create_time) = YEAR(CURDATE()) AND MONTH(create_time) = MONTH(CURDATE())")
    Integer countMonth();

    @Select("SELECT COUNT(*) FROM t_order WHERE status = 'CREATED'")
    Integer countPendingDispatch();

    @Select("SELECT COUNT(*) FROM t_order WHERE status = 'DISPATCHED'")
    Integer countDispatched();

    @Select("SELECT COUNT(*) FROM t_order WHERE status = 'RECEIVED'")
    Integer countReceived();

    @Select("SELECT COUNT(*) FROM t_order WHERE status = 'SERVICE_STARTED'")
    Integer countInService();

    @Select("SELECT COUNT(*) FROM t_order WHERE status IN ('COMPLETED', 'EVALUATED', 'SETTLED')")
    Integer countCompleted();

    @Select("SELECT COUNT(*) FROM t_order WHERE status IN ('CANCELLED', 'REJECTED')")
    Integer countCancelled();

    @Select("SELECT COALESCE(SUM(estimated_price), 0) FROM t_order WHERE deleted = 0")
    BigDecimal sumEstimatedPrice();

    @Select("SELECT COALESCE(SUM(subsidy_amount), 0) FROM t_order WHERE deleted = 0")
    BigDecimal sumSubsidyAmount();

    @Select("SELECT COALESCE(SUM(self_pay_amount), 0) FROM t_order WHERE deleted = 0")
    BigDecimal sumSelfPayAmount();

    @Select("SELECT s.staff_id as staffId, s.staff_name as staffName, p.provider_name as providerName, " +
            "COUNT(o.order_id) as orderCount, " +
            "SUM(CASE WHEN o.status IN ('COMPLETED', 'EVALUATED', 'SETTLED') THEN 1 ELSE 0 END) as completedCount " +
            "FROM t_order o " +
            "LEFT JOIN t_staff s ON o.staff_id = s.staff_id " +
            "LEFT JOIN t_provider p ON s.provider_id = p.provider_id " +
            "GROUP BY s.staff_id, s.staff_name, p.provider_name " +
            "HAVING orderCount > 0 " +
            "ORDER BY completedCount DESC, orderCount DESC " +
            "LIMIT 10")
    List<OrderStatisticsVO.StaffRanking> getStaffRankings();
}
