-- ============================================================================
-- EXIM Marketplace — per-module database bootstrap
-- ============================================================================
-- Architecture: one Spring Boot app, but EACH MODULE owns its own PostgreSQL
-- database. Flyway then manages the schema inside each database from its own
-- migration folder (db/migration/<module>).
--
-- Run this ONCE against your local PostgreSQL before starting the app.
--
--   pgAdmin:  open a Query Tool on the "postgres" maintenance database and run
--             the CREATE DATABASE statement(s) below. (pgAdmin runs each in its
--             own autocommit, which CREATE DATABASE requires.)
--
--   psql:     psql -U postgres -h localhost -f create-databases.sql
--
-- Note: PostgreSQL does not support "CREATE DATABASE IF NOT EXISTS". If a
-- database already exists you'll get a harmless error — skip that line.
-- ============================================================================

-- Phase 1 — identity module (companies, users, auth)
CREATE DATABASE exim_identity;

-- Phase 2 — verification module (KYC documents)
CREATE DATABASE exim_verification;

-- ---------------------------------------------------------------------------
-- Future modules (uncomment as each phase is built — one DB per module):
-- ---------------------------------------------------------------------------
-- CREATE DATABASE exim_catalog;        -- Phase 3: products, HS codes
-- CREATE DATABASE exim_sourcing;       -- Phase 3: RFQs
-- CREATE DATABASE exim_quotation;      -- Phase 4
-- CREATE DATABASE exim_messaging;      -- Phase 4
-- CREATE DATABASE exim_orders;         -- Phase 4
-- CREATE DATABASE exim_documents;      -- Phase 4
-- CREATE DATABASE exim_logistics;      -- Phase 5
-- CREATE DATABASE exim_payments;       -- Phase 5
-- CREATE DATABASE exim_billing;        -- Phase 6
-- CREATE DATABASE exim_trust;          -- Phase 6
-- CREATE DATABASE exim_notification;   -- Phase 6
-- CREATE DATABASE exim_admin;          -- Phase 6
