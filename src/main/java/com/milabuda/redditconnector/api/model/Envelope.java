package com.milabuda.redditconnector.api.model;

public record Envelope<T>(
        String kind,
        T data
) {
}
