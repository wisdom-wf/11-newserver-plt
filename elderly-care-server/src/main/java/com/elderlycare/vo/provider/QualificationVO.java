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

    private String qualificationId;
    private String providerId;
    private String qualificationName;
    private String qualificationType;
    private String qualificationNumber;
    private LocalDateTime issueDate;
    private LocalDateTime expiryDate;
    private String issueOrganization;
    private String attachmentUrl;
    private String status;
    private String auditStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
