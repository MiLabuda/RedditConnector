package com.milabuda.redditconnector.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.api.model.Envelope;
import com.milabuda.redditconnector.api.model.Listing;
import com.milabuda.redditconnector.api.model.Post;
import com.milabuda.redditconnector.api.model.RedditToken;
import feign.Feign;
import feign.Logger.Level;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;

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
            log.info("Fetching posts==============================================");
            log.info("subreddits:" + config.getSubreddits());
            log.info("currentToken:" + currentToken);
            log.info("userAgent:" + config.getUserAgent());
            log.info("=============================================================");

            // Attempt to fetch the posts using the postClientClient
            Envelope<Listing<Post>> postsListingEnvelope = postClientClient.getPosts(config.getSubreddits(), currentToken, config.getUserAgent(), null);
            if(postsListingEnvelope == null) {
                log.error("No posts found");
                return Listing.empty();
            }
            return postsListingEnvelope.data();
        } catch (BadRequestException e) {
            log.error("Bad request error while fetching posts: {}", e.getMessage());
            return Listing.empty(); // You may decide to return a fallback or an empty list here
        } catch (ForbiddenException e) {
            log.error("Forbidden error while fetching posts: {}", e.getMessage());
            return Listing.empty(); // Handle as appropriate for your use case
        } catch (NotFoundException e) {
            log.error("Not found error while fetching posts: {}", e.getMessage());
            return Listing.empty(); // Handle as appropriate
        } catch (Exception e) {
            // General catch for all other exceptions
            log.error("Error occurred while fetching posts: {}", e.getMessage());
            if (e instanceof IOException) {
                log.error("I/O Error: {}", e.getMessage());
            }
            return Listing.empty();
        }
    }

    private PostClient createRedditPostsClient(String bearerToken) {

        ObjectMapper objectMapperExternal = new ObjectMapper();
        objectMapperExternal.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder(objectMapper))
                .errorDecoder(new CustomErrorDecoder())
                .logger(new CustomLogger())
                .logLevel(Level.FULL)
//                .logger(new Logger.JavaLogger().appendToFile("http.log")) //TODO Logger
                .requestInterceptor(new BearerAuthInterceptor(bearerToken))
                .target(PostClient.class, "https://oauth.reddit.com");
    }
}

