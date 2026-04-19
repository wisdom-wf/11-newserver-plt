package com.elderlycare.mapper.elder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.elder.HealthReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康报告Mapper
 */
@Mapper
public interface HealthReportMapper extends BaseMapper<HealthReport> {
}
