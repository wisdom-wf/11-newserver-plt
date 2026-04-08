package com.elderlycare.service.provider.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.dto.provider.QualificationCreateDTO;
import com.elderlycare.entity.provider.ProviderQualification;
import com.elderlycare.mapper.provider.ProviderQualificationMapper;
import com.elderlycare.service.provider.ProviderQualificationService;
import com.elderlycare.vo.provider.QualificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务商资质Service实现
 */
@Service
@RequiredArgsConstructor
public class ProviderQualificationServiceImpl
        extends ServiceImpl<ProviderQualificationMapper, ProviderQualification>
        implements ProviderQualificationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createQualification(String providerId, QualificationCreateDTO dto) {
        ProviderQualification qualification = new ProviderQualification();
        BeanUtils.copyProperties(dto, qualification);
        qualification.setProviderId(providerId);
        qualification.setStatus(1); // 默认有效

        baseMapper.insert(qualification);
        return qualification.getCertId();
    }

    @Override
    public List<QualificationVO> getQualificationsByProviderId(String providerId) {
        LambdaQueryWrapper<ProviderQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderQualification::getProviderId, providerId);
        wrapper.orderByDesc(ProviderQualification::getCreateTime);

        List<ProviderQualification> qualifications = baseMapper.selectList(wrapper);

        return qualifications.stream().map(cert -> {
            QualificationVO vo = new QualificationVO();
            BeanUtils.copyProperties(cert, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQualification(String certId) {
        ProviderQualification qualification = baseMapper.selectById(certId);
        if (qualification == null) {
            throw BusinessException.notFound("资质不存在");
        }

        baseMapper.deleteById(certId);
    }

    @Override
    public boolean isQualificationOwnedByProvider(String certId, String providerId) {
        LambdaQueryWrapper<ProviderQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderQualification::getCertId, certId);
        wrapper.eq(ProviderQualification::getProviderId, providerId);

        return baseMapper.selectCount(wrapper) > 0;
    }
}
