package com.milabuda.redditconnector;

import com.milabuda.redditconnector.api.client.CommentManager;
import com.milabuda.redditconnector.api.client.PostManager;
import com.milabuda.redditconnector.api.model.Comment;
import com.milabuda.redditconnector.api.model.Listing;
import com.milabuda.redditconnector.api.model.Post;
import com.milabuda.redditconnector.api.oauth.AuthManager;
import com.milabuda.redditconnector.redis.RedisManager;
import com.milabuda.redditconnector.sourcerecord.builder.CommentRecordBuilder;
import com.milabuda.redditconnector.sourcerecord.builder.PostRecordBuilder;
import com.milabuda.redditconnector.sourcerecord.transformer.CommentTransformer;
import com.milabuda.redditconnector.sourcerecord.transformer.PostTransformer;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedditSourceTask extends SourceTask {

  private static final Logger log = LoggerFactory.getLogger(RedditSourceTask.class);

  private static boolean initialFullScanDone = false;

  private AuthManager authManager;
  private RedditSourceConfig config;
  private PostManager postManager;
  private PostRecordBuilder postRecordBuilder;
  private PostTransformer postTransformer;
  private CommentManager commentManager;
  private CommentRecordBuilder commentRecordBuilder;
  private CommentTransformer commentTransformer;

  public static void setInitialFullScanDone() {
    RedditSourceTask.initialFullScanDone = true;
  }

  public static boolean isInitialFullScanDone() {
      return RedditSourceTask.initialFullScanDone;
  }

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> props) {
    log.debug("Starting RedditSourceTask.");
    config = new RedditSourceConfig(props);
    authManager = new AuthManager(config);
    postRecordBuilder = new PostRecordBuilder(config);
    postTransformer = new PostTransformer(postRecordBuilder);
    postManager = new PostManager(config, authManager);
    commentManager = new CommentManager(config, authManager);
    commentRecordBuilder = new CommentRecordBuilder();
    commentTransformer = new CommentTransformer(commentRecordBuilder);
  }

  @Override
  public List<SourceRecord> poll() throws InterruptedException {
    List<SourceRecord> sourceRecords = new ArrayList<>();
    sourceRecords.addAll(getRedditPostsAsSourceRecords());
    sourceRecords.addAll(getRedditCommentsAsSourceRecords());
    Thread.sleep(5000);
    return sourceRecords;
  }

  @Override
  public void stop() {
    RedisManager.closePool();
  }

  List<SourceRecord> getRedditPostsAsSourceRecords() {
    Listing<Post> postsResponse = postManager.retrievePosts();
    return postTransformer.transform(postsResponse);
  }

  List<SourceRecord> getRedditCommentsAsSourceRecords() {
    List<Comment> commentsResponse = commentManager.retrieveComments();
    return commentTransformer.transform(commentsResponse);
  }
}