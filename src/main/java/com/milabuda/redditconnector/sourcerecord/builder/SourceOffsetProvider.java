package com.milabuda.redditconnector.sourcerecord.builder;

import java.util.HashMap;
import java.util.Map;

public class SourceOffsetProvider {

    public static final String LAST_READ_POST = "last_read_post";

    public Map<String, String> getSourceOffset(String postId) {
        Map<String, String> map = new HashMap<>();
        map.put(LAST_READ_POST, postId);
        return map;
    }
}