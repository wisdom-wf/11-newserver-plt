package com.elderlycare.service.provider;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.provider.*;
import com.elderlycare.entity.User;
import com.elderlycare.entity.provider.Provider;
import com.elderlycare.vo.provider.ProviderCreateResultVO;
import com.elderlycare.vo.provider.ProviderRatingVO;
import com.elderlycare.vo.provider.ProviderVO;

import java.util.List;

/**
 * 服务商Service接口
 */
public interface ProviderService extends IService<Provider> {

    /**
     * 创建服务商（自动创建对应的管理员账号）
     */
    ProviderCreateResultVO createProvider(ProviderCreateDTO dto);

    /**
     * 分页查询服务商
     */
    PageResult<Provider> queryProviders(ProviderQueryDTO dto);

    /**
     * 获取服务商详情
     */
    ProviderVO getProviderById(String providerId);

    /**
     * 更新服务商信息
     */
    void updateProvider(String providerId, ProviderUpdateDTO dto);

    /**
     * 删除服务商
     */
    void deleteProvider(String providerId);

    /**
     * 更新服务商服务区域
     */
    void updateServiceAreas(String providerId, ProviderServiceAreaDTO dto);

    /**
     * 获取服务商评分
     */
    ProviderRatingVO getProviderRating(String providerId);

    /**
     * 获取服务商管理员账户
     */
    User getProviderAdminAccount(String providerId);

    /**
     * 重置服务商管理员密码
     */
    String resetProviderAdminPassword(String providerId);

    /**
     * 根据信用代码检查服务商是否存在
     */
    boolean existsByCreditCode(String creditCode);

    /**
     * 根据ID列表查询服务商
     */
    List<Provider> listByIds(List<String> providerIds);
}
