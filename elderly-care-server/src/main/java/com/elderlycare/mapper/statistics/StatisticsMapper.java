package com.elderlycare.mapper.statistics;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 数据统计Mapper接口
 */
@Mapper
public interface StatisticsMapper {

    /**
     * 查询老人总数
     */
    @Select("SELECT COUNT(*) FROM t_elder WHERE deleted = 0")
    Long selectTotalElders();

    /**
     * 查询服务商总数
     */
    @Select("SELECT COUNT(*) FROM t_provider WHERE deleted = 0")
    Long selectTotalProviders();

    /**
     * 查询服务人员总数
     */
    @Select("SELECT COUNT(*) FROM t_staff WHERE deleted = 0")
    Long selectTotalStaff();

    /**
     * 查询订单总数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE deleted = 0")
    Long selectTotalOrders();

    /**
     * 查询今日订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE deleted = 0 AND DATE(create_time) = CURDATE()")
    Long selectTodayOrders();

    /**
     * 查询今日已完成订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE deleted = 0 AND DATE(create_time) = CURDATE() AND status = 'COMPLETED'")
    Long selectTodayCompletedOrders();

    /**
     * 查询今日待处理订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE deleted = 0 AND DATE(create_time) = CURDATE() AND status IN ('PENDING', 'DISPATCHED', 'RECEIVED')")
    Long selectTodayPendingOrders();

    /**
     * 查询今日已取消订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE deleted = 0 AND DATE(create_time) = CURDATE() AND status = 'CANCELLED'")
    Long selectTodayCancelledOrders();

    /**
     * 查询本月订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE deleted = 0 AND DATE(create_time) >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)")
    Long selectMonthOrders();

    /**
     * 查询待分配订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE deleted = 0 AND status IN ('PENDING', 'PENDING_DISPATCH')")
    Long selectPendingOrders();

    /**
     * 查询已分配订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE deleted = 0 AND status IN ('ASSIGNED', 'DISPATCHED')")
    Long selectAssignedOrders();

    /**
     * 查询服务中订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE deleted = 0 AND status IN ('IN_SERVICE', 'ACCEPTED', 'RECEIVED')")
    Long selectInServiceOrders();

    /**
     * 查询已完成订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE deleted = 0 AND status = 'COMPLETED'")
    Long selectAllCompletedOrders();

    /**
     * 查询已取消订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE deleted = 0 AND status = 'CANCELLED'")
    Long selectCancelledOrders();

    /**
     * 查询服务类型分布
     */
    @Select("""
        SELECT service_type_code AS serviceTypeCode, service_type_name AS serviceTypeName, COUNT(*) AS orderCount
        FROM t_order WHERE deleted = 0
        GROUP BY service_type_code, service_type_name
        ORDER BY orderCount DESC
        """)
    List<Map<String, Object>> selectServiceTypeDistribution();

    /**
     * 查询近7天订单趋势
     */
    @Select("""
        SELECT DATE(create_time) AS date, COUNT(*) AS orderCount
        FROM t_order WHERE deleted = 0 AND create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
        GROUP BY DATE(create_time)
        ORDER BY date
        """)
    List<Map<String, Object>> selectOrderTrendLast7Days();

    /**
     * 查询TOP服务商
     */
    @Select("""
        SELECT p.provider_id AS providerId, p.provider_name AS providerName,
               COUNT(o.order_id) AS orderCount, p.rating AS rating
        FROM t_provider p
        LEFT JOIN t_order o ON p.provider_id = o.provider_id AND o.deleted = 0
        WHERE p.deleted = 0
        GROUP BY p.provider_id, p.provider_name, p.rating
        ORDER BY orderCount DESC
        LIMIT 10
        """)
    List<Map<String, Object>> selectTopProviders();

    /**
     * 查询本月新增老人数
     */
    @Select("SELECT COUNT(*) FROM t_elder WHERE deleted = 0 AND DATE(create_time) >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)")
    Long selectMonthlyNewElders();

