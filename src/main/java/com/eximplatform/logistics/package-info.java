/**
 * Logistics module - shipments and logistics partners (CHA / freight forwarders).
 *
 * Planned entities:   Shipment, LogisticsPartner
 * Planned endpoints:  POST/GET /api/v1/orders/{id}/shipment, PATCH /api/v1/shipments/{id}
 *
 * Layered structure to implement (mirror the fully-built 'identity' module):
 *   logistics/domain      JPA entities + enums
 *   logistics/repository  Spring Data JPA repositories
 *   logistics/service     business logic, @Transactional
 *   logistics/controller  REST controllers
 *   logistics/dto         request/response objects
 */
package com.eximplatform.logistics;
