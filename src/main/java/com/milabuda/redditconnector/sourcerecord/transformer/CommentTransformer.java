package com.milabuda.redditconnector.sourcerecord.transformer;

import com.milabuda.redditconnector.api.model.Comment;
import com.milabuda.redditconnector.redis.RedisCommentCache;
import com.milabuda.redditconnector.redis.RedisManager;
import com.milabuda.redditconnector.sourcerecord.builder.CommentRecordBuilder;
import org.apache.kafka.connect.source.SourceRecord;
import redis.clients.jedis.Jedis;

import java.util.Comparator;
import java.util.List;

public class CommentTransformer {

    private final CommentRecordBuilder commentRecordBuilder;
    private final RedisCommentCache redisCommentCache;

    public CommentTransformer(CommentRecordBuilder commentRecordBuilder) {
        this.commentRecordBuilder = commentRecordBuilder;
        this.redisCommentCache = new RedisCommentCache();
    }

    public List<SourceRecord> transform(List<Comment> comments) {
        try (Jedis jedis = RedisManager.getJedisResource()) {
            return comments
                    .stream()
                    .sorted(Comparator.comparing(Comment::createdUtc))
                    .filter(post -> redisCommentCache.isCommentNew(jedis, post))
                    .peek(post -> redisCommentCache.markCommentAsProcessed(jedis, post))
                    .map(commentRecordBuilder::buildSourceRecord)
                    .toList();
        }
    }
}
