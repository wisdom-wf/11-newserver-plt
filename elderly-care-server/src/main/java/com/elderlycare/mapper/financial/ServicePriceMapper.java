package com.elderlycare.mapper.financial;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.financial.ServicePriceQueryDTO;
import com.elderlycare.entity.financial.ServicePrice;
import org.apache.ibatis.annotations.Param;

/**
 * 服务定价Mapper接口
 */
public interface ServicePriceMapper extends BaseMapper<ServicePrice> {

    /**
     * 分页查询服务定价
     */
    IPage<ServicePrice> selectPricePage(Page<?> page, @Param("query") ServicePriceQueryDTO query);
}
