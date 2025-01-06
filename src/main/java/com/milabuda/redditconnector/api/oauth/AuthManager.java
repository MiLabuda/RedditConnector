package com.milabuda.redditconnector.api.oauth;

import com.milabuda.redditconnector.RedditSourceConfig;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AuthManager {


    private static final Logger log = LoggerFactory.getLogger(AuthManager.class);

    private final AuthClient httpClient;
    private final RedditSourceConfig config;
    private final TokenStore tokenStore;

    public AuthManager(RedditSourceConfig config, AuthClient httpClient, TokenStore tokenStore) {
        this.config = config;
        this.httpClient = httpClient;
        this.tokenStore = tokenStore;
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
}
