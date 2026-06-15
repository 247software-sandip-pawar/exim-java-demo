-- Phase 1: identity module (companies + users)

CREATE TABLE companies (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(40)  NOT NULL,
    country     VARCHAR(80),
    iec_code    VARCHAR(40),
    gstin       VARCHAR(40),
    verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id            UUID PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone         VARCHAR(40),
    role          VARCHAR(40)  NOT NULL,
    company_id    UUID REFERENCES companies(id),
    version       BIGINT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_company ON users(company_id);
