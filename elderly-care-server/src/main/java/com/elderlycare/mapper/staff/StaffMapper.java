package com.elderlycare.mapper.staff;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.staff.StaffQueryDTO;
import com.elderlycare.entity.staff.Staff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 服务人员Mapper
 */
@Mapper
public interface StaffMapper extends BaseMapper<Staff> {

    /**
     * 分页查询服务人员
     */
    IPage<Staff> selectStaffPage(Page<?> page, @Param("query") StaffQueryDTO query);

    /**
     * 根据ID查询服务人员详情
     */
    Staff selectStaffById(@Param("staffId") String staffId);
}
