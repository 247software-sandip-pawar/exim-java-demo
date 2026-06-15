/**
 * Payments module - payment terms, escrow milestones and LC (partner-backed).
 *
 * Planned entities:   Transaction, PaymentTerm, LetterOfCredit
 * Planned endpoints:  POST /api/v1/orders/{id}/payments/initiate, POST /api/v1/transactions/{id}/release
 *
 * Layered structure to implement (mirror the fully-built 'identity' module):
 *   payments/domain      JPA entities + enums
 *   payments/repository  Spring Data JPA repositories
 *   payments/service     business logic, @Transactional
 *   payments/controller  REST controllers
 *   payments/dto         request/response objects
 */
package com.eximplatform.payments;
