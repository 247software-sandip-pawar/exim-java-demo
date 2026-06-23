package com.eximplatform.common.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

/**
 * Synchronous REST client for the quotation service. Used by orders to build an order from an
 * accepted quote without sharing quotation's database. Forwards the caller's JWT.
 */
@Component
public class QuotationClient {

    private final RestClient restClient;

    public QuotationClient(@Value("${app.clients.quotation.base-url:http://localhost:8085}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** @return the quote, or empty on a 404 (or any non-200 / empty body). */
    public Optional<QuoteView> getQuote(UUID quoteId) {
        Envelope body = restClient.get()
                .uri("/api/v1/quotes/{id}", quoteId)
                .headers(this::forwardAuthorization)
                .exchange((request, response) -> response.getStatusCode().value() == 200
                        ? response.bodyTo(Envelope.class)
                        : null);
        return body == null ? Optional.empty() : Optional.ofNullable(body.data());
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
    private record Envelope(QuoteView data) {
    }
}
