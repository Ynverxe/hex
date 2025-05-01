package com.github.ynverxe.hexserver;

import org.gradle.api.Task;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileTree;
import org.gradle.api.initialization.IncludedBuild;
import org.gradle.api.tasks.TaskReference;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.nio.file.Path;

public class ConfigureGeneratorSettingsTask extends Jar {

  public ConfigureGeneratorSettingsTask() {
    TaskReference launcherShadowJarTask = getProject().getGradle()
        .includedBuild("hex-server")
        .task(":launcher:shadowJar");

    dependsOn(launcherShadowJarTask);
    Task processResourcesTask = getProject().getTasks().getByName("processResources");
    dependsOn(processResourcesTask);

    IncludedBuild hexServerBuild = getProject().getGradle()
        .includedBuild("hex-server");

    Path launcherBuildDir = hexServerBuild.getProjectDir().toPath()
        .resolve("launcher")
        .resolve("build")
        .resolve("libs");

    Path launcherJar = launcherBuildDir.resolve("launcher-all.jar");

    for (File file : processResourcesTask.getOutputs().getFiles()) {
      from(file);
    }

    FileTree files = getProject().zipTree(launcherJar);
    from(files);

    // Merge manifests
    manifest(manifest -> files.getFiles().stream()
        .filter(file -> file.getName().equals("MANIFEST.MF"))
        .findFirst()
        .ifPresent(manifest::from));

    // Jar task excludes META-INF from external jars into final copies
    metaInf(copySpec -> {
      files.getFiles().stream()
          .filter(file -> file.getAbsolutePath().contains("META-INF"))
          .filter(file -> !file.getName().equals("MANIFEST.MF"))
          .forEach(copySpec::from);
    });

    getArchiveFileName().convention(getProject().getName() + "-launcher.jar");

    setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
  }
}