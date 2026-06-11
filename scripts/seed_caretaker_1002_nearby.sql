-- 宠托师 13800000002（user_id=1002 阿周）近距离测试数据
-- 常驻地约：31.1886, 121.4365（上海徐汇，与宠主默认地址一致）
-- 订单地址均在 500m 内，便于定位签到 / 履约打卡
--
-- 执行：./scripts/run_caretaker_1002_nearby_seed.sh

SET NAMES utf8mb4;
SET @now = NOW();
SET @today = CURDATE();

-- 扩大服务半径，避免接单大厅因 0km 过滤掉订单
UPDATE sitter_profiles
SET service_radius_km = 10,
    resident_latitude = 31.1886000,
    resident_longitude = 121.4365000,
    updated_at = @now
WHERE provider_id = 1002;

-- 清理旧演示单（含上海坐标的 9201/9202）
DELETE FROM review_appeals WHERE order_id IN (9201, 9202, 9301, 9302, 9310, 9311);
DELETE FROM reviews WHERE order_id IN (9201, 9202, 9301, 9302, 9310, 9311);
DELETE FROM arbitration_records WHERE order_id IN (9201, 9202, 9301, 9302, 9310, 9311);
DELETE FROM exception_reports WHERE order_id IN (9201, 9202, 9301, 9302, 9310, 9311);
DELETE FROM credit_records WHERE relation_id IN (9201, 9202, 9301, 9302, 9310, 9311);
DELETE FROM chats WHERE order_id IN (9201, 9202, 9301, 9302, 9310, 9311);
DELETE FROM fulfillment_records WHERE order_id IN (9201, 9202, 9301, 9302);
DELETE FROM order_applications WHERE order_id IN (9201, 9202, 9301, 9302, 9310, 9311);
DELETE FROM order_pets_snapshot WHERE order_id IN (9201, 9202, 9301, 9302, 9310, 9311);
DELETE FROM orders WHERE order_id IN (9201, 9202, 9301, 9302, 9310, 9311);
DELETE FROM order_address_snapshots WHERE snapshot_id IN (32001, 32002, 33001, 33002, 33010, 33011);

INSERT INTO order_address_snapshots (
    snapshot_id, source_address_id, contact_name, contact_phone, province, city, district,
    detail_address, address_tag, latitude, longitude, created_at, updated_at, deleted
) VALUES
    (33001, 21001, '小林', '13900000001', '上海市', '上海市', '徐汇区', '示例路100号', '家',
     31.1888000, 121.4368000, @now, @now, 0),
    (33002, 21001, '小林', '13900000001', '上海市', '上海市', '徐汇区', '示例路100号', '家',
     31.1886000, 121.4365000, @now, @now, 0),
    (33010, 21001, '小林', '13900000001', '上海市', '上海市', '徐汇区', '示例路102号', '家',
     31.1890000, 121.4370000, @now, @now, 0),
    (33011, 21001, '小林', '13900000001', '上海市', '上海市', '徐汇区', '示例路108号', '家',
     31.1884000, 121.4362000, @now, @now, 0);

-- 已指派 + 已支付，可直接打卡
INSERT INTO orders (
    order_id, owner_id, provider_id, address_snapshot_id, order_status, total_amount,
    service_date, service_start_time, service_end_time, service_type, created_at, updated_at, deleted
) VALUES
    (9301, 1001, 1002, 33001, 3, 118.00, @today, '09:00:00', '10:00:00', 1, @now, @now, 0),
    (9302, 1001, 1002, 33002, 4, 136.00, @today, '14:00:00', '15:00:00', 1, @now, @now, 0),
    (9310, 1001, NULL, 33010, 1, 98.00, DATE_ADD(@today, INTERVAL 1 DAY), '10:00:00', '11:00:00', 1, @now, @now, 0),
    (9311, 1001, NULL, 33011, 1, 108.00, DATE_ADD(@today, INTERVAL 1 DAY), '15:00:00', '16:00:00', 2, @now, @now, 0);

INSERT INTO order_pets_snapshot (
    snapshot_id, order_id, archive_pet_id, snap_pet_name, snap_pet_type, snap_req,
    created_at, updated_at, deleted
) VALUES
    (93001, 9301, 3001, '团团', 1, '喂湿粮半罐，换清水，清理猫砂盆。', @now, @now, 0),
    (93002, 9302, 3001, '团团', 1, '喂湿粮半罐，换清水，清理猫砂盆。', @now, @now, 0),
    (93010, 9310, 3001, '团团', 1, '日常喂养，观察精神状态。', @now, @now, 0),
    (93011, 9311, 3002, '可乐', 2, '遛狗 25 分钟，擦脚补水。', @now, @now, 0);

INSERT INTO order_applications (
    apply_id, order_id, provider_id, apply_status, created_at, updated_at, deleted
) VALUES
    (93001, 9301, 1002, 2, @now, @now, 0),
    (93002, 9302, 1002, 2, @now, @now, 0);

SELECT 'seed_caretaker_1002_nearby done' AS message,
       @today AS service_date,
       9301 AS order_pending_fulfillment,
       9302 AS order_in_fulfillment,
       9310 AS hall_order_feed,
       9311 AS hall_order_walk;
