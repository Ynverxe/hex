package com.github.ynverxe.hexserver.internal;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Class used to check if the parent process is alive. The launcher
 * passes its pid to the server process via system property and
 * this class checks actively if the launcher process is alive, if not
 * terminate this process. Otherwise, server process will keep alive.
 */
@ApiStatus.Internal
public final class ParentProcessChecker extends Thread {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParentProcessChecker.class);
  public static final ParentProcessChecker INSTANCE = new ParentProcessChecker();

  private long parentPid;

  private ParentProcessChecker() {
    setDaemon(true);
  }

  @Override
  public void run() {
    Optional<ProcessHandle> processHandle = ProcessHandle.of(parentPid);
    if (processHandle.isEmpty()) {
      exit();
      return;
    }

    while(true) {
      if (!processHandle.get().isAlive()) {
        exit();
        break;
      }
    }
  }

  private void exit() {
    LOGGER.info("Parent process is dead, exiting...");
    System.exit(0);
  }

  @Override
  public void start() {
    if (getState() != State.NEW) {
      return;
    }

    if (System.getenv("parent-pid") == null) return;

    this.parentPid = Long.parseLong(System.getenv("parent-pid"));
    LOGGER.info("Parent process checker started with parent pid {}", parentPid);
    super.start();
  }
}