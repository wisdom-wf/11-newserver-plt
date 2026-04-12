-- ============================================
-- 智慧居家养老服务管理平台 - 模拟数据重建脚本
-- 数据库: elderly_care
-- 日期: 2026-04-12
-- ============================================

USE elderly_care;

-- ============================================
-- 第一步：清理旧测试数据
-- ============================================

TRUNCATE TABLE service_log;
TRUNCATE TABLE appointment;
TRUNCATE TABLE quality_check;
TRUNCATE TABLE t_service_evaluation;
TRUNCATE TABLE t_settlement;

-- ============================================
-- 第二步：修复数据一致性问题并补充完整员工信息
-- ============================================

-- 实际的provider UUID:
-- 4253cf396b68903505ea8f74b3d37700 - 延安市宝塔区丁峰家政服务有限公司
-- b147a7db096643eb97385136c555f933 - 延安家享悠网络科技有限公司
-- c49391a097025dcb9bf629975df2726c - 延安丁峰窑洞养老服务有限公司

UPDATE t_staff SET provider_id = 'b147a7db096643eb97385136c555f933' WHERE staff_id IN ('S003', 'S004');
UPDATE t_staff SET provider_id = 'c49391a097025dcb9bf629975df2726c' WHERE staff_id IN ('S005', 'S006');
UPDATE t_staff SET provider_id = '4253cf396b68903505ea8f74b3d37700' WHERE staff_id IN ('S007');
UPDATE t_staff SET provider_id = 'b147a7db096643eb97385136c555f933' WHERE staff_id IN ('S008');

