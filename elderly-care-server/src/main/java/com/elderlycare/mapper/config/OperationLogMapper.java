package com.elderlycare.mapper.config;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.entity.config.OperationLog;
import org.apache.ibatis.annotations.Param;
import com.elderlycare.dto.config.OperationLogQueryDTO;

/**
 * 操作日志Mapper接口
 */
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    /**
     * 分页查询操作日志
     */
    IPage<OperationLog> selectOperationLogPage(Page<?> page, @Param("query") OperationLogQueryDTO query);
}
