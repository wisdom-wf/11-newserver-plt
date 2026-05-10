package com.elderlycare.service.ess;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.common.UserContext;
import com.elderlycare.config.TencentEssConfig;
import com.elderlycare.config.ContractEnterpriseConfig;
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
    private final ContractEnterpriseConfig enterpriseConfig;
    private final EssContractMapper contractMapper;
    private final OrderMapper orderMapper;
    private final StaffMapper staffMapper;
    private final ObjectMapper objectMapper;

    public ContractServiceImpl(TencentEssConfig essConfig,
                               ContractEnterpriseConfig enterpriseConfig,
                               EssContractMapper contractMapper,
                               OrderMapper orderMapper,
                               StaffMapper staffMapper,
                               ObjectMapper objectMapper) {
        this.essConfig = essConfig;
        this.enterpriseConfig = enterpriseConfig;
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

        // 已签署/已完成：获取PDF下载链接
        if ("SIGNED".equals(contract.getStatus()) || "COMPLETED".equals(contract.getStatus())) {
            if (contract.getFlowId() == null) {
                throw new BusinessException(400, "合同流ID为空，无法获取下载链接");
            }
            return getTencentDownloadUrl(contract.getFlowId());
        }

        // 其他状态：获取预览链接（腾讯H5预览页）
        if (contract.getFlowId() == null) {
            throw new BusinessException(400, "合同流ID为空，无法获取预览链接");
        }
        return getTencentPreviewUrl(contract.getFlowId());
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
    public void deleteContract(String contractId) {
        EssContract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw BusinessException.notFound("合同不存在");
        }
        // @TableLogic 自动处理逻辑删除
        contractMapper.deleteById(contractId);
        log.info("合同已删除: contractId={}, contractNo={}", contractId, contract.getContractNo());
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
        agent.setProxyOperator(essConfig.getOperatorId());
        agent.setProxyOrganizationId(essConfig.getAgentId());
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
            

            // 设置操作人（必填）
            UserInfo operator = new UserInfo();
            operator.setUserId(essConfig.getOperatorId());
            createFlowReq.setOperator(operator);

            createFlowReq.setFlowName("智慧居家养老服务合同-" + order.getOrderNo());

            // 签署方配置 - 必须和模板中的角色顺序一致
            // 企业方（甲方 - 从配置读取，静默签署不发短信）
            FlowCreateApprover orgApprover = new FlowCreateApprover();
            orgApprover.setApproverType(0L); // 企业
            orgApprover.setOrganizationName(enterpriseConfig.getName());
            orgApprover.setApproverName(enterpriseConfig.getLegalPerson());
            orgApprover.setApproverMobile(enterpriseConfig.getPhone());
            orgApprover.setNotifyType("none"); // 企业方不发短信

            // 个人方（乙方 - 服务人员，发短信通知签署）
            FlowCreateApprover personalApprover = new FlowCreateApprover();
            personalApprover.setApproverType(1L); // 个人
            personalApprover.setApproverName(staff.getStaffName());
            personalApprover.setApproverMobile(staff.getPhone());
            personalApprover.setNotifyType("sms"); // 个人方发短信通知

            FlowCreateApprover[] approvers = new FlowCreateApprover[]{orgApprover, personalApprover};
            createFlowReq.setApprovers(approvers);

            CreateFlowResponse createFlowResp = client.CreateFlow(createFlowReq);
            String flowId = createFlowResp.getFlowId();
            log.info("CreateFlow成功, flowId={}", flowId);

            // Step 2: CreateDocument - 创建电子文档（绑定模板）
            CreateDocumentRequest createDocReq = new CreateDocumentRequest();
            
            createDocReq.setOperator(operator);
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
            
            startFlowReq.setOperator(operator);
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
     * 构建模板表单控件数据（腾讯电子签模板所有必填字段）
     */
    private FormField[] buildFormFields(Order order, Staff staff) {
        List<FormField> fields = new ArrayList<>();
        String today = java.time.LocalDate.now().toString();
        String nextYear = java.time.LocalDate.now().plusYears(1).toString();

        // ========== 合同内容 ==========
        addField(fields, "劳动合同期限", "1年");
        addField(fields, "劳动合同试用期", "无");
        addField(fields, "乙方工作内容", order.getServiceTypeName() != null ? order.getServiceTypeName() : "居家养老服务");
        addField(fields, "乙方岗位职责", "按照甲方安排，为服务对象提供日常照护、生活照料等养老服务");
        addField(fields, "乙方工作地点", order.getServiceAddress() != null ? order.getServiceAddress() : "客户家中");
        addField(fields, "乙方每月基本工资（正式）", "按服务项目结算");
        addField(fields, "乙方每月基本工资（试用）", "按服务项目结算");
        addField(fields, "发薪日", "每月15日");
        addField(fields, "双方约定其他事项", "按照国家相关法律法规执行");

        // ========== 乙方（员工）信息 ==========
        addField(fields, "签署人姓名", staff.getStaffName());
        addField(fields, "签署人证件类型", "身份证");
        if (staff.getIdCard() != null) {
            addField(fields, "签署人证件号", staff.getIdCard());
        }
        addField(fields, "签署人手机号", staff.getPhone() != null ? staff.getPhone() : "");
        addField(fields, "乙方（员工）联系地址", order.getServiceAddress() != null ? order.getServiceAddress() : "");

        // ========== 甲方（企业）信息 ==========
        addField(fields, "企业全称", enterpriseConfig.getName());
        addField(fields, "统一社会信用代码/注册号", enterpriseConfig.getCreditCode());
        addField(fields, "法定代表人/经营者姓名", enterpriseConfig.getLegalPerson());
        addField(fields, "甲方地址", enterpriseConfig.getAddress());
        addField(fields, "甲方联系电话", enterpriseConfig.getPhone());

        // 兼容旧字段名
        addField(fields, "服务人员姓名", staff.getStaffName());
        addField(fields, "乙方身份证号", staff.getIdCard() != null ? staff.getIdCard() : "");

        return fields.toArray(new FormField[0]);
    }

    private void addField(List<FormField> fields, String name, String value) {
        if (value == null || value.isEmpty()) return;
        FormField field = new FormField();
        field.setComponentName(name);
        field.setComponentValue(value);
        fields.add(field);
    }

    /**
     * 获取腾讯签署链接
     */
    private String getTencentSignUrl(String flowId, String approverName, String approverMobile) {
        log.info("获取腾讯签署链接 - flowId: {}, approver: {}", flowId, approverName);

        if (essConfig.getOperatorId() == null || essConfig.getOperatorId().isEmpty()) {
            log.warn("未配置操作人UserId，使用模拟签署链接");
            return "https://ess.gz.gov.cn/mock-sign?flowId=" + flowId + "&mock=true";
        }

        try {
            EssClient client = createEssClient();
            Agent agent = buildAgent();

            CreateFlowSignUrlRequest req = new CreateFlowSignUrlRequest();
            

            // 设置操作人
            UserInfo operator = new UserInfo();
            operator.setUserId(essConfig.getOperatorId());
            req.setOperator(operator);

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
            return "https://ess.gz.gov.cn/mock-sign?flowId=" + flowId + "&mock=true";

        } catch (Exception e) {
            log.error("调用CreateFlowSignUrl失败", e);
            return "https://ess.gz.gov.cn/mock-sign?flowId=" + flowId + "&mock=true";
        }
    }

    /**
     * 获取合同下载链接
     */
    private String getTencentDownloadUrl(String flowId) {
        log.info("获取腾讯合同下载链接 - flowId: {}", flowId);

        if (essConfig.getOperatorId() == null || essConfig.getOperatorId().isEmpty()) {
            log.warn("未配置操作人UserId，使用模拟下载链接");
            return "https://ess.gz.gov.cn/mock-download?flowId=" + flowId + "&mock=true";
        }

        try {
            EssClient client = createEssClient();

            DescribeFileUrlsRequest req = new DescribeFileUrlsRequest();
            UserInfo operator = new UserInfo();
            operator.setUserId(essConfig.getOperatorId());
            req.setOperator(operator);
            req.setBusinessType("FLOW");
            String[] businessIds = new String[]{flowId};
            req.setBusinessIds(businessIds);

            DescribeFileUrlsResponse resp = client.DescribeFileUrls(req);

            if (resp.getFileUrls() != null && resp.getFileUrls().length > 0) {
                String downloadUrl = resp.getFileUrls()[0].getUrl();
                log.info("获取合同下载链接成功: {}", downloadUrl);
                return downloadUrl;
            }

            log.warn("腾讯电子签返回空下载链接");
return "https://ess.gz.gov.cn/mock-download?flowId=" + flowId + "&mock=true";

        } catch (Exception e) {
            log.error("调用DescribeFileUrls失败", e);
return "https://ess.gz.gov.cn/mock-download?flowId=" + flowId + "&mock=true";
        }
    }

    /**
     * 获取合同预览链接（未签署的合同用H5预览）
     */
    private String getTencentPreviewUrl(String flowId) {
        log.info("获取腾讯合同预览链接 - flowId: {}", flowId);

        if (essConfig.getOperatorId() == null || essConfig.getOperatorId().isEmpty()) {
            log.warn("未配置操作人UserId，使用模拟预览链接");
            return "https://ess.gz.gov.cn/mock-preview?flowId=" + flowId + "&mock=true";
        }

        try {
            EssClient client = createEssClient();

            CreateFlowSignUrlRequest req = new CreateFlowSignUrlRequest();
            UserInfo operator = new UserInfo();
            operator.setUserId(essConfig.getOperatorId());
            req.setOperator(operator);
            req.setFlowId(flowId);
            req.setUrlType(1L); // 1=预览链接（非签署）
            req.setExpiredOn(3600L); // 1小时有效期

            CreateFlowSignUrlResponse resp = client.CreateFlowSignUrl(req);

            if (resp.getFlowApproverUrlInfos() != null && resp.getFlowApproverUrlInfos().length > 0) {
                String previewUrl = resp.getFlowApproverUrlInfos()[0].getSignUrl();
                log.info("获取合同预览链接成功: {}", previewUrl);
                return previewUrl;
            }

            log.warn("腾讯电子签返回空预览链接");
            return "https://ess.gz.gov.cn/mock-preview?flowId=" + flowId + "&mock=true";

        } catch (Exception e) {
            log.error("调用CreateFlowSignUrl(预览)失败", e);
            return "https://ess.gz.gov.cn/mock-preview?flowId=" + flowId + "&mock=true";
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
            
            req.setFlowId(flowId);
            req.setCancelMessage("管理员取消合同");

            CancelFlowResponse resp = client.CancelFlow(req);
            log.info("取消合同成功: flowId={}, requestId={}", flowId, resp.getRequestId());

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
