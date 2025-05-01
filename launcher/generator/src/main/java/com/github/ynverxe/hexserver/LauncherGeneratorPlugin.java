package com.github.ynverxe.hexserver;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class LauncherGeneratorPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.getTasks().register("configureGeneratorSettings", ConfigureGeneratorSettingsTask.class);
  }
}