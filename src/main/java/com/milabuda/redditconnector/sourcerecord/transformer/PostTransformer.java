package com.milabuda.redditconnector.sourcerecord.transformer;

import com.milabuda.redditconnector.api.model.Listing;
import com.milabuda.redditconnector.api.model.Post;
import com.milabuda.redditconnector.redis.RedisManager;
import com.milabuda.redditconnector.redis.RedisPostCache;
import com.milabuda.redditconnector.sourcerecord.builder.PostRecordBuilder;
import org.apache.kafka.connect.source.SourceRecord;
import redis.clients.jedis.Jedis;

import java.util.Comparator;
import java.util.List;

public class PostTransformer implements Transformer<Post> {

    private final PostRecordBuilder postRecordBuilder;
    private final RedisPostCache redisPostCache;

    public PostTransformer(PostRecordBuilder postRecordBuilder) {
        this.postRecordBuilder = postRecordBuilder;
        this.redisPostCache = new RedisPostCache();
    }

    @Override
    public List<SourceRecord> transform(Listing<Post> postsResponse) {
        try (Jedis jedis = RedisManager.getJedisResource()) {
            return postsResponse
                    .children()
                    .stream()
                    .sorted(Comparator.comparing(e -> e.data().createdUtc()))
                    .filter(post -> redisPostCache.isPostNew(jedis, post))
                    .peek(post -> redisPostCache.markPostAsProcessed(jedis, post))
                    .peek(post -> redisPostCache.updateLastUpdateTimestamp(jedis, post.data().createdUtc(), post.data().id()))
                    .map(postRecordBuilder::buildSourceRecord)
                    .toList();
        }
    }
}