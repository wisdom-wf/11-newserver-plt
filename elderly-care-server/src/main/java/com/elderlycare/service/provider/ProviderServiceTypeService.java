package com.elderlycare.service.provider;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elderlycare.dto.provider.ServiceTypeCreateDTO;
import com.elderlycare.dto.provider.ServiceTypeUpdateDTO;
import com.elderlycare.entity.provider.ProviderServiceType;
import com.elderlycare.vo.provider.ServiceTypeVO;

import java.util.List;

/**
 * 服务商服务类型Service接口
 */
public interface ProviderServiceTypeService extends IService<ProviderServiceType> {

    /**
     * 创建服务类型
     */
    String createServiceType(String providerId, ServiceTypeCreateDTO dto);

    /**
     * 获取服务类型列表
     */
    List<ServiceTypeVO> getServiceTypesByProviderId(String providerId);

    /**
     * 更新服务类型
     */
    void updateServiceType(String serviceTypeId, ServiceTypeUpdateDTO dto);

    /**
     * 删除服务类型
     */
    void deleteServiceType(String serviceTypeId);

    /**
     * 检查服务类型是否属于指定服务商
     */
    boolean isServiceTypeOwnedByProvider(String serviceTypeId, String providerId);

    /**
     * 根据服务类型ID获取服务类型
     */
    ServiceTypeVO getServiceTypeById(String serviceTypeId);
}
