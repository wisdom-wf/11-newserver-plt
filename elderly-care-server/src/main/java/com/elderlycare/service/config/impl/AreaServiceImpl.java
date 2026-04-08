package com.elderlycare.service.config.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.dto.config.AreaDTO;
import com.elderlycare.entity.config.Area;
import com.elderlycare.mapper.config.AreaMapper;
import com.elderlycare.service.config.AreaService;
import com.elderlycare.vo.config.AreaTreeVO;
import com.elderlycare.vo.config.AreaVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 区域Service实现
 */
@Service
@RequiredArgsConstructor
public class AreaServiceImpl extends ServiceImpl<AreaMapper, Area> implements AreaService {

    @Override
    public List<AreaTreeVO> getAreaTree() {
        List<Area> allAreas = baseMapper.selectList(
                new LambdaQueryWrapper<Area>().orderByAsc(Area::getSortOrder)
        );
        return buildTree(allAreas, "0");
    }

    @Override
    public List<Area> listAreas() {
        LambdaQueryWrapper<Area> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Area::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public AreaVO getAreaById(String areaId) {
        Area area = baseMapper.selectById(areaId);
        if (area == null) {
            throw BusinessException.notFound("区域不存在");
        }

        AreaVO vo = new AreaVO();
        BeanUtils.copyProperties(area, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createArea(AreaDTO dto) {
        // 检查编码是否已存在
        if (existsByCode(dto.getAreaCode())) {
            throw BusinessException.fail("区域编码已存在");
        }

        Area area = new Area();
        BeanUtils.copyProperties(dto, area);
        if (dto.getStatus() == null) {
            area.setStatus("ACTIVE");
        }
        if (dto.getParentId() == null) {
            area.setParentId("0");
        }

        baseMapper.insert(area);
        return area.getAreaId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArea(String areaId, AreaDTO dto) {
        Area area = baseMapper.selectById(areaId);
        if (area == null) {
            throw BusinessException.notFound("区域不存在");
        }

        if (StringUtils.isNotBlank(dto.getAreaName())) {
            area.setAreaName(dto.getAreaName());
        }
        if (dto.getParentId() != null) {
            area.setParentId(dto.getParentId());
        }
        if (StringUtils.isNotBlank(dto.getAreaLevel())) {
            area.setAreaLevel(dto.getAreaLevel());
        }
        if (dto.getLongitude() != null) {
            area.setLongitude(dto.getLongitude());
        }
        if (dto.getLatitude() != null) {
            area.setLatitude(dto.getLatitude());
        }
        if (dto.getSortOrder() != null) {
            area.setSortOrder(dto.getSortOrder());
        }
        if (StringUtils.isNotBlank(dto.getStatus())) {
            area.setStatus(dto.getStatus());
        }

        baseMapper.updateById(area);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArea(String areaId) {
        Area area = baseMapper.selectById(areaId);
        if (area == null) {
            throw BusinessException.notFound("区域不存在");
        }

        // 检查是否有子区域
        LambdaQueryWrapper<Area> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Area::getParentId, areaId);
        if (baseMapper.selectCount(wrapper) > 0) {
            throw BusinessException.fail("请先删除子区域");
        }

        // 逻辑删除
        baseMapper.deleteById(areaId);
    }

    private boolean existsByCode(String areaCode) {
        LambdaQueryWrapper<Area> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Area::getAreaCode, areaCode);
        return baseMapper.selectCount(wrapper) > 0;
    }

    private List<AreaTreeVO> buildTree(List<Area> areas, String parentId) {
        return areas.stream()
                .filter(area -> parentId.equals(area.getParentId()))
                .map(area -> {
                    AreaTreeVO vo = new AreaTreeVO();
                    BeanUtils.copyProperties(area, vo);
                    vo.setChildren(buildTree(areas, area.getAreaId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
