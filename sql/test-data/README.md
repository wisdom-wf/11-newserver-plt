# 本地测试数据脚本

本目录中的脚本仅用于开发和端到端测试的数据准备或清理，不属于生产部署流程。

执行前请确认目标数据库为本地测试环境，并使用 UTF-8 连接：

```bash
docker exec -i mysql-dev mysql -uroot -proot elderly_care --default-character-set=utf8mb4 < sql/test-data/<script>.sql
```

生产 schema 与初始化数据应从 `elderly-care-server/sql/production/` 管理。
