package com.github.ynverxe.hexserver.helper;

import com.github.ynverxe.hexserver.helper.file.ServerFilesHandler;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.initialization.IncludedBuild;
import org.gradle.api.tasks.TaskReference;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Generates a fat jar for hex-server:launcher and copies it
 * into the build dir of the applied project. This task includes
 * the resource files on the source set "main" into the final jar.
 */
public class GenerateLauncherTask extends Jar {

  private @Nullable ServerFilesHandler serverFilesHandler;

  public GenerateLauncherTask() {
    setGroup("build");

    getArchiveFileName().convention(getProject().getName() + "-launcher.jar");
    setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);

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

    // lazy file tree
    FileTree files = getProject().zipTree(launcherJar);

    // add launcher-all.jar
    from(files);

    // add resources from the current project
    from(processResourcesTask);

    into("server-files", copySpec -> copySpec.from((Callable<List<File>>) () -> {
      if (this.serverFilesHandler != null) {
        return this.serverFilesHandler.mapFiles(this);
      }

      return Collections.emptyList();
    }));

    // Merge manifests
    manifest(manifest -> {
      FileCollection foundManifests = files.filter(file -> file.getName().equals("MANIFEST.MF"));
      manifest.from((Callable<File>) foundManifests::getSingleFile);
    });

    // Jar task excludes META-INF from external jars into final copies
    metaInf(copySpec -> {
      FileCollection metaInfFiles =
          files.filter(file -> file.getAbsolutePath().contains("META-INF") && !file.getName().equals("MANIFEST.MF"));

      copySpec.from(metaInfFiles);
    });
  }

  public void serverFiles(@NotNull Action<ServerFilesHandler> action) {
    action.execute(this.serverFilesHandler = new ServerFilesHandler());
  }
}