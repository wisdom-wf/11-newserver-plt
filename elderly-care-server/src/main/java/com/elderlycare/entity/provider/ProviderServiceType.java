package com.elderlycare.entity.provider;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务商服务类型实体
 */
@Data
@TableName("t_provider_service_type")
public class ProviderServiceType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务类型ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String providerServiceId;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 服务类型编码
     */
    private String serviceTypeCode;

    /**
     * 服务类型名称
     */
    private String serviceTypeName;

    /**
     * 政府补贴价格
     */
    private BigDecimal subsidyPrice;

    /**
     * 市场价格
     */
    private BigDecimal servicePrice;

    /**
     * 服务区域
     */
    private String serviceArea;

    /**
     * 状态: ACTIVE-启用, INACTIVE-禁用
     */
    private String status;

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
