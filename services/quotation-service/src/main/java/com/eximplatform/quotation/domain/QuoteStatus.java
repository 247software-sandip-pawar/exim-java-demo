package com.eximplatform.quotation.domain;

/**
 * Lifecycle of a quote. A {@code SUBMITTED} quote is the only one a buyer can act on; accepting,
 * rejecting, or countering it is terminal for that quote (a counter spawns a new SUBMITTED quote).
 */
public enum QuoteStatus {
    SUBMITTED, ACCEPTED, REJECTED, COUNTERED, EXPIRED
}
