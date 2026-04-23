/**
 * 菜单管理模块 - 动态菜单
 */

declare namespace Api {
  namespace Menu {
    /** 菜单项 */
    interface MenuItem {
      menuId: string;
      menuCode: string;
      menuName: string;
      parentId: string | null;
      menuType: string;
      path: string;
      component: string;
      icon: string;
      orderNum: number;
      isHidden: number;
      children?: MenuItem[];
    }

    /** 菜单树 */
    type MenuTree = MenuItem;
  }
}
