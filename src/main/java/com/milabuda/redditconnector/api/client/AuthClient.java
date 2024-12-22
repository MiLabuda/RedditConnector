package com.milabuda.redditconnector.api.client;

import com.milabuda.redditconnector.api.model.RedditToken;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface AuthClient {

    @RequestLine("POST /api/v1/access_token")
    @Headers({"Content-Type: application/x-www-form-urlencoded", "User-Agent: {userAgent}"})
    RedditToken getRedditToken(@Param("grant_type") String grantType, @Param("userAgent") String userAgent);
}
