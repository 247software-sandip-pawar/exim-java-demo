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
 * Synchronous REST client for the orders service. Used by documents to generate a trade document
 * for an order without sharing orders' database. Forwards the caller's JWT.
 */
@Component
public class OrdersClient {

    private final RestClient restClient;

    public OrdersClient(@Value("${app.clients.orders.base-url:http://localhost:8087}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** @return the order, or empty on a 404 (or any non-200 / empty body). */
    public Optional<OrderView> getOrder(UUID orderId) {
        Envelope body = restClient.get()
                .uri("/api/v1/orders/{id}", orderId)
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
    private record Envelope(OrderView data) {
    }
}
