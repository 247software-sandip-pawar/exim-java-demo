package com.eximplatform.billing.domain;

/** Lifecycle of a platform (subscription) payment. */
public enum PaymentStatus {
    PENDING, PAID, FAILED, REFUNDED
}
