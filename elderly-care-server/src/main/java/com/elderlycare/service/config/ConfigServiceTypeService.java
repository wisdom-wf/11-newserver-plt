package com.elderlycare.service.config;

import com.elderlycare.dto.config.ConfigServiceTypeDTO;
import com.elderlycare.entity.config.ConfigServiceType;
import com.elderlycare.vo.config.ConfigServiceTypeVO;
import java.util.List;

/**
 * 服务类型Service接口(配置模块)
 */
public interface ConfigServiceTypeService {

    /**
     * 服务类型列表
     */
    List<ConfigServiceType> listServiceTypes();

    /**
     * 服务类型详情
     */
    ConfigServiceTypeVO getServiceTypeById(String serviceTypeId);

    /**
     * 服务类型新增
     */
    String createServiceType(ConfigServiceTypeDTO dto);

    /**
     * 服务类型修改
     */
    void updateServiceType(String serviceTypeId, ConfigServiceTypeDTO dto);

    /**
     * 服务类型删除
     */
    void deleteServiceType(String serviceTypeId);
}
