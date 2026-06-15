/**
 * Sourcing module - buyer requirements (RFQ) and matching.
 *
 * Planned entities:   Rfq
 * Planned endpoints:  POST/GET/PATCH /api/v1/rfqs, GET /api/v1/rfqs/{id}/matches
 *
 * Layered structure to implement (mirror the fully-built 'identity' module):
 *   sourcing/domain      JPA entities + enums
 *   sourcing/repository  Spring Data JPA repositories
 *   sourcing/service     business logic, @Transactional
 *   sourcing/controller  REST controllers
 *   sourcing/dto         request/response objects
 */
package com.eximplatform.sourcing;
