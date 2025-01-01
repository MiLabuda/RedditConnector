package com.milabuda.redditconnector.api.model;

public record PostAndCommentData(
        Envelope<Listing<Post>> post,
        Envelope<Listing<Comment>> comments
) {}