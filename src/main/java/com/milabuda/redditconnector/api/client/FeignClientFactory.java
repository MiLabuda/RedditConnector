package com.milabuda.redditconnector.api.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.milabuda.redditconnector.api.oauth.AuthManager;
import com.milabuda.redditconnector.api.oauth.BearerAuthInterceptor;
import com.milabuda.redditconnector.api.oauth.OAuthData;
import feign.Feign;
import feign.Logger;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class FeignClientFactory<T> {

    private final ReentrantLock lock = new ReentrantLock();
    private final AuthManager authManager;
    private final Class<T> clientClass;
    private final String baseUrl;
    private T clientInstance;
    private OAuthData currentToken;

    public FeignClientFactory(AuthManager authManager, Class<T> clientClass, String baseUrl) {
        this.authManager = authManager;
        this.clientClass = clientClass;
        this.baseUrl = baseUrl;
        this.currentToken = authManager.getRedditToken();
        this.clientInstance = createClientInstance(currentToken);
    }

    public T getClient() {
        OAuthData latestToken = authManager.getRedditToken();

        if (!latestToken.equals(currentToken)) {
            lock.lock();
            try {
                if (!latestToken.equals(currentToken)) {
                    this.currentToken = latestToken;
                    this.clientInstance = createClientInstance(latestToken);
                }
            } finally {
                lock.unlock();
            }
        }
        return clientInstance;
    }

    private T createClientInstance(OAuthData token) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder(objectMapper))
                .errorDecoder(new CustomErrorDecoder())
                .logger(new Slf4jLogger(clientClass))
                .logLevel(Logger.Level.HEADERS)
                .retryer(new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3))
                .requestInterceptor(new BearerAuthInterceptor(token.accessToken()))
                .target(clientClass, baseUrl);
    }
}
