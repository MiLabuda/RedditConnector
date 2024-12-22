package com.milabuda.redditconnector.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.api.model.RedditToken;
import feign.Feign;
import feign.FeignException;
import feign.Logger.Level;
import feign.Retryer;
import feign.auth.BasicAuthRequestInterceptor;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AuthManager {

    private static final Logger log = LoggerFactory.getLogger(AuthManager.class);

    private static final String GRANT_TYPE = "client_credentials";

    private final AuthClient redditOAuth;
    private final RedditSourceConfig config;

    private RedditToken token;

    public AuthManager(RedditSourceConfig config) {
        this.config = config;
        this.redditOAuth = redditOAuthClient();
    }

    public RedditToken getRedditToken() {
        if (isTokenValid()) {
            log.debug("Returning existing valid token.");
            return token;
        }

        log.info("Token is invalid or expired. Attempting to refresh token...");
        return refreshToken();
    }

    private boolean isTokenValid() {
        boolean isValid = token != null && token.isValid();
        log.info("Checking if token is valid...: " + isValid);
        log.info("isTokenValid: " + token);
        return token != null && token.isValid();
    }

    private RedditToken refreshToken() {
        RedditToken token;
        try {
            token = fetchNewToken();
            log.info("Token refreshed successfully.");
            log.info(token.accessToken());
            log.info(token.tokenType());
            log.info(Integer.toString(token.expiresIn()));
            log.info(token.scope());
            log.info(token.expireAt().toString());

        } catch (FeignException e) {
            log.error("Error while fetching access token, retrying not supported", e);
            token = null; // Invalidate the token if refresh failed
        }
        this.token = token;
        return token;
    }

    private RedditToken fetchNewToken() {
        log.info("Fetching new token...");
        return redditOAuth.getRedditToken(GRANT_TYPE, config.getUserAgent());
    }

    private AuthClient redditOAuthClient() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return Feign.builder()
                .encoder(new FormEncoder())
                .decoder(new JacksonDecoder(objectMapper))
                .retryer(Retryer.NEVER_RETRY)
                .requestInterceptor(new BasicAuthRequestInterceptor(
                        config.getClientId(), config.getClientSecret()))
                .logger(new CustomLogger())
                .logLevel(Level.FULL)
                .target(AuthClient.class, config.getBaseUrl());
    }
}
