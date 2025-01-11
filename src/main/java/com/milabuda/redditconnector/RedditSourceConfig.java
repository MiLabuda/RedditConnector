package com.milabuda.redditconnector;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;

import java.util.HashMap;
import java.util.Map;


public class RedditSourceConfig extends AbstractConfig {

  public static final String BASE_URL_CONFIG = "reddit.base.url";
  private static final String BASE_URL_DOC = "Reddit base URL.";
  private static final String BASE_URL_DEFAULT = "https://www.reddit.com";

  public static final String API_BASE_URL_CONFIG = "reddit.api.base.url";
  private static final String API_BASE_URL_DOC = "Reddit API base URL.";
  private static final String API_BASE_URL_DEFAULT = "https://oauth.reddit.com";

  public static final String CLIENT_ID_CONFIG = "reddit.client.id";
  private static final String CLIENT_ID_DOC = "Reddit API Client ID.";

  public static final String CLIENT_SECRET_CONFIG = "reddit.client.secret";
  private static final String CLIENT_SECRET_DOC = "Reddit API Client Secret.";

  public static final String USER_AGENT_CONFIG = "reddit.user.agent";
  private static final String USER_AGENT_DOC = "Reddit User Agent for API access.";

  public static final String SUBREDDIT_CONFIG = "reddit.subreddit";
  private static final String SUBREDDIT_DOC = "Name of subreddit to fetch.";

  public static final String LAST_READ_POST_CONFIG = "reddit.last.read.post";
  private static final String LAST_READ_POST_DOC = "Last read timestamp from Reddit API.";

  public static final String INITIAL_FULL_SCAN_CONFIG = "reddit.initial.full.scan";
  private static final String INITIAL_FULL_SCAN_DOC = "Initial full scan of the subreddits. This will allow to fetch up to 1000 posts from subreddit first.";
  private static final boolean INITIAL_FULL_SCAN_DEFAULT = true;

  public static final String POOLING_BATCH_SIZE_CONFIG = "reddit.pooling.batch.size";
  private static final String POOLING_BATCH_SIZE_DOC = "Integer value for pooling batch size.";
  private static final Integer POOLING_BATCH_SIZE_DEFAULT = 10;

  private static final String GRANT_TYPE = "client_credentials";


  public String getBaseUrl() {return this.getString(BASE_URL_CONFIG);}
  public String getApiBaseUrl() {return this.getString(API_BASE_URL_CONFIG);}
  public String getClientId() {return this.getString(CLIENT_ID_CONFIG);}
  public String getClientSecret() {return this.getString(CLIENT_SECRET_CONFIG);}
  public String getUserAgent() {return this.getString(USER_AGENT_CONFIG);}
  public String getSubreddit() {return this.getString(SUBREDDIT_CONFIG);}
  public String getLastReadPost() {return this.getString(LAST_READ_POST_CONFIG);}
  public boolean getInitialFullScan() {return this.getBoolean(INITIAL_FULL_SCAN_CONFIG);}
  public Integer getPoolingBatchSizeDefault() {return this.getInt(POOLING_BATCH_SIZE_CONFIG);}
  public String getGrantType() {return GRANT_TYPE;}


  public RedditSourceConfig(Map<String, String> props) {
    super(config(), props);
  }

  public static ConfigDef config() {
    return new ConfigDef()
            .define(BASE_URL_CONFIG, ConfigDef.Type.STRING, BASE_URL_DEFAULT, ConfigDef.Importance.HIGH, BASE_URL_DOC)
            .define(API_BASE_URL_CONFIG, ConfigDef.Type.STRING, API_BASE_URL_DEFAULT, ConfigDef.Importance.HIGH, API_BASE_URL_DOC)
            .define(CLIENT_ID_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, CLIENT_ID_DOC)
            .define(CLIENT_SECRET_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, CLIENT_SECRET_DOC)
            .define(USER_AGENT_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, USER_AGENT_DOC)
            .define(SUBREDDIT_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, SUBREDDIT_DOC)
            .define(LAST_READ_POST_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, LAST_READ_POST_DOC)
            .define(INITIAL_FULL_SCAN_CONFIG, ConfigDef.Type.BOOLEAN, INITIAL_FULL_SCAN_DEFAULT, Importance.LOW, INITIAL_FULL_SCAN_DOC)
            .define(POOLING_BATCH_SIZE_CONFIG, ConfigDef.Type.INT, POOLING_BATCH_SIZE_DEFAULT, Importance.LOW, POOLING_BATCH_SIZE_DOC);
  }

  RedditSourceConfig returnPropertiesWithDefaultsValuesIfMissing() {
    Map<String, ?> uncastProperties = this.values();
    Map<String, String> config = new HashMap<>(uncastProperties.size());
    uncastProperties.forEach((key, valueToBeCast) -> config.put(key, valueToBeCast.toString()));

    return new RedditSourceConfig(config);
  }
}
