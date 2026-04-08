package com.elderlycare.vo.config;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典类型VO
 */
@Data
public class DictTypeVO implements Serializable {

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
