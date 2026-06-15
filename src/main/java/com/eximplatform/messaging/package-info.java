/**
 * Messaging module - buyer-seller negotiation (REST + WebSocket).
 *
 * Planned entities:   Conversation, Message, Offer
 * Planned endpoints:  GET/POST /api/v1/conversations, /api/v1/conversations/{id}/messages, WS /ws
 *
 * Layered structure to implement (mirror the fully-built 'identity' module):
 *   messaging/domain      JPA entities + enums
 *   messaging/repository  Spring Data JPA repositories
 *   messaging/service     business logic, @Transactional
 *   messaging/controller  REST controllers
 *   messaging/dto         request/response objects
 */
package com.eximplatform.messaging;
