package com.github.ynverxe.hexserver.helper.run;

import org.gradle.api.Project;
import org.gradle.api.Task;

import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.composite.internal.IncludedBuildTaskReference;
import org.gradle.jvm.tasks.Jar;
import org.gradle.work.DisableCachingByDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@DisableCachingByDefault
public class RunHexServerTask extends JavaExec {

  private final Property<Object> serverJarProviderTask;
  private final RegularFileProperty serverJarFile;

  public RunHexServerTask() {
    Project project = getProject();

    this.serverJarFile = project.getObjects().fileProperty();
    this.serverJarProviderTask = project.getObjects().property(Object.class);
  }

  @TaskAction
  @Override
  public void exec() {
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

  public void serverJarProviderTask(@NotNull Object task) {
    Objects.requireNonNull(task);

    if (task instanceof String || task instanceof Task || task instanceof IncludedBuildTaskReference) {
      dependsOn(task);
      this.serverJarProviderTask.set(task);
    } else {
      throw new IllegalArgumentException("Invalid type '" + task.getClass() + "'");
    }
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
    if (this.serverJarProviderTask.isPresent()) {
      Object task = this.serverJarProviderTask.get();

      TaskContainer tasks = getProject().getTasks();
      Task realTask = switch (task) {
        case String s -> tasks.getByPath(s);
        case Task task1 -> task1;
        case TaskReference ignored -> {
          if (task instanceof IncludedBuildTaskReference taskReference) {
            TaskCatcher catcher = new TaskCatcher();
            taskReference.visitDependencies(catcher);

            yield catcher.task;
          } else {
            yield tasks.getByName(((TaskReference) task).getName());
          }
        }
        default -> throw new RuntimeException("Cannot resolve task");
      };

      if (!(realTask instanceof Jar jarTask)) {
        throw new IllegalArgumentException("Task '" + realTask.getPath() + "' isn't an instance of org.gradle.jvms.tasks.Jar");
      }

      return jarTask.getArchiveFile().get().getAsFile();
    } else if (this.serverJarFile.isPresent()) {
      return this.serverJarFile.getAsFile().get();
    } else {
      throw new IllegalStateException("No source for server jar provided");
    }
  }

  private static class TaskCatcher implements TaskDependencyResolveContext {

    private Task task;

    @Override
    public void add(Object dependency) {
      this.task = (Task) dependency;
    }

    @Override
    public void visitFailure(Throwable failure) {

    }

    @Override
    public @Nullable Task getTask() {
      return null;
    }
  }

}