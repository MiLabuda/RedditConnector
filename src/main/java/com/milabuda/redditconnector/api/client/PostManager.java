package com.milabuda.redditconnector.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.api.model.Listing;
import com.milabuda.redditconnector.api.model.Post;
import com.milabuda.redditconnector.api.model.RedditToken;
import feign.Feign;
import feign.Logger.Level;
import feign.Response;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class PostManager {

    private static final Logger log = LoggerFactory.getLogger(PostManager.class);

    private RedditSourceConfig config;
    private PostClient postClientClient;
    private String currentToken;

    public PostManager(RedditSourceConfig config, RedditToken token) {
        this.config = config;
        this.currentToken = token.accessToken();
        this.postClientClient = createRedditPostsClient(currentToken);
    }

    public Listing<Post> getPosts(RedditToken token) {
        if (!token.isValid()) {
            log.info("Token is invalid or expired. Returning back to the main loop.");
            return Listing.empty();
        }
        try {
            return postClientClient.getPosts(config.getSubreddits(), currentToken, config.getUserAgent(), null);
        } catch (Exception e) {
            log.error("Error occurred while fetching posts: {}", e.getMessage());
            return Listing.empty();
        }
    }

    private PostClient createRedditPostsClient(String bearerToken) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return Feign.builder()
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .errorDecoder(new CustomErrorDecoder())
                .logger(new CustomLogger())
                .logLevel(Level.FULL)
//                .logger(new Logger.JavaLogger().appendToFile("http.log")) //TODO Logger
                .requestInterceptor(new BearerAuthInterceptor(bearerToken))
                .target(PostClient.class, config.getBaseUrl());
    }
}

