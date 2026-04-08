package com.elderlycare.mapper.elder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.elder.ElderPageDTO;
import com.elderlycare.entity.elder.Elder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 老人档案Mapper
 */
@Mapper
public interface ElderMapper extends BaseMapper<Elder> {

    /**
     * 分页查询老人档案
     */
    IPage<Elder> selectElderPage(Page<Elder> page, @Param("dto") ElderPageDTO dto);
}
