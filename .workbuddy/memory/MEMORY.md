# 智慧养老平台 - 长期记忆

## 数据库配置

### 连接信息
- **数据库类型**: MySQL 8.0+
- **连接地址**: `localhost:3306`
- **数据库名**: `elderly_care`
- **用户名**: `root`
- **密码**: `root`
- **驱动**: `com.mysql.cj.jdbc.Driver`

### JDBC URL
```
jdbc:mysql://localhost:3306/elderly_care?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&connectionCollation=utf8mb4_unicode_ci
```

### MyBatis-Plus 配置
- **ID生成策略**: `assign_id` (雪花算法)
- **逻辑删除字段**: `deleted`
- **逻辑删除值**: `1` (已删除)
- **未删除值**: `0` (未删除)
- **下划线转驼峰**: 开启 (`map-underscore-to-camel-case: true`)

### 核心数据表
| 表名 | 说明 | 状态 |
|------|------|------|
| `t_order` | 订单表 | 已创建 |
| `appointment` | 预约表 | 已创建 |
| `elder` | 老人档案表 | 已创建 |
| `staff` | 服务人员表 | 已创建 |
| `provider` | 服务商表 | 已创建 |
| `service_log` | 服务日志表 | 已创建 |
| `quality_check` | 质检单表 | 已创建 |
| `evaluation` | 评价表 | 已创建 |
| `financial_record` | 财务记录表 | 已创建 |

---

## 项目结构

### 后端 (elderly-care-server)
- **框架**: Spring Boot 2.7.x
- **ORM**: MyBatis-Plus
- **数据库**: MySQL 8.0
- **端口**: 8080
- **JDK**: 17

### 前端 (dingfeng-work)
- **框架**: Vue 3 + TypeScript
- **UI库**: Naive UI
- **构建工具**: Vite
- **端口**: 9527
- **基础框架**: Soybean Admin

---

*最后更新: 2026-04-16*
