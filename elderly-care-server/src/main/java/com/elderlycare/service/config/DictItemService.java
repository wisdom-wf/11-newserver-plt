package com.elderlycare.service.config;

import com.elderlycare.dto.config.DictItemDTO;
import com.elderlycare.entity.config.DictItem;
import com.elderlycare.vo.config.DictItemVO;
import java.util.List;

/**
 * 字典项Service接口
 */
public interface DictItemService {

    /**
     * 根据字典类型编码查询字典项
     */
    List<DictItem> getDictItemsByTypeCode(String dictTypeCode);

    /**
     * 字典项新增
     */
    String createDictItem(DictItemDTO dto);

    /**
     * 字典项修改
     */
    void updateDictItem(String dictItemId, DictItemDTO dto);

    /**
     * 字典项删除
     */
    void deleteDictItem(String dictItemId);
}
