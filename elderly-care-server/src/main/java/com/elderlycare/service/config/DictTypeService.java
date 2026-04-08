package com.elderlycare.service.config;

import com.elderlycare.common.PageResult;
import com.elderlycare.dto.config.DictTypeDTO;
import com.elderlycare.entity.config.DictType;
import com.elderlycare.vo.config.DictTypeVO;
import java.util.List;

/**
 * 字典类型Service接口
 */
public interface DictTypeService {

    /**
     * 字典类型列表
     */
    List<DictType> listDictTypes();

    /**
     * 字典类型详情
     */
    DictTypeVO getDictTypeById(String dictTypeId);

    /**
     * 字典类型新增
     */
    String createDictType(DictTypeDTO dto);

    /**
     * 字典类型修改
     */
    void updateDictType(String dictTypeId, DictTypeDTO dto);

    /**
     * 字典类型删除
     */
    void deleteDictType(String dictTypeId);
}
