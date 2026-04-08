package com.elderlycare.dto.config;

import lombok.Data;
import java.io.Serializable;

/**
 * 系统参数更新DTO
 */
@Data
public class SystemParamUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 参数值
     */
    private String paramValue;

    /**
     * 参数说明
     */
    private String paramDesc;
}
