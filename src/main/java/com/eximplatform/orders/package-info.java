/**
 * Orders module - orders and contracts created from accepted quotes.
 *
 * Planned entities:   Order, OrderItem
 * Planned endpoints:  GET /api/v1/orders, PATCH /api/v1/orders/{id}/status, POST /api/v1/orders/{id}/cancel
 *
 * Layered structure to implement (mirror the fully-built 'identity' module):
 *   orders/domain      JPA entities + enums
 *   orders/repository  Spring Data JPA repositories
 *   orders/service     business logic, @Transactional
 *   orders/controller  REST controllers
 *   orders/dto         request/response objects
 */
package com.eximplatform.orders;
