package com.elderlycare.dto.provider;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资质创建DTO
 */
@Data
public class QualificationCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资质名称
     */
    @NotBlank(message = "资质名称不能为空")
    private String certName;

    /**
     * 资质类型
     */
    @NotBlank(message = "资质类型不能为空")
    private String certType;

    /**
     * 资质编号
     */
    private String certNumber;

    /**
     * 有效期开始
     */
    private LocalDateTime validFrom;

    /**
     * 有效期截止
     */
    private LocalDateTime validTo;

    /**
     * 资质证书图片URL
     */
    private String certImageUrl;
}
