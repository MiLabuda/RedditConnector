package com.milabuda.redditconnector.api.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
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
import feign.slf4j.Slf4jLogger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

//TODO Align with post client factory because they are similar
public class CommentClientFactory {

    private final ReentrantLock lock = new ReentrantLock();
    private CommentClient commentClient;
    private OAuthData currentToken;
    private final AuthManager authManager;

    public CommentClientFactory(AuthManager authManager) {
        this.authManager = authManager;
        this.currentToken = authManager.getRedditToken();
        this.commentClient = createRedditCommentsClient(this.currentToken);
    }

    public CommentClient getClient() {
        OAuthData latestToken = authManager.getRedditToken();

        if (!latestToken.equals(currentToken)) {
            lock.lock();
            try {
                if (!latestToken.equals(currentToken)) {
                    this.currentToken = latestToken;
                    this.commentClient = createRedditCommentsClient(latestToken);
                }
            } finally {
                lock.unlock();
            }
        }
        return commentClient;
    }

    private CommentClient createRedditCommentsClient(OAuthData token) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder(objectMapper))
                .errorDecoder(new CustomErrorDecoder())
                .logger(new Slf4jLogger(CommentClientFactory.class))
                .logLevel(Level.HEADERS)
                .retryer(new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3))
                .requestInterceptor(new BearerAuthInterceptor(token.accessToken()))
                .target(CommentClient.class, "https://oauth.reddit.com");
    }
}
