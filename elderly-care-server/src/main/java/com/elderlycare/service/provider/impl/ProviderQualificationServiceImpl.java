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
        // Check for duplicate: same provider + same qualification name
        LambdaQueryWrapper<ProviderQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderQualification::getProviderId, providerId)
               .eq(ProviderQualification::getQualificationName, dto.getQualificationName())
               .eq(ProviderQualification::getDeleted, 0);
        if (baseMapper.selectCount(wrapper) > 0) {
            throw BusinessException.fail("该证书已存在，请勿重复上传");
        }

        ProviderQualification qualification = new ProviderQualification();
        BeanUtils.copyProperties(dto, qualification);
        qualification.setProviderId(providerId);
        qualification.setStatus("VALID"); // 默认有效
        qualification.setAuditStatus("APPROVED"); // 默认已审核

        baseMapper.insert(qualification);
        return qualification.getQualificationId();
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
    public QualificationVO getQualificationById(String qualificationId) {
        ProviderQualification qualification = baseMapper.selectById(qualificationId);
        if (qualification == null) {
            return null;
        }
        QualificationVO vo = new QualificationVO();
        BeanUtils.copyProperties(qualification, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQualification(String qualificationId) {
        ProviderQualification qualification = baseMapper.selectById(qualificationId);
        if (qualification == null) {
            throw BusinessException.notFound("资质不存在");
        }

        baseMapper.deleteById(qualificationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQualification(String qualificationId, QualificationCreateDTO dto) {
        ProviderQualification qualification = baseMapper.selectById(qualificationId);
        if (qualification == null) {
            throw BusinessException.notFound("资质不存在");
        }
        if (dto.getQualificationName() != null) {
            qualification.setQualificationName(dto.getQualificationName());
        }
        if (dto.getAttachmentUrl() != null) {
            qualification.setAttachmentUrl(dto.getAttachmentUrl());
        }
        baseMapper.updateById(qualification);
    }

    @Override
    public boolean isQualificationOwnedByProvider(String qualificationId, String providerId) {
        LambdaQueryWrapper<ProviderQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderQualification::getQualificationId, qualificationId);
        wrapper.eq(ProviderQualification::getProviderId, providerId);

        return baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<QualificationVO> getQualificationsPreviewByProviderId(String providerId) {
        LambdaQueryWrapper<ProviderQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderQualification::getProviderId, providerId);
        wrapper.orderByDesc(ProviderQualification::getCreateTime);

        List<ProviderQualification> qualifications = baseMapper.selectList(wrapper);

        return qualifications.stream().map(cert -> {
            QualificationVO vo = new QualificationVO();
            BeanUtils.copyProperties(cert, vo);
            // 预览模式：attachmentUrl 设为 HAS_IMAGES 标记（有图片时）或 null
            if (cert.getAttachmentUrl() != null && !cert.getAttachmentUrl().isEmpty()) {
                vo.setAttachmentUrl("HAS_IMAGES");
            } else {
                vo.setAttachmentUrl(null);
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public String getQualificationImages(String qualificationId) {
        ProviderQualification qualification = baseMapper.selectById(qualificationId);
        if (qualification == null) {
            throw BusinessException.notFound("资质不存在");
        }
        return qualification.getAttachmentUrl();
    }
}
