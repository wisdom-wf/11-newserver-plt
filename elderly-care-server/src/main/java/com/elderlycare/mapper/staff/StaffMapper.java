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

    /**
     * 根据ID更新服务人员（绕过逻辑删除）
     */
    int updateStaffById(@Param("staffId") String staffId, @Param("staffName") String staffName,
                       @Param("gender") Integer gender, @Param("idCard") String idCard,
                       @Param("age") Integer age, @Param("phone") String phone,
                       @Param("birthDate") java.time.LocalDate birthDate,
                       @Param("nation") String nation, @Param("education") String education,
                       @Param("politicalStatus") String politicalStatus, @Param("maritalStatus") String maritalStatus,
                       @Param("domicileAddress") String domicileAddress, @Param("residenceAddress") String residenceAddress,
                       @Param("emergencyContact") String emergencyContact,
                       @Param("emergencyPhone") String emergencyPhone,
                       @Param("serviceTypes") String serviceTypes,
                       @Param("hireDate") java.time.LocalDate hireDate,
                       @Param("leaveDate") java.time.LocalDate leaveDate,
                       @Param("leaveReason") String leaveReason,
                       @Param("avatarUrl") String avatarUrl,
                       @Param("remark") String remark, @Param("status") String status);
}
