package com.eximplatform.messaging.dto;

import com.eximplatform.messaging.domain.Offer;

import java.math.BigDecimal;
import java.util.UUID;

/** In-chat offer payload, both on the way in (message request) and out (message response). */
public record OfferDto(
        UUID productId,
        int quantity,
        String unit,
        BigDecimal unitPrice,
        String currency,
        String incoterm) {

    public static OfferDto from(Offer offer) {
        if (offer == null) {
            return null;
        }
        return new OfferDto(offer.getProductId(), offer.getQuantity(), offer.getUnit(),
                offer.getUnitPrice(), offer.getCurrency(), offer.getIncoterm());
    }

    public Offer toEntity() {
        Offer offer = new Offer();
        offer.setProductId(productId);
        offer.setQuantity(quantity);
        offer.setUnit(unit);
        offer.setUnitPrice(unitPrice);
        offer.setCurrency(currency);
        offer.setIncoterm(incoterm);
        return offer;
    }
}
