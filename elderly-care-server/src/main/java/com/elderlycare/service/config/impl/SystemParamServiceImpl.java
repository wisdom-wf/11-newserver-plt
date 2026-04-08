package com.elderlycare.service.config.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.dto.config.SystemParamDTO;
import com.elderlycare.entity.config.SystemParam;
import com.elderlycare.mapper.config.SystemParamMapper;
import com.elderlycare.service.config.SystemParamService;
import com.elderlycare.vo.config.SystemParamVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统参数Service实现
 */
@Service
@RequiredArgsConstructor
public class SystemParamServiceImpl extends ServiceImpl<SystemParamMapper, SystemParam>
        implements SystemParamService {

    @Override
    public List<SystemParam> listParams() {
        LambdaQueryWrapper<SystemParam> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SystemParam::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public SystemParamVO getParamByCode(String paramCode) {
        SystemParam param = baseMapper.selectById(paramCode);
        if (param == null) {
            // 尝试用编码查询
            LambdaQueryWrapper<SystemParam> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemParam::getParamCode, paramCode);
            param = baseMapper.selectOne(wrapper);
        }
        if (param == null) {
            throw BusinessException.notFound("系统参数不存在");
        }

        SystemParamVO vo = new SystemParamVO();
        BeanUtils.copyProperties(param, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateParam(String paramCode, SystemParamDTO dto) {
        LambdaQueryWrapper<SystemParam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemParam::getParamCode, paramCode);
        SystemParam param = baseMapper.selectOne(wrapper);

        if (param == null) {
            throw BusinessException.notFound("系统参数不存在");
        }

        if (param.getIsSystem() != null && param.getIsSystem() == 1) {
            // 系统参数只允许修改参数值和参数说明
            if (StringUtils.isNotBlank(dto.getParamValue())) {
                param.setParamValue(dto.getParamValue());
            }
            if (StringUtils.isNotBlank(dto.getParamDesc())) {
                param.setParamDesc(dto.getParamDesc());
            }
        } else {
            // 非系统参数可以修改更多字段
            if (StringUtils.isNotBlank(dto.getParamName())) {
                param.setParamName(dto.getParamName());
            }
            if (StringUtils.isNotBlank(dto.getParamValue())) {
                param.setParamValue(dto.getParamValue());
            }
            if (StringUtils.isNotBlank(dto.getParamType())) {
                param.setParamType(dto.getParamType());
            }
            if (StringUtils.isNotBlank(dto.getParamDesc())) {
                param.setParamDesc(dto.getParamDesc());
            }
            if (dto.getSortOrder() != null) {
                param.setSortOrder(dto.getSortOrder());
            }
            if (StringUtils.isNotBlank(dto.getStatus())) {
                param.setStatus(dto.getStatus());
            }
        }

        baseMapper.updateById(param);
    }
}
