-- Phase 3: catalog module (HS codes + products), own database exim_catalog.
-- company_id references a company owned by the identity service (validated over REST).

CREATE TABLE hs_codes (
    id          UUID PRIMARY KEY,
    code        VARCHAR(20)  NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE products (
    id            UUID PRIMARY KEY,
    company_id    UUID          NOT NULL,
    name          VARCHAR(255)  NOT NULL,
    description   VARCHAR(2000),
    hs_code       VARCHAR(20)   NOT NULL,
    unit_price    NUMERIC(18,2) NOT NULL,
    currency      VARCHAR(3)    NOT NULL,
    unit          VARCHAR(20),
    min_order_qty INTEGER       NOT NULL DEFAULT 1,
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    version       BIGINT        NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_hs_code ON products(hs_code);
CREATE INDEX idx_products_company ON products(company_id);

-- Seed a small set of HS codes so products and RFQ matching have a shared vocabulary.
INSERT INTO hs_codes (id, code, description) VALUES
    (gen_random_uuid(), '0901.21', 'Coffee, roasted, not decaffeinated'),
    (gen_random_uuid(), '5208.52', 'Woven cotton fabrics, printed, plain weave'),
    (gen_random_uuid(), '7308.90', 'Structures and parts of structures, of iron or steel'),
    (gen_random_uuid(), '8471.30', 'Portable digital automatic data processing machines'),
    (gen_random_uuid(), '1006.30', 'Semi-milled or wholly milled rice');
