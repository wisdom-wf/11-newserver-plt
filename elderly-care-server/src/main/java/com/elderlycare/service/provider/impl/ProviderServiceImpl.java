package com.elderlycare.service.provider.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.provider.*;
import com.elderlycare.entity.User;
import com.elderlycare.entity.appointment.Appointment;
import com.elderlycare.entity.provider.Provider;
import com.elderlycare.entity.provider.ProviderQualification;
import com.elderlycare.entity.provider.ProviderServiceType;
import com.elderlycare.mapper.appointment.AppointmentMapper;
import com.elderlycare.mapper.provider.ProviderMapper;
import com.elderlycare.service.provider.ProviderAccountService;
import com.elderlycare.service.provider.ProviderQualificationService;
import com.elderlycare.service.provider.ProviderService;
import com.elderlycare.service.provider.ProviderServiceTypeService;
import com.elderlycare.vo.provider.ProviderCreateResultVO;
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
    private final AppointmentMapper appointmentMapper;
    private final ProviderAccountService providerAccountService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProviderCreateResultVO createProvider(ProviderCreateDTO dto) {
        // 检查信用代码是否已存在
        if (existsByCreditCode(dto.getCreditCode())) {
            throw BusinessException.fail("统一社会信用代码已存在");
        }

        Provider provider = new Provider();
        BeanUtils.copyProperties(dto, provider);
        // 如果前端未传状态，默认为 ENABLED
        if (provider.getStatus() == null || provider.getStatus().isEmpty()) {
            provider.setStatus("ENABLED");
        }

        baseMapper.insert(provider);

        // 自动创建服务商管理员账号
        User adminUser = providerAccountService.createProviderAccount(provider);

        // 构建返回结果
        ProviderCreateResultVO.AccountInfo accountInfo = new ProviderCreateResultVO.AccountInfo(
            adminUser.getUsername(),
            "Provider@123",
            "服务商管理员"
        );

        return new ProviderCreateResultVO(
            provider.getProviderId(),
            provider.getProviderName(),
            provider.getCreditCode(),
            accountInfo
        );
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
    public void updateProvider(String providerId, ProviderUpdateDTO dto) {
        Provider provider = baseMapper.selectById(providerId);
        if (provider == null) {
            throw BusinessException.notFound("服务商不存在");
        }

        // 使用自定义SQL更新，绕过逻辑删除限制
        baseMapper.updateProviderById(
            providerId,
            dto.getProviderName(),
            dto.getProviderType(),
            dto.getServiceCategory(),
            dto.getLegalPerson(),
            dto.getContactPhone(),
            dto.getAddress(),
            dto.getServiceAreas(),
            dto.getDescription(),
            dto.getStatus()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProvider(String providerId) {
        Provider provider = baseMapper.selectById(providerId);
        if (provider == null) {
            throw BusinessException.notFound("服务商不存在");
        }

        // 清除PENDING状态预约的服务商关联（这些预约尚未确认，删除服务商后应清空关联）
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getProviderId, providerId)
               .eq(Appointment::getStatus, "PENDING");
        Appointment updateAppointment = new Appointment();
        updateAppointment.setProviderId(null);
        appointmentMapper.update(updateAppointment, wrapper);

        // 删除服务商管理员账号
        providerAccountService.deleteProviderAccount(providerId);

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
