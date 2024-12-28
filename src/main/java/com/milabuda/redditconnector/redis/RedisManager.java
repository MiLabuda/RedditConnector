package com.milabuda.redditconnector.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisManager {

    private static JedisPool jedisPool;

    static {
        final JedisPoolConfig poolConfig = buildPoolConfig();
        jedisPool = new JedisPool(poolConfig, "redis://redis:6379");
    }

    private static JedisPoolConfig buildPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }

    public static Jedis getJedisResource() {
        return jedisPool.getResource();
    }

    public static void closePool() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}