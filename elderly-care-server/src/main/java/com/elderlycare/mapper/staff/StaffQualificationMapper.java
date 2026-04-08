package com.elderlycare.mapper.staff;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.staff.StaffQualification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 服务人员资质Mapper
 */
@Mapper
public interface StaffQualificationMapper extends BaseMapper<StaffQualification> {

    /**
     * 根据服务人员ID查询资质列表
     */
    List<StaffQualification> selectByStaffId(@Param("staffId") String staffId);

    /**
     * 根据资质ID查询详情
     */
    StaffQualification selectByQualificationId(@Param("qualificationId") String qualificationId);
}
