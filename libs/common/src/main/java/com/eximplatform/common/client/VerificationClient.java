package com.eximplatform.common.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Synchronous REST client for the verification service. Used by admin to approve/reject KYC
 * submissions without sharing verification's database. Forwards the caller's JWT.
 */
@Component
public class VerificationClient {

    private final RestClient restClient;

    public VerificationClient(@Value("${app.clients.verification.base-url:http://localhost:8082}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** @return the verification, or empty on a 404 (or any non-200 / empty body). */
    public Optional<VerificationView> getVerification(UUID verificationId) {
        Envelope body = restClient.get()
                .uri("/api/v1/verifications/{id}", verificationId)
                .headers(this::forwardAuthorization)
                .exchange((request, response) -> response.getStatusCode().value() == 200
                        ? response.bodyTo(Envelope.class)
                        : null);
        return body == null ? Optional.empty() : Optional.ofNullable(body.data());
    }

    /**
     * Record an admin decision on a verification.
     * @param decision "APPROVED" or "REJECTED"
     * @return the updated verification, or empty if it does not exist.
     */
    public Optional<VerificationView> decide(UUID verificationId, String decision, String note) {
        Envelope body = restClient.patch()
                .uri("/api/v1/verifications/{id}/decision", verificationId)
                .headers(this::forwardAuthorization)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("status", decision, "reviewerNote", note == null ? "" : note))
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
    private record Envelope(VerificationView data) {
    }
}
