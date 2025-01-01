package com.milabuda.redditconnector.api.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.api.model.Comment;
import com.milabuda.redditconnector.api.model.Envelope;
import com.milabuda.redditconnector.api.model.Listing;
import com.milabuda.redditconnector.api.model.Pair;
import com.milabuda.redditconnector.api.model.Post;
import com.milabuda.redditconnector.api.model.PostAndCommentData;
import com.milabuda.redditconnector.api.oauth.AuthManager;
import com.milabuda.redditconnector.redis.RedisApiCallsQueue;
import com.milabuda.redditconnector.redis.RedisManager;
import com.milabuda.redditconnector.redis.RedisPostCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommentManager {

    private static final int PROCESSING_LIMIT = 10;

    private static final Logger log = LoggerFactory.getLogger(CommentManager.class);

    private final RedditSourceConfig config;
    private final AuthManager authManager;
    private final CommentClientFactory clientFactory;
    private final Jedis jedis = RedisManager.getJedisResource();
    private final RedisApiCallsQueue redisApiCallsQueue = new RedisApiCallsQueue();
    private final RedisPostCache redisPostCache = new RedisPostCache();

    public CommentManager(RedditSourceConfig config, AuthManager authManager) {
        this.config = config;
        this.authManager = authManager;
        this.clientFactory = new CommentClientFactory(authManager);
    }

    public Pair<List<Post>, List<Comment>> retrieveCommentsAndPostUpdates() {
        registerPostsToCheck();
        String postId;
        List<Comment> allComments = new ArrayList<>();
        List<Post> allPosts = new ArrayList<>();

        while ((postId = redisApiCallsQueue.getPostReadyForProcessing()) != null) {
            PostAndCommentData postAndCommentData = getPostAndComments(postId);
            if (postAndCommentData == null) {
                continue;
            }
            int initialSize = allComments.size();
            for (Envelope<Comment> comment : postAndCommentData.comments().data().children()) {
                allComments.addAll(traverseComments(comment.data()));
            }
            int retrievedComments = allComments.size() - initialSize;
            log.info("Retrieved {} comments for postId: {}", retrievedComments, postId);


            for (Envelope<Post> post : postAndCommentData.post().data().children()) {
                allPosts.add(post.data());
                log.info("Retrieved post update for postId: {}", postId);
            }
        }
        return new Pair<>(allPosts, allComments);
    }

    private List<Comment> traverseComments(Comment comment) {
        List<Comment> commentsList = new ArrayList<>();
        commentsList.add(comment);

        if (comment.replies() != null && comment.replies().data() != null && !comment.replies().data().children().isEmpty()) {
            List<Comment> replies = comment.replies().data().children().stream()
                    .map(Envelope::data)
                    .collect(Collectors.toList());

            for (Comment reply : replies) {
                commentsList.addAll(traverseComments(reply));
            }
        }
        return commentsList;
    }

    public void registerPostsToCheck() {
        Instant currentTime = Instant.now();
        int counter = 0;

        List<Tuple> postsWithTimestamps = redisPostCache.getLastUpdateTimestamp(jedis);

        for (Tuple postTuple : postsWithTimestamps) {
            String postId = postTuple.getElement();
            Instant lastActivityTime = Instant.ofEpochMilli((long) postTuple.getScore());

            Instant lastCheckedTimestamp = redisPostCache.getLastCheckedTimestamp(jedis, postId);

            if (shouldProcessPost(lastActivityTime, lastCheckedTimestamp, currentTime)) {
                redisApiCallsQueue.enqueuePostForProcessing(postId);
                redisPostCache.updateLastCheckedTimestamp(jedis, currentTime, postId);
                log.info("Post added to queue for processing: {}", postId);

                counter++;
                if (counter >= PROCESSING_LIMIT) {
                    log.info("Processed 10 posts, stopping for this iteration.");
                    break;
                }
            }
        }
    }

    private boolean shouldProcessPost(Instant lastActivityTime, Instant lastCheckedTimestamp, Instant currentTime) {
        long elapsedSinceActivity = Duration.between(lastActivityTime, currentTime).toSeconds();
        long elapsedSinceCheck = lastCheckedTimestamp == null
                ? Long.MAX_VALUE
                : Duration.between(lastCheckedTimestamp, currentTime).toSeconds();

        return (elapsedSinceActivity <= 3600 && elapsedSinceCheck >= 600)
                || (elapsedSinceActivity <= 10800 && elapsedSinceCheck >= 900)
                || (elapsedSinceActivity <= 21600 && elapsedSinceCheck >= 1800)
                || (elapsedSinceActivity <= 43200 && elapsedSinceCheck >= 3600)
                || (elapsedSinceActivity <= 86400 && elapsedSinceCheck >= 7200)
                || (elapsedSinceActivity <= 172800 && elapsedSinceCheck >= 10800)
                || (elapsedSinceActivity > 172800 && elapsedSinceCheck >= 21600);
    }

    private PostAndCommentData getPostAndComments(String postId) {
        try {
            CommentClient client = clientFactory.getClient();
            Map<String, Object> queryMap = new HashMap<>();

            JsonNode root = client.getPostWithComments(
                    config.getSubreddits(),
                    postId,
                    authManager.getRedditToken().accessToken(),
                    config.getUserAgent(),
                    queryMap);

            Envelope<Listing<Post>> post = mapJsonNode(root.get(0), new TypeReference<>() {});
            Envelope<Listing<Comment>> comments = mapJsonNode(root.get(1), new TypeReference<>() {});

            return new PostAndCommentData(post, comments);
        } catch (Exception e) {
            log.error("Error occurred while fetching comments for postId: {}", postId, e);
            return null;
        }
    }

    private <T> T mapJsonNode(JsonNode node, TypeReference<T> typeReference) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        return objectMapper.treeToValue(node, typeReference);
    }

}
