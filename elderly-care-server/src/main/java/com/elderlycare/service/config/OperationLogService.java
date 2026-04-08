package com.elderlycare.service.config;

import com.elderlycare.common.PageResult;
import com.elderlycare.dto.config.OperationLogQueryDTO;
import com.elderlycare.entity.config.OperationLog;
import com.elderlycare.vo.config.OperationLogVO;
import java.util.List;

/**
 * 操作日志Service接口
 */
public interface OperationLogService {

    /**
     * 日志查询(分页)
     */
    PageResult<OperationLog> queryOperationLogs(OperationLogQueryDTO dto);

    /**
     * 日志新增
     */
    void createOperationLog(OperationLog operationLog);

    /**
     * 获取日志详情
     */
    OperationLogVO getOperationLogById(String operationLogId);
}
