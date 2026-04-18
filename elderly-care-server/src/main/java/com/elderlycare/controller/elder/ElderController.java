package com.elderlycare.controller.elder;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.dto.elder.*;
import com.elderlycare.entity.elder.*;
import com.elderlycare.service.elder.ElderService;
import com.elderlycare.vo.elder.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 老人档案管理Controller
 */
@RestController
@RequestMapping("/api/elders")
@RequiredArgsConstructor
public class ElderController {

    private final ElderService elderService;

    // ==================== 老人档案管理 ====================

    /**
     * 新增老人档案
     */
    @PostMapping("")
    public Result<Elder> addElder(@RequestBody ElderDTO dto) {
        Elder elder = elderService.addElder(dto);
        return Result.success(elder);
    }

    /**
     * 老人档案列表（分页、区域筛选）
     */
    @GetMapping("")
    public Result<IPage<ElderVO>> getElderPage(ElderPageDTO dto) {
        // 数据权限：服务商管理员自动注入 providerId
        String providerId = UserContext.getProviderId();
        if (providerId != null) {
            dto.setProviderId(providerId);
        }
        IPage<ElderVO> page = elderService.getElderPage(dto);
        return Result.success(page);
    }

    /**
     * 老人档案详情
     */
    @GetMapping("/{elderId}")
    public Result<ElderVO> getElderById(@PathVariable String elderId) {
        ElderVO elder = elderService.getElderById(elderId);
        return Result.success(elder);
    }

    /**
     * 老人档案修改
     */
    @PutMapping("/{elderId}")
    public Result<Elder> updateElder(@PathVariable String elderId, @RequestBody ElderDTO dto) {
        Elder elder = elderService.updateElder(elderId, dto);
        return Result.success(elder);
    }

    /**
     * 老人档案删除
     */
    @DeleteMapping("/{elderId}")
    public Result<Void> deleteElder(@PathVariable String elderId) {
        elderService.deleteElder(elderId);
        return Result.success();
    }

    /**
     * 老人档案状态变更
     */
    @PutMapping("/{elderId}/status")
    public Result<Void> updateElderStatus(@PathVariable String elderId, @RequestBody ElderStatusDTO dto) {
        dto.setElderId(elderId);
        elderService.updateElderStatus(dto);
        return Result.success();
    }

    // ==================== 老人家庭信息管理 ====================

    /**
     * 家庭成员新增
     */
    @PostMapping("/{elderId}/family")
    public Result<ElderFamily> addFamilyMember(@PathVariable String elderId, @RequestBody ElderFamilyDTO dto) {
        ElderFamily family = elderService.addFamilyMember(elderId, dto);
        return Result.success(family);
    }

    /**
     * 家庭成员列表
     */
    @GetMapping("/{elderId}/family")
    public Result<List<ElderFamilyVO>> getFamilyList(@PathVariable String elderId) {
        List<ElderFamilyVO> list = elderService.getFamilyList(elderId);
        return Result.success(list);
    }

    /**
     * 家庭成员修改
     */
    @PutMapping("/elder/family/{familyId}")
    public Result<ElderFamily> updateFamilyMember(@PathVariable String familyId, @RequestBody ElderFamilyDTO dto) {
        ElderFamily family = elderService.updateFamilyMember(familyId, dto);
        return Result.success(family);
    }

    /**
     * 家庭成员删除
     */
    @DeleteMapping("/elder/family/{familyId}")
    public Result<Void> deleteFamilyMember(@PathVariable String familyId) {
        elderService.deleteFamilyMember(familyId);
        return Result.success();
    }

    // ==================== 老人健康信息管理 ====================

    /**
     * 健康档案新增/修改
     */
    @PutMapping("/{elderId}/health")
    public Result<ElderHealth> saveHealth(@PathVariable String elderId, @RequestBody ElderHealthDTO dto) {
        ElderHealth health = elderService.saveHealth(elderId, dto);
        return Result.success(health);
    }

    /**
     * 健康档案查询
     */
    @GetMapping("/{elderId}/health")
    public Result<ElderHealthVO> getHealthByElderId(@PathVariable String elderId) {
        ElderHealthVO health = elderService.getHealthByElderId(elderId);
        return Result.success(health);
    }

    // ==================== 老人服务需求管理 ====================

    /**
     * 需求新增
     */
    @PostMapping("/{elderId}/demands")
    public Result<ElderDemand> addDemand(@PathVariable String elderId, @RequestBody ElderDemandDTO dto) {
        ElderDemand demand = elderService.addDemand(elderId, dto);
        return Result.success(demand);
    }

    /**
     * 需求列表
     */
    @GetMapping("/{elderId}/demands")
    public Result<List<ElderDemandVO>> getDemandList(@PathVariable String elderId) {
        List<ElderDemandVO> list = elderService.getDemandList(elderId);
        return Result.success(list);
    }

    /**
     * 需求修改
     */
    @PutMapping("/elder/demands/{demandId}")
    public Result<ElderDemand> updateDemand(@PathVariable String demandId, @RequestBody ElderDemandDTO dto) {
        ElderDemand demand = elderService.updateDemand(demandId, dto);
        return Result.success(demand);
    }

    /**
     * 需求删除
     */
    @DeleteMapping("/elder/demands/{demandId}")
    public Result<Void> deleteDemand(@PathVariable String demandId) {
        elderService.deleteDemand(demandId);
        return Result.success();
    }

    // ==================== 老人补贴管理 ====================

    /**
     * 补贴申请
     */
    @PostMapping("/{elderId}/subsidy/apply")
    public Result<ElderSubsidy> applySubsidy(@PathVariable String elderId, @RequestBody ElderSubsidyDTO dto) {
        ElderSubsidy subsidy = elderService.applySubsidy(elderId, dto);
        return Result.success(subsidy);
    }

    /**
     * 补贴列表
     */
    @GetMapping("/{elderId}/subsidy")
    public Result<List<ElderSubsidyVO>> getSubsidyList(@PathVariable String elderId) {
        List<ElderSubsidyVO> list = elderService.getSubsidyList(elderId);
        return Result.success(list);
    }

    /**
     * 补贴审核
     */
    @PutMapping("/elder/subsidy/{subsidyId}/audit")
    public Result<Void> auditSubsidy(@PathVariable String subsidyId, @RequestBody SubsidyAuditDTO dto) {
        dto.setSubsidyId(subsidyId);
        elderService.auditSubsidy(dto);
        return Result.success();
    }

    /**
     * 补贴余额查询
     */
    @GetMapping("/elder/subsidy/{subsidyId}/balance")
    public Result<SubsidyBalanceVO> getSubsidyBalance(@PathVariable String subsidyId) {
        SubsidyBalanceVO balance = elderService.getSubsidyBalance(subsidyId);
        return Result.success(balance);
    }
}
