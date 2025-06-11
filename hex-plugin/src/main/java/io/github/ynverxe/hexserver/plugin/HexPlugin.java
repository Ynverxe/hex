package io.github.ynverxe.hexserver.plugin;

import io.github.ynverxe.hexserver.plugin.run.RunHexServerTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class HexPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.getTasks().register("generateLauncher", GenerateLauncherTask.class);
    project.getTasks().register("runHexServer", RunHexServerTask.class);
  }
}