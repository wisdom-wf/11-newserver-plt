package com.elderlycare.service.elder;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderlycare.dto.elder.*;
import com.elderlycare.entity.elder.*;
import com.elderlycare.vo.elder.*;

import java.util.List;

/**
 * 老人档案Service接口
 */
public interface ElderService {

    // ==================== 老人档案管理 ====================

    /**
     * 新增老人档案
     */
    Elder addElder(ElderDTO dto);

    /**
     * 分页查询老人档案
     */
    IPage<ElderVO> getElderPage(ElderPageDTO dto);

    /**
     * 获取老人档案详情
     */
    ElderVO getElderById(String elderId);

    /**
     * 修改老人档案
     */
    Elder updateElder(String elderId, ElderDTO dto);

    /**
     * 删除老人档案
     */
    void deleteElder(String elderId);

    /**
     * 变更老人档案状态
     */
    void updateElderStatus(ElderStatusDTO dto);

    // ==================== 老人家庭信息管理 ====================

    /**
     * 新增家庭成员
     */
    ElderFamily addFamilyMember(String elderId, ElderFamilyDTO dto);

    /**
     * 获取家庭成员列表
     */
    List<ElderFamilyVO> getFamilyList(String elderId);

    /**
     * 修改家庭成员
     */
    ElderFamily updateFamilyMember(String familyId, ElderFamilyDTO dto);

    /**
     * 删除家庭成员
     */
    void deleteFamilyMember(String familyId);

    // ==================== 老人健康信息管理 ====================

    /**
     * 新增或修改健康档案
     */
    ElderHealth saveHealth(String elderId, ElderHealthDTO dto);

    /**
     * 获取健康档案
     */
    ElderHealthVO getHealthByElderId(String elderId);

    // ==================== 老人服务需求管理 ====================

    /**
     * 新增服务需求
     */
    ElderDemand addDemand(String elderId, ElderDemandDTO dto);

    /**
     * 获取服务需求列表
     */
    List<ElderDemandVO> getDemandList(String elderId);

    /**
     * 修改服务需求
     */
    ElderDemand updateDemand(String demandId, ElderDemandDTO dto);

    /**
     * 删除服务需求
     */
    void deleteDemand(String demandId);

    // ==================== 老人补贴管理 ====================

    /**
     * 申请补贴
     */
    ElderSubsidy applySubsidy(String elderId, ElderSubsidyDTO dto);

    /**
     * 获取补贴列表
     */
    List<ElderSubsidyVO> getSubsidyList(String elderId);

    /**
     * 审核补贴
     */
    void auditSubsidy(SubsidyAuditDTO dto);

    /**
     * 查询补贴余额
     */
    SubsidyBalanceVO getSubsidyBalance(String subsidyId);

    /**
     * 获取最近更新的老人档案列表（用于健康卡片展示）
     */
    List<ElderHealthCardVO> getRecentUpdatedElders(String providerId, int limit);
}
