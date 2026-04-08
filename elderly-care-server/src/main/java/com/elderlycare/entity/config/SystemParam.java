package com.elderlycare.entity.config;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统参数实体
 */
@Data
@TableName("t_system_param")
public class SystemParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 参数ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String paramId;

    /**
     * 参数分类
     */
    private String paramCategory;

    /**
     * 参数编码
     */
    private String paramCode;

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数值
     */
    private String paramValue;

    /**
     * 参数类型
     */
    private String paramType;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 参数说明
     */
    private String paramDesc;

    /**
     * 是否系统参数
     */
    private Integer isSystem;

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
