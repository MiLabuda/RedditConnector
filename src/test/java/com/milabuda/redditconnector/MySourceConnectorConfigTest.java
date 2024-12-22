package com.milabuda.redditconnector;

import org.junit.jupiter.api.Test;

public class MySourceConnectorConfigTest {
  @Test
  public void doc() {
    System.out.println(RedditSourceConfig.config().toRst());
  }
}