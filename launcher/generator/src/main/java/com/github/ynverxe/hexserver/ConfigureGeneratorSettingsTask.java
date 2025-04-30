package com.github.ynverxe.hexserver;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;

public class ConfigureGeneratorSettingsTask extends DefaultTask {

  private final Property<String> filename;

  public ConfigureGeneratorSettingsTask() {
    Project project = getProject();
    this.filename = project.getObjects().property(String.class);

    doFirst(task -> configureSystemProperties(project));

    finalizedBy(":triggerGeneratorTask");
  }

  @Input
  @Optional
  public Property<String> getFilename() {
    return filename;
  }

  private void configureSystemProperties(Project project) {
    String outputDir = project.getLayout().getBuildDirectory()
        .get()
        .getAsFile().getAbsolutePath();

    System.setProperty("hex.generator.output.dir", outputDir);

    SourceSet mainSources = ((SourceSetContainer) project.getExtensions().getByName("sourceSets"))
        .named("main")
        .get();

    File resources = mainSources.getResources().getSrcDirs()
        .stream().findFirst()
        .orElse(null);

    if (resources == null) {
      return;
    }

    System.setProperty("hex.generator.input.extra", resources.getAbsolutePath());

    if (filename.isPresent()) {
      System.setProperty("hex.generator.output.filename", filename.get());
    }
  }
}