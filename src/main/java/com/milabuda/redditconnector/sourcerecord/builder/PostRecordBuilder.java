package com.milabuda.redditconnector.sourcerecord.builder;

import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.api.model.Envelope;
import com.milabuda.redditconnector.api.model.Post;
import com.milabuda.redditconnector.sourcerecord.schema.PostSchema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.time.Instant;
import java.util.Map;

public class PostRecordBuilder {

    private static final String POST_KAFKA_TOPIC = "reddit-posts";

    private final RedditSourceConfig config;

    public PostRecordBuilder(RedditSourceConfig config) {
        this.config = config;
    }

    public SourceRecord buildSourceRecord(Envelope<Post> post) {

        Map<String, String> sourcePartition = new SourcePartitionProvider(config.getSubreddits()).getSourcePartition();
        Map<String, String> sourceOffset = new SourceOffsetProvider().getSourceOffset(post.data().id());

        return new SourceRecord(
                sourcePartition,
                sourceOffset,
                POST_KAFKA_TOPIC,
                null,
                PostSchema.KEY_SCHEMA,
                buildRecordKey(post),
                PostSchema.VALUE_SCHEMA,
                buildRecordValue(post),
                Instant.ofEpochSecond(post.data().createdUtc()).toEpochMilli());
    }

    private Struct buildRecordKey(Envelope<Post> post) {
        return new Struct(PostSchema.KEY_SCHEMA)
                .put(PostSchema.POST_ID_FIELD, post.data().id());
    }

    private Struct buildRecordValue(Envelope<Post> post) {
        return new Struct(PostSchema.VALUE_SCHEMA)
                .put(PostSchema.TITLE_FIELD, post.data().title())
                .put(PostSchema.SELF_TEXT_FIELD, post.data().selftext())
                .put(PostSchema.AUTHOR_FIELD, post.data().author())
                .put(PostSchema.SUBREDDIT_FIELD, post.data().subreddit())
                .put(PostSchema.SCORE_FIELD, post.data().score())
                .put(PostSchema.NUM_COMMENTS_FIELD, post.data().numComments())
                .put(PostSchema.CREATED_AT_FIELD, post.data().createdUtc())
                .put(PostSchema.UPVOTE_RATIO_FIELD, post.data().upvoteRatio());
    }
}
