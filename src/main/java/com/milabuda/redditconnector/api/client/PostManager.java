package com.milabuda.redditconnector.api.client;

import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.api.model.Envelope;
import com.milabuda.redditconnector.api.model.Listing;
import com.milabuda.redditconnector.api.model.Post;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class PostManager {

    private static final Logger log = LoggerFactory.getLogger(PostManager.class);

    private final RedditSourceConfig config;
    private final PostClientFactory clientFactory;
    private final InitialFullScanState initialFullScanState;
    private final RateLimiterSingleton rateLimiterSingleton;

    public PostManager(RedditSourceConfig config,
                       PostClientFactory clientFactory,
                       InitialFullScanState initialFullScanState,
                       RateLimiterSingleton rateLimiterSingleton) {
        this.config = config;
        this.clientFactory = clientFactory;
        this.initialFullScanState = initialFullScanState;
        this.rateLimiterSingleton = rateLimiterSingleton;
    }

    public List<Post> retrievePosts() {
        boolean eligibleForFullScan = !initialFullScanState.isInitialFullScanDone() && config.getInitialFullScan();
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
            Listing<Post> pageRecords = fetchPaginatedRecords(after, count);

            postsResponse.addAll(pageRecords.children().stream().map(Envelope::data).toList());
            if (pageRecords.after() == null) {
                break;
            }
            after = pageRecords.after();
            count += pageRecords.children().size();

        }
        initialFullScanState.markInitialFullScanDone();
        return postsResponse;
    }

    private Listing<Post> fetchPaginatedRecords(String after, Integer count) {
        return Objects.requireNonNull(getPostsWithPagination(after, count)).data();
    }

    private Envelope<Listing<Post>> getPostsWithPagination(String after, Integer count) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("limit", 100);
        queryParams.put("show", "all");
        queryParams.put("after", after);
        queryParams.put("count", count);

        try {
            PostApiClient client = clientFactory.getClient();
            return RateLimiter.decorateSupplier(
                            rateLimiterSingleton.getRateLimiter(),
                            () -> client.getPosts(
                                    config.getSubreddit(),
                                    config.getUserAgent(),
                                    queryParams)).get();

        } catch (Exception e) {
            log.error("Error occurred while fetching paginated posts", e);
            return null;
        }
    }

    private List<Post> getPosts() {
        try {
            PostApiClient client = clientFactory.getClient();
            Supplier<Envelope<Listing<Post>>> postsListingEnvelope =
                    RateLimiter.decorateSupplier(
                            rateLimiterSingleton.getRateLimiter(),
                            () -> client.getPosts(
                                    config.getSubreddit(),
                                    config.getUserAgent(),
                                    null));

            if (postsListingEnvelope.get() == null) {
                log.error("No posts found");
                return Collections.emptyList();
            }
            return postsListingEnvelope.get().data().children().stream().map(Envelope::data).toList();
        } catch (Exception e) {
            log.error("Error occurred while fetching posts: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}

