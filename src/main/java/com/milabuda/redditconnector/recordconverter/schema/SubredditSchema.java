//package com.milabuda.redditconnector.model;
//
//import org.apache.kafka.connect.data.Schema;
//import org.apache.kafka.connect.data.SchemaBuilder;
//
//public class SubredditSchema {
//
//    public static final String SUBREDDIT_NAME_FIELD = "subreddit_name";
//    public static final String SUBSCRIBERS_FIELD = "subscribers";
//    public static final String ACTIVE_POSTS_FIELD = "active_posts";
//    public static final String MODERATORS_FIELD = "moderators";
//    public static final String DESCRIPTION_FIELD = "description";
//    public static final String CREATED_AT_FIELD = "created_at";
//
//    public static final String SCHEMA_KEY = "com.milabuda.redditconnector.api.model.SubredditKey";
//    public static final String SCHEMA_VALUE = "com.milabuda.redditconnector.api.model.SubredditValue";
//
//    public static final Schema KEY_SCHEMA = SchemaBuilder.struct().name(SCHEMA_KEY)
//            .version(1)
//            .field(SUBREDDIT_NAME_FIELD, Schema.STRING_SCHEMA)
//            .build();
//
//    public static final Schema VALUE_SCHEMA = SchemaBuilder.struct().name(SCHEMA_VALUE)
//            .version(1)
//            .field(SUBSCRIBERS_FIELD, Schema.INT32_SCHEMA)
//            .field(ACTIVE_POSTS_FIELD, Schema.INT32_SCHEMA)
//            .field(MODERATORS_FIELD, SchemaBuilder.array(Schema.STRING_SCHEMA))
//            .field(DESCRIPTION_FIELD, Schema.STRING_SCHEMA)
//            .field(CREATED_AT_FIELD, Schema.INT64_SCHEMA) // Unix timestamp
//            .build();
//}
