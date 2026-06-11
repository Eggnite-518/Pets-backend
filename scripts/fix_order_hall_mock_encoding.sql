SET NAMES utf8mb4;

UPDATE order_address_snapshots SET
    contact_name = '阿橙',
    province = '北京市',
    city = '北京市',
    district = '海淀区',
    detail_address = '清缘里中区 3 号楼'
WHERE snapshot_id = 30001;

UPDATE order_address_snapshots SET
    contact_name = '阿紫',
    province = '北京市',
    city = '北京市',
    district = '海淀区',
    detail_address = '安宁庄东路 18 号'
WHERE snapshot_id = 30002;

UPDATE order_address_snapshots SET
    contact_name = '阿蓝',
    province = '北京市',
    city = '北京市',
    district = '昌平区',
    detail_address = '回龙观西大街 66 号'
WHERE snapshot_id = 30003;

UPDATE order_address_snapshots SET
    contact_name = '小林',
    province = '北京市',
    city = '北京市',
    district = '海淀区',
    detail_address = '西二旗大街 1 号院'
WHERE snapshot_id = 30004;

UPDATE order_address_snapshots SET
    contact_name = '阿橙',
    province = '北京市',
    city = '北京市',
    district = '海淀区',
    detail_address = '龙域中心 2 号楼'
WHERE snapshot_id = 30005;

UPDATE order_address_snapshots SET
    contact_name = '阿紫',
    province = '北京市',
    city = '北京市',
    district = '昌平区',
    detail_address = '霍营街道 9 号'
WHERE snapshot_id = 30006;

UPDATE order_pets_snapshot SET
    snap_pet_name = '奶茶',
    snap_req = '喂猫粮 50g，换清水，清理猫砂盆。'
WHERE snapshot_id = 90001;

UPDATE order_pets_snapshot SET
    snap_pet_name = '芋圆',
    snap_req = '轻松遛步 25 分钟，避开大型犬。'
WHERE snapshot_id = 90002;

UPDATE order_pets_snapshot SET
    snap_pet_name = '拿铁',
    snap_req = '湿粮半罐 + 干粮 30g，分两次喂食。'
WHERE snapshot_id = 90003;

UPDATE order_pets_snapshot SET
    snap_pet_name = '团团',
    snap_req = '更换清水，喂湿粮半罐，观察精神状态。'
WHERE snapshot_id = 90004;

UPDATE order_pets_snapshot SET
    snap_pet_name = '可乐',
    snap_req = '遛狗 20 分钟，回家后擦脚补水。'
WHERE snapshot_id = 90005;

UPDATE order_pets_snapshot SET
    snap_pet_name = '布丁',
    snap_req = '早间遛狗 30 分钟，避开陌生狗。'
WHERE snapshot_id = 90006;

UPDATE order_pets_snapshot SET
    snap_pet_name = '椰椰',
    snap_req = '处方粮 40g，禁止零食，记录排便。'
WHERE snapshot_id = 90007;
