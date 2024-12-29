//package com.milabuda.redditconnector.model;
//import org.apache.kafka.connect.data.Schema;
//import org.apache.kafka.connect.data.SchemaBuilder;
//
//public class UserSchema {
//
//    public static final String USER_ID_FIELD = "user_id";
//    public static final String USERNAME_FIELD = "username";
//    public static final String SUBSCRIBED_TO_FIELD = "subscribed_to";
//    public static final String POST_COUNT_FIELD = "post_count";
//    public static final String COMMENT_COUNT_FIELD = "comment_count";
//    public static final String CREATED_AT_FIELD = "created_at";
//
//    public static final String SCHEMA_KEY = "com.milabuda.redditconnector.api.model.UserKey";
//    public static final String SCHEMA_VALUE = "com.milabuda.redditconnector.api.model.UserValue";
//
//    public static final Schema KEY_SCHEMA = SchemaBuilder.struct().name(SCHEMA_KEY)
//            .version(1)
//            .field(USER_ID_FIELD, Schema.STRING_SCHEMA)
//            .build();
//
//    public static final Schema VALUE_SCHEMA = SchemaBuilder.struct().name(SCHEMA_VALUE)
//            .version(1)
//            .field(USERNAME_FIELD, Schema.STRING_SCHEMA)
//            .field(SUBSCRIBED_TO_FIELD, SchemaBuilder.array(Schema.STRING_SCHEMA))
//            .field(POST_COUNT_FIELD, Schema.INT32_SCHEMA)
//            .field(COMMENT_COUNT_FIELD, Schema.INT32_SCHEMA)
//            .field(CREATED_AT_FIELD, Schema.INT64_SCHEMA)  // Unix timestamp
//            .build();
//}