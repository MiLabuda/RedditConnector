package com.milabuda.redditconnector.cache;

import com.milabuda.redditconnector.api.model.Comment;
import redis.clients.jedis.Jedis;

public class RedisCommentCache {

    public boolean isCommentNew(Jedis jedis, Comment comment) {
        return !jedis.sismember("comments:stored", comment.id());
    }

    public void markCommentAsProcessed(Jedis jedis, Comment post) {
        jedis.sadd("comments:stored", post.id());
    }
}
