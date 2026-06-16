-- Phase 2: verification module (company KYC documents)
-- Lives in its OWN database (exim_verification). company_id references a company
-- owned by the identity module's database, so it is a plain UUID here with NO
-- cross-database foreign key (modules own their data; the link is by id only).

CREATE TABLE verifications (
    id            UUID PRIMARY KEY,
    company_id    UUID         NOT NULL,
    type          VARCHAR(40)  NOT NULL,   -- IEC | GST | RCMC | BANK
    file_url      VARCHAR(1024) NOT NULL,
    status        VARCHAR(40)  NOT NULL DEFAULT 'PENDING',  -- PENDING | APPROVED | REJECTED
    reviewer_note VARCHAR(1024),
    version       BIGINT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_verifications_company ON verifications(company_id);
