package com.eximplatform.orders.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Create an order from an accepted quote. The quote is read from the quotation service over REST. */
public class CreateOrderRequest {

    @NotNull
    private UUID quoteId;

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }
}
