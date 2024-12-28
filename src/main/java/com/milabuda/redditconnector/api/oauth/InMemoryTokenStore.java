package com.milabuda.redditconnector.api.oauth;

public class InMemoryTokenStore implements TokenStore {

    private OAuthData currentToken;

    @Override
    public void storeLatest(OAuthData data) {
        currentToken = data;
    }

    @Override
    public OAuthData fetchLatest() {
        if (currentToken == null || !currentToken.isValid()) {
            return null;
        }
        return currentToken;
    }

    @Override
    public void deleteLatest() {
        this.currentToken = null;
    }
}