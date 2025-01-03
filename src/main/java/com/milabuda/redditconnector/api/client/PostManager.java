package com.milabuda.redditconnector.api.client;

import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.RedditSourceTask;
import com.milabuda.redditconnector.api.model.Envelope;
import com.milabuda.redditconnector.api.model.Listing;
import com.milabuda.redditconnector.api.model.Post;
import com.milabuda.redditconnector.api.oauth.AuthManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostManager {

    private static final Logger log = LoggerFactory.getLogger(PostManager.class);

    private final RedditSourceConfig config;
    private final AuthManager authManager;
    private final PostClientFactory clientFactory;

    public PostManager(RedditSourceConfig config, AuthManager authManager) {
        this.config = config;
        this.authManager = authManager;
        this.clientFactory = new PostClientFactory(authManager);
    }

    public List<Post> retrievePosts() {
        boolean eligibleForFullScan = !RedditSourceTask.isInitialFullScanDone() && config.getInitialFullScan();
        List<Post> postsResponse =
                eligibleForFullScan
                        ? performInitialFullScan()
                        : getPosts();

        if (postsResponse == null) {
            log.info("No posts found. Returning empty list.");
            return Collections.emptyList();
        }
        return postsResponse;
    }

    private List<Post> performInitialFullScan() {
        log.info("Initial full scan of subreddits.");
        List<Post> postsResponse = new ArrayList<>();
        String after = null;
        int count = 0;

        while (true) {
            Listing<Post> pageRecords = fetchPaginatedRecords( after, count);

            postsResponse.addAll(pageRecords.children().stream().map(Envelope::data).toList());
            if (pageRecords.after() == null) {
                break;
            }
            after = pageRecords.after();
            count += pageRecords.children().size();

        }
        RedditSourceTask.setInitialFullScanDone();
        return postsResponse;
    }

    private Listing<Post> fetchPaginatedRecords(String after, Integer count) {
        return getPostsWithPagination(after, count).data();
    }

    private Envelope<Listing<Post>> getPostsWithPagination(String after, Integer count) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("limit", 100);
        queryParams.put("show", "all");
        queryParams.put("after", after);
        queryParams.put("count", count);

        try {
            PostClient client = clientFactory.getClient();
            return client.getPosts(
                    config.getSubreddits(),
                    authManager.getRedditToken().accessToken(),
                    config.getUserAgent(),
                    queryParams);
        } catch (Exception e) {
            log.error("Error occurred while fetching paginated posts: {}", e.getMessage());
            return null;
        }
    }

    private List<Post> getPosts() {
        try {
            PostClient client = clientFactory.getClient();
            Envelope<Listing<Post>> postsListingEnvelope = client.getPosts(
                    config.getSubreddits(),
                    authManager.getRedditToken().accessToken(),
                    config.getUserAgent(),
                    null);

            if (postsListingEnvelope == null) {
                log.error("No posts found");
                return Collections.emptyList();
            }
            return postsListingEnvelope.data().children().stream().map(Envelope::data).toList();
        } catch (Exception e) {
            log.error("Error occurred while fetching posts: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}

