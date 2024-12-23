package com.milabuda.redditconnector.model;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Timestamp;

public class PostSchema {

    public static final String POST_ID_FIELD = "postId";
    public static final String SELF_TEXT_FIELD = "selftext";
    public static final String TITLE_FIELD = "title";
    public static final String AUTHOR_FIELD = "author";
    public static final String SUBREDDIT_FIELD = "subreddit";
    public static final String SCORE_FIELD = "score";
    public static final String NUM_COMMENTS_FIELD = "numComments";
    public static final String CREATED_AT_FIELD = "createdAt";
    public static final String UPVOTE_RATIO_FIELD = "upvoteRatio";

    public static final Schema KEY_SCHEMA = SchemaBuilder.struct()
            .name(PostSchema.class.getSimpleName())
            .version(1)
            .field(POST_ID_FIELD, Schema.STRING_SCHEMA)
            .build();

    public static final Schema VALUE_SCHEMA = SchemaBuilder.struct()
            .name(PostSchema.class.getSimpleName())
            .version(1)
            .field(TITLE_FIELD, Schema.STRING_SCHEMA)
            .field(SELF_TEXT_FIELD, Schema.STRING_SCHEMA)
            .field(AUTHOR_FIELD, Schema.STRING_SCHEMA)
            .field(SUBREDDIT_FIELD, Schema.STRING_SCHEMA)
            .field(SCORE_FIELD, Schema.INT32_SCHEMA)
            .field(NUM_COMMENTS_FIELD, Schema.INT32_SCHEMA)
            .field(CREATED_AT_FIELD, Schema.INT64_SCHEMA)
            .field(UPVOTE_RATIO_FIELD, Schema.FLOAT64_SCHEMA)
            .build();
}
