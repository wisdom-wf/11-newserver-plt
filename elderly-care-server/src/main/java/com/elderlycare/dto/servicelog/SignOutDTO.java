package com.elderlycare.dto.servicelog;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 签退DTO
 */
@Data
public class SignOutDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 签退位置(经纬度，格式：latitude,longitude)
     */
    @NotBlank(message = "签退位置不能为空")
    private String location;

    /**
     * 签退照片(Base64数组)
     */
    private String[] photos;

    /**
     * 实际服务时长(分钟)
     */
    private Integer actualDuration;

    /**
     * 签退备注
     */
    private String remark;
}
