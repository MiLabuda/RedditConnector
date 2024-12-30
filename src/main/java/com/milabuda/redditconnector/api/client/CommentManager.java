package com.milabuda.redditconnector.api.client;

import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.api.model.Comment;
import com.milabuda.redditconnector.api.model.Envelope;
import com.milabuda.redditconnector.api.model.Listing;
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
import java.util.Set;
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

    public List<Comment> retrieveComments() {
        registerPostsToCheck();
        String postId;
        List<Comment> allComments = new ArrayList<>();

        while ((postId = redisApiCallsQueue.getPostReadyForProcessing()) != null) {
            Listing<Comment> postWithComments = getComments(postId);
            if (postWithComments == null) {
                continue;
            }
            for (Envelope<Comment> comment : postWithComments.children()) {
                allComments.addAll(traverseComments(comment.data()));
            }
            log.info("Retrieved {} comments for postId: {}", allComments.size(), postId);
        }
        return allComments;
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

        List<Tuple> postsWithTimestamps = redisPostCache.getLastUpdateTimestamp(jedis, currentTime);

        for (Tuple postTuple : postsWithTimestamps) {
            String postId = postTuple.getElement();
            Instant lastActivityTime = Instant.ofEpochSecond((long) postTuple.getScore());

            Long lastCheckedTimestamp = redisPostCache.getLastCheckedTimestamp(jedis, postId);

            if (shouldProcessPost(lastActivityTime, lastCheckedTimestamp, currentTime)) {
                redisApiCallsQueue.enqueuePostForProcessing(postId);
                redisPostCache.updateLastCheckedTimestamp(jedis, currentTime.toEpochMilli(), postId);
                log.info("Post added to queue for processing: {}", postId);

                counter++;
                if (counter >= PROCESSING_LIMIT) {
                    log.info("Processed 10 posts, stopping for this iteration.");
                    break;
                }
            }
        }
    }

    private boolean shouldProcessPost(Instant lastActivityTime, Long lastCheckedTimestamp, Instant currentTime) {
        long elapsedSinceActivity = Duration.between(lastActivityTime, currentTime).toSeconds();
        long elapsedSinceCheck = lastCheckedTimestamp == null
                ? Long.MAX_VALUE
                : Duration.between(Instant.ofEpochSecond(lastCheckedTimestamp), currentTime).toSeconds();

        return (elapsedSinceActivity <= 3600 && elapsedSinceCheck >= 600) // < 1 godzina, > 10 minut
                || (elapsedSinceActivity <= 10800 && elapsedSinceCheck >= 900) // < 3 godziny, > 15 minut
                || (elapsedSinceActivity <= 21600 && elapsedSinceCheck >= 1800) // < 6 godzin, > 30 minut
                || (elapsedSinceActivity <= 43200 && elapsedSinceCheck >= 3600) // < 12 godzin, > 60 minut
                || (elapsedSinceActivity <= 86400 && elapsedSinceCheck >= 7200) // < 24 godziny, > 120 minut
                || (elapsedSinceActivity <= 172800 && elapsedSinceCheck >= 10800) // < 48 godzin, > 180 minut
                || (elapsedSinceActivity > 172800 && elapsedSinceCheck >= 21600); // > 48 godzin, > 360 minut
    }

    private Listing<Comment> getComments(String postId) {
        try {
            CommentClient client = clientFactory.getClient();
            Map<String, Object> queryMap = new HashMap<>();

            List<Envelope<Listing<Comment>>> postWithCommentsList = client.getPostWithComments(
                    config.getSubreddits(),
                    postId,
                    authManager.getRedditToken().accessToken(),
                    config.getUserAgent(),
                    queryMap);

            if (postWithCommentsList == null || postWithCommentsList.isEmpty()) {
                log.warn("No comments found for postId: {}", postId);
                return Listing.empty();
            }

            return postWithCommentsList.get(postWithCommentsList.size()-1).data();
        } catch (Exception e) {
            log.error("Error occurred while fetching comments for postId: {}", postId, e);
            return Listing.empty();
        }
    }

}
