package com.elderlycare.entity.config;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据字典项实体
 */
@Data
@TableName("t_dict_item")
public class DictItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典项ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String dictItemId;

    /**
     * 字典类型ID
     */
    private String dictTypeId;

    /**
     * 字典项编码
     */
    private String dictItemCode;

    /**
     * 字典项名称
     */
    private String dictItemName;

    /**
     * 字典项值
     */
    private String dictItemValue;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记
     */
    @TableLogic
    private Integer deleted;
}
