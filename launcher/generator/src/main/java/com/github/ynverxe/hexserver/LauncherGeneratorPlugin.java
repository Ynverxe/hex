package com.github.ynverxe.hexserver;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class LauncherGeneratorPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    // We create a new task just to call :launcher:generateLauncher because
    // we cannot use finalizedBy on tasks from included builds
    project.getTasks().register("triggerGeneratorTask", task -> task.dependsOn(project.getGradle().includedBuild("hex-server").task(":launcher:generateLauncher")));
    project.getTasks().register("configureGeneratorSettings", ConfigureGeneratorSettingsTask.class);
  }
}