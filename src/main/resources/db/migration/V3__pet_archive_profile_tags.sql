ALTER TABLE pet_archives
    ADD COLUMN profile_tags_json JSON DEFAULT NULL
        COMMENT '宠物档案标签：体重、年龄段、生理状态、行为、健康等';

ALTER TABLE order_pets_snapshot
    ADD COLUMN snap_profile_tags_json JSON DEFAULT NULL
        COMMENT '下单时宠物档案标签快照';
