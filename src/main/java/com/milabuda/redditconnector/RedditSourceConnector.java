package com.milabuda.redditconnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.milabuda.redditconnector.util.VersionUtil;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedditSourceConnector extends SourceConnector {


  private static final Logger log = LoggerFactory.getLogger(RedditSourceConnector.class);
  private RedditSourceConfig config;

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }

  @Override
  public void start(Map<String, String> map) {
    log.info("Starting RedditSourceConnector");
    try {
      config = setupSourcePropertiesWithDefaultsIfMissing(map);
    } catch (ConfigException e) {
      throw new ConnectException("Couldn't start RedditSourceConnector due to configuration error");
    }
  }

  private RedditSourceConfig setupSourcePropertiesWithDefaultsIfMissing(Map<String, String> props) throws ConfigException {
    return new RedditSourceConfig(props).returnPropertiesWithDefaultsValuesIfMissing();
  }

  @Override
  public Class<? extends Task> taskClass() {
    //TODO: Return your task implementation.
    return RedditSourceTask.class;
  }

  @Override
  public List<Map<String, String>> taskConfigs(int maxTasks) {
    if (maxTasks != 1) {
        log.info("Ignoring maxTasks setting of {} since RedditSourceConnector only supports one task.", maxTasks);
    }
    List<Map<String, String>> configs = new ArrayList<>(1);
    configs.add(config.originalsStrings());
    return configs;
  }

  @Override
  public void stop() {
    //TODO: Do things that are necessary to stop your connector.
  }

  @Override
  public ConfigDef config() {
    return RedditSourceConfig.config();
  }
}
