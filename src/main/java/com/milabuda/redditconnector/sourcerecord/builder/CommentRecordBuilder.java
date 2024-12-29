package com.milabuda.redditconnector.sourcerecord.builder;

import com.milabuda.redditconnector.api.model.Comment;
import com.milabuda.redditconnector.api.model.Envelope;
import com.milabuda.redditconnector.sourcerecord.schema.CommentSchema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CommentRecordBuilder {

    private static final String COMMENT_KAFKA_TOPIC = "reddit-comments";

    public SourceRecord buildSourceRecord(Comment comment) {
        Map<String, String> sourcePartition = new HashMap<>();
        Map<String, String> sourceOffset = new HashMap<>();

        return new SourceRecord(
                sourcePartition,
                sourceOffset,
                COMMENT_KAFKA_TOPIC,
                null,
                CommentSchema.KEY_SCHEMA,
                buildRecordKey(comment),
                CommentSchema.VALUE_SCHEMA,
                buildRecordValue(comment),
                Instant.ofEpochSecond(comment.createdUtc()).toEpochMilli());
    }

    private Struct buildRecordKey(Comment comment) {
        return new Struct(CommentSchema.KEY_SCHEMA)
                .put(CommentSchema.POST_ID_FIELD, comment.parentId());
    }

    private Struct buildRecordValue(Comment comment) {
        return new Struct(CommentSchema.VALUE_SCHEMA)
                .put(CommentSchema.POST_ID_FIELD, comment.linkId())
                .put(CommentSchema.COMMENT_ID_FIELD, comment.id())
                .put(CommentSchema.AUTHOR_FIELD, comment.author())
                .put(CommentSchema.BODY_FIELD, comment.body())
                .put(CommentSchema.SUBREDDIT_ID_FIELD, comment.subredditId())
                .put(CommentSchema.CREATED_AT_FIELD, comment.createdUtc())
                .put(CommentSchema.SCORE_FIELD, comment.score())
                .put(CommentSchema.UPS_COUNT_FIELD, comment.ups())
                .put(CommentSchema.DOWNS_COUNT_FIELD, comment.downs())
                .put(CommentSchema.PARENT_ID_FIELD, comment.parentId())
                .put(CommentSchema.REPLIES_FIELD, comment.replies())
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
}