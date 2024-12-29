package com.milabuda.redditconnector.redis;

import com.milabuda.redditconnector.api.model.Comment;
import com.milabuda.redditconnector.api.model.Envelope;
import com.milabuda.redditconnector.api.model.Post;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class RedisPostCache {

    public boolean isPostNew(Jedis jedis, Envelope<Post> post) {
        return !jedis.sismember("posts:stored", post.data().id());
    }

    public void markPostAsProcessed(Jedis jedis, Envelope<Post> post) {
        jedis.sadd("posts:stored", post.data().id());
    }

    public List<Tuple> getLastUpdateTimestamp(Jedis jedis, Instant currentTime) {
        List<Tuple> results = jedis.zrangeByScoreWithScores("posts:updates:sorted", 0, currentTime.toEpochMilli());
        return results != null ? results : Collections.emptyList();
    }

    public void updateLastUpdateTimestamp(Jedis jedis, long timestamp, String postId) {
        jedis.zadd("posts:updates:sorted", timestamp, postId);
    }

    public Long getLastCheckedTimestamp(Jedis jedis, String postId) {
        Double score = jedis.zscore("posts:checked:sorted", postId);
        return score != null ? score.longValue() : null;
    }

    public void updateLastCheckedTimestamp(Jedis jedis, long timestamp, String postId) {
        jedis.zadd("posts:checked:sorted", timestamp, postId);
    }


}