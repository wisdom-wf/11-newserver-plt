package com.elderlycare.vo.config;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 区域VO
 */
@Data
public class AreaVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 区域ID
     */
    private String areaId;

    /**
     * 区域编码
     */
    private String areaCode;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 父区域ID
     */
    private String parentId;

    /**
     * 区域层级
     */
    private String areaLevel;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

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
