package com.milabuda.redditconnector.util;


import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.api.client.CommentService;
import com.milabuda.redditconnector.api.client.FeignClientFactoryProvider;
import com.milabuda.redditconnector.api.client.InitialFullScanState;
import com.milabuda.redditconnector.api.client.PostManager;
import com.milabuda.redditconnector.api.client.RateLimiterSingleton;
import com.milabuda.redditconnector.api.client.SubmissionUpdateScheduler;
import com.milabuda.redditconnector.api.oauth.AuthApiClient;
import com.milabuda.redditconnector.api.oauth.AuthClientFactory;
import com.milabuda.redditconnector.api.oauth.AuthManager;
import com.milabuda.redditconnector.api.oauth.InMemoryTokenStore;
import com.milabuda.redditconnector.api.oauth.TokenStore;
import com.milabuda.redditconnector.cache.RedisApiCallsQueue;
import com.milabuda.redditconnector.cache.RedisPostCache;
import com.milabuda.redditconnector.recordconverter.transformer.CommentTransformer;
import com.milabuda.redditconnector.recordconverter.transformer.PostTransformer;

import java.util.Map;

public class DependencyConfigurator {

    public static void configure(SimpleDIContainer container, Map<String, String> props) {
        container.register(RedditSourceConfig.class, () -> new RedditSourceConfig(props));
        container.register(RedisPostCache.class, RedisPostCache::new);
        container.register(TokenStore.class, InMemoryTokenStore::new);
        container.register(InitialFullScanState.class, InitialFullScanState::getInstance);
        container.register(RateLimiterSingleton.class, RateLimiterSingleton::getInstance);
        container.register(RedisApiCallsQueue.class, RedisApiCallsQueue::new);
        container.register(SubmissionUpdateScheduler.class,
                () -> new SubmissionUpdateScheduler(
                        container.resolve(RedisApiCallsQueue.class),
                        container.resolve(RedisPostCache.class),
                        container.resolve(RedditSourceConfig.class)
                ));
        container.register(AuthClientFactory.class, AuthClientFactory::new);
        container.register(AuthApiClient.class, () -> {
            container.resolve(AuthClientFactory.class);
            return AuthClientFactory.getInstance(container.resolve(RedditSourceConfig.class));
        });

        container.register(AuthManager.class,
                () -> new AuthManager(
                        container.resolve(RedditSourceConfig.class),
                        container.resolve(AuthApiClient.class),
                        container.resolve(TokenStore.class)
                ));

        container.register(FeignClientFactoryProvider.class, () -> new FeignClientFactoryProvider(
                container.resolve(AuthManager.class),
                container.resolve(RedditSourceConfig.class)
        ));

        container.register(PostManager.class,
                () -> new PostManager(
                        container.resolve(RedditSourceConfig.class),
                        container.resolve(FeignClientFactoryProvider.class).getPostClientFactory(),
                        container.resolve(InitialFullScanState.class),
                        container.resolve(RateLimiterSingleton.class)
                ));

        container.register(CommentService.class,
                () -> new CommentService(
                        container.resolve(RedditSourceConfig.class),
                        container.resolve(FeignClientFactoryProvider.class).getCommentClientFactory(),
                        container.resolve(RedisApiCallsQueue.class),
                        container.resolve(SubmissionUpdateScheduler.class),
                        container.resolve(RateLimiterSingleton.class)
                ));

        container.register(PostTransformer.class, PostTransformer::new);
        container.register(CommentTransformer.class, CommentTransformer::new);
    }
}