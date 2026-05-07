package com.elderlycare.service.ess.impl;

import com.elderlycare.config.TencentEssConfig;
import com.elderlycare.dto.ess.ContractQueryDTO;
import com.elderlycare.entity.ess.EssContract;
import com.elderlycare.entity.order.Order;
import com.elderlycare.entity.staff.Staff;
import com.elderlycare.mapper.ess.EssContractMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.mapper.staff.StaffMapper;
import com.elderlycare.service.ess.ContractService;
import com.elderlycare.vo.ess.ContractVO;
import com.elderlycare.vo.ess.SignUrlVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ContractServiceImpl implements ContractService {

    private final TencentEssConfig essConfig;
    private final EssContractMapper contractMapper;
    private final OrderMapper orderMapper;
    private final StaffMapper staffMapper;
    private final ObjectMapper objectMapper;

    public ContractServiceImpl(TencentEssConfig essConfig,
                               EssContractMapper contractMapper,
                               OrderMapper orderMapper,
                               StaffMapper staffMapper,
                               ObjectMapper objectMapper) {
        this.essConfig = essConfig;
        this.contractMapper = contractMapper;
        this.orderMapper = orderMapper;
        this.staffMapper = staffMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public ContractVO createServiceContract(String orderId, String staffId) {
        try {
            // 获取订单信息
            Order order = orderMapper.selectByIdWithNames(orderId);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }

            // 获取服务人员信息
            Staff staff = staffMapper.selectById(staffId);
            if (staff == null) {
                throw new RuntimeException("服务人员不存在");
            }

            // 创建合同记录
            EssContract contract = new EssContract();
            contract.setContractId(UUID.randomUUID().toString().replace("-", ""));
            contract.setContractNo(generateContractNo());
            contract.setOrderId(orderId);
            contract.setOrderNo(order.getOrderNo());
            contract.setContractName("智慧居家养老服务合同");

            // 签署方信息
            Map<String, Object> signer1 = new HashMap<>();
            signer1.put("name", staff.getStaffName());
            signer1.put("mobile", staff.getPhone());
            signer1.put("type", 0); // 个人

            Map<String, Object> signer2 = new HashMap<>();
            signer2.put("name", order.getProviderName());
            signer2.put("type", 1); // 企业

            List<Map<String, Object>> signers = Arrays.asList(signer1, signer2);
            contract.setSigners(objectMapper.writeValueAsString(signers));

            contract.setStatus("INITIATED");
            contract.setCreateTime(LocalDateTime.now());
            contract.setUpdateTime(LocalDateTime.now());

            // 调用腾讯电子签API创建合同（模拟实现）
            String flowId = createTencentFlow(order, staff);
            contract.setFlowId(flowId);

            contractMapper.insert(contract);

            ContractVO vo = new ContractVO();
            BeanUtils.copyProperties(contract, vo);
            return vo;

        } catch (Exception e) {
            log.error("创建电子签合同失败", e);
            throw new RuntimeException("创建电子签合同失败: " + e.getMessage());
        }
    }

    @Override
    public ContractVO getContractByOrderId(String orderId) {
        EssContract contract = contractMapper.selectByOrderId(orderId);
        if (contract == null) {
            return null;
        }
        ContractVO vo = new ContractVO();
        BeanUtils.copyProperties(contract, vo);
        vo.setStatusText(getStatusText(contract.getStatus()));

        // 解析signers JSON，提取甲方/乙方名称
        if (contract.getSigners() != null && !contract.getSigners().isEmpty()) {
            try {
                List<Map<String, Object>> signerList = objectMapper.readValue(
                    contract.getSigners(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                );
                for (Map<String, Object> signer : signerList) {
                    Integer type = (Integer) signer.get("type");
                    if (type != null) {
                        if (type == 0) {
                            // 个人（乙方 - 服务人员）
                            vo.setStaffName((String) signer.get("name"));
                        } else if (type == 1) {
                            // 企业（甲方 - 服务商）
                            vo.setProviderName((String) signer.get("name"));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析signers失败", e);
            }
        }

        return vo;
    }

    @Override
    public SignUrlVO getSignUrl(String contractId, Integer approverType) {
        try {
            EssContract contract = contractMapper.selectById(contractId);
            if (contract == null) {
                throw new RuntimeException("合同不存在");
            }

            // 调用腾讯电子签获取签署链接（模拟实现）
            String signUrl = getTencentSignUrl(contract.getFlowId(), approverType);

            SignUrlVO vo = new SignUrlVO();
            vo.setSignUrl(signUrl);
            vo.setExpireTime(LocalDateTime.now().plusDays(essConfig.getDefaultExpireDays()));

            return vo;
        } catch (Exception e) {
            log.error("获取签署链接失败", e);
            throw new RuntimeException("获取签署链接失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isContractSigned(String orderId) {
        EssContract contract = contractMapper.selectByOrderId(orderId);
        if (contract == null) {
            // 没有合同记录，默认已签署（兼容旧数据）
            return true;
        }
        return "SIGNED".equals(contract.getStatus()) || "COMPLETED".equals(contract.getStatus());
    }

    @Override
    public String getContractStatus(String contractId) {
        EssContract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            return null;
        }
        return contract.getStatus();
    }

    @Override
    public void handleCallback(Object callbackData) {
        try {
            log.info("收到腾讯电子签回调: {}", callbackData);
            // 实际实现需要解析腾讯电子签的回调数据
            // 根据回调更新合同状态
        } catch (Exception e) {
            log.error("处理腾讯电子签回调失败", e);
        }
    }

    @Override
    public String downloadContract(String contractId) {
        try {
            EssContract contract = contractMapper.selectById(contractId);
            if (contract == null) {
                throw new RuntimeException("合同不存在");
            }
            // 调用腾讯电子签下载接口（模拟实现）
            return getTencentDownloadUrl(contract.getFlowId());
        } catch (Exception e) {
            log.error("下载合同失败", e);
            throw new RuntimeException("下载合同失败: " + e.getMessage());
        }
    }

    @Override
    public List<ContractVO> getContractList(ContractQueryDTO queryDTO) {
        List<EssContract> contracts = contractMapper.selectContractList(
            queryDTO.getStatus(),
            queryDTO.getContractNo()
        );
        return contracts.stream().map(contract -> {
            ContractVO vo = new ContractVO();
            BeanUtils.copyProperties(contract, vo);
            vo.setStatusText(getStatusText(contract.getStatus()));

            // 解析signers JSON，提取甲方/乙方名称
            if (contract.getSigners() != null && !contract.getSigners().isEmpty()) {
                try {
                    List<Map<String, Object>> signerList = objectMapper.readValue(
                        contract.getSigners(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                    );
                    for (Map<String, Object> signer : signerList) {
                        Integer type = (Integer) signer.get("type");
                        if (type != null) {
                            if (type == 0) {
                                vo.setStaffName((String) signer.get("name"));
                            } else if (type == 1) {
                                vo.setProviderName((String) signer.get("name"));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析signers失败", e);
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private String createTencentFlow(Order order, Staff staff) throws Exception {
        log.info("创建腾讯电子签流程 - 订单: {}, 服务人员: {}, AgentId: {}",
                order.getOrderNo(), staff.getStaffName(), essConfig.getAgentId());

        // TODO: 实际集成时调用腾讯电子签SDK
        // 当前使用模拟实现，返回模拟flowId
        // 腾讯电子签SDK集成需要配置代理或使用内网访问
        log.info("腾讯电子签SDK调用已配置，AgentId: {}, SecretId: {}",
                essConfig.getAgentId(), essConfig.getSecretId());

        return "FLOW_" + System.currentTimeMillis();
    }

    private String getTencentSignUrl(String flowId, Integer approverType) throws Exception {
        log.info("获取腾讯签署链接 - flowId: {}, approverType: {}", flowId, approverType);

        // TODO: 实际集成时调用腾讯电子签SDK
        // 返回模拟签署URL
        // 实际应调用 CreateFlowThirdPartyUrl API
        return "https://ess.gz.gov.cn/mock-sign?flowId=" + flowId;
    }

    private String getTencentDownloadUrl(String flowId) throws Exception {
        log.info("获取腾讯合同下载链接 - flowId: {}", flowId);

        // TODO: 实际集成时调用腾讯电子签SDK
        // 返回模拟下载URL
        // 实际应调用 DownloadFlow API
        return "https://ess.gz.gov.cn/mock-download?flowId=" + flowId;
    }

    private String generateContractNo() {
        return "CONTRACT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
    }

    private String getStatusText(String status) {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("DRAFT", "草稿");
        statusMap.put("INITIATED", "已发起");
        statusMap.put("SIGNING", "签署中");
        statusMap.put("SIGNED", "已签署");
        statusMap.put("COMPLETED", "已完成");
        statusMap.put("EXPIRED", "已过期");
        statusMap.put("REJECTED", "已拒签");
        statusMap.put("CANCELLED", "已撤回");
        return statusMap.getOrDefault(status, status);
    }
}