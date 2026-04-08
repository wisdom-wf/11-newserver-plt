package com.elderlycare.mapper.staff;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.staff.WorkRecordQueryDTO;
import com.elderlycare.entity.staff.StaffWorkRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 服务人员工作记录Mapper
 */
@Mapper
public interface StaffWorkRecordMapper extends BaseMapper<StaffWorkRecord> {

    /**
     * 分页查询工作记录
     */
    IPage<StaffWorkRecord> selectWorkRecordPage(Page<?> page, @Param("query") WorkRecordQueryDTO query);

    /**
     * 根据服务人员ID查询今日工作记录
     */
    StaffWorkRecord selectTodayRecordByStaffId(@Param("staffId") String staffId);

    /**
     * 根据工作记录ID查询详情
     */
    StaffWorkRecord selectByRecordId(@Param("recordId") String recordId);

    /**
     * 根据订单ID查询工作记录
     */
    List<StaffWorkRecord> selectByOrderId(@Param("orderId") String orderId);
}
