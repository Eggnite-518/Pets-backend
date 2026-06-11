-- 补齐缺失的订单结算/托管表（部分环境 Flyway 历史与表结构不一致）
CREATE TABLE IF NOT EXISTS order_settlements (
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

CREATE TABLE IF NOT EXISTS platform_financial_logs (
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

-- 为已支付且已指派宠托师、但缺少托管记录的订单补建 escrow
INSERT INTO order_settlements (
    settlement_id, order_id, owner_id, provider_id,
    gross_amount, commission_rate, commission_amount, provider_income,
    settlement_status, created_at, updated_at, deleted
)
SELECT
    o.order_id * 100 + 50,
    o.order_id,
    o.owner_id,
    o.provider_id,
    o.total_amount,
    0.3000,
    ROUND(o.total_amount * 0.30, 2),
    ROUND(o.total_amount * 0.70, 2),
    1,
    NOW(),
    NOW(),
    0
FROM orders o
WHERE o.provider_id IS NOT NULL
  AND o.order_status IN (3, 4, 5)
  AND o.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM order_settlements s WHERE s.order_id = o.order_id AND s.deleted = 0
  );
