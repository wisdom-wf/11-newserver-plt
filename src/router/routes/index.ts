import type { CustomRoute, ElegantConstRoute, ElegantRoute } from '@elegant-router/types';
import { generatedRoutes } from '../elegant/routes';
import { layouts, views } from '../elegant/imports';
import { transformElegantRoutesToVueRoutes } from '../elegant/transform';

/**
 * custom routes
 *
 * @link https://github.com/soybeanjs/elegant-router?tab=readme-ov-file#custom-route
 */
const customRoutes: CustomRoute[] = [
  // 公开路由 - 使用空白布局，无侧边栏和头部
  {
    name: 'public',
    path: '/public',
    component: 'layout.blank',
    meta: {
      constant: true,
      hideInMenu: true
    },
    children: [
      {
        name: 'public_dashboard',
        path: '/public/dashboard',
        component: 'view.public_dashboard',
        meta: {
          constant: true,
          hideInMenu: true
        }
      },
      {
        name: 'public_mobile',
        path: '/public/mobile',
        component: 'view.public_mobile',
        meta: {
          constant: true,
          hideInMenu: true
        }
      },
      {
        name: 'public_survey',
        path: '/public/survey',
        component: 'view.public_survey',
        meta: {
          constant: true,
          hideInMenu: true
        }
      }
    ]
  }
];

/** create routes when the auth route mode is static */
export function createStaticRoutes() {
  const routeMap = new Map<string, ElegantRoute>();

  // 先加入 generatedRoutes
  generatedRoutes.forEach(item => routeMap.set(item.name, item));

  // 再加入 customRoutes，让后者覆盖前者
  customRoutes.forEach(item => routeMap.set(item.name, item));

  const constantRoutes: ElegantRoute[] = [];
  const authRoutes: ElegantRoute[] = [];

  routeMap.forEach(item => {
    if (item.meta?.constant) {
      constantRoutes.push(item);
    } else {
      authRoutes.push(item);
    }
  });

  return {
    constantRoutes,
    authRoutes
  };
}

/**
 * Get auth vue routes
 *
 * @param routes Elegant routes
 */
export function getAuthVueRoutes(routes: ElegantConstRoute[]) {
  return transformElegantRoutesToVueRoutes(routes, layouts, views);
}
