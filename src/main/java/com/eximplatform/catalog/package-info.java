/**
 * Catalog module - product listings and HS codes.
 *
 * Planned entities:   Product, HsCode
 * Planned endpoints:  GET/POST/PUT/DELETE /api/v1/products, GET /api/v1/hs-codes
 *
 * Layered structure to implement (mirror the fully-built 'identity' module):
 *   catalog/domain      JPA entities + enums
 *   catalog/repository  Spring Data JPA repositories
 *   catalog/service     business logic, @Transactional
 *   catalog/controller  REST controllers
 *   catalog/dto         request/response objects
 */
package com.eximplatform.catalog;
