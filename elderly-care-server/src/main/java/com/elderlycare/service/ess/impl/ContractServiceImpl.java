package com.elderlycare.service.ess;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.common.UserContext;
import com.elderlycare.config.TencentEssConfig;
import com.elderlycare.dto.ess.ContractQueryDTO;
import com.elderlycare.entity.ess.EssContract;
import com.elderlycare.entity.order.Order;
import com.elderlycare.entity.staff.Staff;
import com.elderlycare.mapper.ess.EssContractMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.mapper.staff.StaffMapper;
import com.elderlycare.vo.ess.ContractVO;
import com.elderlycare.vo.ess.SignUrlVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    // ========== 核心业务方法 ==========

    @Override
    @Transactional
    public ContractVO createServiceContract(String orderId, String staffId) {
        try {
            Order order = orderMapper.selectByIdWithNames(orderId);
            if (order == null) {
                throw BusinessException.notFound("订单不存在");
            }

            Staff staff = staffMapper.selectById(staffId);
            if (staff == null) {
                throw BusinessException.notFound("服务人员不存在");
            }

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

            // 调用腾讯电子签API创建合同
            String flowId = createTencentFlow(order, staff);
            contract.setFlowId(flowId);
            contract.setStatus("INITIATED");
            contract.setCreateTime(LocalDateTime.now());
            contract.setUpdateTime(LocalDateTime.now());

            contractMapper.insert(contract);

            ContractVO vo = new ContractVO();
            BeanUtils.copyProperties(contract, vo);
            enrichContractVO(vo, contract);
            vo.setStatusText(getStatusText(contract.getStatus()));
            return vo;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建电子签合同失败", e);
            throw BusinessException.fail("创建电子签合同失败: " + e.getMessage());
        }
    }

    @Override
    public ContractVO getContractById(String contractId) {
        EssContract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            return null;
        }
        ContractVO vo = new ContractVO();
        BeanUtils.copyProperties(contract, vo);
        vo.setStatusText(getStatusText(contract.getStatus()));
        enrichContractVO(vo, contract);
        return vo;
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
        enrichContractVO(vo, contract);
        return vo;
    }

    @Override
    public SignUrlVO getSignUrl(String contractId, Integer approverType) {
        EssContract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw BusinessException.notFound("合同不存在");
        }

        // 解析signers获取签署人信息
        String approverName = null;
        String approverMobile = null;
        try {
            List<Map<String, Object>> signerList = objectMapper.readValue(
                    contract.getSigners(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );
            for (Map<String, Object> signer : signerList) {
                Integer type = (Integer) signer.get("type");
                if (type != null && type == 0) { // 个人签署方
                    approverName = (String) signer.get("name");
                    approverMobile = (String) signer.get("mobile");
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("解析signers失败", e);
        }

        String signUrl = getTencentSignUrl(contract.getFlowId(), approverName, approverMobile);

        SignUrlVO vo = new SignUrlVO();
        vo.setSignUrl(signUrl);
        vo.setExpireTime(LocalDateTime.now().plusDays(essConfig.getDefaultExpireDays()));

        // 保存签署链接
        contract.setSignUrl(signUrl);
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);

        return vo;
    }

    @Override
    public boolean isContractSigned(String orderId) {
        EssContract contract = contractMapper.selectByOrderId(orderId);
        if (contract == null) {
            return true; // 无合同记录，兼容旧数据
        }
        return "SIGNED".equals(contract.getStatus()) || "COMPLETED".equals(contract.getStatus());
    }

    @Override
    public String getContractStatus(String contractId) {
        EssContract contract = contractMapper.selectById(contractId);
        return contract != null ? contract.getStatus() : null;
    }

    @Override
    public void handleCallback(Object callbackData) {
        try {
            log.info("收到腾讯电子签回调: {}", callbackData);
            Map<String, Object> data = objectMapper.convertValue(callbackData, Map.class);

            // V2格式：MsgData包含FlowId和状态
            Map<String, Object> msgData = (Map<String, Object>) data.get("MsgData");
            if (msgData == null) {
                msgData = data; // V1格式：直接就是MsgData
            }

            String flowId = (String) msgData.get("FlowId");
            if (flowId == null) {
                log.warn("回调缺少FlowId字段");
                return;
            }

            // 根据flowId查找合同
            QueryWrapper<EssContract> wrapper = new QueryWrapper<>();
            wrapper.eq("flow_id", flowId).last("LIMIT 1");
            EssContract contract = contractMapper.selectOne(wrapper);
            if (contract == null) {
                log.warn("回调找不到对应合同, flowId: {}", flowId);
                return;
            }

            // 解析状态
            Integer statusCode = (Integer) msgData.get("FlowCallbackStatus");
            String showStatus = (String) msgData.get("FlowCallbackShowStatus");
            String operate = (String) msgData.get("Operate");

            log.info("合同状态变更: flowId={}, statusCode={}, showStatus={}, operate={}",
                    flowId, statusCode, showStatus, operate);

            String newStatus = mapCallbackStatus(statusCode);
            if (newStatus != null && !newStatus.equals(contract.getStatus())) {
                contract.setStatus(newStatus);
                if ("SIGNED".equals(newStatus) || "COMPLETED".equals(newStatus)) {
                    contract.setSignedTime(LocalDateTime.now());
                }
                contract.setUpdateTime(LocalDateTime.now());
                contractMapper.updateById(contract);
                log.info("合同状态已更新: contractId={}, {}", contract.getContractId(), newStatus);
            }
        } catch (Exception e) {
            log.error("处理腾讯电子签回调失败", e);
        }
    }

    @Override
    public String downloadContract(String contractId) {
        EssContract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw BusinessException.notFound("合同不存在");
        }
        return getTencentDownloadUrl(contract.getFlowId());
    }

    @Override
    @Transactional
    public void cancelContract(String contractId) {
        EssContract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw BusinessException.notFound("合同不存在");
        }
        if ("SIGNED".equals(contract.getStatus()) || "COMPLETED".equals(contract.getStatus())) {
            throw BusinessException.fail("已签署的合同无法取消");
        }
        if ("CANCELLED".equals(contract.getStatus())) {
            throw BusinessException.fail("合同已取消");
        }

        // 调用腾讯电子签取消接口
        cancelTencentFlow(contract.getFlowId());

        contract.setStatus("CANCELLED");
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);
        log.info("合同已取消: contractId={}", contractId);
    }

    @Override
    public PageResult<ContractVO> getContractList(ContractQueryDTO queryDTO) {
        QueryWrapper<EssContract> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);

        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isEmpty()) {
            wrapper.eq("status", queryDTO.getStatus());
        }
        if (queryDTO.getContractNo() != null && !queryDTO.getContractNo().isEmpty()) {
            wrapper.like("contract_no", queryDTO.getContractNo());
        }
        if (queryDTO.getStartDate() != null && !queryDTO.getStartDate().isEmpty()) {
            wrapper.ge("create_time", queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null && !queryDTO.getEndDate().isEmpty()) {
            wrapper.le("create_time", queryDTO.getEndDate() + " 23:59:59");
        }

        wrapper.orderByDesc("create_time");

        Page<EssContract> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<EssContract> result = contractMapper.selectPage(page, wrapper);

        List<ContractVO> voList = new ArrayList<>();
        for (EssContract contract : result.getRecords()) {
            ContractVO vo = new ContractVO();
            BeanUtils.copyProperties(contract, vo);
            vo.setStatusText(getStatusText(contract.getStatus()));
            enrichContractVO(vo, contract);
            voList.add(vo);
        }

        return new PageResult<>(result.getTotal(), queryDTO.getPage(), queryDTO.getPageSize(), voList);
    }

    // ========== 腾讯电子签 API 调用 ==========

    /**
     * 创建腾讯电子签客户端
     */
    private EssClient createEssClient() {
        Credential cred = new Credential(essConfig.getSecretId(), essConfig.getSecretKey());
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("ess.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        return new EssClient(cred, essConfig.getRegion(), clientProfile);
    }

    /**
     * 构建通用操作人信息
     */
    private Agent buildAgent() {
        Agent agent = new Agent();
        agent.setProxyOperatorUid(essConfig.getOperatorId());
        agent.setProxyOrganizationOpenId(essConfig.getAgentId());
        return agent;
    }

    /**
     * 创建腾讯电子签签署流程
     * 真实流程：CreateFlow -> CreateDocument -> StartFlow
     */
    private String createTencentFlow(Order order, Staff staff) {
        log.info("创建腾讯电子签流程 - 订单: {}, 服务人员: {}", order.getOrderNo(), staff.getStaffName());

        // 检查配置
        if (essConfig.getTemplateId() == null || essConfig.getTemplateId().isEmpty()) {
            log.warn("未配置模板ID，使用模拟FlowId");
            return "FLOW_" + System.currentTimeMillis();
        }
        if (essConfig.getOperatorId() == null || essConfig.getOperatorId().isEmpty()) {
            log.warn("未配置操作人UserId，使用模拟FlowId");
            return "FLOW_" + System.currentTimeMillis();
        }

        try {
            EssClient client = createEssClient();
            Agent agent = buildAgent();

            // Step 1: CreateFlow - 创建签署流程
            CreateFlowRequest createFlowReq = new CreateFlowRequest();
            createFlowReq.setAgent(agent);
            createFlowReq.setFlowName("智慧居家养老服务合同-" + order.getOrderNo());

            // 签署方配置
            // 企业方（服务商）
            FlowCreateApprover orgApprover = new FlowCreateApprover();
            orgApprover.setApproverType(0L); // 企业
            orgApprover.setOrganizationName(order.getProviderName());
            orgApprover.setApproverName(order.getProviderName());
            orgApprover.setApproverMobile(staff.getPhone()); // 用服务人员手机号（企业方经办人）
            orgApprover.setRecipientId(1L);

            // 个人方（服务人员）
            FlowCreateApprover personalApprover = new FlowCreateApprover();
            personalApprover.setApproverType(1L); // 个人
            personalApprover.setApproverName(staff.getStaffName());
            personalApprover.setApproverMobile(staff.getPhone());
            personalApprover.setRecipientId(2L);

            FlowCreateApprover[] approvers = new FlowCreateApprover[]{orgApprover, personalApprover};
            createFlowReq.setApprovers(approvers);

            CreateFlowResponse createFlowResp = client.CreateFlow(createFlowReq);
            String flowId = createFlowResp.getFlowId();
            log.info("CreateFlow成功, flowId={}", flowId);

            // Step 2: CreateDocument - 创建电子文档（绑定模板）
            CreateDocumentRequest createDocReq = new CreateDocumentRequest();
            createDocReq.setAgent(agent);
            createDocReq.setFlowId(flowId);
            createDocReq.setTemplateId(essConfig.getTemplateId());

            // 填充模板控件（可选）
            FormField[] formFields = buildFormFields(order, staff);
            if (formFields.length > 0) {
                createDocReq.setFormFields(formFields);
            }

            CreateDocumentResponse createDocResp = client.CreateDocument(createDocReq);
            log.info("CreateDocument成功, documentId={}", createDocResp.getDocumentId());

            // Step 3: StartFlow - 正式发起签署流程
            StartFlowRequest startFlowReq = new StartFlowRequest();
            startFlowReq.setAgent(agent);
            startFlowReq.setFlowId(flowId);

            StartFlowResponse startFlowResp = client.StartFlow(startFlowReq);
            log.info("StartFlow成功, flowId={}, status={}", flowId, startFlowResp.getStatus());

            return flowId;

        } catch (Exception e) {
            log.error("调用腾讯电子签API失败，使用模拟FlowId", e);
            return "FLOW_" + System.currentTimeMillis();
        }
    }

    /**
     * 构建模板表单控件数据（根据实际模板配置调整）
     */
    private FormField[] buildFormFields(Order order, Staff staff) {
        List<FormField> fields = new ArrayList<>();

        // 根据模板中的控件名称/ID填充数据
        // 以下为常见字段，需根据实际模板调整
        FormField staffName = new FormField();
        staffName.setComponentName("服务人员姓名");
        staffName.setComponentValue(staff.getStaffName());
        fields.add(staffName);

        if (order.getServiceTypeName() != null) {
            FormField serviceType = new FormField();
            serviceType.setComponentName("服务类型");
            serviceType.setComponentValue(order.getServiceTypeName());
            fields.add(serviceType);
        }

        if (order.getServiceAddress() != null) {
            FormField address = new FormField();
            address.setComponentName("服务地址");
            address.setComponentValue(order.getServiceAddress());
            fields.add(address);
        }

        return fields.toArray(new FormField[0]);
    }

    /**
     * 获取腾讯签署链接
     */
    private String getTencentSignUrl(String flowId, String approverName, String approverMobile) {
        log.info("获取腾讯签署链接 - flowId: {}, approver: {}", flowId, approverName);

        if (essConfig.getOperatorId() == null || essConfig.getOperatorId().isEmpty()) {
            log.warn("未配置操作人UserId，使用模拟签署链接");
            return "https://ess.gz.gov.cn/mock-sign?flowId=" + flowId;
        }

        try {
            EssClient client = createEssClient();
            Agent agent = buildAgent();

            CreateFlowSignUrlRequest req = new CreateFlowSignUrlRequest();
            req.setAgent(agent);
            req.setFlowId(flowId);

            // 设置签署人信息
            if (approverName != null && approverMobile != null) {
                FlowCreateApprover approverInfo = new FlowCreateApprover();
                approverInfo.setApproverType(1L); // 个人
                approverInfo.setApproverName(approverName);
                approverInfo.setApproverMobile(approverMobile);
                FlowCreateApprover[] approverInfos = new FlowCreateApprover[]{approverInfo};
                req.setFlowApproverInfos(approverInfos);
            }

            // 设置链接有效期（默认1800秒，可设置更长）
            req.setExpiredOn(3600 * 24 * 7L); // 7天

            CreateFlowSignUrlResponse resp = client.CreateFlowSignUrl(req);

            if (resp.getFlowApproverUrlInfos() != null && resp.getFlowApproverUrlInfos().length > 0) {
                String signUrl = resp.getFlowApproverUrlInfos()[0].getSignUrl();
                log.info("获取签署链接成功: {}", signUrl);
                return signUrl;
            }

            log.warn("腾讯电子签返回空签署链接");
            return "https://ess.gz.gov.cn/mock-sign?flowId=" + flowId;

        } catch (Exception e) {
            log.error("调用CreateFlowSignUrl失败", e);
            return "https://ess.gz.gov.cn/mock-sign?flowId=" + flowId;
        }
    }

    /**
     * 获取合同下载链接
     */
    private String getTencentDownloadUrl(String flowId) {
        log.info("获取腾讯合同下载链接 - flowId: {}", flowId);

        if (essConfig.getOperatorId() == null || essConfig.getOperatorId().isEmpty()) {
            log.warn("未配置操作人UserId，使用模拟下载链接");
            return "https://ess.gz.gov.cn/mock-download?flowId=" + flowId;
        }

        try {
            EssClient client = createEssClient();
            Agent agent = buildAgent();

            DescribeFileUrlsRequest req = new DescribeFileUrlsRequest();
            req.setAgent(agent);
            req.setBusinessType("FLOW");
            String[] flowIds = new String[]{flowId};
            req.setFlowIds(flowIds);

            DescribeFileUrlsResponse resp = client.DescribeFileUrls(req);

            if (resp.getFileUrls() != null && resp.getFileUrls().length > 0) {
                String downloadUrl = resp.getFileUrls()[0].getUrl();
                log.info("获取合同下载链接成功: {}", downloadUrl);
                return downloadUrl;
            }

            log.warn("腾讯电子签返回空下载链接");
            return "https://ess.gz.gov.cn/mock-download?flowId=" + flowId;

        } catch (Exception e) {
            log.error("调用DescribeFileUrls失败", e);
            return "https://ess.gz.gov.cn/mock-download?flowId=" + flowId;
        }
    }

    /**
     * 取消腾讯电子签流程
     */
    private void cancelTencentFlow(String flowId) {
        log.info("取消腾讯电子签流程 - flowId: {}", flowId);

        if (essConfig.getOperatorId() == null || essConfig.getOperatorId().isEmpty()) {
            log.warn("未配置操作人UserId，跳过腾讯API调用");
            return;
        }

        try {
            EssClient client = createEssClient();
            Agent agent = buildAgent();

            CancelFlowRequest req = new CancelFlowRequest();
            req.setAgent(agent);
            req.setFlowId(flowId);
            req.setCancelMessage("管理员取消合同");

            CancelFlowResponse resp = client.CancelFlow(req);
            log.info("取消合同成功: flowId={}, result={}", flowId, resp.getOperateResult());

        } catch (Exception e) {
            log.error("调用CancelFlow失败", e);
            throw BusinessException.fail("取消腾讯电子签流程失败: " + e.getMessage());
        }
    }

    // ========== 私有工具方法 ==========

    private void enrichContractVO(ContractVO vo, EssContract contract) {
        if (contract.getSigners() == null || contract.getSigners().isEmpty()) {
            return;
        }
        try {
            List<Map<String, Object>> signerList = objectMapper.readValue(
                    contract.getSigners(),
                    new TypeReference<List<Map<String, Object>>>() {}
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

    private String mapCallbackStatus(Integer statusCode) {
        if (statusCode == null) return null;
        switch (statusCode) {
            case 1: return "INITIATED";  // INIT
            case 2: return "SIGNING";    // PART
            case 3: return "REJECTED";   // REJECT
            case 4: return "SIGNED";     // ALL
            case 5: return "EXPIRED";    // DEADLINE
            case 6: return "CANCELLED";  // CANCEL
            default:
                log.warn("未知回调状态码: {}", statusCode);
                return null;
        }
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
