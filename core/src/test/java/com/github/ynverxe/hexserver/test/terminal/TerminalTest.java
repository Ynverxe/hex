package com.github.ynverxe.hexserver.test.terminal;

import com.github.ynverxe.hexserver.test.TestServerInitializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TerminalTest {

  @Test
  public void testConsumeInput() throws Throwable {
    InputStream defIn = System.in;

    byte[] buffer = "some unknown command".getBytes(StandardCharsets.UTF_8);
    ByteArrayInputStream newIn = new ByteArrayInputStream(buffer);
    System.setIn(newIn);

    try {
      TestServerInitializer.startServer();

      Thread.sleep(200L); // wait to ServerTerminal to read the input

      Assertions.assertEquals(0, newIn.available(), "ServerTerminal isn't reading input from stdin");
    } finally {
      System.setIn(defIn);
    }
  }
}