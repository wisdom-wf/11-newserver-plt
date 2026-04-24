package com.elderlycare.controller.elder;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.dto.elder.*;
import com.elderlycare.entity.elder.*;
import com.elderlycare.service.elder.ElderService;
import com.elderlycare.service.elder.HealthAdviceService;
import com.elderlycare.service.elder.HealthMeasurementService;
import com.elderlycare.service.elder.HealthReportService;
import com.elderlycare.vo.elder.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final HealthMeasurementService healthMeasurementService;
    private final HealthReportService healthReportService;
    private final HealthAdviceService healthAdviceService;

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
        String userType = UserContext.getUserType();
        String providerId = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && providerId != null) {
            dto.setProviderId(providerId);
        } else if ("STAFF".equals(userType) && providerId != null) {
            // STAFF用户：只看所属服务商的老人（强制覆盖前端传入的providerId）
            dto.setProviderId(providerId);
        }
        IPage<ElderVO> page = elderService.getElderPage(dto);
        return Result.success(page);
    }

    /**
     * 获取最近更新的老人档案列表（用于健康卡片展示）
     */
    @GetMapping("/recent")
    public Result<List<ElderHealthCardVO>> getRecentUpdatedElders(
            @RequestParam(defaultValue = "10") int limit) {
        String userType = UserContext.getUserType();
        String providerId = UserContext.getProviderId();
        List<ElderHealthCardVO> cards = elderService.getRecentUpdatedElders(
                "PROVIDER".equals(userType) ? providerId : null, limit);
        return Result.success(cards);
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

    // ==================== 健康测量记录管理 ====================

    /**
     * 添加健康测量记录
     */
    @PostMapping("/{elderId}/measurements")
    public Result<HealthMeasurement> addMeasurement(
            @PathVariable String elderId,
            @RequestBody HealthMeasurementDTO dto) {
        HealthMeasurement measurement = healthMeasurementService.addMeasurement(elderId, dto);
        return Result.success(measurement);
    }

    /**
     * 批量添加健康测量记录
     */
    @PostMapping("/{elderId}/measurements/batch")
    public Result<List<HealthMeasurement>> addMeasurements(
            @PathVariable String elderId,
            @RequestBody List<HealthMeasurementDTO> dtos) {
        List<HealthMeasurement> measurements = healthMeasurementService.addMeasurements(elderId, dtos);
        return Result.success(measurements);
    }

    /**
     * 获取健康测量历史
     */
    @GetMapping("/{elderId}/measurements")
    public Result<List<HealthMeasurementVO>> getMeasurementHistory(
            @PathVariable String elderId,
            @RequestParam(required = false) String measurementType,
            @RequestParam(required = false) Integer limit) {
        List<HealthMeasurementVO> history = healthMeasurementService.getMeasurementHistory(elderId, measurementType, limit);
        return Result.success(history);
    }

    /**
     * 获取老人最新一次测量记录（指定类型）
     */
    @GetMapping("/{elderId}/measurements/latest")
    public Result<HealthMeasurementVO> getLatestMeasurement(
            @PathVariable String elderId,
            @RequestParam String measurementType) {
        HealthMeasurementVO measurement = healthMeasurementService.getLatestMeasurement(elderId, measurementType);
        return Result.success(measurement);
    }

    /**
     * 获取老人最新测量记录（所有类型）
     */
    @GetMapping("/{elderId}/measurements/latest/all")
    public Result<List<HealthMeasurementVO>> getLatestMeasurements(@PathVariable String elderId) {
        List<HealthMeasurementVO> measurements = healthMeasurementService.getLatestMeasurements(elderId);
        return Result.success(measurements);
    }

    /**
     * 获取健康测量统计数据
     */
    @GetMapping("/{elderId}/measurements/statistics")
    public Result<HealthMeasurementStatisticsVO> getMeasurementStatistics(
            @PathVariable String elderId,
            @RequestParam String measurementType) {
        HealthMeasurementStatisticsVO stats = healthMeasurementService.getMeasurementStatistics(elderId, measurementType);
        return Result.success(stats);
    }

    /**
     * 获取老人所有类型测量的统计数据
     */
    @GetMapping("/{elderId}/measurements/statistics/all")
    public Result<List<HealthMeasurementStatisticsVO>> getAllMeasurementStatistics(@PathVariable String elderId) {
        List<HealthMeasurementStatisticsVO> stats = healthMeasurementService.getAllMeasurementStatistics(elderId);
        return Result.success(stats);
    }

    /**
     * 删除测量记录
     */
    @DeleteMapping("/measurements/{measurementId}")
    public Result<Void> deleteMeasurement(@PathVariable String measurementId) {
        healthMeasurementService.deleteMeasurement(measurementId);
        return Result.success();
    }

    // ==================== 健康报告管理 ====================

    /**
     * 生成健康报告
     */
    @PostMapping("/{elderId}/health-reports")
    public Result<HealthReport> generateHealthReport(
            @PathVariable String elderId,
            @RequestBody HealthReportGenerateDTO dto) {
        dto.setElderId(elderId);
        HealthReport report = healthReportService.generateReport(dto);
        return Result.success(report);
    }

    /**
     * 获取老人健康报告列表
     */
    @GetMapping("/{elderId}/health-reports")
    public Result<List<HealthReportVO>> getHealthReportList(@PathVariable String elderId) {
        List<HealthReportVO> reports = healthReportService.getReportList(elderId);
        return Result.success(reports);
    }

    /**
     * 获取健康报告详情
     */
    @GetMapping("/health-reports/{reportId}")
    public Result<HealthReportVO> getHealthReportById(@PathVariable String reportId) {
        HealthReportVO report = healthReportService.getReportById(reportId);
        return Result.success(report);
    }

    /**
     * 下载健康报告PDF
     */
    @GetMapping("/health-reports/{reportId}/pdf")
    public ResponseEntity<byte[]> downloadHealthReportPdf(@PathVariable String reportId) {
        byte[] pdfContent = healthReportService.downloadPdf(reportId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "health-report.pdf");

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    /**
     * 删除健康报告
     */
    @DeleteMapping("/health-reports/{reportId}")
    public Result<Void> deleteHealthReport(@PathVariable String reportId) {
        healthReportService.deleteReport(reportId);
        return Result.success();
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

    // ==================== AI健康建议（规则引擎）====================

    /**
     * 获取护理建议
     */
    @GetMapping("/{elderId}/care-suggestions")
    public Result<CareSuggestionVO> getCareSuggestions(@PathVariable String elderId) {
        CareSuggestionVO suggestions = healthAdviceService.getCareSuggestions(elderId);
        return Result.success(suggestions);
    }

    /**
     * 获取就医建议
     */
    @GetMapping("/{elderId}/medical-suggestions")
    public Result<MedicalSuggestionVO> getMedicalSuggestions(@PathVariable String elderId) {
        MedicalSuggestionVO suggestions = healthAdviceService.getMedicalSuggestions(elderId);
        return Result.success(suggestions);
    }
}