    /**
     * 查询活跃老人数（近30天有订单）
     */
    @Select("""
        SELECT COUNT(DISTINCT elder_id) FROM t_order
        WHERE deleted = 0 AND create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        """)
    Long selectActiveElders();

    /**
     * 查询老人年龄段分布
     */
    @Select("""
        SELECT
            CASE
                WHEN TIMESTAMPDIFF(YEAR, birth_date, CURDATE()) < 60 THEN '0-60'
                WHEN TIMESTAMPDIFF(YEAR, birth_date, CURDATE()) BETWEEN 60 AND 69 THEN '60-69'
                WHEN TIMESTAMPDIFF(YEAR, birth_date, CURDATE()) BETWEEN 70 AND 79 THEN '70-79'
                WHEN TIMESTAMPDIFF(YEAR, birth_date, CURDATE()) BETWEEN 80 AND 89 THEN '80-89'
                ELSE '90+'
            END AS ageRange,
            COUNT(*) AS count
        FROM t_elder WHERE deleted = 0 AND birth_date IS NOT NULL
        GROUP BY ageRange
        ORDER BY MIN(TIMESTAMPDIFF(YEAR, birth_date, CURDATE()))
        """)
    List<Map<String, Object>> selectElderAgeDistribution();

    /**
     * 查询老人护理级别分布
     */
    @Select("""
        SELECT care_level AS careLevel, COUNT(*) AS count
        FROM t_elder WHERE deleted = 0
        GROUP BY care_level
        ORDER BY care_level
        """)
    List<Map<String, Object>> selectElderCareLevelDistribution();

    /**
     * 查询老人服务需求分布
     */
    @Select("""
        SELECT service_type_code AS serviceType, COUNT(*) AS count
        FROM t_elder_demand WHERE deleted = 0
        GROUP BY service_type_code
        ORDER BY count DESC
        """)
    List<Map<String, Object>> selectElderServiceDemandDistribution();

    /**
     * 查询在册老人数（状态为ACTIVE）
     */
    @Select("SELECT COUNT(*) FROM t_elder WHERE deleted = 0 AND status = 'ACTIVE'")
    Long selectRegisteredElders();

    /**
     * 查询暂停服务老人数（状态不为ACTIVE）
     */
    @Select("SELECT COUNT(*) FROM t_elder WHERE deleted = 0 AND status != 'ACTIVE'")
    Long selectSuspendedElders();

    /**
     * 查询待审核服务商数
     */
    @Select("SELECT COUNT(*) FROM t_provider WHERE deleted = 0 AND status = 'PENDING'")
    Long selectPendingProviders();

    /**
     * 查询已审核通过服务商数
     */
    @Select("SELECT COUNT(*) FROM t_provider WHERE deleted = 0 AND status = 'APPROVED'")
    Long selectApprovedProviders();

    /**
     * 查询服务商类型分布
     */
    @Select("""
        SELECT provider_type AS providerType, COUNT(*) AS count
        FROM t_provider WHERE deleted = 0
        GROUP BY provider_type
        ORDER BY count DESC
        """)
    List<Map<String, Object>> selectProviderTypeDistribution();

    /**
     * 查询服务商排名
     */
    @Select("""
        SELECT p.provider_id AS providerId, p.provider_name AS providerName, p.provider_type AS providerType,
               COUNT(o.order_id) AS orderCount,
               SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedOrderCount,
               p.rating AS rating
        FROM t_provider p
        LEFT JOIN t_order o ON p.provider_id = o.provider_id AND o.deleted = 0
        WHERE p.deleted = 0
        GROUP BY p.provider_id, p.provider_name, p.provider_type, p.rating
        ORDER BY orderCount DESC
        LIMIT 20
        """)
    List<Map<String, Object>> selectProviderRankings();

