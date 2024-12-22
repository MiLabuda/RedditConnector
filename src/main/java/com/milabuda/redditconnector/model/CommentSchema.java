//package com.milabuda.redditconnector.model;
//import org.apache.kafka.connect.data.Schema;
//import org.apache.kafka.connect.data.SchemaBuilder;
//import org.apache.kafka.connect.data.Timestamp;
//
//public class CommentSchema {
//
//    public static final String POST_ID_FIELD = "post_id";
//    public static final String COMMENT_ID_FIELD = "comment_id";
//    public static final String AUTHOR_FIELD = "author";
//    public static final String TEXT_FIELD = "text";
//    public static final String SCORE_FIELD = "score";
//    public static final String CREATED_AT_FIELD = "created_at";
//
//    public static final String SCHEMA_KEY = "com.milabuda.redditconnector.api.modelCommentKey";
//    public static final String SCHEMA_VALUE = "com.milabuda.redditconnector.api.modelCommentValue";
//
//    public static final Schema KEY_SCHEMA = SchemaBuilder.struct().name(SCHEMA_KEY)
//            .version(1)
//            .field(POST_ID_FIELD, Schema.STRING_SCHEMA)
//            .build();
//
//    public static final Schema VALUE_SCHEMA = SchemaBuilder.struct().name(SCHEMA_VALUE)
//            .version(1)
//            .field(COMMENT_ID_FIELD, Schema.STRING_SCHEMA)
//            .field(AUTHOR_FIELD, Schema.STRING_SCHEMA)
//            .field(TEXT_FIELD, Schema.STRING_SCHEMA)
//            .field(SCORE_FIELD, Schema.INT32_SCHEMA)
//            .field(CREATED_AT_FIELD, Timestamp.SCHEMA)
//            .build();
//}