-- 老人测试数据（使用现有服务商ID）
-- 创建日期: 2026-05-14

-- 获取前5个可用的服务商ID
SET @provider1 = (SELECT provider_id FROM t_provider WHERE status='ENABLED' LIMIT 1);
SET @provider2 = (SELECT provider_id FROM t_provider WHERE status='ENABLED' LIMIT 1 OFFSET 1);
SET @provider3 = (SELECT provider_id FROM t_provider WHERE status='ENABLED' LIMIT 1 OFFSET 2);
SET @provider4 = (SELECT provider_id FROM t_provider WHERE status='ENABLED' LIMIT 1 OFFSET 3);
SET @provider5 = (SELECT provider_id FROM t_provider WHERE status='ENABLED' LIMIT 1 OFFSET 4);

-- 插入老人测试数据
INSERT IGNORE INTO t_elder (elder_id, elder_name, gender, birth_date, age, id_card, phone, care_type, care_level, health_status, address, area_id, provider_id, emergency_contact, emergency_phone, status, create_time, update_time, deleted) VALUES
('ELDER001', '张大爷', 'MALE', '1945-03-15', 81, '610102194503150011', '13900002001', 'HOME', 'HIGH', 'FAIR', '西安市碑林区长安北路101号', NULL, @provider1, '张小明', '13900003001', 'ACTIVE', NOW(), NOW(), 0),
('ELDER002', '李奶奶', 'FEMALE', '1948-07-22', 78, '610102194807220022', '13900002002', 'HOME', 'MEDIUM', 'GOOD', '西安市雁塔区科技路201号', NULL, @provider2, '李小红', '13900003002', 'ACTIVE', NOW(), NOW(), 0),
('ELDER003', '王大爷', 'MALE', '1942-11-08', 84, '610102194211080033', '13900002003', 'COMMUNITY', 'HIGH', 'POOR', '西安市未央区凤城五路301号', NULL, @provider3, '王小军', '13900003003', 'ACTIVE', NOW(), NOW(), 0),
('ELDER004', '赵阿姨', 'FEMALE', '1950-05-18', 76, '610102195005180044', '13900002004', 'HOME', 'NORMAL', 'GOOD', '西安市长安区韦曲街100号', NULL, @provider4, '赵小芳', '13900003004', 'ACTIVE', NOW(), NOW(), 0),
('ELDER005', '刘大爷', 'MALE', '1940-09-30', 86, '610102194009300055', '13900002005', 'INSTITUTION', 'HIGH', 'POOR', '西安市新城区长乐西路401号', NULL, @provider5, '刘小刚', '13900003005', 'ACTIVE', NOW(), NOW(), 0),
('ELDER006', '陈奶奶', 'FEMALE', '1946-12-01', 80, '610102194612010066', '13900002006', 'HOME', 'MEDIUM', 'FAIR', '西安市莲湖区劳动路501号', NULL, @provider1, '陈小丽', '13900003006', 'ACTIVE', NOW(), NOW(), 0),
('ELDER007', '孙大爷', 'MALE', '1944-04-25', 82, '610102194404250077', '13900002007', 'COMMUNITY', 'NORMAL', 'GOOD', '西安市雁塔区科技路202号', NULL, @provider2, '孙小明', '13900003007', 'ACTIVE', NOW(), NOW(), 0),
('ELDER008', '周阿姨', 'FEMALE', '1952-08-14', 74, '610102195208140088', '13900002008', 'HOME', 'NORMAL', 'GOOD', '西安市未央区凤城五路302号', NULL, @provider3, '周小红', '13900003008', 'ACTIVE', NOW(), NOW(), 0),
('ELDER009', '吴大爷', 'MALE', '1943-01-20', 83, '610102194301200099', '13900002009', 'HOME', 'HIGH', 'FAIR', '西安市长安区韦曲街101号', NULL, @provider4, '吴小强', '13900003009', 'ACTIVE', NOW(), NOW(), 0),
('ELDER010', '黄奶奶', 'FEMALE', '1947-06-05', 79, '610102194706050100', '13900002010', 'COMMUNITY', 'MEDIUM', 'GOOD', '西安市新城区长乐西路402号', NULL, @provider5, '黄小芳', '13900003010', 'ACTIVE', NOW(), NOW(), 0);

-- 验证插入结果
SELECT '老人测试数据插入完成' as result;
SELECT COUNT(*) as elder_count FROM t_elder WHERE elder_id LIKE 'ELDER%';
SELECT elder_id, elder_name, provider_id FROM t_elder LIMIT 5;