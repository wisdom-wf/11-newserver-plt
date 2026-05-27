-- 清理所有测试数据，保留基础数据

-- 1. 清理业务数据（按依赖关系顺序）
-- 先清理有外键依赖的表

-- 清理服务日志
DELETE FROM service_log WHERE 1=1;
DELETE FROM t_service_log WHERE 1=1;

-- 清理质检记录
DELETE FROM quality_check WHERE 1=1;
DELETE FROM t_quality_check WHERE 1=1;

-- 清理评价
DELETE FROM t_service_evaluation WHERE 1=1;

-- 清理订单
DELETE FROM t_order WHERE 1=1;

-- 清理预约
DELETE FROM t_appointment WHERE 1=1;
DELETE FROM appointment WHERE 1=1;

-- 清理老人档案
DELETE FROM t_elder WHERE 1=1;

-- 清理服务商（可选，根据需求决定）
-- DELETE FROM t_provider WHERE 1=1;

-- 2. 重置自增ID
ALTER TABLE service_log AUTO_INCREMENT = 1;
ALTER TABLE quality_check AUTO_INCREMENT = 1;
ALTER TABLE t_order AUTO_INCREMENT = 1;
ALTER TABLE t_appointment AUTO_INCREMENT = 1;
ALTER TABLE t_elder AUTO_INCREMENT = 1;

-- 3. 验证清理结果
SELECT 'service_log' as table_name, COUNT(*) as count FROM service_log
UNION ALL
SELECT 'quality_check', COUNT(*) FROM quality_check
UNION ALL
SELECT 't_order', COUNT(*) FROM t_order
UNION ALL
SELECT 't_appointment', COUNT(*) FROM t_appointment
UNION ALL
SELECT 't_elder', COUNT(*) FROM t_elder
UNION ALL
SELECT 't_provider', COUNT(*) FROM t_provider;