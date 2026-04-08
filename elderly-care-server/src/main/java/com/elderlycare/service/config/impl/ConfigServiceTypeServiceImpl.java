package com.elderlycare.service.config.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.dto.config.ConfigServiceTypeDTO;
import com.elderlycare.entity.config.ConfigServiceType;
import com.elderlycare.mapper.config.ConfigServiceTypeMapper;
import com.elderlycare.service.config.ConfigServiceTypeService;
import com.elderlycare.vo.config.ConfigServiceTypeVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 服务类型Service实现(配置模块)
 */
@Service
@RequiredArgsConstructor
public class ConfigServiceTypeServiceImpl extends ServiceImpl<ConfigServiceTypeMapper, ConfigServiceType>
        implements ConfigServiceTypeService {

    @Override
    public List<ConfigServiceType> listServiceTypes() {
        LambdaQueryWrapper<ConfigServiceType> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ConfigServiceType::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public ConfigServiceTypeVO getServiceTypeById(String serviceTypeId) {
        ConfigServiceType serviceType = baseMapper.selectById(serviceTypeId);
        if (serviceType == null) {
            throw BusinessException.notFound("服务类型不存在");
        }

        ConfigServiceTypeVO vo = new ConfigServiceTypeVO();
        BeanUtils.copyProperties(serviceType, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createServiceType(ConfigServiceTypeDTO dto) {
        // 检查编码是否已存在
        if (existsByCode(dto.getServiceTypeCode())) {
            throw BusinessException.fail("服务类型编码已存在");
        }

        ConfigServiceType serviceType = new ConfigServiceType();
        BeanUtils.copyProperties(dto, serviceType);
        if (dto.getStatus() == null) {
            serviceType.setStatus("ACTIVE");
        }

        baseMapper.insert(serviceType);
        return serviceType.getServiceTypeId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceType(String serviceTypeId, ConfigServiceTypeDTO dto) {
        ConfigServiceType serviceType = baseMapper.selectById(serviceTypeId);
        if (serviceType == null) {
            throw BusinessException.notFound("服务类型不存在");
        }

        if (StringUtils.isNotBlank(dto.getServiceTypeName())) {
            serviceType.setServiceTypeName(dto.getServiceTypeName());
        }
        if (StringUtils.isNotBlank(dto.getServiceLevel())) {
            serviceType.setServiceLevel(dto.getServiceLevel());
        }
        if (StringUtils.isNotBlank(dto.getServiceDesc())) {
            serviceType.setServiceDesc(dto.getServiceDesc());
        }
        if (StringUtils.isNotBlank(dto.getServiceStandard())) {
            serviceType.setServiceStandard(dto.getServiceStandard());
        }
        if (StringUtils.isNotBlank(dto.getServiceUnit())) {
            serviceType.setServiceUnit(dto.getServiceUnit());
        }
        if (dto.getEstimatedDuration() != null) {
            serviceType.setEstimatedDuration(dto.getEstimatedDuration());
        }
        if (StringUtils.isNotBlank(dto.getIcon())) {
            serviceType.setIcon(dto.getIcon());
        }
        if (dto.getSortOrder() != null) {
            serviceType.setSortOrder(dto.getSortOrder());
        }
        if (StringUtils.isNotBlank(dto.getStatus())) {
            serviceType.setStatus(dto.getStatus());
        }

        baseMapper.updateById(serviceType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServiceType(String serviceTypeId) {
        ConfigServiceType serviceType = baseMapper.selectById(serviceTypeId);
        if (serviceType == null) {
            throw BusinessException.notFound("服务类型不存在");
        }

        // 逻辑删除
        baseMapper.deleteById(serviceTypeId);
    }

    private boolean existsByCode(String serviceTypeCode) {
        LambdaQueryWrapper<ConfigServiceType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConfigServiceType::getServiceTypeCode, serviceTypeCode);
        return baseMapper.selectCount(wrapper) > 0;
    }
}
