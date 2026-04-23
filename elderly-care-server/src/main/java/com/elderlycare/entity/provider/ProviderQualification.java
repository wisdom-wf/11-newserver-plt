package com.elderlycare.entity.provider;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务商资质信息实体
 */
@Data
@TableName("t_provider_qualification")
public class ProviderQualification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资质ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String qualificationId;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 资质名称
     */
    private String qualificationName;

    /**
     * 资质类型
     */
    private String qualificationType;

    /**
     * 资质编号
     */
    private String qualificationNumber;

    /**
     * 有效期开始
     */
    private LocalDateTime issueDate;

    /**
     * 有效期截止
     */
    private LocalDateTime expiryDate;

    /**
     * 发证机构
     */
    private String issueOrganization;

    /**
     * 资质证书图片URL
     */
    private String attachmentUrl;

    /**
     * 状态: VALID-有效, INVALID-失效
     */
    private String status;

    /**
     * 审核状态: APPROVED-已审核, PENDING-待审核, REJECTED-已拒绝
     */
    private String auditStatus;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志
     */
    @TableLogic
    private Integer deleted;
}
