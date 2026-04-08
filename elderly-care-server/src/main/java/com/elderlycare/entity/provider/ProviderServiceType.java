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
    private String serviceTypeId;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 服务类型编码
     */
    private String serviceCode;

    /**
     * 服务类型名称
     */
    private String serviceName;

    /**
     * 服务描述
     */
    private String description;

    /**
     * 政府补贴价格
     */
    private BigDecimal subsidyPrice;

    /**
     * 市场价格
     */
    private BigDecimal marketPrice;

    /**
     * 单位(小时/次/户等)
     */
    private String unit;

    /**
     * 预计服务时长(分钟)
     */
    private Integer estimatedDuration;

    /**
     * 状态: 0禁用, 1启用
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
