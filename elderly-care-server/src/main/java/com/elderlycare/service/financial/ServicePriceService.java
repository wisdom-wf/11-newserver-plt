package com.elderlycare.service.financial;

import com.elderlycare.common.PageResult;
import com.elderlycare.dto.financial.ServicePriceCreateDTO;
import com.elderlycare.dto.financial.ServicePriceQueryDTO;
import com.elderlycare.dto.financial.ServicePriceUpdateDTO;
import com.elderlycare.vo.financial.ServicePriceVO;

/**
 * 服务定价Service接口
 */
public interface ServicePriceService {

    /**
     * 创建服务定价
     */
    String createPrice(ServicePriceCreateDTO dto);

    /**
     * 查询服务定价列表
     */
    PageResult<ServicePriceVO> queryPrices(ServicePriceQueryDTO dto);

    /**
     * 获取服务定价详情
     */
    ServicePriceVO getPriceById(String priceId);

    /**
     * 更新服务定价
     */
    void updatePrice(String priceId, ServicePriceUpdateDTO dto);

    /**
     * 删除服务定价
     */
    void deletePrice(String priceId);
}
