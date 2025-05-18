package com.github.ynverxe.hexserver.launcher.test.system;

import com.github.ynverxe.hexserver.launcher.util.ConnectionMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SetupGithubToken {

  private static final Logger LOGGER = LoggerFactory.getLogger(SetupGithubToken.class);

  static {
    String githubToken = System.getenv("GITHUB_TOKEN");

    if (githubToken != null) {
      ConnectionMaker.addConfigurator(urlConnection -> urlConnection.setRequestProperty("Authorization", "Bearer " + githubToken));
      LOGGER.info("GITHUB_TOKEN configured.");
    }
  }

  private SetupGithubToken() {
  }

  public static void setup() {}
}