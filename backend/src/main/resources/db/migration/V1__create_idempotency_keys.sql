CREATE TABLE idempotency_keys (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    order_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_idempotency_user_key
        UNIQUE (user_id, idempotency_key),

    CONSTRAINT fk_idempotency_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_idempotency_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
);