    /**
     * 查询完成订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE deleted = 0 AND status = 'COMPLETED'")
    Long selectCompletedOrders();

    /**
     * 查询订单平均评分
     */
    @Select("""
        SELECT COALESCE(AVG(overall_score), 0) FROM t_service_evaluation WHERE deleted = 0 AND overall_score IS NOT NULL
        """)
    Double selectAverageRating();

    /**
     * 查询订单总额
     */
    @Select("""
        SELECT COALESCE(SUM(estimated_price), 0) FROM t_order WHERE deleted = 0
        """)
    BigDecimal selectTotalOrderAmount();

    /**
     * 查询订单服务类型分布
     */
    @Select("""
        SELECT service_type_code AS serviceTypeCode, service_type_name AS serviceTypeName,
               COUNT(*) AS orderCount,
               SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedCount,
               COALESCE(SUM(estimated_price), 0) AS totalAmount
        FROM t_order WHERE deleted = 0
        GROUP BY service_type_code, service_type_name
        ORDER BY orderCount DESC
        """)
    List<Map<String, Object>> selectOrderServiceTypeDistribution();

    /**
     * 查询订单来源分布
     */
    @Select("""
        SELECT order_source AS orderSource, COUNT(*) AS count
        FROM t_order WHERE deleted = 0
        GROUP BY order_source
        ORDER BY count DESC
        """)
    List<Map<String, Object>> selectOrderSourceDistribution();

    /**
     * 查询财务统计数据
     */
    @Select("""
        SELECT
            COALESCE(SUM(estimated_price), 0) AS totalAmount,
            COALESCE(SUM(subsidy_amount), 0) AS totalSubsidyAmount,
            COALESCE(SUM(self_pay_amount), 0) AS totalSelfPayAmount
        FROM t_order WHERE deleted = 0
        """)
    Map<String, Object> selectFinancialSummary();

    /**
     * 查询本月财务数据
     */
    @Select("""
        SELECT
            COALESCE(SUM(estimated_price), 0) AS monthlyAmount,
            COALESCE(SUM(subsidy_amount), 0) AS monthlySubsidyAmount,
            COALESCE(SUM(self_pay_amount), 0) AS monthlySelfPayAmount
        FROM t_order WHERE deleted = 0 AND create_time >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)
        """)
    Map<String, Object> selectMonthlyFinancialSummary();

    /**
     * 查询月度财务趋势
     */
    @Select("""
        SELECT DATE_FORMAT(create_time, '%Y-%m') AS month,
               COALESCE(SUM(estimated_price), 0) AS totalAmount,
               COALESCE(SUM(subsidy_amount), 0) AS subsidyAmount,
               COALESCE(SUM(self_pay_amount), 0) AS selfPayAmount,
               COUNT(*) AS orderCount
        FROM t_order WHERE deleted = 0
        GROUP BY DATE_FORMAT(create_time, '%Y-%m')
        ORDER BY month DESC
        LIMIT 12
        """)
    List<Map<String, Object>> selectMonthlyFinancialTrend();

    /**
     * 按服务类型查询财务分布
     */
    @Select("""
        SELECT service_type_code AS serviceTypeCode, service_type_name AS serviceTypeName,
               COALESCE(SUM(estimated_price), 0) AS totalAmount,
               COALESCE(SUM(subsidy_amount), 0) AS subsidyAmount,
               COALESCE(SUM(self_pay_amount), 0) AS selfPayAmount,
               COUNT(*) AS orderCount
        FROM t_order WHERE deleted = 0
        GROUP BY service_type_code, service_type_name
        ORDER BY totalAmount DESC
        """)
    List<Map<String, Object>> selectFinancialServiceTypeDistribution();

    /**
     * 查询平均评分（基于服务商）
     */
    @Select("SELECT COALESCE(AVG(rating), 0) FROM t_provider WHERE deleted = 0 AND rating IS NOT NULL")
    Double selectAverageProviderRating();

