package com.elderlycare.mapper.servicelog;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.servicelog.ServiceLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服务日志Mapper
 */
@Mapper
public interface ServiceLogMapper extends BaseMapper<ServiceLog> {
}
