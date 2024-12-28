package com.milabuda.redditconnector;

import com.milabuda.redditconnector.api.client.PostManager;
import com.milabuda.redditconnector.api.model.Envelope;
import com.milabuda.redditconnector.api.model.Listing;
import com.milabuda.redditconnector.api.model.Post;
import com.milabuda.redditconnector.api.oauth.AuthManager;
import com.milabuda.redditconnector.model.PostSchema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.milabuda.redditconnector.model.PostSchema.AUTHOR_FIELD;
import static com.milabuda.redditconnector.model.PostSchema.SELF_TEXT_FIELD;
import static com.milabuda.redditconnector.model.PostSchema.CREATED_AT_FIELD;
import static com.milabuda.redditconnector.model.PostSchema.NUM_COMMENTS_FIELD;
import static com.milabuda.redditconnector.model.PostSchema.SCORE_FIELD;
import static com.milabuda.redditconnector.model.PostSchema.TITLE_FIELD;
import static com.milabuda.redditconnector.model.PostSchema.UPVOTE_RATIO_FIELD;

public class RedditSourceTask extends SourceTask {
  static final Logger log = LoggerFactory.getLogger(RedditSourceTask.class);

  private static final String POST_KAFKA_TOPIC = "reddit-posts";

  public static final String LAST_READ_POST = "last_read_post";
  public static final String DEFAULT_FROM = "Full scan";
  public static final String SUBREDDIT_FIELD = "subreddit";

  private RedditSourceConfig config;
  private AuthManager authManager;
  private String nextPostAfter;
  private boolean initialFullScanDone = false;
  private final Map<String, Long> postsMap = new HashMap<>();


  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> props) {
    log.debug("Starting RedditSourceTask.");
    config = new RedditSourceConfig(props);
    log.debug("RedditSourceTask config: {}", config);
    initializeLastVariables();
    authManager = new AuthManager(config);
  }

  private void initializeLastVariables() {
    Map<String, Object> lastSourceOffset = null;
    lastSourceOffset = context.offsetStorageReader().offset(sourcePartition());
    if(lastSourceOffset == null){
      log.debug("Offset storage reader is not available, using default value: {}", DEFAULT_FROM);
      nextPostAfter = config.getLastReadPost();
    } else{
      log.debug("Offset storage reader is available.");
      Object lastReadPost = lastSourceOffset.get(LAST_READ_POST);
      if(lastReadPost instanceof String){
        nextPostAfter = (String) lastReadPost;
      }
    }
  }

  @Override
  public List<SourceRecord> poll() throws InterruptedException {
    List<SourceRecord> sourceRecords = getRedditPostsAsSourceRecords(authManager);
    Thread.sleep(5000);
    return sourceRecords;
  }

  @Override
  public void stop() {
    //TODO: Do whatever is required to stop your task.
  }

  List<SourceRecord> getRedditPostsAsSourceRecords(AuthManager authManager) {
    PostManager postManager = new PostManager(config, authManager);
    Listing<Post> postsResponse = retrievePosts(postManager);

    if (postsResponse == null) {
      log.info("No posts found. Returning empty list.");
      return Collections.emptyList();
    }

    return postsResponse
            .children()
            .stream()
            .filter(post -> !postsMap.containsKey(post.data().id()))
            .peek(post -> postsMap.put(post.data().id(), post.data().createdUtc()))
            .map(this::generatePostSourceRecord)
            .toList();
  }

  private Listing<Post> retrievePosts(PostManager postManager) {
    boolean eligibleForFullScan = !initialFullScanDone && config.getInitialFullScan();
    return eligibleForFullScan ? performInitialFullScan(postManager) : postManager.getPosts();
  }

  private Listing<Post> performInitialFullScan(PostManager postManager) {
    log.info("Initial full scan of subreddits.");
    Listing<Post> postsResponse = Listing.empty();
    String after = null;
    int count = 0;

    while (true) {
      Listing<Post> pageRecords = fetchPaginatedRecords(postManager, after, count);
      if (pageRecords.after() == null) {
        break;
      }
      postsResponse = postsResponse.addAll(pageRecords.children());
      after = pageRecords.after();
      count += pageRecords.children().size();
    }
    initialFullScanDone = true;
    return postsResponse;
  }

  private Listing<Post> fetchPaginatedRecords(PostManager postManager, String after, Integer count) {
    return postManager.getPostsWithPagination(after, count).data();
  }

  private SourceRecord generatePostSourceRecord(Envelope<Post> post) {
    return new SourceRecord(
            sourcePartition(),
            sourceOffset(post.data().id()),
            POST_KAFKA_TOPIC,
            null,
            PostSchema.KEY_SCHEMA,
            buildRecordKey(post),
            PostSchema.VALUE_SCHEMA,
            buildRecordValue(post),
            Instant.ofEpochSecond(post.data().createdUtc()).toEpochMilli());
  }

    private Struct buildRecordKey(Envelope<Post> post) {
      return new Struct(PostSchema.KEY_SCHEMA)
              .put(PostSchema.POST_ID_FIELD, post.data().id());
    }

    private Struct buildRecordValue(Envelope<Post> post) {
        return new Struct(PostSchema.VALUE_SCHEMA)
                .put(TITLE_FIELD, post.data().title())
                .put(SELF_TEXT_FIELD, post.data().selftext())
                .put(AUTHOR_FIELD, post.data().author())
                .put(SUBREDDIT_FIELD, post.data().subreddit())
                .put(SCORE_FIELD, post.data().score())
                .put(NUM_COMMENTS_FIELD, post.data().numComments())
                .put(CREATED_AT_FIELD, post.data().createdUtc())
                .put(UPVOTE_RATIO_FIELD, post.data().upvoteRatio());
    }

  private Map<String, String> sourcePartition() {
    Map<String, String> map = new HashMap<>();
    map.put(SUBREDDIT_FIELD, config.getSubreddits());
    return map;
  }

  private Map<String, String> sourceOffset(String postId) {
    Map<String, String> map = new HashMap<>();
    map.put(LAST_READ_POST, postId);
    return map;
  }
}