    /**
     * 查询评价总数
     */
    @Select("""
        SELECT COUNT(*) FROM t_service_evaluation WHERE deleted = 0 AND overall_score IS NOT NULL
        """)
    Long selectTotalEvaluations();

    /**
     * 查询好评数（评分>=4）
     */
    @Select("""
        SELECT COUNT(*) FROM t_service_evaluation WHERE deleted = 0 AND overall_score IS NOT NULL AND overall_score >= 4
        """)
    Long selectPositiveEvaluations();

    /**
     * 查询中评数（评分=3）
     */
    @Select("""
        SELECT COUNT(*) FROM t_service_evaluation WHERE deleted = 0 AND overall_score IS NOT NULL AND overall_score = 3
        """)
    Long selectNeutralEvaluations();

    /**
     * 查询差评数（评分<3）
     */
    @Select("""
        SELECT COUNT(*) FROM t_service_evaluation WHERE deleted = 0 AND overall_score IS NOT NULL AND overall_score < 3
        """)
    Long selectNegativeEvaluations();

    /**
     * 查询近7天评分趋势
     */
    @Select("""
        SELECT DATE(evaluation_time) AS date,
               COALESCE(AVG(overall_score), 0) AS averageRating,
               COUNT(*) AS evaluationCount
        FROM t_service_evaluation WHERE deleted = 0 AND overall_score IS NOT NULL AND evaluation_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
        GROUP BY DATE(evaluation_time)
        ORDER BY date
        """)
    List<Map<String, Object>> selectRatingTrendLast7Days();

    /**
     * 根据日期范围查询订单趋势
     */
    List<Map<String, Object>> selectOrderTrendByDateRange(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("groupBy") String groupBy);

    /**
     * 根据日期范围和服务类型查询订单统计
     */
    List<Map<String, Object>> selectOrderStatisticsByDateRange(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("serviceTypeCode") String serviceTypeCode);

    /**
     * 查询在职员工数
     */
    @Select("SELECT COUNT(*) FROM t_staff WHERE deleted = 0 AND status = 'ON_JOB'")
    Long selectActiveStaff();

    /**
     * 查询待上岗员工数
     */
    @Select("SELECT COUNT(*) FROM t_staff WHERE deleted = 0 AND status = 'PENDING'")
    Long selectPendingStaff();

    /**
     * 查询离职员工数
     */
    @Select("SELECT COUNT(*) FROM t_staff WHERE deleted = 0 AND status = 'OFF_JOB'")
    Long selectInactiveStaff();

    /**
     * 查询待结算数
     */
    @Select("SELECT COUNT(*) FROM t_settlement WHERE payment_status = 'UNPAID'")
    Long selectPendingSettlementCount();

    /**
     * 查询已完成结算数
     */
    @Select("SELECT COUNT(*) FROM t_settlement WHERE payment_status = 'PAID'")
    Long selectCompletedSettlementCount();

    /**
     * 查询服务人员平均评分
     */
    @Select("""
        SELECT COALESCE(AVG(e.overall_score), 0) FROM t_service_evaluation e
        INNER JOIN t_staff s ON e.staff_id = s.staff_id
        WHERE e.deleted = 0 AND e.overall_score IS NOT NULL AND s.deleted = 0
        """)
    Double selectAverageStaffRating();

    /**
     * 查询服务人员排名TOP
     */
    @Select("SELECT s.staff_id AS staffId, s.staff_name AS staffName, p.provider_name AS providerName, " +
            "COUNT(o.order_id) AS orderCount, " +
            "SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END) AS serviceCount " +
            "FROM t_staff s " +
            "LEFT JOIN t_order o ON s.staff_id = o.staff_id AND o.deleted = 0 " +
            "LEFT JOIN t_provider p ON s.provider_id = p.provider_id " +
            "WHERE s.deleted = 0 " +
            "GROUP BY s.staff_id, s.staff_name, p.provider_name " +
            "ORDER BY orderCount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopStaffRankings(@Param("limit") int limit);
}
