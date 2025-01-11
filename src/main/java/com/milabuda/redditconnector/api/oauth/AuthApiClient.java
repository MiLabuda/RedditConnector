package com.milabuda.redditconnector.api.oauth;

import com.milabuda.redditconnector.RedditSourceConfig;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface AuthApiClient {

    @RequestLine("POST /api/v1/access_token")
    @Headers({"Content-Type: application/x-www-form-urlencoded", "User-Agent: {userAgent}"})
    OAuthData getRedditToken(@Param("grant_type") String grantType, @Param("userAgent") String userAgent);

    default OAuthData getRedditToken(RedditSourceConfig config) {
        return getRedditToken(config.getGrantType(), config.getUserAgent());
    }

}
