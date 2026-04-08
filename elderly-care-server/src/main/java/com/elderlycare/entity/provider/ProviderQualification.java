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
    private String certId;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 资质名称
     */
    private String certName;

    /**
     * 资质类型
     */
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

    /**
     * 状态: 0失效, 1有效
     */
    private Integer status;

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
