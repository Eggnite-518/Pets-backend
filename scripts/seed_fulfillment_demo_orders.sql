-- 履约打卡演示数据：模拟「选宠托师 → 支付 → 待履约/履约中」完整链路
-- 适用账号：宠托师 13800000002（阿周 user_id=1002），宠主 13800000001（小林 user_id=1001）
-- 服务日期固定为「今天」，否则打卡会因 FULFILLMENT_TIME_ERROR 被拒绝
--
-- 执行：
--   mysql --default-character-set=utf8mb4 -u root -p pets_db < scripts/seed_fulfillment_demo_orders.sql
--
-- 生成订单：
--   9201  status=3 待履约  从节点 1「抵达签到」开始完整打卡
--   9202  status=4 履约中  已完成节点 1，可从节点 2「入户确认」继续（含图片/视频节点）

SET NAMES utf8mb4;
SET @now = NOW();
SET @today = CURDATE();

-- 清理旧演示数据（可重复执行）
DELETE FROM fulfillment_records WHERE order_id IN (9201, 9202);
DELETE FROM order_applications WHERE order_id IN (9201, 9202);
DELETE FROM order_pets_snapshot WHERE order_id IN (9201, 9202);
DELETE FROM orders WHERE order_id IN (9201, 9202);
DELETE FROM order_address_snapshots WHERE snapshot_id IN (32001, 32002);

INSERT INTO order_address_snapshots (
    snapshot_id, source_address_id, contact_name, contact_phone, province, city, district,
    detail_address, address_tag, latitude, longitude, created_at, updated_at, deleted
) VALUES
    (32001, 21001, '小林', '13900000001', '上海市', '上海市', '徐汇区', '示例路100号', '家',
     31.1886000, 121.4365000, @now, @now, 0),
    (32002, 21001, '小林', '13900000001', '上海市', '上海市', '徐汇区', '示例路100号', '家',
     31.1886000, 121.4365000, @now, @now, 0);

INSERT INTO orders (
    order_id, owner_id, provider_id, address_snapshot_id, order_status, total_amount,
    service_date, service_start_time, service_end_time, service_type, created_at, updated_at, deleted
) VALUES
    (9201, 1001, 1002, 32001, 3, 120.00, @today, '09:00:00', '10:00:00', 1, @now, @now, 0),
    (9202, 1001, 1002, 32002, 4, 128.00, @today, '14:00:00', '15:00:00', 1, @now, @now, 0);

INSERT INTO order_pets_snapshot (
    snapshot_id, order_id, archive_pet_id, snap_pet_name, snap_pet_type, snap_req,
    created_at, updated_at, deleted
) VALUES
    (92001, 9201, 3001, '团团', 1, '喂湿粮半罐，更换清水，清理猫砂盆。', @now, @now, 0),
    (92002, 9202, 3001, '团团', 1, '喂湿粮半罐，更换清水，清理猫砂盆。', @now, @now, 0);

INSERT INTO order_applications (
    apply_id, order_id, provider_id, apply_status, created_at, updated_at, deleted
) VALUES
    (92001, 9201, 1002, 2, @now, @now, 0),
    (92002, 9202, 1002, 2, @now, @now, 0);

-- 若库中已有 order_settlements 表，可手动插入托管记录模拟支付（非打卡必需）：
-- INSERT INTO order_settlements (...) VALUES (...);

-- 9202 已完成「抵达签到」，便于直接测图片/视频节点
INSERT INTO fulfillment_records (
    record_id, order_id, node_type, latitude, longitude, image_url, media_type, object_key, file_size,
    content_type, frame_rate, processing_status, processing_error_code, processing_error, original_object_key,
    original_content_type, original_file_size, watermark_text, created_at, updated_at, deleted
) VALUES
    (92001, 9202, 1, 31.1886500, 121.4365500, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE',
     'fulfillment/demo/9202/nodes/1/demo.jpg', 102400, 'image/jpeg', NULL, 'SUCCESS', NULL, NULL,
     NULL, NULL, NULL, NULL, @now, @now, 0);

SELECT 'seed_fulfillment_demo_orders done' AS message,
       @today AS service_date,
       9201 AS order_pending_fulfillment,
       9202 AS order_in_fulfillment;
