package com.github.ynverxe.hexserver.test.server;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.test.TestServerInitializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

public class ServerShutdownTest {

  @BeforeEach
  public void startServer() throws Throwable {
    TestServerInitializer.startServer();
  }

  @Test
  public void testInstanceClearing() {
    HexServer.instance().shutdown();
    Assertions.assertFalse(HexServer.optionalInstance().isPresent(), "HexServer#INSTANCE wasn't cleared");
  }

  @Test
  public void testShutdownListeners() {
    Object completionMark = new Object();
    CompletableFuture<Object> future = new CompletableFuture<>();

    HexServer server = HexServer.instance();
    server.addShutdownListener(hexServer -> future.complete(completionMark));
    server.shutdown();

    Assertions.assertEquals(completionMark, future.join(), "Shutdown listener wasn't executed");
  }
}