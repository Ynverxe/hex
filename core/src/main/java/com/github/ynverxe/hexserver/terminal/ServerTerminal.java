package com.github.ynverxe.hexserver.terminal;

import com.github.ynverxe.hexserver.HexServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minestom.server.ServerProcess;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandParser;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ServerTerminal implements Runnable {

  private static final AtomicReference<ServerTerminal> INSTANCE = new AtomicReference<>(null);
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerTerminal.class);

  private final HexServer hexServer = HexServer.instance();

  private boolean alive = true;

  private final Terminal terminal;
  private final LineReader lineReader;

  private ServerTerminal() {
    this.hexServer.addShutdownListener(hexServer -> this.shutdown());

    try {
      this.terminal = TerminalBuilder.builder()
          .system(true)
          .dumb(true)
          .build();

      this.lineReader = LineReaderBuilder.builder()
          .terminal(this.terminal)
          .completer(new Completer(this.hexServer))
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    Thread thread = Thread.ofVirtual().unstarted(this);
    thread.setName("ServerTerminalThread");
    thread.setDaemon(true);
    thread.start();
  }

  @Override
  public void run() {
    while (alive) {
      try {
        String input = lineReader.readLine();
        submitInput(input);
      } catch (UserInterruptException | EndOfFileException ignore) {
      } catch (Exception e) {
        LOGGER.error("Cannot read input", e);
      }
    }
  }

  private void shutdown() {
    alive = false;
    INSTANCE.set(null);
    try {
      terminal.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void submitInput(String input) {
    ServerProcess process = this.hexServer.process();
    process.scheduler()
        .buildTask(() -> {
          CommandManager commandManager = process.command();
          commandManager.execute(commandManager.getConsoleSender(), input);
        }).schedule();
  }

  public static void init() {
    INSTANCE.getAndUpdate(serverTerminal -> {
      if (serverTerminal != null)
        throw new IllegalStateException("Cannot override instance");

      return new ServerTerminal();
    });
  }

  private record Completer(HexServer hexServer) implements org.jline.reader.Completer {

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
      CommandManager commandManager = this.hexServer.process().command();
      String input = line.line();

      if (input.contains(" ")) {
        CommandSender sender = commandManager.getConsoleSender();
        CommandParser.Result result = commandManager.parseCommand(sender, line.line());
        Suggestion suggestion = result.suggestion(sender);
        if (suggestion != null) {
          for (SuggestionEntry entry : suggestion.getEntries()) {
            String value = entry.getEntry();
            Component tooltip = Objects.requireNonNullElse(entry.getTooltip(), Component.empty());

            candidates.add(new Candidate(value, value, null, renderAndSerialize(tooltip), null, null, true, 0));
          }
        }
      } else {
        for (@NotNull Command command : commandManager.getCommands()) {
          String name = command.getName();
          if (name.startsWith(input)) {
            candidates.add(new Candidate(name));
          }
        }
      }
    }

    private String renderAndSerialize(Component component) {
      Component rendered = GlobalTranslator.render(component, MinestomAdventure.getDefaultLocale());
      return ANSIComponentSerializer.ansi().serialize(rendered);
    }
  }
}