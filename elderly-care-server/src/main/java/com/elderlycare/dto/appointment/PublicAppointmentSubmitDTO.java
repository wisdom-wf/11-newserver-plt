package com.elderlycare.dto.appointment;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 公开预约提交DTO（扫码填写）
 */
@Data
public class PublicAppointmentSubmitDTO {

    /** 老人姓名 */
    @NotBlank(message = "姓名不能为空")
    private String elderName;

    /** 老人手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String elderPhone;

    /** 老人身份证号 */
    private String elderIdCard;

    /** 老人地址（用户填写的部分，后端拼上前缀） */
    private String elderAddress;
}
