-- 清空业务数据（用于解决命令行导入导致的乱码问题，保持系统干净）
-- 执行方式: 在 MySQL 客户端中执行，避免命令行编码问题
-- 注意：此操作会清空所有业务数据，请谨慎执行！

-- 禁用外键检查（避免关联表删除限制）
SET FOREIGN_KEY_CHECKS = 0;

-- 清空服务人员相关表
TRUNCATE TABLE t_staff;
TRUNCATE TABLE t_staff_qualification;
TRUNCATE TABLE t_staff_schedule;
TRUNCATE TABLE t_staff_work_record;
TRUNCATE TABLE t_staff_penalty_record;

-- 清空服务商相关表
TRUNCATE TABLE t_provider_qualification;
TRUNCATE TABLE t_provider_service_type;
TRUNCATE TABLE t_provider;

-- 清空老人档案（因为关联了服务商）
TRUNCATE TABLE t_elder;
TRUNCATE TABLE t_elder_demand;
TRUNCATE TABLE t_elder_family;
TRUNCATE TABLE t_elder_health;
TRUNCATE TABLE t_elder_subsidy;

-- 清空预约表
TRUNCATE TABLE t_appointment;
TRUNCATE TABLE appointment;

-- 清空订单相关表
TRUNCATE TABLE t_order;
TRUNCATE TABLE t_order_dispatch;
TRUNCATE TABLE t_service_record;
TRUNCATE TABLE t_service_log;
TRUNCATE TABLE t_quality_check;
TRUNCATE TABLE t_service_evaluation;
TRUNCATE TABLE t_settlement;
TRUNCATE TABLE t_refund;
TRUNCATE TABLE t_customer_feedback;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 验证清空结果
SELECT 't_provider' as table_name, COUNT(*) as record_count FROM t_provider
UNION ALL
SELECT 't_staff', COUNT(*) FROM t_staff
UNION ALL
SELECT 't_elder', COUNT(*) FROM t_elder
UNION ALL
SELECT 't_order', COUNT(*) FROM t_order
UNION ALL
SELECT 't_appointment', COUNT(*) FROM t_appointment
UNION ALL
SELECT 'appointment', COUNT(*) FROM appointment;
