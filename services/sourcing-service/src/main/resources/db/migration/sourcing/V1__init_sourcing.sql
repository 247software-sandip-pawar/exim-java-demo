-- Phase 3: sourcing module (RFQs), own database exim_sourcing.
-- buyer_company_id references a company owned by the identity service (validated over REST).

CREATE TABLE rfqs (
    id                UUID PRIMARY KEY,
    buyer_company_id  UUID          NOT NULL,
    title             VARCHAR(255)  NOT NULL,
    description       VARCHAR(2000),
    hs_code           VARCHAR(20)   NOT NULL,
    quantity          INTEGER       NOT NULL,
    unit              VARCHAR(20),
    target_price      NUMERIC(18,2),
    currency          VARCHAR(3),
    status            VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
    version           BIGINT        NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_rfqs_hs_code ON rfqs(hs_code);
CREATE INDEX idx_rfqs_buyer ON rfqs(buyer_company_id);
