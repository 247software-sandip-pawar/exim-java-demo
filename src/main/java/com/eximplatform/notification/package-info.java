/**
 * Notification module - in-app and push notifications.
 *
 * Planned entities:   Notification
 * Planned endpoints:  GET /api/v1/notifications, PATCH /api/v1/notifications/{id}/read
 *
 * Layered structure to implement (mirror the fully-built 'identity' module):
 *   notification/domain      JPA entities + enums
 *   notification/repository  Spring Data JPA repositories
 *   notification/service     business logic, @Transactional
 *   notification/controller  REST controllers
 *   notification/dto         request/response objects
 */
package com.eximplatform.notification;
