package com.milabuda.redditconnector.recordconverter.transformer;

import com.milabuda.redditconnector.api.model.Post;
import com.milabuda.redditconnector.cache.RedisManager;
import com.milabuda.redditconnector.cache.RedisPostCache;
import com.milabuda.redditconnector.recordconverter.builder.PostRecordBuilder;
import com.milabuda.redditconnector.recordconverter.schema.EventType;
import org.apache.kafka.connect.source.SourceRecord;
import redis.clients.jedis.Jedis;

import java.util.Comparator;
import java.util.List;

public class PostTransformer{

    private final PostRecordBuilder postRecordBuilder;
    private final RedisPostCache redisPostCache;

    public PostTransformer() {
        this.postRecordBuilder = new PostRecordBuilder();
        this.redisPostCache = new RedisPostCache();
    }

    public List<SourceRecord> transform(List<Post> postsResponse, EventType eventType) {
        try (Jedis jedis = RedisManager.getJedisResource()) {
            return postsResponse
                    .stream()
                    .sorted(Comparator.comparing(Post::createdUtc, Comparator.nullsFirst(Long::compareTo)))
                    .filter(post -> eventType != EventType.CREATE || redisPostCache.isPostNew(jedis, post))
                    .peek(post -> redisPostCache.markPostAsProcessed(jedis, post))
                    .peek(post -> redisPostCache.updateLastUpdateTimestamp(jedis, post))
                    .map(post -> postRecordBuilder.buildSourceRecord(post, eventType))
                    .toList();
        }
    }
}