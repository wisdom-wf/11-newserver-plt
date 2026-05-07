package com.elderlycare.mapper.appointment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.appointment.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 预约Mapper
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    /** 按token查询预约模板记录 */
    @Select("SELECT * FROM appointment WHERE appointment_token = #{token} LIMIT 1")
    Appointment selectByToken(@Param("token") String token);

    /** 统计某手机号近期预约数（防刷） */
    @Select("SELECT COUNT(*) FROM appointment WHERE elder_phone = #{phone} AND create_time >= #{since}")
    Long countRecentByPhone(@Param("phone") String phone, @Param("since") LocalDateTime since);

    /** 批量统计各状态数量 */
    @Select("<script>" +
            "SELECT status, COUNT(*) as cnt FROM appointment WHERE validity = 'VALID' " +
            "<if test='providerId != null'> AND provider_id = #{providerId} </if>" +
            "<if test='areaId != null and areaId != \"\"'> AND elder_area_id = #{areaId} </if>" +
            "<if test='startDate != null'> AND create_time >= #{startDate} </if>" +
            "<if test='endDate != null'> AND create_time &lt;= #{endDate} </if>" +
            " GROUP BY status" +
            "</script>")
    java.util.List<java.util.Map<String, Object>> selectStatusCounts(
            @Param("providerId") String providerId,
            @Param("areaId") String areaId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
