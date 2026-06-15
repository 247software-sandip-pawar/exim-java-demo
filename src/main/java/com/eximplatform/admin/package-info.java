/**
 * Admin module - moderation, KYC approval queue, sanctions screening, analytics.
 *
 * Planned entities:   (operational)
 * Planned endpoints:  GET /api/v1/admin/verifications, POST /api/v1/admin/verifications/{id}/approve|reject
 *
 * Layered structure to implement (mirror the fully-built 'identity' module):
 *   admin/domain      JPA entities + enums
 *   admin/repository  Spring Data JPA repositories
 *   admin/service     business logic, @Transactional
 *   admin/controller  REST controllers
 *   admin/dto         request/response objects
 */
package com.eximplatform.admin;
