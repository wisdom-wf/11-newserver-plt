-- 完整业务流程测试数据（修正版）
-- 创建日期: 2026-05-14

-- 1. 创建订单
INSERT INTO t_order (order_id, order_no, elder_id, elder_name, elder_phone, elder_address, service_type_code, service_type_name, service_date, service_time, service_duration, service_address, order_type, order_source, status, provider_id, provider_name, estimated_price, create_time, update_time, deleted) VALUES
('ORDER001', 'ORD20260514001', 'ELDER001', '张大爷', '13900002001', '西安市碑林区长安北路101号', 'HOME_CARE', '居家照料', '2026-05-15', '09:00', 2, '西安市碑林区长安北路101号', 'NORMAL', 'ONLINE', 'CREATED', (SELECT provider_id FROM t_elder WHERE elder_id='ELDER001'), NULL, 200.00, NOW(), NOW(), 0),
('ORDER002', 'ORD20260514002', 'ELDER002', '李奶奶', '13900002002', '西安市雁塔区科技路201号', 'HEALTH_MONITOR', '健康管理', '2026-05-15', '14:00', 1, '西安市雁塔区科技路201号', 'NORMAL', 'PHONE', 'CREATED', (SELECT provider_id FROM t_elder WHERE elder_id='ELDER002'), NULL, 150.00, NOW(), NOW(), 0),
('ORDER003', 'ORD20260514003', 'ELDER003', '王大爷', '13900002003', '西安市未央区凤城五路301号', 'MEAL_DELIVERY', '助餐服务', '2026-05-15', '11:30', 1, '西安市未央区凤城五路301号', 'NORMAL', 'ONLINE', 'CREATED', (SELECT provider_id FROM t_elder WHERE elder_id='ELDER003'), NULL, 50.00, NOW(), NOW(), 0);

-- 2. 创建服务日志
INSERT INTO service_log (service_log_id, log_no, order_id, order_no, elder_id, elder_name, service_type_code, service_type_name, service_date, service_start_time, service_end_time, service_duration, service_content, service_result, staff_id, staff_name, provider_id, provider_name, status, create_time, update_time, deleted) VALUES
('LOG001', 'SL20260514001', 'ORDER001', 'ORD20260514001', 'ELDER001', '张大爷', 'HOME_CARE', '居家照料', '2026-05-15', '09:00', '11:00', 2, '为老人提供日常照料服务，包括清洁、整理等', 'COMPLETED', NULL, NULL, (SELECT provider_id FROM t_elder WHERE elder_id='ELDER001'), NULL, 'SUBMITTED', NOW(), NOW(), 0),
('LOG002', 'SL20260514002', 'ORDER002', 'ORD20260514002', 'ELDER002', '李奶奶', 'HEALTH_MONITOR', '健康管理', '2026-05-15', '14:00', '15:00', 1, '为老人测量血压、血糖等健康指标', 'COMPLETED', NULL, NULL, (SELECT provider_id FROM t_elder WHERE elder_id='ELDER002'), NULL, 'SUBMITTED', NOW(), NOW(), 0);

-- 3. 创建质检记录
INSERT INTO quality_check (quality_check_id, check_no, order_id, order_no, service_log_id, service_category, provider_id, provider_name, check_type, check_method, check_content, check_result, check_score, check_status, check_user_id, check_user_name, create_time, update_time, deleted) VALUES
('QC001', 'QC20260514001', 'ORDER001', 'ORD20260514001', 'LOG001', 'SERVICE_QUALITY', (SELECT provider_id FROM t_elder WHERE elder_id='ELDER001'), NULL, 'SERVICE_QUALITY', 'PHONE', '对居家照料服务质量进行检查', 'GOOD', 85, 'COMPLETED', NULL, NULL, NOW(), NOW(), 0);

-- 4. 验证数据
SELECT '订单数据' as data_type, COUNT(*) as count FROM t_order
UNION ALL
SELECT '服务日志', COUNT(*) FROM service_log
UNION ALL
SELECT '质检记录', COUNT(*) FROM quality_check
UNION ALL
SELECT '老人档案', COUNT(*) FROM t_elder;

-- 5. 显示创建的数据
SELECT '订单详情:' as info;
SELECT order_id, order_no, elder_name, service_type_name, status FROM t_order;

SELECT '服务日志详情:' as info;
SELECT service_log_id, log_no, order_no, elder_name, service_type_name, status FROM service_log;

SELECT '质检记录详情:' as info;
SELECT quality_check_id, check_no, order_no, check_result, check_score FROM quality_check;