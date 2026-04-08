package com.elderlycare.dto.staff;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 资质创建DTO
 */
@Data
public class QualificationCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资质类型：0-身份证，1-健康证，2-职业资格证，3-培训证书，4-无犯罪记录证明，5-其他
     */
    private Integer qualificationType;

    /**
     * 资质名称
     */
    private String qualificationName;

    /**
     * 资质编号
     */
    private String qualificationNo;

    /**
     * 发证机关
     */
    private String issuingAuthority;

    /**
     * 发证日期
     */
    private LocalDate issueDate;

    /**
     * 有效期至
     */
    private LocalDate expireDate;

    /**
     * 资质证书图片URL，多个用逗号分隔
     */
    private String certificateUrls;

    /**
     * 备注
     */
    private String remark;
}
