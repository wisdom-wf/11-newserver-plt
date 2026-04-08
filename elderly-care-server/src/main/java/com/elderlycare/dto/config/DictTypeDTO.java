package com.elderlycare.dto.config;

import lombok.Data;
import java.io.Serializable;

/**
 * 字典类型DTO
 */
@Data
public class DictTypeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典类型ID
     */
    private String dictTypeId;

    /**
     * 字典类型编码
     */
    private String dictTypeCode;

    /**
     * 字典类型名称
     */
    private String dictTypeName;

    /**
     * 字典类型描述
     */
    private String dictTypeDesc;

    /**
     * 是否系统字典
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
