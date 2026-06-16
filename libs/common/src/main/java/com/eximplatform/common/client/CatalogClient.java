package com.eximplatform.common.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * Synchronous REST client for the catalog service. Used by sourcing to match RFQs to listed
 * products by HS code without sharing catalog's database. Forwards the caller's JWT.
 */
@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(@Value("${app.clients.catalog.base-url:http://localhost:8083}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** @return products listed under the given HS code (empty if none / on a non-200 response). */
    public List<ProductMatch> findProductsByHsCode(String hsCode) {
        Envelope body = restClient.get()
                .uri(uri -> uri.path("/api/v1/products").queryParam("hsCode", hsCode).queryParam("size", 100).build())
                .headers(this::forwardAuthorization)
                .exchange((request, response) -> response.getStatusCode().value() == 200
                        ? response.bodyTo(Envelope.class)
                        : null);
        if (body == null || body.data() == null || body.data().items() == null) {
            return List.of();
        }
        return body.data().items();
    }

    private void forwardAuthorization(HttpHeaders headers) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            String authorization = servletAttrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null) {
                headers.set(HttpHeaders.AUTHORIZATION, authorization);
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Envelope(PageData data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PageData(List<ProductMatch> items) {
    }
}
