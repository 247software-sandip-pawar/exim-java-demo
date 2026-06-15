/**
 * Quotation module - quotes against RFQs and proforma invoices.
 *
 * Planned entities:   Quote, ProformaInvoice
 * Planned endpoints:  POST /api/v1/rfqs/{id}/quotes, POST /api/v1/quotes/{id}/accept|reject|counter
 *
 * Layered structure to implement (mirror the fully-built 'identity' module):
 *   quotation/domain      JPA entities + enums
 *   quotation/repository  Spring Data JPA repositories
 *   quotation/service     business logic, @Transactional
 *   quotation/controller  REST controllers
 *   quotation/dto         request/response objects
 */
package com.eximplatform.quotation;
