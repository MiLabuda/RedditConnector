package com.milabuda.redditconnector.redis;

import redis.clients.jedis.Jedis;

public class RedisApiCallsQueue {

    private static final String POSTS_TO_CHECK_KEY = "posts:to_check";

    private final Jedis jedis = RedisManager.getJedisResource();

    public void enqueuePostForProcessing(String postId) {
        jedis.rpush(POSTS_TO_CHECK_KEY, postId);
    }

    public String getPostReadyForProcessing() {
        return jedis.lpop(POSTS_TO_CHECK_KEY);
    }
}
