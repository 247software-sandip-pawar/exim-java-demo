package com.eximplatform.sourcing.dto;

import com.eximplatform.common.client.ProductMatch;

import java.util.List;
import java.util.UUID;

/**
 * Matches for an RFQ: products (fetched from the catalog service) listed under the RFQ's HS code.
 */
public record RfqMatchesResponse(UUID rfqId, String hsCode, int count, List<ProductMatch> matches) {

    public static RfqMatchesResponse of(UUID rfqId, String hsCode, List<ProductMatch> matches) {
        return new RfqMatchesResponse(rfqId, hsCode, matches.size(), matches);
    }
}
