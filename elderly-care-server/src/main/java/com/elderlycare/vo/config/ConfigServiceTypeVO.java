package com.elderlycare.vo.config;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务类型VO(配置模块)
 */
@Data
public class ConfigServiceTypeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务类型ID
     */
    private String serviceTypeId;

    /**
     * 服务类型编码
     */
    private String serviceTypeCode;

    /**
     * 服务类型名称
     */
    private String serviceTypeName;

    /**
     * 父类型ID
     */
    private String parentId;

    /**
     * 服务层级
     */
    private String serviceLevel;

    /**
     * 服务描述
     */
    private String serviceDesc;

    /**
     * 服务标准
     */
    private String serviceStandard;

    /**
     * 计价单位
     */
    private String serviceUnit;

    /**
     * 预计服务时长(分钟)
     */
    private Integer estimatedDuration;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
