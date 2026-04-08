package com.elderlycare.dto.config;

import lombok.Data;
import java.io.Serializable;

/**
 * 字典项DTO
 */
@Data
public class DictItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典项ID
     */
    private String dictItemId;

    /**
     * 字典类型ID
     */
    private String dictTypeId;

    /**
     * 字典类型编码
     */
    private String dictTypeCode;

    /**
     * 字典项编码
     */
    private String dictItemCode;

    /**
     * 字典项名称
     */
    private String dictItemName;

    /**
     * 字典项值
     */
    private String dictItemValue;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private String status;
}