-- 补充员工完整信息
UPDATE t_staff SET
    id_card = CASE staff_id
        WHEN 'S001' THEN '610602198503150013'
        WHEN 'S002' THEN '61060219780722001X'
        WHEN 'S003' THEN '610602199011080033'
        WHEN 'S004' THEN '610602198205300044'
        WHEN 'S005' THEN '610602198809120055'
        WHEN 'S006' THEN '610602199201250066'
        WHEN 'S007' THEN '610602199506180077'
        WHEN 'S008' THEN '610602198012030088'
        ELSE id_card
    END,
    age = CASE staff_id
        WHEN 'S001' THEN TIMESTAMPDIFF(YEAR, '1985-03-15', CURDATE())
        WHEN 'S002' THEN TIMESTAMPDIFF(YEAR, '1978-07-22', CURDATE())
        WHEN 'S003' THEN TIMESTAMPDIFF(YEAR, '1990-11-08', CURDATE())
        WHEN 'S004' THEN TIMESTAMPDIFF(YEAR, '1982-05-30', CURDATE())
        WHEN 'S005' THEN TIMESTAMPDIFF(YEAR, '1988-09-12', CURDATE())
        WHEN 'S006' THEN TIMESTAMPDIFF(YEAR, '1992-01-25', CURDATE())
        WHEN 'S007' THEN TIMESTAMPDIFF(YEAR, '1995-06-18', CURDATE())
        WHEN 'S008' THEN TIMESTAMPDIFF(YEAR, '1980-12-03', CURDATE())
    END,
    emergency_contact = CASE staff_id
        WHEN 'S001' THEN '张建国'
        WHEN 'S002' THEN '李秀英'
        WHEN 'S003' THEN '王大海'
        WHEN 'S004' THEN '陈淑芬'
        WHEN 'S005' THEN '林志强'
        WHEN 'S006' THEN '周桂英'
        WHEN 'S007' THEN '吴建平'
        WHEN 'S008' THEN '黄丽华'
        ELSE emergency_contact
    END,
    emergency_phone = CASE staff_id
        WHEN 'S001' THEN '13800138011'
        WHEN 'S002' THEN '13800138012'
        WHEN 'S003' THEN '13800138013'
        WHEN 'S004' THEN '13800138014'
        WHEN 'S005' THEN '13800138015'
        WHEN 'S006' THEN '13800138016'
        WHEN 'S007' THEN '13800138017'
        WHEN 'S008' THEN '13800138018'
        ELSE emergency_phone
    END,
    education = CASE staff_id
        WHEN 'S001' THEN 'COLLEGE'
        WHEN 'S002' THEN 'BACHELOR'
        WHEN 'S003' THEN 'SENIOR'
        WHEN 'S004' THEN 'COLLEGE'
        WHEN 'S005' THEN 'BACHELOR'
        WHEN 'S006' THEN 'COLLEGE'
        WHEN 'S007' THEN 'BACHELOR'
        WHEN 'S008' THEN 'SENIOR'
        ELSE education
    END,
    address = CASE staff_id
        WHEN 'S001' THEN '延安市宝塔区中心街1号'
        WHEN 'S002' THEN '延安市宝塔区东关街2号'
        WHEN 'S003' THEN '延安市安塞区真武洞3号'
        WHEN 'S004' THEN '延安市宝塔区南关街4号'
        WHEN 'S005' THEN '延安市宝塔区北关街5号'
        WHEN 'S006' THEN '延安市宝塔区市场街6号'
        WHEN 'S007' THEN '延安市宝塔区新建路7号'
        WHEN 'S008' THEN '延安市宝塔区七里铺街8号'
        ELSE address
    END,
    hire_date = CASE staff_id
        WHEN 'S001' THEN '2022-03-15'
        WHEN 'S002' THEN '2022-05-20'
        WHEN 'S003' THEN '2023-01-10'
        WHEN 'S004' THEN '2023-04-25'
        WHEN 'S005' THEN '2023-07-08'
        WHEN 'S006' THEN '2023-09-12'
        WHEN 'S007' THEN '2024-01-18'
        WHEN 'S008' THEN '2024-02-22'
        ELSE hire_date
    END,
    nation = '汉族',
    political_status = CASE staff_id
        WHEN 'S002' THEN 'COMMUNIST'
        WHEN 'S006' THEN 'COMMUNIST'
        ELSE 'MEMBER'
    END,
    marital_status = CASE staff_id
        WHEN 'S001' THEN 'MARRIED'
        WHEN 'S002' THEN 'MARRIED'
        WHEN 'S003' THEN 'UNMARRIED'
        WHEN 'S004' THEN 'MARRIED'
        WHEN 'S005' THEN 'UNMARRIED'
        WHEN 'S006' THEN 'MARRIED'
        WHEN 'S007' THEN 'UNMARRIED'
        WHEN 'S008' THEN 'MARRIED'
        ELSE marital_status
    END,
    work_status = 'IDLE',
    remark = CASE staff_id
        WHEN 'S001' THEN '资深护理员，擅长老人日常照料'
        WHEN 'S002' THEN '金牌服务员，客户好评率高'
        WHEN 'S003' THEN '专业护士资格，医疗护理经验足'
        WHEN 'S004' THEN '居家保洁专家，服务细致耐心'
        WHEN 'S005' THEN '康复护理师，擅长术后康复'
        WHEN 'S006' THEN '家政服务多年，口碑良好'
        WHEN 'S007' THEN '年轻有活力，学习能力强'
        WHEN 'S008' THEN '经验丰富，应变能力强'
        ELSE remark
    END
WHERE staff_id IN ('S001', 'S002', 'S003', 'S004', 'S005', 'S006', 'S007', 'S008');

-- ============================================
-- 第三步：更新部分订单为已完成
-- ============================================

UPDATE t_order
SET status = 'COMPLETED',
    service_duration = CASE
        WHEN service_type_code = '05' THEN 120
        WHEN service_type_code = '06' THEN 90
        WHEN service_type_code = '01' THEN 60
        WHEN service_type_code = '02' THEN 480
        WHEN service_type_code = '03' THEN 90
        WHEN service_type_code = '04' THEN 90
        WHEN service_type_code = '07' THEN 60
        WHEN service_type_code = '08' THEN 120
        WHEN service_type_code = '09' THEN 60
        ELSE 120
    END,
    actual_price = COALESCE(estimated_price, 80),
    update_time = NOW()
