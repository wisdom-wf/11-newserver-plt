package com.elderlycare.service.config.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.dto.config.DictTypeDTO;
import com.elderlycare.entity.config.DictType;
import com.elderlycare.mapper.config.DictTypeMapper;
import com.elderlycare.service.config.DictTypeService;
import com.elderlycare.vo.config.DictTypeVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典类型Service实现
 */
@Service
@RequiredArgsConstructor
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictType> implements DictTypeService {

    @Override
    public List<DictType> listDictTypes() {
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(DictType::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public DictTypeVO getDictTypeById(String dictTypeId) {
        DictType dictType = baseMapper.selectById(dictTypeId);
        if (dictType == null) {
            throw BusinessException.notFound("字典类型不存在");
        }

        DictTypeVO vo = new DictTypeVO();
        BeanUtils.copyProperties(dictType, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createDictType(DictTypeDTO dto) {
        // 检查编码是否已存在
        if (existsByCode(dto.getDictTypeCode())) {
            throw BusinessException.fail("字典类型编码已存在");
        }

        DictType dictType = new DictType();
        BeanUtils.copyProperties(dto, dictType);
        if (dto.getStatus() == null) {
            dictType.setStatus("NORMAL");
        }

        baseMapper.insert(dictType);
        return dictType.getDictTypeId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictType(String dictTypeId, DictTypeDTO dto) {
        DictType dictType = baseMapper.selectById(dictTypeId);
        if (dictType == null) {
            throw BusinessException.notFound("字典类型不存在");
        }

        if (dictType.getIsSystem() != null && dictType.getIsSystem() == 1) {
            throw BusinessException.fail("系统字典不允许修改");
        }

        if (StringUtils.isNotBlank(dto.getDictTypeName())) {
            dictType.setDictTypeName(dto.getDictTypeName());
        }
        if (StringUtils.isNotBlank(dto.getDictTypeDesc())) {
            dictType.setDictTypeDesc(dto.getDictTypeDesc());
        }
        if (dto.getSortOrder() != null) {
            dictType.setSortOrder(dto.getSortOrder());
        }
        if (StringUtils.isNotBlank(dto.getStatus())) {
            dictType.setStatus(dto.getStatus());
        }

        baseMapper.updateById(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictType(String dictTypeId) {
        DictType dictType = baseMapper.selectById(dictTypeId);
        if (dictType == null) {
            throw BusinessException.notFound("字典类型不存在");
        }

        if (dictType.getIsSystem() != null && dictType.getIsSystem() == 1) {
            throw BusinessException.fail("系统字典不允许删除");
        }

        // 逻辑删除
        baseMapper.deleteById(dictTypeId);
    }

    private boolean existsByCode(String dictTypeCode) {
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictType::getDictTypeCode, dictTypeCode);
        return baseMapper.selectCount(wrapper) > 0;
    }
}
