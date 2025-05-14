package com.github.ynverxe.hexserver.helper.file;

import com.github.ynverxe.hexserver.helper.GenerateLauncherTask;
import com.github.ynverxe.hexserver.helper.configuration.ConfigurationNodeBuilder;
import org.gradle.api.Action;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ServerFilesHandler {

  private final List<FallbackFileHandler> fallbackFileHandlers = new ArrayList<>();

  public List<File> mapFiles(@NotNull GenerateLauncherTask task) {
    File tempDir = task.getTemporaryDir();

    return this.fallbackFileHandlers.stream()
        .map(fallbackFileHandler -> {
          File tempFile = new File(tempDir, fallbackFileHandler.relativeFilePath);

          HoconConfigurationLoader loader = newLoader(tempFile);
          try {
            loader.save(fallbackFileHandler.node);
          } catch (ConfigurateException e) {
            throw new RuntimeException(e);
          }

          return tempFile;
        })
        .toList();
  }

  private @NotNull HoconConfigurationLoader newLoader(@NotNull File file) {
    return HoconConfigurationLoader.builder()
        .file(file)
        .build();
  }

  public <T extends ConfigurationNodeBuilder> void handleConfiguration(
      @NotNull DefaultConfiguration<T> configuration, @NotNull Action<T> configurator) {
    T builder = configuration.factory().get();
    configurator.execute(builder);

    this.fallbackFileHandlers.add(new FallbackFileHandler(configuration.filePath(), builder.buildCopy()));
  }

  private static class FallbackFileHandler {
    private final String relativeFilePath;
    private final ConfigurationNode node;

    public FallbackFileHandler(String relativeFilePath, ConfigurationNode node) {
      this.relativeFilePath = relativeFilePath;
      this.node = node;
    }
  }
}