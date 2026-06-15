/**
 * Billing module - subscription plans and platform revenue.
 *
 * Planned entities:   Plan, Subscription, PlatformPayment
 * Planned endpoints:  GET /api/v1/plans, POST /api/v1/companies/{id}/subscription
 *
 * Layered structure to implement (mirror the fully-built 'identity' module):
 *   billing/domain      JPA entities + enums
 *   billing/repository  Spring Data JPA repositories
 *   billing/service     business logic, @Transactional
 *   billing/controller  REST controllers
 *   billing/dto         request/response objects
 */
package com.eximplatform.billing;
