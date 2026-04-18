package com.elderlycare.controller.provider;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.dto.provider.*;
import com.elderlycare.entity.provider.Provider;
import com.elderlycare.service.provider.ProviderQualificationService;
import com.elderlycare.service.provider.ProviderService;
import com.elderlycare.service.provider.ProviderServiceTypeService;
import com.elderlycare.vo.provider.ProviderCreateResultVO;
import com.elderlycare.vo.provider.ProviderRatingVO;
import com.elderlycare.vo.provider.ProviderVO;
import com.elderlycare.vo.provider.QualificationVO;
import com.elderlycare.vo.provider.ServiceTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务商管理Controller
 */
@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;
    private final ProviderQualificationService qualificationService;
    private final ProviderServiceTypeService serviceTypeService;

    // ==================== 服务商管理接口 ====================

    /**
     * 服务商注册（自动创建对应的管理员账号）
     */
    @PostMapping
    public Result<ProviderCreateResultVO> createProvider(@Validated @RequestBody ProviderCreateDTO dto) {
        ProviderCreateResultVO result = providerService.createProvider(dto);
        return Result.success(result);
    }

    /**
     * 服务商列表
     */
    @GetMapping
    public Result<PageResult<Provider>> queryProviders(ProviderQueryDTO dto) {
        PageResult<Provider> result = providerService.queryProviders(dto);
        return Result.success(result);
    }

    /**
     * 服务商详情
     */
    @GetMapping("/{providerId}")
    public Result<ProviderVO> getProviderById(@PathVariable String providerId) {
        ProviderVO vo = providerService.getProviderById(providerId);
        return Result.success(vo);
    }

    /**
     * 服务商信息修改
     */
    @PutMapping("/{providerId}")
    public Result<Void> updateProvider(
            @PathVariable String providerId,
            @Validated @RequestBody ProviderUpdateDTO dto) {
        providerService.updateProvider(providerId, dto);
        return Result.success();
    }

    /**
     * 服务商删除
     */
    @DeleteMapping("/{providerId}")
    public Result<Void> deleteProvider(@PathVariable String providerId) {
        providerService.deleteProvider(providerId);
        return Result.success();
    }

    /**
     * 服务商服务区域管理
     */
    @PutMapping("/{providerId}/service-areas")
    public Result<Void> updateServiceAreas(
            @PathVariable String providerId,
            @RequestBody ProviderServiceAreaDTO dto) {
        providerService.updateServiceAreas(providerId, dto);
        return Result.success();
    }

    /**
     * 服务商评分查询
     */
    @GetMapping("/{providerId}/ratings")
    public Result<ProviderRatingVO> getProviderRating(@PathVariable String providerId) {
        ProviderRatingVO vo = providerService.getProviderRating(providerId);
        return Result.success(vo);
    }

    // ==================== 服务商资质管理接口 ====================

    /**
     * 资质新增
     */
    @PostMapping("/{providerId}/certificates")
    public Result<String> createQualification(
            @PathVariable String providerId,
            @Validated @RequestBody QualificationCreateDTO dto) {
        String certId = qualificationService.createQualification(providerId, dto);
        return Result.success(certId);
    }

    /**
     * 资质列表
     */
    @GetMapping("/{providerId}/certificates")
    public Result<List<QualificationVO>> getQualifications(@PathVariable String providerId) {
        List<QualificationVO> list = qualificationService.getQualificationsByProviderId(providerId);
        return Result.success(list);
    }

    /**
     * 资质删除
     */
    @DeleteMapping("/certificates/{certId}")
    public Result<Void> deleteQualification(@PathVariable String certId) {
        qualificationService.deleteQualification(certId);
        return Result.success();
    }

    // ==================== 服务商服务类型管理接口 ====================

    /**
     * 服务类型配置
     */
    @PostMapping("/{providerId}/service-types")
    public Result<String> createServiceType(
            @PathVariable String providerId,
            @Validated @RequestBody ServiceTypeCreateDTO dto) {
        String serviceTypeId = serviceTypeService.createServiceType(providerId, dto);
        return Result.success(serviceTypeId);
    }

    /**
     * 服务类型列表
     */
    @GetMapping("/{providerId}/service-types")
    public Result<List<ServiceTypeVO>> getServiceTypes(@PathVariable String providerId) {
        List<ServiceTypeVO> list = serviceTypeService.getServiceTypesByProviderId(providerId);
        return Result.success(list);
    }

    /**
     * 服务类型修改
     */
    @PutMapping("/service-types/{serviceTypeId}")
    public Result<Void> updateServiceType(
            @PathVariable String serviceTypeId,
            @Validated @RequestBody ServiceTypeUpdateDTO dto) {
        serviceTypeService.updateServiceType(serviceTypeId, dto);
        return Result.success();
    }

    /**
     * 服务类型删除
     */
    @DeleteMapping("/service-types/{serviceTypeId}")
    public Result<Void> deleteServiceType(@PathVariable String serviceTypeId) {
        serviceTypeService.deleteServiceType(serviceTypeId);
        return Result.success();
    }
}
