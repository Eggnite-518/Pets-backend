CREATE TABLE users (
    user_id BIGINT NOT NULL COMMENT '用户唯一标识',
    phone VARCHAR(20) NOT NULL COMMENT '手机号,登录凭证,需加密存储',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    password_hash VARCHAR(255) NULL COMMENT '密码哈希值，验证码注册用户可为空',
    avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像链接',
    role_type TINYINT NOT NULL DEFAULT 1 COMMENT '角色状态:1=仅宠主,2=仅喂养员,3=双重身份',
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户可用余额',
    frozen_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0=未删除,1=已删除',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名，对应实名核验',
    id_card_no VARCHAR(18) DEFAULT NULL COMMENT '身份证号,需加密存储',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE sitter_profiles (
    provider_id BIGINT NOT NULL COMMENT '喂养员ID,1:1扩展基础用户表',
    gender TINYINT DEFAULT NULL COMMENT '性别:1=男,2=女',
    verify_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态:0=未认证,1=审核中,2=已通过',
    deposit_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '已缴保证金',
    credit_score INT NOT NULL DEFAULT 80 COMMENT '信用分，初始80，最高100',
    service_radius_km INT NOT NULL DEFAULT 5 COMMENT '服务半径(千米)',
    resident_address VARCHAR(255) DEFAULT NULL COMMENT '常驻地址',
    resident_latitude DECIMAL(10,7) DEFAULT NULL COMMENT '常驻地址纬度',
    resident_longitude DECIMAL(10,7) DEFAULT NULL COMMENT '常驻地址经度',
    cert_labels_json JSON DEFAULT NULL COMMENT '特色标签JSON数组',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0=未删除,1=已删除',
    PRIMARY KEY (provider_id),
    CONSTRAINT fk_sitter_profiles_provider
        FOREIGN KEY (provider_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠托师档案表';

CREATE TABLE pet_owners (
    owner_id BIGINT NOT NULL COMMENT '宠主ID,1:1扩展基础用户表',
    emergency_contact VARCHAR(20) DEFAULT NULL COMMENT '紧急联系人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0=未删除,1=已删除',
    PRIMARY KEY (owner_id),
    CONSTRAINT fk_pet_owners_owner
        FOREIGN KEY (owner_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠主扩展表';

CREATE TABLE user_addresses (
    address_id BIGINT NOT NULL COMMENT '用户地址ID',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    contact_name VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系人手机号',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) NOT NULL COMMENT '区县',
    detail_address VARCHAR(255) NOT NULL COMMENT '详细地址',
    address_tag VARCHAR(30) DEFAULT NULL COMMENT '地址标签',
    is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认地址：0=否，1=是',
    latitude DECIMAL(10,7) DEFAULT NULL COMMENT '地址纬度',
    longitude DECIMAL(10,7) DEFAULT NULL COMMENT '地址经度',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (address_id),
    CONSTRAINT fk_user_addresses_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户地址表';

CREATE TABLE question_bank (
    question_id BIGINT NOT NULL COMMENT '题目唯一标识',
    question_type TINYINT NOT NULL COMMENT '题目类型：1=基础题，2=核心安全题',
    content VARCHAR(500) NOT NULL COMMENT '题目题干',
    options_json JSON NOT NULL COMMENT '选项集合',
    correct_answer VARCHAR(10) NOT NULL COMMENT '正确答案',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库表';

CREATE TABLE order_address_snapshots (
    snapshot_id BIGINT NOT NULL COMMENT '订单地址快照ID',
    source_address_id BIGINT DEFAULT NULL COMMENT '来源用户地址ID，临时地址允许为空',
    contact_name VARCHAR(50) NOT NULL COMMENT '联系人姓名快照',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系人手机号快照',
    province VARCHAR(50) NOT NULL COMMENT '省份快照',
    city VARCHAR(50) NOT NULL COMMENT '城市快照',
    district VARCHAR(50) NOT NULL COMMENT '区县快照',
    detail_address VARCHAR(255) NOT NULL COMMENT '详细地址快照',
    address_tag VARCHAR(30) DEFAULT NULL COMMENT '地址标签快照',
    latitude DOUBLE DEFAULT NULL COMMENT '地址纬度',
    longitude DOUBLE DEFAULT NULL COMMENT '地址经度',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (snapshot_id),
    CONSTRAINT fk_order_address_snapshots_source_address
        FOREIGN KEY (source_address_id) REFERENCES user_addresses (address_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单地址快照表';

CREATE TABLE orders (
    order_id BIGINT NOT NULL COMMENT '订单号',
    owner_id BIGINT NOT NULL COMMENT '发布者宠主ID',
    provider_id BIGINT DEFAULT NULL COMMENT '接单者喂养员ID，悬赏中允许为空',
    address_snapshot_id BIGINT NOT NULL COMMENT '订单地址快照ID',
    order_status TINYINT NOT NULL COMMENT '订单状态：1=悬赏中，2=待支付，3=待履约，4=履约中，5=待宠主确认，6=已完成',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总价',
    service_date DATE NOT NULL COMMENT '预约上门日期',
    service_start_time TIME NOT NULL COMMENT '预约上门开始时间',
    service_end_time TIME NOT NULL COMMENT '预约上门结束时间',
    service_type TINYINT NOT NULL DEFAULT 1 COMMENT '服务类型：1=代喂，2=代遛',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (order_id),
    CONSTRAINT fk_orders_owner
        FOREIGN KEY (owner_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_orders_provider
        FOREIGN KEY (provider_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_orders_address_snapshot
        FOREIGN KEY (address_snapshot_id) REFERENCES order_address_snapshots (snapshot_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单/悬赏主表';

CREATE TABLE order_applications (
    apply_id BIGINT NOT NULL COMMENT '报名流水号',
    order_id BIGINT NOT NULL COMMENT '关联订单号',
    provider_id BIGINT NOT NULL COMMENT '报名的喂养员ID',
    apply_status TINYINT NOT NULL DEFAULT 0 COMMENT '报名状态：0=报名中，1=落选，2=被选中',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (apply_id),
    CONSTRAINT fk_order_applications_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_order_applications_provider
        FOREIGN KEY (provider_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接单意向表';

CREATE TABLE exception_reports (
    report_id BIGINT NOT NULL COMMENT '异常上报流水号',
    order_id BIGINT NOT NULL COMMENT '关联订单号',
    reporter_id BIGINT NOT NULL COMMENT '上报人ID',
    exception_type TINYINT NOT NULL COMMENT '异常类型：1=门禁异常，2=宠物异常，3=物资异常，4=设施异常，5=其他',
    description TEXT NOT NULL COMMENT '异常情况描述',
    proof_images VARCHAR(500) DEFAULT NULL COMMENT '现场凭证图片',
    report_status TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0=待处理，1=宠主已确认/处理中，2=已解决',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    resolved_at DATETIME DEFAULT NULL COMMENT '解决时间',
    PRIMARY KEY (report_id),
    CONSTRAINT fk_exception_reports_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_exception_reports_reporter
        FOREIGN KEY (reporter_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异常上报表';

CREATE TABLE arbitration_records (
    arbitration_id BIGINT NOT NULL COMMENT '仲裁案卷号',
    order_id BIGINT NOT NULL COMMENT '关联订单号',
    plaintiff_id BIGINT NOT NULL COMMENT '发起方/原告ID',
    defendant_id BIGINT NOT NULL COMMENT '被诉方/被告ID',
    arb_type TINYINT NOT NULL COMMENT '仲裁类型：1=服务质量，2=宠物伤害，3=财产损失，4=评价申诉，5=退款纠纷',
    reason TEXT NOT NULL COMMENT '诉求与理由',
    evidence_urls VARCHAR(500) DEFAULT NULL COMMENT '证据文件',
    arbitration_status TINYINT NOT NULL DEFAULT 0 COMMENT '仲裁状态：0=待介入，1=客服取证中，2=客服判定中，3=已完结',
    result_type TINYINT DEFAULT NULL COMMENT '判决结果：1=原告胜诉，2=被告胜诉，3=双方和解',
    admin_memo TEXT COMMENT '平台处理备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请仲裁时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    closed_at DATETIME DEFAULT NULL COMMENT '结案时间',
    PRIMARY KEY (arbitration_id),
    CONSTRAINT fk_arbitration_records_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_arbitration_records_plaintiff
        FOREIGN KEY (plaintiff_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_arbitration_records_defendant
        FOREIGN KEY (defendant_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仲裁记录表';

CREATE TABLE pet_archives (
    pet_id BIGINT NOT NULL COMMENT '宠物档案ID',
    owner_id BIGINT NOT NULL COMMENT '所属宠主ID',
    pet_name VARCHAR(50) NOT NULL COMMENT '宠物昵称',
    pet_type TINYINT NOT NULL COMMENT '宠物种类：1=猫，2=狗，3=其他',
    default_req TEXT COMMENT '默认喂食要求',
    image VARCHAR(255) DEFAULT NULL COMMENT '宠物照片链接',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (pet_id),
    CONSTRAINT fk_pet_archives_owner
        FOREIGN KEY (owner_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物档案表';

CREATE TABLE order_pets_snapshot (
    snapshot_id BIGINT NOT NULL COMMENT '快照流水号',
    order_id BIGINT NOT NULL COMMENT '关联订单号',
    archive_pet_id BIGINT DEFAULT NULL COMMENT '关联的宠物档案ID，临时手填时允许为空',
    snap_pet_name VARCHAR(50) NOT NULL COMMENT '宠物昵称快照',
    snap_pet_type TINYINT NOT NULL COMMENT '宠物种类快照：1=猫，2=狗，3=其他',
    snap_req TEXT COMMENT '本次实际要求',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (snapshot_id),
    CONSTRAINT fk_order_pets_snapshot_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_order_pets_snapshot_archive_pet
        FOREIGN KEY (archive_pet_id) REFERENCES pet_archives (pet_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单宠物快照表';

CREATE TABLE chats (
    message_id BIGINT NOT NULL COMMENT '消息ID',
    order_id BIGINT NOT NULL COMMENT '关联订单号',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT NOT NULL COMMENT '聊天内容',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读:0=未读,1=已读',
    sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (message_id),
    CONSTRAINT fk_chats_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_chats_sender
        FOREIGN KEY (sender_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_chats_receiver
        FOREIGN KEY (receiver_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天记录表';

CREATE TABLE fulfillment_records (
    record_id BIGINT NOT NULL COMMENT '打卡流水号',
    order_id BIGINT NOT NULL COMMENT '关联订单号',
    node_type TINYINT NOT NULL COMMENT '打卡节点：1=抵达签到，2=入户确认，3=喂食换水，4=铲屎清洁，5=遛宠中，6=锁门离场',
    latitude DOUBLE DEFAULT NULL COMMENT '打卡纬度',
    longitude DOUBLE DEFAULT NULL COMMENT '打卡经度',
    image_url VARCHAR(255) DEFAULT NULL COMMENT '现场媒体地址；OSS 私有读时仅作兼容展示，长期标识由后续迁移的 object_key 保存',
    media_type VARCHAR(20) DEFAULT NULL COMMENT '媒体类型：VIDEO/IMAGE',
    object_key VARCHAR(255) DEFAULT NULL COMMENT 'OSS对象Key',
    file_size BIGINT DEFAULT NULL COMMENT '文件大小，单位字节',
    content_type VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型',
    frame_rate INT DEFAULT NULL COMMENT '视频帧率',
    processing_status VARCHAR(20) DEFAULT NULL COMMENT '视频处理状态：PROCESSING/SUCCESS/FAILED',
    processing_error_code VARCHAR(32) DEFAULT NULL COMMENT '视频处理错误码',
    processing_error VARCHAR(500) DEFAULT NULL COMMENT '视频处理失败原因',
    original_object_key VARCHAR(255) DEFAULT NULL COMMENT '原始视频OSS对象Key',
    original_content_type VARCHAR(100) DEFAULT NULL COMMENT '原始视频MIME类型',
    original_file_size BIGINT DEFAULT NULL COMMENT '原始视频文件大小，单位字节',
    watermark_text VARCHAR(100) DEFAULT NULL COMMENT '视频水印文案',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (record_id),
    CONSTRAINT fk_fulfillment_records_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='履约打卡表';

CREATE TABLE reviews (
    review_id BIGINT NOT NULL COMMENT '评价ID',
    order_id BIGINT NOT NULL COMMENT '关联订单号',
    reviewer_id BIGINT NOT NULL COMMENT '评价人ID',
    target_id BIGINT NOT NULL COMMENT '被评价人ID',
    score TINYINT NOT NULL COMMENT '兼容字段：综合星级，1-5星',
    overall_score TINYINT NOT NULL COMMENT '综合评分：1-5星',
    punctuality_score TINYINT NOT NULL COMMENT '准时度评分：1-5星',
    professional_score TINYINT NOT NULL COMMENT '专业度评分：1-5星',
    is_low_score TINYINT NOT NULL DEFAULT 0 COMMENT '是否低分评价：0=否，1=是',
    review_status TINYINT NOT NULL DEFAULT 1 COMMENT '评价状态：1=正常，2=申诉中，3=申诉成立，4=申诉驳回，5=已隐藏，6=已修正',
    content TEXT COMMENT '评价内容',
    review_type TINYINT NOT NULL COMMENT '评价类型：1=宠主评喂养员，2=喂养员评宠主/宠物',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (review_id),
    CONSTRAINT fk_reviews_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_reviews_reviewer
        FOREIGN KEY (reviewer_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_reviews_target
        FOREIGN KEY (target_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

CREATE TABLE review_deduction_reasons (
    reason_id BIGINT NOT NULL COMMENT '评价扣分理由ID',
    review_id BIGINT NOT NULL COMMENT '关联评价ID',
    reason_type TINYINT NOT NULL COMMENT '扣分理由：1=未按时到达，2=未按要求喂食，3=宠物异常未及时反馈，4=打卡记录缺失，5=服务态度差，6=其他',
    reason_text VARCHAR(255) DEFAULT NULL COMMENT '其他或补充说明',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (reason_id),
    UNIQUE KEY uk_review_deduction_reasons_review_type (review_id, reason_type),
    CONSTRAINT fk_review_deduction_reasons_review
        FOREIGN KEY (review_id) REFERENCES reviews (review_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价扣分理由表';

CREATE TABLE review_attachments (
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

CREATE TABLE review_appeals (
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

CREATE TABLE financial_logs (
    log_id BIGINT NOT NULL COMMENT '流水号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '变动金额，正数表示增加，负数表示减少',
    balance_after DECIMAL(10,2) NOT NULL COMMENT '变动后余额',
    trade_type TINYINT NOT NULL COMMENT '交易类型',
    relation_id BIGINT DEFAULT NULL COMMENT '关联单号，对应订单ID或提现单ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (log_id),
    CONSTRAINT fk_financial_logs_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资金流水表';

CREATE TABLE order_settlements (
    settlement_id BIGINT NOT NULL COMMENT '结算/托管记录ID',
    order_id BIGINT NOT NULL COMMENT '关联订单号',
    owner_id BIGINT NOT NULL COMMENT '宠主ID',
    provider_id BIGINT NOT NULL COMMENT '服务者ID',
    gross_amount DECIMAL(10,2) NOT NULL COMMENT '订单托管总金额',
    commission_rate DECIMAL(5,4) NOT NULL COMMENT '平台佣金比例',
    commission_amount DECIMAL(10,2) NOT NULL COMMENT '平台佣金金额',
    provider_income DECIMAL(10,2) NOT NULL COMMENT '服务者实收金额',
    settlement_status TINYINT NOT NULL DEFAULT 1 COMMENT '结算状态：1=托管中，2=已结算，3=结算失败',
    settled_at DATETIME DEFAULT NULL COMMENT '结算完成时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (settlement_id),
    UNIQUE KEY uk_order_settlements_order (order_id),
    CONSTRAINT fk_order_settlements_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_order_settlements_owner
        FOREIGN KEY (owner_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_order_settlements_provider
        FOREIGN KEY (provider_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单结算/托管记录表';

CREATE TABLE platform_financial_logs (
    log_id BIGINT NOT NULL COMMENT '平台流水号',
    amount DECIMAL(10,2) NOT NULL COMMENT '变动金额，正数表示平台收入',
    balance_after DECIMAL(10,2) NOT NULL COMMENT '变动后平台账面余额',
    trade_type TINYINT NOT NULL COMMENT '交易类型：101=订单佣金收入',
    relation_id BIGINT DEFAULT NULL COMMENT '关联订单ID',
    remark VARCHAR(255) DEFAULT NULL COMMENT '流水备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (log_id),
    CONSTRAINT fk_platform_financial_logs_order
        FOREIGN KEY (relation_id) REFERENCES orders (order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台资金流水表';

CREATE TABLE withdrawal_records (
    withdraw_id BIGINT NOT NULL COMMENT '提现单号',
    user_id BIGINT NOT NULL COMMENT '申请用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '提现金额',
    withdrawal_status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=待审核，1=审核通过待打款，2=成功，3=驳回',
    account_type TINYINT NOT NULL COMMENT '收款账户类型：1=支付宝，2=微信，3=银行卡',
    account_info VARCHAR(255) NOT NULL COMMENT '收款账号信息，需加密存储',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (withdraw_id),
    CONSTRAINT fk_withdrawal_records_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提现记录表';

CREATE TABLE credit_records (
    record_id BIGINT NOT NULL COMMENT '流水号',
    provider_id BIGINT NOT NULL COMMENT '喂养员ID',
    change_score INT NOT NULL COMMENT '变动分数',
    score_after INT NOT NULL COMMENT '变动后的当前总信用分快照',
    reason_type TINYINT NOT NULL COMMENT '变动原因：1=五星好评，2=未能按时履约，3=服务质量低，4=服务态度差，5=未能按时上传打卡，6=严重事故，7=评价申诉修正',
    relation_id BIGINT DEFAULT NULL COMMENT '关联订单ID，允许为空',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0=未删除，1=已删除',
    PRIMARY KEY (record_id),
    CONSTRAINT fk_credit_records_provider
        FOREIGN KEY (provider_id) REFERENCES users (user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_credit_records_relation_order
        FOREIGN KEY (relation_id) REFERENCES orders (order_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信用流水表';
