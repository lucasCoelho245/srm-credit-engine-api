CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE currencies (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code       VARCHAR(3)  NOT NULL UNIQUE,
    name       VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE exchange_rates (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_currency_id UUID          NOT NULL REFERENCES currencies(id),
    to_currency_id   UUID          NOT NULL REFERENCES currencies(id),
    rate             DECIMAL(19,6) NOT NULL CHECK (rate > 0),
    effective_date   DATE          NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_rate_pair_date UNIQUE (from_currency_id, to_currency_id, effective_date)
);

CREATE TABLE product_types (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(100) NOT NULL UNIQUE,
    spread_monthly DECIMAL(5,4) NOT NULL CHECK (spread_monthly >= 0),
    description    TEXT
);

CREATE TABLE transactions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cedente             VARCHAR(200)  NOT NULL,
    face_value          DECIMAL(19,4) NOT NULL CHECK (face_value > 0),
    present_value       DECIMAL(19,4) NOT NULL,
    discount            DECIMAL(19,4) NOT NULL,
    base_rate           DECIMAL(5,4)  NOT NULL,
    term_months         INTEGER       NOT NULL CHECK (term_months > 0),
    product_type_id     UUID          NOT NULL REFERENCES product_types(id),
    title_currency_id   UUID          NOT NULL REFERENCES currencies(id),
    payment_currency_id UUID          NOT NULL REFERENCES currencies(id),
    exchange_rate_used  DECIMAL(19,6),
    due_date            DATE          NOT NULL,
    liquidated_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tx_cedente    ON transactions(cedente);
CREATE INDEX idx_tx_created    ON transactions(created_at DESC);
CREATE INDEX idx_tx_currency   ON transactions(payment_currency_id);
CREATE INDEX idx_tx_product    ON transactions(product_type_id);
CREATE INDEX idx_tx_liquidated ON transactions(liquidated_at) WHERE liquidated_at IS NOT NULL;
