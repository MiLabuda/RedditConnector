package com.milabuda.redditconnector.cache;

import com.milabuda.redditconnector.api.model.Post;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.time.Instant;
import java.util.List;

public class RedisPostCache {

    public boolean isPostNew(Jedis jedis, Post post) {
        return !jedis.sismember("posts:stored", post.id());
    }

    public void markPostAsProcessed(Jedis jedis, Post post) {
        jedis.sadd("posts:stored", post.id());
    }

    public List<Tuple> getLastUpdateTimestamp(Jedis jedis) {
        return jedis.zrangeWithScores("posts:updates:sorted", 0, -1);
    }

    public void updateLastUpdateTimestamp(Jedis jedis, Post post) {
        jedis.zadd("posts:updates:sorted", getEpochMilliseconds(post.createdUtc()), post.id());
    }

    public Instant getLastCheckedTimestamp(Jedis jedis, String postId) {
        Double score = jedis.zscore("posts:checked:sorted", postId);
        return score != null ? Instant.ofEpochMilli(score.longValue()) : Instant.EPOCH;
    }

    public void updateLastCheckedTimestamp(Jedis jedis, Instant currentTime, String postId) {
        jedis.zadd("posts:checked:sorted", currentTime.toEpochMilli(), postId);
    }

    private Long getEpochMilliseconds(long timestamp) {
        return Instant.ofEpochSecond(timestamp).toEpochMilli();
    }

}