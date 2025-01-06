package com.milabuda.redditconnector.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.milabuda.redditconnector.api.oauth.AuthManager;
import com.milabuda.redditconnector.api.oauth.BearerAuthInterceptor;
import com.milabuda.redditconnector.api.oauth.OAuthData;
import feign.Feign;
import feign.Logger.Level;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class PostClientFactory {

    private final ReentrantLock lock = new ReentrantLock();
    private PostClient postClient;
    private OAuthData currentToken;
    private final AuthManager authManager;

    public PostClientFactory(AuthManager authManager) {
        this.authManager = authManager;
        this.currentToken = authManager.getRedditToken();
        this.postClient = createRedditPostsClient(this.currentToken);
    }

    public PostClient getClient() {
        OAuthData latestToken = authManager.getRedditToken();

        if (!latestToken.equals(currentToken)) {
            lock.lock();
            try {
                if (!latestToken.equals(currentToken)) {
                    this.currentToken = latestToken;
                    this.postClient = createRedditPostsClient(latestToken);
                }
            } finally {
                lock.unlock();
            }
        }
        return postClient;
    }

    private PostClient createRedditPostsClient(OAuthData token) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder(objectMapper))
                .errorDecoder(new CustomErrorDecoder())
                .logger(new CustomLogger())
                .logLevel(Level.FULL)
                .retryer(new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3))
                .requestInterceptor(new BearerAuthInterceptor(token.accessToken()))
                .target(PostClient.class, "https://oauth.reddit.com");
    }
}