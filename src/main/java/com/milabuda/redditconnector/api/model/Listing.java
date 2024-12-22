package com.milabuda.redditconnector.api.model;

import java.util.List;

public record Listing<T> (
        String after,
        Integer dist,
        String modhash,
        String geoFilter,
        List<Envelope<T>> children,
        String before
) {
    public static <T> Listing<T> empty() {
        return new Listing<>(null, null, null, null, List.of(), null);
    }
}
