package com.github.ynverxe.hexserver.helper;

import com.github.ynverxe.hexserver.helper.run.RunHexServerTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class HexPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.getTasks().register("generateLauncher", GenerateLauncherTask.class);
    project.getTasks().register("runHexServer", RunHexServerTask.class);
  }
}