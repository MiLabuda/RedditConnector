package com.milabuda.redditconnector.api.client;

import com.milabuda.redditconnector.api.model.Envelope;
import com.milabuda.redditconnector.api.model.Listing;
import com.milabuda.redditconnector.api.model.Post;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

import java.util.Map;

public interface PostApiClient {

    @RequestLine("GET /r/{subreddit}/new.json")
    @Headers({"Content-Type: application/json", "User-Agent: {userAgent}"})
    Envelope<Listing<Post>> getPosts(
            @Param("subreddit") String subreddit,
            @Param("userAgent") String userAgent,
            @QueryMap Map<String, Object> queryMap);

}
