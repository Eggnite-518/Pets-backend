-- 将历史「其他」(3) 与曾用编码 4 的异宠统一为 3=异宠
UPDATE pet_archives SET pet_type = 3 WHERE pet_type = 4;
UPDATE order_pets_snapshot SET snap_pet_type = 3 WHERE snap_pet_type = 4;

ALTER TABLE pet_archives
    MODIFY COLUMN pet_type TINYINT NOT NULL COMMENT '宠物种类：1=猫，2=狗，3=异宠';

ALTER TABLE order_pets_snapshot
    MODIFY COLUMN snap_pet_type TINYINT NOT NULL COMMENT '宠物种类快照：1=猫，2=狗，3=异宠';
