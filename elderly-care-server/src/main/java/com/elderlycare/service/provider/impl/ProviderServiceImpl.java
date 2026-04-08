package com.elderlycare.service.provider.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.provider.*;
import com.elderlycare.entity.provider.Provider;
import com.elderlycare.entity.provider.ProviderQualification;
import com.elderlycare.entity.provider.ProviderServiceType;
import com.elderlycare.mapper.provider.ProviderMapper;
import com.elderlycare.service.provider.ProviderQualificationService;
import com.elderlycare.service.provider.ProviderService;
import com.elderlycare.service.provider.ProviderServiceTypeService;
import com.elderlycare.vo.provider.ProviderRatingVO;
import com.elderlycare.vo.provider.ProviderVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务商Service实现
 */
@Service
@RequiredArgsConstructor
public class ProviderServiceImpl extends ServiceImpl<ProviderMapper, Provider> implements ProviderService {

    private final ProviderQualificationService qualificationService;
    private final ProviderServiceTypeService serviceTypeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createProvider(ProviderCreateDTO dto) {
        // 检查信用代码是否已存在
        if (existsByCreditCode(dto.getCreditCode())) {
            throw BusinessException.fail("统一社会信用代码已存在");
        }

        Provider provider = new Provider();
        BeanUtils.copyProperties(dto, provider);
        provider.setAuditStatus("PENDING"); // 默认待审核
        provider.setStatus("ENABLED"); // 默认启用

        baseMapper.insert(provider);
        return provider.getProviderId();
    }

    @Override
    public PageResult<Provider> queryProviders(ProviderQueryDTO dto) {
        Page<Provider> page = new Page<>(dto.getPage(), dto.getPageSize());
        IPage<Provider> result = baseMapper.selectProviderPage(page, dto);

        List<Provider> records = result.getRecords();
        long total = result.getTotal();

        return PageResult.of(total, dto.getPage(), dto.getPageSize(), records);
    }

    @Override
    public ProviderVO getProviderById(String providerId) {
        Provider provider = baseMapper.selectById(providerId);
        if (provider == null) {
            throw BusinessException.notFound("服务商不存在");
        }

        ProviderVO vo = new ProviderVO();
        BeanUtils.copyProperties(provider, vo);

        // 查询资质列表
        vo.setQualifications(qualificationService.getQualificationsByProviderId(providerId));

        // 查询服务类型列表
        vo.setServiceTypes(serviceTypeService.getServiceTypesByProviderId(providerId));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditProvider(String providerId, ProviderAuditDTO dto) {
        Provider provider = baseMapper.selectById(providerId);
        if (provider == null) {
            throw BusinessException.notFound("服务商不存在");
        }

        if (!"PENDING".equals(provider.getAuditStatus())) {
            throw BusinessException.fail("该服务商已审核过，无法重复审核");
        }

        provider.setAuditStatus(dto.getAuditStatus());
        provider.setAuditComment(dto.getAuditComment());
        provider.setAuditTime(LocalDateTime.now());

        baseMapper.updateById(provider);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProvider(String providerId, ProviderUpdateDTO dto) {
        Provider provider = baseMapper.selectById(providerId);
        if (provider == null) {
            throw BusinessException.notFound("服务商不存在");
        }

        if (StringUtils.isNotBlank(dto.getProviderName())) {
            provider.setProviderName(dto.getProviderName());
        }
        if (StringUtils.isNotBlank(dto.getProviderType())) {
            provider.setProviderType(dto.getProviderType());
        }
        if (StringUtils.isNotBlank(dto.getLegalPerson())) {
            provider.setLegalPerson(dto.getLegalPerson());
        }
        if (StringUtils.isNotBlank(dto.getContactPhone())) {
            provider.setContactPhone(dto.getContactPhone());
        }
        if (StringUtils.isNotBlank(dto.getAddress())) {
            provider.setAddress(dto.getAddress());
        }
        if (dto.getServiceAreas() != null) {
            provider.setServiceAreas(dto.getServiceAreas());
        }
        if (dto.getDescription() != null) {
            provider.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            provider.setStatus(dto.getStatus());
        }

        baseMapper.updateById(provider);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProvider(String providerId) {
        Provider provider = baseMapper.selectById(providerId);
        if (provider == null) {
            throw BusinessException.notFound("服务商不存在");
        }

        // 逻辑删除
        baseMapper.deleteById(providerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceAreas(String providerId, ProviderServiceAreaDTO dto) {
        Provider provider = baseMapper.selectById(providerId);
        if (provider == null) {
            throw BusinessException.notFound("服务商不存在");
        }

        String serviceAreas = StringUtils.join(dto.getServiceAreas(), ",");
        provider.setServiceAreas(serviceAreas);

        baseMapper.updateById(provider);
    }

    @Override
    public ProviderRatingVO getProviderRating(String providerId) {
        Provider provider = baseMapper.selectById(providerId);
        if (provider == null) {
            throw BusinessException.notFound("服务商不存在");
        }

        ProviderRatingVO vo = new ProviderRatingVO();
        vo.setProviderId(providerId);
        vo.setProviderName(provider.getProviderName());
        vo.setAverageRating(provider.getRating() != null ? provider.getRating() : 0.0);
        vo.setRatingCount(provider.getRatingCount() != null ? provider.getRatingCount() : 0);

        // 评分分布暂时返回默认值，实际项目中应从评价表统计
        vo.setFiveStarCount(0);
        vo.setFourStarCount(0);
        vo.setThreeStarCount(0);
        vo.setTwoStarCount(0);
        vo.setOneStarCount(0);

        return vo;
    }

    @Override
    public boolean existsByCreditCode(String creditCode) {
        LambdaQueryWrapper<Provider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Provider::getCreditCode, creditCode);
        return baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<Provider> listByIds(List<String> providerIds) {
        return baseMapper.selectBatchIds(providerIds);
    }
}
