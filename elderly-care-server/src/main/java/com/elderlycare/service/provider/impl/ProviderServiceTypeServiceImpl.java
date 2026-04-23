package com.elderlycare.service.provider.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.dto.provider.ServiceTypeCreateDTO;
import com.elderlycare.dto.provider.ServiceTypeUpdateDTO;
import com.elderlycare.entity.provider.ProviderServiceType;
import com.elderlycare.mapper.provider.ProviderServiceTypeMapper;
import com.elderlycare.service.provider.ProviderServiceTypeService;
import com.elderlycare.vo.provider.ServiceTypeVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务商服务类型Service实现
 */
@Service
@RequiredArgsConstructor
public class ProviderServiceTypeServiceImpl
        extends ServiceImpl<ProviderServiceTypeMapper, ProviderServiceType>
        implements ProviderServiceTypeService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createServiceType(String providerId, ServiceTypeCreateDTO dto) {
        ProviderServiceType serviceType = new ProviderServiceType();
        BeanUtils.copyProperties(dto, serviceType);
        serviceType.setProviderId(providerId);
        serviceType.setStatus("ACTIVE"); // 默认启用

        baseMapper.insert(serviceType);
        return serviceType.getProviderServiceId();
    }

    @Override
    public List<ServiceTypeVO> getServiceTypesByProviderId(String providerId) {
        LambdaQueryWrapper<ProviderServiceType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderServiceType::getProviderId, providerId);
        wrapper.orderByDesc(ProviderServiceType::getCreateTime);

        List<ProviderServiceType> serviceTypes = baseMapper.selectList(wrapper);

        return serviceTypes.stream().map(st -> {
            ServiceTypeVO vo = new ServiceTypeVO();
            BeanUtils.copyProperties(st, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceType(String providerServiceId, ServiceTypeUpdateDTO dto) {
        ProviderServiceType serviceType = baseMapper.selectById(providerServiceId);
        if (serviceType == null) {
            throw BusinessException.notFound("服务类型不存在");
        }

        if (StringUtils.isNotBlank(dto.getServiceTypeName())) {
            serviceType.setServiceTypeName(dto.getServiceTypeName());
        }
        if (dto.getSubsidyPrice() != null) {
            serviceType.setSubsidyPrice(dto.getSubsidyPrice());
        }
        if (dto.getServicePrice() != null) {
            serviceType.setServicePrice(dto.getServicePrice());
        }
        if (StringUtils.isNotBlank(dto.getServiceArea())) {
            serviceType.setServiceArea(dto.getServiceArea());
        }
        if (dto.getStatus() != null) {
            serviceType.setStatus(dto.getStatus());
        }

        baseMapper.updateById(serviceType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServiceType(String providerServiceId) {
        ProviderServiceType serviceType = baseMapper.selectById(providerServiceId);
        if (serviceType == null) {
            throw BusinessException.notFound("服务类型不存在");
        }

        baseMapper.deleteById(providerServiceId);
    }

    @Override
    public boolean isServiceTypeOwnedByProvider(String providerServiceId, String providerId) {
        LambdaQueryWrapper<ProviderServiceType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderServiceType::getProviderServiceId, providerServiceId);
        wrapper.eq(ProviderServiceType::getProviderId, providerId);

        return baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public ServiceTypeVO getServiceTypeById(String providerServiceId) {
        ProviderServiceType serviceType = baseMapper.selectById(providerServiceId);
        if (serviceType == null) {
            throw BusinessException.notFound("服务类型不存在");
        }

        ServiceTypeVO vo = new ServiceTypeVO();
        BeanUtils.copyProperties(serviceType, vo);
        return vo;
    }
}
