package com.elderlycare.service.dispute.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.common.PageResult;
import com.elderlycare.common.UserContext;
import com.elderlycare.entity.dispute.ServiceDispute;
import com.elderlycare.mapper.dispute.ServiceDisputeMapper;
import com.elderlycare.service.dispute.ServiceDisputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 服务争议服务实现
 */
@Service
@RequiredArgsConstructor
public class ServiceDisputeServiceImpl implements ServiceDisputeService {

    private final ServiceDisputeMapper disputeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createDispute(ServiceDispute dispute) {
        dispute.setDisputeId(IDGenerator.generateId());
        dispute.setDisputeNo("SD" + System.currentTimeMillis());
        dispute.setDisputeStatus("APPLIED");
        dispute.setAppliedTime(LocalDateTime.now());
        dispute.setCreateTime(LocalDateTime.now());
        disputeMapper.insert(dispute);
        return dispute.getDisputeId();
    }

    @Override
    public PageResult<ServiceDispute> getDisputeList(String providerId, String disputeType, String disputeStatus, int page, int pageSize) {
        Page<ServiceDispute> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<ServiceDispute> wrapper = new LambdaQueryWrapper<>();

        if (providerId != null && !providerId.isEmpty()) {
            wrapper.eq(ServiceDispute::getProviderId, providerId);
        }
        if (disputeType != null && !disputeType.isEmpty()) {
            wrapper.eq(ServiceDispute::getDisputeType, disputeType);
        }
        if (disputeStatus != null && !disputeStatus.isEmpty()) {
            wrapper.eq(ServiceDispute::getDisputeStatus, disputeStatus);
        }
        wrapper.orderByDesc(ServiceDispute::getCreateTime);

        IPage<ServiceDispute> result = disputeMapper.selectPage(pageObj, wrapper);
        return PageResult.of(result.getTotal(), page, pageSize, result.getRecords());
    }

    @Override
    public ServiceDispute getDisputeById(String disputeId) {
        return disputeMapper.selectById(disputeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startInvestigation(String disputeId, String investigationContent) {
        ServiceDispute dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) {
            throw new RuntimeException("争议不存在");
        }
        if (!"APPLIED".equals(dispute.getDisputeStatus())) {
            throw new RuntimeException("当前状态不允许进行调查");
        }
        dispute.setDisputeStatus("INVESTIGATING");
        dispute.setInvestigationContent(investigationContent);
        dispute.setInvestigatorId(UserContext.getUserId());
        dispute.setInvestigatorName(UserContext.getUsername());
        dispute.setInvestigatedTime(LocalDateTime.now());
        dispute.setUpdateTime(LocalDateTime.now());
        disputeMapper.updateById(dispute);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mediate(String disputeId, String mediationContent) {
        ServiceDispute dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) {
            throw new RuntimeException("争议不存在");
        }
        if (!"INVESTIGATING".equals(dispute.getDisputeStatus())) {
            throw new RuntimeException("当前状态不允许进行调解");
        }
        dispute.setDisputeStatus("MEDIATING");
        dispute.setMediationContent(mediationContent);
        dispute.setMediatorId(UserContext.getUserId());
        dispute.setMediatorName(UserContext.getUsername());
        dispute.setMediatedTime(LocalDateTime.now());
        dispute.setUpdateTime(LocalDateTime.now());
        disputeMapper.updateById(dispute);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reachAgreement(String disputeId, String agreementContent) {
        ServiceDispute dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) {
            throw new RuntimeException("争议不存在");
        }
        if (!"MEDIATING".equals(dispute.getDisputeStatus())) {
            throw new RuntimeException("当前状态不允许达成协议");
        }
        dispute.setDisputeStatus("AGREED");
        dispute.setAgreementContent(agreementContent);
        dispute.setAgreedTime(LocalDateTime.now());
        dispute.setUpdateTime(LocalDateTime.now());
        disputeMapper.updateById(dispute);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeDispute(String disputeId, String closeReason) {
        ServiceDispute dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) {
            throw new RuntimeException("争议不存在");
        }
        dispute.setDisputeStatus("CLOSED");
        dispute.setCloseReason(closeReason);
        dispute.setClosedTime(LocalDateTime.now());
        dispute.setUpdateTime(LocalDateTime.now());
        disputeMapper.updateById(dispute);
    }
}
