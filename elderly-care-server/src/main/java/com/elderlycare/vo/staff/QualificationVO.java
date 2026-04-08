package com.elderlycare.vo.staff;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资质VO
 */
@Data
public class QualificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String qualificationId;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 服务人员姓名
     */
    private String staffName;

    /**
     * 资质类型：0-身份证，1-健康证，2-职业资格证，3-培训证书，4-无犯罪记录证明，5-其他
     */
    private Integer qualificationType;

    /**
     * 资质类型文本
     */
    private String qualificationTypeText;

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
     * 资质证书图片URL
     */
    private String certificateUrls;

    /**
     * 状态：0-有效，1-即将到期，2-已过期
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
