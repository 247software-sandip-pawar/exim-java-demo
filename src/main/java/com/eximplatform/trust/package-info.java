/**
 * Trust module - counterparty ratings and disputes.
 *
 * Planned entities:   Rating, Dispute
 * Planned endpoints:  POST /api/v1/orders/{id}/ratings, POST /api/v1/orders/{id}/disputes
 *
 * Layered structure to implement (mirror the fully-built 'identity' module):
 *   trust/domain      JPA entities + enums
 *   trust/repository  Spring Data JPA repositories
 *   trust/service     business logic, @Transactional
 *   trust/controller  REST controllers
 *   trust/dto         request/response objects
 */
package com.eximplatform.trust;
