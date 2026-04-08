package com.elderlycare.mapper.staff;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.staff.ScheduleQueryDTO;
import com.elderlycare.entity.staff.StaffSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * 服务人员排班Mapper
 */
@Mapper
public interface StaffScheduleMapper extends BaseMapper<StaffSchedule> {

    /**
     * 根据服务人员ID查询排班列表
     */
    List<StaffSchedule> selectByStaffId(@Param("staffId") String staffId);

    /**
     * 根据日期查询排班列表
     */
    List<StaffSchedule> selectByDate(@Param("date") LocalDate date);

    /**
     * 分页查询排班
     */
    IPage<StaffSchedule> selectSchedulePage(Page<?> page, @Param("query") ScheduleQueryDTO query);

    /**
     * 根据排班ID查询详情
     */
    StaffSchedule selectByScheduleId(@Param("scheduleId") String scheduleId);
}
