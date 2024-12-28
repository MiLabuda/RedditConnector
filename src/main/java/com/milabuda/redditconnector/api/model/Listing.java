package com.milabuda.redditconnector.api.model;

import java.util.ArrayList;
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

    public Listing<T> addAll(List<Envelope<T>> additionalChildren) {
        List<Envelope<T>> updatedChildren = new ArrayList<>(this.children);
        updatedChildren.addAll(additionalChildren);
        return new Listing<>(this.after, this.dist, this.modhash, this.geoFilter, updatedChildren, this.before);
    }

}
