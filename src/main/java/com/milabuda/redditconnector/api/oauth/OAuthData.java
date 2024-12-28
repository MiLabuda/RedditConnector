package com.milabuda.redditconnector.api.oauth;

import java.time.ZonedDateTime;

public record OAuthData(
        String accessToken,
        String tokenType,
        int expiresIn,
        String scope,
        ZonedDateTime expireAt
) {
    public OAuthData(String accessToken,
                       String tokenType,
                       int expiresIn,
                       String scope) {
        this(accessToken, tokenType, expiresIn, scope, ZonedDateTime.now().plusSeconds(expiresIn));
    }

    public OAuthData(String accessToken,
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
