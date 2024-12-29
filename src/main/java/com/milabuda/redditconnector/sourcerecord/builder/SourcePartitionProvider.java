package com.milabuda.redditconnector.sourcerecord.builder;

import java.util.HashMap;
import java.util.Map;

public class SourcePartitionProvider {
    public static final String SUBREDDIT_FIELD = "subreddit";

    private final String subreddit;

    public SourcePartitionProvider(String subreddit) {
        this.subreddit = subreddit;
    }

    public Map<String, String> getSourcePartition() {
        Map<String, String> map = new HashMap<>();
        map.put(SUBREDDIT_FIELD, subreddit);
        return map;
    }
}