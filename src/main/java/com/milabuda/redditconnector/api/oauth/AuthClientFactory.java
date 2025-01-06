package com.milabuda.redditconnector.api.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.api.client.CustomLogger;
import feign.Feign;
import feign.Logger.Level;
import feign.Retryer;
import feign.auth.BasicAuthRequestInterceptor;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;

public class AuthClientFactory {

    private static volatile AuthClient instance;

    public AuthClientFactory() {
    }

    public static AuthClient getInstance(RedditSourceConfig config) {
        if (instance == null) {
            synchronized (AuthClientFactory.class) {
                if (instance == null) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

                    instance = Feign.builder()
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
        }
        return instance;
    }
}