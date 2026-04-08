package com.elderlycare.entity.provider;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务商信息实体
 */
@Data
@TableName("t_provider")
public class Provider implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务商ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String providerId;

    /**
     * 服务商名称
     */
    private String providerName;

    /**
     * 服务商类型
     */
    private String providerType;

    /**
     * 统一社会信用代码
     */
    private String creditCode;

    /**
     * 法定代表人
     */
    private String legalPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 所在地址
     */
    private String address;

    /**
     * 服务区域(多个用逗号分隔)
     */
    private String serviceAreas;

    /**
     * 简介
     */
    private String description;

    /**
     * 审核状态: PENDING待审核, APPROVED已通过, REJECTED已拒绝
     */
    private String auditStatus;

    /**
     * 审核备注
     */
    private String auditComment;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人ID
     */
    private String auditorId;

    /**
     * 状态: 0禁用, 1启用
     */
    private String status;

    /**
     * 平均评分
     */
    private Double rating;

    /**
     * 评分次数
     */
    private Integer ratingCount;

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
