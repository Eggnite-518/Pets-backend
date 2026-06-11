-- 部分环境 Flyway 历史与表结构不一致，补齐评价扣分理由表
CREATE TABLE IF NOT EXISTS review_deduction_reasons (
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

INSERT IGNORE INTO review_deduction_reasons (
    reason_id, review_id, reason_type, reason_text, created_at, updated_at, deleted
) VALUES
    (12004, 11001, 6, NULL, '2026-05-01 20:00:00', '2026-05-01 20:00:00', 0),
    (12018, 11002, 6, NULL, '2026-05-01 20:10:00', '2026-05-01 20:10:00', 0);
