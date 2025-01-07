package com.milabuda.redditconnector.api.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.milabuda.redditconnector.RedditSourceConfig;
import com.milabuda.redditconnector.api.model.Comment;
import com.milabuda.redditconnector.api.model.Envelope;
import com.milabuda.redditconnector.api.model.Listing;
import com.milabuda.redditconnector.api.model.Pair;
import com.milabuda.redditconnector.api.model.Post;
import com.milabuda.redditconnector.api.model.PostAndCommentData;
import com.milabuda.redditconnector.api.oauth.AuthManager;
import com.milabuda.redditconnector.redis.RedisApiCallsQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommentManager {
    private static final Logger log = LoggerFactory.getLogger(CommentManager.class);

    private final RedditSourceConfig config;
    private final CommentClientFactory clientFactory;
    private final RedisApiCallsQueue redisApiCallsQueue;
    private final SubmissionUpdateScheduler submissionUpdateScheduler;

    public CommentManager(RedditSourceConfig config,
                          CommentClientFactory clientFactory,
                          RedisApiCallsQueue redisApiCallsQueue,
                          SubmissionUpdateScheduler submissionUpdateScheduler) {
        this.config = config;
        this.clientFactory = clientFactory;
        this.redisApiCallsQueue = redisApiCallsQueue;
        this.submissionUpdateScheduler = submissionUpdateScheduler;
    }

    public Pair<List<Post>, List<Comment>> retrieveCommentsAndPostUpdates() {
        submissionUpdateScheduler.schedulePostsForUpdate();
        String postId;
        List<Comment> allComments = new ArrayList<>();
        List<Post> allPosts = new ArrayList<>();

        while ((postId = redisApiCallsQueue.getPostReadyForProcessing()) != null) {
            PostAndCommentData postAndCommentData = getPostAndComments(postId);
            if (postAndCommentData == null) {
                continue;
            }
            int initialSize = allComments.size();
            for (Envelope<Comment> comment : postAndCommentData.comments().data().children()) {
                allComments.addAll(traverseComments(comment.data()));
            }
            int retrievedComments = allComments.size() - initialSize;
            log.info("Retrieved {} comments for postId: {}", retrievedComments, postId);


            for (Envelope<Post> post : postAndCommentData.post().data().children()) {
                allPosts.add(post.data());
                log.info("Retrieved post update for postId: {}", postId);
            }
        }
        return new Pair<>(allPosts, allComments);
    }

    private List<Comment> traverseComments(Comment comment) {
        List<Comment> commentsList = new ArrayList<>();
        commentsList.add(comment);

        if (comment.replies() != null && comment.replies().data() != null && !comment.replies().data().children().isEmpty()) {
            List<Comment> replies = comment.replies().data().children().stream()
                    .map(Envelope::data)
                    .collect(Collectors.toList());

            for (Comment reply : replies) {
                commentsList.addAll(traverseComments(reply));
            }
        }
        return commentsList;
    }

    private PostAndCommentData getPostAndComments(String postId) {
        try {
            CommentClient client = clientFactory.getClient();
            Map<String, Object> queryMap = new HashMap<>();

            JsonNode root = client.getPostWithComments(
                    config.getSubreddit(),
                    postId,
                    config.getUserAgent(),
                    queryMap);

            Envelope<Listing<Post>> post = mapJsonNode(root.get(0), new TypeReference<>() {});
            Envelope<Listing<Comment>> comments = mapJsonNode(root.get(1), new TypeReference<>() {});

            return new PostAndCommentData(post, comments);
        } catch (Exception e) {
            log.error("Error occurred while fetching comments for postId: {}", postId, e);
            return null;
        }
    }

    private <T> T mapJsonNode(JsonNode node, TypeReference<T> typeReference) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        return objectMapper.treeToValue(node, typeReference);
    }

}
