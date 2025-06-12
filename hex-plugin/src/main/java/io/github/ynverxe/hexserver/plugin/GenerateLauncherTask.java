package io.github.ynverxe.hexserver.plugin;

import io.github.ynverxe.hexserver.plugin.file.ServerFilesHandler;
import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.Task;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileTree;
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
@Incubating
public class GenerateLauncherTask extends Jar {

  private static final String LAUNCHER_ID = "hex-launcher";
  private static final String LAUNCHER_PATHNAME = "launcher";
  private static final String SHADOW_JAR_PATH = ":%1$s:shadowJar".formatted(LAUNCHER_ID);

  private @Nullable ServerFilesHandler serverFilesHandler;
  private @Nullable Jar jarTask;

  private @Nullable FileTree fatJarFiles;

  public GenerateLauncherTask() {
    setGroup("build");

    getArchiveFileName().convention(getProject().getName() + "-launcher.jar");
    setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);

    Task processResourcesTask = getProject().getTasks().getByName("processResources");
    dependsOn(processResourcesTask);

    Callable<FileTree> lazyFileTree = this::resolveFatJarFiles;

    // add launcher-all.jar
    from(lazyFileTree);

    // add resources from the current project
    from(processResourcesTask);

    into("server-files", copySpec -> copySpec.from((Callable<List<File>>) () -> {
      if (this.serverFilesHandler != null) {
        return this.serverFilesHandler.mapFiles(this);
      }

      return Collections.emptyList();
    }));

    // Merge manifests
    manifest(manifest -> manifest.from((Callable<?>) () -> {
      FileTree files = resolveFatJarFiles();
      return files.filter(file -> file.getName().equals("MANIFEST.MF"))
          .getSingleFile();
    }));

    // Jar task excludes META-INF from external jars into final copies
    metaInf(copySpec -> {
      copySpec.from((Callable<?>) () -> {
        FileTree files = resolveFatJarFiles();

        return files.filter(file -> file.getAbsolutePath().contains("META-INF") && !file.getName().equals("MANIFEST.MF"));
      });
    });

    dependsOn((Callable<?>) () -> {
      if (this.jarTask != null) return this.jarTask;

      return getProject().getGradle()
          .includedBuild("hex-server")
          .task(SHADOW_JAR_PATH);
    });
  }

  private FileTree resolveFatJarFiles() {
    if (this.fatJarFiles != null) return this.fatJarFiles;

    Path pathToLauncher;

    if (this.jarTask == null) {
      pathToLauncher = getProject().getGradle().includedBuild("hex-server").getProjectDir().toPath()
          .resolve(LAUNCHER_PATHNAME)
          .resolve("build")
          .resolve("libs")
          .resolve("launcher-all.jar");
    } else {
      pathToLauncher = this.jarTask.getArchiveFile().get().getAsFile().toPath();
    }

    // lazy file tree
    return this.fatJarFiles = getProject().zipTree(pathToLauncher);
  }

  public void serverFiles(@NotNull Action<ServerFilesHandler> action) {
    action.execute(this.serverFilesHandler = new ServerFilesHandler());
  }

  public void overrideJarTask(@Nullable Jar jarTask) {
    this.jarTask = jarTask;
  }

  public void overrideJarTask(@NotNull String path) {
    overrideJarTask((Jar) getProject().getTasks().getByPath(path));
  }
}