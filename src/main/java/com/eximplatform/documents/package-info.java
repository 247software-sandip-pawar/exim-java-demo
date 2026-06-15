/**
 * Documents module - generation of trade documents.
 *
 * Planned entities:   TradeDocument
 * Planned endpoints:  POST/GET /api/v1/orders/{id}/documents, GET /api/v1/documents/{id}
 *
 * Layered structure to implement (mirror the fully-built 'identity' module):
 *   documents/domain      JPA entities + enums
 *   documents/repository  Spring Data JPA repositories
 *   documents/service     business logic, @Transactional
 *   documents/controller  REST controllers
 *   documents/dto         request/response objects
 */
package com.eximplatform.documents;
