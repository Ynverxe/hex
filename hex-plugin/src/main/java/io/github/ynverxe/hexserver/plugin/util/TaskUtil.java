package io.github.ynverxe.hexserver.plugin.util;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskReference;
import org.gradle.composite.internal.IncludedBuildTaskReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TaskUtil {

  private TaskUtil() {
  }

  public static <T extends Task> @NotNull T checkIsInstance(Task task, Class<T> expected) {
    if (!expected.isInstance(task)) {
      throw new IllegalArgumentException("Task '" + task.getPath() + "' isn't an instance of " + expected.getName());
    }

    return expected.cast(task);
  }

  public static Task resolveTask(Project project, Object task) {
    TaskContainer tasks = project.getTasks();
    return switch (task) {
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
      default -> throw new RuntimeException("Cannot resolve task using '" + task + "'");
    };
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