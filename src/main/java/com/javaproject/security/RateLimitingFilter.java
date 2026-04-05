package com.javaproject.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaproject.dto.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using Bucket4j's token bucket algorithm.
 *
 * Each IP address gets its own bucket with a configurable capacity and refill rate.
 * When the bucket is empty, the client receives a 429 Too Many Requests response
 * with a Retry-After header indicating when they can retry.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final int capacity;
    private final int refillMinutes;

    public RateLimitingFilter(
            ObjectMapper objectMapper,
            @Value("${rate-limit.capacity:10}") int capacity,
            @Value("${rate-limit.refill-minutes:1}") int refillMinutes) {
        this.objectMapper = objectMapper;
        this.capacity = capacity;
        this.refillMinutes = refillMinutes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = getClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> createBucket());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        // Always include rate limit headers so clients can monitor their usage
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
        } else {
            long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse error = new ErrorResponse(
                    429,
                    "Rate limit exceeded. Try again in " + retryAfterSeconds + " seconds.",
                    null
            );
            objectMapper.writeValue(response.getWriter(), error);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")
                || path.startsWith("/ws");
    }

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, Duration.ofMinutes(refillMinutes))
                        .build())
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        // Support clients behind reverse proxies
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
