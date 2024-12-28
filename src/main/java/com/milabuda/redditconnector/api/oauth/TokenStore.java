package com.milabuda.redditconnector.api.oauth;


public interface TokenStore {

    void storeLatest(OAuthData data);

    OAuthData fetchLatest();

    void deleteLatest();
}
