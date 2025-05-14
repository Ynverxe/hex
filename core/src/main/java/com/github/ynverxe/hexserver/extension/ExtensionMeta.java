package com.github.ynverxe.hexserver.extension;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents information of an extension, some of them
 * defined by an {@link com.github.ynverxe.hexserver.extension.internal.ExtensionManifest}.
 *
 * @see com.github.ynverxe.hexserver.extension.internal.SimpleExtensionMeta
 */
@ApiStatus.NonExtendable
public interface ExtensionMeta {

  /**
   * @return the name of the extension.
   */
  @NotNull String name();

  /**
   * @return the version of the extension.
   */
  @NotNull String version();

  /**
   * @return the authors of the extension.
   */
  @NotNull List<String> authors();

  /**
   * @return the working directory of the extension, which is the extensions
   * folder + the extension name.
   */
  @NotNull Path directory();

  /**
   * @return the absolute path of the extension JAR.
   */
  @NotNull Path sourceJar();

  /**
   * @return the hard dependencies of the extension.
   */
  @NotNull List<String> dependencies();

  /**
   * @return the soft dependencies of the extension.
   */
  @NotNull List<String> softDependencies();

  /**
   * @return the hard and soft dependencies of the extension.
   */
  @NotNull List<String> fullDependencies();

}