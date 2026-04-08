package com.elderlycare.service.config.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.dto.config.DictItemDTO;
import com.elderlycare.entity.config.DictItem;
import com.elderlycare.entity.config.DictType;
import com.elderlycare.mapper.config.DictItemMapper;
import com.elderlycare.mapper.config.DictTypeMapper;
import com.elderlycare.service.config.DictItemService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典项Service实现
 */
@Service
@RequiredArgsConstructor
public class DictItemServiceImpl extends ServiceImpl<DictItemMapper, DictItem> implements DictItemService {

    private final DictTypeMapper dictTypeMapper;

    @Override
    public List<DictItem> getDictItemsByTypeCode(String dictTypeCode) {
        return baseMapper.selectByDictTypeCode(dictTypeCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createDictItem(DictItemDTO dto) {
        // 检查字典类型是否存在
        DictType dictType = dictTypeMapper.selectById(dto.getDictTypeId());
        if (dictType == null) {
            throw BusinessException.notFound("字典类型不存在");
        }

        DictItem dictItem = new DictItem();
        BeanUtils.copyProperties(dto, dictItem);
        if (dto.getStatus() == null) {
            dictItem.setStatus("NORMAL");
        }

        baseMapper.insert(dictItem);
        return dictItem.getDictItemId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictItem(String dictItemId, DictItemDTO dto) {
        DictItem dictItem = baseMapper.selectById(dictItemId);
        if (dictItem == null) {
            throw BusinessException.notFound("字典项不存在");
        }

        if (StringUtils.isNotBlank(dto.getDictItemName())) {
            dictItem.setDictItemName(dto.getDictItemName());
        }
        if (StringUtils.isNotBlank(dto.getDictItemValue())) {
            dictItem.setDictItemValue(dto.getDictItemValue());
        }
        if (dto.getSortOrder() != null) {
            dictItem.setSortOrder(dto.getSortOrder());
        }
        if (StringUtils.isNotBlank(dto.getStatus())) {
            dictItem.setStatus(dto.getStatus());
        }

        baseMapper.updateById(dictItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictItem(String dictItemId) {
        DictItem dictItem = baseMapper.selectById(dictItemId);
        if (dictItem == null) {
            throw BusinessException.notFound("字典项不存在");
        }

        // 逻辑删除
        baseMapper.deleteById(dictItemId);
    }
}
