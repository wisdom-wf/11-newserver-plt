package com.elderlycare.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 菜单视图对象
 */
@Data
public class MenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String menuId;

    private String menuCode;

    private String menuName;

    private String parentId;

    private String menuType;

    private String path;

    private String component;

    private String icon;

    private Integer orderNum;

    private Integer isHidden;

    private List<MenuVO> children;
}
