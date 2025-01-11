package com.milabuda.redditconnector.recordconverter.transformer;

import com.milabuda.redditconnector.api.model.Comment;
import com.milabuda.redditconnector.cache.RedisCommentCache;
import com.milabuda.redditconnector.cache.RedisManager;
import com.milabuda.redditconnector.recordconverter.builder.CommentRecordBuilder;
import org.apache.kafka.connect.source.SourceRecord;
import redis.clients.jedis.Jedis;

import java.util.Comparator;
import java.util.List;

public class CommentTransformer {

    private final CommentRecordBuilder commentRecordBuilder;
    private final RedisCommentCache redisCommentCache;

    public CommentTransformer() {
        this.commentRecordBuilder = new CommentRecordBuilder();
        this.redisCommentCache = new RedisCommentCache();
    }

    public List<SourceRecord> transform(List<Comment> comments) {
        try (Jedis jedis = RedisManager.getJedisResource()) {
            return comments
                    .stream()
                    .sorted(Comparator.comparing(Comment::createdUtc, Comparator.nullsFirst(Long::compareTo)))
                    .map(comment -> {
                        boolean isNew = redisCommentCache.isCommentNew(jedis, comment);
                        redisCommentCache.markCommentAsProcessed(jedis, comment);
                        return isNew
                                ? commentRecordBuilder.buildSourceRecordOfTypeCreate(comment)
                                : commentRecordBuilder.buildSourceRecordOfTypeUpdate(comment);
                    })
                    .toList();
        }
    }
}
