package com.milabuda.redditconnector.api.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.api.client.CustomLogger;
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

    private final AuthClient httpClient;
    private final RedditSourceConfig config;
    private final TokenStore tokenStore;

    public AuthManager(RedditSourceConfig config) {
        this.config = config;
        this.tokenStore = new InMemoryTokenStore();
        this.httpClient = createOAuthHttpClient();
    }

    public OAuthData getRedditToken() {
        OAuthData fetchedToken = tokenStore.fetchLatest();
        if (fetchedToken != null) {
            log.debug("Returning existing valid token.");
            return fetchedToken;
        }

        log.info("Token is invalid or expired. Attempting to refresh token...");
        return refreshToken();
    }

    private OAuthData refreshToken() {
        try {
            OAuthData token = fetchNewToken();
            tokenStore.storeLatest(token);
            return token;
        } catch (FeignException e) {
            log.error("Error while fetching access token, retrying not supported", e);
            tokenStore.deleteLatest();
            return null;
        }
    }

    private OAuthData fetchNewToken() {
        log.info("Fetching new token...");
        return httpClient.getRedditToken(config);
    }

    private AuthClient createOAuthHttpClient() {

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
