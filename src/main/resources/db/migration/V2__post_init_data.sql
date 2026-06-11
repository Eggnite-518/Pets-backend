-- 合并迁移 V2 ~ V18（Flyway squash）
-- 依赖：V1__init_database.sql
-- 已省略：V10（reviews 列已在 V1）、V15（service_* 列已在 V1）、V16（时间已写入 V14 订单 INSERT）


-- =============================================================================
-- 原 V2__*
-- =============================================================================

INSERT INTO users (
    user_id, phone, nickname, password_hash, avatar_url, role_type, balance, frozen_amount,
    created_at, updated_at, deleted, real_name, id_card_no
) VALUES
    (999999, '19900000000', '平台消息', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', NULL, 3, 0.00, 0.00,
     '2026-04-20 08:00:00', '2026-04-20 08:00:00', 0, NULL, NULL),
    (1001, '13800000001', '小林', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 1, 500.00, 0.00,
     '2026-04-20 09:00:00', '2026-04-20 09:00:00', 0, '林一', '110101199001010011'),
    (1002, '13800000002', '阿周', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 2, 320.00, 0.00,
     '2026-04-20 09:30:00', '2026-04-20 09:30:00', 0, '周二', '110101199002020022'),
    (1003, '13800000003', '小陈', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 3, 800.00, 100.00,
     '2026-04-20 10:00:00', '2026-04-20 10:00:00', 0, '陈三', '110101199003030033');

INSERT INTO sitter_profiles (
    provider_id, gender, verify_status, deposit_amount, credit_score, service_radius_km,
    resident_address, resident_latitude, resident_longitude, created_at, updated_at, deleted
) VALUES
    (1002, 1, 2, 200.00, 80, 5, '上海市徐汇区示例路88号', 31.1886500, 121.4365500,
     '2026-04-20 09:30:00', '2026-04-20 09:30:00', 0),
    (1003, 2, 2, 100.00, 80, 8, '上海市浦东新区示例路168号', 31.2213500, 121.5440500,
     '2026-04-20 10:00:00', '2026-04-20 10:00:00', 0);

INSERT INTO pet_owners (
    owner_id, emergency_contact, created_at, updated_at, deleted
) VALUES
    (1001, '13900000001', '2026-04-20 09:00:00', '2026-04-20 09:00:00', 0),
    (1003, '13900000003', '2026-04-20 10:00:00', '2026-04-20 10:00:00', 0);

INSERT INTO user_addresses (
    address_id, user_id, contact_name, contact_phone, province, city, district, detail_address,
    address_tag, is_default, latitude, longitude, created_at, updated_at, deleted
) VALUES
    (21001, 1001, '小林', '13900000001', '上海市', '上海市', '徐汇区', '示例路100号', '家',
     1, 31.1886000, 121.4365000, '2026-04-20 09:00:00', '2026-04-20 09:00:00', 0),
    (21002, 1003, '小陈', '13900000003', '上海市', '上海市', '浦东新区', '示例路200号', '家',
     1, 31.2213000, 121.5440000, '2026-04-20 10:00:00', '2026-04-20 10:00:00', 0);

INSERT INTO question_bank (
    question_id, question_type, content, options_json, correct_answer, created_at, updated_at, deleted
) VALUES
    (5001, 1, '上门喂养前首先应该做什么？',
     '{"options":["确认订单信息和宠物要求","直接开始喂食","先向宠物喂零食"],"shuffle":true}', '确认订单',
     '2026-04-20 09:00:00', '2026-04-20 09:00:00', 0),
    (5002, 2, '发现宠物状态异常时应该如何处理？',
     '{"options":["立即联系宠主并按流程上报","继续完成订单后再说","自行用药处理"],"shuffle":true}', '立即上报',
     '2026-04-20 09:00:00', '2026-04-20 09:00:00', 0),
    (5003, 1, '离开宠主住所前需要确认什么？',
     '{"options":["门窗水电和宠物状态","只上传照片","只清理垃圾"],"shuffle":true}', '安全确认',
     '2026-04-20 09:00:00', '2026-04-20 09:00:00', 0);

INSERT INTO order_address_snapshots (
    snapshot_id, source_address_id, contact_name, contact_phone, province, city, district, detail_address,
    address_tag, latitude, longitude, created_at, updated_at, deleted
) VALUES
    (22001, 21001, '小林', '13900000001', '上海市', '上海市', '徐汇区', '示例路100号', '家',
     31.1886000, 121.4365000, '2026-04-25 08:00:00', '2026-04-25 08:00:00', 0),
    (22002, 21001, '小林', '13900000001', '上海市', '上海市', '徐汇区', '示例路100号', '家',
     31.1886000, 121.4365000, '2026-04-25 09:00:00', '2026-04-25 09:00:00', 0),
    (22003, NULL, '小陈', '13900000003', '上海市', '上海市', '浦东新区', '示例路200号', '临时',
     31.2213000, 121.5440000, '2026-04-26 10:00:00', '2026-04-26 10:00:00', 0);

INSERT INTO orders (
    order_id, owner_id, provider_id, address_snapshot_id, order_status, total_amount, service_date,
    service_start_time, service_end_time, service_type, created_at, updated_at, deleted
) VALUES
    (2001, 1001, 1002, 22001, 6, 168.00, '2026-05-01', '08:00:00', '09:00:00', 1, '2026-04-25 08:00:00', '2026-05-01 18:30:00', 0),
    (2002, 1001, NULL, 22002, 1, 98.00, '2026-05-03', '14:00:00', '15:00:00', 1, '2026-04-25 09:00:00', '2026-04-25 09:00:00', 0),
    (2003, 1003, 1002, 22003, 4, 128.00, '2026-05-05', '10:30:00', '11:30:00', 2, '2026-04-26 10:00:00', '2026-05-05 11:30:00', 0);

INSERT INTO order_applications (
    apply_id, order_id, provider_id, apply_status, created_at, updated_at, deleted
) VALUES
    (4001, 2001, 1002, 2, '2026-04-25 09:00:00', '2026-04-25 09:30:00', 0),
    (4002, 2002, 1002, 0, '2026-04-25 10:00:00', '2026-04-25 10:00:00', 0),
    (4003, 2002, 1003, 0, '2026-04-25 10:15:00', '2026-04-25 10:15:00', 0);

INSERT INTO exception_reports (
    report_id, order_id, reporter_id, exception_type, description, proof_images, report_status,
    created_at, updated_at, deleted, resolved_at
) VALUES
    (6001, 2003, 1002, 2, '宠物食欲较差，已联系宠主确认后继续处理。',
     '["https://example.com/proof/6001-1.jpg"]', 0, '2026-05-05 11:20:00', '2026-05-05 11:20:00', 0, NULL),
    (6002, 2001, 1001, 5, '订单完成后发现垃圾袋未带走，已沟通解决。',
     '["https://example.com/proof/6002-1.jpg"]', 2, '2026-05-01 18:00:00', '2026-05-01 19:00:00', 0, '2026-05-01 19:00:00');

INSERT INTO arbitration_records (
    arbitration_id, order_id, plaintiff_id, defendant_id, arb_type, reason, evidence_urls,
    arbitration_status, result_type, admin_memo, created_at, updated_at, deleted, closed_at
) VALUES
    (7001, 2001, 1001, 1002, 1, '宠主认为服务照片不完整，申请平台核查。',
     '["https://example.com/evidence/7001-1.jpg"]', 3, 3, '双方已沟通补充说明，平台判定和解。',
     '2026-05-02 10:00:00', '2026-05-02 16:00:00', 0, '2026-05-02 16:00:00'),
    (7002, 2003, 1003, 1002, 2, '宠物状态异常，宠主申请客服介入。',
     '["https://example.com/evidence/7002-1.jpg"]', 0, NULL, NULL,
     '2026-05-05 12:00:00', '2026-05-05 12:00:00', 0, NULL);

INSERT INTO pet_archives (
    pet_id, owner_id, pet_name, pet_type, default_req, image, created_at, updated_at, deleted
) VALUES
    (3001, 1001, '团团', 1, '每日更换清水，喂湿粮半罐，观察精神状态。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg',
     '2026-04-20 09:05:00', '2026-04-20 09:05:00', 0),
    (3002, 1001, '可乐', 2, '遛狗20分钟，回家后补水并擦脚。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg',
     '2026-04-20 09:06:00', '2026-04-20 09:06:00', 0),
    (3003, 1003, '米粒', 1, '只吃处方粮，禁止喂零食。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg',
     '2026-04-20 10:05:00', '2026-04-20 10:05:00', 0);

INSERT INTO order_pets_snapshot (
    snapshot_id, order_id, archive_pet_id, snap_pet_name, snap_pet_type, snap_req, created_at, updated_at, deleted
) VALUES
    (8001, 2001, 3001, '团团', 1, '喂湿粮半罐，更换清水。', '2026-05-01 08:00:00', '2026-05-01 08:00:00', 0),
    (8002, 2001, 3002, '可乐', 2, '遛狗20分钟，擦脚后回家。', '2026-05-01 08:00:00', '2026-05-01 08:00:00', 0),
    (8003, 2003, 3003, '米粒', 1, '只喂处方粮，观察进食情况。', '2026-05-05 10:30:00', '2026-05-05 10:30:00', 0);

INSERT INTO chats (
    message_id, order_id, sender_id, receiver_id, content, created_at, updated_at, deleted
) VALUES
    (9001, 2001, 1001, 1002, '您好，团团今天需要多观察饮水。', '2026-05-01 08:30:00', '2026-05-01 08:30:00', 0),
    (9002, 2001, 1002, 1001, '收到，我会完成后上传照片。', '2026-05-01 08:35:00', '2026-05-01 08:35:00', 0),
    (9003, 2003, 1002, 1003, '米粒今天食欲一般，我已经拍照记录。', '2026-05-05 11:10:00', '2026-05-05 11:10:00', 0);

INSERT INTO fulfillment_records (
    record_id, order_id, node_type, latitude, longitude, image_url, media_type, object_key, file_size,
    content_type, frame_rate, processing_status, processing_error_code, processing_error, original_object_key,
    original_content_type, original_file_size, watermark_text, created_at, updated_at, deleted
) VALUES
    (10001, 2001, 1, 31.1886500, 121.4365500, 'https://example.com/checkin/10001.jpg', 'IMAGE',
     'fulfillment/orders/2001/nodes/1/10001.jpg', 102400, 'image/jpeg', NULL, 'SUCCESS', NULL, NULL,
     NULL, NULL, NULL, NULL, '2026-05-01 09:00:00', '2026-05-01 09:00:00', 0),
    (10002, 2001, 3, 31.1886600, 121.4365600, 'https://example.com/checkin/10002.jpg', 'IMAGE',
     'fulfillment/orders/2001/nodes/3/10002.jpg', 112640, 'image/jpeg', NULL, 'SUCCESS', NULL, NULL,
     NULL, NULL, NULL, NULL, '2026-05-01 09:20:00', '2026-05-01 09:20:00', 0),
    (10003, 2003, 6, 31.2213500, 121.5440500, NULL, 'VIDEO',
     'fulfillment/orders/2003/nodes/6/watermarked-10003.mp4', 4096000, 'video/mp4', 30, 'SUCCESS', NULL, NULL,
     'fulfillment/orders/2003/nodes/6/original/original-10003.mp4', 'video/mp4', 6144000, '宠托：阿周',
     '2026-05-05 11:00:00', '2026-05-05 11:00:00', 0);

INSERT INTO reviews (
    review_id, order_id, reviewer_id, target_id, score, overall_score, punctuality_score, professional_score,
    is_low_score, review_status, content, review_type, created_at, updated_at, deleted
) VALUES
    (11001, 2001, 1001, 1002, 5, 5, 5, 5, 0, 1, '服务认真，反馈及时。', 1, '2026-05-01 20:00:00', '2026-05-01 20:00:00', 0),
    (11002, 2001, 1002, 1001, 5, 5, 5, 5, 0, 1, '沟通清楚，宠物信息完整。', 2, '2026-05-01 20:10:00', '2026-05-01 20:10:00', 0);

INSERT INTO financial_logs (
    log_id, user_id, amount, balance_after, trade_type, relation_id, created_at, updated_at, deleted
) VALUES
    (12001, 1001, -168.00, 332.00, 21, 2001, '2026-05-01 18:10:00', '2026-05-01 18:10:00', 0),
    (12002, 1002, 117.60, 437.60, 11, 2001, '2026-05-01 18:11:00', '2026-05-01 18:11:00', 0),
    (12003, 1003, -100.00, 700.00, 13, NULL, '2026-04-20 11:00:00', '2026-04-20 11:00:00', 0);

INSERT INTO withdrawal_records (
    withdraw_id, user_id, amount, withdrawal_status, account_type, account_info, created_at, updated_at, deleted
) VALUES
    (13001, 1002, 100.00, 0, 1, 'alipay:zhou@example.com', '2026-05-02 09:00:00', '2026-05-02 09:00:00', 0),
    (13002, 1003, 50.00, 2, 2, 'wechat:chen-demo', '2026-05-03 10:00:00', '2026-05-03 12:00:00', 0);

INSERT INTO credit_records (
    record_id, provider_id, change_score, score_after, reason_type, relation_id, created_at, updated_at, deleted
) VALUES
    (14001, 1002, 4, 84, 1, 2001, '2026-05-01 18:20:00', '2026-05-01 18:20:00', 0),
    (14002, 1003, 0, 80, 1, NULL, '2026-04-20 11:10:00', '2026-04-20 11:10:00', 0);

INSERT INTO order_settlements (
    settlement_id, order_id, owner_id, provider_id, gross_amount, commission_rate, commission_amount,
    provider_income, settlement_status, settled_at, created_at, updated_at, deleted
) VALUES
    (15001, 2001, 1001, 1002, 168.00, 0.3000, 50.40, 117.60, 2,
     '2026-05-01 18:11:00', '2026-05-01 18:10:00', '2026-05-01 18:11:00', 0);

INSERT INTO platform_financial_logs (
    log_id, amount, balance_after, trade_type, relation_id, remark, created_at, updated_at, deleted
) VALUES
    (16001, 50.40, 50.40, 101, 2001, 'Order commission 30%',
     '2026-05-01 18:11:00', '2026-05-01 18:11:00', 0);

-- =============================================================================
-- 原 V3__*
-- =============================================================================

ALTER TABLE sitter_profiles
    ADD COLUMN is_banned TINYINT NOT NULL DEFAULT 0 COMMENT '是否封禁:0=否,1=是' AFTER credit_score;

-- =============================================================================
-- 原 V4__*
-- =============================================================================

