package com.eximplatform.gateway;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Records every authenticated API request to the activity-service, forming a platform-wide audit
 * trail. The gateway is the single choke point for all {@code /api/**} traffic, so one filter here
 * captures every user's actions without touching the 14 downstream services.
 *
 * <p>The POST carries the caller's own {@code Authorization} header, so the activity-service derives
 * the actor from that JWT (the user authorizes recording their own action). The call is
 * fire-and-forget (async, errors swallowed) so logging never blocks or breaks a request. Requests
 * with no token (e.g. login) and the activity endpoint itself are skipped.
 */
@Component
public class ActivityLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ActivityLoggingFilter.class);

    private final String activityBaseUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ActivityLoggingFilter(@Value("${app.activity.base-url:}") String activityBaseUrl) {
        this.activityBaseUrl = activityBaseUrl;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {
            record(request, response.getStatus());
        }
    }

    private void record(HttpServletRequest request, int status) {
        if (!StringUtils.hasText(activityBaseUrl)) {
            return;
        }
        String path = request.getRequestURI();
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Only authenticated /api/v1 calls; never record the recording call (would loop).
        if (auth == null || !path.startsWith("/api/v1/") || path.startsWith("/api/v1/activity")) {
            return;
        }
        String body = "{\"method\":\"" + esc(request.getMethod())
                + "\",\"path\":\"" + esc(path)
                + "\",\"status\":" + status + "}";
        HttpRequest post = HttpRequest.newBuilder()
                .uri(URI.create(activityBaseUrl + "/api/v1/activity"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        httpClient.sendAsync(post, HttpResponse.BodyHandlers.discarding())
                .exceptionally(ex -> {
                    log.debug("activity log skipped: {}", ex.getMessage());
                    return null;
                });
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
