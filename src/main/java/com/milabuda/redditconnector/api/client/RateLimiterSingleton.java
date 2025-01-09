package com.milabuda.redditconnector.api.client;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class RateLimiterSingleton {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterSingleton.class);

    private static final RateLimiterSingleton INSTANCE = new RateLimiterSingleton();
    private final RateLimiterRegistry rateLimiterRegistry;
    private final RateLimiter rateLimiter;

    private RateLimiterSingleton() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .timeoutDuration(Duration.ofSeconds(30))
                .build();

        this.rateLimiterRegistry = RateLimiterRegistry.of(config);
        this.rateLimiter = rateLimiterRegistry.rateLimiter("redditLimiter");
        this.rateLimiter.getEventPublisher().onSuccess(event ->
                log.info("Request permitted. Available permissions: {}", rateLimiter.getMetrics().getAvailablePermissions()));
        this.rateLimiter.getEventPublisher().onFailure(event ->
                log.error("Rate limit exceeded! Available permissions: {}", rateLimiter.getMetrics().getAvailablePermissions()));
    }

    public static RateLimiterSingleton getInstance() {
        return INSTANCE;
    }

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }
}
