package com.milabuda.redditconnector;


import com.milabuda.redditconnector.api.client.CommentClientFactory;
import com.milabuda.redditconnector.api.client.CommentManager;
import com.milabuda.redditconnector.api.client.InitialFullScanState;
import com.milabuda.redditconnector.api.client.PostClientFactory;
import com.milabuda.redditconnector.api.client.PostManager;
import com.milabuda.redditconnector.api.client.SubmissionUpdateScheduler;
import com.milabuda.redditconnector.api.oauth.AuthClient;
import com.milabuda.redditconnector.api.oauth.AuthClientFactory;
import com.milabuda.redditconnector.api.oauth.AuthManager;
import com.milabuda.redditconnector.api.oauth.InMemoryTokenStore;
import com.milabuda.redditconnector.api.oauth.TokenStore;
import com.milabuda.redditconnector.redis.RedisApiCallsQueue;
import com.milabuda.redditconnector.sourcerecord.transformer.CommentTransformer;
import com.milabuda.redditconnector.sourcerecord.transformer.PostTransformer;
import com.milabuda.redditconnector.util.SimpleDIContainer;

import java.util.Map;

public class DependencyConfigurator {

    public static void configure(SimpleDIContainer container, Map<String, String> props) {
        container.register(RedditSourceConfig.class, () -> new RedditSourceConfig(props));
        container.register(AuthClientFactory.class, AuthClientFactory::new);
        container.register(TokenStore.class, InMemoryTokenStore::new);
        container.register(InitialFullScanState.class, InitialFullScanState::getInstance);
        container.register(RedisApiCallsQueue.class, RedisApiCallsQueue::new);
        container.register(SubmissionUpdateScheduler.class, SubmissionUpdateScheduler::new);

        container.register(AuthClient.class, () -> {
            container.resolve(AuthClientFactory.class);
            return AuthClientFactory.getInstance(container.resolve(RedditSourceConfig.class));
        });

        container.register(AuthManager.class,
                () -> new AuthManager(
                        container.resolve(RedditSourceConfig.class),
                        container.resolve(AuthClient.class),
                        container.resolve(TokenStore.class)
                ));

        container.register(PostClientFactory.class,
                () -> new PostClientFactory(
                        container.resolve(AuthManager.class)
                ));

        container.register(PostManager.class,
                () -> new PostManager(
                        container.resolve(RedditSourceConfig.class),
                        container.resolve(PostClientFactory.class),
                        container.resolve(InitialFullScanState.class)
                ));

        container.register(CommentClientFactory.class,
                () -> new CommentClientFactory(
                        container.resolve(AuthManager.class)
                ));

        container.register(CommentManager.class,
                () -> new CommentManager(
                        container.resolve(RedditSourceConfig.class),
                        container.resolve(CommentClientFactory.class),
                        container.resolve(RedisApiCallsQueue.class),
                        container.resolve(SubmissionUpdateScheduler.class)
                ));

        container.register(PostTransformer.class, PostTransformer::new);
        container.register(CommentTransformer.class, CommentTransformer::new);
    }
}