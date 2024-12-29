package com.milabuda.redditconnector.redis;

import com.milabuda.redditconnector.api.model.Comment;
import redis.clients.jedis.Jedis;

public class RedisCommentCache {

    public boolean isCommentNew(Jedis jedis, Comment comment) {
        return !jedis.sismember("processed_comments", comment.id());
    }

    public void markCommentAsProcessed(Jedis jedis, Comment post) {
        jedis.sadd("processed_comments", post.id());
    }
}
