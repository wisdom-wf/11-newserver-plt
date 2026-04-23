package com.elderlycare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单实体
 */
@Data
@TableName("t_menu")
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 菜单ID */
    @TableId(type = IdType.ASSIGN_ID)
    private String menuId;

    /** 菜单代码(路由name) */
    private String menuCode;

    /** 菜单名称 */
    private String menuName;

    /** 父菜单ID */
    private String parentId;

    /** 类型 MENU/BUTTON */
    private String menuType;

    /** 路由路径 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer orderNum;

    /** 是否隐藏 0-否 1-是 */
    private Integer isHidden;

    /** 状态 */
    private String status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 删除标记 */
    @TableLogic
    private Integer deleted;
}
