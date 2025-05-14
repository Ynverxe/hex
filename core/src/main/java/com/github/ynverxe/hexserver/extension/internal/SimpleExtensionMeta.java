package com.github.ynverxe.hexserver.extension.internal;

import com.github.ynverxe.hexserver.extension.ExtensionMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.*;

@ApiStatus.Internal
public record SimpleExtensionMeta(@NotNull String name, @NotNull String version, @NotNull List<String> authors,
                                  @NotNull Path directory, @NotNull Path sourceJar, @NotNull List<String> dependencies,
                                  @NotNull List<String> softDependencies) implements ExtensionMeta {

  @Override
  public @NotNull List<String> fullDependencies() {
    List<String> list = new ArrayList<>();
    list.addAll(dependencies);
    list.addAll(softDependencies);
    return list;
  }

  @ApiStatus.Internal
  public static @NotNull SimpleExtensionMeta create(@NotNull ExtensionManifest manifest, @NotNull Path sourceJar, @NotNull Path directory) {
    return new SimpleExtensionMeta(
        requireNonNull(manifest.name, "name"),
        requireNonNull(manifest.version, "version"),
        requireNonNullElseGet(manifest.authors, Collections::emptyList),
        requireNonNull(directory, "directory"),
        requireNonNull(sourceJar, "sourceJar"),
        requireNonNullElseGet(manifest.dependencies, Collections::emptyList),
        requireNonNullElseGet(manifest.softDependencies, Collections::emptyList)
    );
  }
}