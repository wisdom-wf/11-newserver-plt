package com.elderlycare.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 密码修改DTO
 */
@Data
public class PasswordDTO {

    /** 用户ID */
    private String userId;

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