CREATE TABLE training_materials (
    material_id BIGINT NOT NULL COMMENT '培训素材ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '培训内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0=未删除,1=已删除',
    PRIMARY KEY (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='training_materials';

CREATE TABLE sitter_training_records (
    provider_id BIGINT NOT NULL COMMENT '喂养员ID',
    learning_completed_at DATETIME DEFAULT NULL COMMENT '学习完成时间',
    last_exam_score INT DEFAULT NULL COMMENT '最近一次考试分数',
    last_exam_passed TINYINT DEFAULT NULL COMMENT '最近一次考试是否通过',
    last_exam_at DATETIME DEFAULT NULL COMMENT '最近一次考试时间',
    reset_reason VARCHAR(50) DEFAULT NULL COMMENT '重置原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0=未删除,1=已删除',
    PRIMARY KEY (provider_id),
    CONSTRAINT fk_training_records_provider
        FOREIGN KEY (provider_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='sitter_training_records';

ALTER TABLE sitter_profiles
    MODIFY COLUMN verify_status TINYINT NOT NULL DEFAULT 0 COMMENT 'verify_status:0=init,1=learning_done,2=passed';

INSERT INTO training_materials (material_id, title, content, created_at, updated_at, deleted)
VALUES (900001, 'Pet Sitter Training Manual', 'Covers pet behavior basics, harness use, home entry workflow, and emergency handling guidelines.',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- =============================================================================
-- 原 V5__*
-- =============================================================================

CREATE TABLE sms_verification_codes (
    id BIGINT NOT NULL COMMENT '验证码记录ID',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    code VARCHAR(6) NOT NULL COMMENT '6位验证码',
    used TINYINT NOT NULL DEFAULT 0 COMMENT '是否已使用:0=未使用,1=已使用',
    expires_at DATETIME NOT NULL COMMENT '过期时间(发送后5分钟)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (id),
    INDEX idx_sms_phone_created (phone, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信验证码表';

-- =============================================================================
-- 原 V6__*
-- =============================================================================

CREATE TABLE IF NOT EXISTS review_attachments (
    attachment_id BIGINT NOT NULL COMMENT '评价附件ID',
    review_id BIGINT NOT NULL COMMENT '关联评价ID',
    url VARCHAR(500) NOT NULL COMMENT '附件访问地址',
    object_key VARCHAR(255) DEFAULT NULL COMMENT 'OSS对象Key',
    media_type VARCHAR(20) NOT NULL COMMENT '媒体类型：IMAGE/VIDEO/FILE',
    content_type VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型',
    file_size BIGINT DEFAULT NULL COMMENT '文件大小，单位字节',
    sort_order INT NOT NULL DEFAULT 1 COMMENT '展示排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (attachment_id),
    KEY idx_review_attachments_review (review_id),
    CONSTRAINT fk_review_attachments_review
        FOREIGN KEY (review_id) REFERENCES reviews (review_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价附件表';

CREATE TABLE IF NOT EXISTS review_appeals (
    appeal_id BIGINT NOT NULL COMMENT '评价申诉案卷ID',
    review_id BIGINT NOT NULL COMMENT '关联评价ID',
    order_id BIGINT NOT NULL COMMENT '关联订单ID',
    provider_id BIGINT NOT NULL COMMENT '申诉服务者ID',
    owner_id BIGINT NOT NULL COMMENT '评价宠主ID',
    reason TEXT NOT NULL COMMENT '申诉原因',
    evidence_urls VARCHAR(1000) DEFAULT NULL COMMENT '补充证据文件地址，逗号分隔',
    appeal_status TINYINT NOT NULL DEFAULT 1 COMMENT '申诉状态：1=待仲裁，2=取证中，3=已判定，4=申诉成立，5=申诉失败',
    admin_memo TEXT DEFAULT NULL COMMENT '平台处理备注',
    appeal_deadline DATETIME DEFAULT NULL COMMENT '申诉截止时间',
    closed_at DATETIME DEFAULT NULL COMMENT '结案时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (appeal_id),
    KEY idx_review_appeals_review_status (review_id, appeal_status),
    KEY idx_review_appeals_order (order_id),
    CONSTRAINT fk_review_appeals_review
        FOREIGN KEY (review_id) REFERENCES reviews (review_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_review_appeals_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_review_appeals_provider
        FOREIGN KEY (provider_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_review_appeals_owner
        FOREIGN KEY (owner_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价申诉表';

-- =============================================================================
-- 原 V7__*
-- =============================================================================

ALTER TABLE orders
    MODIFY COLUMN order_status TINYINT NOT NULL
    COMMENT '订单状态：1=悬赏中，2=待支付，3=待履约，4=履约中，5=待宠主确认，6=已完成，7=履约受阻-等待雇主确认，8=异常结束';

-- =============================================================================
-- 原 V8__*
-- =============================================================================

ALTER TABLE orders
    MODIFY COLUMN order_status TINYINT NOT NULL
    COMMENT '订单状态：1=悬赏中，2=待支付，3=待履约，4=履约中，5=待宠主确认，6=已完成，7=履约受阻-等待雇主确认，8=异常结束，9=紧急终止/平台介入';

ALTER TABLE exception_reports
    MODIFY COLUMN exception_type TINYINT NOT NULL
    COMMENT '异常类型：1=门禁异常，2=宠物异常，3=物资异常，4=设施异常，5=其他，6=人身威胁';

-- =============================================================================
-- 原 V9__*
-- =============================================================================

ALTER TABLE orders
    ADD COLUMN hard_filter_tags JSON DEFAULT NULL
        COMMENT '硬性门槛标签JSON数组，如 ["FEMALE_ONLY"]';

-- =============================================================================
-- 原 V11__*
-- =============================================================================

-- 更新培训材料：替换为完整中文培训手册（Markdown 格式）
UPDATE training_materials
SET title   = '宠托师上岗培训手册',
    content = '## 第一章　宠托师职责与平台规范\n\n**宠托师**是平台认证的专业宠物看护服务提供者，承担为宠主提供上门喂养、遛狗、陪伴等服务的职责。成为合格的宠托师，需要严格遵守平台规范，以专业、负责的态度对待每一位宠物和宠主。\n\n### 1.1 基本职责\n\n- 按约定时间准时到达，不迟到、不爽约\n- 严格按订单要求完成服务内容，不擅自删改项目\n- 如实上传服务打卡照片/视频，确保凭证真实有效\n- 服务结束后保持服务区域整洁，将垃圾置于指定位置或带走\n- 全程保护宠主个人隐私，不拍摄、不传播与服务无关的宠主家庭信息\n\n### 1.2 严禁行为\n\n以下行为将导致账号封禁并承担相应责任：\n\n- **私下收取**宠主额外费用，绕过平台结算\n- 未经授权将宠物带离宠主住所\n- 上传虚假打卡凭证\n- 泄露宠主住址、密码、家庭隐私等信息\n- 服务期间使用宠主私人物品或占用宠主设施\n\n---\n\n## 第二章　上门服务标准流程\n\n### 2.1 接单前准备\n\n1. 仔细阅读订单详情，确认**服务时间、地址、宠物数量及特殊要求**\n2. 提前与宠主沟通，了解宠物习性、饮食禁忌和紧急联系人\n3. 确认自己能够按时到达，如有特殊情况提前告知\n\n### 2.2 上门签到\n\n1. 到达宠主住所后，在 App 中点击**【签到打卡】**，系统将记录到达时间和位置\n2. 拍摄到达凭证照片（含门口环境），上传至订单\n3. 按宠主提供的方式进入，注意保管门禁钥匙/密码，不得转交第三方\n\n### 2.3 服务执行\n\n**喂食服务：**\n- 按宠主要求的时间、种类和份量喂食\n- 检查宠物食欲，观察进食情况并拍照记录\n- 更换新鲜清水，清洗水碗\n\n**猫砂盆清洁：**\n- 每次服务时清理猫砂盆中的排泄物\n- 检查猫砂余量，不足时告知宠主\n\n**遛狗服务：**\n- 全程佩戴牵引绳，不得解开\n- 避免接近不明陌生犬只\n- 遛狗结束后，回家第一步：清洁爪子，补充饮水\n\n### 2.4 安全离开\n\n离开前必须逐项确认：\n\n- ✅ 宠物状态正常（进食、精神、排泄）\n- ✅ 门窗已关好，门已上锁\n- ✅ 水电气安全（水龙头关闭、燃气阀关闭、电器断电）\n- ✅ 垃圾已处理\n- ✅ 已在 App 完成**签退打卡**并上传离开凭证\n\n---\n\n## 第三章　宠物护理基础知识\n\n### 3.1 猫咪护理要点\n\n- 猫咪**换粮需逐渐过渡**，新旧食物混合喂食至少 7 天，避免消化不适\n- 每天提供充足新鲜清水，定时清洗水碗\n- 每次服务清理猫砂盆，保持清洁\n- 不强迫猫咪互动，尊重猫咪的独立习性\n\n### 3.2 犬类护理要点\n\n- 遛狗前检查牵引绳和项圈是否牢固\n- 遛狗后清洁爪子，尤其雨天或泥地后\n- 回家后立即补水，避免立即进食（防胃扭转）\n- 注意犬只体征：正常犬鼻子湿润、眼神清亮、尾巴自然摆动\n\n### 3.3 严格禁止喂食的食物\n\n| 动物 | 禁止食物 |\n|------|----------|\n| 猫和狗 | 葡萄/葡萄干、洋葱、大蒜、巧克力、咖啡因、酒精、生面团 |\n| 猫 | 生鱼（长期）、牛奶（部分乳糖不耐受）|\n| 狗 | 夏威夷果、木糖醇（代糖）|\n\n> ⚠️ **严禁在未经宠主授权的情况下给宠物喂食零食或人类食物。**\n\n### 3.4 基础健康观察\n\n服务过程中请关注以下健康指标：\n\n- **食欲**：进食量是否明显减少\n- **精神**：是否萎靡、嗜睡或异常兴奋\n- **排泄**：是否有腹泻、血便、长时间未排泄\n- **外观**：是否有异常分泌物、伤口、肿胀\n\n如发现任何异常，须**立即拍照记录并联系宠主**。\n\n---\n\n## 第四章　紧急情况处理规范\n\n### 4.1 宠物健康紧急情况\n\n**出现以下症状时，视为紧急情况：**\n\n- 抽搐、昏迷、意识不清\n- 呼吸急促或困难\n- 大量呕吐或腹泻（带血）\n- 误食有毒物质\n\n**处理步骤：**\n1. 保持冷静，**不擅自用药**\n2. 立即拨打宠主电话，告知症状\n3. 宠主无法接听时，联系订单中的**紧急联系人**\n4. 必要时拨打当地宠物急救电话，并在平台留言\n5. 全程记录时间线，等待宠主指示\n\n### 4.2 家庭安全紧急情况\n\n- **煤气泄漏**：立即带宠物离开，不开灯/不打电话（室内），到室外后联系宠主和物业\n- **火灾**：优先保障自身和宠物安全，迅速撤离，拨打 119 并联系宠主\n- **发现陌生人入室**：立即离开，拨打 110，在安全处联系宠主\n\n> 🚨 **任何情况下，人身安全优先于服务完成。**\n\n### 4.3 联系规范\n\n- 紧急联系宠主时，务必说明：时间、地点、具体情况\n- 所有紧急沟通记录须在平台留存\n- 不得私自处置宠主财物或宠物医疗决策\n\n---\n\n## 第五章　隐私保护与信息安全\n\n### 5.1 宠主隐私保护\n\n- **家庭信息保密**：宠主住址、门禁密码、家庭布局等信息严格保密，不得向任何第三方透露\n- **不拍摄无关内容**：服务凭证照片仅拍摄宠物和服务相关区域，不拍摄与服务无关的家庭物品或私人区域\n- **不传播**：服务照片、视频仅用于平台凭证，不得分享至任何社交媒体\n\n### 5.2 记录与上传规范\n\n- 所有上传凭证须**真实、清晰**，不得使用旧照片代替\n- 打卡时间须与实际到达/离开时间一致\n- 发现宠物异常时的记录照片须包含：宠物全貌、异常部位特写、当前环境\n\n---\n\n## 考前温馨提示\n\n本次考试共 **20 道题**，满分 100 分，**90 分及以上**方可通过认证。其中核心题（标有⭐）答错任意一题即不通过，请务必仔细作答。\n\n预祝您顺利通过考试，成为一名优秀的宠托师！'
WHERE material_id = 900001;

-- 补充基础题（question_type=1），目标：共 15 道基础题（已有 5001、5003 共 2 道，新增 13 道）
INSERT INTO question_bank (question_id, question_type, content, options_json, correct_answer, created_at, updated_at, deleted)
VALUES
    (5004, 1, '以下哪种食物对猫和狗都有毒，严禁喂食？',
     '{"options":["葡萄和葡萄干","鸡胸肉（熟）","胡萝卜"],"shuffle":true}',
     '葡萄',
     NOW(), NOW(), 0),

    (5005, 1, '为猫咪更换新品牌猫粮时，正确的做法是？',
     '{"options":["新旧猫粮混合逐渐过渡至少 7 天","当天全部换成新猫粮","按猫咪当天口味决定是否换"],"shuffle":true}',
     '逐渐过渡',
     NOW(), NOW(), 0),

    (5006, 1, '遛狗结束回到宠主家后，第一步应该做什么？',
     '{"options":["清洁狗狗爪子并补充饮水","立即给狗狗喂食","解开牵引绳让狗自由活动"],"shuffle":true}',
     '清洁爪子',
     NOW(), NOW(), 0),

    (5007, 1, '服务期间发现宠物食欲明显减退，应该？',
     '{"options":["拍照记录并第一时间联系宠主","不用理会，宠物自己会调节","自行判断是否需要就医"],"shuffle":true}',
     '联系宠主',
     NOW(), NOW(), 0),

    (5008, 1, '上门服务到达后，应当优先完成哪项操作？',
     '{"options":["在 App 中签到打卡并上传到达凭证照片","直接进屋先与宠物互动","等服务完成后再统一打卡"],"shuffle":true}',
     '签到打卡',
     NOW(), NOW(), 0),

    (5009, 1, '关于猫砂盆清洁，下列哪种做法符合服务规范？',
     '{"options":["每次上门服务时清理猫砂盆中的排泄物","每周清理一次即可","只在宠主要求时才清理"],"shuffle":true}',
     '每次清理',
     NOW(), NOW(), 0),

    (5010, 1, '上门服务结束离开前，必须确认哪项安全内容？',
     '{"options":["门已上锁、水电气安全、宠物状态正常","只要宠物吃完饭就可以离开","拍一张离开照片即完成"],"shuffle":true}',
     '门已上锁',
     NOW(), NOW(), 0),

    (5011, 1, '服务期间联系宠主但电话打不通时，应该怎么处理？',
     '{"options":["尝试联系订单中的紧急联系人或在平台留言","自行决定继续或取消服务","直接离开不作处理"],"shuffle":true}',
     '紧急联系人',
     NOW(), NOW(), 0),

    (5012, 1, '服务期间宠主家的私人物品和家庭布局，应如何对待？',
     '{"options":["不接触、不拍摄，严格保护宠主隐私","如有需要可合理使用","可以拍摄后分享到朋友圈记录工作"],"shuffle":true}',
     '不接触',
     NOW(), NOW(), 0),

    (5013, 1, '带犬遛步时，关于牵引绳的正确做法是？',
     '{"options":["全程保持牵引绳连接，避免靠近陌生犬只","可在空旷地带解开牵引绳让狗自由奔跑","遇到其他狗时主动让两只狗近距离接触"],"shuffle":true}',
     '全程保持牵引绳',
     NOW(), NOW(), 0),

    (5014, 1, '关于宠物日常饮水，以下哪种做法符合规范？',
     '{"options":["每次上门服务时检查并更换新鲜清水","每三天换一次水即可","宠物口渴时自然会去喝"],"shuffle":true}',
     '每次更换',
     NOW(), NOW(), 0),

    (5015, 1, '服务结束后，对服务区域的卫生要求是？',
     '{"options":["保持服务区域整洁，垃圾带走或放入指定位置","不需要打扫，只需完成喂食","宠主不在家不会注意卫生，不影响评价"],"shuffle":true}',
     '保持整洁',
     NOW(), NOW(), 0),

    (5016, 1, '以下哪项行为严重违反平台规定？',
     '{"options":["私下向宠主收取额外费用，绕过平台结算","按时到达并完成服务项目","上传真实的服务凭证照片"],"shuffle":true}',
     '私下收取',
     NOW(), NOW(), 0);

-- 补充核心题（question_type=2），目标：共 5 道核心题（已有 5002 共 1 道，新增 4 道）
INSERT INTO question_bank (question_id, question_type, content, options_json, correct_answer, created_at, updated_at, deleted)
VALUES
    (5017, 2, '发现宠物出现抽搐、意识不清等严重健康异常时，应该？',
     '{"options":["立即联系宠主并根据宠主指示处置，不擅自用药","等待宠物自行恢复，继续完成服务","自行给宠物喂食药物处理"],"shuffle":true}',
     '立即联系宠主',
     NOW(), NOW(), 0),

    (5018, 2, '未经宠主明确授权，宠托师能否将宠物带离宠主住所？',
     '{"options":["不能，任何情况下均须获得宠主明确授权","可以，只要宠托师认为对宠物好","可以，服务期间宠托师对宠物有完全自主权"],"shuffle":true}',
     '不能',
     NOW(), NOW(), 0),

    (5019, 2, '服务中发现宠主家存在煤气泄漏等安全隐患，应该？',
     '{"options":["立即带宠物撤离，到室外后联系宠主和相关部门","关上房间门继续完成服务","不影响服务，等服务结束后再告知宠主"],"shuffle":true}',
     '立即撤离',
     NOW(), NOW(), 0),

    (5020, 2, '若宠托师因突发情况无法按时完成服务，应如何处理？',
     '{"options":["第一时间在平台说明情况并联系宠主协商解决方案","不通知任何人，直接不去","服务时间结束后再说明原因"],"shuffle":true}',
     '第一时间说明',
     NOW(), NOW(), 0);

-- =============================================================================
-- 原 V12__*
-- =============================================================================

-- 修正考题的 correct_answer，确保其为对应正确选项文本的连续子串
-- 后端匹配逻辑：provided.contains(correct) || correct.contains(provided)

-- Q5003（旧题）：正确选项"门窗水电和宠物状态"，原答案"安全确认"不在其中
UPDATE question_bank SET correct_answer = '门窗水电' WHERE question_id = 5003;

-- Q5006：正确选项"清洁狗狗爪子并补充饮水"，原答案"清洁爪子"因中间有"狗狗"无法匹配
UPDATE question_bank SET correct_answer = '清洁狗狗爪子' WHERE question_id = 5006;

-- Q5009：正确选项"每次上门服务时清理猫砂盆中的排泄物"，原答案"每次清理"因中间有"上门服务时"无法匹配
UPDATE question_bank SET correct_answer = '猫砂盆' WHERE question_id = 5009;

-- Q5014：正确选项"每次上门服务时检查并更换新鲜清水"，原答案"每次更换"因中间有"上门服务时检查并"无法匹配
UPDATE question_bank SET correct_answer = '新鲜清水' WHERE question_id = 5014;

-- Q5015：正确选项"保持服务区域整洁，垃圾带走或放入指定位置"，原答案"保持整洁"因中间有"服务区域"无法匹配
UPDATE question_bank SET correct_answer = '服务区域整洁' WHERE question_id = 5015;

-- Q5016：正确选项"私下向宠主收取额外费用，绕过平台结算"，原答案"私下收取"因中间有"向宠主"无法匹配
UPDATE question_bank SET correct_answer = '绕过平台' WHERE question_id = 5016;

-- Q5019：正确选项"立即带宠物撤离，到室外后联系宠主和相关部门"，原答案"立即撤离"因中间有"带宠物"无法匹配
UPDATE question_bank SET correct_answer = '带宠物撤离' WHERE question_id = 5019;

-- Q5020：正确选项"第一时间在平台说明情况并联系宠主协商解决方案"，原答案"第一时间说明"因中间有"在平台"无法匹配
UPDATE question_bank SET correct_answer = '平台说明' WHERE question_id = 5020;

-- =============================================================================
-- 原 V13__*
-- =============================================================================

-- Q5002：正确选项"立即联系宠主并按流程上报"，原答案"立即上报"因中间有"联系宠主并按流程"无法匹配
-- 修正为选项中连续出现的子串"按流程上报"
UPDATE question_bank SET correct_answer = '按流程上报' WHERE question_id = 5002;

-- =============================================================================
-- 原 V14__*
-- =============================================================================

-- ─── 接单大厅测试数据 ─────────────────────────────────────────────────────────
-- 新增 3 位宠主 + 8 条状态=1（悬赏中）的订单，覆盖代喂/代遛、猫/狗等多种场景

-- 1. 新增宠主用户（role_type=1 仅宠主）
INSERT INTO users (
    user_id, phone, nickname, password_hash, avatar_url, role_type, balance, frozen_amount,
    created_at, updated_at, deleted, real_name, id_card_no
) VALUES
    (1004, '13800000004', '大白', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', NULL, 1, 300.00, 0.00,
     '2026-06-01 10:00:00', '2026-06-01 10:00:00', 0, '白四', '110101199004040044'),
    (1005, '13800000005', '小橙', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', NULL, 1, 450.00, 0.00,
     '2026-06-01 10:30:00', '2026-06-01 10:30:00', 0, '橙五', '110101199005050055'),
    (1006, '13800000006', '阿蓝', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', NULL, 1, 600.00, 0.00,
     '2026-06-01 11:00:00', '2026-06-01 11:00:00', 0, '蓝六', '110101199006060066');

-- 2. 新增宠主档案
INSERT INTO pet_owners (
    owner_id, emergency_contact, created_at, updated_at, deleted
) VALUES
    (1004, '13900000004', '2026-06-01 10:00:00', '2026-06-01 10:00:00', 0),
    (1005, '13900000005', '2026-06-01 10:30:00', '2026-06-01 10:30:00', 0),
    (1006, '13900000006', '2026-06-01 11:00:00', '2026-06-01 11:00:00', 0);

-- 3. 新增宠物档案
INSERT INTO pet_archives (
    pet_id, owner_id, pet_name, pet_type, default_req, image, created_at, updated_at, deleted
) VALUES
    (3004, 1004, '奶茶', 1, '每天上午喂猫粮 50g，换清水，清理猫砂盆。', NULL,
     '2026-06-01 10:05:00', '2026-06-01 10:05:00', 0),
    (3005, 1004, '布丁', 2, '遛狗 30 分钟，回家后用湿巾擦脚，注意不要接触陌生狗。', NULL,
     '2026-06-01 10:06:00', '2026-06-01 10:06:00', 0),
    (3006, 1005, '椰椰', 1, '只吃皇家处方粮，每次 40g，禁止零食，注意观察排便。', NULL,
     '2026-06-01 10:35:00', '2026-06-01 10:35:00', 0),
    (3007, 1005, '芋圆', 2, '遛狗 20 分钟即可，体力较弱，不要跑步，回家需补水。', NULL,
     '2026-06-01 10:36:00', '2026-06-01 10:36:00', 0),
    (3008, 1006, '拿铁', 1, '早晚各喂一次，每次湿粮 1/3 罐 + 猫粮 30g，需换水。', NULL,
     '2026-06-01 11:05:00', '2026-06-01 11:05:00', 0),
    (3009, 1006, '美式', 1, '与拿铁分笼喂食，两只猫相互抢食，注意隔离。', NULL,
     '2026-06-01 11:06:00', '2026-06-01 11:06:00', 0);

-- 4. 订单地址快照
INSERT INTO order_address_snapshots (
    snapshot_id, source_address_id, contact_name, contact_phone, province, city, district, detail_address,
    address_tag, latitude, longitude, created_at, updated_at, deleted
) VALUES
    (22004, NULL, '大白', '13900000004', '上海市', '上海市', '长宁区', '天山路 268 号',     '家', 31.2206000, 121.4028000, '2026-06-05 09:00:00', '2026-06-05 09:00:00', 0),
    (22005, NULL, '大白', '13900000004', '上海市', '上海市', '长宁区', '天山路 268 号',     '家', 31.2206000, 121.4028000, '2026-06-05 10:00:00', '2026-06-05 10:00:00', 0),
    (22006, NULL, '小橙', '13900000005', '上海市', '上海市', '静安区', '南京西路 1266 号',  '公司', 31.2286000, 121.4463000, '2026-06-05 11:00:00', '2026-06-05 11:00:00', 0),
    (22007, NULL, '小橙', '13900000005', '上海市', '上海市', '静安区', '南京西路 1266 号',  '公司', 31.2286000, 121.4463000, '2026-06-05 12:00:00', '2026-06-05 12:00:00', 0),
    (22008, NULL, '阿蓝', '13900000006', '上海市', '上海市', '虹口区', '四川北路 555 号',   '家', 31.2639000, 121.4803000, '2026-06-05 13:00:00', '2026-06-05 13:00:00', 0),
    (22009, NULL, '阿蓝', '13900000006', '上海市', '上海市', '虹口区', '四川北路 555 号',   '家', 31.2639000, 121.4803000, '2026-06-05 14:00:00', '2026-06-05 14:00:00', 0),
    (22010, NULL, '小林', '13900000001', '上海市', '上海市', '徐汇区', '示例路 100 号',      '家', 31.1886000, 121.4365000, '2026-06-06 08:00:00', '2026-06-06 08:00:00', 0),
    (22011, NULL, '小陈', '13900000003', '上海市', '上海市', '浦东新区', '张江高科路 88 号', '家', 31.2050000, 121.6030000, '2026-06-06 09:00:00', '2026-06-06 09:00:00', 0);

-- 5. 悬赏订单（order_status = 1）
INSERT INTO orders (
    order_id, owner_id, provider_id, address_snapshot_id, order_status, total_amount, service_date,
    service_start_time, service_end_time, service_type, created_at, updated_at, deleted
) VALUES
    -- 代喂猫（单猫）
    (2004, 1004, NULL, 22004, 1,  88.00, '2026-06-10', '08:30:00', '09:30:00', 1, '2026-06-05 09:00:00', '2026-06-05 09:00:00', 0),
    -- 代遛狗
    (2005, 1004, NULL, 22005, 1,  68.00, '2026-06-11', '07:00:00', '07:45:00', 2, '2026-06-05 10:00:00', '2026-06-05 10:00:00', 0),
    -- 代喂猫（处方粮）
    (2006, 1005, NULL, 22006, 1,  98.00, '2026-06-10', '12:00:00', '12:45:00', 1, '2026-06-05 11:00:00', '2026-06-05 11:00:00', 0),
    -- 代遛狗（体弱老狗）
    (2007, 1005, NULL, 22007, 1,  58.00, '2026-06-12', '06:30:00', '07:00:00', 2, '2026-06-05 12:00:00', '2026-06-05 12:00:00', 0),
    -- 代喂两只猫
    (2008, 1006, NULL, 22008, 1, 138.00, '2026-06-10', '07:30:00', '08:30:00', 1, '2026-06-05 13:00:00', '2026-06-05 13:00:00', 0),
    -- 代喂猫（晚间）
    (2009, 1006, NULL, 22009, 1,  88.00, '2026-06-11', '19:00:00', '19:45:00', 1, '2026-06-05 14:00:00', '2026-06-05 14:00:00', 0),
    -- 小林发布 代喂猫
    (2010, 1001, NULL, 22010, 1,  78.00, '2026-06-13', '09:00:00', '09:45:00', 1, '2026-06-06 08:00:00', '2026-06-06 08:00:00', 0),
    -- 小陈发布 代遛狗
    (2011, 1003, NULL, 22011, 1,  75.00, '2026-06-14', '07:30:00', '08:15:00', 2, '2026-06-06 09:00:00', '2026-06-06 09:00:00', 0);

-- 6. 订单宠物快照
INSERT INTO order_pets_snapshot (
    snapshot_id, order_id, archive_pet_id, snap_pet_name, snap_pet_type, snap_req, created_at, updated_at, deleted
) VALUES
    (8004, 2004, 3004, '奶茶', 1, '喂猫粮 50g，换清水，清理猫砂盆。',                         '2026-06-05 09:00:00', '2026-06-05 09:00:00', 0),
    (8005, 2005, 3005, '布丁', 2, '遛狗 30 分钟，回家擦脚，避免接触陌生狗。',                 '2026-06-05 10:00:00', '2026-06-05 10:00:00', 0),
    (8006, 2006, 3006, '椰椰', 1, '皇家处方粮 40g，禁止零食，注意排便情况。',                 '2026-06-05 11:00:00', '2026-06-05 11:00:00', 0),
    (8007, 2007, 3007, '芋圆', 2, '轻松遛步 20 分钟，不要跑，回家补水。',                     '2026-06-05 12:00:00', '2026-06-05 12:00:00', 0),
    (8008, 2008, 3008, '拿铁', 1, '湿粮 1/3 罐 + 猫粮 30g，需换水。',                        '2026-06-05 13:00:00', '2026-06-05 13:00:00', 0),
    (8009, 2008, 3009, '美式', 1, '与拿铁分笼喂食，注意隔离，防止抢食。',                     '2026-06-05 13:00:00', '2026-06-05 13:00:00', 0),
    (8010, 2009, 3008, '拿铁', 1, '晚间喂食，湿粮半罐，检查饮水。',                           '2026-06-05 14:00:00', '2026-06-05 14:00:00', 0),
    (8011, 2010, 3001, '团团', 1, '每日更换清水，喂湿粮半罐，观察精神状态。',                 '2026-06-06 08:00:00', '2026-06-06 08:00:00', 0),
    (8012, 2011, 3003, '米粒', 1, '只喂处方粮，禁止零食，上门后先检查水碗。',                 '2026-06-06 09:00:00', '2026-06-06 09:00:00', 0);

-- =============================================================================
-- 原 V17__*
-- =============================================================================

-- 将 V14 测试订单地址迁移至北京，同时把测试宠托师的服务半径设为 0（不限距离）
-- 基准坐标：39.9498556, 116.3358137（石景山/海淀边界）

-- 1. 更新地址快照坐标和区域描述
UPDATE order_address_snapshots SET
    province = '北京市', city = '北京市', district = '海淀区',
    detail_address = '颐和园路 5 号',
    latitude = 39.9927000, longitude = 116.3061000
WHERE snapshot_id = 22004;

UPDATE order_address_snapshots SET
    province = '北京市', city = '北京市', district = '海淀区',
    detail_address = '中关村南大街 5 号',
    latitude = 39.9685000, longitude = 116.3196000
WHERE snapshot_id = 22005;

UPDATE order_address_snapshots SET
    province = '北京市', city = '北京市', district = '石景山区',
    detail_address = '苹果园路 18 号',
    latitude = 39.9400000, longitude = 116.2038000
WHERE snapshot_id = 22006;

UPDATE order_address_snapshots SET
    province = '北京市', city = '北京市', district = '石景山区',
    detail_address = '八角游乐园路 3 号',
    latitude = 39.9224000, longitude = 116.2291000
WHERE snapshot_id = 22007;

UPDATE order_address_snapshots SET
    province = '北京市', city = '北京市', district = '丰台区',
    detail_address = '丰台科技园区景丰路 88 号',
    latitude = 39.8584000, longitude = 116.2851000
WHERE snapshot_id = 22008;

UPDATE order_address_snapshots SET
    province = '北京市', city = '北京市', district = '丰台区',
    detail_address = '马家堡东路 105 号',
    latitude = 39.8667000, longitude = 116.3631000
WHERE snapshot_id = 22009;

UPDATE order_address_snapshots SET
    province = '北京市', city = '北京市', district = '西城区',
    detail_address = '西直门内大街 2 号',
    latitude = 39.9448000, longitude = 116.3564000
WHERE snapshot_id = 22010;

UPDATE order_address_snapshots SET
    province = '北京市', city = '北京市', district = '朝阳区',
    detail_address = '三里屯路 19 号',
    latitude = 39.9369000, longitude = 116.4560000
WHERE snapshot_id = 22011;

-- 2. 把测试宠托师（1002、1003）的服务半径改为 0（不限距离），方便测试
UPDATE sitter_profiles SET service_radius_km = 0 WHERE provider_id IN (1002, 1003);

-- =============================================================================
-- 原 V18__*
-- =============================================================================

-- 批量补充初始数据
-- 目标：各核心表约 20 条记录

INSERT INTO users (
    user_id, phone, nickname, password_hash, avatar_url, role_type, balance, frozen_amount,
    created_at, updated_at, deleted, real_name, id_card_no
) VALUES
    (1010, '13800001010', '宠托-小芳', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 2, 260.00, 0.00, '2026-06-10 09:00:00', '2026-06-10 09:00:00', 0, '芳芳', '110101199010101010'),
    (1011, '13800001011', '宠托-小美', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 2, 310.00, 0.00, '2026-06-10 09:05:00', '2026-06-10 09:05:00', 0, '美美', '110101199011111011'),
    (1012, '13800001012', '宠托-小雨', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 2, 280.00, 0.00, '2026-06-10 09:10:00', '2026-06-10 09:10:00', 0, '雨雨', '110101199012121012'),
    (1013, '13800001013', '宠托-小静', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 2, 350.00, 0.00, '2026-06-10 09:15:00', '2026-06-10 09:15:00', 0, '静静', '110101199013131013'),
    (1014, '13800001014', '宠托-小慧', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 2, 290.00, 0.00, '2026-06-10 09:20:00', '2026-06-10 09:20:00', 0, '慧慧', '110101199014141014'),
    (1015, '13800001015', '宠托-阿强', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 2, 420.00, 0.00, '2026-06-10 09:25:00', '2026-06-10 09:25:00', 0, '强子', '110101199015151015'),
    (1016, '13800001016', '宠托-阿明', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 2, 380.00, 0.00, '2026-06-10 09:30:00', '2026-06-10 09:30:00', 0, '明明', '110101199016161016'),
    (1017, '13800001017', '宠托-阿杰', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 2, 330.00, 0.00, '2026-06-10 09:35:00', '2026-06-10 09:35:00', 0, '杰哥', '110101199017171017'),
    (1018, '13800001018', '宠托-阿文', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 2, 270.00, 0.00, '2026-06-10 09:40:00', '2026-06-10 09:40:00', 0, '文文', '110101199018181018'),
    (1019, '13800001019', '宠主-小夏', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 1, 520.00, 0.00, '2026-06-10 10:00:00', '2026-06-10 10:00:00', 0, '夏夏', '110101199019191019'),
    (1020, '13800001020', '宠主-小秋', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 1, 480.00, 0.00, '2026-06-10 10:05:00', '2026-06-10 10:05:00', 0, '秋秋', '110101199020202020'),
    (1021, '13800001021', '宠主-小冬', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 1, 610.00, 0.00, '2026-06-10 10:10:00', '2026-06-10 10:10:00', 0, '冬冬', '110101199021212021'),
    (1022, '13800001022', '宠主-小春', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 1, 550.00, 0.00, '2026-06-10 10:15:00', '2026-06-10 10:15:00', 0, '春春', '110101199022222022'),
    (1023, '13800001023', '宠主-小阳', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 1, 430.00, 0.00, '2026-06-10 10:20:00', '2026-06-10 10:20:00', 0, '阳阳', '110101199023232023'),
    (1024, '13800001024', '宠主-小月', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 1, 390.00, 0.00, '2026-06-10 10:25:00', '2026-06-10 10:25:00', 0, '月月', '110101199024242024'),
    (1025, '13800001025', '宠主-小星', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 1, 470.00, 0.00, '2026-06-10 10:30:00', '2026-06-10 10:30:00', 0, '星星', '110101199025252025'),
    (1026, '13800001026', '宠主-小云', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 1, 510.00, 0.00, '2026-06-10 10:35:00', '2026-06-10 10:35:00', 0, '云云', '110101199026262026'),
    (1027, '13800001027', '双身份-小海', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 3, 720.00, 50.00, '2026-06-10 10:40:00', '2026-06-10 10:40:00', 0, '海海', '110101199027272027'),
    (1028, '13800001028', '双身份-小江', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 3, 680.00, 0.00, '2026-06-10 10:45:00', '2026-06-10 10:45:00', 0, '江江', '110101199028282028'),
    (1029, '13800001029', '双身份-小湖', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 3, 640.00, 0.00, '2026-06-10 10:50:00', '2026-06-10 10:50:00', 0, '湖湖', '110101199029292029'),
    (1030, '13800001030', '宠主-小石', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 1, 360.00, 0.00, '2026-06-10 11:00:00', '2026-06-10 11:00:00', 0, '石石', '110101199030303030'),
    (1031, '13800001031', '宠主-小木', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 1, 340.00, 0.00, '2026-06-10 11:05:00', '2026-06-10 11:05:00', 0, '木木', '110101199031313031'),
    (1032, '13800001032', '宠主-小水', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 1, 370.00, 0.00, '2026-06-10 11:10:00', '2026-06-10 11:10:00', 0, '水水', '110101199032323032'),
    (1033, '13800001033', '宠主-小火', 'SuU2FqUfi0qolaFjKQLIexkz+G52I17arUJhW+V+J5c=', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 1, 410.00, 0.00, '2026-06-10 11:15:00', '2026-06-10 11:15:00', 0, '火火', '110101199033333033');

INSERT INTO sitter_profiles (
    provider_id, gender, verify_status, deposit_amount, credit_score, service_radius_km,
    resident_address, resident_latitude, resident_longitude, is_banned, created_at, updated_at, deleted
) VALUES
    (1010, 2, 2, 200.00, 92, 8, '北京市海淀区中关村大街1号', 39.9836000, 116.3164000, 0, '2026-06-10 09:00:00', '2026-06-10 09:00:00', 0),
    (1011, 2, 2, 200.00, 88, 6, '北京市朝阳区三里屯路8号', 39.9369000, 116.4560000, 0, '2026-06-10 09:05:00', '2026-06-10 09:05:00', 0),
    (1012, 2, 2, 200.00, 85, 5, '北京市西城区西直门外大街16号', 39.9448000, 116.3564000, 0, '2026-06-10 09:10:00', '2026-06-10 09:10:00', 0),
    (1013, 2, 2, 500.00, 96, 10, '北京市丰台区丰台路66号', 39.8584000, 116.2851000, 0, '2026-06-10 09:15:00', '2026-06-10 09:15:00', 0),
    (1014, 2, 2, 200.00, 90, 7, '北京市石景山区苹果园路20号', 39.9400000, 116.2038000, 0, '2026-06-10 09:20:00', '2026-06-10 09:20:00', 0),
    (1015, 1, 2, 200.00, 82, 5, '北京市海淀区颐和园路5号', 39.9927000, 116.3061000, 0, '2026-06-10 09:25:00', '2026-06-10 09:25:00', 0),
    (1016, 1, 2, 200.00, 84, 6, '北京市朝阳区望京街10号', 39.9950000, 116.4810000, 0, '2026-06-10 09:30:00', '2026-06-10 09:30:00', 0),
    (1017, 1, 2, 200.00, 79, 5, '北京市东城区东直门内大街8号', 39.9410000, 116.4350000, 0, '2026-06-10 09:35:00', '2026-06-10 09:35:00', 0),
    (1018, 1, 2, 200.00, 87, 8, '北京市大兴区亦庄荣华南路12号', 39.7950000, 116.5060000, 0, '2026-06-10 09:40:00', '2026-06-10 09:40:00', 0),
    (1027, 2, 2, 200.00, 91, 9, '北京市通州区新华大街18号', 39.9097000, 116.6564000, 0, '2026-06-10 10:40:00', '2026-06-10 10:40:00', 0),
    (1028, 1, 2, 200.00, 83, 7, '北京市昌平区回龙观西大街6号', 40.0740000, 116.3260000, 0, '2026-06-10 10:45:00', '2026-06-10 10:45:00', 0),
    (1029, 2, 2, 200.00, 89, 6, '北京市顺义区府前街3号', 40.1300000, 116.6540000, 0, '2026-06-10 10:50:00', '2026-06-10 10:50:00', 0);

INSERT INTO pet_owners (owner_id, emergency_contact, created_at, updated_at, deleted) VALUES
    (1019, '13900001019', '2026-06-10 10:00:00', '2026-06-10 10:00:00', 0),
    (1020, '13900001020', '2026-06-10 10:05:00', '2026-06-10 10:05:00', 0),
    (1021, '13900001021', '2026-06-10 10:10:00', '2026-06-10 10:10:00', 0),
    (1022, '13900001022', '2026-06-10 10:15:00', '2026-06-10 10:15:00', 0),
    (1023, '13900001023', '2026-06-10 10:20:00', '2026-06-10 10:20:00', 0),
    (1024, '13900001024', '2026-06-10 10:25:00', '2026-06-10 10:25:00', 0),
    (1025, '13900001025', '2026-06-10 10:30:00', '2026-06-10 10:30:00', 0),
    (1026, '13900001026', '2026-06-10 10:35:00', '2026-06-10 10:35:00', 0),
    (1027, '13900001027', '2026-06-10 10:40:00', '2026-06-10 10:40:00', 0),
    (1028, '13900001028', '2026-06-10 10:45:00', '2026-06-10 10:45:00', 0),
    (1029, '13900001029', '2026-06-10 10:50:00', '2026-06-10 10:50:00', 0),
    (1030, '13900001030', '2026-06-10 11:00:00', '2026-06-10 11:00:00', 0),
    (1031, '13900001031', '2026-06-10 11:05:00', '2026-06-10 11:05:00', 0),
    (1032, '13900001032', '2026-06-10 11:10:00', '2026-06-10 11:10:00', 0),
    (1033, '13900001033', '2026-06-10 11:15:00', '2026-06-10 11:15:00', 0);

INSERT INTO user_addresses (
    address_id, user_id, contact_name, contact_phone, province, city, district, detail_address,
    address_tag, is_default, latitude, longitude, created_at, updated_at, deleted
) VALUES
    (21103, 1019, '小夏', '13900001019', '北京市', '北京市', '海淀区', '中关村南大街12号', '家', 1, 39.9685000, 116.3196000, '2026-06-10 10:00:00', '2026-06-10 10:00:00', 0),
    (21104, 1020, '小秋', '13900001020', '北京市', '北京市', '朝阳区', '建国路88号', '家', 1, 39.9087000, 116.4710000, '2026-06-10 10:05:00', '2026-06-10 10:05:00', 0),
    (21105, 1021, '小冬', '13900001021', '北京市', '北京市', '西城区', '金融街7号', '家', 1, 39.9170000, 116.3660000, '2026-06-10 10:10:00', '2026-06-10 10:10:00', 0),
    (21106, 1022, '小春', '13900001022', '北京市', '北京市', '东城区', '东单北大街1号', '家', 1, 39.9140000, 116.4180000, '2026-06-10 10:15:00', '2026-06-10 10:15:00', 0),
    (21107, 1023, '小阳', '13900001023', '北京市', '北京市', '丰台区', '南四环西路188号', '家', 1, 39.8380000, 116.2870000, '2026-06-10 10:20:00', '2026-06-10 10:20:00', 0),
    (21108, 1024, '小月', '13900001024', '北京市', '北京市', '石景山区', '八角游乐园路5号', '家', 1, 39.9224000, 116.2291000, '2026-06-10 10:25:00', '2026-06-10 10:25:00', 0),
    (21109, 1025, '小星', '13900001025', '北京市', '北京市', '通州区', '运河西大街35号', '家', 1, 39.9020000, 116.6580000, '2026-06-10 10:30:00', '2026-06-10 10:30:00', 0),
    (21110, 1026, '小云', '13900001026', '北京市', '北京市', '昌平区', '回龙观东大街6号', '家', 1, 40.0760000, 116.3360000, '2026-06-10 10:35:00', '2026-06-10 10:35:00', 0),
    (21111, 1027, '小海', '13900001027', '北京市', '北京市', '大兴区', '亦庄荣华中路8号', '家', 1, 39.7970000, 116.5040000, '2026-06-10 10:40:00', '2026-06-10 10:40:00', 0),
    (21112, 1028, '小江', '13900001028', '北京市', '北京市', '顺义区', '府前西街10号', '家', 1, 40.1280000, 116.6520000, '2026-06-10 10:45:00', '2026-06-10 10:45:00', 0),
    (21113, 1029, '小湖', '13900001029', '北京市', '北京市', '房山区', '良乡拱辰大街2号', '家', 1, 39.7350000, 116.1430000, '2026-06-10 10:50:00', '2026-06-10 10:50:00', 0),
    (21114, 1004, '大白', '13900000004', '北京市', '北京市', '海淀区', '颐和园路20号', '公司', 0, 39.9900000, 116.3100000, '2026-06-11 09:00:00', '2026-06-11 09:00:00', 0),
    (21115, 1005, '小橙', '13900000005', '北京市', '北京市', '朝阳区', '工体北路13号', '家', 0, 39.9330000, 116.4470000, '2026-06-11 09:05:00', '2026-06-11 09:05:00', 0),
    (21116, 1006, '阿蓝', '13900000006', '北京市', '北京市', '西城区', '德胜门外大街1号', '家', 0, 39.9520000, 116.3780000, '2026-06-11 09:10:00', '2026-06-11 09:10:00', 0),
    (21117, 1001, '小林', '13900000001', '北京市', '北京市', '海淀区', '知春路56号', '家', 0, 39.9760000, 116.3390000, '2026-06-11 09:15:00', '2026-06-11 09:15:00', 0),
    (21118, 1003, '小陈', '13900000003', '北京市', '北京市', '朝阳区', '望京SOHO T1', '家', 0, 39.9960000, 116.4810000, '2026-06-11 09:20:00', '2026-06-11 09:20:00', 0),
    (21119, 1010, '小芳', '13800001010', '北京市', '北京市', '海淀区', '学院路30号', '家', 1, 39.9840000, 116.3500000, '2026-06-11 09:25:00', '2026-06-11 09:25:00', 0),
    (21120, 1011, '小美', '13800001011', '北京市', '北京市', '朝阳区', '光华路9号', '家', 1, 39.9140000, 116.4600000, '2026-06-11 09:30:00', '2026-06-11 09:30:00', 0);

INSERT INTO pet_archives (pet_id, owner_id, pet_name, pet_type, default_req, image, created_at, updated_at, deleted) VALUES
    (3010, 1019, '橘子', 1, '每天换粮换水，清理猫砂。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', '2026-06-10 10:01:00', '2026-06-10 10:01:00', 0),
    (3011, 1019, '旺财', 2, '遛狗25分钟，擦脚后回家。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', '2026-06-10 10:02:00', '2026-06-10 10:02:00', 0),
    (3012, 1020, '雪球', 1, '只吃冻干，禁止罐头。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', '2026-06-10 10:06:00', '2026-06-10 10:06:00', 0),
    (3013, 1021, '豆豆', 2, '老年犬，慢走15分钟。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', '2026-06-10 10:11:00', '2026-06-10 10:11:00', 0),
    (3014, 1022, '灰灰', 1, '两只猫分笼喂食。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', '2026-06-10 10:16:00', '2026-06-10 10:16:00', 0),
    (3015, 1023, '乐乐', 2, '需佩戴嘴套，避免捡食。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', '2026-06-10 10:21:00', '2026-06-10 10:21:00', 0),
    (3016, 1024, '咪咪', 1, '观察精神状态并拍照。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', '2026-06-10 10:26:00', '2026-06-10 10:26:00', 0),
    (3017, 1025, '虎子', 2, '大型犬，需有力牵引。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', '2026-06-10 10:31:00', '2026-06-10 10:31:00', 0),
    (3018, 1026, '花花', 1, '长毛猫，需梳毛5分钟。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', '2026-06-10 10:36:00', '2026-06-10 10:36:00', 0),
    (3019, 1027, '小七', 2, '按时喂药，药在玄关抽屉。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', '2026-06-10 10:41:00', '2026-06-10 10:41:00', 0),
    (3020, 1028, '奶糖', 1, '幼猫，少量多餐。', 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', '2026-06-10 10:46:00', '2026-06-10 10:46:00', 0);

INSERT INTO order_address_snapshots (
    snapshot_id, source_address_id, contact_name, contact_phone, province, city, district, detail_address,
    address_tag, latitude, longitude, created_at, updated_at, deleted
) VALUES
    (22012, 21103, '小夏', '13900001019', '北京市', '北京市', '海淀区', '中关村南大街12号', '家', 39.9685000, 116.3196000, '2026-06-12 08:00:00', '2026-06-12 08:00:00', 0),
    (22013, 21104, '小秋', '13900001020', '北京市', '北京市', '朝阳区', '建国路88号', '家', 39.9087000, 116.4710000, '2026-06-12 08:10:00', '2026-06-12 08:10:00', 0),
    (22014, 21105, '小冬', '13900001021', '北京市', '北京市', '西城区', '金融街7号', '家', 39.9170000, 116.3660000, '2026-06-12 08:20:00', '2026-06-12 08:20:00', 0),
    (22015, 21106, '小春', '13900001022', '北京市', '北京市', '东城区', '东单北大街1号', '家', 39.9140000, 116.4180000, '2026-06-12 08:30:00', '2026-06-12 08:30:00', 0),
    (22016, 21107, '小阳', '13900001023', '北京市', '北京市', '丰台区', '南四环西路188号', '家', 39.8380000, 116.2870000, '2026-06-12 08:40:00', '2026-06-12 08:40:00', 0),
    (22017, 21108, '小月', '13900001024', '北京市', '北京市', '石景山区', '八角游乐园路5号', '家', 39.9224000, 116.2291000, '2026-06-12 08:50:00', '2026-06-12 08:50:00', 0),
    (22018, 21109, '小星', '13900001025', '北京市', '北京市', '通州区', '运河西大街35号', '家', 39.9020000, 116.6580000, '2026-06-12 09:00:00', '2026-06-12 09:00:00', 0),
    (22019, 21110, '小云', '13900001026', '北京市', '北京市', '昌平区', '回龙观东大街6号', '家', 40.0760000, 116.3360000, '2026-06-12 09:10:00', '2026-06-12 09:10:00', 0),
    (22020, 21111, '小海', '13900001027', '北京市', '北京市', '大兴区', '亦庄荣华中路8号', '家', 39.7970000, 116.5040000, '2026-06-12 09:20:00', '2026-06-12 09:20:00', 0);

INSERT INTO orders (
    order_id, owner_id, provider_id, address_snapshot_id, order_status, total_amount, service_date,
    service_start_time, service_end_time, service_type, hard_filter_tags, created_at, updated_at, deleted
) VALUES
    (2012, 1019, NULL, 22012, 1, 88.00, '2026-06-20', '09:00:00', '10:00:00', 1, '["FEMALE_ONLY"]', '2026-06-12 08:00:00', '2026-06-12 08:00:00', 0),
    (2013, 1020, NULL, 22013, 1, 76.00, '2026-06-21', '14:00:00', '15:00:00', 2, NULL, '2026-06-12 08:10:00', '2026-06-12 08:10:00', 0),
    (2014, 1021, 1010, 22014, 6, 128.00, '2026-06-15', '10:00:00', '11:00:00', 1, NULL, '2026-06-12 08:20:00', '2026-06-15 18:00:00', 0),
    (2015, 1022, 1011, 22015, 3, 98.00, '2026-06-22', '08:30:00', '09:30:00', 1, NULL, '2026-06-12 08:30:00', '2026-06-12 08:30:00', 0),
    (2016, 1023, NULL, 22016, 1, 68.00, '2026-06-23', '07:00:00', '07:45:00', 2, NULL, '2026-06-12 08:40:00', '2026-06-12 08:40:00', 0),
    (2017, 1024, 1012, 22017, 4, 108.00, '2026-06-18', '11:00:00', '12:00:00', 1, NULL, '2026-06-12 08:50:00', '2026-06-18 12:00:00', 0),
    (2018, 1025, 1014, 22018, 2, 86.00, '2026-06-19', '16:00:00', '17:00:00', 1, '["FEMALE_ONLY"]', '2026-06-12 09:00:00', '2026-06-12 09:00:00', 0),
    (2019, 1026, 1013, 22019, 5, 118.00, '2026-06-17', '13:00:00', '14:00:00', 1, NULL, '2026-06-12 09:10:00', '2026-06-17 14:00:00', 0),
    (2020, 1027, NULL, 22020, 1, 92.00, '2026-06-24', '10:30:00', '11:30:00', 2, NULL, '2026-06-12 09:20:00', '2026-06-12 09:20:00', 0);

INSERT INTO order_pets_snapshot (
    snapshot_id, order_id, archive_pet_id, snap_pet_name, snap_pet_type, snap_req, created_at, updated_at, deleted
) VALUES
    (8013, 2012, 3010, '橘子', 1, '换粮换水，清理猫砂。', '2026-06-12 08:00:00', '2026-06-12 08:00:00', 0),
    (8014, 2013, 3012, '雪球', 1, '只喂冻干。', '2026-06-12 08:10:00', '2026-06-12 08:10:00', 0),
    (8015, 2014, 3013, '豆豆', 2, '老年犬慢走。', '2026-06-12 08:20:00', '2026-06-12 08:20:00', 0),
    (8016, 2015, 3014, '灰灰', 1, '分笼喂食。', '2026-06-12 08:30:00', '2026-06-12 08:30:00', 0),
    (8017, 2016, 3015, '乐乐', 2, '佩戴嘴套遛狗。', '2026-06-12 08:40:00', '2026-06-12 08:40:00', 0),
    (8018, 2017, 3016, '咪咪', 1, '观察精神并拍照。', '2026-06-12 08:50:00', '2026-06-12 08:50:00', 0),
    (8019, 2018, 3017, '虎子', 2, '大型犬需牵引。', '2026-06-12 09:00:00', '2026-06-12 09:00:00', 0),
    (8020, 2019, 3018, '花花', 1, '梳毛5分钟。', '2026-06-12 09:10:00', '2026-06-12 09:10:00', 0);

INSERT INTO order_applications (apply_id, order_id, provider_id, apply_status, created_at, updated_at, deleted) VALUES
    (4010, 2012, 1010, 0, '2026-06-12 10:00:00', '2026-06-12 10:00:00', 0),
    (4011, 2012, 1011, 0, '2026-06-12 10:05:00', '2026-06-12 10:05:00', 0),
    (4012, 2012, 1013, 0, '2026-06-12 10:10:00', '2026-06-12 10:10:00', 0),
    (4013, 2013, 1015, 0, '2026-06-12 10:15:00', '2026-06-12 10:15:00', 0),
    (4014, 2013, 1016, 0, '2026-06-12 10:20:00', '2026-06-12 10:20:00', 0),
    (4015, 2014, 1010, 2, '2026-06-12 10:25:00', '2026-06-12 10:30:00', 0),
    (4016, 2015, 1011, 2, '2026-06-12 10:35:00', '2026-06-12 10:40:00', 0),
    (4017, 2016, 1017, 0, '2026-06-12 10:45:00', '2026-06-12 10:45:00', 0),
    (4018, 2016, 1018, 0, '2026-06-12 10:50:00', '2026-06-12 10:50:00', 0),
    (4019, 2017, 1012, 2, '2026-06-12 11:00:00', '2026-06-12 11:05:00', 0),
    (4020, 2018, 1014, 2, '2026-06-12 11:10:00', '2026-06-12 11:10:00', 0),
    (4021, 2018, 1010, 0, '2026-06-12 11:15:00', '2026-06-12 11:15:00', 0),
    (4022, 2019, 1013, 2, '2026-06-12 11:20:00', '2026-06-12 11:25:00', 0),
    (4023, 2020, 1015, 0, '2026-06-12 11:30:00', '2026-06-12 11:30:00', 0),
    (4024, 2002, 1010, 0, '2026-06-12 11:35:00', '2026-06-12 11:35:00', 0),
    (4025, 2004, 1011, 0, '2026-06-12 11:40:00', '2026-06-12 11:40:00', 0),
    (4026, 2006, 1012, 0, '2026-06-12 11:45:00', '2026-06-12 11:45:00', 0);

INSERT INTO fulfillment_records (
    record_id, order_id, node_type, latitude, longitude, image_url, media_type, object_key, file_size,
    content_type, processing_status, created_at, updated_at, deleted
) VALUES
    (10010, 2014, 1, 39.9171000, 116.3661000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 98304, 'image/jpeg', 'SUCCESS', '2026-06-15 10:05:00', '2026-06-15 10:05:00', 0),
    (10011, 2014, 3, 39.9172000, 116.3662000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 102400, 'image/jpeg', 'SUCCESS', '2026-06-15 10:25:00', '2026-06-15 10:25:00', 0),
    (10012, 2014, 6, 39.9173000, 116.3663000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 105000, 'image/jpeg', 'SUCCESS', '2026-06-15 10:50:00', '2026-06-15 10:50:00', 0),
    (10013, 2017, 1, 39.9225000, 116.2292000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 99000, 'image/jpeg', 'SUCCESS', '2026-06-18 11:05:00', '2026-06-18 11:05:00', 0),
    (10014, 2017, 3, 39.9226000, 116.2293000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 101000, 'image/jpeg', 'SUCCESS', '2026-06-18 11:25:00', '2026-06-18 11:25:00', 0),
    (10015, 2019, 1, 39.9021000, 116.6581000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 97000, 'image/jpeg', 'SUCCESS', '2026-06-17 13:05:00', '2026-06-17 13:05:00', 0),
    (10016, 2019, 3, 39.9022000, 116.6582000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 103000, 'image/jpeg', 'SUCCESS', '2026-06-17 13:25:00', '2026-06-17 13:25:00', 0),
    (10017, 2001, 2, 31.1887000, 121.4366000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 96000, 'image/jpeg', 'SUCCESS', '2026-05-01 09:10:00', '2026-05-01 09:10:00', 0),
    (10018, 2003, 5, 31.2214000, 121.5441000, NULL, 'VIDEO', 'fulfillment/orders/2003/nodes/5/10018.mp4', 2048000, 'video/mp4', 'SUCCESS', '2026-05-05 10:50:00', '2026-05-05 10:50:00', 0),
    (10019, 2015, 1, 39.9141000, 116.4181000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 88000, 'image/jpeg', 'SUCCESS', '2026-06-22 08:35:00', '2026-06-22 08:35:00', 0),
    (10020, 2015, 3, 39.9142000, 116.4182000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 91000, 'image/jpeg', 'SUCCESS', '2026-06-22 08:55:00', '2026-06-22 08:55:00', 0),
    (10021, 2004, 1, 39.9928000, 116.3062000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 94000, 'image/jpeg', 'SUCCESS', '2026-06-10 08:35:00', '2026-06-10 08:35:00', 0),
    (10022, 2005, 1, 39.9686000, 116.3197000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 93000, 'image/jpeg', 'SUCCESS', '2026-06-11 07:05:00', '2026-06-11 07:05:00', 0),
    (10023, 2006, 3, 39.9401000, 116.2039000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 95000, 'image/jpeg', 'SUCCESS', '2026-06-10 12:10:00', '2026-06-10 12:10:00', 0),
    (10024, 2007, 5, 39.9225000, 116.2292000, NULL, 'VIDEO', 'fulfillment/orders/2007/nodes/5/10024.mp4', 1800000, 'video/mp4', 'SUCCESS', '2026-06-12 06:35:00', '2026-06-12 06:35:00', 0),
    (10025, 2008, 1, 39.8585000, 116.2852000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 92000, 'image/jpeg', 'SUCCESS', '2026-06-10 07:35:00', '2026-06-10 07:35:00', 0),
    (10026, 2009, 6, 39.8668000, 116.3632000, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'IMAGE', 'seed/default/pets-seed.jpg', 89000, 'image/jpeg', 'SUCCESS', '2026-06-11 19:40:00', '2026-06-11 19:40:00', 0);

INSERT INTO reviews (
    review_id, order_id, reviewer_id, target_id, score, overall_score, punctuality_score, professional_score,
    is_low_score, review_status, content, review_type, created_at, updated_at, deleted
) VALUES
    (11010, 2014, 1021, 1010, 5, 5, 5, 5, 0, 1, '准时上门，宠物状态反馈清晰。', 1, '2026-06-15 19:00:00', '2026-06-15 19:00:00', 0),
    (11011, 2014, 1010, 1021, 5, 5, 5, 5, 0, 1, '宠主沟通及时，要求明确。', 2, '2026-06-15 19:10:00', '2026-06-15 19:10:00', 0),
    (11012, 2017, 1024, 1012, 4, 4, 4, 5, 0, 1, '整体不错，照片稍晚上传。', 1, '2026-06-18 20:00:00', '2026-06-18 20:00:00', 0),
    (11013, 2017, 1012, 1024, 5, 5, 5, 5, 0, 1, '地址准确，进门方式说明清楚。', 2, '2026-06-18 20:10:00', '2026-06-18 20:10:00', 0),
    (11014, 2008, 1006, 1013, 5, 5, 5, 5, 0, 1, '两只猫分笼喂食执行到位。', 1, '2026-06-10 20:00:00', '2026-06-10 20:00:00', 0),
    (11015, 2009, 1006, 1012, 4, 4, 4, 4, 0, 1, '晚间服务整体满意。', 1, '2026-06-11 20:00:00', '2026-06-11 20:00:00', 0),
    (11016, 2019, 1026, 1013, 5, 5, 5, 4, 0, 1, '梳毛很细致，推荐。', 1, '2026-06-17 20:00:00', '2026-06-17 20:00:00', 0),
    (11017, 2019, 1013, 1026, 5, 5, 5, 5, 0, 1, '配合度高。', 2, '2026-06-17 20:10:00', '2026-06-17 20:10:00', 0),
    (11018, 2015, 1022, 1011, 3, 3, 3, 3, 1, 1, '迟到10分钟，服务尚可。', 1, '2026-06-22 20:00:00', '2026-06-22 20:00:00', 0),
    (11019, 2015, 1011, 1022, 4, 4, 4, 4, 0, 1, '地址略难找。', 2, '2026-06-22 20:10:00', '2026-06-22 20:10:00', 0),
    (11020, 2003, 1003, 1002, 4, 4, 4, 4, 0, 1, '整体满意。', 1, '2026-05-05 20:00:00', '2026-05-05 20:00:00', 0),
    (11021, 2003, 1002, 1003, 5, 5, 5, 5, 0, 1, '要求合理。', 2, '2026-05-05 20:10:00', '2026-05-05 20:10:00', 0),
    (11022, 2012, 1019, 1010, 5, 5, 5, 5, 0, 1, '沟通顺畅，等待服务。', 1, '2026-06-12 12:00:00', '2026-06-12 12:00:00', 0),
    (11023, 2013, 1020, 1015, 5, 5, 5, 5, 0, 1, '已确认上门时间。', 1, '2026-06-12 12:05:00', '2026-06-12 12:05:00', 0),
    (11024, 2016, 1023, 1017, 4, 4, 4, 4, 0, 1, '预约已确认。', 1, '2026-06-12 12:10:00', '2026-06-12 12:10:00', 0),
    (11025, 2018, 1025, 1014, 5, 5, 5, 5, 0, 1, '要求已同步。', 1, '2026-06-12 12:15:00', '2026-06-12 12:15:00', 0),
    (11026, 2020, 1027, 1015, 5, 5, 5, 5, 0, 1, '期待服务。', 1, '2026-06-12 12:20:00', '2026-06-12 12:20:00', 0),
    (11027, 2004, 1004, 1011, 5, 5, 5, 5, 0, 1, '沟通良好。', 1, '2026-06-12 12:25:00', '2026-06-12 12:25:00', 0);

INSERT INTO chats (message_id, order_id, sender_id, receiver_id, content, is_read, created_at, updated_at, deleted) VALUES
    (9010, 2012, 1019, 1010, '你好，请准时上门，猫粮在厨房。', 1, '2026-06-12 10:00:00', '2026-06-12 10:00:00', 0),
    (9011, 2012, 1010, 1019, '收到，我会提前10分钟到。', 1, '2026-06-12 10:05:00', '2026-06-12 10:05:00', 0),
    (9012, 2013, 1020, 1015, '狗狗比较怕生，请轻声进入。', 0, '2026-06-12 10:10:00', '2026-06-12 10:10:00', 0),
    (9013, 2014, 1021, 1010, '豆豆今天精神一般，请多观察。', 1, '2026-06-15 09:50:00', '2026-06-15 09:50:00', 0),
    (9014, 2014, 1010, 1021, '已完成喂食并拍照上传。', 1, '2026-06-15 10:30:00', '2026-06-15 10:30:00', 0),
    (9015, 2015, 1022, 1011, '门禁密码是666888。', 0, '2026-06-22 08:00:00', '2026-06-22 08:00:00', 0),
    (9016, 2016, 1023, 1017, '请佩戴嘴套，谢谢。', 0, '2026-06-12 11:00:00', '2026-06-12 11:00:00', 0),
    (9017, 2017, 1024, 1012, '咪咪今天可能躲床底，请耐心寻找。', 1, '2026-06-18 10:50:00', '2026-06-18 10:50:00', 0),
    (9018, 2018, 1025, 1014, '仅限女性上门，谢谢理解。', 0, '2026-06-12 11:10:00', '2026-06-12 11:10:00', 0),
    (9019, 2019, 1026, 1013, '梳毛工具在阳台。', 1, '2026-06-17 12:50:00', '2026-06-17 12:50:00', 0),
    (9020, 2020, 1027, 1015, '大型犬，请确认牵引绳牢固。', 0, '2026-06-12 11:20:00', '2026-06-12 11:20:00', 0),
    (9021, 2002, 1001, 1002, '团团今天要多观察饮水。', 1, '2026-05-03 13:30:00', '2026-05-03 13:30:00', 0),
    (9022, 2004, 1004, 1011, '奶茶比较怕陌生人。', 0, '2026-06-10 08:00:00', '2026-06-10 08:00:00', 0),
    (9023, 2006, 1005, 1012, '处方粮在冰箱上层。', 0, '2026-06-10 11:30:00', '2026-06-10 11:30:00', 0),
    (9024, 2008, 1006, 1013, '两只猫要分笼喂食。', 1, '2026-06-10 07:00:00', '2026-06-10 07:00:00', 0),
    (9025, 2010, 1001, 1010, '请自带鞋套。', 0, '2026-06-13 08:30:00', '2026-06-13 08:30:00', 0),
    (9026, 2011, 1003, 1015, '米粒只吃处方粮。', 0, '2026-06-14 07:00:00', '2026-06-14 07:00:00', 0);

INSERT INTO exception_reports (
    report_id, order_id, reporter_id, exception_type, description, proof_images, report_status,
    created_at, updated_at, deleted, resolved_at
) VALUES
    (6010, 2017, 1012, 2, '猫咪躲藏未进食，已联系宠主。', '["https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg"]', 1, '2026-06-18 11:30:00', '2026-06-18 11:30:00', 0, NULL),
    (6011, 2015, 1011, 1, '门禁临时失效，等待宠主远程开门。', '["https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg"]', 0, '2026-06-22 08:40:00', '2026-06-22 08:40:00', 0, NULL),
    (6012, 2019, 1013, 3, '猫砂余量不足，已告知宠主。', NULL, 2, '2026-06-17 13:40:00', '2026-06-17 14:00:00', 0, '2026-06-17 14:00:00'),
    (6013, 2003, 1002, 2, '宠物食欲一般，已拍照记录。', '["https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg"]', 0, '2026-05-05 11:20:00', '2026-05-05 11:20:00', 0, NULL),
    (6014, 2007, 1016, 4, '电梯故障，步行上楼延迟5分钟。', NULL, 2, '2026-06-12 06:40:00', '2026-06-12 07:00:00', 0, '2026-06-12 07:00:00'),
    (6015, 2014, 1010, 5, '垃圾袋用完，使用自备袋子处理。', NULL, 2, '2026-06-15 10:55:00', '2026-06-15 11:10:00', 0, '2026-06-15 11:10:00'),
    (6016, 2008, 1013, 3, '猫粮桶盖损坏，已拍照说明。', '["https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg"]', 1, '2026-06-10 08:00:00', '2026-06-10 08:00:00', 0, NULL),
    (6017, 2012, 1010, 1, '小区临时登记，稍有延迟。', NULL, 0, '2026-06-12 09:50:00', '2026-06-12 09:50:00', 0, NULL),
    (6018, 2005, 1015, 5, '雨天路滑，到达稍晚。', NULL, 2, '2026-06-11 07:10:00', '2026-06-11 07:30:00', 0, '2026-06-11 07:30:00'),
    (6019, 2006, 1012, 2, '宠物轻微软便，已联系宠主。', '["https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg"]', 1, '2026-06-10 12:30:00', '2026-06-10 12:30:00', 0, NULL),
    (6020, 2016, 1017, 4, '牵引绳卡扣较紧，已调整。', NULL, 2, '2026-06-12 07:10:00', '2026-06-12 07:20:00', 0, '2026-06-12 07:20:00'),
    (6021, 2004, 1011, 1, '门禁需人脸识别，等待宠主授权。', NULL, 0, '2026-06-10 08:40:00', '2026-06-10 08:40:00', 0, NULL),
    (6022, 2018, 1014, 5, '宠主临时改时间，已确认新时段。', NULL, 2, '2026-06-12 15:00:00', '2026-06-12 15:10:00', 0, '2026-06-12 15:10:00'),
    (6023, 2020, 1015, 2, '狗狗对陌生人吠叫，已保持距离。', '["https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg"]', 0, '2026-06-12 10:40:00', '2026-06-12 10:40:00', 0, NULL),
    (6024, 2009, 1012, 3, '晚间光线不足，拍照略模糊。', '["https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg"]', 2, '2026-06-11 19:50:00', '2026-06-11 20:00:00', 0, '2026-06-11 20:00:00'),
    (6025, 2010, 1010, 5, '鞋套已自备，无需担心。', NULL, 2, '2026-06-13 08:50:00', '2026-06-13 09:00:00', 0, '2026-06-13 09:00:00'),
    (6026, 2011, 1015, 4, '电梯拥挤，改走楼梯。', NULL, 2, '2026-06-14 07:40:00', '2026-06-14 07:50:00', 0, '2026-06-14 07:50:00'),
    (6027, 2001, 1002, 5, '垃圾已带走。', NULL, 2, '2026-05-01 09:40:00', '2026-05-01 09:50:00', 0, '2026-05-01 09:50:00');

INSERT INTO arbitration_records (
    arbitration_id, order_id, plaintiff_id, defendant_id, arb_type, reason, evidence_urls,
    arbitration_status, result_type, admin_memo, created_at, updated_at, deleted, closed_at
) VALUES
    (7010, 2015, 1022, 1011, 1, '认为服务迟到影响体验。', '["https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg"]', 0, NULL, NULL, '2026-06-22 20:00:00', '2026-06-22 20:00:00', 0, NULL),
    (7011, 2017, 1024, 1012, 1, '照片上传时间偏晚。', NULL, 3, 3, '双方和解，服务者补传照片。', '2026-06-18 21:00:00', '2026-06-19 10:00:00', 0, '2026-06-19 10:00:00'),
    (7012, 2003, 1003, 1002, 2, '宠物状态异常争议。', '["https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg"]', 1, NULL, NULL, '2026-05-05 14:00:00', '2026-05-05 14:00:00', 0, NULL),
    (7013, 2014, 1021, 1010, 5, '退款金额协商。', NULL, 3, 3, '已部分退款和解。', '2026-06-16 09:00:00', '2026-06-16 15:00:00', 0, '2026-06-16 15:00:00'),
    (7014, 2001, 1001, 1002, 1, '服务照片数量争议。', NULL, 3, 3, '已补充说明。', '2026-05-02 10:00:00', '2026-05-02 16:00:00', 0, '2026-05-02 16:00:00'),
    (7015, 2019, 1026, 1013, 4, '评价申诉。', '["https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg"]', 0, NULL, NULL, '2026-06-18 09:00:00', '2026-06-18 09:00:00', 0, NULL),
    (7016, 2008, 1006, 1013, 1, '分笼喂食执行争议。', NULL, 2, NULL, NULL, '2026-06-11 10:00:00', '2026-06-11 10:00:00', 0, NULL),
    (7017, 2012, 1019, 1010, 5, '悬赏单取消纠纷。', NULL, 0, NULL, NULL, '2026-06-13 11:00:00', '2026-06-13 11:00:00', 0, NULL),
    (7018, 2006, 1005, 1012, 3, '物品损坏争议。', '["https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg"]', 1, NULL, NULL, '2026-06-11 12:00:00', '2026-06-11 12:00:00', 0, NULL),
    (7019, 2016, 1023, 1017, 1, '遛狗路线争议。', NULL, 3, 2, '判定服务者胜诉。', '2026-06-13 08:00:00', '2026-06-14 09:00:00', 0, '2026-06-14 09:00:00'),
    (7020, 2004, 1004, 1011, 1, '上门时间争议。', NULL, 0, NULL, NULL, '2026-06-11 09:00:00', '2026-06-11 09:00:00', 0, NULL),
    (7021, 2005, 1004, 1015, 1, '服务时长不足争议。', NULL, 3, 3, '和解完成。', '2026-06-12 08:00:00', '2026-06-12 12:00:00', 0, '2026-06-12 12:00:00'),
    (7022, 2018, 1025, 1014, 5, '待支付订单取消争议。', NULL, 0, NULL, NULL, '2026-06-13 10:00:00', '2026-06-13 10:00:00', 0, NULL),
    (7023, 2020, 1027, 1015, 1, '大型犬服务争议。', NULL, 1, NULL, NULL, '2026-06-13 11:00:00', '2026-06-13 11:00:00', 0, NULL),
    (7024, 2007, 1005, 1016, 1, '遛狗强度争议。', NULL, 3, 3, '双方和解。', '2026-06-13 07:00:00', '2026-06-13 15:00:00', 0, '2026-06-13 15:00:00'),
    (7025, 2010, 1001, 1010, 1, '服务细节争议。', NULL, 0, NULL, NULL, '2026-06-14 10:00:00', '2026-06-14 10:00:00', 0, NULL),
    (7026, 2011, 1003, 1015, 4, '评价申诉。', NULL, 0, NULL, NULL, '2026-06-15 09:00:00', '2026-06-15 09:00:00', 0, NULL),
    (7027, 2009, 1006, 1012, 1, '晚间服务争议。', NULL, 3, 1, '判定宠主胜诉。', '2026-06-12 20:00:00', '2026-06-13 10:00:00', 0, '2026-06-13 10:00:00');

INSERT INTO review_deduction_reasons (reason_id, review_id, reason_type, reason_text, created_at, updated_at, deleted) VALUES
    (12001, 11018, 1, '迟到约10分钟', '2026-06-22 20:00:00', '2026-06-22 20:00:00', 0),
    (12002, 11012, 5, '照片上传略晚', '2026-06-18 20:00:00', '2026-06-18 20:00:00', 0),
    (12003, 11020, 2, NULL, '2026-05-05 20:00:00', '2026-05-05 20:00:00', 0),
    (12004, 11001, 6, NULL, '2026-05-01 20:00:00', '2026-05-01 20:00:00', 0),
    (12005, 11010, 6, NULL, '2026-06-15 19:00:00', '2026-06-15 19:00:00', 0),
    (12006, 11011, 6, NULL, '2026-06-15 19:10:00', '2026-06-15 19:10:00', 0),
    (12007, 11013, 6, NULL, '2026-06-18 20:10:00', '2026-06-18 20:10:00', 0),
    (12008, 11016, 6, NULL, '2026-06-17 20:00:00', '2026-06-17 20:00:00', 0),
    (12009, 11017, 6, NULL, '2026-06-17 20:10:00', '2026-06-17 20:10:00', 0),
    (12010, 11019, 1, '地址难找', '2026-06-22 20:10:00', '2026-06-22 20:10:00', 0),
    (12011, 11021, 6, NULL, '2026-05-05 20:10:00', '2026-05-05 20:10:00', 0),
    (12012, 11022, 6, NULL, '2026-06-12 12:00:00', '2026-06-12 12:00:00', 0),
    (12013, 11023, 6, NULL, '2026-06-12 12:05:00', '2026-06-12 12:05:00', 0),
    (12014, 11024, 6, NULL, '2026-06-12 12:10:00', '2026-06-12 12:10:00', 0),
    (12015, 11025, 6, NULL, '2026-06-12 12:15:00', '2026-06-12 12:15:00', 0),
    (12016, 11026, 6, NULL, '2026-06-12 12:20:00', '2026-06-12 12:20:00', 0),
    (12017, 11027, 6, NULL, '2026-06-12 12:25:00', '2026-06-12 12:25:00', 0),
    (12018, 11002, 6, NULL, '2026-05-01 20:10:00', '2026-05-01 20:10:00', 0),
    (12019, 11014, 6, NULL, '2026-05-01 20:00:00', '2026-05-01 20:00:00', 0),
    (12020, 11015, 6, NULL, '2026-05-01 20:10:00', '2026-05-01 20:10:00', 0);

INSERT INTO review_attachments (
    attachment_id, review_id, url, object_key, media_type, content_type, file_size, sort_order, created_at, updated_at, deleted
) VALUES
    (13001, 11010, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 88000, 1, '2026-06-15 19:00:00', '2026-06-15 19:00:00', 0),
    (13002, 11012, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 92000, 1, '2026-06-18 20:00:00', '2026-06-18 20:00:00', 0),
    (13003, 11018, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 76000, 1, '2026-06-22 20:00:00', '2026-06-22 20:00:00', 0),
    (13004, 11001, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 81000, 1, '2026-05-01 20:00:00', '2026-05-01 20:00:00', 0),
    (13005, 11016, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 85000, 1, '2026-06-17 20:00:00', '2026-06-17 20:00:00', 0),
    (13006, 11020, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 79000, 1, '2026-05-05 20:00:00', '2026-05-05 20:00:00', 0),
    (13007, 11014, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 83000, 1, '2026-05-01 20:00:00', '2026-05-01 20:00:00', 0),
    (13008, 11011, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 87000, 1, '2026-06-15 19:10:00', '2026-06-15 19:10:00', 0),
    (13009, 11013, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 90000, 1, '2026-06-18 20:10:00', '2026-06-18 20:10:00', 0),
    (13010, 11017, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 86000, 1, '2026-06-17 20:10:00', '2026-06-17 20:10:00', 0),
    (13011, 11019, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 74000, 1, '2026-06-22 20:10:00', '2026-06-22 20:10:00', 0),
    (13012, 11021, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 82000, 1, '2026-05-05 20:10:00', '2026-05-05 20:10:00', 0),
    (13013, 11022, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 78000, 1, '2026-06-12 12:00:00', '2026-06-12 12:00:00', 0),
    (13014, 11023, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 77000, 1, '2026-06-12 12:05:00', '2026-06-12 12:05:00', 0),
    (13015, 11024, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 80000, 1, '2026-06-12 12:10:00', '2026-06-12 12:10:00', 0),
    (13016, 11025, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 81000, 1, '2026-06-12 12:15:00', '2026-06-12 12:15:00', 0),
    (13017, 11026, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 79000, 1, '2026-06-12 12:20:00', '2026-06-12 12:20:00', 0),
    (13018, 11027, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 84000, 1, '2026-06-12 12:25:00', '2026-06-12 12:25:00', 0),
    (13019, 11002, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 88000, 1, '2026-05-01 20:10:00', '2026-05-01 20:10:00', 0),
    (13020, 11015, 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg', 'seed/default/pets-seed.jpg', 'IMAGE', 'image/jpeg', 86000, 1, '2026-05-01 20:10:00', '2026-05-01 20:10:00', 0);

INSERT INTO review_appeals (
    appeal_id, review_id, order_id, provider_id, owner_id, reason, evidence_urls, appeal_status,
    admin_memo, appeal_deadline, closed_at, created_at, updated_at, deleted
) VALUES
    (14001, 11018, 2015, 1011, 1022, '实际仅迟到5分钟，请求复核。', '["https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/default/pets-seed.jpg"]', 1, NULL, '2026-07-22 20:00:00', NULL, '2026-06-23 09:00:00', '2026-06-23 09:00:00', 0),
    (14002, 11012, 2017, 1012, 1024, '照片因网络延迟上传。', NULL, 4, '申诉失败，维持原评价。', '2026-07-18 20:00:00', '2026-06-25 10:00:00', '2026-06-19 09:00:00', '2026-06-25 10:00:00', 0),
    (14003, 11020, 2003, 1002, 1003, '服务过程已充分沟通。', NULL, 1, NULL, '2026-06-05 20:00:00', NULL, '2026-05-06 09:00:00', '2026-05-06 09:00:00', 0),
    (14004, 11001, 2001, 1002, 1001, '已完成全部服务项目。', NULL, 5, '申诉驳回。', '2026-05-01 20:00:00', '2026-05-10 10:00:00', '2026-05-02 09:00:00', '2026-05-10 10:00:00', 0),
    (14005, 11016, 2019, 1013, 1026, '梳毛时间超出预期但已完成。', NULL, 1, NULL, '2026-07-17 20:00:00', NULL, '2026-06-18 09:00:00', '2026-06-18 09:00:00', 0),
    (14006, 11019, 2015, 1011, 1022, '地址导航误差导致迟到。', NULL, 3, NULL, '2026-07-22 20:00:00', NULL, '2026-06-23 10:00:00', '2026-06-23 10:00:00', 0),
    (14007, 11014, 2008, 1013, 1006, '分笼喂食已按要求完成。', NULL, 4, '申诉成立，维持评价。', '2026-06-11 20:00:00', '2026-06-12 10:00:00', '2026-06-11 09:00:00', '2026-06-12 10:00:00', 0),
    (14008, 11021, 2003, 1002, 1003, '宠主临时变更要求。', NULL, 5, NULL, '2026-05-06 20:00:00', '2026-05-12 10:00:00', '2026-05-07 09:00:00', '2026-05-12 10:00:00', 0),
    (14009, 11010, 2014, 1010, 1021, '无申诉，占位记录。', NULL, 5, NULL, NULL, '2026-06-16 09:00:00', '2026-06-16 09:00:00', '2026-06-16 09:00:00', 0),
    (14010, 11011, 2014, 1021, 1010, '无申诉，占位记录。', NULL, 5, NULL, NULL, '2026-06-16 09:00:00', '2026-06-16 09:00:00', '2026-06-16 09:00:00', 0),
    (14011, 11013, 2017, 1024, 1012, '无申诉，占位记录。', NULL, 5, NULL, NULL, '2026-06-19 09:00:00', '2026-06-19 09:00:00', '2026-06-19 09:00:00', 0),
    (14012, 11017, 2019, 1013, 1026, '无申诉，占位记录。', NULL, 5, NULL, NULL, '2026-06-18 09:00:00', '2026-06-18 09:00:00', '2026-06-18 09:00:00', 0),
    (14013, 11022, 2012, 1010, 1019, '无申诉，占位记录。', NULL, 5, NULL, NULL, '2026-06-13 09:00:00', '2026-06-13 09:00:00', '2026-06-13 09:00:00', 0),
    (14014, 11023, 2013, 1015, 1020, '无申诉，占位记录。', NULL, 5, NULL, NULL, '2026-06-13 09:00:00', '2026-06-13 09:00:00', '2026-06-13 09:00:00', 0),
    (14015, 11024, 2016, 1017, 1023, '无申诉，占位记录。', NULL, 5, NULL, NULL, '2026-06-13 09:00:00', '2026-06-13 09:00:00', '2026-06-13 09:00:00', 0),
    (14016, 11025, 2018, 1014, 1025, '无申诉，占位记录。', NULL, 5, NULL, NULL, '2026-06-13 09:00:00', '2026-06-13 09:00:00', '2026-06-13 09:00:00', 0),
    (14017, 11026, 2020, 1015, 1027, '无申诉，占位记录。', NULL, 5, NULL, NULL, '2026-06-13 09:00:00', '2026-06-13 09:00:00', '2026-06-13 09:00:00', 0),
    (14018, 11027, 2004, 1011, 1004, '无申诉，占位记录。', NULL, 5, NULL, NULL, '2026-06-13 09:00:00', '2026-06-13 09:00:00', '2026-06-13 09:00:00', 0),
    (14019, 11002, 2001, 1001, 1002, '无申诉，占位记录。', NULL, 5, NULL, NULL, '2026-05-02 09:00:00', '2026-05-02 09:00:00', '2026-05-02 09:00:00', 0),
    (14020, 11015, 2009, 1012, 1006, '晚间光线不足，已说明。', NULL, 5, NULL, '2026-06-12 20:00:00', '2026-06-13 10:00:00', '2026-06-12 09:00:00', '2026-06-13 10:00:00', 0);

INSERT INTO financial_logs (log_id, user_id, amount, balance_after, trade_type, relation_id, created_at, updated_at, deleted) VALUES
    (12100, 1019, -88.00, 432.00, 21, 2012, '2026-06-12 10:00:00', '2026-06-12 10:00:00', 0),
    (12101, 1020, -76.00, 404.00, 21, 2013, '2026-06-12 10:10:00', '2026-06-12 10:10:00', 0),
    (12102, 1021, -128.00, 482.00, 21, 2014, '2026-06-15 18:00:00', '2026-06-15 18:00:00', 0),
    (12103, 1010, 89.60, 349.60, 11, 2014, '2026-06-15 18:01:00', '2026-06-15 18:01:00', 0),
    (12104, 1022, -98.00, 452.00, 21, 2015, '2026-06-12 10:30:00', '2026-06-12 10:30:00', 0),
    (12105, 1023, -68.00, 362.00, 21, 2016, '2026-06-12 10:40:00', '2026-06-12 10:40:00', 0),
    (12106, 1024, -108.00, 282.00, 21, 2017, '2026-06-18 12:00:00', '2026-06-18 12:00:00', 0),
    (12107, 1012, 75.60, 355.60, 11, 2017, '2026-06-18 12:01:00', '2026-06-18 12:01:00', 0),
    (12108, 1025, -86.00, 384.00, 21, 2018, '2026-06-12 11:00:00', '2026-06-12 11:00:00', 0),
    (12109, 1026, -118.00, 392.00, 21, 2019, '2026-06-17 14:00:00', '2026-06-17 14:00:00', 0),
    (12110, 1013, 82.60, 432.60, 11, 2019, '2026-06-17 14:01:00', '2026-06-17 14:01:00', 0),
    (12111, 1027, -92.00, 628.00, 21, 2020, '2026-06-12 11:20:00', '2026-06-12 11:20:00', 0),
    (12112, 1004, -88.00, 212.00, 21, 2004, '2026-06-10 09:00:00', '2026-06-10 09:00:00', 0),
    (12113, 1005, -98.00, 352.00, 21, 2006, '2026-06-10 11:00:00', '2026-06-10 11:00:00', 0),
    (12114, 1006, -138.00, 462.00, 21, 2008, '2026-06-10 13:00:00', '2026-06-10 13:00:00', 0),
    (12115, 1010, 200.00, 549.60, 13, NULL, '2026-06-10 09:00:00', '2026-06-10 09:00:00', 0),
    (12116, 1011, -50.00, 260.00, 22, 13103, '2026-06-11 09:00:00', '2026-06-11 09:00:00', 0);

INSERT INTO order_settlements (
    settlement_id, order_id, owner_id, provider_id, gross_amount, commission_rate, commission_amount,
    provider_income, settlement_status, settled_at, created_at, updated_at, deleted
) VALUES
    (15002, 2014, 1021, 1010, 128.00, 0.3000, 38.40, 89.60, 2, '2026-06-15 18:01:00', '2026-06-15 18:00:00', '2026-06-15 18:01:00', 0),
    (15003, 2017, 1024, 1012, 108.00, 0.3000, 32.40, 75.60, 2, '2026-06-18 12:01:00', '2026-06-18 12:00:00', '2026-06-18 12:01:00', 0),
    (15004, 2019, 1026, 1013, 118.00, 0.3000, 35.40, 82.60, 1, NULL, '2026-06-17 14:00:00', '2026-06-17 14:00:00', 0),
    (15005, 2015, 1022, 1011, 98.00, 0.3000, 29.40, 68.60, 1, NULL, '2026-06-22 09:00:00', '2026-06-22 09:00:00', 0),
    (15007, 2003, 1003, 1002, 128.00, 0.3000, 38.40, 89.60, 2, '2026-05-05 12:00:00', '2026-05-05 11:30:00', '2026-05-05 12:00:00', 0),
    (15008, 2012, 1019, 1010, 88.00, 0.3000, 26.40, 61.60, 1, NULL, '2026-06-20 09:00:00', '2026-06-20 09:00:00', 0),
    (15009, 2013, 1020, 1015, 76.00, 0.3000, 22.80, 53.20, 1, NULL, '2026-06-21 14:00:00', '2026-06-21 14:00:00', 0),
    (15010, 2016, 1023, 1017, 68.00, 0.3000, 20.40, 47.60, 1, NULL, '2026-06-23 07:00:00', '2026-06-23 07:00:00', 0),
    (15011, 2018, 1025, 1014, 86.00, 0.3000, 25.80, 60.20, 1, NULL, '2026-06-19 16:00:00', '2026-06-19 16:00:00', 0),
    (15012, 2020, 1027, 1015, 92.00, 0.3000, 27.60, 64.40, 1, NULL, '2026-06-24 10:30:00', '2026-06-24 10:30:00', 0),
    (15013, 2004, 1004, 1011, 88.00, 0.3000, 26.40, 61.60, 1, NULL, '2026-06-10 09:00:00', '2026-06-10 09:00:00', 0),
    (15014, 2006, 1005, 1012, 98.00, 0.3000, 29.40, 68.60, 1, NULL, '2026-06-10 12:00:00', '2026-06-10 12:00:00', 0),
    (15015, 2008, 1006, 1013, 138.00, 0.3000, 41.40, 96.60, 1, NULL, '2026-06-10 08:00:00', '2026-06-10 08:00:00', 0),
    (15016, 2009, 1006, 1012, 88.00, 0.3000, 26.40, 61.60, 1, NULL, '2026-06-11 19:00:00', '2026-06-11 19:00:00', 0),
    (15017, 2010, 1001, 1010, 78.00, 0.3000, 23.40, 54.60, 1, NULL, '2026-06-13 09:00:00', '2026-06-13 09:00:00', 0),
    (15018, 2011, 1003, 1015, 75.00, 0.3000, 22.50, 52.50, 1, NULL, '2026-06-14 07:30:00', '2026-06-14 07:30:00', 0),
    (15019, 2007, 1005, 1016, 58.00, 0.3000, 17.40, 40.60, 1, NULL, '2026-06-12 06:30:00', '2026-06-12 06:30:00', 0),
    (15020, 2005, 1004, 1015, 68.00, 0.3000, 20.40, 47.60, 1, NULL, '2026-06-11 07:00:00', '2026-06-11 07:00:00', 0);

INSERT INTO platform_financial_logs (log_id, amount, balance_after, trade_type, relation_id, remark, created_at, updated_at, deleted) VALUES
    (16002, 38.40, 88.80, 101, 2014, 'Order commission 30%', '2026-06-15 18:01:00', '2026-06-15 18:01:00', 0),
    (16003, 32.40, 121.20, 101, 2017, 'Order commission 30%', '2026-06-18 12:01:00', '2026-06-18 12:01:00', 0),
    (16004, 35.40, 156.60, 101, 2019, 'Order commission 30%', '2026-06-17 14:01:00', '2026-06-17 14:01:00', 0),
    (16005, 29.40, 186.00, 101, 2015, 'Order commission 30%', '2026-06-22 09:00:00', '2026-06-22 09:00:00', 0),
    (16006, 26.40, 212.40, 101, 2012, 'Order commission 30%', '2026-06-20 09:00:00', '2026-06-20 09:00:00', 0),
    (16007, 22.80, 235.20, 101, 2013, 'Order commission 30%', '2026-06-21 14:00:00', '2026-06-21 14:00:00', 0),
    (16008, 20.40, 255.60, 101, 2016, 'Order commission 30%', '2026-06-23 07:00:00', '2026-06-23 07:00:00', 0),
    (16009, 25.80, 281.40, 101, 2018, 'Order commission 30%', '2026-06-19 16:00:00', '2026-06-19 16:00:00', 0),
    (16010, 27.60, 309.00, 101, 2020, 'Order commission 30%', '2026-06-24 10:30:00', '2026-06-24 10:30:00', 0),
    (16011, 26.40, 335.40, 101, 2004, 'Order commission 30%', '2026-06-10 09:00:00', '2026-06-10 09:00:00', 0),
    (16012, 29.40, 364.80, 101, 2006, 'Order commission 30%', '2026-06-10 12:00:00', '2026-06-10 12:00:00', 0),
    (16013, 41.40, 406.20, 101, 2008, 'Order commission 30%', '2026-06-10 08:00:00', '2026-06-10 08:00:00', 0),
    (16014, 26.40, 432.60, 101, 2009, 'Order commission 30%', '2026-06-11 19:00:00', '2026-06-11 19:00:00', 0),
    (16015, 23.40, 456.00, 101, 2010, 'Order commission 30%', '2026-06-13 09:00:00', '2026-06-13 09:00:00', 0),
    (16016, 22.50, 478.50, 101, 2011, 'Order commission 30%', '2026-06-14 07:30:00', '2026-06-14 07:30:00', 0),
    (16017, 17.40, 495.90, 101, 2007, 'Order commission 30%', '2026-06-12 06:30:00', '2026-06-12 06:30:00', 0),
    (16018, 20.40, 516.30, 101, 2005, 'Order commission 30%', '2026-06-11 07:00:00', '2026-06-11 07:00:00', 0),
    (16019, 38.40, 554.70, 101, 2003, 'Order commission 30%', '2026-05-05 12:00:00', '2026-05-05 12:00:00', 0);

INSERT INTO withdrawal_records (withdraw_id, user_id, amount, withdrawal_status, account_type, account_info, created_at, updated_at, deleted) VALUES
    (13100, 1010, 80.00, 0, 1, 'alipay:fang@example.com', '2026-06-16 09:00:00', '2026-06-16 09:00:00', 0),
    (13101, 1011, 60.00, 1, 1, 'alipay:mei@example.com', '2026-06-16 10:00:00', '2026-06-16 10:00:00', 0),
    (13102, 1012, 50.00, 2, 2, 'wechat:yu@example.com', '2026-06-16 11:00:00', '2026-06-16 12:00:00', 0),
    (13103, 1013, 100.00, 0, 1, 'alipay:jing@example.com', '2026-06-16 12:00:00', '2026-06-16 12:00:00', 0),
    (13104, 1014, 70.00, 3, 1, 'alipay:hui@example.com', '2026-06-16 13:00:00', '2026-06-16 14:00:00', 0),
    (13105, 1015, 90.00, 2, 3, 'bank:6222****1005', '2026-06-16 14:00:00', '2026-06-16 16:00:00', 0),
    (13106, 1016, 40.00, 0, 1, 'alipay:ming@example.com', '2026-06-16 15:00:00', '2026-06-16 15:00:00', 0),
    (13107, 1017, 55.00, 1, 2, 'wechat:jie@example.com', '2026-06-16 16:00:00', '2026-06-16 16:00:00', 0),
    (13108, 1018, 65.00, 2, 1, 'alipay:wen@example.com', '2026-06-16 17:00:00', '2026-06-16 18:00:00', 0),
    (13109, 1002, 120.00, 0, 1, 'alipay:zhou@example.com', '2026-06-17 09:00:00', '2026-06-17 09:00:00', 0),
    (13110, 1003, 80.00, 2, 2, 'wechat:chen-demo', '2026-06-17 10:00:00', '2026-06-17 12:00:00', 0),
    (13111, 1027, 100.00, 0, 1, 'alipay:hai@example.com', '2026-06-17 11:00:00', '2026-06-17 11:00:00', 0),
    (13112, 1028, 75.00, 1, 1, 'alipay:jiang@example.com', '2026-06-17 12:00:00', '2026-06-17 12:00:00', 0),
    (13113, 1029, 85.00, 2, 2, 'wechat:hu@example.com', '2026-06-17 13:00:00', '2026-06-17 15:00:00', 0),
    (13114, 1010, 30.00, 2, 1, 'alipay:fang2@example.com', '2026-06-18 09:00:00', '2026-06-18 11:00:00', 0),
    (13115, 1011, 45.00, 0, 1, 'alipay:mei2@example.com', '2026-06-18 10:00:00', '2026-06-18 10:00:00', 0),
    (13116, 1012, 35.00, 3, 2, 'wechat:yu2@example.com', '2026-06-18 11:00:00', '2026-06-18 12:00:00', 0),
    (13117, 1013, 50.00, 1, 1, 'alipay:jing2@example.com', '2026-06-18 12:00:00', '2026-06-18 12:00:00', 0);

INSERT INTO credit_records (record_id, provider_id, change_score, score_after, reason_type, relation_id, created_at, updated_at, deleted) VALUES
    (14100, 1010, 4, 96, 1, 2014, '2026-06-15 18:05:00', '2026-06-15 18:05:00', 0),
    (14101, 1011, -2, 86, 2, 2015, '2026-06-22 09:05:00', '2026-06-22 09:05:00', 0),
    (14102, 1012, 3, 88, 1, 2017, '2026-06-18 12:05:00', '2026-06-18 12:05:00', 0),
    (14103, 1013, 4, 100, 1, 2019, '2026-06-17 14:05:00', '2026-06-17 14:05:00', 0),
    (14104, 1014, 2, 92, 1, NULL, '2026-06-12 11:00:00', '2026-06-12 11:00:00', 0),
    (14105, 1015, 1, 83, 1, NULL, '2026-06-12 11:05:00', '2026-06-12 11:05:00', 0),
    (14106, 1016, 0, 84, 1, NULL, '2026-06-12 11:10:00', '2026-06-12 11:10:00', 0),
    (14107, 1017, -1, 78, 3, NULL, '2026-06-12 11:15:00', '2026-06-12 11:15:00', 0),
    (14108, 1018, 2, 89, 1, NULL, '2026-06-12 11:20:00', '2026-06-12 11:20:00', 0),
    (14109, 1002, 4, 88, 1, 2001, '2026-05-01 18:20:00', '2026-05-01 18:20:00', 0),
    (14110, 1003, 0, 80, 1, NULL, '2026-04-20 11:10:00', '2026-04-20 11:10:00', 0),
    (14111, 1027, 3, 94, 1, NULL, '2026-06-12 11:25:00', '2026-06-12 11:25:00', 0),
    (14112, 1028, 1, 84, 1, NULL, '2026-06-12 11:30:00', '2026-06-12 11:30:00', 0),
    (14113, 1029, 2, 91, 1, NULL, '2026-06-12 11:35:00', '2026-06-12 11:35:00', 0),
    (14114, 1010, -3, 93, 4, NULL, '2026-06-20 09:00:00', '2026-06-20 09:00:00', 0),
    (14115, 1012, 2, 90, 7, 2017, '2026-06-19 10:00:00', '2026-06-19 10:00:00', 0),
    (14116, 1011, 1, 87, 7, 2015, '2026-06-23 10:00:00', '2026-06-23 10:00:00', 0),
    (14117, 1002, -2, 86, 5, 2003, '2026-05-05 12:00:00', '2026-05-05 12:00:00', 0);

INSERT INTO training_materials (material_id, title, content, created_at, updated_at, deleted) VALUES
    (900002, '猫咪行为观察指南', '介绍猫咪常见行为含义及异常识别要点。', '2026-06-01 09:00:00', '2026-06-01 09:00:00', 0),
    (900003, '犬类牵引安全规范', '讲解牵引绳使用、遛狗路线选择与突发情况处理。', '2026-06-01 09:05:00', '2026-06-01 09:05:00', 0),
    (900004, '入户服务礼仪', '规范敲门、换鞋、消毒和个人卫生要求。', '2026-06-01 09:10:00', '2026-06-01 09:10:00', 0),
    (900005, '打卡凭证拍摄规范', '说明照片角度、光线和必拍场景。', '2026-06-01 09:15:00', '2026-06-01 09:15:00', 0),
    (900006, '老年宠物护理', '针对老年猫狗的喂食、活动和观察重点。', '2026-06-01 09:20:00', '2026-06-01 09:20:00', 0),
    (900007, '幼宠护理基础', '幼猫幼犬喂食频次、保暖与社交注意事项。', '2026-06-01 09:25:00', '2026-06-01 09:25:00', 0),
    (900008, '多宠家庭服务要点', '分笼分食、隔离与顺序服务流程。', '2026-06-01 09:30:00', '2026-06-01 09:30:00', 0),
    (900009, '处方粮服务规范', '严格按宠主要求喂食，禁止擅自替换。', '2026-06-01 09:35:00', '2026-06-01 09:35:00', 0),
    (900010, '异常上报流程', '平台异常上报入口、描述规范和凭证要求。', '2026-06-01 09:40:00', '2026-06-01 09:40:00', 0),
    (900011, '沟通与预期管理', '如何与宠主确认服务边界和临时变更。', '2026-06-01 09:45:00', '2026-06-01 09:45:00', 0),
    (900012, '隐私保护与信息安全', '禁止泄露地址、密码和无关家庭信息。', '2026-06-01 09:50:00', '2026-06-01 09:50:00', 0),
    (900013, '恶劣天气服务指引', '雨雪高温情况下的到达与服务调整。', '2026-06-01 09:55:00', '2026-06-01 09:55:00', 0),
    (900014, '门禁与钥匙管理', '临时钥匙、密码锁和智能门禁注意事项。', '2026-06-01 10:00:00', '2026-06-01 10:00:00', 0),
    (900015, '医疗相关边界', '哪些情况必须联系宠主或紧急联系人。', '2026-06-01 10:05:00', '2026-06-01 10:05:00', 0),
    (900016, '服务后离场检查', '门窗、水电、宠物状态和垃圾处理的最后确认。', '2026-06-01 10:10:00', '2026-06-01 10:10:00', 0),
    (900017, '差评预防与复盘', '从常见投诉场景总结改进方法。', '2026-06-01 10:15:00', '2026-06-01 10:15:00', 0),
    (900018, '保证金与平台规则', '说明保证金用途、退还和违规处理。', '2026-06-01 10:20:00', '2026-06-01 10:20:00', 0),
    (900019, '女性服务者安全指引', '夜间服务、位置共享和个人安全防护建议。', '2026-06-01 10:25:00', '2026-06-01 10:25:00', 0),
    (900020, '新手上路常见问题', '首次接单、首次入户和新设备使用FAQ。', '2026-06-01 10:30:00', '2026-06-01 10:30:00', 0);

INSERT INTO sitter_training_records (
    provider_id, learning_completed_at, last_exam_score, last_exam_passed, last_exam_at, reset_reason, created_at, updated_at, deleted
) VALUES
    (1002, '2026-04-21 10:00:00', 95, 1, '2026-04-21 11:00:00', NULL, '2026-04-21 09:00:00', '2026-04-21 11:00:00', 0),
    (1003, '2026-04-21 10:30:00', 92, 1, '2026-04-21 11:30:00', NULL, '2026-04-21 09:30:00', '2026-04-21 11:30:00', 0),
    (1010, '2026-06-11 09:00:00', 96, 1, '2026-06-11 10:00:00', NULL, '2026-06-11 08:00:00', '2026-06-11 10:00:00', 0),
    (1011, '2026-06-11 09:10:00', 90, 1, '2026-06-11 10:10:00', NULL, '2026-06-11 08:10:00', '2026-06-11 10:10:00', 0),
    (1012, '2026-06-11 09:20:00', 88, 1, '2026-06-11 10:20:00', NULL, '2026-06-11 08:20:00', '2026-06-11 10:20:00', 0),
    (1013, '2026-06-11 09:30:00', 98, 1, '2026-06-11 10:30:00', NULL, '2026-06-11 08:30:00', '2026-06-11 10:30:00', 0),
    (1014, '2026-06-11 09:40:00', 91, 1, '2026-06-11 10:40:00', NULL, '2026-06-11 08:40:00', '2026-06-11 10:40:00', 0),
    (1015, '2026-06-11 09:50:00', 87, 1, '2026-06-11 10:50:00', NULL, '2026-06-11 08:50:00', '2026-06-11 10:50:00', 0),
    (1016, '2026-06-11 10:00:00', 93, 1, '2026-06-11 11:00:00', NULL, '2026-06-11 09:00:00', '2026-06-11 11:00:00', 0),
    (1017, '2026-06-11 10:10:00', 85, 0, '2026-06-11 11:10:00', NULL, '2026-06-11 09:10:00', '2026-06-11 11:10:00', 0),
    (1018, '2026-06-11 10:20:00', 94, 1, '2026-06-11 11:20:00', NULL, '2026-06-11 09:20:00', '2026-06-11 11:20:00', 0),
    (1027, '2026-06-11 10:30:00', 92, 1, '2026-06-11 11:30:00', NULL, '2026-06-11 09:30:00', '2026-06-11 11:30:00', 0),
    (1028, '2026-06-11 10:40:00', 89, 1, '2026-06-11 11:40:00', NULL, '2026-06-11 09:40:00', '2026-06-11 11:40:00', 0),
    (1029, '2026-06-11 10:50:00', 97, 1, '2026-06-11 11:50:00', NULL, '2026-06-11 09:50:00', '2026-06-11 11:50:00', 0);

INSERT INTO sms_verification_codes (id, phone, code, used, expires_at, created_at) VALUES
    (1, '13800000001', '123456', 1, '2026-06-01 10:05:00', '2026-06-01 10:00:00'),
    (2, '13800000002', '234567', 1, '2026-06-01 10:10:00', '2026-06-01 10:05:00'),
    (3, '13800000003', '345678', 0, '2026-06-01 10:15:00', '2026-06-01 10:10:00'),
    (4, '13800000004', '456789', 1, '2026-06-02 09:05:00', '2026-06-02 09:00:00'),
    (5, '13800000005', '567890', 1, '2026-06-02 09:10:00', '2026-06-02 09:05:00'),
    (6, '13800000006', '678901', 0, '2026-06-02 09:15:00', '2026-06-02 09:10:00'),
    (7, '13800001010', '789012', 1, '2026-06-10 09:05:00', '2026-06-10 09:00:00'),
    (8, '13800001011', '890123', 1, '2026-06-10 09:10:00', '2026-06-10 09:05:00'),
    (9, '13800001012', '901234', 0, '2026-06-10 09:15:00', '2026-06-10 09:10:00'),
    (10, '13800001019', '112233', 1, '2026-06-10 10:05:00', '2026-06-10 10:00:00'),
    (11, '13800001020', '223344', 1, '2026-06-10 10:10:00', '2026-06-10 10:05:00'),
    (12, '13800001021', '334455', 0, '2026-06-10 10:15:00', '2026-06-10 10:10:00'),
    (13, '13800001022', '445566', 1, '2026-06-10 10:20:00', '2026-06-10 10:15:00'),
    (14, '13800001023', '556677', 0, '2026-06-10 10:25:00', '2026-06-10 10:20:00'),
    (15, '13800001024', '667788', 1, '2026-06-10 10:30:00', '2026-06-10 10:25:00'),
    (16, '13800001025', '778899', 0, '2026-06-10 10:35:00', '2026-06-10 10:30:00'),
    (17, '13800001026', '889900', 1, '2026-06-10 10:40:00', '2026-06-10 10:35:00'),
    (18, '13800001027', '990011', 0, '2026-06-10 10:45:00', '2026-06-10 10:40:00'),
    (19, '13800001028', '101112', 1, '2026-06-10 10:50:00', '2026-06-10 10:45:00'),
    (20, '13800001029', '121314', 0, '2026-06-10 10:55:00', '2026-06-10 10:50:00');
