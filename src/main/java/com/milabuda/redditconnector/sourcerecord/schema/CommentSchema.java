package com.milabuda.redditconnector.sourcerecord.schema;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;


public class CommentSchema {

    public static final String POST_ID_FIELD = "postId";
    public static final String COMMENT_ID_FIELD = "commentId";
    public static final String AUTHOR_FIELD = "author";
    public static final String BODY_FIELD = "body";
    public static final String SUBREDDIT_ID_FIELD = "subredditId";
    public static final String CREATED_AT_FIELD = "createdAt";
    public static final String SCORE_FIELD = "score";
    public static final String UPS_COUNT_FIELD = "ups";
    public static final String DOWNS_COUNT_FIELD = "downs";
    public static final String PARENT_ID_FIELD = "parentId";
    public static final String REPLIES_FIELD = "replies";
    public static final String PERMALINK_FIELD = "permalink";
    public static final String LINK_ID_FIELD = "linkId";
    public static final String IS_SUBMITTER_FIELD = "isSubmitter";
    public static final String CONTROVERSIALITY_FIELD = "controversiality";
    public static final String COLLAPSED_FIELD = "collapsed";
    public static final String NUM_REPORTS_FIELD = "numReports";
    public static final String REMOVAL_REASON_FIELD = "removalReason";
    public static final String STICKIED_FIELD = "stickied";
    public static final String GILDED_FIELD = "gilded";
    public static final String SCORE_HIDDEN_FIELD = "scoreHidden";
    public static final String AUTHOR_PREMIUM_FIELD = "authorPremium";

    public static final Schema KEY_SCHEMA = SchemaBuilder.struct()
            .name(CommentSchema.class.getSimpleName())
            .version(1)
            .field(POST_ID_FIELD, Schema.STRING_SCHEMA)
            .build();

    public static final Schema VALUE_SCHEMA = SchemaBuilder.struct()
            .name(CommentSchema.class.getSimpleName())
            .version(1)
            .field(COMMENT_ID_FIELD, Schema.STRING_SCHEMA)
            .field(AUTHOR_FIELD, Schema.STRING_SCHEMA)
            .field(BODY_FIELD, Schema.STRING_SCHEMA)
            .field(SUBREDDIT_ID_FIELD, Schema.STRING_SCHEMA)
            .field(CREATED_AT_FIELD, Schema.INT64_SCHEMA)
            .field(SCORE_FIELD, Schema.INT32_SCHEMA)
            .field(UPS_COUNT_FIELD, Schema.INT32_SCHEMA)
            .field(DOWNS_COUNT_FIELD, Schema.INT32_SCHEMA)
            .field(PARENT_ID_FIELD, Schema.STRING_SCHEMA)
            .field(PERMALINK_FIELD, Schema.STRING_SCHEMA)
            .field(LINK_ID_FIELD, Schema.STRING_SCHEMA)
            .field(IS_SUBMITTER_FIELD, Schema.BOOLEAN_SCHEMA)
            .field(CONTROVERSIALITY_FIELD, Schema.INT32_SCHEMA)
            .field(COLLAPSED_FIELD, Schema.BOOLEAN_SCHEMA)
            .field(NUM_REPORTS_FIELD, Schema.INT32_SCHEMA)
            .field(REMOVAL_REASON_FIELD, Schema.STRING_SCHEMA)
            .field(STICKIED_FIELD, Schema.BOOLEAN_SCHEMA)
            .field(GILDED_FIELD, Schema.INT32_SCHEMA)
            .field(SCORE_HIDDEN_FIELD, Schema.BOOLEAN_SCHEMA)
            .field(AUTHOR_PREMIUM_FIELD, Schema.BOOLEAN_SCHEMA)
            .build();
}
