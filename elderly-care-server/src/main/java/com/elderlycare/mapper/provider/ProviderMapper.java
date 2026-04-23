package com.elderlycare.mapper.provider;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.provider.ProviderQueryDTO;
import com.elderlycare.entity.provider.Provider;
import org.apache.ibatis.annotations.Param;

/**
 * 服务商Mapper接口
 */
public interface ProviderMapper extends BaseMapper<Provider> {

    /**
     * 分页查询服务商
     */
    IPage<Provider> selectProviderPage(Page<?> page, @Param("query") ProviderQueryDTO query);

    /**
     * 根据ID更新服务商（绕过逻辑删除）
     */
    int updateProviderById(@Param("providerId") String providerId, @Param("providerName") String providerName,
                          @Param("providerType") String providerType, @Param("serviceCategory") String serviceCategory,
                          @Param("legalPerson") String legalPerson, @Param("contactPhone") String contactPhone,
                          @Param("address") String address, @Param("serviceAreas") String serviceAreas,
                          @Param("description") String description, @Param("businessLicense") String businessLicense,
                          @Param("status") String status);
}
