package com.milabuda.redditconnector.api.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.milabuda.redditconnector.RedditSourceConfig;
import feign.Feign;
import feign.Logger.Level;
import feign.Retryer;
import feign.auth.BasicAuthRequestInterceptor;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.slf4j.Slf4jLogger;

//I would like to align this with FeignClientFactoryProvider
public class AuthClientFactory {

    private static volatile AuthApiClient instance;

    public AuthClientFactory() {
    }

    public static AuthApiClient getInstance(RedditSourceConfig config) {
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
                            .logger(new Slf4jLogger(AuthClientFactory.class))
                            .logLevel(Level.HEADERS)
                            .target(AuthApiClient.class, config.getBaseUrl());
                }
            }
        }
        return instance;
    }
}