package com.milabuda.redditconnector.api.client;

import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.redis.RedisApiCallsQueue;
import com.milabuda.redditconnector.redis.RedisManager;
import com.milabuda.redditconnector.redis.RedisPostCache;
import redis.clients.jedis.resps.Tuple;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class SubmissionUpdateScheduler {

    private final RedisApiCallsQueue redisApiCallsQueue;
    private final RedisPostCache redisPostCache;
    private final RedditSourceConfig config;


    public SubmissionUpdateScheduler(RedisApiCallsQueue redisApiCallsQueue,
                                     RedisPostCache redisPostCache,
                                     RedditSourceConfig config) {
        this.redisApiCallsQueue = redisApiCallsQueue;
        this.redisPostCache = redisPostCache;
        this.config = config;
    }

    public void schedulePostsForUpdate() {
        Instant currentTime = Instant.now();
        List<Tuple> postsWithTimestamps = redisPostCache.getLastUpdateTimestamp(RedisManager.getJedisResource());

        int processed = 0;
        for (Tuple post : postsWithTimestamps) {
            if (processed >= config.getPoolingBatchSizeDefault()) break;

            String postId = post.getElement();
            Instant lastActivityTime = Instant.ofEpochMilli((long) post.getScore());
            Instant lastCheckedTimestamp = redisPostCache.getLastCheckedTimestamp(RedisManager.getJedisResource(), postId);

            if (shouldEnqueuePost(lastActivityTime, lastCheckedTimestamp, currentTime)) {
                redisApiCallsQueue.enqueuePostForProcessing(postId);
                redisPostCache.updateLastCheckedTimestamp(RedisManager.getJedisResource(), currentTime, postId);
                processed++;
            }
        }
    }

    private boolean shouldEnqueuePost(Instant lastActivityTime, Instant lastCheckedTimestamp, Instant currentTime) {
        long elapsedSinceActivity = Duration.between(lastActivityTime, currentTime).toSeconds();
        long elapsedSinceCheck = lastCheckedTimestamp == null ? Long.MAX_VALUE
                : Duration.between(lastCheckedTimestamp, currentTime).toSeconds();

        return elapsedSinceCheck >= calculateCheckInterval(elapsedSinceActivity);
    }

    private long calculateCheckInterval(long elapsedSinceActivity) {
        if (elapsedSinceActivity <= 3600) return 600;
        if (elapsedSinceActivity <= 10800) return 900;
        if (elapsedSinceActivity <= 21600) return 1800;
        if (elapsedSinceActivity <= 43200) return 3600;
        if (elapsedSinceActivity <= 86400) return 7200;
        if (elapsedSinceActivity <= 172800) return 10800;
        return 21600;
    }
}