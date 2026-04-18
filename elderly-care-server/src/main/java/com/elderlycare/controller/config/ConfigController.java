package com.elderlycare.controller.config;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.dto.config.*;
import com.elderlycare.entity.config.ConfigServiceType;
import com.elderlycare.entity.config.DictItem;
import com.elderlycare.entity.config.DictType;
import com.elderlycare.entity.config.OperationLog;
import com.elderlycare.entity.config.SystemParam;
import com.elderlycare.service.config.*;
import com.elderlycare.vo.config.*;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置Controller
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final DictTypeService dictTypeService;
    private final DictItemService dictItemService;
    private final ConfigServiceTypeService configServiceTypeService;
    private final SystemParamService systemParamService;
    private final OperationLogService operationLogService;

    // ==================== 字典类型管理接口 ====================

    /**
     * 字典类型列表
     */
    @GetMapping("/dict-types")
    public Result<List<DictType>> listDictTypes() {
        List<DictType> list = dictTypeService.listDictTypes();
        return Result.success(list);
    }

    /**
     * 字典类型详情
     */
    @GetMapping("/dict-types/{dictTypeId}")
    public Result<DictTypeVO> getDictTypeById(@PathVariable String dictTypeId) {
        DictTypeVO vo = dictTypeService.getDictTypeById(dictTypeId);
        return Result.success(vo);
    }

    /**
     * 字典类型新增
     */
    @PostMapping("/dict-types")
    public Result<String> createDictType(@Validated @RequestBody DictTypeDTO dto) {
        String id = dictTypeService.createDictType(dto);
        return Result.success(id);
    }

    /**
     * 字典类型修改
     */
    @PutMapping("/dict-types/{dictTypeId}")
    public Result<Void> updateDictType(
            @PathVariable String dictTypeId,
            @Validated @RequestBody DictTypeDTO dto) {
        dictTypeService.updateDictType(dictTypeId, dto);
        return Result.success();
    }

    /**
     * 字典类型删除
     */
    @DeleteMapping("/dict-types/{dictTypeId}")
    public Result<Void> deleteDictType(@PathVariable String dictTypeId) {
        dictTypeService.deleteDictType(dictTypeId);
        return Result.success();
    }

    // ==================== 字典项管理接口 ====================

    /**
     * 字典项查询(根据字典类型编码)
     */
    @GetMapping("/dict-items/{dictTypeCode}")
    public Result<List<DictItem>> getDictItemsByTypeCode(@PathVariable String dictTypeCode) {
        List<DictItem> list = dictItemService.getDictItemsByTypeCode(dictTypeCode);
        return Result.success(list);
    }

    /**
     * 字典项新增
     */
    @PostMapping("/dict-items")
    public Result<String> createDictItem(@Validated @RequestBody DictItemDTO dto) {
        String id = dictItemService.createDictItem(dto);
        return Result.success(id);
    }

    /**
     * 字典项修改
     */
    @PutMapping("/dict-items/{dictItemId}")
    public Result<Void> updateDictItem(
            @PathVariable String dictItemId,
            @Validated @RequestBody DictItemDTO dto) {
        dictItemService.updateDictItem(dictItemId, dto);
        return Result.success();
    }

    /**
     * 字典项删除
     */
    @DeleteMapping("/dict-items/{dictItemId}")
    public Result<Void> deleteDictItem(@PathVariable String dictItemId) {
        dictItemService.deleteDictItem(dictItemId);
        return Result.success();
    }

    // ==================== 服务类型管理接口 ====================

    /**
     * 服务类型列表
     */
    @GetMapping("/service-types")
    public Result<List<ConfigServiceType>> listServiceTypes() {
        List<ConfigServiceType> list = configServiceTypeService.listServiceTypes();
        return Result.success(list);
    }

    /**
     * 服务类型详情
     */
    @GetMapping("/service-types/{serviceTypeId}")
    public Result<ConfigServiceTypeVO> getServiceTypeById(@PathVariable String serviceTypeId) {
        ConfigServiceTypeVO vo = configServiceTypeService.getServiceTypeById(serviceTypeId);
        return Result.success(vo);
    }

    /**
     * 服务类型新增
     */
    @PostMapping("/service-types")
    public Result<String> createServiceType(@Validated @RequestBody ConfigServiceTypeDTO dto) {
        String id = configServiceTypeService.createServiceType(dto);
        return Result.success(id);
    }

    /**
     * 服务类型修改
     */
    @PutMapping("/service-types/{serviceTypeId}")
    public Result<Void> updateServiceType(
            @PathVariable String serviceTypeId,
            @Validated @RequestBody ConfigServiceTypeDTO dto) {
        configServiceTypeService.updateServiceType(serviceTypeId, dto);
        return Result.success();
    }

    /**
     * 服务类型删除
     */
    @DeleteMapping("/service-types/{serviceTypeId}")
    public Result<Void> deleteServiceType(@PathVariable String serviceTypeId) {
        configServiceTypeService.deleteServiceType(serviceTypeId);
        return Result.success();
    }

    // ==================== 系统参数接口 ====================

    /**
     * 参数列表
     */
    @GetMapping("/params")
    public Result<List<SystemParam>> listParams() {
        List<SystemParam> list = systemParamService.listParams();
        return Result.success(list);
    }

    /**
     * 参数详情
     */
    @GetMapping("/params/{paramCode}")
    public Result<SystemParamVO> getParamByCode(@PathVariable String paramCode) {
        SystemParamVO vo = systemParamService.getParamByCode(paramCode);
        return Result.success(vo);
    }

    /**
     * 参数修改
     */
    @PutMapping("/params/{paramCode}")
    public Result<Void> updateParam(
            @PathVariable String paramCode,
            @RequestBody SystemParamDTO dto) {
        systemParamService.updateParam(paramCode, dto);
        return Result.success();
    }

    // ==================== 操作日志接口 ====================

    /**
     * 日志查询
     */
    @GetMapping("/operation-logs")
    public Result<PageResult<OperationLog>> queryOperationLogs(OperationLogQueryDTO dto) {
        PageResult<OperationLog> result = operationLogService.queryOperationLogs(dto);
        return Result.success(result);
    }

    /**
     * 日志详情
     */
    @GetMapping("/operation-logs/{operationLogId}")
    public Result<OperationLogVO> getOperationLogById(@PathVariable String operationLogId) {
        OperationLogVO vo = operationLogService.getOperationLogById(operationLogId);
        return Result.success(vo);
    }
}
