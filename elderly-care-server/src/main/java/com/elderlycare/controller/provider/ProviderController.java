package com.elderlycare.controller.provider;

import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.dto.provider.*;
import com.elderlycare.entity.User;
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
     * GET /api/providers
     * 隔离规则：SYSTEM/CITY/DISTRICT可见全部；PROVIDER只能看自己
     */
    @GetMapping
    public Result<PageResult<Provider>> queryProviders(ProviderQueryDTO dto) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        // PROVIDER用户强制只看自己；SYSTEM类用户不过滤
        if ("PROVIDER".equals(userType) && autoPid != null) {
            dto.setProviderId(autoPid);  // 只查自己，强制覆盖前端传入
        }
        PageResult<Provider> result = providerService.queryProviders(dto);
        return Result.success(result);
    }

    /**
     * 服务商详情
     * GET /api/providers/{providerId}
     * 隔离规则：PROVIDER只能查自己的详情
     */
    @GetMapping("/{providerId}")
    public Result<ProviderVO> getProviderById(@PathVariable String providerId) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        // PROVIDER用户只能查自己的详情
        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(providerId)) {
            throw BusinessException.fail("无权访问其他服务商信息");
        }
        ProviderVO vo = providerService.getProviderById(providerId);
        return Result.success(vo);
    }

    /**
     * 服务商信息修改
     * PUT /api/providers/{providerId}
     * 隔离规则：PROVIDER只能改自己的
     */
    @PutMapping("/{providerId}")
    public Result<Void> updateProvider(
            @PathVariable String providerId,
            @Validated @RequestBody ProviderUpdateDTO dto) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(providerId)) {
            throw BusinessException.fail("无权修改其他服务商信息");
        }
        providerService.updateProvider(providerId, dto);
        return Result.success();
    }

    /**
     * 服务商删除
     */
    @DeleteMapping("/{providerId}")
    public Result<Void> deleteProvider(@PathVariable String providerId) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(providerId)) {
            throw BusinessException.fail("无权删除其他服务商");
        }
        providerService.deleteProvider(providerId);
        return Result.success();
    }

    /**
     * 服务商批量删除
     * DELETE /api/providers/batch
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteProvider(@RequestBody List<String> providerIds) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null) {
            // PROVIDER用户只能批量删除自己
            for (String pid : providerIds) {
                if (!autoPid.equals(pid)) {
                    throw BusinessException.fail("无权删除其他服务商");
                }
            }
        }
        for (String providerId : providerIds) {
            providerService.deleteProvider(providerId);
        }
        return Result.successMsg("批量删除成功");
    }

    /**
     * 服务商服务区域管理
     */
    @PutMapping("/{providerId}/service-areas")
    public Result<Void> updateServiceAreas(
            @PathVariable String providerId,
            @RequestBody ProviderServiceAreaDTO dto) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(providerId)) {
            throw BusinessException.fail("无权修改其他服务商服务区域");
        }
        providerService.updateServiceAreas(providerId, dto);
        return Result.success();
    }

    /**
     * 服务商评分查询
     */
    @GetMapping("/{providerId}/ratings")
    public Result<ProviderRatingVO> getProviderRating(@PathVariable String providerId) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(providerId)) {
            throw BusinessException.fail("无权查看其他服务商评分");
        }
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
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(providerId)) {
            throw BusinessException.fail("无权为其他服务商添加资质");
        }
        String certId = qualificationService.createQualification(providerId, dto);
        return Result.<String>success(certId);
    }

    /**
     * 资质列表
     */
    @GetMapping("/{providerId}/certificates")
    public Result<List<QualificationVO>> getQualifications(@PathVariable String providerId) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(providerId)) {
            throw BusinessException.fail("无权查看其他服务商资质");
        }
        List<QualificationVO> list = qualificationService.getQualificationsByProviderId(providerId);
        return Result.success(list);
    }

    /**
     * 资质预览（不含图片base64）
     */
    @GetMapping("/{providerId}/certificates/preview")
    public Result<List<QualificationVO>> getQualificationsPreview(@PathVariable String providerId) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(providerId)) {
            throw BusinessException.fail("无权查看其他服务商资质");
        }
        List<QualificationVO> list = qualificationService.getQualificationsPreviewByProviderId(providerId);
        return Result.success(list);
    }

    /**
     * 获取单个资质的图片
     */
    @GetMapping("/certificates/{qualificationId}/images")
    public Result<String> getQualificationImages(@PathVariable String qualificationId) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null) {
            QualificationVO cert = qualificationService.getQualificationById(qualificationId);
            if (cert == null) {
                throw BusinessException.fail("资质不存在");
            }
            if (!autoPid.equals(cert.getProviderId())) {
                throw BusinessException.fail("无权查看其他服务商的资质图片");
            }
        }
        String images = qualificationService.getQualificationImages(qualificationId);
        return Result.success(images);
    }

    /**
     * 更新资质证书
     */
    @PutMapping("/certificates/{certId}")
    public Result<Void> updateQualification(
            @PathVariable String certId,
            @Validated @RequestBody QualificationCreateDTO dto) {
        // 归属校验：PROVIDER用户只能修改自己公司的资质
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null) {
            QualificationVO cert = qualificationService.getQualificationById(certId);
            if (cert == null) {
                throw BusinessException.fail("资质不存在");
            }
            if (!autoPid.equals(cert.getProviderId())) {
                throw BusinessException.fail("无权修改其他服务商的资质");
            }
        }
        qualificationService.updateQualification(certId, dto);
        return Result.success();
    }

    /**
     * 资质删除
     */
    @DeleteMapping("/certificates/{certId}")
    public Result<Void> deleteQualification(@PathVariable String certId) {
        // 归属校验：PROVIDER用户只能删除自己公司的资质
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null) {
            QualificationVO cert = qualificationService.getQualificationById(certId);
            if (cert == null) {
                throw BusinessException.fail("资质不存在");
            }
            if (!autoPid.equals(cert.getProviderId())) {
                throw BusinessException.fail("无权删除其他服务商的资质");
            }
        }
        qualificationService.deleteQualification(certId);
        return Result.success();
    }

    // ==================== 服务商管理员账户接口 ====================

    /**
     * 获取服务商管理员账户
     */
    @GetMapping("/{providerId}/admin-account")
    public Result<User> getProviderAdminAccount(@PathVariable String providerId) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(providerId)) {
            throw BusinessException.fail("无权查看其他服务商管理员账户");
        }
        User user = providerService.getProviderAdminAccount(providerId);
        return Result.success(user);
    }

    /**
     * 重置服务商管理员密码
     */
    @PostMapping("/{providerId}/admin-account/reset")
    public Result<String> resetProviderAdminPassword(@PathVariable String providerId) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(providerId)) {
            throw BusinessException.fail("无权操作其他服务商管理员账户");
        }
        String newPassword = providerService.resetProviderAdminPassword(providerId);
        return Result.success(newPassword);
    }

    // ==================== 服务商服务类型管理接口 ====================

    /**
     * 服务类型配置
     */
    @PostMapping("/{providerId}/service-types")
    public Result<String> createServiceType(
            @PathVariable String providerId,
            @Validated @RequestBody ServiceTypeCreateDTO dto) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        // PROVIDER 用户只能用自己账号的 providerId（长ID），不能用短ID path param
        if ("PROVIDER".equals(userType) && autoPid != null) {
            providerId = autoPid;
        }
        String serviceTypeId = serviceTypeService.createServiceType(providerId, dto);
        return Result.<String>success(serviceTypeId);
    }

    /**
     * 服务类型列表
     */
    @GetMapping("/{providerId}/service-types")
    public Result<List<ServiceTypeVO>> getServiceTypes(@PathVariable String providerId) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null && !autoPid.equals(providerId)) {
            throw BusinessException.fail("无权查看其他服务商服务类型");
        }
        List<ServiceTypeVO> list = serviceTypeService.getServiceTypesByProviderId(providerId);
        return Result.success(list);
    }

    /**
     * 服务类型修改
     * PUT /api/providers/service-types/{serviceTypeId}
     * 隔离：只能改自己公司的服务类型
     */
    @PutMapping("/service-types/{serviceTypeId}")
    public Result<Void> updateServiceType(
            @PathVariable String serviceTypeId,
            @Validated @RequestBody ServiceTypeUpdateDTO dto) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null
                && !serviceTypeService.isServiceTypeOwnedByProvider(serviceTypeId, autoPid)) {
            throw BusinessException.forbidden("无权修改其他公司的服务类型");
        }
        serviceTypeService.updateServiceType(serviceTypeId, dto);
        return Result.success();
    }

    /**
     * 服务类型删除
     * DELETE /api/providers/service-types/{serviceTypeId}
     * 隔离：只能删自己公司的服务类型
     */
    @DeleteMapping("/service-types/{serviceTypeId}")
    public Result<Void> deleteServiceType(@PathVariable String serviceTypeId) {
        String userType = UserContext.getUserType();
        String autoPid = UserContext.getProviderId();
        if ("PROVIDER".equals(userType) && autoPid != null
                && !serviceTypeService.isServiceTypeOwnedByProvider(serviceTypeId, autoPid)) {
            throw BusinessException.forbidden("无权删除其他公司的服务类型");
        }
        serviceTypeService.deleteServiceType(serviceTypeId);
        return Result.success();
    }

    // ==================== 服务商状态管理 ====================

    /**
     * 更新服务商状态
     * PUT /api/providers/{providerId}/status
     * 状态值: ENABLED-正常, DISABLED-禁用, DEMOTED-已降级, ELIMINATED-已淘汰
     */
    @PutMapping("/{providerId}/status")
    public Result<Void> updateProviderStatus(
            @PathVariable String providerId,
            @RequestBody ProviderStatusUpdateDTO dto) {
        providerService.updateProviderStatus(providerId, dto.getStatus());
        return Result.success();
    }
}
