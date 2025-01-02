package com.milabuda.redditconnector.sourcerecord.builder;

import com.milabuda.redditconnector.api.model.Comment;
import com.milabuda.redditconnector.sourcerecord.schema.CommentSchema;
import com.milabuda.redditconnector.sourcerecord.schema.EventType;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.header.Headers;
import org.apache.kafka.connect.source.SourceRecord;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommentRecordBuilder {

    private static final String COMMENT_KAFKA_TOPIC = "reddit-comments";
    public static final String EVENT_TYPE_FIELD = "event_type";
    private final Map<String, String> sourcePartition = new HashMap<>();
    private final Map<String, String> sourceOffset = new HashMap<>();

    public SourceRecord buildSourceRecordOfTypeCreate(Comment comment) {
        return buildSourceRecord(comment, EventType.CREATE);
    }

    public SourceRecord buildSourceRecordOfTypeUpdate(Comment comment) {
        return buildSourceRecord(comment, EventType.UPDATE);
    }

    private SourceRecord buildSourceRecord(Comment comment, EventType eventType) {
        Headers headers = new ConnectHeaders();
        headers.add(EVENT_TYPE_FIELD, eventType.toString(), Schema.STRING_SCHEMA);

        return new SourceRecord(
                sourcePartition,
                sourceOffset,
                COMMENT_KAFKA_TOPIC,
                null,
                CommentSchema.KEY_SCHEMA,
                buildRecordKey(comment),
                CommentSchema.VALUE_SCHEMA,
                buildRecordValue(comment),
                Instant.now().toEpochMilli(),
                headers);
    }

    private Struct buildRecordKey(Comment comment) {
        return new Struct(CommentSchema.KEY_SCHEMA)
                .put(CommentSchema.POST_ID_FIELD, getPostId(comment));
    }

    private Struct buildRecordValue(Comment comment) {
        return new Struct(CommentSchema.VALUE_SCHEMA)
                .put(CommentSchema.ID_FIELD, getId(comment))
                .put(CommentSchema.COMMENT_ID_FIELD, comment.id())
                .put(CommentSchema.AUTHOR_FIELD, comment.author())
                .put(CommentSchema.BODY_FIELD, comment.body())
                .put(CommentSchema.SUBREDDIT_ID_FIELD, comment.subredditId())
                .put(CommentSchema.CREATED_AT_FIELD, comment.createdUtc())
                .put(CommentSchema.SCORE_FIELD, comment.score())
                .put(CommentSchema.UPS_COUNT_FIELD, comment.ups())
                .put(CommentSchema.DOWNS_COUNT_FIELD, comment.downs())
                .put(CommentSchema.PARENT_ID_FIELD, comment.parentId())
                .put(CommentSchema.PERMALINK_FIELD, comment.permalink())
                .put(CommentSchema.LINK_ID_FIELD, comment.linkId())
                .put(CommentSchema.IS_SUBMITTER_FIELD, comment.isSubmitter())
                .put(CommentSchema.CONTROVERSIALITY_FIELD, comment.controversiality())
                .put(CommentSchema.COLLAPSED_FIELD, comment.collapsed())
                .put(CommentSchema.NUM_REPORTS_FIELD, comment.numReports())
                .put(CommentSchema.REMOVAL_REASON_FIELD, comment.removalReason())
                .put(CommentSchema.STICKIED_FIELD, comment.stickied())
                .put(CommentSchema.GILDED_FIELD, comment.gilded())
                .put(CommentSchema.SCORE_HIDDEN_FIELD, comment.scoreHidden())
                .put(CommentSchema.AUTHOR_PREMIUM_FIELD, comment.authorPremium());
    }

    private @NotNull String getPostId(Comment comment) {
        return comment.linkId() != null ? comment.linkId().split("_")[1] : "NullKey";
    }

    private @NotNull String getId(Comment comment) {
        return comment.id() != null ? comment.id() : UUID.randomUUID().toString();
    }
}