WHERE order_id IN (
    SELECT order_id FROM (
        SELECT order_id FROM t_order
        WHERE status = 'DISPATCHED'
        AND staff_id IS NOT NULL
        AND provider_id IS NOT NULL
        LIMIT 150
    ) AS tmp
);

-- ============================================
-- 第四步：创建临时员工信息表
-- ============================================

CREATE TEMPORARY TABLE staff_info (
    staff_id VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    staff_name VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    staff_phone VARCHAR(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
);

INSERT INTO staff_info VALUES
('S001', '张小梅', '13900139001'),
('S002', '李建国', '13900139002'),
('S003', '王秀芳', '13900139003'),
('S004', '陈伟强', '13900139004'),
('S005', '林美红', '13900139005'),
('S006', '周志远', '13900139006'),
('S007', '吴静怡', '13900139007'),
('S008', '黄建国', '13900139008');

-- ============================================
-- 第五步：生成服务日志
-- ============================================

INSERT INTO service_log (
    service_log_id, log_no, order_id, order_no, elder_id, elder_name,
    elder_phone, elder_address, staff_id, staff_name, staff_phone,
    provider_id, provider_name, service_type_code, service_type_name,
    service_date, service_start_time, service_end_time, service_duration,
    service_status, actual_duration, service_score, service_comment, create_time
)
SELECT
    MD5(CONCAT(o.order_id, 'log')) AS service_log_id,
    CONCAT('SL', REPLACE(o.service_date, '-', ''), SUBSTRING(o.order_id, 1, 4)) AS log_no,
    o.order_id,
    o.order_no,
    o.elder_id,
    o.elder_name,
    o.elder_phone,
    o.elder_address,
    o.staff_id,
    si.staff_name,
    si.staff_phone,
    o.provider_id,
    CASE o.provider_id
        WHEN '4253cf396b68903505ea8f74b3d37700' THEN '延安市宝塔区丁峰家政服务有限公司'
        WHEN 'b147a7db096643eb97385136c555f933' THEN '延安家享悠网络科技有限公司'
        WHEN 'c49391a097025dcb9bf629975df2726c' THEN '延安丁峰窑洞养老服务有限公司'
        ELSE '未知服务商'
    END AS provider_name,
    o.service_type_code,
    o.service_type_name,
    o.service_date,
    CONCAT(o.service_date, ' 09:00:00') AS service_start_time,
    DATE_ADD(CONCAT(o.service_date, ' 09:00:00'), INTERVAL o.service_duration MINUTE) AS service_end_time,
    o.service_duration,
    'COMPLETED' AS service_status,
    o.service_duration AS actual_duration,
    ROUND(80 + RAND() * 20, 2) AS service_score,
    '服务态度好，非常满意' AS service_comment,
    NOW() AS create_time
FROM t_order o
INNER JOIN staff_info si ON o.staff_id = si.staff_id
WHERE o.status = 'COMPLETED'
LIMIT 200;

-- ============================================
-- 第六步：生成服务评价
-- ============================================

INSERT INTO t_service_evaluation (
    evaluation_id, order_id, elder_id, provider_id, staff_id,
    attitude_score, quality_score, efficiency_score, overall_score, average_score,
    evaluation_content, evaluation_tags, is_anonymous, evaluation_time, create_time
)
SELECT
    MD5(CONCAT(order_id, 'eval')) AS evaluation_id,
    order_id,
    elder_id,
    provider_id,
    staff_id,
    ROUND(75 + RAND() * 25) AS attitude_score,
    ROUND(75 + RAND() * 25) AS quality_score,
    ROUND(75 + RAND() * 25) AS efficiency_score,
    ROUND(75 + RAND() * 25) AS overall_score,
    ROUND(75 + RAND() * 25, 2) AS average_score,
    '服务非常专业，态度热情，很满意！' AS evaluation_content,
    '服务态度好,准时上门' AS evaluation_tags,
    0 AS is_anonymous,
    service_date AS evaluation_time,
    NOW() AS create_time
FROM t_order
WHERE status = 'COMPLETED'
LIMIT 100;

-- ============================================
-- 第七步：生成结算数据
-- ============================================

INSERT INTO t_settlement (
    settlement_id, settlement_no, order_id, provider_id, staff_id, elder_id,
    service_date, service_duration, unit_price, total_amount,
    subsidy_amount, self_pay_amount, payment_method, payment_status,
    settlement_time, create_time
)
SELECT
    MD5(CONCAT(order_id, 'stl')) AS settlement_id,
    CONCAT('ST', REPLACE(service_date, '-', ''), SUBSTRING(order_id, 1, 4)) AS settlement_no,
    order_id,
    provider_id,
    staff_id,
    elder_id,
    service_date,
    service_duration,
    actual_price AS unit_price,
    actual_price AS total_amount,
    CASE
        WHEN subsidy_type = 'GOVERNMENT' THEN actual_price
        WHEN subsidy_type = 'MIXED' THEN actual_price * 0.6
        ELSE 0
    END AS subsidy_amount,
    CASE
        WHEN subsidy_type = 'SELF_PAY' THEN actual_price
        WHEN subsidy_type = 'MIXED' THEN actual_price * 0.4
        ELSE 0
    END AS self_pay_amount,
    'GOVERNMENT' AS payment_method,
    'PAID' AS payment_status,
    DATE_ADD(service_date, INTERVAL 7 DAY) AS settlement_time,
    NOW() AS create_time
FROM t_order
WHERE status = 'COMPLETED'
AND provider_id IN ('4253cf396b68903505ea8f74b3d37700', 'b147a7db096643eb97385136c555f933', 'c49391a097025dcb9bf629975df2726c')
LIMIT 100;

-- ============================================
-- 第八步：生成预约数据
-- ============================================

INSERT INTO appointment (
    appointment_id, appointment_no, elder_name, elder_id_card, elder_phone,
    elder_address, elder_area_name, service_type, service_type_code,
    appointment_time, service_duration, provider_id, provider_name,
    status, remark, create_time
)
SELECT
    MD5(CONCAT(elder_id, 'apt')) AS appointment_id,
    CONCAT('APT', DATE_FORMAT(NOW(), '%Y%m%d'), SUBSTRING(elder_id, 1, 3)) AS appointment_no,
    elder_name,
    CONCAT('610602195', FLOOR(19500000 + RAND() * 9999999)) AS elder_id_card,
    elder_phone,
    elder_address,
    '宝塔区' AS elder_area_name,
    '上门服务' AS service_type,
    '05' AS service_type_code,
    DATE_ADD(NOW(), INTERVAL 3 DAY) AS appointment_time,
    120 AS service_duration,
    '4253cf396b68903505ea8f74b3d37700' AS provider_id,
    '延安市宝塔区丁峰家政服务有限公司' AS provider_name,
    'PENDING' AS status,
    '日常养老服务预约' AS remark,
    NOW() AS create_time
FROM t_elder
WHERE elder_id IN (SELECT elder_id FROM t_order LIMIT 30);

-- ============================================
-- 第九步：生成质检数据
-- ============================================

INSERT INTO quality_check (
    quality_check_id, check_no, order_id, order_no, provider_id, provider_name,
    staff_id, staff_name, check_type, check_method, check_score, check_result,
    check_remark, check_time, need_rectify, create_time
)
SELECT
    MD5(CONCAT(order_id, 'qc')) AS quality_check_id,
    CONCAT('QC', REPLACE(service_date, '-', ''), SUBSTRING(order_id, 1, 4)) AS check_no,
    order_id,
    order_no,
    provider_id,
    provider_name,
    staff_id,
    staff_name,
    'RANDOM' AS check_type,
    'PHOTO_REVIEW' AS check_method,
    service_score - FLOOR(RAND() * 10) AS check_score,
    'QUALIFIED' AS check_result,
    '服务检查正常' AS check_remark,
    service_date AS check_time,
    0 AS need_rectify,
    NOW() AS create_time
FROM service_log
LIMIT 50;

-- ============================================
-- 清理
-- ============================================
DROP TEMPORARY TABLE IF EXISTS staff_info;

-- ============================================
-- 输出统计
-- ============================================
SELECT '数据重建完成!' AS Result;
