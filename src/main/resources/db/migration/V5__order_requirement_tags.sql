ALTER TABLE orders
    ADD COLUMN requirement_tags_json TEXT NULL COMMENT '订单需求标签快照（物品引导/环境交代/视频打卡）';
