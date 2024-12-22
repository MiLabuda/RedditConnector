package com.milabuda.redditconnector.api.model;

import java.time.ZonedDateTime;

public record RedditToken(
        String accessToken,
        String tokenType,
        int expiresIn,
        String scope,
        ZonedDateTime expireAt
) {
    public RedditToken(String accessToken,
                       String tokenType,
                       int expiresIn,
                       String scope) {
        this(accessToken, tokenType, expiresIn, scope, ZonedDateTime.now().plusSeconds(expiresIn));
    }

    public RedditToken(String accessToken,
                       String tokenType,
                       int expiresIn,
                       String scope,
                       ZonedDateTime expireAt) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.scope = scope;
        this.expireAt = expireAt != null ? expireAt : ZonedDateTime.now().plusSeconds(expiresIn);
    }

    public boolean isValid() {
        return accessToken != null && !accessToken.isEmpty() && ZonedDateTime.now().isBefore(expireAt);
    }
}