package com.eximplatform.notification.domain;

/** The kind of event a notification represents. */
public enum NotificationType {
    QUOTE_RECEIVED,
    QUOTE_ACCEPTED,
    ORDER_CREATED,
    SHIPMENT_UPDATE,
    PAYMENT_RELEASED,
    DISPUTE_UPDATE,
    GENERIC
}
