-- 修复所有地理位置数据为陕西省延安市及下属区县
USE elderly_care;

-- ============================================
-- 1. 修复服务商地址
-- ============================================
UPDATE t_provider SET business_address = '延安市宝塔区南关街88号' WHERE provider_id = 'P001';
UPDATE t_provider SET business_address = '延安市宝塔区枣园路120号' WHERE provider_id = 'P002';
UPDATE t_provider SET business_address = '延安市安塞区文化路66号' WHERE provider_id = 'P003';
UPDATE t_provider SET business_address = '延安市延川县文汇路88号' WHERE provider_id = 'P004';
UPDATE t_provider SET business_address = '延安市子长市兴盛路55号' WHERE provider_id = 'P005';
UPDATE t_provider SET business_address = '延安市黄陵县桥山路99号' WHERE provider_id = 'P006';

-- ============================================
-- 2. 修复服务人员地址
-- ============================================
UPDATE t_staff SET address = '延安市宝塔区南市街道' WHERE staff_id = 'S001';
UPDATE t_staff SET address = '延安市宝塔区凤凰山街道' WHERE staff_id = 'S002';
UPDATE t_staff SET address = '延安市宝塔区桥沟街道' WHERE staff_id = 'S003';
UPDATE t_staff SET address = '延安市宝塔区北关街道' WHERE staff_id = 'S004';
UPDATE t_staff SET address = '延安市安塞区真武洞街道' WHERE staff_id = 'S005';
UPDATE t_staff SET address = '延安市延川县社管中心' WHERE staff_id = 'S006';
UPDATE t_staff SET address = '延安市子长市瓦窑堡街道' WHERE staff_id = 'S007';
UPDATE t_staff SET address = '延安市黄陵县桥山镇' WHERE staff_id = 'S008';

-- ============================================
-- 3. 修复老人地址
-- ============================================
UPDATE t_elder SET address = '延安市宝塔区宝塔山街道102号' WHERE elder_id = 'E001';
UPDATE t_elder SET address = '延安市宝塔区南市街道88号' WHERE elder_id = 'E002';
UPDATE t_elder SET address = '延安市安塞区真武洞街道200号' WHERE elder_id = 'E003';
UPDATE t_elder SET address = '延安市延川县社管中心50号' WHERE elder_id = 'E004';
UPDATE t_elder SET address = '延安市子长市瓦窑堡街道88号' WHERE elder_id = 'E005';
UPDATE t_elder SET address = '延安市志丹县保安街道120号' WHERE elder_id = 'E006';
UPDATE t_elder SET address = '延安市黄陵县桥山镇66号' WHERE elder_id = 'E007';
UPDATE t_elder SET address = '延安市甘泉县美水街道38号' WHERE elder_id = 'E008';
UPDATE t_elder SET address = '延安市富县茶坊街道88号' WHERE elder_id = 'E009';
UPDATE t_elder SET address = '延安市洛川县凤栖街道100号' WHERE elder_id = 'E010';

-- ============================================
-- 4. 修复老人补贴名称
-- ============================================
UPDATE t_elder_subsidy SET subsidy_name = '延安市居家养老补贴' WHERE subsidy_id = 'ES001';
UPDATE t_elder_subsidy SET subsidy_name = '重度失能政府补贴' WHERE subsidy_id = 'ES002';
UPDATE t_elder_subsidy SET subsidy_name = '延安市居家养老补贴' WHERE subsidy_id = 'ES003';
UPDATE t_elder_subsidy SET subsidy_name = '自费用户' WHERE subsidy_id = 'ES004';
UPDATE t_elder_subsidy SET subsidy_name = '延安市居家养老补贴' WHERE subsidy_id = 'ES005';
UPDATE t_elder_subsidy SET subsidy_name = '重度失能政府补贴' WHERE subsidy_id = 'ES006';
UPDATE t_elder_subsidy SET subsidy_name = '延安市居家养老补贴' WHERE subsidy_id = 'ES007';
UPDATE t_elder_subsidy SET subsidy_name = '延安市居家养老补贴' WHERE subsidy_id = 'ES008';

-- ============================================
-- 5. 修复订单服务地址
-- ============================================
UPDATE t_order SET service_address = '延安市宝塔区宝塔山街道102号', elder_address = '延安市宝塔区宝塔山街道102号' WHERE order_id LIKE 'O001%';
UPDATE t_order SET service_address = '延安市宝塔区南市街道88号', elder_address = '延安市宝塔区南市街道88号' WHERE order_id LIKE 'O002%';
UPDATE t_order SET service_address = '延安市安塞区真武洞街道200号', elder_address = '延安市安塞区真武洞街道200号' WHERE order_id LIKE 'O003%';
UPDATE t_order SET service_address = '延安市延川县社管中心50号', elder_address = '延安市延川县社管中心50号' WHERE order_id LIKE 'O004%';
UPDATE t_order SET service_address = '延安市子长市瓦窑堡街道88号', elder_address = '延安市子长市瓦窑堡街道88号' WHERE order_id LIKE 'O005%';
UPDATE t_order SET service_address = '延安市志丹县保安街道120号', elder_address = '延安市志丹县保安街道120号' WHERE order_id LIKE 'O006%';
UPDATE t_order SET service_address = '延安市黄陵县桥山镇66号', elder_address = '延安市黄陵县桥山镇66号' WHERE order_id LIKE 'O007%';
UPDATE t_order SET service_address = '延安市甘泉县美水街道38号', elder_address = '延安市甘泉县美水街道38号' WHERE order_id LIKE 'O008%';
UPDATE t_order SET service_address = '延安市富县茶坊街道88号', elder_address = '延安市富县茶坊街道88号' WHERE order_id LIKE 'O009%';
UPDATE t_order SET service_address = '延安市洛川县凤栖街道100号', elder_address = '延安市洛川县凤栖街道100号' WHERE order_id LIKE 'O010%';

SELECT '延安市地理位置数据修复完成!' AS result;
