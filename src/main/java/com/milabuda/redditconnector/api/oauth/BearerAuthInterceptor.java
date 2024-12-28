package com.milabuda.redditconnector.api.oauth;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class BearerAuthInterceptor implements RequestInterceptor {

    private final String token;

    public BearerAuthInterceptor(String token) {
        this.token = token;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", "Bearer " + token);
    }
}
