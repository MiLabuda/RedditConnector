package com.milabuda.redditconnector.sourcerecord.builder;

import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.api.model.Envelope;
import com.milabuda.redditconnector.api.model.Post;
import com.milabuda.redditconnector.sourcerecord.schema.EventType;
import com.milabuda.redditconnector.sourcerecord.schema.PostSchema;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.header.Headers;
import org.apache.kafka.connect.source.SourceRecord;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class PostRecordBuilder {

    private static final String POST_KAFKA_TOPIC = "reddit-posts";
    private static final String EVENT_TYPE_FIELD = "event_type";

    private final Map<String, String> sourcePartition = new HashMap<>();
    private final Map<String, String> sourceOffset = new HashMap<>();

    public SourceRecord buildSourceRecord(Post post, EventType eventType) {
        Headers headers = new ConnectHeaders();
        headers.add(EVENT_TYPE_FIELD, eventType.toString(), Schema.STRING_SCHEMA);

        return new SourceRecord(
                sourcePartition,
                sourceOffset,
                POST_KAFKA_TOPIC,
                null,
                PostSchema.KEY_SCHEMA,
                buildRecordKey(post),
                PostSchema.VALUE_SCHEMA,
                buildRecordValue(post),
                Instant.now().toEpochMilli(),
                headers);
    }

    private Struct buildRecordKey(Post post) {
        return new Struct(PostSchema.KEY_SCHEMA)
                .put(PostSchema.POST_ID_FIELD, post.id());
    }

    private Struct buildRecordValue(Post post) {
        return new Struct(PostSchema.VALUE_SCHEMA)
                .put(PostSchema.TITLE_FIELD, post.title())
                .put(PostSchema.SELF_TEXT_FIELD, post.selftext())
                .put(PostSchema.AUTHOR_FIELD, post.author())
                .put(PostSchema.SUBREDDIT_FIELD, post.subreddit())
                .put(PostSchema.SCORE_FIELD, post.score())
                .put(PostSchema.NUM_COMMENTS_FIELD, post.numComments())
                .put(PostSchema.CREATED_AT_FIELD, post.createdUtc())
                .put(PostSchema.UPVOTE_RATIO_FIELD, post.upvoteRatio());
    }
}
