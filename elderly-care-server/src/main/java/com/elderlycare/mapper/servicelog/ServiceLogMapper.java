package com.elderlycare.mapper.servicelog;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.servicelog.ServiceLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务日志Mapper
 */
@Mapper
public interface ServiceLogMapper extends BaseMapper<ServiceLog> {

    @Select("SELECT COUNT(*) FROM service_log WHERE deleted = 0 AND create_time >= #{startTime}")
    int countToday(@Param("startTime") LocalDateTime startTime);

    @Select("SELECT COUNT(*) FROM service_log WHERE deleted = 0 AND create_time >= #{startTime}")
    int countMonth(@Param("startTime") LocalDateTime startTime);

    @Select("SELECT AVG(actual_duration) FROM service_log WHERE deleted = 0 AND actual_duration IS NOT NULL")
    BigDecimal avgActualDuration();

    @Select("SELECT AVG(service_score) FROM service_log WHERE deleted = 0 AND service_score IS NOT NULL")
    BigDecimal avgServiceScore();

    @Select("SELECT COUNT(*) FROM service_log WHERE deleted = 0 AND audit_status = #{status}")
    int countByAuditStatus(@Param("status") String status);

    List<ServiceLog> selectServiceLogsByStaffId(@Param("staffId") String staffId, @Param("limit") int limit);
}
