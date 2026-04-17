-- 清空测试数据（保留基础数据如服务商、区域等）

-- 清空服务日志
DELETE FROM t_service_log WHERE 1=1;

-- 清空评价
DELETE FROM t_evaluation WHERE 1=1;

-- 清空质检单
DELETE FROM t_quality_check WHERE 1=1;

-- 清空财务记录
DELETE FROM t_financial_record WHERE 1=1;

-- 清空订单
DELETE FROM t_order WHERE 1=1;

-- 清空老人档案
DELETE FROM t_elder WHERE 1=1;

-- 验证清空结果
SELECT 't_elder' as table_name, COUNT(*) as count FROM t_elder
UNION ALL
SELECT 't_order', COUNT(*) FROM t_order
UNION ALL
SELECT 't_service_log', COUNT(*) FROM t_service_log
UNION ALL
SELECT 't_evaluation', COUNT(*) FROM t_evaluation
UNION ALL
SELECT 't_quality_check', COUNT(*) FROM t_quality_check
UNION ALL
SELECT 't_financial_record', COUNT(*) FROM t_financial_record;
