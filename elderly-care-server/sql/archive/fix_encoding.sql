-- 修复乱码数据
UPDATE t_elder SET elder_name = '王秀兰' WHERE elder_id = '2044964641418907648';

-- 验证更新
SELECT elder_id, elder_name, provider_id FROM t_elder WHERE deleted = 0;
