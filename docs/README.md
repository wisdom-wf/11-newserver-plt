# 文档导航与归档规则

本目录保存可持续维护的项目文档。根目录只保留工程入口文件，例如
`README.md`、`AGENTS.md`、`HANDOFF.md` 和主 `CHANGELOG.md`。

## 目录约定

| 目录 | 内容 | 维护规则 |
| --- | --- | --- |
| `requirements/` | 需求规格与差距分析 | 需求变更时更新 |
| `design/` | 架构、接口、数据模型和专项设计 | 与实现变更同步 |
| `deployment/` | 当前可执行的部署文档 | 只保留有效流程 |
| `changelogs/` | 历史版本、部署和专项变更记录 | 归档后不作为当前操作指南 |
| `evaluations/` | 阶段性评估与改进路线 | 保留结论和日期 |
| `reports/testing/` | 历史测试报告与问题截图 | 作为验收证据归档 |
| `reports/audits/` | 代码、数据一致性和流程审查报告 | 作为诊断证据归档 |
| `reports/operations/` | 环境清理等运维过程报告 | 仅供追溯 |
| `presentations/` | 汇报与推介资料 | 输出文件可保留，构建产物不入库 |
| `推介资料/` | 现有文档生成源码及输出 | 暂保留原路径，`bin/obj` 不入库 |
| `diagnostics/` | 工程治理与专项诊断记录 | 跟踪待治理项 |

## 数据库脚本约定

- 生产部署入口以 `elderly-care-server/sql/production/` 为目标目录。
- `elderly-care-server/sql/archive/` 仅保存历史迁移和排障脚本。
- 根目录 `sql/test-data/` 保存本地测试数据脚本，不用于生产部署。
- `elderly-care-server/src/main/resources/sql/` 现有脚本仍需后续统一迁移来源，
  在完成核对前不要同时修改两套生产 schema。

## 不入库内容

- Playwright/MCP 运行日志、截图缓存和测试报告构建产物。
- `bin/`、`obj/`、`dist/`、`target/` 等编译输出。
- `.DS_Store`、编辑器本地配置和临时日志。

工程目录治理的当前诊断和分步计划见
[工程目录治理诊断与实施计划_20260527.md](diagnostics/工程目录治理诊断与实施计划_20260527.md)。

面向 Claude Code 的直接接续入口见
[CLAUDE_CODE_HANDOFF_20260527.md](diagnostics/CLAUDE_CODE_HANDOFF_20260527.md)。
