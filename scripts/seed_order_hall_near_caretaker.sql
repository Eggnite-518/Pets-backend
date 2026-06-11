-- 接单大厅测试数据：订单地址围绕宠托师常驻地 (40.0508079, 116.2940278) 2km 内
-- 使用前请确认 service_radius_km >= 3，否则请扩大服务半径或调整坐标
-- 执行方式：mysql --default-character-set=utf8mb4 -u root -p pets_db < scripts/seed_order_hall_near_caretaker.sql

SET NAMES utf8mb4;
SET @now = NOW();

INSERT INTO order_address_snapshots (
    snapshot_id, source_address_id, contact_name, contact_phone, province, city, district,
    detail_address, address_tag, latitude, longitude, created_at, updated_at, deleted
) VALUES
    (30001, NULL, '阿橙', '13800000004', '北京市', '北京市', '海淀区', '清缘里中区 3 号楼', '家', 40.0625000, 116.2980000, @now, @now, 0),
    (30002, NULL, '阿紫', '13800000005', '北京市', '北京市', '海淀区', '安宁庄东路 18 号', '家', 40.0410000, 116.3120000, @now, @now, 0),
    (30003, NULL, '阿蓝', '13800000006', '北京市', '北京市', '昌平区', '回龙观西大街 66 号', '家', 40.0350000, 116.2780000, @now, @now, 0),
    (30004, NULL, '小林', '13800000001', '北京市', '北京市', '海淀区', '西二旗大街 1 号院', '家', 40.0580000, 116.3050000, @now, @now, 0),
    (30005, NULL, '阿橙', '13800000004', '北京市', '北京市', '海淀区', '龙域中心 2 号楼', '家', 40.0480000, 116.2860000, @now, @now, 0),
    (30006, NULL, '阿紫', '13800000005', '北京市', '北京市', '昌平区', '霍营街道 9 号', '家', 40.0638000, 116.2815000, @now, @now, 0);

INSERT INTO orders (
    order_id, owner_id, provider_id, address_snapshot_id, order_status, total_amount,
    service_date, service_start_time, service_end_time, service_type, hard_filter_tags,
    created_at, updated_at, deleted
) VALUES
    (9001, 1004, NULL, 30001, 1,  88.00, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '09:45:00', 1, NULL, @now, @now, 0),
    (9002, 1005, NULL, 30002, 1,  68.00, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:30:00', '08:15:00', 2, NULL, @now, @now, 0),
    (9003, 1006, NULL, 30003, 1,  98.00, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '12:00:00', '12:45:00', 1, NULL, @now, @now, 0),
    (9004, 1001, NULL, 30004, 1, 128.00, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '18:00:00', '19:00:00', 1, NULL, @now, @now, 0),
    (9005, 1004, NULL, 30005, 1,  75.00, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '08:00:00', '08:40:00', 2, NULL, @now, @now, 0),
    (9006, 1005, NULL, 30006, 1, 108.00, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:30:00', '11:30:00', 1, NULL, @now, @now, 0);

INSERT INTO order_pets_snapshot (
    snapshot_id, order_id, archive_pet_id, snap_pet_name, snap_pet_type, snap_req,
    created_at, updated_at, deleted
) VALUES
    (90001, 9001, 3004, '奶茶', 1, '喂猫粮 50g，换清水，清理猫砂盆。', @now, @now, 0),
    (90002, 9002, 3007, '芋圆', 2, '轻松遛步 25 分钟，避开大型犬。', @now, @now, 0),
    (90003, 9003, 3008, '拿铁', 1, '湿粮半罐 + 干粮 30g，分两次喂食。', @now, @now, 0),
    (90004, 9004, 3001, '团团', 1, '更换清水，喂湿粮半罐，观察精神状态。', @now, @now, 0),
    (90005, 9004, 3002, '可乐', 2, '遛狗 20 分钟，回家后擦脚补水。', @now, @now, 0),
    (90006, 9005, 3005, '布丁', 2, '早间遛狗 30 分钟，避开陌生狗。', @now, @now, 0),
    (90007, 9006, 3006, '椰椰', 1, '处方粮 40g，禁止零食，记录排便。', @now, @now, 0);
