package com.elderlycare.dto.servicelog;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 签到DTO
 */
@Data
public class SignInDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 签到位置(经纬度，格式：latitude,longitude)
     */
    @NotBlank(message = "签到位置不能为空")
    private String location;

    /**
     * 签到照片(Base64数组)
     */
    private String[] photos;

    /**
     * 签到备注
     */
    private String remark;
}
