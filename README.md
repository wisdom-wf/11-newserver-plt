# 智慧居家养老服务管理平台 - 前端

基于 Vue3 + NaiveUI 构建的政府监管 + 服务商管理后台系统。

## 技术栈

- Vue 3 + TypeScript + Vite 7
- NaiveUI（UI 组件库）
- Pinia（状态管理）
- Vue Router（路由）
- UnoCSS（原子化 CSS）

## 开发

```bash
# 安装依赖
npm install

# 开发模式（代理到 localhost:8080 后端）
npm run dev

# 生产构建
npm run build
```

## 项目结构

```
src/
├── api/           # API 接口封装
├── components/   # 公共组件
├── layouts/      # 页面布局（BaseLayout / BlankLayout）
├── router/        # 路由配置
├── store/         # Pinia 状态
├── utils/         # 工具函数
└── views/         # 页面视图
    └── business/  # 业务模块
        ├── appointment/    # 预约管理
        ├── cockpit/        # 驾驶舱
        ├── elder/          # 老人档案
        ├── evaluation/     # 服务评价
        ├── financial/      # 财务管理
        ├── order/          # 订单管理
        ├── provider/       # 服务商管理
        ├── quality/        # 质检管理
        ├── service-log/    # 服务日志
        ├── service-type/   # 服务类型
        └── staff/         # 服务人员
```

## 登录说明

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 超级管理员 | admin | admin123 |
| 服务商管理员 | FWS1 / FWS2 | admin123 |
| 服务人员 | STAFF1 | admin123 |

## 相关文档

- [项目总览](../AGENTS.md)
- [需求文档](../docs/requirements/)
- [接口文档](./docs/api/)
