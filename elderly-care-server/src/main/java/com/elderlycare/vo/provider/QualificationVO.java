package com.elderlycare.vo.provider;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务商资质视图对象
 */
@Data
public class QualificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String certId;
    private String providerId;
    private String certName;
    private String certType;
    private String certNumber;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String certImageUrl;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
