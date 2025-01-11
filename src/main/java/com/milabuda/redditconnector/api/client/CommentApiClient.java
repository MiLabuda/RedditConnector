package com.milabuda.redditconnector.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

import java.util.Map;

public interface CommentApiClient {

    @RequestLine("GET /r/{subreddit}/comments/{postId}.json")
    @Headers({"Content-Type: application/json", "User-Agent: {userAgent}"})
    JsonNode getPostWithComments(
            @Param("subreddit") String subreddit,
            @Param("postId") String postId,
            @Param("userAgent") String userAgent,
            @QueryMap Map<String, Object> queryMap);
}
