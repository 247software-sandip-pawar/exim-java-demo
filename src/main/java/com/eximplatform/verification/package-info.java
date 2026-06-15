/**
 * Verification module - company KYC (IEC / GST / RCMC document checks).
 *
 * Planned entities:   Verification
 * Planned endpoints:  POST/GET /api/v1/companies/{id}/verifications
 *
 * Layered structure to implement (mirror the fully-built 'identity' module):
 *   verification/domain      JPA entities + enums
 *   verification/repository  Spring Data JPA repositories
 *   verification/service     business logic, @Transactional
 *   verification/controller  REST controllers
 *   verification/dto         request/response objects
 */
package com.eximplatform.verification;
