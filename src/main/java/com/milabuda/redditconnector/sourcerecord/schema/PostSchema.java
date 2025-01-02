package com.milabuda.redditconnector.sourcerecord.schema;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

public class PostSchema {

    public static final String POST_ID_FIELD = "postId";
    public static final String ID_FIELD = "id";
    public static final String SELF_TEXT_FIELD = "selftext";
    public static final String TITLE_FIELD = "title";
    public static final String AUTHOR_FIELD = "author";
    public static final String SUBREDDIT_FIELD = "subreddit";
    public static final String SCORE_FIELD = "score";
    public static final String NUM_COMMENTS_FIELD = "numComments";
    public static final String CREATED_AT_FIELD = "createdAt";
    public static final String UPVOTE_RATIO_FIELD = "upvoteRatio";
    public static final String SUBREDDIT_SUBSCRIBERS_COUNT_FIELD = "subredditSubscribers";
    public static final String UPS_COUNT_FIELD = "ups";
    public static final String DOWNS_COUNT_FIELD = "downs";
    public static final String TOTAL_AWARDS_RECEIVED_COUNT_FIELD = "totalAwardsReceived";
    public static final String IS_VIDEO_FIELD = "isVideo";
    public static final String OVER_18_FIELD = "over18";
    public static final String IS_ORIGINAL_CONTENT_FIELD = "isOriginalContent";
    public static final String URL_FIELD = "url";
    public static final String PERMALINK_FIELD = "permalink";
    public static final String SUBMISSION_TYPE_FIELD = "type";

    public static final Schema KEY_SCHEMA = SchemaBuilder.struct()
            .name(PostSchema.class.getSimpleName())
            .version(1)
            .field(POST_ID_FIELD, Schema.STRING_SCHEMA)
            .build();

    public static final Schema VALUE_SCHEMA = SchemaBuilder.struct()
            .name(PostSchema.class.getSimpleName())
            .version(1)
            .field(TITLE_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(SELF_TEXT_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(AUTHOR_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(SUBREDDIT_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(SCORE_FIELD, Schema.OPTIONAL_INT32_SCHEMA)
            .field(NUM_COMMENTS_FIELD, Schema.OPTIONAL_INT32_SCHEMA)
            .field(CREATED_AT_FIELD, Schema.OPTIONAL_INT64_SCHEMA)
            .field(UPVOTE_RATIO_FIELD, Schema.OPTIONAL_FLOAT64_SCHEMA)
            .field(SUBREDDIT_SUBSCRIBERS_COUNT_FIELD, Schema.OPTIONAL_INT32_SCHEMA)
            .field(UPS_COUNT_FIELD, Schema.OPTIONAL_INT32_SCHEMA)
            .field(DOWNS_COUNT_FIELD, Schema.OPTIONAL_INT32_SCHEMA)
            .field(TOTAL_AWARDS_RECEIVED_COUNT_FIELD, Schema.OPTIONAL_INT32_SCHEMA)
            .field(IS_VIDEO_FIELD, Schema.OPTIONAL_BOOLEAN_SCHEMA)
            .field(OVER_18_FIELD, Schema.OPTIONAL_BOOLEAN_SCHEMA)
            .field(IS_ORIGINAL_CONTENT_FIELD, Schema.OPTIONAL_BOOLEAN_SCHEMA)
            .field(URL_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(PERMALINK_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .field(ID_FIELD, Schema.STRING_SCHEMA)
            .field(SUBMISSION_TYPE_FIELD, Schema.OPTIONAL_STRING_SCHEMA)
            .build();
}
