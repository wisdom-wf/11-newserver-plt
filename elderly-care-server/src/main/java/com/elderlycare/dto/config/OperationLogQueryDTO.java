package com.elderlycare.dto.config;

import lombok.Data;
import java.io.Serializable;

/**
 * 操作日志查询DTO
 */
@Data
public class OperationLogQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作模块
     */
    private String operationModule;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}
