package com.milabuda.redditconnector;

import com.milabuda.redditconnector.api.client.CommentManager;
import com.milabuda.redditconnector.api.client.PostManager;
import com.milabuda.redditconnector.api.model.Comment;
import com.milabuda.redditconnector.api.model.Pair;
import com.milabuda.redditconnector.api.model.Post;
import com.milabuda.redditconnector.cache.RedisManager;
import com.milabuda.redditconnector.sourcerecord.schema.EventType;
import com.milabuda.redditconnector.sourcerecord.transformer.CommentTransformer;
import com.milabuda.redditconnector.sourcerecord.transformer.PostTransformer;
import com.milabuda.redditconnector.util.DependencyConfigurator;
import com.milabuda.redditconnector.util.SimpleDIContainer;
import com.milabuda.redditconnector.util.VersionUtil;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedditSourceTask extends SourceTask {

  private static final Logger log = LoggerFactory.getLogger(RedditSourceTask.class);

  private final SimpleDIContainer container = new SimpleDIContainer();
  private PostManager postManager;
  private PostTransformer postTransformer;
  private CommentManager commentManager;
  private CommentTransformer commentTransformer;

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> props) {
    log.debug("Starting RedditSourceTask.");

    DependencyConfigurator.configure(container, props);

    postManager = container.resolve(PostManager.class);
    commentManager = container.resolve(CommentManager.class);
    postTransformer = container.resolve(PostTransformer.class);
    commentTransformer = container.resolve(CommentTransformer.class);
  }

  @Override
  public List<SourceRecord> poll(){
    List<SourceRecord> sourceRecords = new ArrayList<>();
    sourceRecords.addAll(getRedditPostsAsSourceRecords());
    sourceRecords.addAll(getRedditSubmissionsAsSourceRecords());
    return sourceRecords;
  }

  @Override
  public void stop() {
    RedisManager.closePool();
  }

  List<SourceRecord> getRedditPostsAsSourceRecords() {
    List<Post> postsResponse = postManager.retrievePosts();
    return postTransformer.transform(postsResponse, EventType.CREATE);
  }

  List<SourceRecord> getRedditSubmissionsAsSourceRecords() {
    Pair<List<Post>, List<Comment>> commentsAndPostsResponse = commentManager.retrieveCommentsAndPostUpdates();
    List<Post> posts = commentsAndPostsResponse.left();
    List<Comment> comments = commentsAndPostsResponse.right();

    List<SourceRecord> sourceRecords = new ArrayList<>();
    sourceRecords.addAll(postTransformer.transform(posts, EventType.UPDATE));
    sourceRecords.addAll(commentTransformer.transform(comments));
    return sourceRecords;
  }
}