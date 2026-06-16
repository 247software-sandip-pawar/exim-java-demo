package com.eximplatform.common.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Synchronous REST client for the identity service. Used by other services to validate cross-module
 * references (e.g. that a {@code companyId} exists) without sharing identity's database.
 *
 * <p>The caller's JWT is forwarded so the downstream call is authenticated as the same principal.
 */
@Component
public class IdentityClient {

    private final RestClient restClient;

    public IdentityClient(@Value("${app.clients.identity.base-url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** @return true if the company exists in the identity service, false on 404. */
    public boolean companyExists(UUID companyId) {
        return restClient.get()
                .uri("/api/v1/companies/{id}", companyId)
                .headers(this::forwardAuthorization)
                .exchange((request, response) -> {
                    int status = response.getStatusCode().value();
                    if (status == 200) {
                        return true;
                    }
                    if (status == 404) {
                        return false;
                    }
                    throw new IllegalStateException(
                            "identity-service returned status " + status + " for company lookup");
                });
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
}
