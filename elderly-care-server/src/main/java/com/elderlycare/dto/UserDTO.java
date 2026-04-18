package com.elderlycare.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * 用户DTO
 */
@Data
public class UserDTO {

    /** 用户ID(修改时必填) */
    private String userId;

    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码(新增时必填) */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 性别 */
    private String gender;

    /** 头像URL */
    private String avatar;

    /** 用户类型 */
    private String userType;

    /** 服务商ID */
    private String providerId;

    /** 区域ID */
    private String areaId;

    /** 状态 */
    private String status;

    /** 租户ID */
    private String tenantId;

    /** 角色ID列表 */
    private List<String> roleIds;
}
