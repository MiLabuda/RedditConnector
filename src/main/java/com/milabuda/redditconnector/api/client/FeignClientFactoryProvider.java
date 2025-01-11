package com.milabuda.redditconnector.api.client;

import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.api.oauth.AuthManager;

public class FeignClientFactoryProvider {

    private final AuthManager authManager;
    private final RedditSourceConfig config;

    public FeignClientFactoryProvider(AuthManager authManager, RedditSourceConfig config) {
        this.authManager = authManager;
        this.config = config;
    }

    public FeignClientFactory<PostApiClient> getPostClientFactory() {
        return new FeignClientFactory<>(authManager, PostApiClient.class, config.getApiBaseUrl());
    }

    public FeignClientFactory<CommentApiClient> getCommentClientFactory() {
        return new FeignClientFactory<>(authManager, CommentApiClient.class, config.getApiBaseUrl());
    }
}