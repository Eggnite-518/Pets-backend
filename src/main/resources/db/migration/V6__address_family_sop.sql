-- 家庭 SOP 字段（幂等：列已存在则跳过，避免重复执行报错）

SET @db := DATABASE();

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_addresses'
       AND COLUMN_NAME = 'sop_requirement_tags_json') = 0,
    'ALTER TABLE user_addresses ADD COLUMN sop_requirement_tags_json TEXT NULL COMMENT ''家庭SOP：需求标签快照（物品引导/环境交代/视频打卡）'' AFTER longitude',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_addresses'
       AND COLUMN_NAME = 'sop_hard_filter_tags') = 0,
    'ALTER TABLE user_addresses ADD COLUMN sop_hard_filter_tags JSON NULL COMMENT ''家庭SOP：业务属性门槛'' AFTER sop_requirement_tags_json',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_addresses'
       AND COLUMN_NAME = 'sop_remark') = 0,
    'ALTER TABLE user_addresses ADD COLUMN sop_remark TEXT NULL COMMENT ''家庭SOP：收尾/服务说明'' AFTER sop_hard_filter_tags',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_addresses'
       AND COLUMN_NAME = 'sop_updated_at') = 0,
    'ALTER TABLE user_addresses ADD COLUMN sop_updated_at DATETIME NULL COMMENT ''家庭SOP最近更新时间'' AFTER sop_remark',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
