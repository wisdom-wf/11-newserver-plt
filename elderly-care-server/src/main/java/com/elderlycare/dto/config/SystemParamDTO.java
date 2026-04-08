package com.elderlycare.dto.config;

import lombok.Data;
import java.io.Serializable;

/**
 * 系统参数DTO
 */
@Data
public class SystemParamDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 参数ID
     */
    private String paramId;

    /**
     * 参数分类
     */
    private String paramCategory;

    /**
     * 参数编码
     */
    private String paramCode;

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数值
     */
    private String paramValue;

    /**
     * 参数类型
     */
    private String paramType;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 参数说明
     */
    private String paramDesc;

    /**
     * 是否系统参数
     */
    private Integer isSystem;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private String status;
}
