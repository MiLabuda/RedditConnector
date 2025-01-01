package com.milabuda.redditconnector.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.milabuda.redditconnector.api.model.PostAndCommentData;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

import java.util.List;
import java.util.Map;

public interface CommentClient {

    @RequestLine("GET /r/{subreddit}/comments/{postId}.json")
    @Headers({"Content-Type: application/json", "Authorization: Bearer {token}", "User-Agent: {userAgent}"})
    JsonNode getPostWithComments(
            @Param("subreddit") String subreddit,
            @Param("postId") String postId,
            @Param("token") String token,
            @Param("userAgent") String userAgent,
            @QueryMap Map<String, Object> queryMap);
}
