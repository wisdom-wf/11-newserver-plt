package com.elderlycare.mapper.config;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.config.DictItem;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 字典项Mapper接口
 */
public interface DictItemMapper extends BaseMapper<DictItem> {

    /**
     * 根据字典类型编码查询字典项
     */
    List<DictItem> selectByDictTypeCode(@Param("dictTypeCode") String dictTypeCode);
}
