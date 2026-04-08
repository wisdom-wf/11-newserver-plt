package com.elderlycare.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.order.ServiceRecordQueryDTO;
import com.elderlycare.entity.order.ServiceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 服务记录Mapper
 */
@Mapper
public interface ServiceRecordMapper extends BaseMapper<ServiceRecord> {

    /**
     * 分页查询服务记录
     */
    IPage<ServiceRecord> selectServiceRecordPage(Page<?> page, @Param("query") ServiceRecordQueryDTO query);

    /**
     * 根据订单ID查询服务记录
     */
    ServiceRecord selectByOrderId(@Param("orderId") String orderId);
}
