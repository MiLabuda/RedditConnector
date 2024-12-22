package com.milabuda.redditconnector.api.client;

import com.milabuda.redditconnector.api.model.Listing;
import com.milabuda.redditconnector.api.model.Post;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

import java.util.Map;

public interface PostClient {

    @RequestLine("GET /r/{subreddit}/new.json")
    @Headers({"Content-Type: application/json", "Authorization: Bearer {token}", "User-Agent: {userAgent}"})
    Listing<Post> getPosts(@Param("subreddit") String subreddit,
                           @Param("token") String token,
                           @Param("userAgent") String userAgent,
                           @QueryMap Map<String, Object> queryMap);

}
