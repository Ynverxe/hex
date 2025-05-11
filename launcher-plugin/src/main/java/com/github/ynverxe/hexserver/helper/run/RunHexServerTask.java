package com.github.ynverxe.hexserver.helper.run;

import com.github.ynverxe.hexserver.helper.util.TaskUtil;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.Task;

import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;
import org.gradle.jvm.tasks.Jar;
import org.gradle.work.DisableCachingByDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Incubating
@DisableCachingByDefault
public class RunHexServerTask extends JavaExec {

  private @Nullable Jar serverJarProviderTask;
  private final RegularFileProperty serverJarFile;

  private final List<Jar> extensionTasks = new ArrayList<>();

  public RunHexServerTask() {
    Project project = getProject();

    this.serverJarFile = project.getObjects().fileProperty();
  }

  @TaskAction
  @Override
  public void exec() {
    for (Jar extensionTask : this.extensionTasks) {
      File file = extensionTask.getArchiveFile().get().getAsFile();
      args("-add-extension=" + "\"" + file.getAbsolutePath() + "\"");
    }

    File jarFile = this.resolveServerJarFile();

    if (!jarFile.getName().endsWith(".jar")) {
      throw new IllegalStateException("Provided server file isn't a jar file");
    }

    this.classpath(jarFile);

    File workingDir = this.getWorkingDir();

    if (!workingDir.exists()) {
      if (!workingDir.mkdirs()) {
        throw new RuntimeException("Cannot create directory " + workingDir);
      }
    }

    super.exec();
  }

  @Internal
  public RegularFileProperty getServerJarFile() {
    return serverJarFile;
  }

  public void addExtensionFromTask(@NotNull Object task) {
    Task resolved = TaskUtil.resolveTask(this.getProject(), task);

    this.extensionTasks.add(TaskUtil.checkIsInstance(resolved, Jar.class));
    dependsOn(resolved);
  }

  public void serverJarProviderTask(@NotNull Object task) {
    Objects.requireNonNull(task);

    Task resolved = TaskUtil.resolveTask(this.getProject(), task);

    dependsOn(resolved);
    this.serverJarProviderTask = TaskUtil.checkIsInstance(resolved, Jar.class);
  }

  public void serverJarFile(@NotNull Object jarFile) {
    Objects.requireNonNull(jarFile);

    switch (jarFile) {
      case RegularFile regularFile -> this.serverJarFile.set(regularFile);
      case File file -> this.serverJarFile.set(file);
      case Path path -> this.serverJarFile(path.toFile());
      case String s -> this.serverJarFile(Paths.get(s));
      default -> throw new IllegalArgumentException("Invalid type '" + jarFile.getClass() + "'");
    }
  }

  private File resolveServerJarFile() {
    if (this.serverJarProviderTask != null) {
      Jar task = this.serverJarProviderTask;
      return task.getArchiveFile().get().getAsFile();
    } else if (this.serverJarFile.isPresent()) {
      return this.serverJarFile.getAsFile().get();
    } else {
      throw new IllegalStateException("No source for server jar provided");
    }
  }
}