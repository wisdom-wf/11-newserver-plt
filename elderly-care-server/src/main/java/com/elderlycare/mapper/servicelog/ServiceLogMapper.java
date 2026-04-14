package com.elderlycare.mapper.servicelog;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.servicelog.ServiceLog;
import com.elderlycare.vo.servicelog.ServiceLogStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 服务日志Mapper
 */
@Mapper
public interface ServiceLogMapper extends BaseMapper<ServiceLog> {

    @Select("SELECT COUNT(*) FROM service_log WHERE DATE(create_time) = CURDATE()")
    Integer countToday();

    @Select("SELECT COUNT(*) FROM service_log WHERE YEAR(create_time) = YEAR(CURDATE()) AND MONTH(create_time) = MONTH(CURDATE())")
    Integer countMonth();

    @Select("SELECT COUNT(*) FROM service_log WHERE audit_status = 'PENDING'")
    Integer countPending();

    @Select("SELECT COUNT(*) FROM service_log WHERE audit_status = 'APPROVED'")
    Integer countApproved();

    @Select("SELECT COUNT(*) FROM service_log WHERE audit_status = 'REJECTED'")
    Integer countRejected();

    @Select("SELECT AVG(service_duration) FROM service_log WHERE service_duration IS NOT NULL")
    BigDecimal avgDuration();

    @Select("SELECT AVG(service_score) FROM service_log WHERE service_score IS NOT NULL")
    BigDecimal avgScore();

    @Select("SELECT COUNT(*) FROM service_log WHERE anomaly_status = 'REPORTED'")
    Integer countAnomaly();

    @Select("SELECT COUNT(*) FROM service_log WHERE DATE(create_time) = CURDATE() AND service_status = 'COMPLETED'")
    Integer countTodayCompleted();

    @Select("SELECT AVG(TIMESTAMPDIFF(HOUR, create_time, update_time)) FROM service_log " +
            "WHERE audit_status IN ('APPROVED', 'REJECTED') AND update_time IS NOT NULL")
    BigDecimal avgReviewTimeHours();

    @Select("SELECT s.staff_id as staffId, s.staff_name as staffName, p.provider_name as providerName, " +
            "COUNT(l.service_log_id) as logCount, " +
            "SUM(CASE WHEN l.audit_status = 'APPROVED' THEN 1 ELSE 0 END) as approvedCount, " +
            "SUM(CASE WHEN l.audit_status = 'REJECTED' THEN 1 ELSE 0 END) as rejectedCount " +
            "FROM service_log l " +
            "LEFT JOIN t_staff s ON l.staff_id = s.staff_id " +
            "LEFT JOIN t_provider p ON s.provider_id = p.provider_id " +
            "GROUP BY s.staff_id, s.staff_name, p.provider_name " +
            "HAVING logCount > 0 " +
            "ORDER BY approvedCount DESC, logCount DESC " +
            "LIMIT 10")
    List<ServiceLogStatisticsVO.StaffRanking> getStaffRankings();
}
